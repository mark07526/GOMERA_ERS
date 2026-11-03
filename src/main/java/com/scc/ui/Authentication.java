
package com.scc.ui;

import com.scc.dao.UserDao;
import com.scc.dao.ActivityLogDao;
import com.scc.model.User;

import java.util.Locale;
import java.util.Scanner;

public class Authentication {

    private final UserDao userDao;
    private final ActivityLogDao activityLogDao;
    private final Scanner scanner;

    public Authentication(UserDao userDao, Scanner scanner) {
        this.userDao = userDao;
        this.scanner = scanner;
        this.activityLogDao = new ActivityLogDao();
    }

    // START SYSTEM
    public void start() {
        boolean running = true;

        while (running) {
            System.out.println("\n===== EVENT REGISTRATION SYSTEM =====");
            System.out.println("1. Register Account");
            System.out.println("2. Login");
            System.out.println("3. Exit");
            System.out.print("Choose an option: ");

            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1":
                    registerAccount();
                    break;

                case "2":
                    loginAccount();
                    break;

                case "3":
                    running = false;
                    System.out.println("Thank you for using the system!");
                    break;

                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    // REGISTER ACCOUNT
    private void registerAccount() {
        System.out.println("\n===== REGISTER ACCOUNT =====");

        System.out.print("Enter username (0 to cancel): ");
        String username = scanner.nextLine().trim();

        if (username.equals("0")) {
            System.out.println("Registration cancelled.");
            return;
        }

        if (username.isBlank()) {
            System.out.println("Username is required.");
            return;
        }

        System.out.print("Enter password (0 to cancel): ");
        String password = scanner.nextLine();

        if (password.equals("0")) {
            System.out.println("Registration cancelled.");
            return;
        }

        if (password.isBlank()) {
            System.out.println("Password is required.");
            return;
        }

        String role = readRegistrationRole();

        if (role == null) {
            System.out.println("Registration cancelled.");
            return;
        }

        try {
            userDao.register(username, password, role);
            System.out.println(
                    "Registration request submitted. Your account is PENDING approval."
            );
        } catch (RuntimeException e) {
            System.out.println("Registration failed: " + e.getMessage());
        }
    }

    // SELECT REGISTRATION ROLE
    private String readRegistrationRole() {
        while (true) {
            System.out.println("\nSelect your role:");
            System.out.println("1. ORGANIZER");
            System.out.println("2. PARTICIPANT");
            System.out.println("0. Cancel");
            System.out.print("Choose role: ");

            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1":
                    return "ORGANIZER";

                case "2":
                    return "PARTICIPANT";

                case "0":
                    return null;

                default:
                    System.out.println("Invalid role choice. Enter 1, 2, or 0.");
            }
        }
    }

    // LOGIN ACCOUNT
    private void loginAccount() {
        System.out.println("\n===== LOGIN =====");

        System.out.print("Enter username: ");
        String username = scanner.nextLine().trim();

        System.out.print("Enter password: ");
        String password = scanner.nextLine();

        if (username.isBlank() || password.isBlank()) {
            System.out.println("Username and password are required.");
            return;
        }

        User user = userDao.login(username, password);

        if (user == null) {
            System.out.println("Invalid username or password.");
            return;
        }

        if ("PENDING".equalsIgnoreCase(user.status())) {
            System.out.println("Your account is still pending approval.");
            System.out.println("Please contact the administrator.");
            return;
        }

        if (!"APPROVED".equalsIgnoreCase(user.status())) {
            System.out.println("Your account is not approved.");
            return;
        }

        if (user.roles() == null) {
            System.out.println("Unknown user role.");
            return;
        }

        System.out.println("\nLogin successful!");
        System.out.println("Welcome, " + user.username() + "!");

        activityLogDao.logActivity(
                user.id(),
                "LOGIN",
                "User logged into the system."
        );

        routeByRole(user);
    }

    // ROUTE BY USER ROLE
    private void routeByRole(User user) {
        String role = user.roles().toUpperCase(Locale.ROOT);

        switch (role) {
            case "ADMIN":
                adminMenu(user);
                break;

            case "ORGANIZER":
                organizerMenu(user);
                break;

            case "PARTICIPANT":
                participantMenu(user);
                break;

            default:
                System.out.println("Unknown user role: " + role);
        }
    }

    // ADMIN MENU
    private void adminMenu(User user) {
        AdminMenu menu = new AdminMenu(
                userDao,
                scanner,
                user
        );

        menu.showMenu();
    }

    // ORGANIZER MENU
    private void organizerMenu(User user) {
        OrganizerMenu menu = new OrganizerMenu(
                user,
                scanner
        );

        menu.showMenu();
    }

    // PARTICIPANT MENU
    private void participantMenu(User user) {
        ParticipantsMenu menu = new ParticipantsMenu(
                user,
                scanner
        );

        menu.showMenu();
    }
}
