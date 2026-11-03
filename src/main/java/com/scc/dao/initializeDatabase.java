
package com.scc.dao;

import java.sql.Connection;
import java.sql.Statement;
import java.sql.SQLException;

public class initializeDatabase {

    public static void initialize() {

        String createUsersTable = """
            CREATE TABLE IF NOT EXISTS users (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                username TEXT NOT NULL UNIQUE,
                password TEXT NOT NULL,
                roles TEXT NOT NULL
                    CHECK (roles IN (
                        'ADMIN',
                        'ORGANIZER',
                        'PARTICIPANT'
                    )),
                status TEXT NOT NULL DEFAULT 'PENDING'
                    CHECK (status IN ('PENDING', 'APPROVED')),
                is_active INTEGER NOT NULL DEFAULT 1
            );
            """;

        String createEventsTable = """
            CREATE TABLE IF NOT EXISTS events (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                event_name TEXT NOT NULL,
                description TEXT,
                category TEXT NOT NULL,
                event_date TEXT NOT NULL,
                location TEXT NOT NULL,
                capacity INTEGER NOT NULL CHECK (capacity > 0),
                created_by INTEGER,
                is_active INTEGER NOT NULL DEFAULT 1,
                created_at TEXT DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (created_by) REFERENCES users(id)
            );
            """;

        String createRegistrationsTable = """
            CREATE TABLE IF NOT EXISTS registrations (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                user_id INTEGER NOT NULL,
                event_id INTEGER NOT NULL,
                registration_date TEXT DEFAULT CURRENT_TIMESTAMP,
                status TEXT NOT NULL DEFAULT 'REGISTERED'
                    CHECK (status IN (
                        'REGISTERED',
                        'ATTENDED',
                        'CANCELLED'
                    )),
                FOREIGN KEY (user_id) REFERENCES users(id),
                FOREIGN KEY (event_id) REFERENCES events(id),
                UNIQUE (user_id, event_id)
            );
            """;

        String createActivityLogsTable = """
            CREATE TABLE IF NOT EXISTS activity_logs (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                user_id INTEGER,
                action TEXT NOT NULL,
                details TEXT,
                activity_date TEXT DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (user_id) REFERENCES users(id)
            );
            """;

        try (Connection connection = DatabaseConnection.getConnection()) {

            // Enable foreign key support
            try (Statement statement = connection.createStatement()) {
                statement.execute("PRAGMA foreign_keys = ON");

                // Create tables
                statement.execute(createUsersTable);
                statement.execute(createEventsTable);
                statement.execute(createRegistrationsTable);
                statement.execute(createActivityLogsTable);

                // Migration for existing users table
                addColumnIfMissing(
                        connection,
                        "users",
                        "is_active",
                        "INTEGER NOT NULL DEFAULT 1"
                );

                // Migration for existing events table
                addColumnIfMissing(
                        connection,
                        "events",
                        "category",
                        "TEXT NOT NULL DEFAULT 'GENERAL'"
                );

                addColumnIfMissing(
                        connection,
                        "events",
                        "capacity",
                        "INTEGER NOT NULL DEFAULT 100"
                );

                addColumnIfMissing(
                        connection,
                        "events",
                        "created_by",
                        "INTEGER"
                );

                addColumnIfMissing(
                        connection,
                        "events",
                        "is_active",
                        "INTEGER NOT NULL DEFAULT 1"
                );

                addColumnIfMissing(
                        connection,
                        "events",
                        "created_at",
                        "TEXT"
                );

                System.out.println("Database initialized successfully!");
            }

        } catch (SQLException e) {
            System.out.println("Database initialization failed: "
                    + e.getMessage());
            e.printStackTrace();
        }
    }

    // Check if a column already exists before adding it
    private static void addColumnIfMissing(
            Connection connection,
            String tableName,
            String columnName,
            String columnDefinition) throws SQLException {

        boolean exists = false;

        String sql = "PRAGMA table_info(" + tableName + ")";

        try (Statement statement = connection.createStatement();
             var result = statement.executeQuery(sql)) {

            while (result.next()) {
                if (columnName.equalsIgnoreCase(
                        result.getString("name"))) {
                    exists = true;
                    break;
                }
            }
        }

        if (!exists) {
            String alterSql = "ALTER TABLE " + tableName
                    + " ADD COLUMN " + columnName + " " + columnDefinition;

            try (Statement statement = connection.createStatement()) {
                statement.executeUpdate(alterSql);
                System.out.println("Added column: "
                        + tableName + "." + columnName);
            }
        }
    }
}
