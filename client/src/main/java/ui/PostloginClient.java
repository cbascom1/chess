package ui;

import chess.ChessBoard;
import chess.ChessGame;
import client.ResponseException;
import client.ServerFacade;
import client.ServerTypes;

import java.util.ArrayList;
import java.util.Arrays;

public class PostloginClient {

    private final ServerFacade facade;
    private final String authToken;
    private java.util.List<ServerTypes.GameListItem> lastGames = new java.util.ArrayList<>();

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
        if (params.length != 1) {
            return "Expected: create <NAME>";
        }
        try {
            facade.createGame(authToken, params[0]);
            return "Created game: " + params[0];
        } catch (ResponseException e) {
            return "Could not create game.";
        }
    }

    public String listGames() {
        try {
            ServerTypes.ListGamesResult result = facade.listGames(authToken);
            this.lastGames = new ArrayList<>(result.games());

            if (lastGames.isEmpty()) {
                return "No games exist yet. Use 'create' to make one.";
            }

            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < lastGames.size(); i++) {
                var game = lastGames.get(i);
                int number = i + 1;
                sb.append(number)
                        .append(". ")
                        .append(game.gameName())
                        .append(" White: ")
                        .append(playerOrEmpty(game.whiteUsername()))
                        .append(" Black: ")
                        .append(playerOrEmpty(game.blackUsername()))
                        .append("\n");
            }
            return sb.toString();
        } catch (ResponseException e) {
            return "Could not list games.";
        }
    }

    private String playerOrEmpty(String username) {
        return username == null ? "open" : username;
    }

    private String playGame(String[] params) {
        if (params.length != 2) {
            return "Usage: play <game number> <white|black>";
        }
        int gameNumber;
        try {
            gameNumber = Integer.parseInt(params[0]);
        } catch (NumberFormatException ex) {
            return "Game number must be a number.";
        }
        String color = params[1].toLowerCase();
        if (!color.equals("white") && !color.equals("black")) {
            return "Usage: play <game number> <white|black>";
        }
        int index = gameNumber - 1;
        if (index < 0 || index >= lastGames.size()) {
            return "No game with that number. Try 'list' first.";
        }
        int gameID = lastGames.get(index).gameID();
        try {
            facade.joinGame(authToken, color.toUpperCase(), gameID);
            ChessBoard board = new ChessBoard();
            board.resetBoard();
            ChessGame.TeamColor perspective = color.equals("white")
                    ? ChessGame.TeamColor.WHITE : ChessGame.TeamColor.BLACK;
            return "\n" + BoardRenderer.render(board, perspective);
        } catch (ResponseException ex) {
            return "Could not join game. That color may be taken.";
        }
    }

    private String observeGame(String[] params) {
        if (params.length != 1) {
            return "Usage: observe <game number>";
        }
        int gameNumber;
        try {
            gameNumber = Integer.parseInt(params[0]);
        } catch (NumberFormatException ex) {
            return "Game number must be a number.";
        }
        int index = gameNumber - 1;
        if (index < 0 || index >= lastGames.size()) {
            return "No game with that number. Try 'list' first.";
        }
        int gameID = lastGames.get(index).gameID();
        ChessBoard board = new ChessBoard();
        board.resetBoard();
        return "\n" + BoardRenderer.render(board, ChessGame.TeamColor.WHITE);
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
