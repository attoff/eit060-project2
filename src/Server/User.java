package src.Server;

import java.io.Serializable;

/**
 * Created by Viktor on 2015-02-13.
 */
public abstract class User implements Serializable {
    String id;
    String division;

    public User(String id, String division) {
        this.id = id;
        this.division = division;
    }


    public String getID() {
        return id;
    }

    public String getDivision() {
        return division;
    }

    public abstract boolean isDoctor();

    public abstract boolean isStaff();

    public abstract boolean isGovernment();

    public boolean equals(User p) {
        if (id.compareTo(p.getID()) == 0) {
            return true;
        }
        return false;
    }

    public String toString() {
        return "ID: " + id + ", " + "Division: " + division + "\n";
    }
}
