
package com.scc.dao;

import com.scc.model.Event;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class EventDao {

    // =================================
    // CREATE EVENT
    // =================================

    public boolean addEvent(Event event) {

        String sql = """
            INSERT INTO events (
                event_name,
                description,
                category,
                event_date,
                location,
                capacity,
                created_by,
                is_active
            )
            VALUES (?, ?, ?, ?, ?, ?, ?, 1)
            """;

        if (event == null
                || event.getEventName() == null
                || event.getEventName().isBlank()
                || event.getCategory() == null
                || event.getCategory().isBlank()
                || event.getEventDate() == null
                || event.getEventDate().isBlank()
                || event.getLocation() == null
                || event.getLocation().isBlank()
                || event.getCapacity() <= 0) {

            System.out.println("Invalid event information.");
            return false;
        }

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, event.getEventName());
            statement.setString(2, event.getDescription());
            statement.setString(3, event.getCategory());
            statement.setString(4, event.getEventDate());
            statement.setString(5, event.getLocation());
            statement.setInt(6, event.getCapacity());
            statement.setInt(7, event.getCreatedBy());

            statement.executeUpdate();

            System.out.println("Event added successfully!");
            return true;

        } catch (SQLException e) {
            System.out.println("Error adding event: " + e.getMessage());
            return false;
        }
    }

    // =================================
    // VIEW ALL ACTIVE EVENTS
    // =================================

    public List<Event> getAllEvents() {

        List<Event> events = new ArrayList<>();

        String sql = """
            SELECT *
            FROM events
            WHERE is_active = 1
            ORDER BY event_date
            """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {

            while (result.next()) {
                events.add(mapEvent(result));
            }

        } catch (SQLException e) {
            System.out.println("Error retrieving events: " + e.getMessage());
        }

        return events;
    }

    // =================================
    // SEARCH EVENTS BY CATEGORY
    // =================================

    public List<Event> searchByCategory(String category) {

        List<Event> events = new ArrayList<>();

        String sql = """
            SELECT *
            FROM events
            WHERE category LIKE ?
              AND is_active = 1
            ORDER BY event_date
            """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, "%" + category + "%");

            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    events.add(mapEvent(result));
                }
            }

        } catch (SQLException e) {
            System.out.println("Category search failed: " + e.getMessage());
        }

        return events;
    }

    // =================================
    // SEARCH EVENTS BY DATE
    // =================================

    public List<Event> searchByDate(String eventDate) {

        List<Event> events = new ArrayList<>();

        String sql = """
            SELECT *
            FROM events
            WHERE event_date = ?
              AND is_active = 1
            ORDER BY event_name
            """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, eventDate);

            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    events.add(mapEvent(result));
                }
            }

        } catch (SQLException e) {
            System.out.println("Date search failed: " + e.getMessage());
        }

        return events;
    }

    // =================================
    // UPDATE EVENT DETAILS
    // =================================

    public boolean updateEvent(Event event) {

        if (event == null || event.getCapacity() <= 0) {
            System.out.println("Invalid event information.");
            return false;
        }

        String sql = """
            UPDATE events
            SET event_name = ?,
                description = ?,
                category = ?,
                event_date = ?,
                location = ?,
                capacity = ?
            WHERE id = ?
              AND is_active = 1
            """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, event.getEventName());
            statement.setString(2, event.getDescription());
            statement.setString(3, event.getCategory());
            statement.setString(4, event.getEventDate());
            statement.setString(5, event.getLocation());
            statement.setInt(6, event.getCapacity());
            statement.setInt(7, event.getId());

            int rows = statement.executeUpdate();

            if (rows > 0) {
                System.out.println("Event updated successfully!");
                return true;
            }

            System.out.println("Event not found.");
        } catch (SQLException e) {
            System.out.println("Update failed: " + e.getMessage());
        }

        return false;
    }

    // =================================
    // DELETE EVENT (SOFT DELETE)
    // =================================

    public boolean deleteEvent(int eventId) {

        String sql = """
            UPDATE events
            SET is_active = 0
            WHERE id = ?
              AND is_active = 1
            """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, eventId);

            int rows = statement.executeUpdate();

            if (rows > 0) {
                System.out.println("Event deleted successfully!");
                return true;
            }

            System.out.println("Event not found or already deleted.");

        } catch (SQLException e) {
            System.out.println("Delete failed: " + e.getMessage());
        }

        return false;
    }

    // =================================
    // GET EVENT BY ID
    // =================================

    public Event getEventById(int eventId) {

        String sql = """
            SELECT *
            FROM events
            WHERE id = ?
              AND is_active = 1
            """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, eventId);

            try (ResultSet result = statement.executeQuery()) {

                if (result.next()) {
                    return mapEvent(result);
                }
            }

        } catch (SQLException e) {
            System.out.println("Error finding event: " + e.getMessage());
        }

        return null;
    }

    // =================================
    // CHECK AVAILABLE SLOTS
    // =================================

    public int getAvailableSlots(int eventId) {

        String sql = """
            SELECT
                e.capacity -
                (
                    SELECT COUNT(*)
                    FROM registrations r
                    WHERE r.event_id = e.id
                      AND r.status IN ('REGISTERED', 'ATTENDED')
                ) AS available_slots
            FROM events e
            WHERE e.id = ?
              AND e.is_active = 1
            """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, eventId);

            try (ResultSet result = statement.executeQuery()) {

                if (result.next()) {
                    return Math.max(0, result.getInt("available_slots"));
                }
            }

        } catch (SQLException e) {
            System.out.println("Error checking slots: " + e.getMessage());
        }

        return -1;
    }

    // =================================
    // MAP DATABASE DATA TO EVENT OBJECT
    // =================================

    private Event mapEvent(ResultSet result) throws SQLException {

        return new Event(
                result.getInt("id"),
                result.getString("event_name"),
                result.getString("description"),
                result.getString("category"),
                result.getString("event_date"),
                result.getString("location"),
                result.getInt("capacity"),
                result.getInt("created_by"),
                result.getInt("is_active") == 1
        );
    }
}
