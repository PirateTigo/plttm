package ru.sibsutis.piratetigo.plttm.common;

import javafx.scene.control.TextInputControl;
import lombok.experimental.UtilityClass;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@UtilityClass
public class Tools {

    public final static Character LAMBDA = 'λ';

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
    public HashSet<Character> calculateAlphabet(
            String newSymbols,
            HashSet<Character> oldAlphabet,
            TextInputControl textSrc,
            HashSet<Character> excluded) {
        HashSet<Character> alphabet = new HashSet<>();
        if (isUnique(newSymbols, alphabet) && !areIntersected(alphabet, excluded)) {
            return alphabet;
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
    public HashSet<String> calculateUniqueItems(
            String items,
            HashSet<String> oldItems,
            TextInputControl textSrc,
            HashSet<String> excluded) {
        HashSet<String> newItems = new HashSet<>();
        String itemsReplaced = items.replace(',', ';');
        if (itemsReplaced.lastIndexOf(';') < itemsReplaced.length() - 1) {
            return oldItems;
        }
        if (isUnique(items, "[;,]", newItems) && !areIntersected(newItems, excluded)) {
            return newItems;
        }
        textSrc.textProperty().set(
                oldItems.stream()
                        .map(String::valueOf)
                        .collect(Collectors.joining(";"))
        );
        return oldItems;
    }

    private <T> boolean areIntersected(Set<T> left, Set<T> right) {
        HashSet<T> intersection = new HashSet<>(left);
        intersection.retainAll(right);
        return intersection.size() != 0;
    }

    private boolean isUnique(String value, HashSet<Character> alphabet) {
        char[] characters = value.toCharArray();
        for (Character character: characters) {
            if (!alphabet.add(character)) {
                return false;
            }
        }
        return true;
    }

    private boolean isUnique(String value, String reqex, HashSet<String> alphabet) {
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
