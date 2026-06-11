package client;

import com.google.gson.Gson;
import chess.ChessMove;
import websocket.commands.MakeMoveCommand;
import websocket.commands.UserGameCommand;
import websocket.messages.ErrorMessage;
import websocket.messages.LoadGameMessage;
import websocket.messages.NotificationMessage;
import websocket.messages.ServerMessage;

import jakarta.websocket.ContainerProvider;
import jakarta.websocket.DeploymentException;
import jakarta.websocket.Endpoint;
import jakarta.websocket.EndpointConfig;
import jakarta.websocket.MessageHandler;
import jakarta.websocket.Session;
import jakarta.websocket.WebSocketContainer;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class WebSocketFacade extends Endpoint {

    private Session session;
    private final ServerMessageObserver observer;
    private final Gson gson = new Gson();

    public WebSocketFacade(String url, ServerMessageObserver observer) throws ResponseException {
        try {
            this.observer = observer;
            String socketUrl = url.replace("http://", "ws://").replace("https://", "wss://");
            URI socketURI = new URI(socketUrl + "/ws");

            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            this.session = container.connectToServer(this, socketURI);

            this.session.addMessageHandler(new MessageHandler.Whole<String>() {
                @Override
                public void onMessage(String message) {
                    handleMessage(message);
                }
            });
        } catch (DeploymentException | IOException | URISyntaxException ex) {
            throw new ResponseException(500, ex.getMessage());
        }
    }

    @Override
    public void onOpen(Session session, EndpointConfig endpointConfig) {
    }

    private void handleMessage(String message) {
        ServerMessage base = gson.fromJson(message, ServerMessage.class);
        switch (base.getServerMessageType()) {
            case LOAD_GAME -> observer.notify(gson.fromJson(message, LoadGameMessage.class));
            case ERROR -> observer.notify(gson.fromJson(message, ErrorMessage.class));
            case NOTIFICATION -> observer.notify(gson.fromJson(message, NotificationMessage.class));
        }
    }

    public void connect(String authToken, Integer gameID) throws ResponseException {
        send(new UserGameCommand(UserGameCommand.CommandType.CONNECT, authToken, gameID));
    }

    public void makeMove(String authToken, Integer gameID, ChessMove move) throws ResponseException {
        send(new MakeMoveCommand(authToken, gameID, move));
    }

    public void leave(String authToken, Integer gameID) throws ResponseException {
        send(new UserGameCommand(UserGameCommand.CommandType.LEAVE, authToken, gameID));
    }

    public void resign(String authToken, Integer gameID) throws ResponseException {
        send(new UserGameCommand(UserGameCommand.CommandType.RESIGN, authToken, gameID));
    }

    private void send(UserGameCommand command) throws ResponseException {
        try {
            this.session.getBasicRemote().sendText(gson.toJson(command));
        } catch (IOException ex) {
            throw new ResponseException(500, ex.getMessage());
        }
    }
}