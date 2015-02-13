package src.Server;

import java.util.ArrayList;

/**
 * Created by Viktor on 2015-02-13.
 */
public class Patient extends User {
    private ArrayList<User> autherized;
    private String id;

    public Patient(String id, String division) {
        super(id, division);
        this.id = id;
    }

    @Override
    public boolean isDoctor() {
        return false;
    }

    public void addTreater(User treater) {
        autherized.add(treater);
    }

    @Override
    public boolean isStaff() {
        return false;
    }

    @Override
    public boolean isTreatedBy(User u) {
        for (User user : autherized) {
            if (user.getID().compareTo(u.getID()) == 0) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isGovernment() {
        return false;
    }
}
