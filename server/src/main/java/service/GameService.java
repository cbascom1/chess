package service;

import dataaccess.AlreadyTakenException;
import dataaccess.BadRequestException;

import dataaccess.UnauthorizedException;

import dataaccess.DataAccess;
import dataaccess.DataAccessException;


import model.AuthData;
import model.GameData;

import java.util.ArrayList;
import java.util.Collection;

public class GameService {

    private final DataAccess dataAccess;

    public GameService(DataAccess dataAccess) {
        this.dataAccess = dataAccess;
    }

    public record GameListItem(int gameID, String whiteUsername,
                               String blackUsername, String gameName) {}
    public record ListGamesResult(Collection<GameListItem> games) {}

    public record CreateGameRequest(String gameName) {}
    public record CreateGameResult(int gameID) {}

    public record JoinGameRequest(String playerColor, Integer gameID) {}

    public ListGamesResult listGames(String authToken) throws DataAccessException {
        requireAuth(authToken);
        Collection<GameListItem> items = new ArrayList<>();
        for (GameData game : dataAccess.listGames()) {
            items.add(new GameListItem(game.gameID(), game.whiteUsername(), game.blackUsername(), game.gameName()));
        }

        return new ListGamesResult(items);
    }

    public CreateGameResult createGame(String authToken, CreateGameRequest req)
            throws DataAccessException {
        requireAuth(authToken);
        if (req == null || req.gameName() == null || req.gameName().isBlank()) {
            throw new BadRequestException("Error: bad request");
        }
        int gameID = dataAccess.createGame(req.gameName());
        return new CreateGameResult(gameID);
    }

    public void joinGame(String authToken, JoinGameRequest req) throws DataAccessException {
        AuthData auth = requireAuth(authToken);
        if (req == null || req.gameID() == null || req.playerColor() == null) {
            throw new BadRequestException("Error: bad request");
        }
        GameData game = dataAccess.getGame(req.gameID());
        if (game == null) {
            throw new BadRequestException("Error: bad request");
        }
        String username = auth.username();
        String color = req.playerColor().toUpperCase();

        GameData updated;
        if (color.equals("WHITE")) {
            if (game.whiteUsername() != null) {
                throw new AlreadyTakenException("Error: already taken");
            }
            updated = new GameData(game.gameID(), username, game.blackUsername(),
                    game.gameName(), game.game());
        } else if (color.equals("BLACK")) {
            if (game.blackUsername() != null) {
                throw new AlreadyTakenException("Error: already taken");
            }
            updated = new GameData(game.gameID(), game.whiteUsername(), username,
                    game.gameName(), game.game());
        } else {
            throw new BadRequestException("Error: bad request");
        }
        dataAccess.updateGame(updated);
    }

    private AuthData requireAuth(String authToken) throws DataAccessException {
        if (authToken == null) {
            throw new UnauthorizedException("Error: unauthorized");
        }
        AuthData auth = dataAccess.getAuth(authToken);
        if (auth == null) {
            throw new UnauthorizedException("Error: unauthorized");
        }
        return auth;
    }
}
