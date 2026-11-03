
package com.scc.ui;

import com.scc.dao.EventDao;
import com.scc.dao.RegistrationDao;
import com.scc.dao.ActivityLogDao;
import com.scc.model.Event;
import com.scc.model.Registration;
import com.scc.model.User;

import java.util.List;
import java.util.Scanner;

public class ParticipantsMenu {

    private final User user;
    private final Scanner scanner;
    private final EventDao eventDao;
    private final RegistrationDao registrationDao;
    private final ActivityLogDao activityLogDao;

    public ParticipantsMenu(User user, Scanner scanner) {
        this.user = user;
        this.scanner = scanner;
        this.eventDao = new EventDao();
        this.registrationDao = new RegistrationDao();
        this.activityLogDao = new ActivityLogDao();
    }

    public void showMenu() {
        boolean running = true;

        while (running) {
            System.out.println("\n========== PARTICIPANT MENU ==========");
            System.out.println("1. View All Events");
            System.out.println("2. Search Events by Category");
            System.out.println("3. Search Events by Date");
            System.out.println("4. Register for an Event");
            System.out.println("5. View Registration History");
            System.out.println("6. Cancel Registration");
            System.out.println("7. View Account");
            System.out.println("0. Logout");
            System.out.print("Choose: ");

            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1":
                    viewEvents();
                    break;
                case "2":
                    searchByCategory();
                    break;
                case "3":
                    searchByDate();
                    break;
                case "4":
                    registerForEvent();
                    break;
                case "5":
                    viewHistory();
                    break;
                case "6":
                    cancelRegistration();
                    break;
                case "7":
                    displayAccount();
                    break;
                case "0":
                    running = false;
                    System.out.println("Participant logged out.");
                    break;
                default:
                    System.out.println("Invalid choice.");
            }
        }
    }

    private void viewEvents() {
        List<Event> events = eventDao.getAllEvents();
        displayEvents(events);
    }

    private void searchByCategory() {
        System.out.print("Enter event category: ");
        String category = scanner.nextLine().trim();

        if (category.isBlank()) {
            System.out.println("Category is required.");
            return;
        }

        List<Event> events = eventDao.searchByCategory(category);
        displayEvents(events);
    }

    private void searchByDate() {
        System.out.print("Enter event date (YYYY-MM-DD): ");
        String date = scanner.nextLine().trim();

        if (date.isBlank()) {
            System.out.println("Date is required.");
            return;
        }

        List<Event> events = eventDao.searchByDate(date);
        displayEvents(events);
    }

    private void registerForEvent() {
        viewEvents();

        int eventId = readInt(
                "Enter Event ID to register (0 to cancel): "
        );

        if (eventId == 0) {
            return;
        }

        int availableSlots = eventDao.getAvailableSlots(eventId);

        if (availableSlots < 0) {
            System.out.println("Event not found or unavailable.");
            return;
        }

        if (availableSlots == 0) {
            System.out.println("Sorry, this event is already full.");
            return;
        }

        if (registrationDao.registerParticipant(user.id(), eventId)) {
            System.out.println("Successfully registered for the event.");

            activityLogDao.logActivity(
                    user.id(),
                    "REGISTER EVENT",
                    "Registered for event ID: " + eventId
            );
        } else {
            System.out.println(
                    "Registration failed. You may already be registered "
                            + "or the event may no longer have available slots."
            );
        }
    }

    private void viewHistory() {
        List<Registration> history =
                registrationDao.getRegistrationHistory(user.id());

        if (history == null || history.isEmpty()) {
            System.out.println("You have no registration history.");
            return;
        }

        System.out.println("\n===== MY REGISTRATION HISTORY =====");

        for (Registration registration : history) {
            System.out.println("------------------------------");
            System.out.println("Registration ID: " + registration.getId());
            System.out.println("Event ID: " + registration.getEventId());
            System.out.println("Date: " + registration.getRegistrationDate());
            System.out.println("Status: " + registration.getStatus());
        }
    }

    private void cancelRegistration() {
        viewHistory();

        int registrationId = readInt(
                "Enter Registration ID to cancel (0 to go back): "
        );

        if (registrationId == 0) {
            return;
        }

        if (registrationDao.cancelRegistration(
                registrationId,
                user.id())) {

            System.out.println("Registration cancelled successfully.");

            activityLogDao.logActivity(
                    user.id(),
                    "CANCEL REGISTRATION",
                    "Cancelled registration ID: " + registrationId
            );
        } else {
            System.out.println(
                    "Cancellation failed. Check the registration ID "
                            + "or its current status."
            );
        }
    }

    private void displayEvents(List<Event> events) {
        if (events == null || events.isEmpty()) {
            System.out.println("No events found.");
            return;
        }

        System.out.println("\n========== AVAILABLE EVENTS ==========");

        for (Event event : events) {
            System.out.println("------------------------------");
            System.out.println("Event ID: " + event.getId());
            System.out.println("Name: " + event.getEventName());
            System.out.println("Description: " + event.getDescription());
            System.out.println("Category: " + event.getCategory());
            System.out.println("Date: " + event.getEventDate());
            System.out.println("Location: " + event.getLocation());
            System.out.println("Capacity: " + event.getCapacity());
            System.out.println("Available Slots: "
                    + eventDao.getAvailableSlots(event.getId()));
        }
    }

    private void displayAccount() {
        System.out.println("\n===== ACCOUNT DETAILS =====");
        System.out.println("User ID: " + user.id());
        System.out.println("Username: " + user.username());
        System.out.println("Role: " + user.roles());
        System.out.println("Status: " + user.status());
    }

    private int readInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();

            try {
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number.");
            }
        }
    }
}

