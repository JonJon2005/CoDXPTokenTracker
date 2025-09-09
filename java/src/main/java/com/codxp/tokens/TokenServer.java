package com.codxp.tokens;

import io.javalin.Javalin;
import io.javalin.http.HttpStatus;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.nio.file.*;
import java.util.*;

public class TokenServer {
    private static final String FILENAME = resolveTokensFile();
    private static final String USERNAME = "default";

    private static String resolveTokensFile() {
        Path p = Paths.get("tokens.txt");
        if (!Files.exists(p)) {
            p = Paths.get("../tokens.txt");
        }
        return p.toString();
    }

    public static void main(String[] args) {
        ObjectMapper mapper = new ObjectMapper();
        Javalin app = Javalin.create(config -> {
            config.plugins.enableCors(cors -> cors.add(it -> it.anyHost()));
        });

        app.get("/tokens", ctx -> {
            Map<TokenCategory, List<Integer>> data = TokenLib.readAllTokens(FILENAME, USERNAME);
            Map<String, List<Integer>> out = new LinkedHashMap<>();
            data.forEach((k, v) -> out.put(k.key(), v));
            ctx.json(out);
        });

        app.put("/tokens", ctx -> {
            Map<String, List<Integer>> in = mapper.readValue(ctx.body(), new TypeReference<>() {});
            Map<TokenCategory, List<Integer>> data = new EnumMap<>(TokenCategory.class);
            in.forEach((k, v) -> {
                TokenCategory cat = TokenCategory.valueOf(k.toUpperCase());
                data.put(cat, v);
            });
            TokenLib.writeAllTokens(FILENAME, USERNAME, data);
            ctx.status(HttpStatus.NO_CONTENT);
        });

        app.get("/totals", ctx -> {
            Map<TokenCategory, List<Integer>> data = TokenLib.readAllTokens(FILENAME, USERNAME);
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
