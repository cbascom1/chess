package ui;

import chess.ChessGame;
import chess.ChessMove;
import chess.ChessPiece;
import chess.ChessPosition;
import client.ResponseException;
import client.ServerMessageObserver;
import client.WebSocketFacade;
import websocket.messages.ErrorMessage;
import websocket.messages.LoadGameMessage;
import websocket.messages.NotificationMessage;
import websocket.messages.ServerMessage;

import java.util.Arrays;

public class GameplayClient implements ServerMessageObserver {

    private final WebSocketFacade ws;
    private final String authToken;
    private final int gameID;
    private final ChessGame.TeamColor color;
    private ChessGame game;
    private boolean confirmingResign = false;

    public GameplayClient(String serverUrl, String authToken, int gameID,
                          ChessGame.TeamColor color) throws ResponseException {
        this.authToken = authToken;
        this.gameID = gameID;
        this.color = color;
        this.ws = new WebSocketFacade(serverUrl, this);
        ws.connect(authToken, gameID);
    }

    public String eval(String input) {
        String trimmed = input.trim();

        if (confirmingResign) {
            confirmingResign = false;
            if (trimmed.equalsIgnoreCase("yes") || trimmed.equalsIgnoreCase("y")) {
                return doResign();
            }
            return "Resignation cancelled.";
        }

        var tokens = trimmed.split("\\s+");
        var cmd = (tokens.length > 0 && !tokens[0].isEmpty()) ? tokens[0].toLowerCase() : "help";
        var params = Arrays.copyOfRange(tokens, 1, tokens.length);

        return switch (cmd) {
            case "redraw" -> redraw();
            case "leave" -> leave();
            case "move" -> makeMove(params);
            case "resign" -> resign();
            case "highlight" -> "Highlighting is added in the next step.";
            default -> help();
        };
    }

    @Override
    public void notify(ServerMessage message) {
        if (message instanceof LoadGameMessage loadGame) {
            this.game = loadGame.getGame();
            System.out.println("\n" + BoardRenderer.render(game.getBoard(), perspective()));
        } else if (message instanceof NotificationMessage note) {
            System.out.println("\n" + note.getMessage());
        } else if (message instanceof ErrorMessage error) {
            System.out.println("\n" + error.getErrorMessage());
        }
        System.out.print("\n[IN GAME] >>> ");
    }

    private String help() {
        return """
                redraw - redraw the chess board
                move <FROM> <TO> [PROMOTION] - make a move, e.g. 'move e2 e4'
                highlight <SQUARE> - show legal moves for a piece, e.g. 'highlight e2'
                resign - forfeit the game (you stay to watch)
                leave - leave the game and return to the menu
                help - show this message""";
    }

    private String redraw() {
        if (game == null) {
            return "No game loaded yet.";
        }
        return BoardRenderer.render(game.getBoard(), perspective());
    }

    private String leave() {
        try {
            ws.leave(authToken, gameID);
        } catch (ResponseException ignored) {
        }
        return "leave";
    }

    private String makeMove(String[] params) {
        if (params.length < 2) {
            return "Usage: move <from> <to> [promotion]";
        }
        ChessPosition start = parsePosition(params[0]);
        ChessPosition end = parsePosition(params[1]);
        if (start == null || end == null) {
            return "Invalid square. Use a format like 'e2'.";
        }
        ChessPiece.PieceType promotion = null;
        if (params.length >= 3) {
            promotion = parsePromotion(params[2]);
            if (promotion == null) {
                return "Invalid promotion piece. Use q, r, b, or n.";
            }
        }
        try {
            ws.makeMove(authToken, gameID, new ChessMove(start, end, promotion));
            return "";
        } catch (ResponseException e) {
            return "Could not send move: " + e.getMessage();
        }
    }

    private String resign() {
        if (color == null) {
            return "Observers cannot resign.";
        }
        confirmingResign = true;
        return "Are you sure you want to resign? Type 'yes' to confirm.";
    }

    private String doResign() {
        try {
            ws.resign(authToken, gameID);
            return "";
        } catch (ResponseException e) {
            return "Could not resign: " + e.getMessage();
        }
    }

    private ChessGame.TeamColor perspective() {
        return color == null ? ChessGame.TeamColor.WHITE : color;
    }

    private ChessPosition parsePosition(String s) {
        if (s == null || s.length() != 2) {
            return null;
        }
        char file = Character.toLowerCase(s.charAt(0));
        char rank = s.charAt(1);
        if (file < 'a' || file > 'h' || rank < '1' || rank > '8') {
            return null;
        }
        return new ChessPosition(rank - '0', file - 'a' + 1);
    }

    private ChessPiece.PieceType parsePromotion(String s) {
        return switch (s.toLowerCase()) {
            case "q" -> ChessPiece.PieceType.QUEEN;
            case "r" -> ChessPiece.PieceType.ROOK;
            case "b" -> ChessPiece.PieceType.BISHOP;
            case "n" -> ChessPiece.PieceType.KNIGHT;
            default -> null;
        };
    }
}