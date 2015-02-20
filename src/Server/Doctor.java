package src.Server;

/**
 * Created by Viktor on 2015-02-13.
 */
public class Doctor extends User {
    public Doctor(String id, String division) {
        super(id, division);
    }

    @Override
    public boolean isDoctor() {
        return true;
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
