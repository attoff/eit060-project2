package src.Server;

import javax.net.ServerSocketFactory;
import javax.net.ssl.*;
import javax.security.cert.X509Certificate;

import java.io.*;
import java.net.ServerSocket;
import java.security.KeyStore;
import java.util.Date;
import java.util.HashMap;

public class Server implements Runnable {
	private ServerSocket serverSocket = null;
	private static int numConnectedClients = 0;
	private HashMap<String, Journal> journals;
	private HashMap<String, User> users;

	public Server(ServerSocket ss) throws IOException {
		serverSocket = ss;
		try {
			File file = new File("users");
			// if file doesnt exists, then create it
			if (!file.exists()) {
				file.createNewFile();
				users = new HashMap<String, User>();
			}
			file = new File("journals");
			// if file doesnt exists, then create it
			if (!file.exists()) {
				file.createNewFile();
				journals = new HashMap<String, Journal>();
			}
			file = null;
			try {
				FileInputStream fis = new FileInputStream("journals");
				ObjectInputStream ois = new ObjectInputStream(fis);
				journals = (HashMap<String, Journal>) ois.readObject();
				ois.close();
				fis.close();
			} catch (EOFException e) {
				System.out.println("jurnals was empty, nothing loaded");
				journals = new HashMap<String, Journal>();
			}
			try {
				FileInputStream fis = new FileInputStream("users");
				ObjectInputStream ois = new ObjectInputStream(fis);
				users = (HashMap<String, User>) ois.readObject();
				ois.close();
				fis.close();
			} catch (EOFException e) {
				System.out.println("users was empty, nothing loaded");
				users = new HashMap<String, User>();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		newListener();
	}

	public void run() {
		try {
			SSLSocket socket = (SSLSocket) serverSocket.accept();
			newListener();
			SSLSession session = socket.getSession();
			X509Certificate cert = (X509Certificate) session
					.getPeerCertificateChain()[0];
			// String subject = cert.getSubjectDN().getName();
			// String issuer = cert.getIssuerDN().getName();
			numConnectedClients++;
			System.out.println("client connected");
			// System.out.println("client name (cert subject DN field): " +
			// subject);

			// System.out.println("certifcate issuername: " + issuer + "\n");
			System.out.println("certicate serialnumber: "
					+ cert.getSerialNumber() + "\n");
			System.out.println(numConnectedClients
					+ " concurrent connection(s)\n");
			String currentUserName = getSubjectDN(cert);

			PrintWriter out = null;
			BufferedReader in = null;
			out = new PrintWriter(socket.getOutputStream(), true);
			in = new BufferedReader(new InputStreamReader(
					socket.getInputStream()));
			User currentUser = new Patient("Should", "not exist");
			if (findUser(currentUserName) != null) {
				currentUser = findUser(currentUserName);
				out.println("User " + currentUserName
						+ " logged in succesfully!");

				log(currentUser.toString() + "logged in succesfully!");
			}else if (currentUserName.compareTo("Doctor Name") == 0){
				log("first time user Doctor Name logged in");
			} else {
				out.println("No such user," + currentUserName
						+ " connection closed");
				in.close();
				out.close();
				socket.close();
				numConnectedClients--;
				System.out.println("client disconnected");
				System.out.println(numConnectedClients
						+ " concurrent connection(s)\n");
			}
			out.flush();
			String clientMsg = null;
			while ((clientMsg = in.readLine()) != null) {
				//command create with subcomands
				if (clientMsg.equalsIgnoreCase("create")) {
					out.println("Type the social security number of the new patient:");
					if ((clientMsg = in.readLine()) != null) {
						if (findUser(clientMsg) == null) {
							if (currentUser.isDoctor() == true) {
								Patient pat = new Patient(clientMsg,
										currentUser.getDivision());
								createJournal(currentUser, pat);
								users.put(clientMsg, pat);
								save("users");
								log(currentUser + " added the patient "
										+ clientMsg);
								log(currentUser + " created a new journal for "
										+ clientMsg);
								out.println("A journal for " + clientMsg
										+ " was created");
								out.println("Do you want to add more treaters? Y/N");
								if ((clientMsg = in.readLine()) != null) {
									if (clientMsg.equalsIgnoreCase("y")
											|| clientMsg
													.equalsIgnoreCase("yes")) {
										out.println("what is the id of the treater?");
										if ((clientMsg = in.readLine()) != null
												&& findUser(clientMsg) != null) {
											findJournal(pat).addTreater(
													currentUser,
													findUser(clientMsg));
											save("journals");
											log(currentUser + " added "
													+ findUser(clientMsg)
													+ " as treater to " + pat);
											out.println("Done adding "
													+ findUser(clientMsg)
													+ " as a treater to " + pat);
										} else {
											out.println("Did not manage to add the treater");
										}
									} else {
										out.println("Did not add any treater");
									}
								}
							} else {
								out.println("You are not permitted to do this action");
								log(currentUser
										+ " tried to create a journal for "
										+ clientMsg + " , but was not allowed");
							}
						} else {
							out.println("User could not be found");
						}
					}
					//command edit with subcomands
				} else if (clientMsg.equalsIgnoreCase("edit")) {
					out.println("Type the social security number of the patient you want to edit:");
					if ((clientMsg = in.readLine()) != null) {
						if (findUser(clientMsg) != null) {
							User patient = findUser(clientMsg);
							Journal patientJournal = findJournal(patient);
							out.println("Do you want to add a note or a treater?");
							if ((clientMsg = in.readLine()) != null) {
								if (clientMsg.equalsIgnoreCase("note")) {
									out.println("What note would you like to add?");
									if ((clientMsg = in.readLine()) != null) {
										patientJournal
												.addEntry(new JournalEntry(
														currentUser, clientMsg));
										log(currentUser
												+ " created a new note for "
												+ patient);
										save("journals");
										out.println("Note added for patient "
												+ patient);
									} else {
										out.println("Message was empty, no note was added");
									}
								} else if (clientMsg
										.equalsIgnoreCase("treater")) {
									out.println("what is the id of the treater?");
									if ((clientMsg = in.readLine()) != null
											&& findUser(clientMsg) != null) {
										patientJournal.addTreater(currentUser,
												users.get(clientMsg));
										save("journals");
										log(currentUser + " added "
												+ findUser(clientMsg)
												+ " as treater to " + patient);
										out.println("Done adding "
												+ findUser(clientMsg)
												+ " as a treater to " + patient);
									} else {
										out.println("Did not manage to add the treater");
									}
								}
							}
						} else {
							out.println("User could not be found");
						}
					}
					//command delete with subcommands
				} else if (clientMsg.equalsIgnoreCase("delete")) {
					out.println("Type the social security number of the patient, which journal you would like to delete:");
					if ((clientMsg = in.readLine()) != null) {
						if (findUser(clientMsg) != null) {
							deleteJournal(currentUser,
									(Patient) findUser(clientMsg));
							log(currentUser + " deleted " + clientMsg
									+ "'s journal");
							out.println("Deleted " + clientMsg + "'s journal");
						} else {
							out.println("User could not be found");
						}
					}
					//command read with subcommands
				} else if (clientMsg.equalsIgnoreCase("read")) {
					out.println("Type the social security number of the patient:");
					if ((clientMsg = in.readLine()) != null) {
						if (findUser(clientMsg) != null) {
							User patient = findUser(clientMsg);
							out.println("Journal: \n"
									+ findJournal(patient).readJournal(patient,
											currentUser));
							log(currentUser + " read " + clientMsg + " journal");
						} else {
							out.println("No such user");
							log(currentUser
									+ " tried to read nonexisting journal for "
									+ clientMsg);
						}
					}
					//comand help
				} else if (clientMsg.equalsIgnoreCase("help")) {
					out.println("Commands are as follows: create, edit, delete, read and help. \n Use one of these commands and follow the intructions.");
				//command init, should only be doable once.
				} else if (clientMsg.equalsIgnoreCase("init")) {
					if (init(currentUser)) {
						out.println("tried to initialize");
					} else {
						out.println("server is already initiated, you have been reported!");
					}
					//command createuser, can only be accesed with Doctor Name
				} else if (clientMsg.equalsIgnoreCase("createuser")
						&& currentUser.getID().compareTo("Doctor Name") == 0) {
					out.println("Type the Id of the staffmember");
					String name = "", div = "", type = "";
					if ((clientMsg = in.readLine()) != null) {
						name = clientMsg;
					}
					out.println("Type the division of the staffmember");
					if ((clientMsg = in.readLine()) != null) {
						div = clientMsg;
					}
					out.println("What position does the staffmember have, case-sensitive, always start with a capital");
					if ((clientMsg = in.readLine()) != null) {
						type = clientMsg;
					}
					createUser(name, div, type);
					log(currentUser + " added the " + type + " " + name
							+ " in the division " + div);
					out.println("Successfull add of " + name);
				} else {
					out.println("Invalid command, type help for help!");
				}
				out.flush();
			}
			in.close();
			out.close();
			socket.close();
			numConnectedClients--;
			System.out.println("client disconnected");
			System.out.println(numConnectedClients
					+ " concurrent connection(s)\n");
		} catch (IOException e) {
			System.out.println("Client died: " + e.getMessage());
			e.printStackTrace();
			return;
		}
	}

	private void newListener() {
		(new Thread(this)).start();
	} // calls run()

	public static void main(String args[]) {
		System.out.println("\nServer Starting up\n");
		int port = -1;
		if (args.length >= 1) {
			port = Integer.parseInt(args[0]);
		}
		String type = "TLS";
		try {
			ServerSocketFactory ssf = getServerSocketFactory(type);
			ServerSocket ss = ssf.createServerSocket(port);
			((SSLServerSocket) ss).setNeedClientAuth(true); // enables client
			// authentication
			new Server(ss);
			System.out.println("Server started succesfully!");
		} catch (IOException e) {
			System.out.println("Unable to start Server: " + e.getMessage());
			e.printStackTrace();
		}
	}

	private static ServerSocketFactory getServerSocketFactory(String type) {
		if (type.equals("TLS")) {
			SSLServerSocketFactory ssf = null;
			try { // set up key manager to perform server authentication
				SSLContext ctx = SSLContext.getInstance("TLS");
				KeyManagerFactory kmf = KeyManagerFactory
						.getInstance("SunX509");
				TrustManagerFactory tmf = TrustManagerFactory
						.getInstance("SunX509");
				KeyStore ks = KeyStore.getInstance("JKS");
				KeyStore ts = KeyStore.getInstance("JKS");
//				char[] password = "password".toCharArray();
				char[] password = readPassword("Please enter your password");
				ks.load(new FileInputStream("serverkeystore"), password); // keystore
				// password
				// (storepass)
				ts.load(new FileInputStream("servertruststore"), password); // truststore
				// password
				// (storepass)
				System.out.println("Password entered succesfully!");
				kmf.init(ks, password); // certificate password (keypass)
				tmf.init(ts); // possible to use keystore as truststore here
				ctx.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
				ssf = ctx.getServerSocketFactory();
				return ssf;
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			return ServerSocketFactory.getDefault();
		}
		return null;
	}

	private void createJournal(User currentUser, Patient patient) {
		if (currentUser.isDoctor()) {
			Journal newJournal = new Journal(patient);
			newJournal.addTreater(currentUser, currentUser);
			journals.put(patient.getID(), newJournal);
			try {
				save("journals");
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private void deleteJournal(User currentUser, Patient patient) {
		if (currentUser.isGovernment()) {
			journals.remove(patient.getID());
		}
		try {
			save("journals");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private User findUser(String id) {
		if (users.containsKey(id)) {
			return users.get(id);
		} else {
			return null;
		}
	}

	private Journal findJournal(User patient) {
		return journals.get(patient.getID());
	}

	private static String getSubjectDN(X509Certificate cert) {
		String dn = ((X509Certificate) cert).getSubjectDN().getName();
		String[] dnSplits = dn.split(",");
		for (String dnSplit : dnSplits) {
			String attributeType = "CN=";
			if (dnSplit.contains(attributeType)) {
				String[] cnSplits = dnSplit.trim().split("=");
				if (cnSplits[1] != null) {
					return cnSplits[1].trim();
				}
			}
		}
		return "";
	}

	private boolean init(User currentUser) {
		if (!users.containsKey("Doctor Name")) {
			Doctor name = new Doctor("Doctor Name", "0");
			users.put("Doctor Name", name);
			log("Server was initialized!");
			try {
				FileOutputStream fos = new FileOutputStream("users");
				ObjectOutputStream oos = new ObjectOutputStream(fos);
				oos.writeObject(users);
				oos.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
			return true;
		} else {
			log(currentUser + " tried to init, bad user!");
			return false;
		}
	}

	/* Prints current time and the message to a file */
	static void log(String message) {
		try {
			File file = new File("log.txt");
			// if file doesnt exists, then create it
			if (!file.exists()) {
				file.createNewFile();
			}
			StringBuilder out = new StringBuilder();
			FileWriter fw = new FileWriter(file.getAbsoluteFile(), true);
			BufferedWriter pw = new BufferedWriter(fw);
			out.append(new Date(System.currentTimeMillis()) + ": ");
			out.append(message);
			pw.write(out.toString());
			pw.newLine();
			pw.flush();
			pw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void save(String fileName) throws FileNotFoundException {
		try {
			FileOutputStream fout = new FileOutputStream(fileName);
			ObjectOutputStream oos = new ObjectOutputStream(fout);
			if (fileName.compareTo("users") == 0) {
				oos.writeObject(users);
			} else if (fileName.compareTo("journals") == 0) {
				oos.writeObject(journals);
			}
			fout.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void createUser(String id, String dev, String type) {
		if (type.compareTo("Doctor") == 0) {
			Doctor doc = new Doctor(id, dev);
			users.put(id, doc);

		} else if (type.compareTo("Nurse") == 0) {
			Nurse nurse = new Nurse(id, dev);
			users.put(id, nurse);

		} else if (type.compareTo("Government") == 0) {
			Government gov = new Government(id, dev);
			users.put(id, gov);

		}
		try {
			save("users");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	/* method to get private input of password when not using eclipse because bug */
	private static char[] readPassword(String message) throws IOException {
		if (System.console() != null) {
			return System.console().readPassword(message);
		}
		System.out.println(message);
		return new BufferedReader(new InputStreamReader(System.in)).readLine()
				.toCharArray();
	}
}
