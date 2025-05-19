/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Calendario;

public class Event {

    private int ID;
    private String title;
    private String description;
    private String date;    // formato "yyyy-MM-dd"
    private String time;    // formato "HH:mm:ss"
    private int userId;

    // Constructor completo con ID y userId
    public Event(int ID, String title, String description, String date, String time, int userId) {
        this.ID = ID;
        this.title = title;
        this.description = description;
        this.date = date;
        this.time = time;
        this.userId = userId;
    }

    // Constructor para nuevos eventos sin ID (autogenerado)
    public Event(String title, String description, String date, String time, int userId) {
        this.title = title;
        this.description = description;
        this.date = date;
        this.time = time;
        this.userId = userId;
    }

    public Event() {}

    public int getID() {
        return ID;
    }

    public void setID(int ID) {
        this.ID = ID;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }
}
