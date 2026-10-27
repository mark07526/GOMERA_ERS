package com.scc.ui;

import com.scc.dao.UserDao;
import com.scc.model.User;

import java.util.List;
import java.util.Locale;
import java.util.Scanner;

public class AdminMenu {

    private final UserDao userDao;
    private final Scanner scanner;
    private final User currentAdmin;

    public AdminMenu(UserDao userDao, Scanner scanner) {
        this(userDao, scanner, null);
    }

    public AdminMenu(
            UserDao userDao,
            Scanner scanner,
            User currentAdmin) {

        this.userDao = userDao;
        this.scanner = scanner;
        this.currentAdmin = currentAdmin;
    }

    public void showMenu() {
        boolean running = true;

        while (running) {
            System.out.println("\n========== ADMIN MENU ==========");
            System.out.println("1. View All System Users");
            System.out.println("2. View Pending Registrations");
            System.out.println("3. Approve User Account");
            System.out.println("4. Update User Role and Status");
            System.out.println("0. Logout");
            System.out.print("Choose: ");

            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1":
                    displayUsers(userDao.getAllUsers());
                    break;

                case "2":
                    displayUsers(userDao.getPendingUsers());
                    break;

                case "3":
                    approveAccount();
                    break;

                case "4":
                    updateUser();
                    break;

                case "0":
                    running = false;
                    System.out.println("Admin logged out.");
                    break;

                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    private void displayUsers(List<User> users) {
        if (users == null || users.isEmpty()) {
            System.out.println("No users found.");
            return;
        }

        // Formatted Console Output with exact borders and column constraints
        String border = "+-------+-----------------+--------------+------------+";
        System.out.println("\n================ SYSTEM USERS ================");
        System.out.println(border);
        System.out.printf("| %-5s | %-15s | %-12s | %-10s |\n", "ID", "Username", "Role", "Status");
        System.out.println(border);

        for (User user : users) {
            System.out.printf("| %-5d | %-15s | %-12s | %-10s |\n",
                    user.id(),
                    user.username(),
                    user.roles(),
                    user.status()
            );
        }
        System.out.println(border);
    }

    private void approveAccount() {
        List<User> pending = userDao.getPendingUsers();

        if (pending == null || pending.isEmpty()) {
            System.out.println("No pending registrations.");
            return;
        }

        displayUsers(pending);

        int userId = readIntInput(
                "Enter User ID to approve (0 to cancel): "
        );

        if (userId == 0) {
            System.out.println("Approval cancelled.");
            return;
        }

        if (userId < 0) {
            System.out.println("User ID must be positive.");
            return;
        }

        boolean isPending = pending.stream()
                .anyMatch(user -> user.id() == userId);

        if (!isPending) {
            System.out.println("User ID not found in pending registrations.");
            return;
        }

        if (userDao.approveUser(userId)) {
            System.out.println("Account approved successfully.");
        } else {
            System.out.println("Approval failed. Please try again.");
        }
    }

    private void updateUser() {
        List<User> users = userDao.getAllUsers();

        if (users == null || users.isEmpty()) {
            System.out.println("No users available to update.");
            return;
        }

        displayUsers(users);

        int targetUserId = readIntInput(
                "Enter User ID to update (0 to cancel): "
        );

        if (targetUserId == 0) {
            System.out.println("Update cancelled.");
            return;
        }

        if (targetUserId < 0) {
            System.out.println("User ID must be positive.");
            return;
        }

        User target = users.stream()
                .filter(user -> user.id() == targetUserId)
                .findFirst()
                .orElse(null);

        if (target == null) {
            System.out.println("User ID not found.");
            return;
        }

        if (currentAdmin == null) {
            System.out.println(
                    "Update blocked: logged-in admin identity is unavailable."
            );
            return;
        }

        if (targetUserId == currentAdmin.id()) {
            System.out.println(
                    "You cannot change your own role or status."
            );
            return;
        }

        String newRole = readValidRole();

        if (newRole == null) {
            System.out.println("Update cancelled.");
            return;
        }

        String newStatus = readValidStatus();

        if (newStatus == null) {
            System.out.println("Update cancelled.");
            return;
        }

        System.out.println("\n===== UPDATE SUMMARY =====");
        System.out.println("Username: " + target.username());
        System.out.println("New Role: " + newRole);
        System.out.println("New Status: " + newStatus);

        System.out.print("Confirm update? (Y/N): ");
        String confirmation = scanner.nextLine()
                .trim()
                .toUpperCase(Locale.ROOT);

        if (!confirmation.equals("Y")) {
            System.out.println("Update cancelled.");
            return;
        }

        boolean success = userDao.updateUserRoleAndStatus(
                targetUserId,
                newRole,
                newStatus
        );

        if (success) {
            System.out.println("User updated successfully.");
        } else {
            System.out.println("Update failed. Please check the user ID.");
        }
    }

    private int readIntInput(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();

            try {
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a number.");
            }
        }
    }

    private String readValidRole() {
        while (true) {
            System.out.println("\nAvailable Roles:");
            System.out.println("1. PARTICIPANT");
            System.out.println("2. ORGANIZER");
            System.out.println("3. STUDENT");
            System.out.println("4. TEACHER");
            System.out.println("5. ADMIN");
            System.out.print("Choose role (0 to cancel): ");

            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1":
                    return "PARTICIPANT";
                case "2":
                    return "ORGANIZER";
                case "3":
                    return "STUDENT";
                case "4":
                    return "TEACHER";
                case "5":
                    return "ADMIN";
                case "0":
                    return null;
                default:
                    System.out.println("Invalid choice.");
            }
        }
    }

    private String readValidStatus() {
        while (true) {
            System.out.println("\nAvailable Statuses:");
            System.out.println("1. APPROVED");
            System.out.println("2. PENDING");
            System.out.print("Choose status (0 to cancel): ");

            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1":
                    return "APPROVED";
                case "2":
                    return "PENDING";
                case "0":
                    return null;
                default:
                    System.out.println("Invalid choice.");
            }
        }
    }
}