package br.com.chromeprotocol;

import java.util.*;

/** Serializable state owned by the server. The browser only submits action IDs. */
public class GameState {
    public String name, origin, specialty;
    public int day = 1, money, health = 100, energy = 100, heat, reputation, debt;
    public int level = 1, xp, skillPoints, missionIndex, revision;
    public boolean ended;
    public String ending;
    public Map<String, Integer> attributes = new LinkedHashMap<>();
    public Map<String, Integer> factions = new LinkedHashMap<>();
    public Set<String> flags = new LinkedHashSet<>();
    public Set<String> implants = new LinkedHashSet<>();
    public List<LogEntry> journal = new ArrayList<>();

    public record LogEntry(int day, String title, String text, String tone) {}
}
