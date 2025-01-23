package ru.itis.drawandguess.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.*;

public class Server {
    private static final int PORT = 12345;
    private static Map<String, Lobby> lobbies = new HashMap<>();
    private static List<ClientHandler> clients = new ArrayList<>();

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server started on port " + PORT);

            while (true) {
                ClientHandler clientHandler = new ClientHandler(serverSocket.accept(), clients);
                clients.add(clientHandler);
                new Thread(clientHandler).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static synchronized boolean createLobby(String password, int maxPlayers, ClientHandler leader) {
        String code = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        if (!lobbies.containsKey(password)) {
            lobbies.put(password, new Lobby(code, password, maxPlayers, leader));
            return true;
        }
        return false;
    }

    public static synchronized boolean joinLobby(String password, ClientHandler client) {
        Lobby lobby = lobbies.get(password);
        if (lobby != null && lobby.addPlayer(client)) {
            System.out.println("Player joined lobby with password: " + password);
            return true;
        }
        System.out.println("Failed to join lobby with password: " + password);
        return false;
    }

    public static void broadcast(String message, ClientHandler sender) {
        for (ClientHandler client : clients) {
            if (client != sender) {
                client.sendMessage(message);
            }
        }
    }

    public static synchronized void checkReadyState() {
        for (ClientHandler client : clients) {
            if (!client.isReady()) {
                return; // Если хотя бы один клиент не готов, ждем
            }
        }
        startGame(); // Все клиенты готовы
    }

    private static void startGame() {
        for (Lobby lobby : lobbies.values()) {
            List<ClientHandler> players = lobby.getPlayers();
            if (!players.isEmpty()) {
                for (ClientHandler player : players) {
                    player.sendMessage("GAME_STARTED");
                }
            }
        }
        System.out.println("Game started!");
    }
}
