package com.codxp.tokens;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.mindrot.jbcrypt.BCrypt;

import java.io.IOException;
import java.nio.file.*;
import java.util.HashMap;
import java.util.Map;

public class UserService {
    private static final String USERS_FILE = resolveUsersFile();
    private final ObjectMapper mapper = new ObjectMapper();

    private static String resolveUsersFile() {
        Path p = Paths.get("users.json");
        if (!Files.exists(p)) {
            p = Paths.get("../users.json");
        }
        return p.toString();
    }

    private Map<String, String> loadUsers() throws IOException {
        Path p = Paths.get(USERS_FILE);
        if (!Files.exists(p)) {
            saveUsers(new HashMap<>());
        }
        byte[] bytes = Files.readAllBytes(p);
        if (bytes.length == 0) {
            return new HashMap<>();
        }
        return mapper.readValue(bytes, new TypeReference<Map<String, String>>() {});
    }

    private void saveUsers(Map<String, String> users) throws IOException {
        byte[] bytes = mapper.writerWithDefaultPrettyPrinter().writeValueAsBytes(users);
        Files.write(Paths.get(USERS_FILE), bytes);
    }

    public synchronized boolean register(String username, String password) throws IOException {
        Map<String, String> users = loadUsers();
        if (users.containsKey(username)) {
            return false;
        }
        String hash = BCrypt.hashpw(password, BCrypt.gensalt());
        users.put(username, hash);
        saveUsers(users);
        return true;
    }

    public boolean authenticate(String username, String password) throws IOException {
        Map<String, String> users = loadUsers();
        String hash = users.get(username);
        return hash != null && BCrypt.checkpw(password, hash);
    }

    public String tokensFileFor(String username) {
        Path p = Paths.get(username + "-tokens.txt");
        if (!Files.exists(p)) {
            p = Paths.get("../" + username + "-tokens.txt");
        }
        return p.toString();
    }
}
