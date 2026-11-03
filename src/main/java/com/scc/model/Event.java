
package com.scc.model;

public class Event {

    private int id;
    private String eventName;
    private String description;
    private String category;
    private String eventDate;
    private String location;
    private int capacity;
    private int createdBy;
    private boolean active;

    // Constructor for creating a new event
    public Event(
            String eventName,
            String description,
            String category,
            String eventDate,
            String location,
            int capacity,
            int createdBy) {

        this.eventName = eventName;
        this.description = description;
        this.category = category;
        this.eventDate = eventDate;
        this.location = location;
        this.capacity = capacity;
        this.createdBy = createdBy;
        this.active = true;
    }

    // Constructor for existing events from database
    public Event(
            int id,
            String eventName,
            String description,
            String category,
            String eventDate,
            String location,
            int capacity,
            int createdBy,
            boolean active) {

        this.id = id;
        this.eventName = eventName;
        this.description = description;
        this.category = category;
        this.eventDate = eventDate;
        this.location = location;
        this.capacity = capacity;
        this.createdBy = createdBy;
        this.active = active;
    }

    // Getters
    public int getId() {
        return id;
    }

    public String getEventName() {
        return eventName;
    }

    public String getDescription() {
        return description;
    }

    public String getCategory() {
        return category;
    }

    public String getEventDate() {
        return eventDate;
    }

    public String getLocation() {
        return location;
    }

    public int getCapacity() {
        return capacity;
    }

    public int getCreatedBy() {
        return createdBy;
    }

    public boolean isActive() {
        return active;
    }

    // Setters
    public void setId(int id) {
        this.id = id;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setEventDate(String eventDate) {
        this.eventDate = eventDate;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public void setCreatedBy(int createdBy) {
        this.createdBy = createdBy;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    // Display event information
    @Override
    public String toString() {
        return "Event ID: " + id
                + "\nEvent Name: " + eventName
                + "\nDescription: " + description
                + "\nCategory: " + category
                + "\nDate: " + eventDate
                + "\nLocation: " + location
                + "\nCapacity: " + capacity
                + "\nCreated By: " + createdBy
                + "\nActive: " + active;
    }
}
