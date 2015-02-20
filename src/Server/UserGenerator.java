package src.Server;

import java.util.ArrayList;

public class UserGenerator{



	public static ArrayList<User> generate(){
		ArrayList<User> tmp = new ArrayList<User>();
		User doctor = new Doctor("Doctor", "First");
		User nurse = new Nurse("Nurse", "First");
		tmp.add(doctor);
		tmp.add(nurse);
		return tmp;
	}
}
