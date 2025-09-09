package com.codxp.tokens;

import io.javalin.Javalin;
import io.javalin.http.HttpStatus;
import io.javalin.http.Context;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.exceptions.JWTVerificationException;

import java.util.*;

public class TokenServer {
    private static final Algorithm ALG = Algorithm.HMAC256("secret-key");

    private static String requireUser(Context ctx) {
        String auth = ctx.header("Authorization");
        if (auth == null || !auth.startsWith("Bearer ")) {
            ctx.status(HttpStatus.UNAUTHORIZED);
            return null;
        }
        String token = auth.substring(7);
        try {
            DecodedJWT jwt = JWT.require(ALG).build().verify(token);
            return jwt.getSubject();
        } catch (JWTVerificationException e) {
            ctx.status(HttpStatus.UNAUTHORIZED);
            return null;
        }
    }

    public static void main(String[] args) {
        ObjectMapper mapper = new ObjectMapper();
        UserService users = new UserService();

        Javalin app = Javalin.create(config -> {
            config.plugins.enableCors(cors -> cors.add(it -> it.anyHost()));
        });

        app.post("/register", ctx -> {
            Map<String, String> req = mapper.readValue(ctx.body(), new TypeReference<>() {});
            String user = req.get("username");
            String pass = req.get("password");
            if (user == null || pass == null) {
                ctx.status(HttpStatus.BAD_REQUEST);
                return;
            }
            if (users.register(user, pass)) {
                ctx.status(HttpStatus.CREATED);
            } else {
                ctx.status(HttpStatus.CONFLICT);
            }
        });

        app.post("/login", ctx -> {
            Map<String, String> req = mapper.readValue(ctx.body(), new TypeReference<>() {});
            String user = req.get("username");
            String pass = req.get("password");
            if (user == null || pass == null) {
                ctx.status(HttpStatus.BAD_REQUEST);
                return;
            }
            if (users.authenticate(user, pass)) {
                String token = JWT.create().withSubject(user).sign(ALG);
                ctx.json(Collections.singletonMap("token", token));
            } else {
                ctx.status(HttpStatus.UNAUTHORIZED);
            }
        });

        app.get("/tokens", ctx -> {
            String user = requireUser(ctx);
            if (user == null) return;
            String file = users.tokensFileFor(user);
            Map<TokenCategory, List<Integer>> data = TokenLib.readAllTokens(file);
            Map<String, List<Integer>> out = new LinkedHashMap<>();
            data.forEach((k, v) -> out.put(k.key(), v));
            ctx.json(out);
        });

        app.put("/tokens", ctx -> {
            String user = requireUser(ctx);
            if (user == null) return;
            String file = users.tokensFileFor(user);
            Map<String, List<Integer>> in = mapper.readValue(ctx.body(), new TypeReference<>() {});
            Map<TokenCategory, List<Integer>> data = new EnumMap<>(TokenCategory.class);
            in.forEach((k, v) -> {
                TokenCategory cat = TokenCategory.valueOf(k.toUpperCase());
                data.put(cat, v);
            });
            TokenLib.writeAllTokens(file, data);
            ctx.status(HttpStatus.NO_CONTENT);
        });

        app.get("/totals", ctx -> {
            String user = requireUser(ctx);
            if (user == null) return;
            String file = users.tokensFileFor(user);
            Map<TokenCategory, List<Integer>> data = TokenLib.readAllTokens(file);
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
