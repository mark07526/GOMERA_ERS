
package com.scc.dao;

import com.scc.model.Registration;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class RegistrationDao {

    // =================================
    // REGISTER PARTICIPANT FOR EVENT
    // =================================

    public boolean registerParticipant(int userId, int eventId) {

        String userSql = """
            SELECT id
            FROM users
            WHERE id = ?
              AND roles = 'PARTICIPANT'
              AND status = 'APPROVED'
              AND is_active = 1
            """;

        String eventSql = """
            SELECT capacity
            FROM events
            WHERE id = ?
              AND is_active = 1
            """;

        String countSql = """
            SELECT COUNT(*)
            FROM registrations
            WHERE event_id = ?
              AND status IN ('REGISTERED', 'ATTENDED')
            """;

        String existingSql = """
            SELECT id, status
            FROM registrations
            WHERE user_id = ? AND event_id = ?
            """;

        String insertSql = """
            INSERT INTO registrations (user_id, event_id, status)
            VALUES (?, ?, 'REGISTERED')
            """;

        String reactivateSql = """
            UPDATE registrations
            SET status = 'REGISTERED',
                registration_date = CURRENT_TIMESTAMP
            WHERE user_id = ?
              AND event_id = ?
              AND status = 'CANCELLED'
            """;

        try (Connection connection = DatabaseConnection.getConnection()) {

            connection.setAutoCommit(false);

            try {
                // Check participant account
                try (PreparedStatement statement =
                             connection.prepareStatement(userSql)) {

                    statement.setInt(1, userId);

                    try (ResultSet result = statement.executeQuery()) {
                        if (!result.next()) {
                            throw new SQLException(
                                    "Participant account is invalid, inactive, or not approved."
                            );
                        }
                    }
                }

                // Check event and capacity
                int capacity;

                try (PreparedStatement statement =
                             connection.prepareStatement(eventSql)) {

                    statement.setInt(1, eventId);

                    try (ResultSet result = statement.executeQuery()) {
                        if (!result.next()) {
                            throw new SQLException(
                                    "Event not found or inactive."
                            );
                        }

                        capacity = result.getInt("capacity");
                    }
                }

                // Check existing registration
                boolean existing = false;
                String existingStatus = null;

                try (PreparedStatement statement =
                             connection.prepareStatement(existingSql)) {

                    statement.setInt(1, userId);
                    statement.setInt(2, eventId);

                    try (ResultSet result = statement.executeQuery()) {
                        if (result.next()) {
                            existing = true;
                            existingStatus = result.getString("status");
                        }
                    }
                }

                if (existing && !"CANCELLED".equals(existingStatus)) {
                    throw new SQLException(
                            "You are already registered for this event."
                    );
                }

                // Count occupied slots
                int registeredCount;

                try (PreparedStatement statement =
                             connection.prepareStatement(countSql)) {

                    statement.setInt(1, eventId);

                    try (ResultSet result = statement.executeQuery()) {
                        result.next();
                        registeredCount = result.getInt(1);
                    }
                }

                if (registeredCount >= capacity) {
                    throw new SQLException("Event is already full.");
                }

                // Insert or reactivate registration
                if (existing) {
                    try (PreparedStatement statement =
                                 connection.prepareStatement(reactivateSql)) {

                        statement.setInt(1, userId);
                        statement.setInt(2, eventId);
                        statement.executeUpdate();
                    }
                } else {
                    try (PreparedStatement statement =
                                 connection.prepareStatement(insertSql)) {

                        statement.setInt(1, userId);
                        statement.setInt(2, eventId);
                        statement.executeUpdate();
                    }
                }

                connection.commit();

                System.out.println("Event registration successful!");
                return true;

            } catch (SQLException e) {
                connection.rollback();
                System.out.println("Registration failed: " + e.getMessage());
                return false;

            } finally {
                connection.setAutoCommit(true);
            }

        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
            return false;
        }
    }

    // =================================
    // VIEW PARTICIPANT REGISTRATION HISTORY
    // =================================

    public List<Registration> getRegistrationHistory(int userId) {

        List<Registration> registrations = new ArrayList<>();

        String sql = """
            SELECT id, user_id, event_id, registration_date, status
            FROM registrations
            WHERE user_id = ?
            ORDER BY registration_date DESC
            """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, userId);

            try (ResultSet result = statement.executeQuery()) {

                while (result.next()) {
                    registrations.add(mapRegistration(result));
                }
            }

        } catch (SQLException e) {
            System.out.println("Error retrieving history: " + e.getMessage());
        }

        return registrations;
    }

    // =================================
    // VIEW REGISTRATIONS FOR AN EVENT
    // =================================

    public List<Registration> getRegistrationsForEvent(int eventId) {

        List<Registration> registrations = new ArrayList<>();

        String sql = """
            SELECT id, user_id, event_id, registration_date, status
            FROM registrations
            WHERE event_id = ?
            ORDER BY registration_date
            """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, eventId);

            try (ResultSet result = statement.executeQuery()) {

                while (result.next()) {
                    registrations.add(mapRegistration(result));
                }
            }

        } catch (SQLException e) {
            System.out.println("Error retrieving event registrations: "
                    + e.getMessage());
        }

        return registrations;
    }

    // =================================
    // CANCEL PARTICIPANT REGISTRATION
    // =================================

    public boolean cancelRegistration(int registrationId, int userId) {

        String sql = """
            UPDATE registrations
            SET status = 'CANCELLED'
            WHERE id = ?
              AND user_id = ?
              AND status = 'REGISTERED'
            """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, registrationId);
            statement.setInt(2, userId);

            int rows = statement.executeUpdate();

            if (rows > 0) {
                System.out.println("Registration cancelled successfully!");
                return true;
            }

            System.out.println(
                    "Registration not found or cannot be cancelled."
            );

        } catch (SQLException e) {
            System.out.println("Cancellation failed: " + e.getMessage());
        }

        return false;
    }

    // =================================
    // UPDATE REGISTRATION STATUS
    // =================================

    public boolean updateRegistrationStatus(
            int registrationId, String status) {

        if (status == null) {
            System.out.println("Status is required.");
            return false;
        }

        status = status.trim().toUpperCase();

        if (!List.of("REGISTERED", "ATTENDED", "CANCELLED")
                .contains(status)) {
            System.out.println("Invalid registration status.");
            return false;
        }

        String sql = """
            UPDATE registrations
            SET status = ?
            WHERE id = ?
            """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, status);
            statement.setInt(2, registrationId);

            int rows = statement.executeUpdate();

            if (rows > 0) {
                System.out.println("Registration status updated!");
                return true;
            }

            System.out.println("Registration not found.");

        } catch (SQLException e) {
            System.out.println("Status update failed: " + e.getMessage());
        }

        return false;
    }

    // =================================
    // RECORD ATTENDANCE
    // =================================

    public boolean markAttendance(int registrationId) {
        return updateRegistrationStatus(registrationId, "ATTENDED");
    }

    // =================================
    // GET REGISTRATION BY ID
    // =================================

    public Registration getRegistrationById(int registrationId) {

        String sql = """
            SELECT id, user_id, event_id, registration_date, status
            FROM registrations
            WHERE id = ?
            """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, registrationId);

            try (ResultSet result = statement.executeQuery()) {

                if (result.next()) {
                    return mapRegistration(result);
                }
            }

        } catch (SQLException e) {
            System.out.println("Error retrieving registration: "
                    + e.getMessage());
        }

        return null;
    }

    // =================================
    // MAP DATABASE DATA TO REGISTRATION
    // =================================

    private Registration mapRegistration(ResultSet result)
            throws SQLException {

        return new Registration(
                result.getInt("id"),
                result.getInt("user_id"),
                result.getInt("event_id"),
                result.getString("registration_date"),
                result.getString("status")
        );
    }
}
