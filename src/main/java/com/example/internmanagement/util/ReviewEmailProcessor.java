package com.example.internmanagement.util;

import com.example.internmanagement.dao.ReviewEmailQueueDAO;
import com.example.internmanagement.model.ReviewEmailJob;

public class ReviewEmailProcessor {
    private final ReviewEmailQueueDAO dao = new ReviewEmailQueueDAO();

    public int processDue() throws Exception {
        if (!EmailUtility.isConfigured()) return 0;
        int sent = 0;
        int maxAttempts = intSetting("IMS_RESULT_EMAIL_MAX_ATTEMPTS", 3, 1, 10);
        int retryMinutes = intSetting("IMS_RESULT_EMAIL_RETRY_MINUTES", 30, 1, 1440);
        for (ReviewEmailJob job : dao.findDue(50)) {
            if (!dao.claim(job.getId())) continue;
            try {
                EmailUtility.sendApplicationResultEmail(job.getRecipientEmail(), job.getRecipientName(),
                        job.getDesiredPosition(), job.getDecision(), job.getDecisionReason());
                dao.markSent(job.getId());
                sent++;
            } catch (Exception error) {
                dao.markFailed(job.getId(), job.getAttemptCount() + 1, maxAttempts,
                        error.getMessage(), retryMinutes);
            }
        }
        return sent;
    }

    private static int intSetting(String name, int fallback, int min, int max) {
        String value = System.getenv(name);
        try {
            int parsed = value == null ? fallback : Integer.parseInt(value);
            return Math.max(min, Math.min(max, parsed));
        } catch (NumberFormatException ignored) { return fallback; }
    }
}
