package client;

import com.google.gson.Gson;


import java.io.InputStream;
import java.io.InputStreamReader;

import java.io.OutputStream;
import java.net.HttpURLConnection;

import java.net.URI;
import java.net.URL;

import java.io.Reader;
import java.io.IOException;

public class ServerFacade {
    private final String serverUrl;
    private record ErrorMessage(String message) {}
    private static final Gson gson = new Gson();

    public ServerFacade(String url) {
        this.serverUrl = url;
    }

    public ServerFacade(int port) {
        this.serverUrl = "http://localhost:" + port;
    }

    private <T> T makeRequest(String method, String path, Object body,
                              String authToken, Class<T> responseClass) throws ResponseException {
        try {
            URL url = new URI(serverUrl + path).toURL();
            HttpURLConnection http = (HttpURLConnection) url.openConnection();
            http.setRequestMethod(method);
            http.setDoOutput(true);

            if (authToken != null) {
                http.addRequestProperty("authorization", authToken);
            }
            if (body != null) {
                http.addRequestProperty("Content-Type", "application/json");
                try (OutputStream reqBody = http.getOutputStream()) {
                    reqBody.write(new Gson().toJson(body).getBytes());
                }
            }

            http.connect();

            if (http.getResponseCode() != 200) {
                throw new ResponseException(readError(http));
            }
            if (responseClass == null) {
                return null;
            }
            try (InputStream respBody = http.getInputStream();
                 var reader = new InputStreamReader(respBody)) {
                return new Gson().fromJson(reader, responseClass);
            }
        } catch (ResponseException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ResponseException(ex.getMessage());
        }
    }

    private String readError(HttpURLConnection http) throws IOException {
        int status = http.getResponseCode();
        InputStream errorStream = http.getErrorStream();
        if (errorStream != null) {
            try (Reader reader = new InputStreamReader(errorStream)) {
                ErrorMessage err = gson.fromJson(reader, ErrorMessage.class);
                if (err != null && err.message() != null) {
                    return err.message();
                }
            }
        }
        return "Error: request failed with status " + status;
    }

    public ServerTypes.RegisterResult register(String username, String password, String email)
            throws ResponseException {
        var body = new ServerTypes.RegisterRequest(username, password, email);
        return makeRequest("POST", "/user", body, null, ServerTypes.RegisterResult.class);
    }

    public ServerTypes.LoginResult login(String username, String password)
            throws ResponseException {
        var body = new ServerTypes.LoginRequest(username, password);
        return makeRequest("POST", "/session", body, null, ServerTypes.LoginResult.class);
    }

    public void logout(String authToken) throws ResponseException {
        makeRequest("DELETE", "/session", null, authToken, null);
    }

    public ServerTypes.ListGamesResult listGames(String authToken) throws ResponseException {
        return makeRequest("GET", "/game", null, authToken, ServerTypes.ListGamesResult.class);
    }

    public ServerTypes.CreateGameResult createGame(String authToken, String gameName)
            throws ResponseException {
        var body = new ServerTypes.CreateGameRequest(gameName);
        return makeRequest("POST", "/game", body, authToken, ServerTypes.CreateGameResult.class);
    }

    public void joinGame(String authToken, String playerColor, Integer gameID)
            throws ResponseException {
        var body = new ServerTypes.JoinGameRequest(playerColor, gameID);
        makeRequest("PUT", "/game", body, authToken, null);
    }

    public void clear() throws ResponseException {
        makeRequest("DELETE", "/db", null, null, null);
    }
}
