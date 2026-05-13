# Distributed Multi-Server Chat Application

A distributed real-time chat application built using Java, WebSockets, and TCP socket communication.  
The system supports multi-server communication, real-time messaging, and basic fault tolerance between interconnected servers.

---

## Features

- Real-time chat using WebSockets
- Multi-server communication using TCP sockets
- Distributed message synchronization
- Client-server architecture
- Automatic message relay between servers
- Basic fault tolerance and server switching
- Interactive modern web-based UI
- Multi-user chat support

---

## Technologies Used

- Java
- Jakarta WebSocket API
- TCP Sockets
- Maven
- HTML
- CSS
- JavaScript
- GlassFish Tyrus WebSocket Server

---

## Project Structure

```text
src/
 └── main/
      ├── java/
      │    └── chat/
      │         ├── ChatServer.java
      │         ├── server.java
      │         └── client.java
      │
      └── resources/
           └── static/
                ├── index.html
                ├── style.css
                └── test_html.css
