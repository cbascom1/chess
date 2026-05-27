package service;


import dataaccess.BadRequestException;
import dataaccess.AlreadyTakenException;

import dataaccess.DataAccess;
import dataaccess.DataAccessException;

import model.AuthData;
import model.UserData;

import java.util.UUID;

public class UserService {

    private final DataAccess dataAccess;

    public UserService(DataAccess dataAccess) {
        this.dataAccess = dataAccess;
    }

    public record RegisterRequest(String username, String password, String email) {}
    public record RegisterResult(String username, String authToken) {}

    public RegisterResult register(RegisterRequest req) throws DataAccessException {
        if (req == null || req.username() == null || req.username().isBlank()
                || req.password() == null || req.password().isBlank() || req.email() == null
                || req.email().isBlank()) {
            throw new BadRequestException("Error: bad request");
        }

        if (dataAccess.getUser(req.username()) != null) {
            throw new AlreadyTakenException("Error: already taken");
        }

        dataAccess.createUser(new UserData(req.username(), req.password(), req.email()));
        String token = UUID.randomUUID().toString();

        dataAccess.createAuth(new AuthData(token, req.username()));
        return new RegisterResult(req.username(), token);
    }
}