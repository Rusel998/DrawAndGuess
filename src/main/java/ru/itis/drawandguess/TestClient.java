package ru.itis.drawandguess;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class TestClient {
    private final String host;
    private final int port;
    private String clientId; // Уникальный идентификатор клиента

    public TestClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void start() {
        try (Socket socket = new Socket(host, port);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             Scanner scanner = new Scanner(System.in)) {

            System.out.println("Подключено к серверу на " + host + ":" + port);

            // Подключаемся к серверу
            System.out.print("Введите ваше имя: ");
            String name = scanner.nextLine();
            out.println("CONNECT:" + name);

            Thread readerThread = new Thread(() -> {
                try {
                    String serverMessage;
                    while ((serverMessage = in.readLine()) != null) {
                        System.out.println("Ответ от сервера: " + serverMessage);
                    }
                } catch (IOException e) {
                    System.err.println("Соединение с сервером потеряно.");
                }
            });

            readerThread.start();

            while (true) {
                System.out.println("\nВыберите команду:");
                System.out.println("1. Отправить сообщение (MESSAGE)");
                System.out.println("2. Нарисовать элемент (DRAW)");
                System.out.println("3. Угадать слово (GUESS)");
                System.out.println("4. Выход (exit)");
                System.out.print("Введите номер команды: ");

                String command = scanner.nextLine();
                if (command.equalsIgnoreCase("4") || command.equalsIgnoreCase("exit")) {
                    System.out.println("Отключение...");
                    break;
                }

                switch (command) {
                    case "1":
                        System.out.print("Введите сообщение для чата: ");
                        String chatMessage = scanner.nextLine();
                        out.println("MESSAGE:" + chatMessage);
                        break;

                    case "2":
                        System.out.print("Введите данные для рисования (например, координаты): ");
                        String drawData = scanner.nextLine();
                        out.println("DRAW:" + drawData);
                        break;

                    case "3":
                        System.out.print("Введите ваше предположение: ");
                        String guess = scanner.nextLine();
                        out.println("GUESS:" + guess);
                        break;

                    default:
                        System.out.println("Неизвестная команда. Попробуйте еще раз.");
                        break;
                }
            }

        } catch (IOException e) {
            System.err.println("Ошибка подключения к серверу: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        TestClient client = new TestClient("localhost", 8080); // Хост и порт сервера
        client.start();
    }
}