package com.example.internmanagement.architecture;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

class DatabaseSchemaContractTest {
    private static String schema() throws IOException {
        return Files.readString(Path.of("src", "main", "resources", "database.sql"));
    }

    @Test
    void unifiedSchemaContainsEveryBusinessAggregate() throws IOException {
        String sql = schema().toLowerCase();
        Set<String> required = Set.of(
                "users", "roles", "permissions", "intern_profiles", "intern_documents",
                "internship_programs", "mentors", "contracts", "tasks", "weekly_reports",
                "work_schedules", "attendance_records", "leave_requests", "notifications",
                "meetings", "mentor_final_evaluations", "report_snapshots", "report_send_logs"
        );
        for (String table : required) {
            assertTrue(sql.matches("(?s).*create\\s+table(?:\\s+if\\s+not\\s+exists)?\\s+" + table + "\\b.*"),
                    "Missing table in unified database.sql: " + table);
        }
    }

    @Test
    void foreignKeysReferenceDeclaredTables() throws IOException {
        String sql = schema().toLowerCase();
        Set<String> declared = new java.util.HashSet<>();
        Matcher tables = Pattern.compile("create\\s+table(?:\\s+if\\s+not\\s+exists)?\\s+(\\w+)").matcher(sql);
        while (tables.find()) declared.add(tables.group(1));
        Matcher references = Pattern.compile("references\\s+(\\w+)\\s*\\(").matcher(sql);
        while (references.find()) assertTrue(declared.contains(references.group(1)), "Foreign key references missing table: " + references.group(1));
        assertTrue(declared.size() >= 37, "Unified schema unexpectedly contains only " + declared.size() + " tables");
    }

    @Test
    void demoAccountsAndRoleAssignmentsAreSeeded() throws IOException {
        String sql = schema();
        for (String account : Set.of("admin_demo", "hr_demo", "mentor_demo", "intern_demo")) {
            assertTrue(sql.contains("'" + account + "'"), "Missing demo account: " + account);
        }
        assertTrue(sql.contains("INSERT IGNORE INTO user_roles"));
    }
}
