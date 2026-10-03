package com.example.internmanagement.util;

import jakarta.mail.*;
import jakarta.mail.internet.*;
import java.io.UnsupportedEncodingException;
import java.util.Properties;

public class EmailUtility {

    public static boolean isConfigured() { return value("IMS_SMTP_USER") != null && value("IMS_SMTP_PASSWORD") != null; }

    public static void sendResetPasswordEmail(String recipientEmail, String resetUrl) throws MessagingException, UnsupportedEncodingException {
        if (!isConfigured()) throw new MessagingException("SMTP chưa được cấu hình.");
        Properties props = new Properties(); props.put("mail.smtp.host", valueOr("IMS_SMTP_HOST", "smtp.gmail.com")); props.put("mail.smtp.port", valueOr("IMS_SMTP_PORT", "587")); props.put("mail.smtp.auth", "true"); props.put("mail.smtp.starttls.enable", "true");
        String sender = value("IMS_SMTP_USER");
        Session session = Session.getInstance(props, new Authenticator() { @Override protected PasswordAuthentication getPasswordAuthentication() { return new PasswordAuthentication(sender, value("IMS_SMTP_PASSWORD")); } });
        Message message = new MimeMessage(session); message.setFrom(new InternetAddress(sender, "IMS Portal - Quản lý Thực tập sinh")); message.setRecipient(Message.RecipientType.TO, new InternetAddress(recipientEmail)); message.setSubject("Đặt lại mật khẩu IMS Portal");
        message.setContent("<p>Bạn đã yêu cầu đặt lại mật khẩu.</p><p><a href=\"" + resetUrl + "\">Đặt lại mật khẩu</a></p><p>Liên kết có hiệu lực trong 24 giờ.</p>", "text/html; charset=UTF-8"); Transport.send(message);
    }

