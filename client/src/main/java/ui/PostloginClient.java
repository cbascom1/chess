package ui;

import client.ResponseException;
import client.ServerFacade;
import java.util.Arrays;

public class PostloginClient {

    private final ServerFacade facade;
    private final String authToken;

    public PostloginClient(String serverUrl, String authToken) {
        this.facade = new ServerFacade(serverUrl);
        this.authToken = authToken;
    }

    public String eval(String input) {
        var tokens = input.trim().split("\\s+");
        var cmd = (tokens.length > 0) ? tokens[0].toLowerCase() : "help";
        var params = Arrays.copyOfRange(tokens, 1, tokens.length);
        return switch (cmd) {
            case "quit" -> "quit";
            case "logout" -> logout();
            case "create" -> createGame(params);
            case "list" -> listGames();
            case "play" -> playGame(params);
            case "observe" -> observeGame(params);
            default -> help();
        };
    }

    private String help() {
        return """
                create <NAME> - create a new game
                list - list all games
                play <GAME_NUMBER> <WHITE|BLACK> - join a game as a player
                observe <GAME_NUMBER> - watch a game
                logout - log out
                quit - exit the program
                help - show this message""";
    }

    private String createGame(String[] params) {
        return "create not implemented yet";
    }

    private String listGames() {
        return "list not implemented yet";
    }

    private String playGame(String[] params) {
        return "play not implemented yet";
    }

    private String observeGame(String[] params) {
        return "observe not implemented yet";
    }

    public String logout() {
        try {
            facade.logout(authToken);
            return "logout";
        } catch (ResponseException e) {
            return "logout";
        }
    }

}
