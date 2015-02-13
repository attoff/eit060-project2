package src.Server;

public class JournalEntry {
    private User user;

    public JournalEntry(User user) {
        this.user = user;
    }

    public String getUserDivision() {
        return user.getDivision();
    }

    public String getUserID() {
        return user.getID();

    }
}
