package server.websocket;

import com.google.gson.Gson;
import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import io.javalin.websocket.WsCloseContext;
import io.javalin.websocket.WsConnectContext;
import io.javalin.websocket.WsContext;
import io.javalin.websocket.WsErrorContext;
import io.javalin.websocket.WsMessageContext;
import model.AuthData;
import model.GameData;
import websocket.commands.MakeMoveCommand;
import websocket.commands.UserGameCommand;
import websocket.messages.ErrorMessage;
import websocket.messages.LoadGameMessage;
import websocket.messages.NotificationMessage;
import chess.ChessGame;
import chess.ChessMove;
import chess.ChessPosition;
import chess.InvalidMoveException;

public class WebSocketHandler {

    private final DataAccess dataAccess;
    private final ConnectionManager connections = new ConnectionManager();
    private final Gson gson = new Gson();

    public WebSocketHandler(DataAccess dataAccess) {
        this.dataAccess = dataAccess;
    }

    public void onConnect(WsConnectContext ctx) {
    }

    public void onMessage(WsMessageContext ctx) {
        String message = ctx.message();
        try {
            UserGameCommand command = gson.fromJson(message, UserGameCommand.class);
            switch (command.getCommandType()) {
                case CONNECT -> connect(ctx, command);
                case MAKE_MOVE -> makeMove(ctx, gson.fromJson(message, MakeMoveCommand.class));
                case LEAVE -> leave(ctx, command);
                case RESIGN -> resign(ctx, command);
            }
        } catch (Exception ex) {
            ctx.send(gson.toJson(new ErrorMessage("Error: " + ex.getMessage())));
        }
    }

    public void onClose(WsCloseContext ctx) {
        connections.remove(ctx);
    }

    public void onError(WsErrorContext ctx) {
        System.err.println("WebSocket error: " + ctx.error());
    }

    private void connect(WsContext ctx, UserGameCommand command) throws DataAccessException {
        String authToken = command.getAuthToken();
        Integer gameID = command.getGameID();

        AuthData auth = (authToken == null) ? null : dataAccess.getAuth(authToken);
        if (auth == null) {
            ctx.send(gson.toJson(new ErrorMessage("Error: unauthorized")));
            return;
        }
        if (gameID == null) {
            ctx.send(gson.toJson(new ErrorMessage("Error: invalid game")));
            return;
        }
        GameData game = dataAccess.getGame(gameID);
        if (game == null) {
            ctx.send(gson.toJson(new ErrorMessage("Error: invalid game")));
            return;
        }

        String username = auth.username();
        connections.add(gameID, authToken, ctx);

        ctx.send(gson.toJson(new LoadGameMessage(game.game())));

        String noteText;
        if (username.equals(game.whiteUsername())) {
            noteText = username + " joined the game as white";
        } else if (username.equals(game.blackUsername())) {
            noteText = username + " joined the game as black";
        } else {
            noteText = username + " is observing the game";
        }
        connections.broadcast(gameID, authToken, new NotificationMessage(noteText));
    }

