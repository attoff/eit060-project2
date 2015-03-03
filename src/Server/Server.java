package src.Server;

import javax.net.ServerSocketFactory;
import javax.net.ssl.*;
import javax.security.cert.X509Certificate;
import java.io.*;
import java.net.ServerSocket;
import java.security.KeyStore;
import java.util.HashMap;

public class Server implements Runnable {
    private ServerSocket serverSocket = null;
    private static int numConnectedClients = 0;
    private HashMap<String, Journal> journals;
    private HashMap<String, User> users;

    public Server(ServerSocket ss) throws IOException {
        serverSocket = ss;
        newListener();
        journals = new HashMap<String, Journal>();
        users = new HashMap<String, User>();
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
            log(currentUser.toString());
            String clientMsg = null;
            while ((clientMsg = in.readLine()) != null) {
                if (clientMsg.equalsIgnoreCase("create")) {
                    System.out.println("Type the name of the new patient:");
                    if ((clientMsg = in.readLine()) != null) {
                        if (findUser(clientMsg) == null) {
                            if (currentUser.isDoctor() == true) {
                                createJournal(currentUser, new Patient(clientMsg, currentUser.getDivision()));
                                log(currentUser + " created a new journal for " + clientMsg);
                            } else {
                                System.out.println("You are not permitted to do this action");
                            }
                        } else {
                            System.out.println("User could not be found");
                        }
                    }
                } else if (clientMsg.equalsIgnoreCase("edit")) {
                    System.out.println("Type the name of the patient you want to edit:");
                    if ((clientMsg = in.readLine()) != null) {
                        if (findUser(clientMsg) != null) {
                            User patient = findUser(clientMsg);
                            Journal patientJournal = findJournal(patient);
                            System.out.println("What note would you like to add?");
                            if ((clientMsg = in.readLine()) != null) {
                                patientJournal.addEntry(new JournalEntry(currentUser, clientMsg));
                                log(currentUser + " created a new note for " + clientMsg);
                            } else {
                                System.out.println("Message was empty, no note was added");
                            }
                        } else {
                            System.out.println("User could not be found");
                        }
                    }
                } else if (clientMsg.equalsIgnoreCase("delete")) {
                    System.out.println("Type the name of the patient, which journal you would like to delete:");
                    if ((clientMsg = in.readLine()) != null) {
                        if (findUser(clientMsg) != null) {
                            deleteJournal(currentUser, (Patient) findUser(clientMsg));
                            log(currentUser + " deleted " + clientMsg + " journal");
                        } else {
                            System.out.println("User could not be found");
                        }
                    }
                } else if (clientMsg.equalsIgnoreCase("read")) {
                    out.println("Type the name of the patient:");
                    if ((clientMsg = in.readLine()) != null) {
                        if (findUser(clientMsg) != null) {
                            User patient = findUser(clientMsg);
                            out.print(findJournal(patient).readJournal(patient,
                                    currentUser));
                            log(currentUser + " read " + clientMsg + " journal");
                        }
                    }
                } else if (clientMsg.equalsIgnoreCase("help")) {
                    out.println("Commands are as follows: create, edit, delete, read and help. \n Use one of these commands and follow the intructions.");
                } else {
                    out.println("Invalid command, type help for help!");
                }
                out.flush();
            }
//			for (int i = 0; i < 10; i++) {
//				out.println("HI" + i);
//			}
//			out.println("Exit");
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
            newJournal.addTreater(currentUser, currentUser);
            journals.put(currentUser.getID(), newJournal);
        }
    }

    private void deleteJournal(User currentUser, Patient patient) {
        if (currentUser.isGovernment()) {
            journals.remove(patient.getID());
        }
    }

    private User findUser(String id) {
        return users.get(id);
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

    private static void log(String message) {
        try {
            File file = new File("log.txt");
            // if file doesnt exists, then create it
            if (!file.exists()) {
                file.createNewFile();
            }
            FileWriter fw = new FileWriter(file.getAbsoluteFile());
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(message);
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
