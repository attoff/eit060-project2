package src.Server;

import java.util.ArrayList;

public class JournalEntry {
    private ArrayList<User> contributers = new ArrayList<User>();
    private User latestChanger;
    private StringBuilder notes;


    /*
    Måste hålla koll på, skapare, stringbuilder med notiser, när någon skapar/ändrar läggs denna till i contributers.

     */
    public JournalEntry(User user) {
        this.latestChanger = user;
        contributers.add(user);
    }

    public String getUserDivision() {
        return latestChanger.getDivision();
    }

    public String getUserID() {
        return latestChanger.getID();
    }

    public User getUser(){
        return latestChanger;
    }
}
