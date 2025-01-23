package ru.itis.drawandguess.server;

import java.util.ArrayList;
import java.util.List;

public class Lobby {
    private String code;
    private String password;
    private int maxPlayers;
    private List<ClientHandler> players;

    public Lobby(String code, String password, int maxPlayers, ClientHandler leader) {
        this.code = code;
        this.password = password;
        this.maxPlayers = maxPlayers;
        this.players = new ArrayList<>();
        this.players.add(leader);
    }

    public boolean addPlayer(ClientHandler client) {
        if (players.size() < maxPlayers) {
            players.add(client);
            return true;
        }
        return false;
    }
    public List<ClientHandler> getPlayers() {
        return players;
    }

    public String getCode() {
        return code;
    }

    public int getMaxPlayers() {
        return maxPlayers;
    }
}
