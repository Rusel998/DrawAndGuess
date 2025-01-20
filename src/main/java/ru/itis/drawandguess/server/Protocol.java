//package ru.itis.drawandguess.server;
//
//public class Protocol {
//    public static String processMessage(String clientId, String message, Server server) {
//        GameState gameState = server.getGameState();
//        String[] parts = message.split(":", 2);
//        String command = parts[0].toUpperCase();
//        String data = parts.length > 1 ? parts[1] : "";
//
//        switch (command) {
//            case "CONNECT":
//                gameState.addPlayer(clientId, data);
//                return "CONNECTED: Привет, " + data + "!";
//
//            case "START":
//                server.startGame();
//                return "START_OK";
//
//            case "GUESS":
//                if (data.equalsIgnoreCase(gameState.getCurrentWord())) {
//                    gameState.incrementScore(clientId);
//                    server.broadcast("GUESS_CORRECT: " + gameState.getPlayers().get(clientId) + " угадал слово!", clientId);
//                    return "CORRECT_GUESS: Поздравляем, вы угадали!";
//                } else {
//                    return "INCORRECT_GUESS: Попробуйте еще раз.";
//                }
//
//            case "PASS":
//                return "PASS: Слово принято.";
//
//            default:
//                return "ERROR: Неизвестная команда.";
//        }
//    }
//}
