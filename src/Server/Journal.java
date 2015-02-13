package src.Server;

/**
 * Created by Viktor on 2015-02-13.
 */
public class Journal {
    private Patient patient; //the Journal is about this patient!

    public Journal() {

    }
//
//    /**
//     * Method for adding an entry to the Journal, should only be accesible by a nurse or doctor.
//     *
//     * @
//     */
//    public addEntry(JournalEntry newEntry) {
//        if (getWritePermit(newEntry.getUser) {
//            list.add(newEntry);
//        }else{
//            System.out.println("You are not authorized to add entries to this journal");
//        }
//    }
//
//    public readEntry(/*something to specify entry?*/ User u) {
//
//    }
//
//    public deleteJournal(User u) {
//    }
//
//    private getReadPermit(User currentUser) {
//        if (currentUser.equals(patient)) {
//            return true;
//        } else if (currentUser.isStaff() && currentUser.getDivision() == patient.getDivision()) {
//            return true;
//        } else if (currentUser.isGovernment()) {
//            return true;
//        } else {
//            return false;
//        }
//    }
//
//    private getWritePermit(User currentUser) {
//        if (patient.isTreatedBy(currentUser) {
//            return true;
//        }else{
//            return false;
//        }
//    }
//
//    private getDeletePermit(User currentUser) {
//        if (currentUser.isGovernment()) {
//            return true;
//        } else {
//            return false;
//        }
//    }
}
