package com.example.internmanagement.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

class DatabaseInitializationTest {

    @Test
    void ensureDatabaseInitializedCreatesUsersTableAndSeedData() throws Exception {
        try (Connection connection = DriverManager.getConnection(
                "jdbc:h2:mem:db-init-test;MODE=MySQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE")) {
            DBConnection.ensureDatabaseInitialized(connection);

            try (Statement statement = connection.createStatement();
                 ResultSet resultSet = statement.executeQuery("SELECT COUNT(*) FROM users")) {
                assertTrue(resultSet.next());
                assertTrue(resultSet.getInt(1) > 0);
            }
            try (Statement statement = connection.createStatement();
                 ResultSet resultSet = statement.executeQuery(
                         "SELECT COUNT(*) FROM roles WHERE role_code = 'CANDIDATE'")) {
                assertTrue(resultSet.next());
                assertTrue(resultSet.getInt(1) == 1);
            }
            try (Statement statement = connection.createStatement();
                 ResultSet resultSet = statement.executeQuery("SELECT COUNT(*) FROM candidates")) {
                assertTrue(resultSet.next());
                assertTrue(resultSet.getInt(1) >= 0);
            }
            try (Statement statement = connection.createStatement();
                 ResultSet resultSet = statement.executeQuery("SELECT COUNT(*) FROM tasks")) {
                assertTrue(resultSet.next());
                assertTrue(resultSet.getInt(1) == 0);
            }
        }
        try (Connection connection = DriverManager.getConnection(
                "jdbc:h2:mem:db-init-notification-migration;MODE=MySQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE")) {
            try (Statement statement = connection.createStatement()) {
                statement.execute("CREATE TABLE users (id BIGINT PRIMARY KEY)");
                statement.execute("CREATE TABLE intern_profiles (id BIGINT PRIMARY KEY, user_id BIGINT NOT NULL)");
                statement.execute("CREATE TABLE notifications (id BIGINT AUTO_INCREMENT PRIMARY KEY, "
                        + "user_id BIGINT NOT NULL, title VARCHAR(200) NOT NULL, message TEXT, "
                        + "target_url VARCHAR(255), is_read BOOLEAN NOT NULL DEFAULT FALSE, "
                        + "created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP)");
            }

            DBConnection.ensureDatabaseInitialized(connection);

            try (Statement statement = connection.createStatement();
                 ResultSet resultSet = statement.executeQuery(
                         "SELECT notification_type, target_entity_type, target_entity_id, reminder_key "
                                 + "FROM notifications")) {
                assertFalse(resultSet.next());
            }
            try (Statement statement = connection.createStatement();
                 ResultSet resultSet = statement.executeQuery("SELECT COUNT(*) FROM meeting_attendees")) {
                assertTrue(resultSet.next());
                assertTrue(resultSet.getInt(1) == 0);
            }
            try (Statement statement = connection.createStatement()) {
                statement.execute("INSERT INTO notifications (user_id, title, reminder_key) "
                        + "VALUES (1, 'Nhắc lần một', 'once')");
                boolean duplicateRejected = false;
                try {
                    statement.execute("INSERT INTO notifications (user_id, title, reminder_key) "
                            + "VALUES (1, 'Nhắc lần hai', 'once')");
                } catch (java.sql.SQLException expected) {
                    duplicateRejected = true;
                }
                assertTrue(duplicateRejected);
            }
        }
    }
}
