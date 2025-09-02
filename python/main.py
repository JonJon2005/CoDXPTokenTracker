# main.py
# In Java: // File: Main.java (contains public static void main)

import token_lib as tl  # In Java: import static token_lib.*;  // if everything were static helpers
from typing import Optional

# Optional: better Windows color support (safe if not installed)
# In Java: // N/A; console coloring differs (e.g., JANSI or platform-specific)
try:
    import colorama  # In Java: // N/A
    colorama.just_fix_windows_console()  # In Java: // N/A
except Exception:
    pass  # In Java: // ignore

FILENAME = "tokens.txt"  # In Java: final static String FILENAME = "tokens.txt";

# === ANSI color helpers ===
# In Java: // Define constants for ANSI color codes (or use a library like JANSI)
RESET = "\033[0m"  # In Java: final static String RESET = "\u001B[0m";
BOLD = "\033[1m"  # In Java: final static String BOLD = "\u001B[1m";
BRIGHT_RED = "\033[91m"  # In Java: final static String BRIGHT_RED = "\u001B[91m";
LIGHT_GREEN = "\033[92m"  # In Java: final static String LIGHT_GREEN = "\u001B[92m";
LIGHT_BLUE = "\033[94m"  # In Java: final static String LIGHT_BLUE = "\u001B[94m";
ORANGE = "\033[38;5;208m"  # In Java: final static String ORANGE = "\u001B[38;5;208m"; // 256-color orange
BRIGHT_WHITE = "\033[97m"  # In Java: final static String BRIGHT_WHITE = "\u001B[97m";
DIM = "\033[2m"  # In Java: final static String DIM = "\u001B[2m";

# In Java: static String color(String s, String c) { return c + s + RESET; }
def color(txt: str, c: str) -> str:
    return f"{c}{txt}{RESET}"  # In Java: return c + txt + RESET;

# In Java: static String colorForCategory(String cat) { ... }
def color_for_category(cat: str) -> str:
    if cat == "regular":  # In Java: if ("regular".equals(cat)) return LIGHT_GREEN;
        return LIGHT_GREEN  # In Java: return LIGHT_GREEN;
    if cat == "weapon":  # In Java: else if ("weapon".equals(cat)) return LIGHT_BLUE;
        return LIGHT_BLUE  # In Java: return LIGHT_BLUE;
    if cat == "battlepass":  # In Java: else if ("battlepass".equals(cat)) return ORANGE;
        return ORANGE  # In Java: return ORANGE;
    return BRIGHT_WHITE  # In Java: return BRIGHT_WHITE;

# In Java: static void displayAll(Map<String,List<Integer>> data) { ... System.out.println(...) }
def display_all(data: dict) -> None:
    print(color(BOLD + "=== Current 2XP Tokens ===", BRIGHT_RED))  # In Java: System.out.println(color("=== Current 2XP Tokens ===", BRIGHT_RED + BOLD));
    grand_minutes = 0  # In Java: int grand_minutes = 0;
    for cat in tl.CATEGORIES:  # In Java: for (String cat : CATEGORIES) {
        tokens = data[cat]  # In Java: List<Integer> tokens = data.get(cat);
        label = cat.capitalize()  # In Java: String label = Character.toUpperCase(cat.charAt(0)) + cat.substring(1);
        cat_color = color_for_category(cat)  # In Java: String catColor = colorForCategory(cat);
        print("\n" + color(label + ":", cat_color))  # In Java: System.out.println("\n" + color(label + ":", catColor));
        for mins, count in zip(tl.MINUTE_BUCKETS, tokens):  # In Java: for (int i=0;i<4;i++){ int mins=MINUTE_BUCKETS[i]; int count=tokens.get(i);
            print(f"  {DIM}{mins} min:{RESET} {count}")  # In Java: System.out.println("  " + DIM + mins + " min:" + RESET + " " + count);
        cat_minutes, cat_hours = tl.compute_totals(tokens)  # In Java: var p = computeTotals(tokens); int cat_minutes = p.getKey(); double cat_hours = p.getValue();
        grand_minutes += cat_minutes  # In Java: grand_minutes += cat_minutes;
        print("  " + color(f"→ {cat_minutes} minutes", cat_color) + f" ({cat_hours} hours)")  # In Java: System.out.println("  " + color("→ " + cat_minutes + " minutes", catColor) + " (" + cat_hours + " hours)");

    print("\n" + color(BOLD + "=== Grand Total (all types) ===", BRIGHT_RED))  # In Java: System.out.println("\n" + color("=== Grand Total (all types) ===", BRIGHT_RED + BOLD));
    print(color("Total minutes: ", BRIGHT_WHITE) + f"{grand_minutes}")  # In Java: System.out.println(color("Total minutes: ", BRIGHT_WHITE) + grand_minutes);
    print(color("Total hours: ", BRIGHT_WHITE) + f"{grand_minutes/60.0}\n")  # In Java: System.out.println(color("Total hours: ", BRIGHT_WHITE) + (grand_minutes/60.0) + "\n");

