package com.example.internmanagement.util;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class FinalEvaluationRulesTest {
    @Test
    void computesSkillAttitudeAndOverallScores() {
        assertEquals(new BigDecimal("4.0"), FinalEvaluationRules.average(4, 4));
        assertEquals(new BigDecimal("4.5"), FinalEvaluationRules.average(4, 5));
        assertEquals(new BigDecimal("4.3"), FinalEvaluationRules.average(4, 4, 5, 4));
    }

    @Test
    void averagesOnlyScoresProvidedForDrafts() {
        assertEquals(new BigDecimal("3.0"), FinalEvaluationRules.average(2, null, 4, null));
        assertNull(FinalEvaluationRules.average(null, null));
    }

    @Test
    void roundsHalfScoresToOneDecimalPlace() {
        assertEquals(new BigDecimal("3.5"), FinalEvaluationRules.average(3, 4));
    }

    @Test
    void acceptsOnlyScoresFromOneThroughFive() {
        assertDoesNotThrow(() -> FinalEvaluationRules.validateScore(1));
        assertDoesNotThrow(() -> FinalEvaluationRules.validateScore(5));
        assertThrows(IllegalArgumentException.class, () -> FinalEvaluationRules.validateScore(0));
        assertThrows(IllegalArgumentException.class, () -> FinalEvaluationRules.validateScore(6));
        assertDoesNotThrow(() -> FinalEvaluationRules.validateScore(null));
    }

    @Test
    void finalizationRequiresAllScoresAndAUsefulComment() {
        assertDoesNotThrow(() -> FinalEvaluationRules.validateForFinalization(4, 4, 5, 4, "Rất tốt"));
        assertThrows(IllegalArgumentException.class,
                () -> FinalEvaluationRules.validateForFinalization(4, null, 5, 4, "Tốt"));
        assertThrows(IllegalArgumentException.class,
                () -> FinalEvaluationRules.validateForFinalization(4, 4, 5, 4, "   "));
    }

    @Test
    void evaluationOpensAtTheMidpointOfTheInternship() {
        assertEquals(LocalDate.parse("2026-10-17"),
                FinalEvaluationRules.eligibleAt(LocalDate.parse("2026-10-01"), LocalDate.parse("2026-10-31")));
    }

    @Test
    void invalidInternshipDatesHaveNoEligibilityDate() {
        assertNull(FinalEvaluationRules.eligibleAt(null, LocalDate.parse("2026-10-31")));
        assertNull(FinalEvaluationRules.eligibleAt(LocalDate.parse("2026-11-01"), LocalDate.parse("2026-10-31")));
    }

    @Test
    void missingOrUnknownStatusFiltersFallBackToAll() {
        assertEquals("ALL", FinalEvaluationRules.statusFilter(null));
        assertEquals("ALL", FinalEvaluationRules.statusFilter("unknown"));
        assertEquals("DRAFT", FinalEvaluationRules.statusFilter("DRAFT"));
    }
}
