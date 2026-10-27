package com.scc.model;

import java.util.Locale;

public record User(
        int id,
        String username,
        String password,
        String roles,
        String status
) {

    public User {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Username cannot be empty.");
        }

        if (roles == null || roles.isBlank()) {
            throw new IllegalArgumentException("Role cannot be empty.");
        }

        if (status == null || status.isBlank()) {
            throw new IllegalArgumentException("Status cannot be empty.");
        }

        roles = roles.trim().toUpperCase(Locale.ROOT);
        status = status.trim().toUpperCase(Locale.ROOT);
    }

    public boolean isAdmin() {
        return "ADMIN".equals(roles);
    }

    public boolean isOrganizer() {
        return "ORGANIZER".equals(roles);
    }

    public boolean isParticipant() {
        return "PARTICIPANT".equals(roles);
    }

    public boolean isStudent() {
        return "STUDENT".equals(roles);
    }

    public boolean isTeacher() {
        return "TEACHER".equals(roles);
    }

    public boolean isApproved() {
        return "APPROVED".equals(status);
    }

    public boolean isPending() {
        return "PENDING".equals(status);
    }
}