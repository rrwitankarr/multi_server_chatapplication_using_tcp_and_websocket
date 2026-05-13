package chat;
import jakarta.websocket.*;
import jakarta.websocket.server.ServerEndpoint;
import org.glassfish.tyrus.server.Server;

import java.io.*;
import java.net.*;
import java.util.*;

@ServerEndpoint("/chat")
public class ChatServer {

    static Set<Session> clients = Collections.synchronizedSet(new HashSet<>());
    static Map<Session,String> names = new HashMap<>();

    static int MY_WS_PORT;
    static int OTHER_TCP_PORT;

    // ================= WEBSOCKET =================

    @OnOpen
    public void open(Session s){
        clients.add(s);
        sendUsers();
    }

    @OnMessage
    public void onMessage(String msg, Session s){

        // first message = username
        if(!names.containsKey(s)){
            names.put(s,msg);
            broadcast("SYSTEM: "+msg+" joined");
            sendUsers();
            return;
        }

        // manual switch commands
        if(msg.equalsIgnoreCase("switch A") || msg.equalsIgnoreCase("switch B")){
            try { s.close(); } catch(Exception e){}
            return;
        }

        String full = names.get(s)+": "+msg;

        broadcast(full);
        relay("RELAY:"+full);
    }

    @OnClose
    public void close(Session s){
        clients.remove(s);
        String n = names.get(s);
        names.remove(s);

        if(n!=null)
            broadcast("SYSTEM: "+n+" left");

        sendUsers();
    }

    // ================= HELPERS =================

    static void broadcast(String m){
        for(Session x:clients)
            try{x.getBasicRemote().sendText(m);}catch(Exception e){}
    }

   static void sendUsers(){

    StringBuilder list = new StringBuilder("USERS:");

    int i = 0;

    for(String n : names.values()) {

        if(i > 0)
            list.append(",");

        list.append(n);
        i++;
    }

    broadcast(list.toString());
}
    // ================= TCP RELAY =================

    static void relay(String msg){
        try{
            Socket s=new Socket("localhost",OTHER_TCP_PORT);
            PrintWriter out=new PrintWriter(s.getOutputStream(),true);
            out.println(msg);
            s.close();
        }catch(Exception e){}
    }

    // ================= MAIN =================

    public static void main(String[] args) throws Exception{

        MY_WS_PORT=Integer.parseInt(args[0]);
        OTHER_TCP_PORT=Integer.parseInt(args[1]);

        Server ws=new Server("localhost",MY_WS_PORT,"/",null,ChatServer.class);
        ws.start();

        System.out.println("WebSocket Server on "+MY_WS_PORT);

        // TCP listener for relay
        new Thread(()->{
            try{
                ServerSocket ss=new ServerSocket(MY_WS_PORT+1000);
                while(true){
                    Socket s=ss.accept();
                    BufferedReader in=new BufferedReader(new InputStreamReader(s.getInputStream()));
                    String m=in.readLine();
                    if(m!=null && m.startsWith("RELAY:"))
                        broadcast(m.substring(6));
                    s.close();
                }
            }catch(Exception e){}
        }).start();

        Thread.currentThread().join();
    }
}
