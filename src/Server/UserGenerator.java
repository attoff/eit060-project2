package src.Server;

import java.util.ArrayList;

public class UserGenerator{



	public static ArrayList<User> generate(){
		ArrayList<User> tmp = new ArrayList<User>();
		User doctor = new Doctor("Doctor", "First");
		User nurse = new Nurse("Nurse", "First");
		User test = new Patient("Test", "First");
		tmp.add(doctor);
		tmp.add(nurse);
		tmp.add(test);
		return tmp;
	}
	public static ArrayList<Journal> generateJourn(){
		ArrayList<Journal> tmp = new ArrayList<Journal>();
		Patient pat  = new Patient("Test", "First");
		Journal test = new Journal(pat);
//		test.addEntry(new JournalEntry("This is a test"));
		return tmp;
	}
}
