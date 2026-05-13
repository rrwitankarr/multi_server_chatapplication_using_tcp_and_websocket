package chat; 

import java.io.*;
import java.net.*;
import java.util.*;

public class server {

    static Vector<ClientHandler> clients = new Vector<>();
    static int clientCount = 0;

    static int MY_PORT;
    static int OTHER_SERVER_PORT;

    public static void main(String[] args) throws Exception {

        MY_PORT = Integer.parseInt(args[0]);
        OTHER_SERVER_PORT = Integer.parseInt(args[1]);

        ServerSocket ss = new ServerSocket(MY_PORT);
        System.out.println("Server started on port " + MY_PORT);
        System.out.println("Connected peer server on port " + OTHER_SERVER_PORT);

        while(true) {

            Socket s = ss.accept();

            BufferedReader tempIn =
                new BufferedReader(new InputStreamReader(s.getInputStream()));

            String first = tempIn.readLine();

            // RELAY from other server
            if(first != null && first.startsWith("RELAY:")) {

                String actual = first.substring(6);

                for(ClientHandler c : clients)
                    c.out.println(actual);

                s.close();
                continue;
            }

            // REAL client
            clientCount++;

            ClientHandler ch = new ClientHandler(s, clientCount, first);
            clients.add(ch);
            ch.start();
        }
    }

    static void relayToOtherServer(String msg) {

        try {
            Socket s = new Socket("localhost", OTHER_SERVER_PORT);
            PrintWriter out = new PrintWriter(s.getOutputStream(), true);
            out.println(msg);
            s.close();
        } catch(Exception e) {
            System.out.println("Other server unreachable.");
        }
    }

    static class ClientHandler extends Thread {

        Socket socket;
        BufferedReader in;
        PrintWriter out;
        int clientId;
        String name;

        ClientHandler(Socket s, int id, String firstName) throws Exception {

            socket = s;
            clientId = id;
            name = firstName;

            in = new BufferedReader(new InputStreamReader(s.getInputStream()));
            out = new PrintWriter(s.getOutputStream(), true);

            System.out.println("Client-" + clientId + " (" + name + ") connected");
        }

        public void run() {

            try {

                String msg;

                while((msg = in.readLine()) != null) {

                    String full = name + ": " + msg;

                    System.out.println(full);

                    // send to ALL local clients
                    for(ClientHandler c : clients)
                        c.out.println(full);

                    // forward to other server
                    relayToOtherServer("RELAY:" + full);
                }

            } catch(Exception e){}
        }
    }
}
