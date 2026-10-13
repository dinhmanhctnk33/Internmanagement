package com.example.internmanagement.service;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.example.internmanagement.dao.JdbcInternshipContextDAO;
import com.example.internmanagement.dao.NotificationDAO;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;

public class ReminderSchedulerListener implements ServletContextListener {
    private static final LocalTime DAILY_RUN_TIME = LocalTime.of(9, 0);
    private ScheduledExecutorService scheduler;

    @Override
    public void contextInitialized(ServletContextEvent event) {
        ScheduledReminderService reminderService = new ScheduledReminderService(
                new NotificationDAO(), new JdbcInternshipContextDAO(), java.time.Clock.systemDefaultZone());
        scheduler = Executors.newScheduledThreadPool(3, runnable -> {
            Thread thread = new Thread(runnable, "internship-reminder-scheduler");
            thread.setDaemon(true);
            return thread;
        });
        scheduler.scheduleAtFixedRate(reminderService::scanMeetingReminders,
                0, 5, TimeUnit.MINUTES);
        scheduler.scheduleAtFixedRate(reminderService::scanTaskDueReminders,
                delayUntilDailyRun(), TimeUnit.DAYS.toSeconds(1), TimeUnit.SECONDS);
        scheduler.scheduleAtFixedRate(reminderService::scanWeeklyReportReminders,
                delayUntilFridayRun(), TimeUnit.DAYS.toSeconds(7), TimeUnit.SECONDS);
    }

    @Override
    public void contextDestroyed(ServletContextEvent event) {
        if (scheduler != null) {
            scheduler.shutdownNow();
        }
    }

    private long delayUntilDailyRun() {
        ZoneId zone = ZoneId.systemDefault();
        LocalDateTime now = LocalDateTime.now(zone);
        LocalDateTime next = now.toLocalDate().atTime(DAILY_RUN_TIME);
        if (!next.isAfter(now)) {
            next = next.plusDays(1);
        }
        return Duration.between(now, next).toSeconds();
    }

    private long delayUntilFridayRun() {
        ZoneId zone = ZoneId.systemDefault();
        LocalDateTime now = LocalDateTime.now(zone);
        LocalDateTime next = now.toLocalDate()
                .with(TemporalAdjusters.nextOrSame(DayOfWeek.FRIDAY))
                .atTime(DAILY_RUN_TIME);
        if (!next.isAfter(now)) {
            next = next.plusWeeks(1);
        }
        return Duration.between(now, next).toSeconds();
    }
}