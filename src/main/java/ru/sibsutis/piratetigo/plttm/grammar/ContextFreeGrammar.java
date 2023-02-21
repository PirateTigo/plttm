package ru.sibsutis.piratetigo.plttm.grammar;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.scene.control.TextInputControl;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import ru.sibsutis.piratetigo.plttm.common.Tools;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import static ru.sibsutis.piratetigo.plttm.common.Tools.*;

/**
 * Контекстно-свободная грамматика.
 */
@Log
@RequiredArgsConstructor
public class ContextFreeGrammar {

    private static final String RULE_DELIMITER = "|";

    private static final String RULE_FORMAT_VIOLATION =
            "Нарушен формат ввода правила вывода";

    /**
     * Пользовательский элемент управления для отображения множества
     * терминальных символов.
     */
    private final TextInputControl userTerminals;
    /** Пользовательский элемент управления для отображения множества
     * нетерминальных символов.
     */
    private final TextInputControl userNonTerminals;
    /**
     * Пользовательский элемент управления для отображения целевого
     * символа.
     */
    private final ComboBox<String> goalCharacter;
    /**
     * Пользовательский элемент управления для отображения правил
     * вывода.
     */
    private final ListView<String> userRules;

    private ObjectMapper objectMapper;

    {
        objectMapper = new ObjectMapper();
        objectMapper.configure(SerializationFeature.INDENT_OUTPUT, true);
    }

    /** Множество терминальных символов. */
    @Getter
    private Set<Character> terminals = new HashSet<>();

    /** Множество нетерминальных символов. */
    @Getter
    private Set<String> nonTerminals = new HashSet<>();

    /** Целевой символ. */
    @Getter
    private String goal;

    /** Правила вывода. */
    @Getter
    private HashMap<String, Set<String>> rules = new HashMap<>();

    /**
     * Задает множество терминальных символов на основе переданной строки.
     * <p>
     * На строку накладываются ограничения:
     * <ul>
     *  <li>Она не должна содержать пробельных символов;
     *  <li>Каждый символ строки рассматривается как терминальных символ.
     * </ul>
     *
     * @param newValue Строка символов.
     */
    public void setTerminals(String newValue) {
        terminals = calculateAlphabet(
                newValue.trim(),
                terminals,
                userTerminals,
                nonTerminals
        );
    }

    /**
     * Задает множество нетерминальных символов на основе переданной строки.
     * <p>
     * На строку накладываются ограничения:
     * <ul>
     *  <li>Она не должна содержать пробельных символов;
     *  <li>Все лексемы нетерминальных символов должны быть разделены
     *  запятыми или точками с запятой.</li>
     *  <li>Лексемы нетерминальных символов не должны содержать
     *  символы {@code <} и {@code >}.</li>
     * </ul>
     * @param newValue Строка символов.
     */
    public void setNonTerminals(String newValue) {
        nonTerminals = calculateUniqueItems(
                newValue.trim(),
                nonTerminals,
                userNonTerminals,
                symbolsToStrings(terminals)
        );
    }

    /**
     * Устанавливает целевой символ грамматики.
     * <p>
     * Формат задания целевого символа:
     * <ul>
     *  <li>Целевой символ должен принадлежать множеству нетерминальных
     *  символов;</li>
     *  <li>Целевой символ не может содержать символы обертки
     *  {@code <} и {@code >}.</li>
     * </ul>
     * @param goal Целевой символ.
     */
    public void setGoal(String goal) {
        if (setContainsLexeme(goal, nonTerminals, false)) {
            this.goal = goal;
        } else {
            throw new IllegalArgumentException(
                    "Нарушен формат задания целевого символа"
            );
        }
    }

