package ru.itis.drawandguess.server;

import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Server {
    private final int port;
    private final Map<String, PrintWriter> clients = new ConcurrentHashMap<>();
    private final GameState gameState = new GameState();

    public Server(int port) {
        this.port = port;
    }

    public GameState getGameState() {
        return gameState;
    }

    public void addClient(String clientId, PrintWriter writer) {
        clients.put(clientId, writer);
    }

    public void removeClient(String clientId) {
        clients.remove(clientId);
    }

    public void broadcast(String message, String excludeClientId) {
        for (Map.Entry<String, PrintWriter> client : clients.entrySet()) {
            if (!client.getKey().equals(excludeClientId)) {
                client.getValue().println(message);
            }
        }
    }

    public void startGame() {
        // Выбираем первого ведущего
        String leaderId = gameState.getNextLeader();
        gameState.setLeader(leaderId);

        // Устанавливаем загаданное слово (пока фиксированное, можно позже сделать выбор случайным)
        gameState.setCurrentWord("Sun");

        broadcast("GAME_STARTED: Ведущий - " + gameState.getPlayers().get(leaderId), null);
        clients.get(leaderId).println("YOU_ARE_LEADER: Ваше слово - " + gameState.getCurrentWord());
    }

    public void start() {
        try (var serverSocket = new ServerSocket(port)) {
            System.out.println("Сервер запущен на порту: " + port);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Клиент подключен: " + clientSocket.getInetAddress());
                new Thread(new ClientHandler(clientSocket, this)).start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        Server server = new Server(8080);
        server.start();
    }
}
