package com.example.internmanagement.util;

import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.assertTrue;

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
    }
}
