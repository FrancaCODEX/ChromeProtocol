package br.com.chromeprotocol;

import java.util.*;

/** Server-owned career state. Money, offers and results never come from the browser. */
public class GameState {
    public String gameMode = "valorant-career-v1";
    public String name, nationality, role, background, agent;
    public Appearance appearance = Appearance.defaults();
    public int age = 15, year = 2026, phase, revision;
    public int money = 500, energy = 80, form = 50, stress = 15, chemistry = 50, reputation, followers = 100;
    public int wins, losses, seasonWins, seasonLosses, titles, earnings;
    public boolean ended;
    public String ending;
    public Map<String,Integer> attributes = new LinkedHashMap<>();
    public Contract contract;
    public Match match;
    public Outcome outcome;
    public List<Season> seasons = new ArrayList<>();
    public List<Trophy> trophies = new ArrayList<>();
    public List<History> history = new ArrayList<>();
    public record Contract(String orgId, String name, int tier, int salary, int endYear, String position, int pressure) {}
    public static class Match {
        public String opponent, map, competition;
        public int stage, roundsWon, score = 4, opponentScore = 4;
        public boolean finalEligible;
    }
    public record Change(String label, String before, String after) {}
    public record Outcome(String title, String narrative, String tone, String date, List<Change> changes) {}
    public record Season(int year, int age, String organization, int wins, int losses, int titles, int earnings) {}
    public record Trophy(int year, String name, String organization) {}
    public record History(String date, String title, String text, String tone, List<Change> changes) {}
}
