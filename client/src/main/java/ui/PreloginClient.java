package ui;

import client.ResponseException;
import client.ServerFacade;
import java.util.Arrays;
import client.ServerTypes.LoginResult;
import client.ServerTypes.RegisterResult;



public class PreloginClient {
    private final ServerFacade facade;

    private String authToken;

    public String getAuthToken() {
        return authToken;
    }

    public PreloginClient(String serverUrl) {
        this.facade = new ServerFacade(serverUrl);
    }

    public String eval(String input) {
        var tokens = input.trim().split("\\s+");
        var cmd = (tokens.length > 0) ? tokens[0].toLowerCase() : "help";
        var params = Arrays.copyOfRange(tokens, 1, tokens.length);
        return switch (cmd) {
            case "quit" -> "quit";
            case "login" -> login(params);
            case "register" -> register(params);
            default -> help();
        };
    }

    private String login(String[] params)  {
        if (params.length != 2) {
            return "Expected: login <USERNAME> <PASSWORD>";
        }
        try {
            LoginResult result = facade.login(params[0], params[1]);
            this.authToken = result.authToken();
            return "Logged in as " + result.username() + ".";
        } catch (ResponseException e) {
            return "Login failed. Check your username and password.";
        }
    }

    private String register(String[] params) {
        if (params.length != 3) {
            return "Expected: register <USERNAME> <PASSWORD> <EMAIL>";
        }
        try {
            RegisterResult result = facade.register(params[0], params[1], params[2]);
            this.authToken = result.authToken();
            return "Logged in as " + result.username() + ".";
        } catch (ResponseException e) {
            return "Could not register. That username may already be taken.";
        }
    }

    public void clearAuth() {
        this.authToken = null;
    }

    private String help() {
        return """
            register <USERNAME> <PASSWORD> <EMAIL> - create an account
            login <USERNAME> <PASSWORD> - log in to play
            quit - exit the program
            help - show this message""";
    }
}