package src.Server;

import javax.net.ServerSocketFactory;
import javax.net.ssl.*;
import javax.security.cert.X509Certificate;

import java.io.*;
import java.net.ServerSocket;
import java.security.KeyStore;
import java.util.ArrayList;

public class Server implements Runnable {
	private ServerSocket serverSocket = null;
	private static int numConnectedClients = 0;
	private ArrayList<Journal> database;
	private ArrayList<User> users;

	public Server(ServerSocket ss) throws IOException {
		serverSocket = ss;
		newListener();
		database = new ArrayList<Journal>();
		users = new ArrayList<User>();
		users.addAll(UserGenerator.generate());
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
			} else {
				out.println("No such user," + currentUserName
						+ " connection closed");
			}
			String clientMsg = null;
			 while ((clientMsg = in.readLine()) != null) {
			 if (clientMsg.equalsIgnoreCase("create")) {
			 } else if (clientMsg.equalsIgnoreCase("edit")) {
			 } else if (clientMsg.equalsIgnoreCase("delete")) {
			 } else if (clientMsg.equalsIgnoreCase("read")) {
			 out.println("Type the name of the patient:");
			 if ((clientMsg = in.readLine()) != null) {
			 if(findUser(clientMsg) != null){
			 User patient = findUser(clientMsg);
			 out.print(findJournal(patient).readJournal(patient,
			 currentUser));
			 }
			 }
			 } else if (clientMsg.equalsIgnoreCase("help")) {
			 out.println("Commands are as follows: create, edit, delete, read and help");
			 } else {
			 out.println("Invalid command, type help for help!");
			 }
			 // String rev = new
			 // StringBuilder(clientMsg).reverse().toString();
			 // System.out.println("received '" + clientMsg +
			 // "' from client");
			 // System.out.print("sending '" + rev + "' to client...");
			 // out.println(rev);
			 out.flush();
			 // System.out.println("done\n");
			 }
			for (int i = 0; i < 10; i++) {
				out.println("HI" + i);
			}
			out.println("Exit");
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
		System.out.println("\nServer Started\n");
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
				char[] password = "password".toCharArray();

				ks.load(new FileInputStream("serverkeystore"), password); // keystore
																			// password
																			// (storepass)
				ts.load(new FileInputStream("servertruststore"), password); // truststore
																			// password
																			// (storepass)
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
			database.add(newJournal);
		}
	}

	private void deleteJournal(User currentUser, Patient patient) {
		if (currentUser.isGovernment()) {
			Journal tmp = findJournal(patient);
			database.remove(tmp);
		}
	}

	private User findUser(String id) {
		for (User u : users) {
			if (u.getID().compareTo(id) == 0) {
				return u;
			}
		}
		return null;
	}

	private Journal findJournal(User patient) {
		for (int i = 0; i < database.size(); i++) {
			if (database.get(i).getPatient().getID().compareTo(patient.getID()) == 0) {
				return database.get(i);
			}
		}
		return null;
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
}
