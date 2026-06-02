package service;


import dataaccess.BadRequestException;
import dataaccess.AlreadyTakenException;
import dataaccess.UnauthorizedException;

import dataaccess.DataAccess;
import dataaccess.DataAccessException;

import model.AuthData;
import model.UserData;

import java.util.UUID;
import org.mindrot.jbcrypt.BCrypt;

public class UserService {

    private final DataAccess dataAccess;

    public UserService(DataAccess dataAccess) {
        this.dataAccess = dataAccess;
    }

    public record RegisterRequest(String username, String password, String email) {}
    public record RegisterResult(String username, String authToken) {}

    public record LoginRequest(String username, String password) {}
    public record LoginResult(String username, String authToken) {}

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


    public LoginResult login(LoginRequest req) throws DataAccessException {
        if (req == null || req.username() == null || req.password() == null) {
            throw new BadRequestException("Error: bad request");
        }
        UserData user = dataAccess.getUser(req.username());
        if (user == null || !BCrypt.checkpw(req.password(), user.password())) {
            throw new UnauthorizedException("Error: unauthorized");
        }
        String token = UUID.randomUUID().toString();
        dataAccess.createAuth(new AuthData(token, req.username()));
        return new LoginResult(req.username(), token);
    }


    public void logout(String authToken) throws DataAccessException {
        if (authToken == null || dataAccess.getAuth(authToken) == null) {
            throw new UnauthorizedException("Error: unauthorized");
        }
        dataAccess.deleteAuth(authToken);
    }

}