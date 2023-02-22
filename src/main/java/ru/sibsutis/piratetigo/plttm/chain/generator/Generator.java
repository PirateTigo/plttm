package ru.sibsutis.piratetigo.plttm.chain.generator;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import lombok.extern.java.Log;
import ru.sibsutis.piratetigo.plttm.common.InferenceType;

import java.util.*;
import java.util.concurrent.CompletableFuture;

import static ru.sibsutis.piratetigo.plttm.common.Tools.*;

/**
 * Генератор цепочек символов на основе КС-грамматики.
 */
@Log
public class Generator {

    private final HashMap<String, Set<String>> rules;
    private final String goal;
    private final InferenceType direction;
    private final Integer minLength;
    private final Integer maxLength;
    private final Set<String> terminals;

    private final List<String> chains = new LinkedList<>();
    private final LinkedList<String> nonTerminalsUniquePath = new LinkedList<>();

    private boolean isCanceled = false;

    private boolean isRunning = false;

    public Generator(
            HashMap<String, Set<String>> rules,
            String goal,
            InferenceType direction,
            Integer minLength,
            Integer maxLength,
            Set<String> terminals
    ) {
        this.rules = rules;
        this.goal = goal;
        this.direction = direction;
        this.minLength = minLength;
        this.maxLength = maxLength;
        this.terminals = terminals;
    }

    /**
     * Создает задачу генерации цепочек.
     *
     * @param generator Генератор цепочек.
     * @param inference Пользовательский элемент управления для отображения
     * сгенерированных цепочек.
     * @param chainCount Пользовательский элемент управления для отображения
     * Количества сгенерированных цепочек.
     * @param generation Способ асинхронного отслеживания статуса
     * завершения генерации.
     */
    public static Thread createGenerationTask(
            Generator generator,
            ListView<String> inference,
            Label chainCount,
            CompletableFuture<String> generation) {
        return new Thread(new Task<List<String>>() {
            private List<String> chains = new ArrayList<>();

            @Override
            protected List<String> call() {
                generator.generate();
                chains = generator.getChains();
                return chains;
            }

            @Override
            protected void succeeded() {
                super.succeeded();
                Platform.runLater(() -> {
                    inference.getItems().setAll(chains);
                    chainCount.setText("Всего цепочек: " + chains.size());
                });
                generation.complete("Completed");
            }
        });
    }

    /**
     * Запускает генерацию цепочек символов.
     */
    public void generate() {
        isCanceled = false;
        isRunning = true;
        chains.clear();
        String chain = "";
        Set<String> mainRules = rules.get(goal);
        nonTerminalsUniquePath.push(goal);
        try {
            if (direction == InferenceType.LEFT) {
                for (String mainRule : mainRules) {
                    try {
                        add(generateChainLeft(mainRule, chain, ""));
                    } catch (MissRuleException
                             | ChainLengthExceededException
                             | LoopDetectionException ex) {
                        LOGGER.info(ex.getMessage()
                                + ". Переходим к следующему правилу");
                    }
                }
            } else {
                for (String mainRule : mainRules) {
                    try {
                        add(generateChainRight(mainRule, chain, ""));
                    } catch (MissRuleException ex) {
                        LOGGER.info(ex.getMessage()
                                + ". Переходим к следующему правилу");
                    } catch (ChainLengthExceededException ex) {
                        LOGGER.info(ex.getMessage()
                                + ". Останавливаем обработку правила");
                    } catch (LoopDetectionException ex) {
                        LOGGER.info(ex.getMessage()
                                + ". Продолжаем пропуск рекурсивной ветки. "
                                + "Возврат к обработке нетерминала '"
                                + goal + "'");
                    }
                }
            }
            nonTerminalsUniquePath.pop();
        } catch (CancelException ex) {
            nonTerminalsUniquePath.clear();
            chains.clear();
        } finally {
            isRunning = false;
        }
    }

    /**
     * Получает список сгенерированных цепочек символов.
     * <p>
     * Перед получением необходимо вызвать метод {@link #generate()}.
     */
    public List<String> getChains() {
        return chains;
    }

    /**
     * Отменяет процесс вычислений.
     */
    public void cancel() {
        isCanceled = true;
    }

    /**
     * Возвращает статус работы генератора.
     */
    public boolean isRunning() {
        return isRunning;
    }

