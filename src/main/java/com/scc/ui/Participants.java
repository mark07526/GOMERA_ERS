
package com.scc.ui;

import com.scc.model.User;
import java.util.Scanner;

public class Participants {

    private final User user;
    private final Scanner scanner;

    public Participants(User user, Scanner scanner) {
        this.user = user;
        this.scanner = scanner;
    }

    public void start() {
        boolean running = true;

        while (running) {
            System.out.println("\n===== PARTICIPANT MENU =====");
            System.out.println("1. View Account");
            System.out.println("2. View Available Events");
            System.out.println("3. Register for Event");
            System.out.println("0. Logout");
            System.out.print("Choose an option: ");

            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1":
                    viewAccount();
                    break;

                case "2":
                    viewEvents();
                    break;

                case "3":
                    registerForEvent();
                    break;

                case "0":
                    running = false;
                    System.out.println("Participant logged out.");
                    break;

                default:
                    System.out.println("Invalid choice. Try again.");
            }
        }
    }

    private void viewAccount() {
        System.out.println("\n===== PARTICIPANT ACCOUNT =====");
        System.out.println("User ID: " + user.id());
        System.out.println("Username: " + user.username());
        System.out.println("Role: " + user.roles());
        System.out.println("Status: " + user.status());
    }

    private void viewEvents() {
        System.out.println("\n===== AVAILABLE EVENTS =====");
        System.out.println(
                "Event listing will be available after the event "
                        + "database and DAO are implemented."
        );
    }

    private void registerForEvent() {
        System.out.println("\n===== EVENT REGISTRATION =====");
        System.out.println(
                "Event registration will be available after the event "
                        + "database and registration DAO are implemented."
        );
    }
}
