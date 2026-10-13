
package com.scc.dao;

import com.scc.model.User;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Base64;

public class UserDao {

    private static final int ITERATIONS = 210_000;
    private static final int KEY_LENGTH = 256;
    private static final SecureRandom RANDOM = new SecureRandom();

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
            SecretKeyFactory factory = SecretKeyFactory.getInstance(
                    "PBKDF2WithHmacSHA256"
            );

            return factory.generateSecret(spec).getEncoded();

        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new IllegalStateException("Password hashing failed.", e);
        } finally {
            spec.clearPassword();
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
            byte[] actualHash = deriveKey(password, salt);

            return MessageDigest.isEqual(expectedHash, actualHash);

        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    public boolean register(String username, String password, String role) {
        if (username == null || username.isBlank()
                || password == null || password.isEmpty()) {
            System.out.println("Username and password are required.");
            return false;
        }

        if (role == null
                || (!role.equals("TEACHER") && !role.equals("STUDENT"))) {
            System.out.println("Invalid role.");
            return false;
        }

        String sql = """
                INSERT INTO users (username, password, roles, status)
                VALUES (?, ?, ?, 'PENDING')
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setString(1, username.trim());
            ps.setString(2, hashPassword(password));
            ps.setString(3, role);

            ps.executeUpdate();

            System.out.println("Registration successful!");
            System.out.println("Account status: PENDING");
            return true;

        } catch (SQLException e) {
            if (e.getMessage() != null
                    && e.getMessage().contains("UNIQUE constraint failed")) {
                System.out.println("Username already exists.");
            } else {
                System.out.println("Registration failed: " + e.getMessage());
            }

            return false;
        }
    }

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

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setString(1, username.trim());

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String storedPassword = rs.getString("password");

                    if (verifyPassword(password, storedPassword)) {
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
            System.out.println("Login failed: " + e.getMessage());
        }

        return null;
    }
}
