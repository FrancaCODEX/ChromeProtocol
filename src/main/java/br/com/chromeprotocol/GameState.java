package br.com.chromeprotocol;

import java.util.*;

/** Server-owned career state. Money, offers and results never come from the browser. */
public class GameState {
    public String gameMode = "valorant-career-v1";
    public String name, nationality, role, background, agent;
    public int rulesVersion = 3;
    public String developmentMode = "standard";
    public boolean igl, pendingCards, retiring, qualifiedMasters1, qualifiedMasters2, qualifiedPlayoffs, qualifiedChampions;
    public int cardChanceBonus, eventVariant;
    public Set<String> activitySlots = new LinkedHashSet<>();
    public String seasonGoal;
    public int goalStart;
    public boolean goalSettled;
    public List<GoalAchievement> goals = new ArrayList<>();
    public long careerSeed;
    public String pace = "detailed";
    public int seriesWins, seriesLosses, seasonSeriesWins, seasonSeriesLosses, runEventCount;
    public boolean tourEvent, legacyMap;
    public Set<Integer> seenLifeEvents = new LinkedHashSet<>();
    public Map<Integer,Campaign> campaigns = new LinkedHashMap<>();
    public Series currentSeries;
    public List<SeriesResult> series = new ArrayList<>();
    public static class Campaign {
        public String part;
        public int played, wins, losses, groupWins, groupLosses, upperWins, bracketLosses, lowerRemaining, seed;
        public boolean done, champion, qualified;
    }
    public static class Series {
        public String id, opponent, competition, round, critical;
        public int year, phase, bestOf = 3, wins, losses;
        public boolean official;
    }
    public record SeriesResult(String id,int year,int phase,String competition,String round,String opponent,String team,int bestOf,int wins,int losses,boolean won,boolean official,String mode,List<MapStats> maps) {}

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
    public Contract expiredContract;
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
        public String seriesId;
        public int mapNumber;
    }
    public record Change(String label, String before, String after) {}
    public record Outcome(String title, String narrative, String tone, String date, List<Change> changes, MapStats mapStats,List<SeriesResult> seriesResults) {
        public Outcome(String title,String narrative,String tone,String date,List<Change> changes,MapStats mapStats){this(title,narrative,tone,date,changes,mapStats,List.of());}
    }
    public record Season(int year, int age, String organization, int wins, int losses, int titles, int earnings, Integer overall,Integer seriesWins,Integer seriesLosses) {
        public Season(int year,int age,String organization,int wins,int losses,int titles,int earnings,Integer overall){this(year,age,organization,wins,losses,titles,earnings,overall,null,null);}
    }
    public record Trophy(int year, String name, String organization) {}
    public record GoalAchievement(int year,String name,boolean completed,int cash) {}
    public record SelectedCard(int year,String id,String name) {}
    public record Award(int year,String title,String detail) {}
    public record MapStats(int year,int phase,String competition,String map,String opponent,String team,boolean won,int score,int opponentScore,int kills,int deaths,int assists,int combatScore,int rounds,double acs,String kd,boolean official,String seriesId,Integer mapNumber,String mode) {
        public MapStats(int year,int phase,String competition,String map,String opponent,String team,boolean won,int score,int opponentScore,int kills,int deaths,int assists,int combatScore,int rounds,double acs,String kd,boolean official){this(year,phase,competition,map,opponent,team,won,score,opponentScore,kills,deaths,assists,combatScore,rounds,acs,kd,official,null,null,"legacy");}
    }
    public record History(String date, String title, String text, String tone, List<Change> changes) {}
}
