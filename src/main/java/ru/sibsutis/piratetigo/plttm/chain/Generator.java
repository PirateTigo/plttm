package ru.sibsutis.piratetigo.plttm.chain;

import lombok.extern.java.Log;
import ru.sibsutis.piratetigo.plttm.common.InferenceType;

import java.util.*;

/**
 * Генератор цепочек символов на основе КС-грамматики.
 */
@Log
public class Generator {

    public final static Character LAMBDA = 'λ';

    private final HashMap<Character, List<String>> rules;
    private final Character goal;
    private final InferenceType direction;
    private final Integer minLength;
    private final Integer maxLength;
    private final HashSet<Character> terminals;

    private final List<String> chains = new LinkedList<>();
    private final LinkedList<Character> nonTerminalsUniquePath = new LinkedList<>();

    public Generator(
            HashMap<Character, List<String>> rules,
            Character goal,
            InferenceType direction,
            Integer minLength,
            Integer maxLength,
            HashSet<Character> terminals
    ) {
        this.rules = rules;
        this.goal = goal;
        this.direction = direction;
        this.minLength = minLength;
        this.maxLength = maxLength;
        this.terminals = terminals;
    }

    /**
     * Запускает генерацию цепочек символов.
     */
    public void generate() {
        chains.clear();
        String chain = "";
        List<String> mainRules = rules.get(goal);
        nonTerminalsUniquePath.push(goal);
        if (direction == InferenceType.LEFT) {
            for (String mainRule : mainRules) {
                try {
                    generateChainLeft(mainRule, chain, "");
                } catch (MissRuleException ex) {
                    LOGGER.info(ex.getMessage()
                            + ". Переходим к следующему правилу");
                } catch (ChainLengthExceededException ex) {
                    LOGGER.info(ex.getMessage()
                            + ". Останавливаем обработку правила");
                    break;
                } catch (LoopDetectionException ex) {
                    LOGGER.info(ex.getMessage()
                            + ". Продолжаем пропуск рекурсивной ветки. "
                            + "Возврат к обработке символа '"
                            + goal + "'");
                    break;
                }
            }
        } else {
            for (String mainRule : mainRules) {
                try {
                    generateChainRight(mainRule, chain, "");
                } catch (MissRuleException ex) {
                    LOGGER.info(ex.getMessage()
                            + ". Переходим к следующему правилу");
                } catch (ChainLengthExceededException ex) {
                    LOGGER.info(ex.getMessage()
                            + ". Останавливаем обработку правила");
                    break;
                } catch (LoopDetectionException ex) {
                    LOGGER.info(ex.getMessage()
                            + ". Продолжаем пропуск рекурсивной ветки. "
                            + "Возврат к обработке символа '"
                            + goal + "'");
                    break;
                }
            }
        }
        nonTerminalsUniquePath.pop();
    }

    /**
     * Получает список сгенерированных цепочек символов.
     * <p>
     * Перед получением необходимо вызвать метод {@link #generate()}.
     */
    public List<String> getChains() {
        return chains;
    }

    private String generateChainLeft(String rule, String chain, String rest) {
        if (rule.length() > 0) {
            Character handledCharacter = rule.charAt(0);
            String ruleRest = rule.substring(1);
            if (terminals.contains(handledCharacter)) {
                Character lastNonTerminal = nonTerminalsUniquePath.peek();
                String fullChain = generateChainLeft(
                        ruleRest,
                        chain + handledCharacter,
                        rest
                );
                if (rest.length() > 0) {
                    nonTerminalsUniquePath.push(lastNonTerminal);
                }
                return fullChain;
            } else {
                nonTerminalsUniquePath.push(handledCharacter);
                long deep = nonTerminalsUniquePath.stream()
                        .filter(handledCharacter::equals).count() - 1;
                if (deep > maxLength) {
                    nonTerminalsUniquePath.pop();
                    throw new LoopDetectionException(
                            String.format(
                                    "Потенциальная длина генерируемой цепочки превышает максимальное значение {%s}",
                                    maxLength
                            ),
                            handledCharacter
                    );
                }
                List<String> newRules = rules.get(handledCharacter);
                for (String newRule : newRules) {
                    try {
                        if (newRule.equals(LAMBDA.toString())) {
                            Character lastNonTerminal =
                                    nonTerminalsUniquePath.peek();
                            String fullChain =
                                    generateChainLeft("", chain, ruleRest + rest);
                            if ((ruleRest + rest).length() > 0) {
                                nonTerminalsUniquePath.push(lastNonTerminal);
                            }
                            add(fullChain);
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
                        nonTerminalsUniquePath.push(handledCharacter);
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
                                + "Возврат к обработке символа '"
                                + handledCharacter + "'");
                        if (handledCharacter != ex.getRecursiveNonTerminal()) {
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
            Character handledCharacter = rule.charAt(rule.length() - 1);
            String ruleRest = rule.substring(0, rule.length() - 1);
            if (terminals.contains(handledCharacter)) {
                Character lastNonTerminal = nonTerminalsUniquePath.peek();
                String fullChain = generateChainRight(
                        ruleRest,
                        handledCharacter + chain,
                        rest
                );
                if (rest.length() > 0) {
                    nonTerminalsUniquePath.push(lastNonTerminal);
                }
                return fullChain;
            } else {
                nonTerminalsUniquePath.push(handledCharacter);
                long deep = nonTerminalsUniquePath.stream()
                        .filter(handledCharacter::equals).count() - 1;
                if (deep > maxLength) {
                    nonTerminalsUniquePath.pop();
                    throw new LoopDetectionException(
                            String.format(
                                    "Потенциальная длина генерируемой цепочки превышает максимальное значение {%s}",
                                    maxLength
                            ),
                            handledCharacter
                    );
                }
                List<String> newRules = rules.get(handledCharacter);
                for (String newRule : newRules) {
                    try {
                        if (newRule.equals(LAMBDA.toString())) {
                            Character lastNonTerminal =
                                    nonTerminalsUniquePath.peek();
                            String fullChain =
                                    generateChainRight("", chain, rest + ruleRest);
                            if ((ruleRest + rest).length() > 0) {
                                nonTerminalsUniquePath.push(lastNonTerminal);
                            }
                            add(fullChain);
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
                        nonTerminalsUniquePath.push(handledCharacter);
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
                                + "Возврат к обработке символа '"
                                + handledCharacter + "'");
                        if (handledCharacter != ex.getRecursiveNonTerminal()) {
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
        nonTerminalsUniquePath.pop();
        return generateChainLeft(rest, chain, "");
    }

    private String generateRestChainRight(String rest, String chain) {
        nonTerminalsUniquePath.pop();
        return generateChainRight(rest, chain, "");
    }

}
