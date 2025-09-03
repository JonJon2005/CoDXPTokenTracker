package com.codxp.tokens;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

public class Main {
    static final String FILENAME = "../tokens.txt";

    static final String RESET = "\u001B[0m";
    static final String BOLD = "\u001B[1m";
    static final String BRIGHT_RED = "\u001B[91m";
    static final String LIGHT_GREEN = "\u001B[92m";
    static final String LIGHT_BLUE = "\u001B[94m";
    static final String ORANGE = "\u001B[38;5;208m";
    static final String BRIGHT_WHITE = "\u001B[97m";
    static final String DIM = "\u001B[2m";

    static String color(String txt, String c) {
        return c + txt + RESET;
    }

    static String colorForCategory(TokenCategory cat) {
        switch (cat) {
            case REGULAR:
                return LIGHT_GREEN;
            case WEAPON:
                return LIGHT_BLUE;
            case BATTLEPASS:
                return ORANGE;
            default:
                return BRIGHT_WHITE;
        }
    }

    static void displayAll(Map<TokenCategory, List<Integer>> data) {
        System.out.println(color(BOLD + "=== Current 2XP Tokens ===", BRIGHT_RED));
        int grand = 0;
        for (TokenCategory cat : TokenCategory.values()) {
            List<Integer> tokens = data.get(cat);
            String label = cat.displayName();
            String catColor = colorForCategory(cat);
            System.out.println("\n" + color(label + ":", catColor));
            for (int i = 0; i < 4; i++) {
                int mins = TokenLib.MINUTE_BUCKETS[i];
                int count = tokens.get(i);
                System.out.println("  " + DIM + mins + " min:" + RESET + " " + count);
            }
            AbstractMap.SimpleEntry<Integer, Double> p = TokenLib.computeTotals(tokens);
            int catMinutes = p.getKey();
            double catHours = p.getValue();
            grand += catMinutes;
            System.out.println("  " + color("→ " + catMinutes + " minutes", catColor) + " (" + catHours + " hours)");
        }
        System.out.println("\n" + color(BOLD + "=== Grand Total (all types) ===", BRIGHT_RED));
        System.out.println(color("Total minutes: ", BRIGHT_WHITE) + grand);
        System.out.println(color("Total hours: ", BRIGHT_WHITE) + (grand / 60.0) + "\n");
    }

    static TokenCategory pickCategory(Scanner sc) {
        while (true) {
            System.out.println("\n" + color(BOLD + "Choose token type:", BRIGHT_RED));
            System.out.println("  1) " + color("Regular XP", LIGHT_GREEN));
            System.out.println("  2) " + color("Weapon XP", LIGHT_BLUE));
            System.out.println("  3) " + color("Battle Pass XP", ORANGE));
            String s = sc.nextLine().trim();
            if (s.isEmpty()) {
                return null;
            }
            switch (s) {
                case "1":
                    return TokenCategory.REGULAR;
                case "2":
                    return TokenCategory.WEAPON;
                case "3":
                    return TokenCategory.BATTLEPASS;
                default:
                    System.out.println(color("Invalid token type. Please try again.", BRIGHT_RED));
            }
        }
    }

    static void editSingle(Map<TokenCategory, List<Integer>> data, Scanner sc) {
        TokenCategory cat = pickCategory(sc);
        if (cat == null) {
            System.out.println(color("Edit cancelled.", BRIGHT_RED));
            return;
        }
        List<Integer> tokens = data.get(cat);
        String catColor = colorForCategory(cat);

        Map<String, Integer> indexMap = new HashMap<>();
        indexMap.put("1", 0);
        indexMap.put("2", 1);
        indexMap.put("3", 2);
        indexMap.put("4", 3);

        int i;
        while (true) {
            System.out.println("\n" + color("Which duration do you want to edit?", catColor));
            System.out.println("  1) 15 minutes");
            System.out.println("  2) 30 minutes");
            System.out.println("  3) 45 minutes");
            System.out.println("  4) 60 minutes");
            String choice = sc.nextLine().trim();
            if (choice.isEmpty()) {
                System.out.println(color("Edit cancelled.", BRIGHT_RED));
                return;
            }
            if (indexMap.containsKey(choice)) {
                i = indexMap.get(choice);
                break;
            }
            System.out.println(color("Invalid duration. Please try again.", BRIGHT_RED));
        }

        while (true) {
            System.out.print(color(
                "Enter new token count for " + TokenLib.MINUTE_BUCKETS[i] + " minutes (blank to cancel): ",
                catColor));
            String raw = sc.nextLine().trim();
            if (raw.isEmpty()) {
                System.out.println(color("Edit cancelled.", BRIGHT_RED));
                return;
            }
            try {
                int newVal = Integer.parseInt(raw);
                if (newVal < 0) newVal = 0;
                tokens.set(i, newVal);
                data.put(cat, tokens);
                break;
            } catch (NumberFormatException e) {
                System.out.println(color("Not a valid integer. Please try again.", BRIGHT_RED));
            }
        }
    }

