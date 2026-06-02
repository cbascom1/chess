package dataaccess;

import chess.ChessGame;
import model.AuthData;
import model.GameData;
import model.UserData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

public class SqlDataAccessTests {

    private SqlDataAccess dataAccess;

    @BeforeEach
    void setUp() throws DataAccessException {
        dataAccess = new SqlDataAccess();
        dataAccess.clear();
    }

    @Test
    @DisplayName("clear removes all data")
    void clearSucceeds() throws DataAccessException {
        dataAccess.createUser(new UserData("alice", "password123", "alice@byu.edu"));
        dataAccess.createAuth(new AuthData("token-abc", "alice"));
        dataAccess.createGame("Game One");

        dataAccess.clear();

        assertNull(dataAccess.getUser("alice"));
        assertNull(dataAccess.getAuth("token-abc"));
        assertTrue(dataAccess.listGames().isEmpty());
    }

    @Test
    @DisplayName("createUser inserts a new user")
    void createUserSucceeds() throws DataAccessException {
        var user = new UserData("alice", "password123", "alice@byu.edu");
        assertDoesNotThrow(() -> dataAccess.createUser(user));

        var fetched = dataAccess.getUser("alice");
        assertNotNull(fetched);
        assertEquals("alice", fetched.username());
        assertEquals("alice@byu.edu", fetched.email());
    }

    @Test
    @DisplayName("createUser fails on duplicate username")
    void createUserDuplicateFails() throws DataAccessException {
        var user = new UserData("alice", "password123", "alice@byu.edu");
        dataAccess.createUser(user);

        assertThrows(DataAccessException.class, () -> dataAccess.createUser(user));
    }

    @Test
    @DisplayName("getUser returns existing user with hashed password")
    void getUserSucceeds() throws DataAccessException {
        dataAccess.createUser(new UserData("alice", "password123", "alice@byu.edu"));

        var fetched = dataAccess.getUser("alice");
        assertNotNull(fetched);
        assertEquals("alice", fetched.username());
        assertNotEquals("password123", fetched.password(),
                "stored password should be hashed, not plaintext");
    }

    @Test
    @DisplayName("getUser returns null for missing user")
    void getUserMissingReturnsNull() throws DataAccessException {
        assertNull(dataAccess.getUser("nonexistent"));
    }

    @Test
    @DisplayName("createGame inserts and returns a positive gameID")
    void createGameSucceeds() throws DataAccessException {
        int gameID = dataAccess.createGame("Opening Match");

        assertTrue(gameID > 0);
        var fetched = dataAccess.getGame(gameID);
        assertNotNull(fetched);
        assertEquals("Opening Match", fetched.gameName());
        assertNull(fetched.whiteUsername());
        assertNull(fetched.blackUsername());
        assertNotNull(fetched.game());
    }

    @Test
    @DisplayName("createGame fails when gameName is null")
    void createGameNullNameFails() {
        assertThrows(DataAccessException.class, () -> dataAccess.createGame(null));
    }

    @Test
    @DisplayName("getGame returns the matching game")
    void getGameSucceeds() throws DataAccessException {
        int gameID = dataAccess.createGame("Test Game");

        var fetched = dataAccess.getGame(gameID);
        assertNotNull(fetched);
        assertEquals(gameID, fetched.gameID());
        assertEquals("Test Game", fetched.gameName());
    }

    @Test
    @DisplayName("getGame returns null for missing gameID")
    void getGameMissingReturnsNull() throws DataAccessException {
        assertNull(dataAccess.getGame(99999));
    }

    @Test
    @DisplayName("listGames returns all created games")
    void listGamesSucceeds() throws DataAccessException {
        dataAccess.createGame("Game One");
        dataAccess.createGame("Game Two");
        dataAccess.createGame("Game Three");

        Collection<GameData> games = dataAccess.listGames();
        assertEquals(3, games.size());
    }

    @Test
    @DisplayName("listGames returns empty collection when no games exist")
    void listGamesEmpty() throws DataAccessException {
        Collection<GameData> games = dataAccess.listGames();
        assertNotNull(games);
        assertTrue(games.isEmpty());
    }

    @Test
    @DisplayName("updateGame modifies player slots")
    void updateGameSucceeds() throws DataAccessException {
        int gameID = dataAccess.createGame("Game One");
        var original = dataAccess.getGame(gameID);

        var updated = new GameData(gameID, "alice", "bob",
                original.gameName(), original.game());
        dataAccess.updateGame(updated);

        var fetched = dataAccess.getGame(gameID);
        assertEquals("alice", fetched.whiteUsername());
        assertEquals("bob", fetched.blackUsername());
    }

    @Test
    @DisplayName("updateGame on nonexistent gameID does not insert a row")
    void updateGameMissingDoesNothing() throws DataAccessException {
        var phantom = new GameData(99999, "alice", "bob", "Ghost Game", new ChessGame());
        dataAccess.updateGame(phantom);

        assertNull(dataAccess.getGame(99999));
    }

    @Test
    @DisplayName("createAuth inserts an auth token")
    void createAuthSucceeds() throws DataAccessException {
        dataAccess.createUser(new UserData("alice", "pw", "alice@byu.edu"));
        var auth = new AuthData("token-abc", "alice");

        assertDoesNotThrow(() -> dataAccess.createAuth(auth));
        assertNotNull(dataAccess.getAuth("token-abc"));
    }

    @Test
    @DisplayName("createAuth fails on duplicate token")
    void createAuthDuplicateFails() throws DataAccessException {
        dataAccess.createUser(new UserData("alice", "pw", "alice@byu.edu"));
        var auth = new AuthData("token-abc", "alice");
        dataAccess.createAuth(auth);

        assertThrows(DataAccessException.class, () -> dataAccess.createAuth(auth));
    }

    @Test
    @DisplayName("getAuth returns existing auth data")
    void getAuthSucceeds() throws DataAccessException {
        dataAccess.createUser(new UserData("alice", "pw", "alice@byu.edu"));
        dataAccess.createAuth(new AuthData("token-abc", "alice"));

        var fetched = dataAccess.getAuth("token-abc");
        assertNotNull(fetched);
        assertEquals("token-abc", fetched.authToken());
        assertEquals("alice", fetched.username());
    }

    @Test
    @DisplayName("getAuth returns null for missing token")
    void getAuthMissingReturnsNull() throws DataAccessException {
        assertNull(dataAccess.getAuth("not-a-real-token"));
    }

    @Test
    @DisplayName("deleteAuth removes the token")
    void deleteAuthSucceeds() throws DataAccessException {
        dataAccess.createUser(new UserData("alice", "pw", "alice@byu.edu"));
        dataAccess.createAuth(new AuthData("token-abc", "alice"));

        dataAccess.deleteAuth("token-abc");

        assertNull(dataAccess.getAuth("token-abc"));
    }

    @Test
    @DisplayName("deleteAuth on missing token does not throw")
    void deleteAuthMissingDoesNotThrow() {
        assertDoesNotThrow(() -> dataAccess.deleteAuth("not-a-real-token"));
    }
}