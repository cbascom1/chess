package client;

import org.junit.jupiter.api.*;
import server.Server;
import static org.junit.jupiter.api.Assertions.*;


public class ServerFacadeTests {

    private static Server server;
    private static ServerFacade facade;

    @BeforeAll
    public static void init() {
        server = new Server();
        var port = server.run(0);
        System.out.println("Started test HTTP server on " + port);
        facade = new ServerFacade(port);
    }

    @AfterAll
    static void stopServer() {
        server.stop();
    }


    @Test
    public void sampleTest() {
        Assertions.assertTrue(true);
    }

    @BeforeEach
    public void clearDatabase() throws Exception {
        facade.clear();
    }

    @Test
    public void registerPositive() throws Exception {
        var auth = facade.register("player1", "password", "p1@email.com");
        assertTrue(auth.authToken().length() > 10);
        assertEquals("player1", auth.username());
    }

    @Test
    public void registerNegative() throws Exception {
        facade.register("player1", "password", "p1@email.com");
        assertThrows(ResponseException.class,
                () -> facade.register("player1", "password", "p1@email.com"));
    }

    @Test
    public void loginPositive() throws Exception {
        facade.register("player1", "password", "p1@email.com");
        var auth = facade.login("player1", "password");
        assertTrue(auth.authToken().length() > 10);
    }

    @Test
    public void loginNegative() {
        assertThrows(ResponseException.class,
                () -> facade.login("nobody", "wrongpassword"));
    }

    @Test
    public void logoutPositive() throws Exception {
        var auth = facade.register("player1", "password", "p1@email.com");
        assertDoesNotThrow(() -> facade.logout(auth.authToken()));
    }

    @Test
    public void logoutNegative() {
        assertThrows(ResponseException.class,
                () -> facade.logout("not-a-real-token"));
    }

    @Test
    public void createGamePositive() throws Exception {
        var auth = facade.register("player1", "password", "p1@email.com");
        var result = facade.createGame(auth.authToken(), "myGame");
        assertTrue(result.gameID() > 0);
    }

    @Test
    public void createGameNegative() {
        assertThrows(ResponseException.class,
                () -> facade.createGame("bad-token", "myGame"));
    }

    @Test
    public void listGamesPositive() throws Exception {
        var auth = facade.register("player1", "password", "p1@email.com");
        facade.createGame(auth.authToken(), "gameOne");
        facade.createGame(auth.authToken(), "gameTwo");
        var result = facade.listGames(auth.authToken());
        assertEquals(2, result.games().size());
    }

    @Test
    public void listGamesNegative() {
        assertThrows(ResponseException.class,
                () -> facade.listGames("bad-token"));
    }

    @Test
    public void joinGamePositive() throws Exception {
        var auth = facade.register("player1", "password", "p1@email.com");
        var game = facade.createGame(auth.authToken(), "joinable");
        assertDoesNotThrow(() -> facade.joinGame(auth.authToken(), "WHITE", game.gameID()));
    }

    @Test
    public void joinGameNegative() throws Exception {
        var auth = facade.register("player1", "password", "p1@email.com");
        assertThrows(ResponseException.class,
                () -> facade.joinGame(auth.authToken(), "WHITE", 99999));
    }

    @Test
    public void clearPositive() throws Exception {
        var auth = facade.register("player1", "password", "p1@email.com");
        facade.createGame(auth.authToken(), "willBeCleared");
        facade.clear();
        var newAuth = facade.register("player1", "password", "p1@email.com");
        assertNotNull(newAuth.authToken());
    }

}
