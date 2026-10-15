package br.com.chromeprotocol;

/** 2026 competition names; the player path is simulated in Tour, not live fixtures. */
public final class Calendar {
    private Calendar() {}
    public static int tier(GameState s){return s.contract==null?1:s.vctPromotions.getOrDefault(s.contract.orgId(),Integer.MAX_VALUE)<=s.year?3:s.contract.tier();}
    public static String region(GameState s){
        if(s.contract!=null)return Catalog.ORGS.stream().filter(o->o.id().equals(s.contract.orgId())).findFirst().map(Catalog.Organization::region).orElse(Catalog.country(s.nationality).region());
        return Catalog.country(s.nationality).region();
    }
    public static String regionName(String region){return switch(region){case "AMERICAS"->"Americas";case "EMEA"->"EMEA";case "CN"->"CN";default->"Pacific";};}
    public static String challengers(GameState s){if(s.contract!=null&&java.util.Set.of("furia-academy","mibr-academy","red-canids","2game","intz").contains(s.contract.orgId()))return "VCB — Gamers Club Challengers Brazil";return switch(region(s)){case "EMEA"->"VALORANT Challengers EMEA";case "PACIFIC"->"VALORANT Challengers "+(s.nationality.equals("JP")?"Japan":"Korea");case "CN"->"VCT CN Qualifier";default->s.nationality.equals("BR")?"VCB — Gamers Club Challengers Brazil":"VALORANT Challengers "+(s.nationality.equals("AR")?"LATAM":"North America");};}
    public static boolean reserve(GameState s){return s.contract!=null&&s.contract.position().equals("Reserva");}
    public static boolean academy(GameState s){return s.contract!=null&&Catalog.ORGS.stream().anyMatch(o->o.id().equals(s.contract.orgId())&&o.academy());}
    public static String competition(GameState s){
        String vct="VCT "+regionName(region(s));
        return switch(s.phase){
            case 3 -> tier(s)==3?vct+" Kickoff":tier(s)==2||academy(s)?challengers(s)+" — Etapa 1":"Premier — Split 1";
            case 4 -> s.year==2026?"VALORANT Masters Santiago":"VALORANT Masters I";
            case 5 -> tier(s)==3?vct+" Stage 1":tier(s)==2||academy(s)?challengers(s)+" — Etapa 2":"Premier — Split 2";
            case 6 -> s.year==2026?"VALORANT Masters London":"VALORANT Masters II";
            case 8 -> tier(s)==3?vct+" Stage 2":tier(s)==2||academy(s)?challengers(s)+" — Playoffs":"Premier — Split 3";
            case 9 -> tier(s)==3?vct+" — Playoffs Stage 2":"VCT Ascension "+regionName(region(s));
            case 10 -> s.year==2026?"VALORANT Champions Shanghai":"VALORANT Champions";
            default -> Catalog.PHASES.get(s.phase);
        };
    }
    public static boolean match(GameState s){return s.legacyMap&&s.match!=null||switch(s.phase){case 3,5,8->true;case 4->s.qualifiedMasters1;case 6->s.qualifiedMasters2;case 9->tier(s)==3?s.qualifiedPlayoffs:s.qualifiedAscension;case 10->s.qualifiedChampions;default->false;};}
    public static boolean trophy(GameState s){return !reserve(s)&&switch(s.phase){case 3,4,5,6,10->true;case 8->tier(s)<=2;case 9->tier(s)==3?s.qualifiedPlayoffs:s.qualifiedAscension;default->false;};}
    public static void qualification(GameState s,boolean won){if(reserve(s))return;switch(s.phase){case 3->s.qualifiedMasters1=won&&tier(s)==3;case 5->s.qualifiedMasters2=won&&tier(s)==3;case 8->{s.qualifiedPlayoffs=won&&tier(s)==3;s.qualifiedAscension=won&&tier(s)<3&&s.contract!=null&&!academy(s);}case 9->{if(tier(s)==3)s.qualifiedChampions=won&&s.qualifiedPlayoffs;else if(won&&s.qualifiedAscension)s.vctPromotions.put(s.contract.orgId(),s.year+1);}default->{}}}
}
