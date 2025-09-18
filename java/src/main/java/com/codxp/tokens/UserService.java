package com.codxp.tokens;

import com.mongodb.ErrorCategory;
import com.mongodb.MongoWriteException;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.model.Updates;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.bson.Document;
import org.mindrot.jbcrypt.BCrypt;

import java.security.Key;
import java.time.Duration;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Simple user management helper providing registration, authentication
 * and JWT token handling backed by MongoDB.
 */
public class UserService {
    private static final Key KEY = Keys.secretKeyFor(SignatureAlgorithm.HS256);
    private static final Duration TOKEN_TTL = Duration.ofHours(1);

    private UserService() {
    }

    private static MongoCollection<Document> users() {
        return MongoService.usersCollection();
    }

    private static Document emptyTokensDocument() {
        Document tokens = new Document();
        for (TokenCategory cat : TokenCategory.values()) {
            tokens.append(cat.key(), Arrays.asList(0, 0, 0, 0));
        }
        return tokens;
    }

    public static boolean register(String username, String password) {
        MongoCollection<Document> users = users();
        if (users.find(Filters.eq("username", username)).first() != null) {
            return false;
        }
        String hash = BCrypt.hashpw(password, BCrypt.gensalt());
        Document doc = new Document("username", username)
                .append("password_hash", hash)
                .append("tokens", emptyTokensDocument())
                .append("cod_username", "")
                .append("prestige", "")
                .append("level", 1);
        try {
            users.insertOne(doc);
            return true;
        } catch (MongoWriteException e) {
            if (e.getError() != null && e.getError().getCategory() == ErrorCategory.DUPLICATE_KEY) {
                return false;
            }
            throw e;
        }
    }

    public static boolean validate(String username, String password) {
        Document doc = users().find(Filters.eq("username", username)).first();
        if (doc == null) {
            return false;
        }
        String hash = doc.getString("password_hash");
        if (hash == null || hash.isBlank()) {
            return false;
        }
        return BCrypt.checkpw(password, hash);
    }

    public static String issueToken(String username) {
        Date expiry = Date.from(java.time.Instant.now().plus(TOKEN_TTL));
        return Jwts.builder()
                .setSubject(username)
                .setExpiration(expiry)
                .signWith(KEY)
                .compact();
    }

    public static String verifyToken(String token) {
        try {
            return Jwts.parserBuilder().setSigningKey(KEY).build()
                    .parseClaimsJws(token).getBody().getSubject();
        } catch (Exception e) {
            return null;
        }
    }

    /** Read profile information for the given user. */
    public static Map<String, Object> getProfile(String username) {
        Document doc = users().find(Filters.eq("username", username)).first();
        Map<String, Object> profile = new LinkedHashMap<>();
        if (doc == null) {
            profile.put("cod_username", "");
            profile.put("prestige", "");
            profile.put("level", 1);
            return profile;
        }
        profile.put("cod_username", doc.getOrDefault("cod_username", ""));
        profile.put("prestige", doc.getOrDefault("prestige", ""));
        profile.put("level", clampLevel(doc.get("level")));
        return profile;
    }

    private static int clampLevel(Object levelObj) {
        int level = 1;
        if (levelObj instanceof Number) {
            level = ((Number) levelObj).intValue();
        } else if (levelObj instanceof String) {
            try {
                level = Integer.parseInt((String) levelObj);
            } catch (NumberFormatException ignored) {
                level = 1;
            }
        }
        if (level < 1) {
            return 1;
        }
        return Math.min(level, 1000);
    }

    /** Update profile information for the given user. */
    public static void updateProfile(String username, String codUsername, String prestige, int level) {
        MongoCollection<Document> users = users();
        UpdateOptions options = new UpdateOptions().upsert(true);
        users.updateOne(
                Filters.eq("username", username),
                Updates.combine(
                        Updates.set("cod_username", codUsername),
                        Updates.set("prestige", prestige),
                        Updates.set("level", clampLevel(level)),
                        Updates.setOnInsert("password_hash", ""),
                        Updates.setOnInsert("tokens", emptyTokensDocument())
                ),
                options
        );
    }
}
