package ru.itis.drawandguess.server;

import java.io.*;
import java.net.*;
import java.util.*;

public class Server {
    private static final int PORT = 12345;
    private static List<ClientHandler> clients = new ArrayList<>();
    private static List<String> words = Arrays.asList("apple", "banana", "cat", "dog", "elephant"); // Example word list
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

    public static synchronized void checkAndStartGame() {
        // Check if all players are ready and there are at least two players
        if (clients.size() >= 2 && clients.stream().allMatch(ClientHandler::isReady) && currentDrawer == null) {
            startGame();
        }
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

        System.out.println("Drawer: " + currentDrawer.getNickname() + ", Word: " + currentWord);
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

        System.out.println("Drawer: " + currentDrawer.getNickname() + ", Word: " + currentWord);
    }
}
