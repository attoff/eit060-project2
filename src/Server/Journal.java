package src.Server;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by Viktor on 2015-02-13.
 */
public class Journal implements Serializable {
	private Patient patient; // the Journal is about this patient!
	private ArrayList<JournalEntry> entries;
	private ArrayList<User> autherized;

	public Journal(Patient patient) {
		this.patient = patient;
		autherized = new ArrayList<User>();
		entries = new ArrayList<JournalEntry>();

	}

	/**
	 * Method for adding an entry to the Journal, should only be accesible by a
	 * nurse or doctor.
	 *
	 * @
	 */
	public void addEntry(JournalEntry newEntry) {
		if (getWritePermit(newEntry.getUser())) {
			entries.add(newEntry);
		} else {
			System.out
					.println("You are not authorized to add entries to this journal");
		}
	}

	public void addTreater(User currentUser, User treater) {
		if (currentUser.isDoctor()) {
			autherized.add(treater);
		} else {
			System.out.println(currentUser
					+ " is not authorized to add a treater to "
					+ patient.getID());
		}
	}

	public String readJournal(User patient, User reader) {
		/* Kolla readpermit, släng ut allt i entries */
		StringBuilder sb = new StringBuilder();
		if (getReadPermit(reader)) {
			for (JournalEntry entry : entries) {
				sb.append(entry.toString() + "\n");
			}
			return sb.toString();
		}
		sb.append("You are not allowed to read this journal!");
		return sb.toString();
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
		System.out.println(currentUser + " " + patient);
		if (currentUser.equals(patient)) {
			return true;
		} else if (currentUser.isStaff()
				&& currentUser.getDivision().compareTo(patient.getDivision())==0) {
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

	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (JournalEntry entry : entries) {
			sb.append(entry.toString() + "\n");
		}
		return sb.toString();
	}
}
