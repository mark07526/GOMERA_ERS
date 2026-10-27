
package com.scc.ui;

import com.scc.model.User;
import java.util.Scanner;

public class OrganizerMenu {

    private final User user;
    private final Scanner scanner;

    public OrganizerMenu(User user, Scanner scanner) {
        this.user = user;
        this.scanner = scanner;
    }

    public void start() {
        boolean running = true;

        while (running) {
            System.out.println("\n===== ORGANIZER / STAFF MENU =====");
            System.out.println("1. View Account");
            System.out.println("2. Create Event");
            System.out.println("3. View Events");
            System.out.println("0. Logout");
            System.out.print("Choose an option: ");

            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1":
                    viewAccount();
                    break;

                case "2":
                    createEvent();
                    break;

                case "3":
                    viewEvents();
                    break;

                case "0":
                    running = false;
                    System.out.println("Organizer / Staff logged out.");
                    break;

                default:
                    System.out.println("Invalid choice. Try again.");
            }
        }
    }

    private void viewAccount() {
        System.out.println("\n===== ORGANIZER / STAFF ACCOUNT =====");
        System.out.println("User ID: " + user.id());
        System.out.println("Username: " + user.username());
        System.out.println("Role: " + user.roles());
        System.out.println("Status: " + user.status());
    }

    private void createEvent() {
        System.out.println("\n===== CREATE EVENT =====");
        System.out.println(
                "Event creation will be available after the event "
                        + "database and DAO are implemented."
        );
    }

    private void viewEvents() {
        System.out.println("\n===== AVAILABLE EVENTS =====");
        System.out.println(
                "Event listing will be available after the event "
                        + "database and DAO are implemented."
        );
    }
}
