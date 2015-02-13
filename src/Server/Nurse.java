package src.Server;

/**
 * Created by Viktor on 2015-02-13.
 */
public class Nurse extends User {

    public Nurse(String id, String division) {
        super(id, division);
    }

    @Override
    public boolean isDoctor() {
        return false;
    }

    @Override
    public boolean isStaff() {
        return true;
    }

    @Override
    public boolean isGovernment() {
        return false;
    }
}