    /**
     * Добавляет правило вывода в список, учитывая корректность
     * его задания.
     * <p>
     * Для корректного задания правила вывода необходимо придерживаться
     * следующего формата:
     * <ul>
     *  <li>Порождающий символ и тело правила должны быть разделены
     *  символом →;</li>
     *  <li>Порождающий символ должен быть одной из лексем множества
     *  нетерминальных символов;</li>
     *  <li>Порождающий символ не может быть обернут в символы
     *  {@code <} и {@code >};</li>
     *  <li>Все лексемы тела правила должны быть либо нетерминальными
     *  символами, либо терминальными, либо символом λ;</li>
     *  <li>Если символ λ присутствует в теле правила, то оно
     *  не может содержать других символов;</li>
     *  <li>Лексемы нетерминальных символов могут быть обернуты в
     *  символы {@code <} и {@code >}.</li>
     * </ul>
     * @param rule Добавляемое правило.
     * @throws IllegalArgumentException Если правило не соответствует формату.
     */
    public void addRule(String rule) {
        String[] splitRule = rule.split(ARROW.toString());
        if (splitRule.length != 2
                || !setContainsLexeme(splitRule[0], nonTerminals, false)) {
            throw new IllegalArgumentException(RULE_FORMAT_VIOLATION);
        }
        HashSet<String> allLexemes = new HashSet<>();
        allLexemes.addAll(symbolsToStrings(terminals));
        allLexemes.addAll(nonTerminals);
        allLexemes.add(LAMBDA.toString());
        for (String lexeme : stringToLexemes(splitRule[1])) {
            if (lexeme.charAt(0) == LESS_THAN) {
                if (!setContainsLexeme(lexeme, nonTerminals, true)) {
                    throw new IllegalArgumentException(RULE_FORMAT_VIOLATION);
                }
                continue;
            }
            if (!setContainsLexeme(lexeme, allLexemes, false)) {
                throw new IllegalArgumentException(RULE_FORMAT_VIOLATION);
            }
            if (lexeme.contains(LAMBDA.toString())) {
                if (lexeme.length() > 1 || splitRule[1].length() > 1) {
                    throw new IllegalArgumentException(RULE_FORMAT_VIOLATION);
                }
            }
        }
        rules.computeIfAbsent(splitRule[0], k -> new HashSet<>())
                .add(splitRule[1]);
    }

    /**
     * Загружает КС-грамматику из файла.
     *
     * @param file Файл с КС-грамматикой в формате JSON.
     * @throws IOException При ошибках десериализации или работы с файлом.
     */
    public void load(File file) throws IOException {
        GrammarDTO grammarDTO = objectMapper.readValue(file, GrammarDTO.class);
        setTerminals(grammarDTO.getTerminals());
        setUserTerminals();
        setNonTerminals(grammarDTO.getNonTerminals());
        setUserNonTerminals();
        goalCharacter.setValue(grammarDTO.getGoal());
        rules.clear();
        grammarDTO.getRules().forEach(ruleDTO -> {
            String[] ruleBodies = ruleDTO.getBody().split("\\" + RULE_DELIMITER);
            for (String ruleBody: ruleBodies) {
                String rule = ruleDTO.getHead()
                        + ARROW
                        + ruleBody;
                rule = rule.replace("<lambda>", LAMBDA.toString());
                addRule(rule);
            }
        });
        setUserRules();
    }

    /**
     * Сохраняет КС-грамматику в файл.
     *
     * @param file Файл для сохранения КС-грамматики в формате JSON.
     * @throws IOException При ошибках сериализации или работы с файлом.
     */
    public void save(File file) throws IOException {
        GrammarDTO grammarDTO = new GrammarDTO();
        grammarDTO.setTerminals(userTerminals.getText());
        grammarDTO.setNonTerminals(userNonTerminals.getText());
        grammarDTO.setGoal(goalCharacter.getValue());
        List<GrammarDTO.RuleDTO> ruleDTOList = new ArrayList<>();
        rules.forEach((head, bodies) -> {
            GrammarDTO.RuleDTO ruleDTO = new GrammarDTO.RuleDTO();
            ruleDTO.setHead(head);
            String bodiesString = String.join(RULE_DELIMITER, bodies);
            bodiesString = bodiesString.replace(LAMBDA.toString(), "<lambda>");
            ruleDTO.setBody(bodiesString);
            ruleDTOList.add(ruleDTO);
        });
        grammarDTO.setRules(ruleDTOList);
        objectMapper.writeValue(file, grammarDTO);
    }

