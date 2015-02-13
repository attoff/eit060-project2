package src.Server;

import java.util.ArrayList;

/**
 * Created by Viktor on 2015-02-13.
 */
public class Journal {
    private Patient patient; //the Journal is about this patient!
    private ArrayList<JournalEntry> entries;
    private ArrayList<User> autherized;

    public Journal(Patient patient) {
        this.patient = patient;
    }

    /**
     * Method for adding an entry to the Journal, should only be accesible by a nurse or doctor.
     *
     * @
     */
    public void addEntry(JournalEntry newEntry) {
        if (getCreatePermit(newEntry.getUser())) {
            entries.add(newEntry);
        } else {
            System.out.println("You are not authorized to add entries to this journal");
        }
    }

    public User getPatient() {
        return patient;
    }

    public void addTreater(User currentUser, User treater) {
        if (currentUser.isDoctor()) {
            autherized.add(treater);
        }
        System.out.println("You are not authorized to add a treater to this patient");
    }


    public StringBuilder readJournal(User currentUser, User reader) {
        /*Kolla readpermit, släng ut allt i entries */
        StringBuilder sb = new StringBuilder();
        if (getReadPermit(currentUser)) {
            for (JournalEntry entry : entries) {
                sb.append(entry.toString() + "\n");
            }
            return sb;
        }
        sb.append("Ni får ej läsa denna journal");
        return sb;
    }

    private boolean isTreatedBy(User u) {
        for (User user : autherized) {
            if (user.getID().compareTo(u.getID()) == 0) {
                return true;
            }
        }
        return false;
    }


    private boolean getReadPermit(User currentUser) {
        if (currentUser.equals(patient)) {
            return true;
        } else if (currentUser.isStaff() && currentUser.getDivision() == patient.getDivision()) {
            return true;
        } else if (currentUser.isGovernment()) {
            return true;
        } else {
            return false;
        }
    }

    private boolean getWritePermit(User currentUser) {
        if (isTreatedBy(currentUser)) {
            return true;
        }
        return false;

    }

    private boolean getCreatePermit(User currentUser) {
        if (currentUser.isDoctor()) {
            return true;
        }
        return false;
    }
}