# In Java: static int pickCategory(Scanner sc) { ... return 0..2 or -1; }
def pick_category() -> Optional[int]:
    while True:
        print("\n" + color(BOLD + "Choose token type:", BRIGHT_RED))
        print("  1) " + color("Regular XP", LIGHT_GREEN))
        print("  2) " + color("Weapon XP", LIGHT_BLUE))
        print("  3) " + color("Battle Pass XP", ORANGE))
        s = input(color("Enter option (1-3) or press Enter to cancel: ", BRIGHT_WHITE)).strip()
        if not s:
            return None
        mapping = {"1": 0, "2": 1, "3": 2}
        if s in mapping:
            return mapping[s]
        print(color("Invalid token type. Please try again.", BRIGHT_RED))

# In Java: static void editSingle(Map<String,List<Integer>> data, Scanner sc) { ... }
def edit_single(data: dict) -> None:
    ci = pick_category()
    if ci is None:
        print(color("Edit cancelled.", BRIGHT_RED))
        return

    cat = tl.CATEGORIES[ci]
    tokens = data[cat]
    cat_color = color_for_category(cat)

    index_map = {"1": 0, "2": 1, "3": 2, "4": 3}
    while True:
        print("\n" + color("Which duration do you want to edit?", cat_color))
        print("  1) 15 minutes")
        print("  2) 30 minutes")
        print("  3) 45 minutes")
        print("  4) 60 minutes")
        choice = input(color("Enter option (1-4) or press Enter to cancel: ", BRIGHT_WHITE)).strip()
        if not choice:
            print(color("Edit cancelled.", BRIGHT_RED))
            return
        if choice in index_map:
            i = index_map[choice]
            break
        print(color("Invalid duration. Please try again.", BRIGHT_RED))

    while True:
        s = input(color(f"Enter new token count for {tl.MINUTE_BUCKETS[i]} minutes (blank to cancel): ", cat_color)).strip()
        if not s:
            print(color("Edit cancelled.", BRIGHT_RED))
            return
        try:
            new_val = int(s)
            if new_val < 0:
                new_val = 0
            tokens[i] = new_val
            data[cat] = tokens
            break
        except ValueError:
            print(color("Not a valid integer. Please try again.", BRIGHT_RED))

# In Java: static void editAllCategory(Map<String,List<Integer>> data, Scanner sc) { ... }
def edit_all_category(data: dict) -> None:
    ci = pick_category()
    if ci is None:
        print(color("Edit cancelled.", BRIGHT_RED))
        return
    cat = tl.CATEGORIES[ci]
    cat_color = color_for_category(cat)

    while True:
        print(color(f"Enter four integers for {cat.capitalize()} (15, 30, 45, 60), separated by spaces.", cat_color))
        raw = input(color("Example: 2 3 1 4\n> ", BRIGHT_WHITE)).strip()
        if not raw:
            print(color("Edit cancelled.", BRIGHT_RED))
            return
        parts = raw.split()
        if len(parts) != 4:
            print(color("Please enter exactly four integers.", BRIGHT_RED))
            continue
        try:
            vals = [max(0, int(p)) for p in parts]
            data[cat] = vals
            break
        except ValueError:
            print(color("One or more entries were not valid integers. Please try again.", BRIGHT_RED))

# In Java: static void editAllCategories(Map<String,List<Integer>> data, Scanner sc) { ... }
def edit_all_categories(data: dict) -> None:
    while True:
        print(color("Enter 12 integers for Regular, Weapon, Battle Pass (each 15,30,45,60).", BRIGHT_RED))
        print("Order: " + color("R15 R30 R45 R60", LIGHT_GREEN) + "  " + color("W15 W30 W45 W60", LIGHT_BLUE) + "  " + color("B15 B30 B45 B60", ORANGE))
        raw = input(color("Example: 1 0 2 0  0 1 0 0  3 0 0 1\n> ", BRIGHT_WHITE)).strip()
        if not raw:
            print(color("Edit cancelled.", BRIGHT_RED))
            return
        parts = raw.split()
        if len(parts) != 12:
            print(color("Please enter exactly 12 integers.", BRIGHT_RED))
            continue
        try:
            vals = [max(0, int(p)) for p in parts]
            data["regular"] = vals[0:4]
            data["weapon"] = vals[4:8]
            data["battlepass"] = vals[8:12]
            break
        except ValueError:
            print(color("One or more entries were not valid integers. Please try again.", BRIGHT_RED))