    /**
     * Переводит грамматику в канонический вид.
     */
    public void toCanonical() {
        deleteBarrenSymbols();
        deleteUnreachableSymbols();
        deleteLambdaRules();
        deleteChainRules();
    }

    /**
     * Переводит грамматику в Бинарную Нормальную Форму (Хомского).
     */
    public void toChomsky() {
        HashMap<String, Set<String>> newRules = new HashMap<>();
        Set<String> stringTerminals = symbolsToStrings(terminals);
        rules.forEach((head, ruleBodies) -> {
            ruleBodies.forEach(ruleBody -> {
                LinkedList<String> lexemes = stringToLexemes(ruleBody);
                if (lexemes.size() == 1) {
                    if (stringTerminals.contains(lexemes.get(0))) {
                        newRules.computeIfAbsent(head, (h) -> new HashSet<>())
                                .add(ruleBody);
                        return;
                    }
                    if (head.equals(goal)) {
                        if (lexemes.get(0).equals(LAMBDA.toString())) {
                            newRules.computeIfAbsent(head, (h) -> new HashSet<>())
                                    .add(ruleBody);
                            return;
                        }
                    }
                }
                if (lexemes.size() == 2) {
                    if (nonTerminals.contains(fromLexeme(lexemes.get(0)))
                        && nonTerminals.contains(fromLexeme(lexemes.get(1)))) {
                        newRules.computeIfAbsent(head, (h) -> new HashSet<>())
                                .add(ruleBody);
                        return;
                    }
                    if (stringTerminals.contains(lexemes.get(0))
                            && nonTerminals.contains(fromLexeme(lexemes.get(1)))) {
                        String newNonTerminal =
                                head + lexemes.get(0) + fromLexeme(lexemes.get(1));
                        newRules.computeIfAbsent(head, (h) -> new HashSet<>())
                                .add(toLexeme(newNonTerminal) + lexemes.get(1));
                        newRules.computeIfAbsent(newNonTerminal, (h) -> new HashSet<>())
                                .add(lexemes.get(0));
                        nonTerminals.add(newNonTerminal);
                        return;
                    }
                    if (nonTerminals.contains(fromLexeme(lexemes.get(0)))
                            && stringTerminals.contains(lexemes.get(1))) {
                        String newNonTerminal =
                                head + fromLexeme(lexemes.get(0)) + lexemes.get(1);
                        newRules.computeIfAbsent(head, (h) -> new HashSet<>())
                                .add(lexemes.get(0) + toLexeme(newNonTerminal));
                        newRules.computeIfAbsent(newNonTerminal, (h) -> new HashSet<>())
                                .add(lexemes.get(1));
                        nonTerminals.add(newNonTerminal);
                        return;
                    }
                    if (stringTerminals.contains(lexemes.get(0))
                            && stringTerminals.contains(lexemes.get(1))) {
                        String newNonTerminal1 = head + lexemes.get(0);
                        String newNonTerminal2 = head + lexemes.get(1);
                        newRules.computeIfAbsent(head, (h) -> new HashSet<>())
                                .add(toLexeme(newNonTerminal1) + toLexeme(newNonTerminal2));
                        newRules.computeIfAbsent(newNonTerminal1, (h) -> new HashSet<>())
                                .add(lexemes.get(0));
                        newRules.computeIfAbsent(newNonTerminal2, (h) -> new HashSet<>())
                                .add(lexemes.get(1));
                        nonTerminals.add(newNonTerminal1);
                        nonTerminals.add(newNonTerminal2);
                    }
                }
                if (lexemes.size() > 2) {
                    final AtomicBoolean areAllTerminals = new AtomicBoolean(true);
                    lexemes.forEach(lexeme -> {
                        if (!areAllTerminals.get()) return;
                        if (!stringTerminals.contains(lexeme)
                                && !nonTerminals.contains(fromLexeme(lexeme))) {
                            areAllTerminals.set(false);
                        }
                    });
                    if (areAllTerminals.get()) {
                        String prevNonTerminal = head;
                        while (lexemes.size() > 1) {
                            String nextLexeme = lexemes.pollFirst();
                            String newLexeme = getNewLexeme(nextLexeme, newRules);
                            if (lexemes.size() > 1) {
                                String newNonTerminal = lexemes.stream()
                                        .map(Tools::fromLexeme)
                                        .collect(Collectors.joining());
                                newRules.computeIfAbsent(prevNonTerminal, (h) -> new HashSet<>())
                                        .add(newLexeme + toLexeme(newNonTerminal));
                                nonTerminals.add(newNonTerminal);
                                prevNonTerminal = newNonTerminal;
                            } else {
                                String rightNewLexeme = getNewLexeme(lexemes.pop(), newRules);
                                newRules.computeIfAbsent(prevNonTerminal, (h) -> new HashSet<>())
                                        .add(newLexeme + rightNewLexeme);
                            }
                        }
                    }
                }
            });
            String copyGoal = goal;
            setUserNonTerminals();
            goalCharacter.setValue(copyGoal);
            rules = newRules;
            setUserRules();
        });
    }

