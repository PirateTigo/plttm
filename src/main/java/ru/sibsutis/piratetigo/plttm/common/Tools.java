package ru.sibsutis.piratetigo.plttm.common;

import com.google.common.collect.Sets;
import javafx.scene.control.TextInputControl;
import lombok.experimental.UtilityClass;

import java.util.*;
import java.util.stream.Collectors;

@UtilityClass
public class Tools {

    public final static String COUNTER_DEFAULT = "Всего цепочек: 0";
    public final static Character ARROW = '→';
    public final static Character LAMBDA = 'λ';
    public final static Character RULE = '┠';
    public final static Character LESS_THAN = '<';
    public final static Character GREATER_THAN = '>';

    /**
     * Вычисляет множество уникальных символов (алфавит) на основе
     * переданной строки символов {@code newSymbols}, уже имеющегося
     * множества символов {@code oldAlphabet}, javafx-элемента текстового
     * типа {@code textSrc} и множества {@code excluded} исключаемых символов
     * (запрещенных).
     *
     * @param newSymbols Источник нового набора символов.
     * @param oldAlphabet Имеющийся алфавит.
     * @param textSrc Элемент управления, отображающий ввод пользователя.
     * @param excluded Запрещенные для ввода символы.
     * @return Вычисленный алфавит.
     */
    public Set<Character> calculateAlphabet(
            String newSymbols,
            Set<Character> oldAlphabet,
            TextInputControl textSrc,
            Set<String> excluded) {
        Set<Character> alphabet = new HashSet<>();
        if (isUnique(newSymbols, alphabet)) {
            if (areNotIntersected(symbolsToStrings(alphabet), excluded)) {
                return alphabet;
            }
        }
        textSrc.textProperty().set(
                oldAlphabet.stream()
                        .map(String::valueOf)
                        .collect(Collectors.joining())
        );
        return oldAlphabet;
    }

    /**
     * Вычисляет множество уникальных строк на основе переданной строки
     * элементов {@code items}, разделенных запятой, а также уже
     * имеющегося множества строк {@code oldItems}, javafx-элемента текстового
     * типа {@code textSrc} и множества {@code excluded} исключаемых строк (запрещенных).
     *
     * @param items Источник нового набора элементов.
     * @param oldItems Имеющееся множество элементов.
     * @param textSrc Элемент управления, отображающий ввод пользователя.
     * @param excluded Запрещенные для ввода строки.
     * @return Вычисленное множество элементов.
     */
    public Set<String> calculateUniqueItems(
            String items,
            Set<String> oldItems,
            TextInputControl textSrc,
            Set<String> excluded) {
        Set<String> newItems = new HashSet<>();
        String itemsReplaced = items.replace(',', ';');
        if (itemsReplaced.lastIndexOf(';') < itemsReplaced.length() - 1) {
            return oldItems;
        }
        if (isUnique(items, "[;,]", newItems)
                && areNotIntersected(newItems, excluded)) {
            return newItems;
        }
        textSrc.textProperty().set(
                oldItems.stream()
                        .map(String::valueOf)
                        .collect(Collectors.joining(";"))
        );
        return oldItems;
    }

    /**
     * Трансформировать множество символов во множество строк.
     *
     * @param characters Символы.
     * @return Строки.
     */
    public Set<String> symbolsToStrings(Set<Character> characters) {
        return characters.stream()
                .map(Object::toString)
                .collect(Collectors.toSet());
    }

    /**
     * Трансформируем строки в список лексем.
     * <p>
     * Каждая лексема - это либо символ, либо набор символов,
     * заключенных в угловые скобки (Например, {@code <example>}).
     *
     * @param tested Исходная строка.
     * @return Список лексем.
     */
    public LinkedList<String> stringToLexemes(String tested) {
        LinkedList<String> result = new LinkedList<>();
        StringBuilder buffer = null;
        boolean isLexem = false;
        for (Character next : tested.toCharArray()) {
            if (isLexem) {
                if (next == LESS_THAN) {
                    throw new IllegalArgumentException(
                            "Unexpected '<' symbol"
                    );
                }
                buffer.append(next);
                if (next == GREATER_THAN) {
                    result.add(buffer.toString());
                    isLexem = false;
                }
            } else {
                if (next == GREATER_THAN) {
                    throw new IllegalArgumentException(
                            "Unexpected '>' symbol"
                    );
                }
                if (next == LESS_THAN) {
                    buffer = new StringBuilder();
                    buffer.append(next);
                    isLexem = true;
                    continue;
                }
                result.add(next.toString());
            }
        }
        if (isLexem) {
            throw new IllegalArgumentException(
                    "Unexpected end of lexeme"
            );
        }
        return result;
    }

