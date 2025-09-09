package com.codxp.tokens;

import io.javalin.Javalin;
import io.javalin.http.HttpStatus;
import io.javalin.http.Context;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.*;

public class TokenServer {
    private static String requireUser(Context ctx) {
        String auth = ctx.header("Authorization");
        if (auth == null || !auth.startsWith("Bearer ")) {
            return null;
        }
        String token = auth.substring(7);
        return UserService.verifyToken(token);
    }

    public static void main(String[] args) {
        ObjectMapper mapper = new ObjectMapper();
        Javalin app = Javalin.create(config -> {
            config.plugins.enableCors(cors -> cors.add(it -> it.anyHost()));
        });

        app.post("/register", ctx -> {
            Map<String, String> creds = mapper.readValue(ctx.body(), new TypeReference<>() {});
            String username = creds.get("username");
            String password = creds.get("password");
            if (username == null || password == null) {
                ctx.status(HttpStatus.BAD_REQUEST);
                return;
            }
            boolean ok = UserService.register(username, password);
            if (ok) {
                ctx.status(HttpStatus.CREATED);
            } else {
                ctx.status(HttpStatus.CONFLICT);
            }
        });

        app.post("/login", ctx -> {
            Map<String, String> creds = mapper.readValue(ctx.body(), new TypeReference<>() {});
            String username = creds.get("username");
            String password = creds.get("password");
            if (username != null && password != null && UserService.validate(username, password)) {
                String token = UserService.issueToken(username);
                ctx.json(Map.of("token", token));
            } else {
                ctx.status(HttpStatus.UNAUTHORIZED);
            }
        });

        app.get("/tokens", ctx -> {
            String username = requireUser(ctx);
            if (username == null) {
                ctx.status(HttpStatus.UNAUTHORIZED);
                return;
            }
            Map<TokenCategory, List<Integer>> data = TokenLib.readAllTokens(UserService.getTokensFile(), username);
            Map<String, List<Integer>> out = new LinkedHashMap<>();
            data.forEach((k, v) -> out.put(k.key(), v));
            ctx.json(out);
        });

        app.put("/tokens", ctx -> {
            String username = requireUser(ctx);
            if (username == null) {
                ctx.status(HttpStatus.UNAUTHORIZED);
                return;
            }
            Map<String, List<Integer>> in = mapper.readValue(ctx.body(), new TypeReference<>() {});
            Map<TokenCategory, List<Integer>> data = new EnumMap<>(TokenCategory.class);
            in.forEach((k, v) -> {
                TokenCategory cat = TokenCategory.valueOf(k.toUpperCase());
                data.put(cat, v);
            });
            TokenLib.writeAllTokens(UserService.getTokensFile(), username, data);
            ctx.status(HttpStatus.NO_CONTENT);
        });

        app.get("/totals", ctx -> {
            String username = requireUser(ctx);
            if (username == null) {
                ctx.status(HttpStatus.UNAUTHORIZED);
                return;
            }
            Map<TokenCategory, List<Integer>> data = TokenLib.readAllTokens(UserService.getTokensFile(), username);
            Map<String, Object> out = new LinkedHashMap<>();
            int grand = 0;
            for (TokenCategory cat : TokenCategory.values()) {
                List<Integer> tokens = data.get(cat);
                var totals = TokenLib.computeTotals(tokens);
                Map<String, Object> stats = new LinkedHashMap<>();
                stats.put("minutes", totals.getKey());
                stats.put("hours", totals.getValue());
                out.put(cat.key(), stats);
                grand += totals.getKey();
            }
            Map<String, Object> grandStats = new LinkedHashMap<>();
            grandStats.put("minutes", grand);
            grandStats.put("hours", grand / 60.0);
            out.put("grand", grandStats);
            ctx.json(out);
        });

        app.start(7001);
    }
}
