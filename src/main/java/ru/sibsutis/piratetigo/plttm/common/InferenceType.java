package ru.sibsutis.piratetigo.plttm.common;

/**
 * Перечисляет типы формальных выводов.
 */
public enum InferenceType {
    /** Левосторонний тип. */
    LEFT("Левосторонний"),
    /** Правосторонний тип. */
    RIGHT("Правосторонний");

    private final String value;

    InferenceType(String value) {
        this.value = value;
    }

    /**
     * Возвращает строковое значение типа вывода.
     */
    public String getValue() {
        return value;
    }

    public static InferenceType from(String value) {
        if (LEFT.value.equals(value)) {
            return LEFT;
        } else if (RIGHT.value.equals(value)) {
            return RIGHT;
        } else {
            throw new IllegalArgumentException(
                    String.format("Неизвестный элемент перечисления %s", value)
            );
        }
    }
}


