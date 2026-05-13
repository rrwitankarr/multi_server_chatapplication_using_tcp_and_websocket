package chat; 

import java.io.*;
import java.net.*;

public class client {

    static boolean crashed = false;
    static boolean notified = false;

    public static void main(String[] args) {

        int port = Integer.parseInt(args[0]);
        int backupPort = Integer.parseInt(args[1]);

        try {

            Socket s = new Socket("localhost", port);

            BufferedReader input = new BufferedReader(new InputStreamReader(System.in));

            BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));

            PrintWriter out = new PrintWriter(s.getOutputStream(), true);

            System.out.print("Enter your name: ");
            String name = input.readLine();
            out.println(name);

            // receiver
            new Thread(() -> {

                try {
                    String msg;
                    while ((msg = in.readLine()) != null)
                        System.out.println(msg);

                } catch (Exception e) {

                    crashed = true;

                    System.out.println("\nServer disconnected!");
                    System.out.println("Backup Server running on port " + backupPort);
                    System.out.println("Reconnect using:");
                    System.out.println("java Client " + backupPort + " " + port);

                    // check primary ONLY after crash
                    new Thread(() -> {

                        while (crashed) {

                            try {
                                Thread.sleep(5000);
                                new Socket("localhost", port).close();

                                if (!notified) {
                                    System.out
                                            .println("\nPrimary server is back online. You may reconnect if desired.");
                                    notified = true;
                                    crashed = false; // stop loop
                                }

                            } catch (Exception ex) {
                            }
                        }

                    }).start();
                }

            }).start();

            String text;
            while ((text = input.readLine()) != null)
                out.println(text);

        } catch (Exception e) {
            System.out.println("Unable to connect to server.");
        }
    }
}