    public static void sendAccountCreatedEmail(String recipientEmail, String fullName,
            String username, String initialPassword, String roleName, String loginUrl)
            throws MessagingException, UnsupportedEncodingException {
        if (!isConfigured()) throw new MessagingException("SMTP is not configured.");
        Properties props = new Properties(); props.put("mail.smtp.host", valueOr("IMS_SMTP_HOST", "smtp.gmail.com")); props.put("mail.smtp.port", valueOr("IMS_SMTP_PORT", "587")); props.put("mail.smtp.auth", "true"); props.put("mail.smtp.starttls.enable", "true");
        String sender = value("IMS_SMTP_USER");
        Session session = Session.getInstance(props, new Authenticator() { @Override protected PasswordAuthentication getPasswordAuthentication() { return new PasswordAuthentication(sender, value("IMS_SMTP_PASSWORD")); } });
        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(sender, "IMS Portal"));
        message.setRecipient(Message.RecipientType.TO, new InternetAddress(recipientEmail));
        message.setSubject("Tài khoản IMS Portal của bạn đã được tạo");
        String htmlContent = "<div style='font-family:Arial,sans-serif;max-width:560px;margin:auto;padding:24px;border:1px solid #d9e2ef;border-radius:12px;color:#17365d;'>"
                + "<p><strong>Mật khẩu khởi tạo:</strong> " + html(initialPassword) + "</p>"
                + "<h2 style='margin-top:0;'>IMS Portal</h2><p>Chào " + html(fullName) + ",</p>"
                + "<p>Quản trị viên đã tạo tài khoản cho bạn trên hệ thống quản lý thực tập sinh.</p>"
                + "<table style='border-collapse:collapse;width:100%;margin:18px 0;'>"
                + "<tr><td style='padding:8px;border-bottom:1px solid #e5e7eb;'>Tên đăng nhập</td><td style='padding:8px;border-bottom:1px solid #e5e7eb;font-weight:bold;'>" + html(username) + "</td></tr>"
                + "<tr><td style='padding:8px;border-bottom:1px solid #e5e7eb;'>Email</td><td style='padding:8px;border-bottom:1px solid #e5e7eb;'>" + html(recipientEmail) + "</td></tr>"
                + "<tr><td style='padding:8px;'>Vai trò</td><td style='padding:8px;font-weight:bold;'>" + html(roleName) + "</td></tr></table>"
                + "<p><a href='" + html(loginUrl) + "' style='display:inline-block;background:#2c5282;color:#fff;padding:11px 18px;border-radius:7px;text-decoration:none;font-weight:bold;'>Đăng nhập IMS Portal</a></p>"
                + "<p style='font-size:13px;color:#64748b;'>Vui lòng đổi mật khẩu sau lần đăng nhập đầu tiên và không chia sẻ email này cho người khác.</p></div>";
        message.setContent(htmlContent, "text/html; charset=UTF-8");
        Transport.send(message);
    }

    private static String html(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;").replace("<", "&lt;")
                .replace(">", "&gt;").replace("\"", "&quot;").replace("'", "&#39;");
    }

    private static String value(String name) { String result = System.getenv(name); return result == null || result.isBlank() ? null : result.trim(); }
    private static String valueOr(String name, String fallback) { String result = value(name); return result == null ? fallback : result; }

    // Cấu hình Email gửi đi (Nên dùng App Password của Gmail)
    private static final String SMTP_HOST = "smtp.gmail.com";
    private static final String SMTP_PORT = "587";
    private static final String SENDER_EMAIL = "your-email@gmail.com"; // Email của bạn
    private static final String SENDER_PASSWORD = "xxxx xxxx xxxx xxxx"; // Mật khẩu ứng dụng (App Password)

    public static void sendOtpEmail(String recipientEmail, String otpCode) 
            throws MessagingException, UnsupportedEncodingException {

        Properties props = new Properties();
        props.put("mail.smtp.host", SMTP_HOST);
        props.put("mail.smtp.port", SMTP_PORT);
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");

        Authenticator auth = new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(SENDER_EMAIL, SENDER_PASSWORD);
            }
        };

        Session session = Session.getInstance(props, auth);

        Message message = new MimeMessage(session);
        
        // Dòng này gây ra ngoại lệ UnsupportedEncodingException
        message.setFrom(new InternetAddress(SENDER_EMAIL, "IMS Portal - Quản lý Thực tập sinh"));
        message.setRecipient(Message.RecipientType.TO, new InternetAddress(recipientEmail));
        message.setSubject("Mã xác minh OTP - Khôi phục mật khẩu");

        String htmlContent = "<div style='font-family: Arial, sans-serif; padding: 20px; background-color: #F7FAFC;'>"
                + "<div style='max-width: 500px; margin: 0 auto; background-color: #FFFFFF; border-radius: 8px; padding: 30px; border: 1px solid #E2E8F0;'>"
                + "<h2 style='color: #1B365D; margin-top: 0;'>🎓 IMS Portal</h2>"
                + "<p style='color: #4A5568;'>Bạn đã yêu cầu khôi phục mật khẩu. Dưới đây là mã xác minh OTP của bạn:</p>"
                + "<div style='text-align: center; margin: 25px 0;'>"
                + "<span style='font-size: 28px; font-weight: bold; letter-spacing: 6px; color: #2C5282; background-color: #EDF2F7; padding: 10px 20px; border-radius: 6px; border: 1px dashed #2C5282;'>" + otpCode + "</span>"
                + "</div>"
                + "<p style='color: #DD6B20; font-size: 13px;'>⚠️ Mã OTP này có hiệu lực trong <b>5 phút</b>. Vui lòng không chia sẻ mã này cho bất kỳ ai.</p>"
                + "<hr style='border: none; border-top: 1px solid #E2E8F0; margin-top: 20px;'>"
                + "<p style='font-size: 11px; color: #A0AEC0;'>Đây là email tự động, vui lòng không phản hồi.</p>"
                + "</div></div>";

        message.setContent(htmlContent, "text/html; charset=UTF-8");
        Transport.send(message);
    }
}
