package com.syntropic.industrial.reports.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Controller
public class ApplicationController {

    @Value("${appmode:sqlserver}")
    private String appMode;

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("mode", appMode.toUpperCase());
        return "index";
    }

    @GetMapping("/test-database")
    public String testDatabasePage() {
        return "TestDatabase";
    }
    @GetMapping("/test-database2")
    public String testDatabase2Page() {
        return "TestDatabase2";
    }

    public record DbRequest(
            String connectionString,
            String username,
            String password,
            String query
    ) {}

    private DriverManagerDataSource createDataSource(DbRequest req) {
        if (req.connectionString() == null || req.connectionString().isBlank()) {
            throw new IllegalArgumentException("Connection string cannot be empty.");
        }
        DriverManagerDataSource ds = new DriverManagerDataSource();
        ds.setDriverClassName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
        ds.setUrl(req.connectionString().trim());

        // Apply username/password if provided separately from the connection string
        if (req.username() != null && !req.username().isBlank()) {
            ds.setUsername(req.username().trim());
        }
        if (req.password() != null && !req.password().isEmpty()) {
            ds.setPassword(req.password());
        }
        return ds;
    }

    @PostMapping("/api/db/connect")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> connect(@RequestBody DbRequest req) {
        try {
            DriverManagerDataSource ds = createDataSource(req);
            try (Connection conn = ds.getConnection()) {
                DatabaseMetaData meta = conn.getMetaData();
                String info = meta.getDatabaseProductName() + " " + meta.getDatabaseProductVersion();
                return ResponseEntity.ok(Map.of(
                        "success", true,
                        "message", "Connected! (" + info + ")"
                ));
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Connection failed: " + e.getMessage()
            ));
        }
    }

    @PostMapping("/api/db/query")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> executeQuery(@RequestBody DbRequest req) {
        try {
            if (req.query() == null || req.query().isBlank()) {
                throw new IllegalArgumentException("SQL query cannot be empty.");
            }
            JdbcTemplate jdbc = new JdbcTemplate(createDataSource(req));
            List<Map<String, Object>> rows = jdbc.queryForList(req.query().trim());
            List<String> columns = rows.isEmpty() ? List.of() : new ArrayList<>(rows.get(0).keySet());

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "columns", columns,
                    "rows", rows
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Query failed: " + e.getMessage()
            ));
        }
    }

}