# In Java: static void exportTotalsInteractive(Map<String,List<Integer>> data, Scanner sc) throws IOException { ... }
def export_totals_interactive(data: dict) -> None:
    out_name = input(color("Enter filename to save totals (e.g., totals.txt): ", BRIGHT_WHITE)).strip()  # In Java: String out_name = sc.nextLine().trim();
    if not out_name:  # In Java: if (out_name.isEmpty()) { System.out.println("No filename provided."); return; }
        print(color("No filename provided.", BRIGHT_RED))
        return  # In Java: return;
    if not out_name.lower().endswith(".txt"):  # In Java: if (!out_name.toLowerCase().endsWith(".txt")) out_name += ".txt";
        out_name += ".txt"  # In Java: out_name = out_name + ".txt";
    report = tl.build_totals_report(data)  # In Java: String report = buildTotalsReport(data);
    try:  # In Java: try {
        with open(out_name, "w") as f:  # In Java: Files.writeString(Paths.get(out_name), report, StandardCharsets.UTF_8);
            f.write(report)  # In Java: // write report
        print(color(f"Saved totals to '{out_name}'.", LIGHT_GREEN))  # In Java: System.out.println(color("Saved totals to '...'", LIGHT_GREEN));
    except OSError as e:  # In Java: } catch (IOException e) {
        print(color(f"Failed to save file: {e}", BRIGHT_RED))  # In Java: System.out.println(color("Failed to save file: " + e, BRIGHT_RED));

# In Java: static void printMenu() { System.out.println(...); }
def print_menu() -> None:
    print(color(BOLD + "=== 2XP Token Manager (Regular / Weapon / Battle Pass) ===", BRIGHT_RED))  # In Java: System.out.println(color("=== ... ===", BRIGHT_RED + BOLD));
    print("1) View all tokens and totals")  # In Java: System.out.println("1) View all tokens and totals");
    print("2) Edit " + color("ONE", BRIGHT_WHITE) + " duration in " + color("ONE category", BRIGHT_WHITE))  # In Java: System.out.println("2) Edit ONE duration in ONE category");
    print("3) Edit ALL four values in ONE category")  # In Java: System.out.println("3) Edit ALL four values in ONE category");
    print("4) Edit ALL categories at once (12 values)")  # In Java: System.out.println("4) Edit ALL categories at once (12 values)");
    print("5) Save changes")  # In Java: System.out.println("5) Save changes");
    print("6) Save & Exit")  # In Java: System.out.println("6) Save & Exit");
    print("7) Exit WITHOUT saving")  # In Java: System.out.println("7) Exit WITHOUT saving");
    print("8) Export totals to a text file")  # In Java: System.out.println("8) Export totals to a text file");

# In Java: public static void main(String[] args) throws Exception { ... }
def main() -> None:
    data = tl.read_all_tokens(FILENAME)  # In Java: Map<String,List<Integer>> data = readAllTokens(FILENAME);
    dirty = False  # In Java: boolean dirty = false;
    while True:  # In Java: while (true) {
        print_menu()  # In Java: printMenu();
        choice = input(color("Choose an option (1-8): ", BRIGHT_WHITE)).strip()  # In Java: String choice = sc.nextLine().trim();
        if choice == "1":  # In Java: if ("1".equals(choice)) {
            display_all(data)  # In Java: displayAll(data);
        elif choice == "2":  # In Java: } else if ("2".equals(choice)) {
            edit_single(data)  # In Java: editSingle(data, sc);
            dirty = True  # In Java: dirty = true;
        elif choice == "3":  # In Java: } else if ("3".equals(choice)) {
            edit_all_category(data)  # In Java: editAllCategory(data, sc);
            dirty = True  # In Java: dirty = true;
        elif choice == "4":  # In Java: } else if ("4".equals(choice)) {
            edit_all_categories(data)  # In Java: editAllCategories(data, sc);
            dirty = True  # In Java: dirty = true;
        elif choice == "5":  # In Java: } else if ("5".equals(choice)) {
            tl.write_all_tokens(FILENAME, data)  # In Java: writeAllTokens(FILENAME, data);
            dirty = False  # In Java: dirty = false;
            print(color("Saved.\n", LIGHT_GREEN))  # In Java: System.out.println(color("Saved.\n", LIGHT_GREEN));
        elif choice == "6":  # In Java: } else if ("6".equals(choice)) {
            tl.write_all_tokens(FILENAME, data)  # In Java: writeAllTokens(FILENAME, data);
            print(color("Saved. Exiting...", LIGHT_GREEN))  # In Java: System.out.println(color("Saved. Exiting...", LIGHT_GREEN));
            break  # In Java: break;
        elif choice == "7":  # In Java: } else if ("7".equals(choice)) {
            if dirty:  # In Java: if (dirty) {
                confirm = input(color("Unsaved changes. Exit without saving? (y/N): ", BRIGHT_WHITE)).strip().lower()  # In Java: String confirm = sc.nextLine().trim().toLowerCase();
                if confirm != "y":  # In Java: if (!"y".equals(confirm)) { continue; }
                    continue  # In Java: continue;
            print(color("Exiting without saving...", BRIGHT_RED))  # In Java: System.out.println(color("Exiting without saving...", BRIGHT_RED));
            break  # In Java: break;
        elif choice == "8":  # In Java: } else if ("8".equals(choice)) {
            export_totals_interactive(data)  # In Java: exportTotalsInteractive(data, sc);
        else:  # In Java: } else {
            print(color("Invalid option.\n", BRIGHT_RED))  # In Java: System.out.println(color("Invalid option.\n", BRIGHT_RED));

if __name__ == "__main__":  # In Java: // Not applicable; Java uses the main method signature.
    main()  # In Java: main(new String[] {});
