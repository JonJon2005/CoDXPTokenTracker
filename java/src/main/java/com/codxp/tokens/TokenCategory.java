package com.codxp.tokens;

public enum TokenCategory {
    REGULAR("regular"),
    WEAPON("weapon"),
    BATTLEPASS("battlepass");

    private final String key;

    TokenCategory(String key) {
        this.key = key;
    }

    public String key() {
        return key;
    }

    public String displayName() {
        return Character.toUpperCase(key.charAt(0)) + key.substring(1);
    }
}
