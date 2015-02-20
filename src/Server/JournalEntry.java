package src.Server;

public class JournalEntry {
    private User creater;
    private StringBuilder notes;


    /*
    Måste hålla koll på, skapare, stringbuilder med notiser, när någon skapar/ändrar läggs denna till i contributers.

     */
    public JournalEntry(User user) {
        this.creater = user;
    }

    public String getUserDivision() {
        return creater.getDivision();
    }

    public String getUserID() {
        return creater.getID();
    }

    public User getUser() {
        return creater;
    }

    public String toString() {
        return creater.toString() + ", Note: " + notes;
    }
}
