package src.Server;

/**
 * Created by Viktor on 2015-02-13.
 */
public class Government extends User {
    public Government(String id, String division) {
        super(id, division);
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
        return true;
    }
}
