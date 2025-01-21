package ru.itis.drawandguess.client;



import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;


public class Client {
    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;

    // Параметры подключения
    private final String HOST = "localhost"; // Сервер работает локально
    private final int PORT = 12345;          // Порт для подключения

    public void start() {
        try {
            // Подключаемся к серверу
            socket = new Socket(HOST, PORT);
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());

            System.out.println("Connected to server!");

            // Отправляем тестовое сообщение
            sendMessage("Уебок");

            // Поток для получения сообщений от сервера
            new Thread(() -> {
                try {
                    while (true) {
                        Object response = in.readObject();
                        System.out.println("Server says: " + response);
                    }
                } catch (IOException | ClassNotFoundException e) {
                    System.err.println("Connection closed.");
                }
            }).start();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Метод для отправки сообщений серверу
    public void sendMessage(Object message) {
        try {
            out.writeObject(message);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Точка входа для клиента
    public static void main(String[] args) {
        new Client().start();
    }
}

