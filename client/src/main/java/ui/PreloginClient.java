package ui;

import client.ServerFacade;
import java.util.Arrays;


public class PreloginClient {
    private final ServerFacade facade;

    public PreloginClient(String serverUrl) {
        this.facade = new ServerFacade(serverUrl);
    }

    public String eval(String input) {
        var tokens = input.toLowerCase().split(" ");
        var cmd = (tokens.length > 0) ? tokens[0] : "help";
        var params = Arrays.copyOfRange(tokens, 1, tokens.length);
        return switch (cmd) {
            case "quit" -> "quit";
            case "login" -> login(params);
            case "register" -> register(params);
            default -> help();
        };
    }


    //not implemented todavia
    private String login(String[] params) {
        return "";
    }

    private String register(String[] params) {
        return "";
    }

    private String help() {
        return "";
    }
}