    private String generateChainLeft(String rule, String chain, String rest) {
        if (rule.length() > 0) {
            LinkedList<String> ruleLexemes = stringToLexemes(rule);
            String handledLexeme = ruleLexemes.get(0);
            String ruleRest =
                    lexemesToString(ruleLexemes, 1, ruleLexemes.size());
            if (terminals.contains(handledLexeme)) {
                return generateChainLeft(ruleRest, chain + handledLexeme, rest);
            } else {
                String handledNonTerminal = fromLexeme(handledLexeme);
                nonTerminalsUniquePath.push(handledNonTerminal);
                long deep = nonTerminalsUniquePath.stream()
                        .filter(handledNonTerminal::equals).count() - 1;
                if (deep > maxLength) {
                    nonTerminalsUniquePath.pop();
                    throw new LoopDetectionException(
                            String.format(
                                    "Потенциальная длина генерируемой цепочки превышает максимальное значение {%s}",
                                    maxLength
                            ),
                            handledNonTerminal
                    );
                }
                Set<String> newRules = rules.get(handledNonTerminal);
                for (String newRule : newRules) {
                    if (isCanceled) {
                        throw new CancelException("Процесс отменен");
                    }
                    try {
                        if (newRule.equals(LAMBDA.toString())) {
                            add(generateChainLeft(
                                    "",
                                    chain,
                                    ruleRest + rest
                            ));
                        } else {
                            add(generateChainLeft(
                                    newRule,
                                    chain,
                                    ruleRest + rest
                            ));
                        }
                    } catch(MissRuleException ex) {
                        LOGGER.info(ex.getMessage()
                                + ". Переходим к следующему правилу");
                    } catch (ChainLengthExceededException ex) {
                        LOGGER.info(ex.getMessage()
                                + ". Останавливаем обработку правила");
                        nonTerminalsUniquePath.pop();
                        throw new MissRuleException(
                                "Остановка обработки правила"
                        );
                    } catch (LoopDetectionException ex) {
                        LOGGER.info(ex.getMessage()
                                + ". Продолжаем пропуск рекурсивной ветки. "
                                + "Возврат к обработке нетерминала '"
                                + handledNonTerminal + "'");
                        if (!handledNonTerminal.equals(ex.getRecursiveNonTerminal())) {
                            nonTerminalsUniquePath.pop();
                            throw ex;
                        }
                    }
                }
                nonTerminalsUniquePath.pop();
                return "";
            }
        } else {
            return rest.length() > 0 ? generateRestChainLeft(rest, chain) : chain;
        }
    }

    private String generateChainRight(String rule, String chain, String rest) {
        if (rule.length() > 0) {
            LinkedList<String> ruleLexemes = stringToLexemes(rule);
            String handledLexeme = ruleLexemes.peekLast();
            String ruleRest =
                    lexemesToString(ruleLexemes, 0, ruleLexemes.size() - 1);
            if (terminals.contains(handledLexeme)) {
                return generateChainRight(ruleRest, handledLexeme + chain, rest);
            } else {
                String handledNonTerminal = fromLexeme(handledLexeme);
                nonTerminalsUniquePath.push(handledNonTerminal);
                long deep = nonTerminalsUniquePath.stream()
                        .filter(handledNonTerminal::equals).count() - 1;
                if (deep > maxLength) {
                    nonTerminalsUniquePath.pop();
                    throw new LoopDetectionException(
                            String.format(
                                    "Потенциальная длина генерируемой цепочки превышает максимальное значение {%s}",
                                    maxLength
                            ),
                            handledNonTerminal
                    );
                }
                Set<String> newRules = rules.get(handledNonTerminal);
                for (String newRule : newRules) {
                    if (isCanceled) {
                        throw new CancelException("Процесс отменен");
                    }
                    try {
                        if (newRule.equals(LAMBDA.toString())) {
                            add(generateChainRight(
                                    "",
                                    chain,
                                    rest + ruleRest
                            ));
                        } else {
                            add(generateChainRight(
                                    newRule,
                                    chain,
                                    rest + ruleRest
                            ));
                        }
                    } catch (MissRuleException ex) {
                        LOGGER.info(ex.getMessage()
                                + ". Переходим к следующему правилу");
                    } catch (ChainLengthExceededException ex) {
                        LOGGER.info(ex.getMessage()
                                + ". Останавливаем обработку правила");
                        nonTerminalsUniquePath.pop();
                        throw new MissRuleException(
                                "Остановка обработки правила"
                        );
                    } catch (LoopDetectionException ex) {
                        LOGGER.info(ex.getMessage()
                                + ". Продолжаем пропуск рекурсивной ветки. "
                                + "Возврат к обработке нетерминала '"
                                + handledNonTerminal + "'");
                        if (!handledNonTerminal.equals(ex.getRecursiveNonTerminal())) {
                            nonTerminalsUniquePath.pop();
                            throw ex;
                        }
                    }
                }
                nonTerminalsUniquePath.pop();
                return "";
            }
        } else {
            return rest.length() > 0 ? generateRestChainRight(rest, chain) : chain;
        }
    }

    private void add(String chain) {
        if (chain.length() >= minLength) {
            if (chain.length() <= maxLength) {
                if (!chains.contains(chain)) {
                    chains.add(chain);
                }
            } else {
                throw new ChainLengthExceededException(
                        String.format(
                                "Длина сгенерированной цепочки {%s} превышает заданное значение {%s}",
                                chain.length(),
                                maxLength
                        )
                );
            }
        }
    }

    private String generateRestChainLeft(String rest, String chain) {
        String popItem = nonTerminalsUniquePath.pop();
        try {
            return generateChainLeft(rest, chain, "");
        } finally {
            nonTerminalsUniquePath.push(popItem);
        }
    }

    private String generateRestChainRight(String rest, String chain) {
        String popItem = nonTerminalsUniquePath.pop();
        try {
            return generateChainRight(rest, chain, "");
        } finally {
            nonTerminalsUniquePath.push(popItem);
        }
    }

}
