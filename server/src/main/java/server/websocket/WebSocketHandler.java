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

    private void makeMove(WsContext ctx, MakeMoveCommand command) {
    }

    private void leave(WsContext ctx, UserGameCommand command) {
    }

    private void resign(WsContext ctx, UserGameCommand command) {
    }
}
