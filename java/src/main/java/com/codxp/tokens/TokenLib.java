package com.codxp.tokens;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.model.Updates;
import org.bson.Document;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class TokenLib {
    public static final int[] MINUTE_BUCKETS = {15, 30, 45, 60};

    private TokenLib() {
    }

    private static MongoCollection<Document> users() {
        return MongoService.usersCollection();
    }

    private static List<Integer> ensureSize4(List<?> vals) {
        List<Integer> list = new ArrayList<>(Arrays.asList(0, 0, 0, 0));
        if (vals == null) {
            return list;
        }
        int limit = Math.min(4, vals.size());
        for (int i = 0; i < limit; i++) {
            Object raw = vals.get(i);
            int value = 0;
            if (raw instanceof Number) {
                value = ((Number) raw).intValue();
            } else if (raw instanceof String) {
                try {
                    value = Integer.parseInt(((String) raw).trim());
                } catch (NumberFormatException ignored) {
                    value = 0;
                }
            }
            if (value < 0) {
                value = 0;
            }
            list.set(i, value);
        }
        return list;
    }

    private static Map<TokenCategory, List<Integer>> emptyTokens() {
        Map<TokenCategory, List<Integer>> data = new EnumMap<>(TokenCategory.class);
        for (TokenCategory cat : TokenCategory.values()) {
            data.put(cat, Arrays.asList(0, 0, 0, 0));
        }
        return data;
    }

    private static Document toDocument(Map<TokenCategory, List<Integer>> data) {
        Document doc = new Document();
        for (TokenCategory cat : TokenCategory.values()) {
            doc.append(cat.key(), ensureSize4(data.get(cat)));
        }
        return doc;
    }

    public static Map<TokenCategory, List<Integer>> readAllTokens(String username) {
        Document doc = users().find(Filters.eq("username", username)).first();
        Map<TokenCategory, List<Integer>> data = new EnumMap<>(TokenCategory.class);
        if (doc == null) {
            data.putAll(emptyTokens());
            writeAllTokens(username, data);
            return data;
        }
        Object tokensObj = doc.get("tokens");
        Document tokensDoc = tokensObj instanceof Document ? (Document) tokensObj : new Document();
        for (TokenCategory cat : TokenCategory.values()) {
            Object raw = tokensDoc.get(cat.key());
            List<?> rawList = raw instanceof List ? (List<?>) raw : null;
            data.put(cat, ensureSize4(rawList));
        }
        return data;
    }

    public static Map<TokenCategory, List<Integer>> readAllTokens(String filename, String username) {
        return readAllTokens(username);
    }

    public static void writeAllTokens(String username, Map<TokenCategory, List<Integer>> data) {
        Document tokensDoc = toDocument(data);
        UpdateOptions options = new UpdateOptions().upsert(true);
        users().updateOne(
                Filters.eq("username", username),
                Updates.combine(
                        Updates.set("tokens", tokensDoc),
                        Updates.setOnInsert("password_hash", ""),
                        Updates.setOnInsert("cod_username", ""),
                        Updates.setOnInsert("prestige", ""),
                        Updates.setOnInsert("level", 1)
                ),
                options
        );
    }

    public static void writeAllTokens(String filename, String username, Map<TokenCategory, List<Integer>> data) {
        writeAllTokens(username, data);
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