    /**
     * Создает новый экземпляр КС-грамматики на основе этой грамматики.
     *
     * @param userTerminals Пользовательский элемент управления
     * для отображения множества терминальных символов.
     * @param userNonTerminals Пользовательский элемент управления
     * для отображения множества нетерминальных символов.
     * @param goalCharacter Пользовательский элемент управления
     * для отображения целевого символа.
     * @param userRules Пользовательский элемент управления
     * для отображения правил вывода.
     * @return Новый экземпляр КС-грамматики.
     */
    public ContextFreeGrammar clone(
            TextInputControl userTerminals,
            TextInputControl userNonTerminals,
            ComboBox<String> goalCharacter,
            ListView<String> userRules
    ) {
        ContextFreeGrammar copy = new ContextFreeGrammar(
                userTerminals,
                userNonTerminals,
                goalCharacter,
                userRules
        );
        copy.terminals = new HashSet<>(terminals);
        copy.setUserTerminals();
        copy.nonTerminals = new HashSet<>(nonTerminals);
        copy.setUserNonTerminals();
        goalCharacter.setValue(goal);
        copy.goal = goal;
        copy.rules = new HashMap<>();
        rules.forEach((head, ruleBodies) ->
            copy.rules.computeIfAbsent(head, (h) -> new HashSet<>())
                .addAll(ruleBodies));
        copy.setUserRules();
        return copy;
    }

    /**
     * Вывести информацию о грамматике в журнал приложения.
     */
    public void log() {
        StringJoiner stringedRules = new StringJoiner(
                ", ",
                "Целевой символ: " + goal + "\nИсходные правила: [",
                "]"
        );
        rules.forEach((left, right) -> {
            StringJoiner oneRule = new StringJoiner(
                    "|",
                    left + "=>",
                    ""
            );
            for (String rule: right) {
                oneRule.add(rule.replace(LAMBDA.toString(), "<lambda>"));
            }
            stringedRules.add(oneRule.toString());
        });
        LOGGER.info(stringedRules.toString());
    }

