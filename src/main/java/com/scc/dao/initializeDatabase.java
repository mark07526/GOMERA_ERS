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
                        'PARTICIPANT',
                        'ORGANIZER',
                        'STUDENT',
                        'TEACHER'
                    )),
                status TEXT NOT NULL DEFAULT 'PENDING'
            );
            """;

        String createEventsTable = """
            CREATE TABLE IF NOT EXISTS events (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                event_name TEXT NOT NULL,
                description TEXT,
                event_date TEXT NOT NULL,
                location TEXT
            );
            """;

        String createRegistrationsTable = """
            CREATE TABLE IF NOT EXISTS registrations (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                user_id INTEGER NOT NULL,
                event_id INTEGER NOT NULL,
                registration_date TEXT DEFAULT CURRENT_TIMESTAMP,
                status TEXT NOT NULL DEFAULT 'REGISTERED',
                FOREIGN KEY (user_id) REFERENCES users(id),
                FOREIGN KEY (event_id) REFERENCES events(id),
                UNIQUE (user_id, event_id)
            );
            """;

        try (Connection connection = DatabaseConnection.getConnection();
             Statement statement = connection.createStatement()) {

            // Create the tables if they do not exist.
            statement.execute(createUsersTable);
            statement.execute(createEventsTable);
            statement.execute(createRegistrationsTable);

            // Check whether the existing users table has an old role restriction.
            boolean hasOldRoleConstraint = false;

            try (var result = statement.executeQuery(
                    "SELECT sql FROM sqlite_master " +
                            "WHERE type = 'table' AND name = 'users'")) {

                if (result.next()) {
                    String tableSql = result.getString("sql");

                    if (tableSql != null &&
                            tableSql.contains(
                                    "CHECK (roles IN ('ORGANIZER', 'PARTICIPANT'))")) {
                        hasOldRoleConstraint = true;
                    }
                }
            }

            if (hasOldRoleConstraint) {
                statement.execute("PRAGMA foreign_keys = OFF");

                try {
                    connection.setAutoCommit(false);

                    // 1. Create the new users table with all roles
                    statement.execute("""
                        CREATE TABLE users_new (
                            id INTEGER PRIMARY KEY AUTOINCREMENT,
                            username TEXT NOT NULL UNIQUE,
                            password TEXT NOT NULL,
                            roles TEXT NOT NULL
                                CHECK (roles IN (
                                    'ADMIN',
                                    'PARTICIPANT',
                                    'ORGANIZER',
                                    'STUDENT',
                                    'TEACHER'
                                )),
                            status TEXT NOT NULL DEFAULT 'PENDING'
                        );
                        """);

                    // 2. Copy data from old table to new table
                    statement.execute("""
                        INSERT INTO users_new (id, username, password, roles, status)
                        SELECT id, username, password, roles, status FROM users;
                        """);

                    // 3. Drop old table and rename new table
                    statement.execute("DROP TABLE users;");
                    statement.execute("ALTER TABLE users_new RENAME TO users;");

                    connection.commit();
                    connection.setAutoCommit(true);
                } catch (SQLException e) {
                    connection.rollback();
                    connection.setAutoCommit(true);
                    throw e;
                } finally {
                    statement.execute("PRAGMA foreign_keys = ON");
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}