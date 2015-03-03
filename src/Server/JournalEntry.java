package src.Server;

import java.io.Serializable;

public class JournalEntry implements Serializable{
    private User creater;
    private StringBuilder notes;


    /*
    Måste hålla koll på, skapare, stringbuilder med notiser, när någon skapar/ändrar läggs denna till i contributers.

     */
    public JournalEntry(User user, String message) {
        this.creater = user;
        this.notes = new StringBuilder();
        notes.append(message);
    }

    public User getUser() {
        return creater;
    }

    public String toString() {
        return creater.toString() + ", Note: " + notes;
    }
}
