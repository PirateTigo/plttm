package ru.sibsutis.piratetigo.plttm.grammar;

import lombok.Data;

import java.util.List;

/**
 * Транспортный объект представления КС-грамматики,
 * используемый для сериализации / десериалиации.
 */
@Data
public class GrammarDTO {
    private String terminals;
    private String nonTerminals;
    private String goal;
    private List<RuleDTO> rules;

    @Data
    public static class RuleDTO {
        private String head;
        private String body;
    }
}
