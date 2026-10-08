package com.example.internmanagement.util;

import com.example.internmanagement.dao.ReviewEmailQueueDAO;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@WebListener
public class ReviewEmailScheduler implements ServletContextListener {
    private ScheduledExecutorService executor;

    @Override public void contextInitialized(ServletContextEvent event) {
        try { new ReviewEmailQueueDAO().ensureSchema(); }
        catch (Exception error) { event.getServletContext().log("Không thể khởi tạo hàng đợi email", error); }
        executor = Executors.newSingleThreadScheduledExecutor(runnable -> {
            Thread thread = new Thread(runnable, "review-email-scheduler");
            thread.setDaemon(true); return thread;
        });
        executor.scheduleWithFixedDelay(() -> {
            try { new ReviewEmailProcessor().processDue(); }
            catch (Exception error) { event.getServletContext().log("Lỗi tiến trình gửi email kết quả", error); }
        }, 20, 60, TimeUnit.SECONDS);
    }

    @Override public void contextDestroyed(ServletContextEvent event) {
        if (executor != null) executor.shutdownNow();
    }
}
