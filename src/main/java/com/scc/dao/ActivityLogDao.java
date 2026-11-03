
package com.scc.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ActivityLogDao {

    // =================================
    // RECORD USER ACTIVITY
    // =================================

    public boolean logActivity(
            int userId,
            String action,
            String details) {

        if (action == null || action.isBlank()) {
            System.out.println("Activity action is required.");
            return false;
        }

        String sql = """
            INSERT INTO activity_logs (
                user_id,
                action,
                details
            )
            VALUES (?, ?, ?)
            """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            if (userId > 0) {
                statement.setInt(1, userId);
            } else {
                statement.setNull(1, java.sql.Types.INTEGER);
            }

            statement.setString(2, action);
            statement.setString(3, details);

            statement.executeUpdate();

            return true;

        } catch (SQLException e) {
            System.out.println("Activity logging failed: " + e.getMessage());
            return false;
        }
    }

    // =================================
    // GET ALL ACTIVITY LOGS
    // =================================

    public List<String> getAllActivityLogs() {

        List<String> logs = new ArrayList<>();

        String sql = """
            SELECT
                activity_logs.id,
                activity_logs.user_id,
                users.username,
                activity_logs.action,
                activity_logs.details,
                activity_logs.activity_date
            FROM activity_logs
            LEFT JOIN users
                ON activity_logs.user_id = users.id
            ORDER BY activity_logs.id DESC
            """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {

            while (result.next()) {

                String username = result.getString("username");

                if (username == null) {
                    username = "Unknown / Deleted User";
                }

                String log =
                        "Log ID: " + result.getInt("id")
                                + "\nUser ID: " + result.getString("user_id")
                                + "\nUsername: " + username
                                + "\nAction: " + result.getString("action")
                                + "\nDetails: " + result.getString("details")
                                + "\nDate: " + result.getString("activity_date")
                                + "\n-----------------------------";

                logs.add(log);
            }

        } catch (SQLException e) {
            System.out.println("Error retrieving logs: " + e.getMessage());
        }

        return logs;
    }

    // =================================
    // GET ACTIVITY LOGS BY USER
    // =================================

    public List<String> getActivityLogsByUser(int userId) {

        List<String> logs = new ArrayList<>();

        String sql = """
            SELECT id, user_id, action, details, activity_date
            FROM activity_logs
            WHERE user_id = ?
            ORDER BY id DESC
            """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, userId);

            try (ResultSet result = statement.executeQuery()) {

                while (result.next()) {

                    String log =
                            "Log ID: " + result.getInt("id")
                                    + "\nAction: " + result.getString("action")
                                    + "\nDetails: " + result.getString("details")
                                    + "\nDate: " + result.getString("activity_date")
                                    + "\n-----------------------------";

                    logs.add(log);
                }
            }

        } catch (SQLException e) {
            System.out.println("Error retrieving user logs: "
                    + e.getMessage());
        }

        return logs;
    }

    // =================================
    // GET ACTIVITY LOGS BY ACTION
    // =================================

    public List<String> getActivityLogsByAction(String action) {

        List<String> logs = new ArrayList<>();

        String sql = """
            SELECT
                activity_logs.id,
                activity_logs.user_id,
                users.username,
                activity_logs.action,
                activity_logs.details,
                activity_logs.activity_date
            FROM activity_logs
            LEFT JOIN users
                ON activity_logs.user_id = users.id
            WHERE activity_logs.action LIKE ?
            ORDER BY activity_logs.id DESC
            """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, "%" + action + "%");

            try (ResultSet result = statement.executeQuery()) {

                while (result.next()) {

                    String username = result.getString("username");

                    if (username == null) {
                        username = "Unknown / Deleted User";
                    }

                    String log =
                            "Log ID: " + result.getInt("id")
                                    + "\nUsername: " + username
                                    + "\nAction: " + result.getString("action")
                                    + "\nDetails: " + result.getString("details")
                                    + "\nDate: " + result.getString("activity_date")
                                    + "\n-----------------------------";

                    logs.add(log);
                }
            }

        } catch (SQLException e) {
            System.out.println("Error searching activity logs: "
                    + e.getMessage());
        }

        return logs;
    }

    // =================================
    // DISPLAY ALL ACTIVITY LOGS
    // =================================

    public void displayAllActivityLogs() {

        List<String> logs = getAllActivityLogs();

        if (logs.isEmpty()) {
            System.out.println("No activity logs found.");
            return;
        }

        System.out.println("\n===== SYSTEM ACTIVITY LOGS =====");

        for (String log : logs) {
            System.out.println(log);
        }
    }
}
