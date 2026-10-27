package com.scc.dao;

import com.scc.model.User;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Locale;

public class UserDao {

    private static final int ITERATIONS = 210_000;
    private static final int KEY_LENGTH = 256;
    private static final SecureRandom RANDOM = new SecureRandom();

    // =========================
    // PASSWORD HASHING
    // =========================

    private String hashPassword(String password) {
        byte[] salt = new byte[16];
        RANDOM.nextBytes(salt);

        byte[] hash = deriveKey(password, salt);

        return Base64.getEncoder().encodeToString(salt)
                + ":"
                + Base64.getEncoder().encodeToString(hash);
    }

    private byte[] deriveKey(String password, byte[] salt) {
        PBEKeySpec spec = new PBEKeySpec(
                password.toCharArray(),
                salt,
                ITERATIONS,
                KEY_LENGTH
        );

        try {
            SecretKeyFactory factory =
                    SecretKeyFactory.getInstance(
                            "PBKDF2WithHmacSHA256"
                    );

            return factory.generateSecret(spec).getEncoded();

        } catch (NoSuchAlgorithmException
                 | InvalidKeySpecException e) {

            throw new IllegalStateException(
                    "Password hashing failed.", e
            );

        } finally {
            spec.clearPassword();
        }
    }

    private boolean verifyPassword(
            String password, String storedPassword) {

        if (storedPassword == null) {
            return false;
        }

        try {
            String[] parts = storedPassword.split(":");

            if (parts.length != 2) {
                return false;
            }

            byte[] salt =
                    Base64.getDecoder().decode(parts[0]);

            byte[] expectedHash =
                    Base64.getDecoder().decode(parts[1]);

            byte[] actualHash = deriveKey(password, salt);

            return MessageDigest.isEqual(
                    expectedHash, actualHash
            );

        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    // =========================
    // REGISTER USER
    // =========================

    public boolean register(
            String username, String password, String role) {

        if (username == null || username.isBlank()
                || password == null || password.isEmpty()) {

            System.out.println(
                    "Username and password are required."
            );
            return false;
        }

        if (role == null) {
            System.out.println("Invalid role.");
            return false;
        }

        String normalizedRole =
                role.trim().toUpperCase(Locale.ROOT);

        if (!normalizedRole.equals("ORGANIZER")
                && !normalizedRole.equals("PARTICIPANT")
                && !normalizedRole.equals("ADMIN")) {

            System.out.println("Invalid role.");
            return false;
        }

        String sql = """
                INSERT INTO users (username, password, roles, status)
                VALUES (?, ?, ?, 'PENDING')
                """;

        try (Connection connection =
                     DatabaseConnection.getConnection();
             PreparedStatement ps =
                     connection.prepareStatement(sql)) {

            ps.setString(1, username.trim());
            ps.setString(2, hashPassword(password));
            ps.setString(3, normalizedRole);

            ps.executeUpdate();

            System.out.println("Registration successful!");
            System.out.println("Account status: PENDING");

            return true;

        } catch (SQLException e) {

            if (e.getMessage() != null
                    && e.getMessage().contains(
                    "UNIQUE constraint failed")) {

                System.out.println("Username already exists.");

            } else {
                System.out.println(
                        "Registration failed: " + e.getMessage()
                );
            }

            return false;
        }
    }

    // =========================
    // LOGIN USER
    // =========================

    public User login(String username, String password) {

        if (username == null || username.isBlank()
                || password == null || password.isEmpty()) {
            return null;
        }

        String sql = """
                SELECT id, username, password, roles, status
                FROM users
                WHERE username = ?
                """;

        try (Connection connection =
                     DatabaseConnection.getConnection();
             PreparedStatement ps =
                     connection.prepareStatement(sql)) {

            ps.setString(1, username.trim());

            try (ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {

                    String storedPassword =
                            rs.getString("password");

                    if (verifyPassword(
                            password, storedPassword)) {

                        return new User(
                                rs.getInt("id"),
                                rs.getString("username"),
                                storedPassword,
                                rs.getString("roles"),
                                rs.getString("status")
                        );
                    }
                }
            }

        } catch (SQLException e) {
            System.out.println(
                    "Login failed: " + e.getMessage()
            );
        }

        return null;
    }

    // =========================
    // GET ALL USERS
    // =========================

    public List<User> getAllUsers() {

        List<User> users = new ArrayList<>();

        String sql = """
                SELECT id, username, password, roles, status
                FROM users
                ORDER BY id
                """;

        try (Connection connection =
                     DatabaseConnection.getConnection();
             PreparedStatement ps =
                     connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {

                User user = new User(
                        rs.getInt("id"),
                        rs.getString("username"),
                        rs.getString("password"),
                        rs.getString("roles"),
                        rs.getString("status")
                );

                users.add(user);
            }

        } catch (SQLException e) {
            System.out.println(
                    "Unable to retrieve users: " + e.getMessage()
            );
        }

        return users;
    }

    // =========================
    // GET PENDING USERS
    // =========================

    public List<User> getPendingUsers() {

        List<User> users = new ArrayList<>();

        String sql = """
                SELECT id, username, password, roles, status
                FROM users
                WHERE status = 'PENDING'
                ORDER BY id
                """;

        try (Connection connection =
                     DatabaseConnection.getConnection();
             PreparedStatement ps =
                     connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {

                User user = new User(
                        rs.getInt("id"),
                        rs.getString("username"),
                        rs.getString("password"),
                        rs.getString("roles"),
                        rs.getString("status")
                );

                users.add(user);
            }

        } catch (SQLException e) {
            System.out.println(
                    "Unable to retrieve pending users: "
                            + e.getMessage()
            );
        }

        return users;
    }

    // =========================
    // APPROVE USER
    // =========================

    public boolean approveUser(int userId) {

        if (userId <= 0) {
            System.out.println("Invalid user ID.");
            return false;
        }

        String sql = """
                UPDATE users
                SET status = 'APPROVED'
                WHERE id = ?
                  AND status = 'PENDING'
                """;

        try (Connection connection =
                     DatabaseConnection.getConnection();
             PreparedStatement ps =
                     connection.prepareStatement(sql)) {

            ps.setInt(1, userId);

            int rowsUpdated = ps.executeUpdate();

            if (rowsUpdated > 0) {
                System.out.println(
                        "User approved successfully!"
                );
                return true;
            }

            System.out.println(
                    "User not found or account is not pending."
            );
            return false;

        } catch (SQLException e) {
            System.out.println(
                    "Unable to approve user: " + e.getMessage()
            );
            return false;
        }
    }

    // =========================
    // UPDATE USER ROLE AND STATUS
    // =========================

    public boolean updateUserRoleAndStatus(
            int userId,
            String newRole,
            String newStatus) {

        if (userId <= 0) {
            System.out.println("Invalid user ID.");
            return false;
        }

        if (newRole == null || newStatus == null) {
            System.out.println(
                    "Role and status are required."
            );
            return false;
        }

        String normalizedRole =
                newRole.trim().toUpperCase(Locale.ROOT);

        String normalizedStatus =
                newStatus.trim().toUpperCase(Locale.ROOT);

        // Validate role
        if (!normalizedRole.equals("PARTICIPANT")
                && !normalizedRole.equals("ORGANIZER")
                && !normalizedRole.equals("ADMIN")) {

            System.out.println("Invalid role.");
            return false;
        }

        // Validate status
        if (!normalizedStatus.equals("APPROVED")
                && !normalizedStatus.equals("PENDING")) {

            System.out.println("Invalid status.");
            return false;
        }

        String sql = """
                UPDATE users
                SET roles = ?, status = ?
                WHERE id = ?
                """;

        try (Connection connection =
                     DatabaseConnection.getConnection();
             PreparedStatement ps =
                     connection.prepareStatement(sql)) {

            ps.setString(1, normalizedRole);
            ps.setString(2, normalizedStatus);
            ps.setInt(3, userId);

            int rowsUpdated = ps.executeUpdate();

            if (rowsUpdated > 0) {
                System.out.println(
                        "User role and status updated successfully!"
                );
                return true;
            }

            System.out.println(
                    "Update failed. User ID not found."
            );
            return false;

        } catch (SQLException e) {
            System.out.println(
                    "Unable to update user: " + e.getMessage()
            );
            return false;
        }
    }

    // =========================
    // BACKWARD COMPATIBILITY
    // =========================

    public List<User> user() {
        return getAllUsers();
    }
}