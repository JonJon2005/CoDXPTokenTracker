# token_lib.py
# In Java: // File: token_lib.java (conceptually a utility class with static methods)

import os
# In Java: import java.nio.file.*; import java.io.*;  // for Files.exists, etc.

from typing import List, Dict, Tuple
# In Java: import java.util.*;  // for List, Map, etc.

MINUTE_BUCKETS = [15, 30, 45, 60]
# In Java: final static int[] MINUTE_BUCKETS = {15, 30, 45, 60};

CATEGORIES = ["regular", "weapon", "battlepass"]
# In Java: final static String[] CATEGORIES = {"regular", "weapon", "battlepass"};

def ensure_file(filename: str) -> None:
    # In Java: static void ensureFile(String filename) throws IOException { if (!Files.exists(Paths.get(filename))) Files.write(Paths.get(filename), Collections.nCopies(12, "0")); }
    if not os.path.exists(filename):
        # In Java: if (!Files.exists(Paths.get(filename))) { Files.write(Paths.get(filename), Collections.nCopies(12, "0")); }
        with open(filename, "w") as f:
            # In Java: try (BufferedWriter bw = Files.newBufferedWriter(Paths.get(filename))) { for (int i=0;i<12;i++) { bw.write("0"); bw.newLine(); } }
            f.write(("0\n" * 12))

def _parse_ints(lines: List[str], n: int) -> List[int]:
    # In Java: static List<Integer> parseInts(List<String> lines, int n) { var out = new ArrayList<Integer>(); for (int i=0;i<n;i++){ try{ out.add(Integer.parseInt(lines.get(i).trim())); } catch(Exception e){ out.add(0);} } return out; }
    out: List[int] = []
    # In Java: List<Integer> out = new ArrayList<>();
    for i in range(n):
        # In Java: for (int i=0; i<n; i++) {
        try:
            # In Java: out.add(Integer.parseInt(lines.get(i).trim()));
            out.append(int(lines[i].strip()))
        except Exception:
            # In Java: } catch (Exception e) { out.add(0); }
            out.append(0)
    return out
    # In Java: return out;

def read_all_tokens(filename: str) -> Dict[str, List[int]]:
    # In Java: static Map<String,List<Integer>> readAllTokens(String filename) throws IOException { ... }
    ensure_file(filename)
    # In Java: ensureFile(filename);
    with open(filename, "r") as f:
        # In Java: List<String> raw = Files.readAllLines(Paths.get(filename));
        raw = [line.strip() for line in f.readlines()]
    data: Dict[str, List[int]] = {}
    # In Java: Map<String,List<Integer>> data = new HashMap<>();
    if len(raw) >= 12:
        # In Java: if (raw.size() >= 12) {
        data["regular"] = _parse_ints(raw[0:4], 4)
        # In Java: data.put("regular", parseInts(raw.subList(0,4), 4));
        data["weapon"] = _parse_ints(raw[4:8], 4)
        # In Java: data.put("weapon", parseInts(raw.subList(4,8), 4));
        data["battlepass"] = _parse_ints(raw[8:12], 4)
        # In Java: data.put("battlepass", parseInts(raw.subList(8,12), 4));
    else:
        # In Java: } else {
        data["regular"] = _parse_ints(raw, 4)
        # In Java: data.put("regular", parseInts(raw, 4));
        data["weapon"] = [0, 0, 0, 0]
        # In Java: data.put("weapon", Arrays.asList(0,0,0,0));
        data["battlepass"] = [0, 0, 0, 0]
        # In Java: data.put("battlepass", Arrays.asList(0,0,0,0));
    return data
    # In Java: return data;

def write_all_tokens(filename: str, data: Dict[str, List[int]]) -> None:
    # In Java: static void writeAllTokens(String filename, Map<String,List<Integer>> data) throws IOException { ... }
    out_lines: List[str] = []
    # In Java: List<String> out = new ArrayList<>(12);
    for cat in CATEGORIES:
        # In Java: for (String cat : CATEGORIES) {
        vals = (data.get(cat, [0, 0, 0, 0]) + [0, 0, 0, 0])[:4]
        # In Java: List<Integer> vals = ensureSize4(data.getOrDefault(cat, Arrays.asList(0,0,0,0)));
        for v in vals:
            # In Java: for (Integer v : vals) { out.add(String.valueOf(v)); }
            out_lines.append(str(v))
    with open(filename, "w") as f:
        # In Java: Files.write(Paths.get(filename), out, StandardCharsets.UTF_8);
        f.write("\n".join(out_lines) + "\n")

def compute_totals(tokens: List[int]) -> Tuple[int, float]:
    # In Java: static Pair<Integer,Double> computeTotals(List<Integer> tokens) { int total = 0; for (int i=0;i<4;i++) total += tokens.get(i)*MINUTE_BUCKETS[i]; double hours = total/60.0; return new Pair<>(total, hours); }
    total_minutes = 0
    # In Java: int total_minutes = 0;
    for count, minutes in zip(tokens, MINUTE_BUCKETS):
        # In Java: for (int i=0;i<4;i++){ int count=tokens.get(i); int minutes=MINUTE_BUCKETS[i];
        total_minutes += count * minutes
        # In Java: total_minutes += count * minutes;
    total_hours = total_minutes / 60.0
    # In Java: double total_hours = total_minutes / 60.0;
    return total_minutes, total_hours
    # In Java: return new Pair<>(total_minutes, total_hours);

def build_totals_report(data: Dict[str, List[int]]) -> str:
    # In Java: static String buildTotalsReport(Map<String,List<Integer>> data) { var sb=new StringBuilder(); ... return sb.toString(); }
    lines: List[str] = []
    # In Java: List<String> lines = new ArrayList<>();
    lines.append("=== 2XP Totals Report ===")
    # In Java: lines.add("=== 2XP Totals Report ===");
    grand_minutes = 0
    # In Java: int grand_minutes = 0;
    for cat in CATEGORIES:
        # In Java: for (String cat : CATEGORIES) {
        tokens = data[cat]
        # In Java: List<Integer> tokens = data.get(cat);
        cat_minutes, cat_hours = compute_totals(tokens)
        # In Java: var p = computeTotals(tokens); int cat_minutes = p.getKey(); double cat_hours = p.getValue();
        label = cat.capitalize()
        # In Java: String label = Character.toUpperCase(cat.charAt(0)) + cat.substring(1);
        lines.append(f"{label}: {cat_minutes} minutes ({cat_hours:.2f} hours)")
        # In Java: lines.add(label + ": " + cat_minutes + " minutes (" + String.format(Locale.US, "%.2f", cat_hours) + " hours)");
        grand_minutes += cat_minutes
        # In Java: grand_minutes += cat_minutes;
    lines.append("")
    # In Java: lines.add("");
    lines.append(f"Grand Total: {grand_minutes} minutes ({grand_minutes/60.0:.2f} hours)")
    # In Java: lines.add("Grand Total: " + grand_minutes + " minutes (" + String.format(Locale.US, "%.2f", grand_minutes/60.0) + " hours)");
    return "\n".join(lines)
    # In Java: return String.join("\n", lines);
