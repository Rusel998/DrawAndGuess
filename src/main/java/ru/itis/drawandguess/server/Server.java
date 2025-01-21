package ru.itis.drawandguess.server;

import java.io.*;
import java.net.*;
import java.util.*;

public class Server {
    private static final int PORT = 12345;
    private static List<ClientHandler> clients = new ArrayList<>();
    private static List<String> words = Arrays.asList("apple", "banana", "cat", "dog", "elephant");
    private static ClientHandler currentDrawer = null;
    private static String currentWord = null;

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server started on port " + PORT);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("New client connected: " + clientSocket.getInetAddress());

                ClientHandler clientHandler = new ClientHandler(clientSocket, clients);
                clients.add(clientHandler);
                new Thread(clientHandler).start();

                if (clients.size() >= 2 && currentDrawer == null) {
                    startGame();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void broadcast(String message) {
        for (ClientHandler client : clients) {
            client.sendMessage(message);
        }
    }

    public static void sendPlayerList() {
        StringBuilder playerList = new StringBuilder("Connected players: ");
        for (ClientHandler client : clients) {
            playerList.append(client.getNickname()).append(", ");
        }
        broadcast(playerList.toString());
    }

    private static void startGame() {
        Random random = new Random();
        currentDrawer = clients.get(random.nextInt(clients.size()));
        currentWord = words.get(random.nextInt(words.size()));

        for (ClientHandler client : clients) {
            if (client == currentDrawer) {
                client.sendMessage("You are drawing. Your word is: " + currentWord);
            } else {
                client.sendMessage("Game started! Try to guess the word.");
            }
        }

        broadcast("Game started! " + currentDrawer.getNickname() + " is drawing.");
        System.out.println("Drawer: " + currentDrawer.getNickname() + ", Word: " + currentWord);
    }

    public static void handleDrawMessage(String message, ClientHandler sender) {
        for (ClientHandler client : clients) {
            if (client != sender) {
                client.sendMessage(message);
            }
        }
    }

    public static boolean isDrawer(ClientHandler client) {
        return client == currentDrawer;
    }

    public static void nextRound() {
        Random random = new Random();
        ClientHandler previousDrawer = currentDrawer;

        while (true) {
            currentDrawer = clients.get(random.nextInt(clients.size()));
            if (currentDrawer != previousDrawer) {
                break;
            }
        }

        currentWord = words.get(random.nextInt(words.size()));

        for (ClientHandler client : clients) {
            if (client == currentDrawer) {
                client.sendMessage("You are drawing. Your word is: " + currentWord);
            } else {
                client.sendMessage("New round started! Try to guess the word.");
            }
        }

        broadcast("New round started! " + currentDrawer.getNickname() + " is drawing.");
        System.out.println("Drawer: " + currentDrawer.getNickname() + ", Word: " + currentWord);
    }
}

class ClientHandler implements Runnable {
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private String nickname;
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
                out.println("Enter your nickname: ");
                nickname = in.readLine();
                if (isNicknameUnique(nickname)) {
                    break;
                } else {
                    out.println("Nickname already in use. Please enter a different one.");
                }
            }

            System.out.println(nickname + " joined the game.");
            Server.broadcast(nickname + " has joined the game.");
            Server.sendPlayerList();

            String message;
            while ((message = in.readLine()) != null) {
                if (message.startsWith("DRAW:")) {
                    if (Server.isDrawer(this)) {
                        Server.handleDrawMessage(message, this);
                    } else {
                        sendMessage("You are not the drawer!");
                    }
                } else {
                    System.out.println(nickname + ": " + message);
                    Server.broadcast(nickname + ": " + message);
                }
            }
        } catch (IOException e) {
            System.out.println("Connection with " + nickname + " lost.");
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            clients.remove(this);
            Server.broadcast(nickname + " has left the game.");
            Server.sendPlayerList();
        }
    }

    public void sendMessage(String message) {
        out.println(message);
    }

    public String getNickname() {
        return nickname;
    }

    private boolean isNicknameUnique(String nickname) {
        for (ClientHandler client : clients) {
            if (client != this && client.getNickname() != null && client.getNickname().equalsIgnoreCase(nickname)) {
                return false;
            }
        }
        return true;
    }
}
