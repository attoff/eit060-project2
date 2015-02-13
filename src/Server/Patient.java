package src.Server;

import java.util.ArrayList;

/**
 * Created by Viktor on 2015-02-13.
 */
public class Patient extends User {

    private String id;

    public Patient(String id, String division) {
        super(id, division);
        this.id = id;
    }

    @Override
    public boolean isDoctor() {
        return false;
    }



    @Override
    public boolean isStaff() {
        return false;
    }



    @Override
    public boolean isGovernment() {
        return false;
    }
}
