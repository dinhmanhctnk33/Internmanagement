package com.example.internmanagement.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Set;

public final class FinalEvaluationRules {
    public static final int MAX_COMMENT_LENGTH = 2000;

    private FinalEvaluationRules() {
    }

    public static void validateScore(Integer score) {
        if (score != null && (score < 1 || score > 5)) {
            throw new IllegalArgumentException("Điểm từng tiêu chí phải nằm trong khoảng 1 đến 5.");
        }
    }

    public static void validateForFinalization(Integer knowledge, Integer problemSolving,
                                               Integer initiative, Integer teamwork,
                                               String comment) {
        Integer[] scores = {knowledge, problemSolving, initiative, teamwork};
        Arrays.stream(scores).forEach(FinalEvaluationRules::validateScore);
        if (Arrays.stream(scores).anyMatch(score -> score == null)) {
            throw new IllegalArgumentException("Vui lòng chấm đủ cả 4 tiêu chí trước khi chốt.");
        }
        String normalizedComment = comment == null ? "" : comment.trim();
        if (normalizedComment.length() < 5) {
            throw new IllegalArgumentException("Nhận xét chung cần có ít nhất 5 ký tự.");
        }
        if (normalizedComment.length() > MAX_COMMENT_LENGTH) {
            throw new IllegalArgumentException("Nhận xét chung không được vượt quá 2000 ký tự.");
        }
    }

    public static BigDecimal average(Integer... scores) {
        int total = 0;
        int count = 0;
        for (Integer score : scores) {
            if (score != null) {
                validateScore(score);
                total += score;
                count++;
            }
        }
        return count == 0 ? null : BigDecimal.valueOf(total)
                .divide(BigDecimal.valueOf(count), 1, RoundingMode.HALF_UP);
    }

    public static String statusFilter(String value) {
        Set<String> allowed = Set.of("ALL", "NOT_STARTED", "DRAFT", "FINALIZED", "FINALIZED_AGAIN");
        return value != null && allowed.contains(value) ? value : "ALL";
    }

    public static LocalDate eligibleAt(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null || endDate.isBefore(startDate)) {
            return null;
        }
        long durationDays = ChronoUnit.DAYS.between(startDate, endDate);
        return startDate.plusDays(durationDays / 2 + 1);
    }
}
