package com.codxp.tokens;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public class TokenLib {
    public static final int[] MINUTE_BUCKETS = {15, 30, 45, 60};

    private static Path userFile(String filename, String username) {
        Path base = Paths.get(filename).getParent();
        if (base == null) base = Paths.get(".");
        return base.resolve("data").resolve("users").resolve(username + ".json");
    }

    static void ensureFile(String filename) throws IOException {
        Path p = Paths.get(filename);
        if (!Files.exists(p)) {
            List<String> zeros = Collections.nCopies(12, "0");
            Files.write(p, zeros, StandardCharsets.UTF_8);
        }
    }

    private static List<Integer> parseInts(List<String> lines, int n) {
        List<Integer> out = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            try {
                out.add(Integer.parseInt(lines.get(i).trim()));
            } catch (Exception e) {
                out.add(0);
            }
        }
        return out;
    }

    private static Map<TokenCategory, List<Integer>> readLegacyTokens(String filename) throws IOException {
        ensureFile(filename);
        List<String> raw = Files.readAllLines(Paths.get(filename), StandardCharsets.UTF_8);
        Map<TokenCategory, List<Integer>> data = new EnumMap<>(TokenCategory.class);
        if (raw.size() >= 12) {
            data.put(TokenCategory.REGULAR, parseInts(raw.subList(0, 4), 4));
            data.put(TokenCategory.WEAPON, parseInts(raw.subList(4, 8), 4));
            data.put(TokenCategory.BATTLEPASS, parseInts(raw.subList(8, 12), 4));
        } else {
            data.put(TokenCategory.REGULAR, parseInts(raw, 4));
            data.put(TokenCategory.WEAPON, Arrays.asList(0, 0, 0, 0));
            data.put(TokenCategory.BATTLEPASS, Arrays.asList(0, 0, 0, 0));
        }
        return data;
    }

    private static List<Integer> ensureSize4(List<Integer> vals) {
        List<Integer> list = new ArrayList<>(Arrays.asList(0, 0, 0, 0));
        for (int i = 0; i < Math.min(4, vals.size()); i++) {
            list.set(i, vals.get(i));
        }
        return list;
    }

    private static void writeLegacyTokens(String filename, Map<TokenCategory, List<Integer>> data) throws IOException {
        List<String> out = new ArrayList<>();
        for (TokenCategory cat : TokenCategory.values()) {
            List<Integer> vals = ensureSize4(data.getOrDefault(cat, Arrays.asList(0, 0, 0, 0)));
            for (Integer v : vals) {
                out.add(String.valueOf(v));
            }
        }
        Files.write(Paths.get(filename), out, StandardCharsets.UTF_8);
    }

    public static Map<TokenCategory, List<Integer>> readAllTokens(String filename, String username) throws IOException {
        Path userPath = userFile(filename, username);
        if (Files.exists(userPath)) {
            ObjectMapper mapper = new ObjectMapper();
            try (InputStream in = Files.newInputStream(userPath)) {
                Map<String, Object> obj = mapper.readValue(in, new TypeReference<>() {});
                Map<String, List<Integer>> tokens = (Map<String, List<Integer>>) obj.getOrDefault("tokens", Collections.emptyMap());
                Map<TokenCategory, List<Integer>> data = new EnumMap<>(TokenCategory.class);
                for (TokenCategory cat : TokenCategory.values()) {
                    List<Integer> vals = tokens.getOrDefault(cat.key(), Arrays.asList(0, 0, 0, 0));
                    data.put(cat, ensureSize4(vals));
                }
                return data;
            }
        }
        return readLegacyTokens(filename);
    }

    public static void writeAllTokens(String filename, String username, Map<TokenCategory, List<Integer>> data) throws IOException {
        Path userPath = userFile(filename, username);
        if ("default".equals(username) && !Files.exists(userPath)) {
            writeLegacyTokens(filename, data);
            return;
        }
        Files.createDirectories(userPath.getParent());
        ObjectMapper mapper = new ObjectMapper();
        String hash = "";
        if (Files.exists(userPath)) {
            try (InputStream in = Files.newInputStream(userPath)) {
                Map<String, Object> obj = mapper.readValue(in, new TypeReference<>() {});
                Object ph = obj.get("password_hash");
                if (ph instanceof String) hash = (String) ph;
            }
        }
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("password_hash", hash);
        Map<String, List<Integer>> tokens = new LinkedHashMap<>();
        for (TokenCategory cat : TokenCategory.values()) {
            tokens.put(cat.key(), ensureSize4(data.getOrDefault(cat, Arrays.asList(0, 0, 0, 0))));
        }
        out.put("tokens", tokens);
        try (OutputStream os = Files.newOutputStream(userPath)) {
            mapper.writerWithDefaultPrettyPrinter().writeValue(os, out);
        }
    }

    public static AbstractMap.SimpleEntry<Integer, Double> computeTotals(List<Integer> tokens) {
        int total = 0;
        for (int i = 0; i < 4; i++) {
            int count = tokens.get(i);
            int minutes = MINUTE_BUCKETS[i];
            total += count * minutes;
        }
        double hours = total / 60.0;
        return new AbstractMap.SimpleEntry<>(total, hours);
    }

    public static String buildTotalsReport(Map<TokenCategory, List<Integer>> data) {
        List<String> lines = new ArrayList<>();
        lines.add("=== 2XP Totals Report ===");
        int grand = 0;
        for (TokenCategory cat : TokenCategory.values()) {
            List<Integer> tokens = data.get(cat);
            AbstractMap.SimpleEntry<Integer, Double> p = computeTotals(tokens);
            int catMinutes = p.getKey();
            double catHours = p.getValue();
            String label = cat.displayName();
            lines.add(label + ": " + catMinutes + " minutes (" + String.format(Locale.US, "%.2f", catHours) + " hours)");
            grand += catMinutes;
        }
        lines.add("");
        lines.add("Grand Total: " + grand + " minutes (" + String.format(Locale.US, "%.2f", grand / 60.0) + " hours)");
        return String.join("\n", lines);
    }
}

