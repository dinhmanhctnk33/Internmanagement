package com.example.internmanagement.architecture;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class ArchitectureContractTest {
    private static final Path MAIN = Path.of("src", "main", "java");
    private static final Path WEB = Path.of("src", "webapp");

    @Test
    void controllerViewsExistAndStayProtectedUnderWebInf() throws IOException {
        Pattern dispatcher = Pattern.compile("getRequestDispatcher\\(\\s*\"([^\"]+)\"");
        try (Stream<Path> files = Files.walk(MAIN.resolve("com/example/internmanagement/controller"))) {
            for (Path file : files.filter(p -> p.toString().endsWith(".java")).toList()) {
                Matcher matcher = dispatcher.matcher(Files.readString(file));
                while (matcher.find()) {
                    String view = matcher.group(1);
                    if (!view.endsWith(".jsp")) continue;
                    assertTrue(view.startsWith("/WEB-INF/views/"), file + " exposes JSP outside WEB-INF: " + view);
                    assertTrue(Files.exists(WEB.resolve(view.substring(1))), file + " forwards to missing view: " + view);
                }
            }
        }
    }

    @Test
    void servletRoutesAreUnique() throws IOException {
        Pattern annotation = Pattern.compile("@WebServlet\\((.*?)\\)", Pattern.DOTALL);
        Pattern route = Pattern.compile("\"(/[^\"]+)\"");
        Set<String> routes = new HashSet<>();
        try (Stream<Path> files = Files.walk(MAIN.resolve("com/example/internmanagement/controller"))) {
            for (Path file : files.filter(p -> p.toString().endsWith(".java")).toList()) {
                Matcher block = annotation.matcher(Files.readString(file));
                while (block.find()) {
                    Matcher values = route.matcher(block.group(1));
                    while (values.find()) assertTrue(routes.add(values.group(1)), "Duplicate servlet route: " + values.group(1));
                }
            }
        }
        assertTrue(routes.size() >= 40, "Expected the complete servlet surface, found only " + routes.size());
    }

    @Test
    void layersDoNotDependOnControllersOrViews() throws IOException {
        for (String layer : Set.of("dao", "model")) {
            try (Stream<Path> files = Files.walk(MAIN.resolve("com/example/internmanagement/" + layer))) {
                for (Path file : files.filter(p -> p.toString().endsWith(".java")).toList()) {
                    String source = Files.readString(file);
                    assertFalse(source.contains("com.example.internmanagement.controller"), file + " depends on controller layer");
                    assertFalse(source.contains("jakarta.servlet"), file + " depends on web framework");
                    assertFalse(source.contains("/WEB-INF/views"), file + " depends on a JSP view");
                }
            }
        }
    }

    @Test
    void databaseAccessIsConfinedOutsideModels() throws IOException {
        try (Stream<Path> files = Files.walk(MAIN.resolve("com/example/internmanagement/model"))) {
            for (Path file : files.filter(p -> p.toString().endsWith(".java")).toList()) {
                String source = Files.readString(file);
                assertFalse(source.contains("java.sql"), file + " leaks persistence into the model layer");
                assertFalse(source.contains("DBConnection"), file + " opens a database connection from a model");
            }
        }
    }
}
