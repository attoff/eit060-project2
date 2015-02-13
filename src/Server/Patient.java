package src.Server;

/**
 * Created by Viktor on 2015-02-13.
 */
public class Patient extends User {

    public Patient(String id, String division) {
        super(id, division);
    }

    @Override
    public boolean isStaff() {
        return false;
    }

    @Override
    public boolean isTreatedBy() {
        return false;
    }

    @Override
    public boolean isGovernment() {
        return false;
    }
}
