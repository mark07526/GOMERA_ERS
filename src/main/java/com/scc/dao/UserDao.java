
package com.scc.dao;

import com.scc.model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.util.Base64;
import java.util.List;
import java.util.ArrayList;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public class UserDao {

    private static final int ITERATIONS = 210_000;
    private static final int KEY_LENGTH = 256;
    private static final int SALT_LENGTH = 16;

    private static final SecureRandom RANDOM = new SecureRandom();

    // ==============================
    // PASSWORD HASHING
    // ==============================

    private String hashPassword(String password) {
        try {
            byte[] salt = new byte[SALT_LENGTH];
            RANDOM.nextBytes(salt);

            KeySpec spec = new PBEKeySpec(
                    password.toCharArray(),
                    salt,
                    ITERATIONS,
                    KEY_LENGTH
            );

            SecretKeyFactory factory =
                    SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");

            byte[] hash = factory.generateSecret(spec).getEncoded();

            return Base64.getEncoder().encodeToString(salt)
                    + ":"
                    + Base64.getEncoder().encodeToString(hash);

        } catch (Exception e) {
            throw new RuntimeException("Password hashing failed.", e);
        }
    }

    private boolean verifyPassword(String password, String storedPassword) {
        try {
            String[] parts = storedPassword.split(":");

            if (parts.length != 2) {
                return false;
            }

            byte[] salt = Base64.getDecoder().decode(parts[0]);
            byte[] expectedHash = Base64.getDecoder().decode(parts[1]);

            KeySpec spec = new PBEKeySpec(
                    password.toCharArray(),
                    salt,
                    ITERATIONS,
                    KEY_LENGTH
            );

            SecretKeyFactory factory =
                    SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");

            byte[] actualHash = factory.generateSecret(spec).getEncoded();

            if (actualHash.length != expectedHash.length) {
                return false;
            }

            int difference = 0;

            for (int i = 0; i < actualHash.length; i++) {
                difference |= actualHash[i] ^ expectedHash[i];
            }

            return difference == 0;

        } catch (Exception e) {
            return false;
        }
    }

    // ==============================
    // REGISTER ACCOUNT
    // ==============================

    public void register(String username, String password, String role) {

        if (username == null || username.isBlank()
                || password == null || password.isBlank()) {
            System.out.println("Username and password are required.");
            return;
        }

        if (role == null) {
            System.out.println("Invalid role.");
            return;
        }

        role = role.trim().toUpperCase();

        if (!List.of("ADMIN", "ORGANIZER", "PARTICIPANT").contains(role)) {
            System.out.println("Invalid role.");
            return;
        }

        String sql = """
            INSERT INTO users (username, password, roles, status)
            VALUES (?, ?, ?, 'PENDING')
            """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, username.trim());
            statement.setString(2, hashPassword(password));
            statement.setString(3, role);

            statement.executeUpdate();

            System.out.println("Registration successful!");
            System.out.println("Your account is now PENDING approval.");

        } catch (SQLException e) {
            if (e.getMessage() != null
                    && e.getMessage().toLowerCase().contains("unique")) {
                System.out.println("Username already exists.");
            } else {
                System.out.println("Registration failed: " + e.getMessage());
            }
        }
    }

    // ==============================
    // LOGIN
    // ==============================

    public User login(String username, String password) {

        String sql = """
            SELECT id, username, password, roles, status
            FROM users
            WHERE username = ?
              AND COALESCE(is_active, 1) = 1
            """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, username.trim());

            try (ResultSet result = statement.executeQuery()) {

                if (result.next()) {

                    String storedPassword = result.getString("password");

                    if (verifyPassword(password, storedPassword)) {

                        return new User(
                                result.getInt("id"),
                                result.getString("username"),
                                storedPassword,
                                result.getString("roles"),
                                result.getString("status")
                        );
                    }
                }
            }

        } catch (SQLException e) {
            System.out.println("Login error: " + e.getMessage());
        }

        return null;
    }

    // ==============================
    // GET ALL USERS
    // ==============================

    public List<User> getAllUsers() {

        List<User> users = new ArrayList<>();

        String sql = """
            SELECT id, username, password, roles, status
            FROM users
            ORDER BY id
            """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {

            while (result.next()) {
                users.add(new User(
                        result.getInt("id"),
                        result.getString("username"),
                        result.getString("password"),
                        result.getString("roles"),
                        result.getString("status")
                ));
            }

        } catch (SQLException e) {
            System.out.println("Error retrieving users: " + e.getMessage());
        }

        return users;
    }

    // ==============================
    // GET PENDING USERS
    // ==============================

    public List<User> getPendingUsers() {

        List<User> users = new ArrayList<>();

        String sql = """
            SELECT id, username, password, roles, status
            FROM users
            WHERE status = 'PENDING'
              AND COALESCE(is_active, 1) = 1
            ORDER BY id
            """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {

            while (result.next()) {
                users.add(new User(
                        result.getInt("id"),
                        result.getString("username"),
                        result.getString("password"),
                        result.getString("roles"),
                        result.getString("status")
                ));
            }

        } catch (SQLException e) {
            System.out.println("Error retrieving pending users: " + e.getMessage());
        }

        return users;
    }

    // ==============================
    // APPROVE USER
    // ==============================

    public boolean approveUser(int userId) {

        String sql = """
            UPDATE users
            SET status = 'APPROVED'
            WHERE id = ?
              AND status = 'PENDING'
              AND COALESCE(is_active, 1) = 1
            """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, userId);

            int rows = statement.executeUpdate();

            if (rows > 0) {
                System.out.println("User approved successfully.");
                return true;
            }

            System.out.println("User not found or already approved.");
        } catch (SQLException e) {
            System.out.println("Approval failed: " + e.getMessage());
        }

        return false;
    }

    // ==============================
    // UPDATE USER ROLE AND STATUS
    // ==============================

    public boolean updateUserRoleAndStatus(
            int userId, String role, String status) {

        if (role == null || status == null) {
            System.out.println("Role and status are required.");
            return false;
        }

        role = role.trim().toUpperCase();
        status = status.trim().toUpperCase();

        if (!List.of("ADMIN", "ORGANIZER", "PARTICIPANT").contains(role)) {
            System.out.println("Invalid role.");
            return false;
        }

        if (!List.of("APPROVED", "PENDING").contains(status)) {
            System.out.println("Invalid status.");
            return false;
        }

        String sql = """
            UPDATE users
            SET roles = ?, status = ?
            WHERE id = ?
              AND COALESCE(is_active, 1) = 1
            """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, role);
            statement.setString(2, status);
            statement.setInt(3, userId);

            return statement.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("Update failed: " + e.getMessage());
        }

        return false;
    }

    // ==============================
    // SOFT DELETE / DEACTIVATE USER
    // ==============================

    public boolean deactivateUser(int userId) {

        String sql = """
            UPDATE users
            SET is_active = 0
            WHERE id = ?
              AND COALESCE(is_active, 1) = 1
            """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, userId);

            int rows = statement.executeUpdate();

            if (rows > 0) {
                System.out.println("User account deactivated successfully.");
                return true;
            }

            System.out.println("User not found or already deactivated.");

        } catch (SQLException e) {
            System.out.println("Deactivation failed: " + e.getMessage());
        }

        return false;
    }

    // ==============================
    // BACKWARD COMPATIBILITY
    // ==============================

    public User user(int id) {

        String sql = """
            SELECT id, username, password, roles, status
            FROM users
            WHERE id = ?
              AND COALESCE(is_active, 1) = 1
            """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, id);

            try (ResultSet result = statement.executeQuery()) {

                if (result.next()) {
                    return new User(
                            result.getInt("id"),
                            result.getString("username"),
                            result.getString("password"),
                            result.getString("roles"),
                            result.getString("status")
                    );
                }
            }

        } catch (SQLException e) {
            System.out.println("Error retrieving user: " + e.getMessage());
        }

        return null;
    }
}
