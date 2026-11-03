
package com.scc.ui;

import com.scc.dao.EventDao;
import com.scc.dao.RegistrationDao;
import com.scc.dao.ActivityLogDao;
import com.scc.model.Event;
import com.scc.model.Registration;
import com.scc.model.User;

import java.util.List;
import java.util.Scanner;

public class OrganizerMenu {

    private final User user;
    private final Scanner scanner;
    private final EventDao eventDao;
    private final RegistrationDao registrationDao;
    private final ActivityLogDao activityLogDao;

    public OrganizerMenu(User user, Scanner scanner) {
        this.user = user;
        this.scanner = scanner;
        this.eventDao = new EventDao();
        this.registrationDao = new RegistrationDao();
        this.activityLogDao = new ActivityLogDao();
    }

    public void showMenu() {
        boolean running = true;

        while (running) {
            System.out.println("\n========== ORGANIZER MENU ==========");
            System.out.println("1. View All Events");
            System.out.println("2. Create Event");
            System.out.println("3. Update Event");
            System.out.println("4. Delete Event");
            System.out.println("5. View Event Registrations");
            System.out.println("6. Mark Attendance");
            System.out.println("0. Logout");
            System.out.print("Choose: ");

            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1":
                    viewEvents();
                    break;
                case "2":
                    createEvent();
                    break;
                case "3":
                    updateEvent();
                    break;
                case "4":
                    deleteEvent();
                    break;
                case "5":
                    viewRegistrations();
                    break;
                case "6":
                    markAttendance();
                    break;
                case "0":
                    running = false;
                    System.out.println("Organizer logged out.");
                    break;
                default:
                    System.out.println("Invalid choice.");
            }
        }
    }

    private void viewEvents() {
        List<Event> events = eventDao.getAllEvents();

        if (events == null || events.isEmpty()) {
            System.out.println("No active events found.");
            return;
        }

        for (Event event : events) {
            displayEvent(event);
        }
    }

    private void createEvent() {
        System.out.println("\n===== CREATE EVENT =====");

        System.out.print("Event name: ");
        String name = scanner.nextLine().trim();

        System.out.print("Description: ");
        String description = scanner.nextLine().trim();

        System.out.print("Category: ");
        String category = scanner.nextLine().trim();

        System.out.print("Event date (YYYY-MM-DD): ");
        String date = scanner.nextLine().trim();

        System.out.print("Location: ");
        String location = scanner.nextLine().trim();

        int capacity = readInt("Capacity: ");

        if (name.isBlank() || category.isBlank()
                || date.isBlank() || location.isBlank()
                || capacity <= 0) {
            System.out.println("Invalid event details.");
            return;
        }

        Event event = new Event(
                name,
                description,
                category,
                date,
                location,
                capacity,
                user.id()
        );

        if (eventDao.addEvent(event)) {
            System.out.println("Event created successfully.");

            activityLogDao.logActivity(
                    user.id(),
                    "CREATE EVENT",
                    "Created event: " + name
            );
        } else {
            System.out.println("Failed to create event.");
        }
    }

    private void updateEvent() {
        int eventId = readInt("Enter Event ID to update (0 to cancel): ");

        if (eventId == 0) {
            return;
        }

        Event event = eventDao.getEventById(eventId);

        if (event == null) {
            System.out.println("Event not found.");
            return;
        }

        System.out.println("Leave a field blank to keep its current value.");

        System.out.print("New event name [" + event.getEventName() + "]: ");
        String name = scanner.nextLine().trim();

        System.out.print("New description [" + event.getDescription() + "]: ");
        String description = scanner.nextLine().trim();

        System.out.print("New category [" + event.getCategory() + "]: ");
        String category = scanner.nextLine().trim();

        System.out.print("New date [" + event.getEventDate() + "]: ");
        String date = scanner.nextLine().trim();

        System.out.print("New location [" + event.getLocation() + "]: ");
        String location = scanner.nextLine().trim();

        System.out.print("New capacity [" + event.getCapacity() + "]: ");
        String capacityInput = scanner.nextLine().trim();

        if (!name.isBlank()) event.setEventName(name);
        if (!description.isBlank()) event.setDescription(description);
        if (!category.isBlank()) event.setCategory(category);
        if (!date.isBlank()) event.setEventDate(date);
        if (!location.isBlank()) event.setLocation(location);

        if (!capacityInput.isBlank()) {
            try {
                int capacity = Integer.parseInt(capacityInput);

                if (capacity <= 0) {
                    System.out.println("Capacity must be positive.");
                    return;
                }

                event.setCapacity(capacity);
            } catch (NumberFormatException e) {
                System.out.println("Invalid capacity.");
                return;
            }
        }

        if (eventDao.updateEvent(event)) {
            System.out.println("Event updated successfully.");

            activityLogDao.logActivity(
                    user.id(),
                    "UPDATE EVENT",
                    "Updated event ID: " + eventId
            );
        } else {
            System.out.println("Failed to update event.");
        }
    }

    private void deleteEvent() {
        int eventId = readInt("Enter Event ID to delete (0 to cancel): ");

        if (eventId == 0) {
            return;
        }

        Event event = eventDao.getEventById(eventId);

        if (event == null) {
            System.out.println("Event not found.");
            return;
        }

        System.out.print("Confirm deletion? (Y/N): ");
        String confirmation = scanner.nextLine().trim();

        if (!confirmation.equalsIgnoreCase("Y")) {
            System.out.println("Deletion cancelled.");
            return;
        }

        if (eventDao.deleteEvent(eventId)) {
            System.out.println("Event deleted successfully.");

            activityLogDao.logActivity(
                    user.id(),
                    "DELETE EVENT",
                    "Deactivated event ID: " + eventId
            );
        } else {
            System.out.println("Failed to delete event.");
        }
    }

    private void viewRegistrations() {
        int eventId = readInt("Enter Event ID: ");

        List<Registration> registrations =
                registrationDao.getRegistrationsForEvent(eventId);

        if (registrations == null || registrations.isEmpty()) {
            System.out.println("No registrations found.");
            return;
        }

        System.out.println("\n===== EVENT REGISTRATIONS =====");

        for (Registration registration : registrations) {
            System.out.println("------------------------------");
            System.out.println("Registration ID: " + registration.getId());
            System.out.println("Participant ID: " + registration.getUserId());
            System.out.println("Status: " + registration.getStatus());
            System.out.println("Registration Date: "
                    + registration.getRegistrationDate());
        }
    }

    private void markAttendance() {
        int registrationId = readInt(
                "Enter Registration ID to mark attended: "
        );

        if (registrationId <= 0) {
            System.out.println("Invalid registration ID.");
            return;
        }

        if (registrationDao.markAttendance(registrationId)) {
            System.out.println("Attendance recorded successfully.");

            activityLogDao.logActivity(
                    user.id(),
                    "MARK ATTENDANCE",
                    "Marked registration ID " + registrationId
                            + " as attended."
            );
        } else {
            System.out.println("Failed to record attendance.");
        }
    }

    private void displayEvent(Event event) {
        System.out.println("\n------------------------------");
        System.out.println("Event ID: " + event.getId());
        System.out.println("Name: " + event.getEventName());
        System.out.println("Description: " + event.getDescription());
        System.out.println("Category: " + event.getCategory());
        System.out.println("Date: " + event.getEventDate());
        System.out.println("Location: " + event.getLocation());
        System.out.println("Capacity: " + event.getCapacity());
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