    /**
     * Удаляет бесплодные символы.
     */
    private void deleteBarrenSymbols() {
        HashSet<String> ySet = new HashSet<>();
        int lengthYSet;
        do {
            lengthYSet = ySet.size();
            HashSet<String> newsInYSet = new HashSet<>();
            Set<String> addedTerminalsSet = symbolsToStrings(terminals);
            addedTerminalsSet.addAll(ySet);
            addedTerminalsSet.add(LAMBDA.toString());
            nonTerminals.forEach(symbol -> {
                final AtomicBoolean symbolAdded = new AtomicBoolean(false);
                rules.get(symbol).forEach(ruleBody -> {
                    if (!symbolAdded.get()) {
                        boolean isContains = true;
                        for (String lexeme : stringToLexemes(ruleBody)) {
                            if (!addedTerminalsSet.contains(fromLexeme(lexeme))) {
                                isContains = false;
                                break;
                            }
                        }
                        if (isContains) {
                            newsInYSet.add(symbol);
                            symbolAdded.set(true);
                        }
                    }
                });
            });

            ySet.addAll(newsInYSet);
        } while (lengthYSet != ySet.size());

        if (ySet.size() != nonTerminals.size()) {
            setNonTerminals(String.join(";", ySet) + ";");
            Set<String> addedTerminalsSet = symbolsToStrings(terminals);
            addedTerminalsSet.addAll(ySet);

            HashMap<String, Set<String>> newRules = new HashMap<>();

            rules.forEach((head, ruleBodies) -> {
                if (addedTerminalsSet.contains(head)) {
                    ruleBodies.forEach(ruleBody -> {
                        boolean isContains = true;
                        for (String lexeme : stringToLexemes(ruleBody)) {
                            if (!addedTerminalsSet.contains(fromLexeme(lexeme))) {
                                isContains = false;
                                break;
                            }
                        }
                        if (isContains) {
                            newRules.computeIfAbsent(head, (h) -> new HashSet<>())
                                    .add(ruleBody);
                        }
                    });
                }
            });

            setUserNonTerminals();
            goalCharacter.setValue(goal);
            rules = newRules;
            setUserRules();
        }
    }

    /**
     * Удаляет недостижимые символы.
     */
    private void deleteUnreachableSymbols() {
        HashSet<String> vSet = new HashSet<>();
        vSet.add(goal);
        int lengthVSet;
        HashSet<String> allTerminals = new HashSet<>();
        allTerminals.addAll(symbolsToStrings(terminals));
        allTerminals.addAll(nonTerminals);
        allTerminals.add(LAMBDA.toString());
        do {
            lengthVSet = vSet.size();
            HashSet<String> newsInVSet = new HashSet<>();
            HashSet<String> intersection = new HashSet<>(nonTerminals);
                intersection.retainAll(vSet);
            intersection.forEach(nonTerminal ->
                rules.get(nonTerminal).forEach(ruleBody -> {
                    StringBuilder leftPart = new StringBuilder();
                    stringToLexemes(ruleBody).forEach(lexeme -> {
                        String rightPart = ruleBody.substring(
                                getLastIndexOf(ruleBody, leftPart + lexeme)
                        );
                        final AtomicBoolean leftPartIsOk = new AtomicBoolean(true);
                        final AtomicBoolean rightPartIsOk = new AtomicBoolean(true);
                        if (!isNullOrEmpty(leftPart.toString())) {
                            stringToLexemes(leftPart.toString()).forEach(leftPartLexeme -> {
                                if (!leftPartIsOk.get()) return;
                                if (!allTerminals.contains(fromLexeme(leftPartLexeme))) {
                                    leftPartIsOk.set(false);
                                }
                            });
                        }
                        if (leftPartIsOk.get()) {
                            if (!isNullOrEmpty(rightPart)) {
                                stringToLexemes(rightPart).forEach(rightPartLexeme -> {
                                    if (!rightPartIsOk.get()) return;
                                    if (!allTerminals.contains(fromLexeme(rightPartLexeme))) {
                                        rightPartIsOk.set(false);
                                    }
                                });
                            }
                            if (rightPartIsOk.get()) {
                                if (allTerminals.contains(fromLexeme(lexeme))) {
                                    newsInVSet.add(fromLexeme(lexeme));
                                }
                            }
                        }
                        leftPart.append(lexeme);
                    });
                })
            );
            vSet.addAll(newsInVSet);
        } while (lengthVSet != vSet.size());

        HashSet<String> newTerminals = new HashSet<>(vSet);
        newTerminals.retainAll(symbolsToStrings(terminals));
        setTerminals(String.join("", newTerminals));
        HashSet<String> newNonTerminals = new HashSet<>(vSet);
        newNonTerminals.retainAll(nonTerminals);
        setNonTerminals(String.join(";", newNonTerminals) + ";");

        HashMap<String, Set<String>> newRules = new HashMap<>();

        rules.forEach((head, ruleBodies) -> {
            if (vSet.contains(head)) {
                ruleBodies.forEach(ruleBody -> {
                    boolean isContains = true;
                    for (String lexeme : stringToLexemes(ruleBody)) {
                        if (!vSet.contains(fromLexeme(lexeme))) {
                            isContains = false;
                            break;
                        }
                    }
                    if (isContains) {
                        newRules.computeIfAbsent(head, (h) -> new HashSet<>())
                                .add(ruleBody);
                    }
                });
            }
        });

        setUserTerminals();
        setUserNonTerminals();
        goalCharacter.setValue(goal);
        rules = newRules;
        setUserRules();
    }

