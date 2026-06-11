package server.websocket;

import io.javalin.websocket.WsContext;

public class Connection {
    public final String authToken;
    public final WsContext session;

    public Connection(String authToken, WsContext session) {
        this.authToken = authToken;
        this.session = session;
    }

    public void send(String message) {
        session.send(message);
    }
}
