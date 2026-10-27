
package com.scc.ui;

import com.scc.dao.UserDao;
import com.scc.model.User;

import java.util.Locale;
import java.util.Scanner;

public class Authentication {

    private final UserDao userDao;
    private final Scanner scanner;

    public Authentication(UserDao userDao, Scanner scanner) {
        this.userDao = userDao;
        this.scanner = scanner;
    }

    // =========================
    // START SYSTEM
    // =========================

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
                    System.out.println(
                            "Thank you for using the system!"
                    );
                    break;

                default:
                    System.out.println(
                            "Invalid choice. Please try again."
                    );
            }
        }
    }

    // =========================
    // REGISTER ACCOUNT
    // =========================

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

        if (password.isEmpty()) {
            System.out.println("Password is required.");
            return;
        }

        String role = readRegistrationRole();

        if (role == null) {
            System.out.println("Registration cancelled.");
            return;
        }

        userDao.register(username, password, role);
    }

    private String readRegistrationRole() {
        while (true) {
            System.out.println("\nSelect your role:");
            System.out.println("1. ORGANIZER");
            System.out.println("2. PARTICIPANTS");
            System.out.println("0. Cancel");
            System.out.print("Choose role: ");

            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1":
                    return "ORGANIZER";

                case "2":
                    return "PARTICIPANTS";

                case "0":
                    return null;

                default:
                    System.out.println(
                            "Invalid role choice. Enter 1, 2, or 0."
                    );
            }
        }
    }

    // =========================
    // LOGIN ACCOUNT
    // =========================

    private void loginAccount() {
        System.out.println("\n===== LOGIN =====");

        System.out.print("Enter username: ");
        String username = scanner.nextLine().trim();

        System.out.print("Enter password: ");
        String password = scanner.nextLine();

        User user = userDao.login(username, password);

        if (user == null) {
            System.out.println(
                    "Invalid username or password."
            );
            return;
        }

        // Match the status values used by UserDao.
        if ("PENDING".equalsIgnoreCase(user.status())) {
            System.out.println(
                    "Your account is still pending approval."
            );
            System.out.println(
                    "Please contact the administrator."
            );
            return;
        }

        if (!"APPROVED".equalsIgnoreCase(user.status())) {
            System.out.println(
                    "Your account is not approved."
            );
            return;
        }

        System.out.println("\nLogin successful!");
        System.out.println("Welcome, " + user.username() + "!");

        routeByRole(user);
    }

    // =========================
    // ROUTE BY USER ROLE
    // =========================

    private void routeByRole(User user) {
        if (user.roles() == null) {
            System.out.println("Unknown user role.");
            return;
        }

        switch (user.roles().toUpperCase(Locale.ROOT)) {
            case "ADMIN":
                adminMenu(user);
                break;

            case "ORGANIZER":
                OrganizerMenu(user);
                break;

            case "PARTICIPANTS":
                ParticipantsMenu(user);
                break;

            default:
                System.out.println("Unknown user role.");
        }
    }

    // =========================
    // ADMIN MENU
    // =========================

    private void adminMenu(User user) {
        AdminMenu menu = new AdminMenu(
                userDao,
                scanner,
                user
        );

        menu.showMenu();

        System.out.println("Admin logged out.");
    }

    // =========================
    // TEACHER MENU
    // =========================

    private void OrganizerMenu(User user) {
        boolean loggedIn = true;

        while (loggedIn) {
            System.out.println("\n===== ORGANIZER MENU =====");
            System.out.println("1. View Account");
            System.out.println("2. Logout");
            System.out.print("Choose an option: ");

            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1":
                    displayAccount(user);
                    break;

                case "2":
                    loggedIn = false;
                    System.out.println("Organizer logged out.");
                    break;

                default:
                    System.out.println("Invalid choice.");
            }
        }
    }

    // =========================
    // STUDENT MENU
    // =========================

    private void ParticipantsMenu(User user) {
        boolean loggedIn = true;

        while (loggedIn) {
            System.out.println("\n===== PARTICIPANT MENU =====");
            System.out.println("1. View Account");
            System.out.println("2. Logout");
            System.out.print("Choose an option: ");

            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1":
                    displayAccount(user);
                    break;

                case "2":
                    loggedIn = false;
                    System.out.println("Participants logged out.");
                    break;

                default:
                    System.out.println("Invalid choice.");
            }
        }
    }

    // =========================
    // DISPLAY ACCOUNT
    // =========================

    private void displayAccount(User user) {
        System.out.println("\n===== ACCOUNT DETAILS =====");
        System.out.println("User ID: " + user.id());
        System.out.println("Username: " + user.username());
        System.out.println("Role: " + user.roles());
        System.out.println("Status: " + user.status());
    }
}