    /**
     * Удаляет лямбда-правила.
     */
    private void deleteLambdaRules() {
        HashSet<String> wSet = new HashSet<>();
        nonTerminals.forEach(nonTerminal -> {
            final AtomicBoolean nonTerminalAdded = new AtomicBoolean(false);
            rules.get(nonTerminal).forEach(ruleBody -> {
                if (!nonTerminalAdded.get()) {
                    if (ruleBody.equals(LAMBDA.toString())) {
                        wSet.add(nonTerminal);
                        nonTerminalAdded.set(true);
                    }
                }
            });
        });

        int lengthWSet;
        do {
            lengthWSet = wSet.size();
            HashSet<String> newsInWSet = new HashSet<>();
            nonTerminals.forEach(nonTerminal -> {
                final AtomicBoolean nonTerminalAdded = new AtomicBoolean(false);
                rules.get(nonTerminal).forEach(ruleBody -> {
                    if (!nonTerminalAdded.get()) {
                        boolean isWContains = true;
                        for (String lexeme: stringToLexemes(ruleBody)) {
                            if (!wSet.contains(fromLexeme(lexeme))) {
                                isWContains = false;
                                break;
                            }
                        }
                        if (isWContains) {
                            newsInWSet.add(nonTerminal);
                            nonTerminalAdded.set(true);
                        }
                    }
                });
            });
            wSet.addAll(newsInWSet);
        } while (lengthWSet != wSet.size());

        final HashMap<String, Set<String>> newRules = new HashMap<>();
        rules.forEach((head, ruleBodies) ->
            ruleBodies.forEach(ruleBody -> {
                if (!ruleBody.equals(LAMBDA.toString())) {
                    newRules.computeIfAbsent(head, (h) -> new HashSet<>())
                            .add(ruleBody);
                }
            })
        );

        Set<Set<String>> wCombinations = createCombination(wSet);

        newRules.forEach((head, ruleBodies) -> {
            Set<String> newRuleBodies = new HashSet<>();
            ruleBodies.forEach(ruleBody -> {
                boolean isContainsWSymbols = false;
                for (String lexeme: stringToLexemes(ruleBody)) {
                    if (wSet.contains(fromLexeme(lexeme))) {
                        isContainsWSymbols = true;
                        break;
                    }
                }
                if (isContainsWSymbols) {
                    wCombinations.forEach(combination -> {
                        final StringBuilder builder = new StringBuilder(ruleBody);
                        combination.forEach(symbol -> {
                            String newValue = builder.toString().replace(symbol, "");
                            builder.delete(0, builder.length());
                            builder.append(newValue);
                        });
                        String newRuleBody = builder.toString();
                        newRuleBody = newRuleBody.replace("<>", "");
                        if (!isNullOrEmpty(newRuleBody)) {
                            List<String> newRuleBodyLexemes = stringToLexemes(newRuleBody);
                            if (newRuleBodyLexemes.size() == 1) {
                                if (!fromLexeme(newRuleBodyLexemes.get(0)).equals(head)) {
                                    newRuleBodies.add(newRuleBody);
                                }
                                return;
                            }
                            newRuleBodies.add(newRuleBody);
                        }
                    });
                }
            });
            newRules.get(head).addAll(newRuleBodies);
        });

        if (wSet.contains(goal)) {
            final AtomicBoolean isInRules = new AtomicBoolean(false);
            newRules.forEach((head, ruleBodies) -> {
                if (!isInRules.get()) {
                    ruleBodies.forEach(ruleBody -> {
                        if (!isInRules.get()) {
                            if (ruleBody.contains(goal)) {
                                isInRules.set(true);
                            }
                        }
                    });
                }
            });
            if (isInRules.get()) {
                String newGoal = goal + '\'';
                setNonTerminals(String.join(";", nonTerminals) + ";" + newGoal + ";");
                setUserNonTerminals();
                newRules.put(newGoal, new HashSet<>());
                newRules.get(newGoal).add(LAMBDA.toString());
                newRules.get(newGoal).add(goal.length() > 1 ? toLexeme(goal) : goal);
                goalCharacter.setValue(newGoal);
            }
        }
        rules = newRules;
        setUserRules();
    }