    /**
     * Трансформирует список лексем в строку.
     *
     * @param lexemes Список лексем.
     * @param startIndex Начальный индекс списка для трансформации.
     * @param endIndex Конечный индекс списка для трансформации.
     * @return Строка лексем.
     */
    public String lexemesToString(LinkedList<String> lexemes, int startIndex, int endIndex) {
        StringBuilder builder = new StringBuilder();
        ListIterator<String> lexemesIterator =
                lexemes.listIterator(startIndex);
        int maxIndex = endIndex - startIndex;
        int counter = 0;
        while (lexemesIterator.hasNext()) {
            if (counter < maxIndex) {
                counter++;
                String nextLexeme = lexemesIterator.next();
                builder.append(nextLexeme);
                continue;
            }
            break;
        }
        return builder.toString();
    }

    /**
     * Вычисляет признак того, что лексема содержится в указанном
     * множестве лексем.
     *
     * @param lexeme Лексема.
     * @param lexemes Множество лексем.
     * @param stripped Признак необходимости избавления указанной
     * лексемы от угловых скобок перед проверкой вхождения.
     * @return Признак наличия лексемы в указанном множестве лексем.
     */
    public boolean setContainsLexeme(String lexeme, Set<String> lexemes, boolean stripped) {
        String testedLexeme = lexeme;
        if (stripped) {
            testedLexeme = fromLexeme(testedLexeme);
        }
        return lexemes.contains(testedLexeme);
    }

    /**
     * Оборачивает строку символов угловыми скобками,
     * трансформируя ее в лексему.
     *
     * @param string Строка символов.
     * @return Лексема.
     */
    public String toLexeme(String string) {
        return "<" + string + ">";
    }

    /**
     * Трансформирует лексему в строку символов, убирая
     * угловые скобки - обертки.
     *
     * @param lexeme Лексема.
     * @return Строка символов.
     */
    public String fromLexeme(String lexeme) {
        return lexeme.replace("<", "")
                .replace(">", "");
    }

    /**
     * Формирует все возможные комбинации символов из исходного
     * множества.
     *
     * @param lexemes Множество символов.
     * @return Наборы комбинаций символов.
     */
    public Set<Set<String>> createCombination(Set<String> lexemes) {
        Set<Set<String>> result = new HashSet<>();
        result.add(lexemes);
        for (int i = 1; i < lexemes.size(); i++) {
            Set<Set<String>> intermediateResult =
                    Sets.combinations(lexemes, i);
            result.addAll(intermediateResult);
        }
        return result;
    }

    /**
     * Вычисляет последний индекс начальной подстроки строки.
     *
     * @param string Строка.
     * @param startSubstring Начальная подстрока.
     * @return Индекс.
     */
    public int getLastIndexOf(String string, String startSubstring) {
        int i;
        for (i = 0; i < string.length() && i < startSubstring.length(); i++) {
            if (string.charAt(i) != startSubstring.charAt(i)) {
                break;
            }
        }
        return i;
    }
    /**
     * Проверяет, что тело правила вывода содержит единственный нетерминальный символ.
     *
     * @param ruleBody Проверяемое тело правила вывода.
     * @param nonTerminals Множество нетерминальных символов.
     * @return Признак того, что правило вывода содержит единственный
     * нетерминальный символ.
     */
    public boolean ruleHasSingleNonTerminal(String ruleBody, Set<String> nonTerminals) {
        List<String> lexemes = stringToLexemes(ruleBody);
        if (lexemes.size() == 1) {
            String strippedLexeme = fromLexeme(lexemes.get(0));
            return nonTerminals.contains(strippedLexeme);
        }
        return false;
    }

    /**
     * Проверяет строку на пустоту.
     *
     * @param value Строка.
     * @return Признак пустоты.
     */
    public boolean isNullOrEmpty(String value) {
        return value == null || value.isBlank();
    }

    private <T> boolean areNotIntersected(Set<T> left, Set<T> right) {
        HashSet<T> intersection = new HashSet<>(left);
        intersection.retainAll(right);
        return intersection.size() == 0;
    }

    private boolean isUnique(String value, Set<Character> alphabet) {
        char[] characters = value.toCharArray();
        for (Character character: characters) {
            if (!alphabet.add(character)) {
                return false;
            }
        }
        return true;
    }

    private boolean isUnique(String value, String reqex, Set<String> alphabet) {
        String[] items = value.split(reqex);
        for (String item: items) {
            String inserted = item.trim();
            if (inserted.length() > 0) {
                if (!alphabet.add(item.trim())) {
                    return false;
                }
            }
        }
        return true;
    }

}
