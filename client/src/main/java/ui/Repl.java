package ui;

import java.util.Scanner;

public class Repl {

    private enum State { PRELOGIN, POSTLOGIN }

    private final String serverUrl;
    private final PreloginClient preClient;
    private PostloginClient postClient;
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
                } else {
                    result = postClient.eval(line);
                    if (result.equals("logout")) {
                        preClient.clearAuth();
                        state = State.PRELOGIN;
                        System.out.println("Logged out.");
                    } else if (!result.equals("quit")) {
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
        if (state == State.PRELOGIN) {
            System.out.print("\n[LOGGED OUT] >>> ");
        } else {
            System.out.print("\n[LOGGED IN] >>> ");
        }
    }

}