    /**
     * Удаляет цепные правила.
     */
    private void deleteChainRules() {
        HashMap<String, Set<String>> nSets = new HashMap<>();
        nonTerminals.forEach(nonTerminal -> {
            Set<String> nSet = new HashSet<>();
            nSet.add(nonTerminal);
            int lengthNSet;
            do {
                lengthNSet = nSet.size();
                Set<String> newsInNSet = new HashSet<>();
                nSet.forEach(symbol ->
                    rules.get(symbol).forEach(ruleBody -> {
                        if (ruleHasSingleNonTerminal(ruleBody, nonTerminals)) {
                            newsInNSet.add(fromLexeme(ruleBody));
                        }
                    })
                );
                nSet.addAll(newsInNSet);
            } while (lengthNSet != nSet.size());
            nSet.remove(nonTerminal);
            nSets.put(nonTerminal, nSet);
        });

        HashMap<String, Set<String>> newRules = new HashMap<>();
        rules.forEach((head, ruleBodies) ->
            ruleBodies.forEach(ruleBody -> {
                if (!ruleHasSingleNonTerminal(ruleBody, nonTerminals)) {
                    newRules.computeIfAbsent(head, (h) -> new HashSet<>())
                            .add(ruleBody);
                }
            })
        );

        HashMap<String, Set<String>> newNewRules = new HashMap<>();

        newRules.forEach((head, ruleBodies) ->
            nSets.forEach((x, nSet) -> {
                if (nSet.contains(head)) {
                    newNewRules.computeIfAbsent(x, (isX) -> new HashSet<>())
                                    .addAll(ruleBodies);
                }
            })
        );

        newNewRules.forEach((head, ruleBodies) ->
            newRules.computeIfAbsent(head, (h) -> new HashSet<>())
                    .addAll(ruleBodies)
        );

        rules = newRules;
        setUserRules();
        goalCharacter.setValue(goal);
    }

    private void setUserTerminals() {
        userTerminals.textProperty().set(
                terminals.stream()
                        .map(String::valueOf)
                        .collect(Collectors.joining())
        );
    }

    private void setUserNonTerminals() {
        userNonTerminals.textProperty().set(
                nonTerminals.stream()
                        .collect(Collectors.joining(";", "", ";"))
        );
    }

    private void setUserRules() {
        userRules.getItems().clear();
        rules.forEach((head, ruleBodies) ->
                ruleBodies.forEach(ruleBody ->
                        userRules.getItems().add(head + ARROW + ruleBody)));
    }

    private String getNewLexeme(
            String nextLexeme, HashMap<String,
            Set<String>> newRules) {
        String newLexeme;
        if (nonTerminals.contains(fromLexeme(nextLexeme))) {
            // Нетерминальный символ
            newLexeme = nextLexeme;
        } else {
            // Терминальный символ
            newLexeme = "[" + nextLexeme + "]";
            nonTerminals.add(newLexeme);
            newRules.computeIfAbsent(newLexeme, (h) -> new HashSet<>())
                    .add(nextLexeme);
            newLexeme = toLexeme(newLexeme);
        }
        return newLexeme;
    }

}