    static void editAllCategory(Map<TokenCategory, List<Integer>> data, Scanner sc) {
        TokenCategory cat = pickCategory(sc);
        if (cat == null) {
            System.out.println(color("Edit cancelled.", BRIGHT_RED));
            return;
        }
        String catColor = colorForCategory(cat);

        while (true) {
            System.out.println(color(
                "Enter four integers for " + cat.displayName() + " (15, 30, 45, 60), separated by spaces.",
                catColor));
            System.out.print(color("Example: 2 3 1 4\n> ", BRIGHT_WHITE));
            String raw = sc.nextLine().trim();
            if (raw.isEmpty()) {
                System.out.println(color("Edit cancelled.", BRIGHT_RED));
                return;
            }
            String[] parts = raw.split("\\s+");
            if (parts.length != 4) {
                System.out.println(color("Please enter exactly four integers.", BRIGHT_RED));
                continue;
            }
            try {
                List<Integer> vals = new ArrayList<>();
                for (String p : parts) {
                    int v = Integer.parseInt(p);
                    vals.add(Math.max(0, v));
                }
                data.put(cat, vals);
                break;
            } catch (NumberFormatException e) {
                System.out.println(color("One or more entries were not valid integers. Please try again.", BRIGHT_RED));
            }
        }
    }

    static void editAllCategories(Map<TokenCategory, List<Integer>> data, Scanner sc) {
        while (true) {
            System.out.println(color("Enter 12 integers for Regular, Weapon, Battle Pass (each 15,30,45,60).", BRIGHT_RED));
            System.out.println("Order: " + color("R15 R30 R45 R60", LIGHT_GREEN) + "  " + color("W15 W30 W45 W60", LIGHT_BLUE) + "  " + color("B15 B30 B45 B60", ORANGE));
            System.out.print(color("Example: 1 0 2 0  0 1 0 0  3 0 0 1\n> ", BRIGHT_WHITE));
            String raw = sc.nextLine().trim();
            if (raw.isEmpty()) {
                System.out.println(color("Edit cancelled.", BRIGHT_RED));
                return;
            }
            String[] parts = raw.split("\\s+");
            if (parts.length != 12) {
                System.out.println(color("Please enter exactly 12 integers.", BRIGHT_RED));
                continue;
            }
            try {
                List<Integer> vals = new ArrayList<>();
                for (String p : parts) {
                    int v = Integer.parseInt(p);
                    vals.add(Math.max(0, v));
                }
                data.put(TokenCategory.REGULAR, vals.subList(0, 4));
                data.put(TokenCategory.WEAPON, vals.subList(4, 8));
                data.put(TokenCategory.BATTLEPASS, vals.subList(8, 12));
                break;
            } catch (NumberFormatException e) {
                System.out.println(color("One or more entries were not valid integers. Please try again.", BRIGHT_RED));
            }
        }
    }

    static void exportTotalsInteractive(Map<TokenCategory, List<Integer>> data, Scanner sc) {
        System.out.print(color("Enter filename to save totals (e.g., totals.txt): ", BRIGHT_WHITE));
        String outName = sc.nextLine().trim();
        if (outName.isEmpty()) {
            System.out.println(color("No filename provided.", BRIGHT_RED));
            return;
        }
        if (!outName.toLowerCase().endsWith(".txt")) {
            outName = outName + ".txt";
        }
        String report = TokenLib.buildTotalsReport(data);
        try {
            Files.writeString(Paths.get(outName), report, StandardCharsets.UTF_8);
            System.out.println(color("Saved totals to '" + outName + "'.", LIGHT_GREEN));
        } catch (IOException e) {
            System.out.println(color("Failed to save file: " + e, BRIGHT_RED));
        }
    }

    static void printMenu() {
        System.out.println(color(BOLD + "=== 2XP Token Manager (Regular / Weapon / Battle Pass) ===", BRIGHT_RED));
        System.out.println("1) View all tokens and totals");
        System.out.println("2) Edit " + color("ONE", BRIGHT_WHITE) + " duration in " + color("ONE category", BRIGHT_WHITE));
        System.out.println("3) Edit ALL four values in ONE category");
        System.out.println("4) Edit ALL categories at once (12 values)");
        System.out.println("5) Save changes");
        System.out.println("6) Save & Exit");
        System.out.println("7) Exit WITHOUT saving");
        System.out.println("8) Export totals to a text file");
    }

    public static void main(String[] args) throws Exception {
        Scanner sc = new Scanner(System.in);
        Map<TokenCategory, List<Integer>> data = TokenLib.readAllTokens(FILENAME);
        boolean dirty = false;
        while (true) {
            printMenu();
            System.out.print(color("Choose an option (1-8): ", BRIGHT_WHITE));
            String choice = sc.nextLine().trim();
            if ("1".equals(choice)) {
                displayAll(data);
            } else if ("2".equals(choice)) {
                editSingle(data, sc);
                dirty = true;
            } else if ("3".equals(choice)) {
                editAllCategory(data, sc);
                dirty = true;
            } else if ("4".equals(choice)) {
                editAllCategories(data, sc);
                dirty = true;
            } else if ("5".equals(choice)) {
                TokenLib.writeAllTokens(FILENAME, data);
                dirty = false;
                System.out.println(color("Saved.\n", LIGHT_GREEN));
            } else if ("6".equals(choice)) {
                TokenLib.writeAllTokens(FILENAME, data);
                System.out.println(color("Saved. Exiting...", LIGHT_GREEN));
                break;
            } else if ("7".equals(choice)) {
                if (dirty) {
                    System.out.print(color("Unsaved changes. Exit without saving? (y/N): ", BRIGHT_WHITE));
                    String confirm = sc.nextLine().trim().toLowerCase();
                    if (!"y".equals(confirm)) {
                        continue;
                    }
                }
                System.out.println(color("Exiting without saving...", BRIGHT_RED));
                break;
            } else if ("8".equals(choice)) {
                exportTotalsInteractive(data, sc);
            } else {
                System.out.println(color("Invalid option.\n", BRIGHT_RED));
            }
        }
        sc.close();
    }
}

