package br.com.chromeprotocol;

import java.util.*;

/** Server-owned career state. Money, offers and results never come from the browser. */
public class GameState {
    public String gameMode = "valorant-career-v1";
    public String name, nationality, role, background, agent;
    public int rulesVersion = 2;
    public String developmentMode = "standard";
    public boolean igl, pendingCards, retiring, qualifiedMasters1, qualifiedMasters2, qualifiedPlayoffs, qualifiedChampions;
    public int cardChanceBonus, eventVariant;
    public Set<String> activitySlots = new LinkedHashSet<>();
    public String seasonGoal;
    public int goalStart;
    public boolean goalSettled;
    public List<GoalAchievement> goals = new ArrayList<>();
    public long careerSeed;
    public List<String> favoriteAgents = new ArrayList<>();
    public Map<String,Integer> investments = new LinkedHashMap<>();
    public Map<String,Integer> permanentBonuses = new LinkedHashMap<>();
    public List<SelectedCard> cards = new ArrayList<>();
    public List<MapStats> maps = new ArrayList<>();
    public List<Award> awards = new ArrayList<>();
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
    public record Outcome(String title, String narrative, String tone, String date, List<Change> changes, MapStats mapStats) {}
    public record Season(int year, int age, String organization, int wins, int losses, int titles, int earnings, Integer overall) {}
    public record Trophy(int year, String name, String organization) {}
    public record GoalAchievement(int year,String name,boolean completed,int cash) {}
    public record SelectedCard(int year,String id,String name) {}
    public record Award(int year,String title,String detail) {}
    public record MapStats(int year,int phase,String competition,String map,String opponent,String team,boolean won,int score,int opponentScore,int kills,int deaths,int assists,int combatScore,int rounds,double acs,String kd,boolean official) {}
    public record History(String date, String title, String text, String tone, List<Change> changes) {}
}
