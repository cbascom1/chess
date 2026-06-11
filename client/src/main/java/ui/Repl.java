package ui;

import java.util.Scanner;
import chess.ChessGame;
import client.ResponseException;

public class Repl {

    private enum State { PRELOGIN, POSTLOGIN, GAMEPLAY }

    private final String serverUrl;
    private final PreloginClient preClient;
    private PostloginClient postClient;
    private GameplayClient gameClient;
    private State state = State.PRELOGIN;

    public Repl(String serverUrl) {
        this.serverUrl = serverUrl;
        preClient = new PreloginClient(serverUrl);
    }

    public void run() {
        System.out.println("♕ Welcome to 240 Chess. Type 'help' to get started.");
        Scanner scanner = new Scanner(System.in);
        String result = "";
        while (!result.equals("quit")) {
            printPrompt();
            String line = scanner.nextLine();
            try {
                if (state == State.PRELOGIN) {
                    result = preClient.eval(line);
                    if (!result.equals("quit")) {
                        System.out.println(result);
                    }
                    if (preClient.getAuthToken() != null) {
                        postClient = new PostloginClient(serverUrl, preClient.getAuthToken());
                        state = State.POSTLOGIN;
                    }
                } else if (state == State.POSTLOGIN) {
                    result = postClient.eval(line);
                    if (result.equals("logout")) {
                        preClient.clearAuth();
                        state = State.PRELOGIN;
                        System.out.println("Logged out.");
                    } else if (result.equals("enterGame")) {
                        gameClient = new GameplayClient(serverUrl, preClient.getAuthToken(),
                                postClient.consumePendingGameID(), postClient.consumePendingColor());
                        state = State.GAMEPLAY;
                    } else if (!result.equals("quit")) {
                        System.out.println(result);
                    }
                } else {
                    result = gameClient.eval(line);
                    if (result.equals("leave")) {
                        state = State.POSTLOGIN;
                        System.out.println("Left the game.");
                    } else if (!result.equals("quit") && !result.isEmpty()) {
                        System.out.println(result);
                    }
                }
            } catch (Throwable e) {
                System.out.println("Something went wrong. Please try again.");
            }
        }
        System.out.println("Goodbye!");
    }

    private void printPrompt() {
        switch (state) {
            case PRELOGIN -> System.out.print("\n[LOGGED OUT] >>> ");
            case POSTLOGIN -> System.out.print("\n[LOGGED IN] >>> ");
            case GAMEPLAY -> System.out.print("\n[IN GAME] >>> ");
        }
    }

}