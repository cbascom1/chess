package service;

import dataaccess.AlreadyTakenException;
import dataaccess.DataAccess;

import dataaccess.MemoryDataAccess;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class UserServiceTests {

    private DataAccess dataAccess;
    private UserService service;

    @BeforeEach
    void setup() {
        dataAccess = new MemoryDataAccess();
        service = new UserService(dataAccess);
    }

    @Test
    void registerSuccess() throws Exception {
        var result = service.register(
                new UserService.RegisterRequest("Batman", "pw", "a@b.c"));

        assertEquals("Batman", result.username());
        assertNotNull(result.authToken());
        assertFalse(result.authToken().isBlank());
    }

    @Test
    void registerDuplicateThrows() throws Exception {
        service.register(new UserService.RegisterRequest("Batman", "pw", "a@b.c"));

        assertThrows(AlreadyTakenException.class, () ->
                service.register(new UserService.RegisterRequest("Batman", "pw2", "x@y.z")));
    }
}