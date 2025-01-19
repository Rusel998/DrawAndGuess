package ru.itis.drawandguess.server;

import java.io.*;
import java.net.Socket;
import java.util.UUID;

public class ClientHandler implements Runnable {
    private final Socket clientSocket;
    private final Server server;
    private String clientId;

    public ClientHandler(Socket clientSocket, Server server) {
        this.clientSocket = clientSocket;
        this.server = server;
        this.clientId = UUID.randomUUID().toString(); // Уникальный идентификатор клиента
    }

    @Override
    public void run() {
        try (
                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)
        ) {
            server.addClient(clientId, out); // Регистрируем клиента

            String message;
            while ((message = in.readLine()) != null) {
                System.out.println("Сообщение от клиента " + clientId + ": " + message);

                // Обработка сообщения через Protocol
                String response = Protocol.processMessage(clientId, message, server);
                out.println(response);
            }
        } catch (IOException e) {
            System.err.println("Клиент " + clientId + " отключился.");
        } finally {
            server.removeClient(clientId); // Удаляем клиента
            try {
                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
