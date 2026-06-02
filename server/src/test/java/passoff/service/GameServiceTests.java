package service;

import dataaccess.AlreadyTakenException;
import dataaccess.BadRequestException;
import dataaccess.UnauthorizedException;

import dataaccess.DataAccess;
import dataaccess.MemoryDataAccess;

import model.AuthData;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class GameServiceTests {

    private DataAccess dataAccess;
    private GameService service;
    private String validToken;

    @BeforeEach
    void setup() throws Exception {
        dataAccess = new MemoryDataAccess();
        service = new GameService(dataAccess);
        validToken = "valid-token-for-testing";
        dataAccess.createAuth(new AuthData(validToken, "Batman"));
    }

    @Test
    void listGamesSuccess() throws Exception {
        service.createGame(validToken, new GameService.CreateGameRequest("game1"));
        service.createGame(validToken, new GameService.CreateGameRequest("game2"));

        var result = service.listGames(validToken);

        assertEquals(2, result.games().size());
    }

    @Test
    void listGamesBadTokenThrows() {
        assertThrows(UnauthorizedException.class, () ->
                service.listGames("bad-token"));
    }

    @Test
    void createGameSuccess() throws Exception {
        var result = service.createGame(validToken,
                new GameService.CreateGameRequest("my-game"));

        assertNotNull(result);
        assertTrue(result.gameID() > 0);
    }

    @Test
    void createGameBlankNameThrows() {
        assertThrows(BadRequestException.class, () ->
                service.createGame(validToken,
                        new GameService.CreateGameRequest("")));
    }

    @Test
    void joinGameSuccess() throws Exception {
        var created = service.createGame(validToken,
                new GameService.CreateGameRequest("game1"));

        service.joinGame(validToken,
                new GameService.JoinGameRequest("WHITE", created.gameID()));

        var listed = service.listGames(validToken);
        var item = listed.games().iterator().next();
        assertEquals("Batman", item.whiteUsername());
    }

    @Test
    void joinGameColorTakenThrows() throws Exception {
        var created = service.createGame(validToken,
                new GameService.CreateGameRequest("game1"));
        service.joinGame(validToken,
                new GameService.JoinGameRequest("WHITE", created.gameID()));

        dataAccess.createAuth(new AuthData("token2", "other-user"));

        assertThrows(AlreadyTakenException.class, () ->
                service.joinGame("token2",
                        new GameService.JoinGameRequest("WHITE", created.gameID())));
    }
}
