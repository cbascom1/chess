package server.websocket;

import com.google.gson.Gson;
import io.javalin.websocket.WsContext;
import websocket.messages.ServerMessage;

import java.util.concurrent.ConcurrentHashMap;

public class ConnectionManager {
    private final ConcurrentHashMap<Integer, ConcurrentHashMap<String, Connection>> connections =
            new ConcurrentHashMap<>();
    private final Gson gson = new Gson();

    public void add(int gameID, String authToken, WsContext session) {
        connections.computeIfAbsent(gameID, k -> new ConcurrentHashMap<>())
                .put(authToken, new Connection(authToken, session));
    }

    public void remove(int gameID, String authToken) {
        var game = connections.get(gameID);
        if (game != null) {
            game.remove(authToken);
            if (game.isEmpty()) {
                connections.remove(gameID);
            }
        }
    }

    public void remove(WsContext session) {
        for (var game : connections.values()) {
            game.values().removeIf(c -> c.session == session);
        }
    }

    public void send(int gameID, String authToken, ServerMessage message) {
        var game = connections.get(gameID);
        if (game == null) {
            return;
        }
        Connection c = game.get(authToken);
        if (c != null) {
            c.send(gson.toJson(message));
        }
    }

    public void broadcast(int gameID, String excludeAuthToken, ServerMessage message) {
        var game = connections.get(gameID);
        if (game == null) {
            return;
        }
        String json = gson.toJson(message);
        for (Connection c : game.values()) {
            if (!c.authToken.equals(excludeAuthToken)) {
                c.send(json);
            }
        }
    }
}
