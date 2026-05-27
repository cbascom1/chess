package service;

import dataaccess.DataAccess;
import dataaccess.MemoryDataAccess;

import model.AuthData;
import model.UserData;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class ClearServiceTests {

    private DataAccess dataAccess;
    private ClearService service;

    @BeforeEach
    void setup() {
        dataAccess = new MemoryDataAccess();
        service = new ClearService(dataAccess);
    }

    @Test
    void clearRemovesAllData() throws Exception {
        dataAccess.createUser(new UserData("Batman", "pw", "a@b.c"));
        dataAccess.createAuth(new AuthData("token-1", "Batman"));
        dataAccess.createGame("game-1");

        service.clear();

        assertNull(dataAccess.getUser("Batman"));
        assertNull(dataAccess.getAuth("token-1"));
        assertEquals(0, dataAccess.listGames().size());
    }
}