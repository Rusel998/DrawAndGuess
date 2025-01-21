package ru.itis.drawandguess.server;

import java.util.*;

public class GameState {
    private String currentWord; // Загаданное слово
    private String leaderId; // ID ведущего
    private final Map<String, String> players = new HashMap<>(); // ID игрока -> Имя
    private final Map<String, Integer> scores = new HashMap<>(); // ID игрока -> Очки

    public void addPlayer(String playerId, String playerName) {
        players.put(playerId, playerName);
        scores.put(playerId, 0);
    }

    public void removePlayer(String playerId) {
        players.remove(playerId);
        scores.remove(playerId);
    }

    public void setLeader(String leaderId) {
        this.leaderId = leaderId;
    }

    public String getLeader() {
        return leaderId;
    }

    public void setCurrentWord(String word) {
        this.currentWord = word;
    }

    public String getCurrentWord() {
        return currentWord;
    }

    public Map<String, String> getPlayers() {
        return players;
    }

    public Map<String, Integer> getScores() {
        return scores;
    }

    public void incrementScore(String playerId) {
        scores.put(playerId, scores.getOrDefault(playerId, 0) + 1);
    }

    public String getNextLeader() {
        List<String> playerIds = new ArrayList<>(players.keySet());
        int index = playerIds.indexOf(leaderId);
        return playerIds.get((index + 1) % playerIds.size()); // Следующий игрок
    }
}
