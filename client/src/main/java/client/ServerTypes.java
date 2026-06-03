package client;

import java.util.Collection;

public class ServerTypes {
    public record RegisterRequest(String username, String password, String email) {}
    public record RegisterResult(String username, String authToken) {}

    public record LoginRequest(String username, String password) {}
    public record LoginResult(String username, String authToken) {}

    public record CreateGameRequest(String gameName) {}
    public record CreateGameResult(int gameID) {}

    public record JoinGameRequest(String playerColor, Integer gameID) {}

    public record GameListItem(int gameID, String whiteUsername,
                               String blackUsername, String gameName) {}
    public record ListGamesResult(Collection<GameListItem> games) {}


}

