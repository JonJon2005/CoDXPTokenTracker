package com.codxp.tokens;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.mindrot.jbcrypt.BCrypt;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.*;
import java.security.Key;
import java.time.*;
import java.util.*;

/**
 * Simple user management helper providing registration, authentication
 * and JWT token handling.
 */
public class UserService {
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final String TOKENS_FILE = resolveTokensFile();
    private static final Key KEY = Keys.secretKeyFor(SignatureAlgorithm.HS256);
    private static final Duration TOKEN_TTL = Duration.ofHours(1);

    private static Path userFile(String username) {
        Path base = Paths.get(TOKENS_FILE).getParent();
        if (base == null) base = Paths.get(".");
        return base.resolve("data").resolve("users").resolve(username + ".json");
    }

    public static String resolveTokensFile() {
        Path p = Paths.get("tokens.txt");
        if (!Files.exists(p)) {
            p = Paths.get("../tokens.txt");
        }
        return p.toString();
    }

    public static boolean register(String username, String password) throws IOException {
        Path userPath = userFile(username);
        if (Files.exists(userPath)) {
            return false;
        }
        Files.createDirectories(userPath.getParent());
        String hash = BCrypt.hashpw(password, BCrypt.gensalt());
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("password_hash", hash);
        Map<String, List<Integer>> tokens = new LinkedHashMap<>();
        for (TokenCategory cat : TokenCategory.values()) {
            tokens.put(cat.key(), Arrays.asList(0, 0, 0, 0));
        }
        data.put("tokens", tokens);
        try (OutputStream out = Files.newOutputStream(userPath)) {
            MAPPER.writerWithDefaultPrettyPrinter().writeValue(out, data);
        }
        return true;
    }

    public static boolean validate(String username, String password) throws IOException {
        Path userPath = userFile(username);
        if (!Files.exists(userPath)) {
            return false;
        }
        try (InputStream in = Files.newInputStream(userPath)) {
            Map<String, Object> obj = MAPPER.readValue(in, new TypeReference<>() {});
            Object hashObj = obj.get("password_hash");
            if (!(hashObj instanceof String)) {
                return false;
            }
            String hash = (String) hashObj;
            return BCrypt.checkpw(password, hash);
        }
    }

    public static boolean changePassword(String username, String oldPassword, String newPassword) throws IOException {
        Path userPath = userFile(username);
        if (!Files.exists(userPath)) {
            return false;
        }
        Map<String, Object> obj;
        try (InputStream in = Files.newInputStream(userPath)) {
            obj = MAPPER.readValue(in, new TypeReference<>() {});
        }
        Object hashObj = obj.get("password_hash");
        if (!(hashObj instanceof String)) {
            return false;
        }
        String hash = (String) hashObj;
        if (!BCrypt.checkpw(oldPassword, hash)) {
            return false;
        }
        String newHash = BCrypt.hashpw(newPassword, BCrypt.gensalt());
        obj.put("password_hash", newHash);
        try (OutputStream out = Files.newOutputStream(userPath)) {
            MAPPER.writerWithDefaultPrettyPrinter().writeValue(out, obj);
        }
        return true;
    }

    public static String issueToken(String username) {
        Date expiry = Date.from(Instant.now().plus(TOKEN_TTL));
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

    public static String getTokensFile() {
        return TOKENS_FILE;
    }
}
