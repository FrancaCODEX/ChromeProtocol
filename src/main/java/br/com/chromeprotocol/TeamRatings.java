package br.com.chromeprotocol;

import java.util.*;

/** Official GPR snapshot -> fictional OVR. Future lineups and form belong only to this career. */
public final class TeamRatings {
    private TeamRatings() {}
    public static final String SOURCE="https://valorantesports.com/gpr";
    public static final String SNAPSHOT="2026-10-16";
    public static final Map<String,Integer> GPR=Map.ofEntries(
        Map.entry("leviatan-esports",1498),Map.entry("nrg",1482),Map.entry("loud",1481),Map.entry("100-thieves",1463),Map.entry("g2-esports",1436),
        Map.entry("team-vitality",1405),Map.entry("paper-rex",1403),Map.entry("global-esports",1403),Map.entry("karmine-corp",1395),Map.entry("t1",1386),
        Map.entry("nongshim-redforce",1364),Map.entry("team-liquid",1348),Map.entry("mibr",1314),Map.entry("geng-esports",1292),Map.entry("varrel",1274),
        Map.entry("team-heretics",1271),Map.entry("detonation-gaming",1270),Map.entry("tyloo-gaming",1269),Map.entry("drx",1262),Map.entry("fnatic",1260),
        Map.entry("bbl",1259),Map.entry("futbolist",1256),Map.entry("rex-regum-qeon",1254),Map.entry("eternal-fire",1247),Map.entry("team-envy",1238),
        Map.entry("full-sense",1228),Map.entry("edward-gaming",1215),Map.entry("cloud9",1210),Map.entry("sentinels",1208),Map.entry("natus-vincere",1207),
        Map.entry("kru-visa",1205),Map.entry("supra-beaux-gosses",1199),Map.entry("zeta-division",1197),Map.entry("evil-geniuses",1188),Map.entry("furia",1180),
        Map.entry("all-gamers",1173),Map.entry("xilai-gaming",1172),Map.entry("bilibili-gaming",1168),Map.entry("jdg-esports",1164),Map.entry("giants",1164),
        Map.entry("nova-esports",1148),Map.entry("dragon-ranger-gaming",1147),Map.entry("team-secret",1141),Map.entry("funplus-phoenix",1114),Map.entry("wolves-esports",1109),
        Map.entry("pcific-esports",1087),Map.entry("trace-esports",1071),Map.entry("titan-esports-club",1062));
    public record Profile(String orgId,String name,int overall,List<Integer> roster,int initialOvr,boolean includesPlayer) {}
    public record Matchup(String team,int teamOvr,String opponent,int opponentOvr,int mapWinChance) {}
    public static int base(String id,int tier){return GPR.containsKey(id)?60+(int)Math.round((GPR.get(id)-1062)*32.0/436):tier==3?70:tier==2?57:46;}
    public static int stageStrength(GameState s,String name){return profile(s,name,false,true).overall();}
    public static Profile profile(GameState s,String name,boolean player){return profile(s,name,player,false);}
    private static Profile profile(GameState s,String name,boolean player,boolean priorStages){
        var org=Catalog.ORGS.stream().filter(o->o.name().equals(name)).findFirst().orElse(null);
        String id=org==null?name:org.id();int tier=org==null?1:Catalog.organizationTier(s,org);
        int base=base(id,org==null?1:org.tier());
        if(s.vctPromotions.getOrDefault(id,Integer.MAX_VALUE)<=s.year)base=Math.max(68,base);
        int form=0;var recent=s.series.stream().filter(r->r.official()&&(r.team().equals(name)||r.opponent().equals(name))&&r.year()>=s.year-1&&(!priorStages||r.year()!=s.year||r.phase()!=s.phase)).limit(10).toList();
        for(var r:recent)form+=(r.team().equals(name)?r.won():!r.won())?1:-1;form=clamp(form/2,-4,4);
        var roster=new ArrayList<Integer>();
        for(int slot=0;slot<5;slot++){
            int rating=base+new Random(Objects.hash(s.careerSeed,id,slot,"initial")).nextInt(11)-5;
            for(int year=2027;year<=s.year;year++)rating=clamp(rating+new Random(Objects.hash(s.careerSeed,id,slot,year,"roster")).nextInt(11)-5,35,96);
            roster.add(clamp(rating+form,30,99));
        }
        boolean include=player&&name.equals(Catalog.team(s))&&!Calendar.reserve(s);
        if(include)roster.set(4,Catalog.overall(s));
        return new Profile(id,name,(int)Math.round(roster.stream().mapToInt(Integer::intValue).average().orElse(base)),List.copyOf(roster),base,include);
    }
    public static int chance(GameState s,GameState.Series f){
        int own=f.teamOvr==null?profile(s,Catalog.team(s),true).overall():f.teamOvr;
        int rival=f.opponentOvr==null?profile(s,f.opponent,false).overall():f.opponentOvr;
        int condition=(s.energy-50)/20+(s.form-50)/20+(s.chemistry-50)/20-s.stress/25;
        return clamp(50+(own-rival)*2+condition+Math.min(6,s.cardChanceBonus/3)+(s.developmentMode.equals("prodigy")?3:0)+(s.igl?1:0),12,85);
    }
    private static int clamp(int n,int lo,int hi){return Math.max(lo,Math.min(hi,n));}
    public static Matchup matchup(GameState s){
        if(!Calendar.match(s)||s.pendingCards||s.ended)return null;
        var f=s.currentSeries==null?Tour.fixture(s):s.currentSeries;
        if(s.currentSeries==null&&s.match!=null){f=new GameState.Series();f.opponent=s.match.opponent;}
        return new Matchup(Catalog.team(s),f.teamOvr==null?profile(s,Catalog.team(s),true).overall():f.teamOvr,f.opponent,f.opponentOvr==null?profile(s,f.opponent,false).overall():f.opponentOvr,chance(s,f));
    }
}
