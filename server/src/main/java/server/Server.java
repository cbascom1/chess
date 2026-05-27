package server;


import io.javalin.*;

import dataaccess.UnauthorizedException;
import dataaccess.AlreadyTakenException;
import dataaccess.BadRequestException;

import dataaccess.DataAccess;
import dataaccess.MemoryDataAccess;

import com.google.gson.Gson;

import service.ClearService;
import java.util.Map;

import service.UserService;

import service.GameService;


public class Server {

    private final Javalin javalin;
    private final DataAccess dataAccess = new MemoryDataAccess();
    private final Gson gson = new Gson();


    public Server() {
        javalin = Javalin.create(config -> config.staticFiles.add("web"));

        // Register your endpoints and exception handlers here.

        javalin.delete("/db", ctx -> {
            new ClearService(dataAccess).clear();
            ctx.status(200).result("{}");
        });

        javalin.post("/user", ctx -> {
            var req = gson.fromJson(ctx.body(), UserService.RegisterRequest.class);
            var result = new UserService(dataAccess).register(req);
            ctx.status(200).result(gson.toJson(result));
        });

        javalin.post("/session", ctx -> {
            var req = gson.fromJson(ctx.body(), UserService.LoginRequest.class);
            var result = new UserService(dataAccess).login(req);
            ctx.status(200).result(gson.toJson(result));
        });

        javalin.delete("/session", ctx -> {
            String token = ctx.header("authorization");
            new UserService(dataAccess).logout(token);
            ctx.status(200).result("{}");
        });

        javalin.get("/game", ctx -> {
            String token = ctx.header("authorization");
            var result = new GameService(dataAccess).listGames(token);
            ctx.status(200).result(gson.toJson(result));
        });

        javalin.post("/game", ctx -> {
            String token = ctx.header("authorization");
            var req = gson.fromJson(ctx.body(), GameService.CreateGameRequest.class);
            var result = new GameService(dataAccess).createGame(token, req);
            ctx.status(200).result(gson.toJson(result));
        });

        javalin.put("/game", ctx -> {
            String token = ctx.header("authorization");
            var req = gson.fromJson(ctx.body(), GameService.JoinGameRequest.class);
            new GameService(dataAccess).joinGame(token, req);
            ctx.status(200).result("{}");
        });

        javalin.exception(BadRequestException.class, (e, ctx) ->
                ctx.status(400).result(gson.toJson(Map.of("message", e.getMessage()))));

        javalin.exception(UnauthorizedException.class, (e, ctx) ->
                ctx.status(401).result(gson.toJson(Map.of("message", e.getMessage()))));

        javalin.exception(AlreadyTakenException.class, (e, ctx) ->
                ctx.status(403).result(gson.toJson(Map.of("message", e.getMessage()))));

        javalin.exception(Exception.class, (e, ctx) ->
                ctx.status(500).result(gson.toJson(Map.of("message", "Error: " + e.getMessage()))));


    }

    public int run(int desiredPort) {
        javalin.start(desiredPort);
        return javalin.port();
    }

    public void stop() {
        javalin.stop();
    }
}
