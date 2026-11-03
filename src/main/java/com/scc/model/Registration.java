
package com.scc.model;

public class Registration {

    private int id;
    private int userId;
    private int eventId;
    private String registrationDate;
    private String status;

    // Constructor for new registration
    public Registration(int userId, int eventId) {
        this.userId = userId;
        this.eventId = eventId;
        this.status = "REGISTERED";
    }

    // Constructor for existing registration from database
    public Registration(
            int id,
            int userId,
            int eventId,
            String registrationDate,
            String status) {

        this.id = id;
        this.userId = userId;
        this.eventId = eventId;
        this.registrationDate = registrationDate;
        this.status = status;
    }

    // Getters
    public int getId() {
        return id;
    }

    public int getUserId() {
        return userId;
    }

    public int getEventId() {
        return eventId;
    }

    public String getRegistrationDate() {
        return registrationDate;
    }

    public String getStatus() {
        return status;
    }

    // Setters
    public void setId(int id) {
        this.id = id;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public void setEventId(int eventId) {
        this.eventId = eventId;
    }

    public void setRegistrationDate(String registrationDate) {
        this.registrationDate = registrationDate;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "Registration ID: " + id
                + "\nUser ID: " + userId
                + "\nEvent ID: " + eventId
                + "\nRegistration Date: " + registrationDate
                + "\nStatus: " + status;
    }
}