    private void makeMove(WsContext ctx, MakeMoveCommand command) throws DataAccessException {
        String authToken = command.getAuthToken();
        Integer gameID = command.getGameID();

        AuthData auth = (authToken == null) ? null : dataAccess.getAuth(authToken);
        if (auth == null) {
            sendError(ctx, "Error: unauthorized");
            return;
        }
        GameData gameData = (gameID == null) ? null : dataAccess.getGame(gameID);
        if (gameData == null) {
            sendError(ctx, "Error: invalid game");
            return;
        }

        ChessGame game = gameData.game();
        String username = auth.username();
        ChessGame.TeamColor playerColor = colorOf(username, gameData);

        if (game.isGameOver()) {
            sendError(ctx, "Error: game is already over");
            return;
        }
        if (playerColor == null) {
            sendError(ctx, "Error: observers cannot make moves");
            return;
        }
        if (playerColor != game.getTeamTurn()) {
            sendError(ctx, "Error: it is not your turn");
            return;
        }

        try {
            game.makeMove(command.getMove());
        } catch (InvalidMoveException ex) {
            sendError(ctx, "Error: invalid move");
            return;
        }

        ChessGame.TeamColor opponent = game.getTeamTurn();
        String opponentName = (opponent == ChessGame.TeamColor.WHITE)
                ? gameData.whiteUsername() : gameData.blackUsername();

        boolean checkmate = game.isInCheckmate(opponent);
        boolean stalemate = game.isInStalemate(opponent);
        boolean check = game.isInCheck(opponent);
        if (checkmate || stalemate) {
            game.setGameOver(true);
        }

        dataAccess.updateGame(gameData);

        connections.broadcast(gameID, null, new LoadGameMessage(game));
        connections.broadcast(gameID, authToken,
                new NotificationMessage(describeMove(username, command.getMove())));

        if (checkmate) {
            connections.broadcast(gameID, null,
                    new NotificationMessage(opponentName + " is in checkmate"));
        } else if (stalemate) {
            connections.broadcast(gameID, null,
                    new NotificationMessage("Game is in stalemate"));
        } else if (check) {
            connections.broadcast(gameID, null,
                    new NotificationMessage(opponentName + " is in check"));
        }
    }

    private void leave(WsContext ctx, UserGameCommand command) throws DataAccessException {
        String authToken = command.getAuthToken();
        Integer gameID = command.getGameID();

        AuthData auth = (authToken == null) ? null : dataAccess.getAuth(authToken);
        if (auth == null) {
            sendError(ctx, "Error: unauthorized");
            return;
        }
        GameData gameData = (gameID == null) ? null : dataAccess.getGame(gameID);
        if (gameData == null) {
            sendError(ctx, "Error: invalid game");
            return;
        }

        String username = auth.username();
        ChessGame.TeamColor playerColor = colorOf(username, gameData);

        if (playerColor == ChessGame.TeamColor.WHITE) {
            dataAccess.updateGame(new GameData(gameData.gameID(), null, gameData.blackUsername(), gameData.gameName(), gameData.game()));
        } else if (playerColor == ChessGame.TeamColor.BLACK) {
            dataAccess.updateGame(new GameData(gameData.gameID(), gameData.whiteUsername(), null, gameData.gameName(), gameData.game()));
        }

        connections.remove(gameID, authToken);
        connections.broadcast(gameID, authToken, new NotificationMessage(username + " left the game"));
    }

    private void resign(WsContext ctx, UserGameCommand command) throws DataAccessException{
        String authToken = command.getAuthToken();
        Integer gameID = command.getGameID();

        AuthData auth = (authToken == null) ? null : dataAccess.getAuth(authToken);
        if (auth == null) {
            sendError(ctx, "Error: unauthorized");
            return;
        }
        GameData gameData = (gameID == null) ? null : dataAccess.getGame(gameID);
        if (gameData == null) {
            sendError(ctx, "Error: invalid game");
            return;
        }

        ChessGame game = gameData.game();
        String username = auth.username();
        ChessGame.TeamColor playerColor = colorOf(username, gameData);

        if (playerColor == null) {
            sendError(ctx, "Error: observers cannot resign");
            return;
        }
        if (game.isGameOver()) {
            sendError(ctx, "Error: game is already over");
            return;
        }

        game.setGameOver(true);
        dataAccess.updateGame(gameData);

        connections.broadcast(gameID, null, new NotificationMessage(username + " resigned the game"));
    }
    private ChessGame.TeamColor colorOf(String username, GameData gameData) {
        if (username.equals(gameData.whiteUsername())) {
            return ChessGame.TeamColor.WHITE;
        }
        if (username.equals(gameData.blackUsername())) {
            return ChessGame.TeamColor.BLACK;
        }
        return null;
    }

    private String describeMove(String username, ChessMove move) {
        return username + " moved " + posToString(move.getStartPosition())
                + " to " + posToString(move.getEndPosition());
    }

    private String posToString(ChessPosition pos) {
        char file = (char) ('a' + pos.getColumn() - 1);
        return "" + file + pos.getRow();
    }

    private void sendError(WsContext ctx, String message) {
        ctx.send(gson.toJson(new ErrorMessage(message)));
    }
}
