package ru.itis.drawandguess.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.*;

public class ClientHandler implements Runnable {
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private String nickname;
    private boolean ready = false;
    private List<ClientHandler> clients;

    public ClientHandler(Socket socket, List<ClientHandler> clients) {
        this.socket = socket;
        this.clients = clients;
    }

    @Override
    public void run() {
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            while (true) {
                String message = in.readLine();
                if (message == null) {
                    return;
                }

                if (message.startsWith("CREATE_LOBBY")) {
                    String[] parts = message.split(" ", 3);
                    String password = parts[1];
                    int maxPlayers = Integer.parseInt(parts[2]);
                    if (Server.createLobby(password, maxPlayers, this)) {
                        out.println("LOBBY_CREATED");
                    } else {
                        out.println("ERROR: Could not create lobby.");
                    }
                } else if (message.startsWith("JOIN_LOBBY")) {
                    String password = message.split(" ", 2)[1];
                    if (Server.joinLobby(password, this)) {
                        out.println("LOBBY_JOINED");
                    } else {
                        out.println("ERROR: Could not join lobby.");
                    }
                } else if (message.startsWith("NICKNAME")) {
                    this.nickname = message.substring(9); // Example: NICKNAME player1
                    ready = true;
                } else {
                    Server.broadcast(message, this);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            disconnect();
        }
    }

    private void disconnect() {
        clients.remove(this);
        Server.broadcast(nickname + " has left the game.", this);
    }

    public void sendMessage(String message) {
        out.println(message);
    }

    public String getNickname() {
        return nickname;
    }

    public boolean isReady() {
        return ready;
    }
}
