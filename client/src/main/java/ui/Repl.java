package ui;


import java.util.Scanner;

public class Repl {

    private final PreloginClient preClient;

    public Repl(String serverUrl) {
        preClient = new PreloginClient(serverUrl);
    }

    public void run() {
        System.out.println("♕ Welcome to 240 Chess. Type 'help' to get started.");
        Scanner scanner = new Scanner(System.in);
        String result = "";
        while (!result.equals("quit")) {
            System.out.print("\n>>> ");
            String line = scanner.nextLine();
            try {
                result = preClient.eval(line);
                if (!result.equals("quit")) {
                    System.out.println(result);
                }
            } catch (Throwable e) {
                System.out.println("Something went wrong. Please try again.");
            }
        }
        System.out.println("Goodbye!");
    }
}