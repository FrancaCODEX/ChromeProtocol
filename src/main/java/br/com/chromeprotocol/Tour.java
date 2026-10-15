package br.com.chromeprotocol;

import java.util.*;

/** A fictional player path inspired by 2026. Other teams' brackets are simplified, not live fixtures. */
public final class Tour {
    private Tour() {}
    public record Standing(String team,int played,int wins,int losses,boolean player) {}
    public record Agenda(String competition,String round,int completed,int bestOf,String opponent,String critical,List<Standing> standings,String format) {}
    static boolean elite(GameState s){return Calendar.tier(s)==3&&!Calendar.reserve(s);}
    static int quota(GameState s){return s.phase==8?10:8;}
    static GameState.Campaign progress(GameState s){
        var existing=s.campaigns.get(s.phase);if(existing!=null)return existing;
        var c=new GameState.Campaign();
        c.part=elite(s)?switch(s.phase){case 3->"kickoff";case 5,8->"groups";case 4,6,10->"qualifier";default->"bracket";}:"league";
        if(s.phase==9){c.seed=s.campaigns.containsKey(8)?s.campaigns.get(8).seed:2;seedBracket(c);}
        if((s.phase==4&&s.campaigns.containsKey(3)&&s.campaigns.get(3).champion)||(s.phase==6&&s.campaigns.containsKey(5)&&s.campaigns.get(5).champion))c.part="bracket";
        return c;
    }
    static void seedBracket(GameState.Campaign c){if(c.seed==1)c.upperWins=1;if(c.seed==4){c.bracketLosses=1;c.lowerRemaining=4;}}
    static List<String> pool(GameState s){
        boolean global=Set.of(4,6,10).contains(s.phase);
        var names=new ArrayList<>(Catalog.ORGS.stream().filter(o->!o.name().equals(Catalog.team(s))&&(elite(s)?o.tier()==3&&(global||o.region().equals(Calendar.region(s))):o.tier()<3&&o.region().equals(Calendar.region(s)))).map(Catalog.Organization::name).toList());
        if(s.contract==null){names.clear();names.addAll(List.of("Mix Aurora","Mix Horizonte","Mix Nexus","Mix Impacto","Mix Eclipse","Mix Vanguard"));}
        for(int i=names.size();i<5;i++)names.add("Mix regional "+(i+1));
        Collections.shuffle(names,new Random(Objects.hash(s.careerSeed,s.year,s.phase,"pool")));return names;
    }
    // Six-team single round robin. Other matches are deterministic and only counted after their round.
    static List<int[]> pairs(int round){var circle=new ArrayList<>(List.of(0,1,2,3,4,5));for(int i=0;i<round;i++)circle.add(1,circle.remove(5));return List.of(new int[]{circle.get(0),circle.get(5)},new int[]{circle.get(1),circle.get(4)},new int[]{circle.get(2),circle.get(3)});}
    static int groupOpponent(int round){for(var p:pairs(round))if(p[0]==0||p[1]==0)return p[0]+p[1];throw new IllegalStateException();}
    static List<Standing> standings(GameState s,GameState.Campaign c){
        var names=new ArrayList<String>();names.add(Catalog.team(s));names.addAll(pool(s).subList(0,5));int[] wins=new int[6],played=new int[6];
        var results=s.series.stream().filter(r->r.year()==s.year&&r.phase()==s.phase&&r.round().startsWith("Grupos")).sorted(Comparator.comparing( GameState.SeriesResult::id)).toList();
        for(int round=0;round<Math.min(5,c.groupWins+c.groupLosses);round++)for(var pair:pairs(round)){
            int a=pair[0],b=pair[1];played[a]++;played[b]++;
            if(a==0||b==0){String opponent=names.get(a+b);boolean won=results.stream().filter(r->r.opponent().equals(opponent)).findFirst().map(GameState.SeriesResult::won).orElse(false);wins[won?0:a+b]++;}
            else wins[Math.floorMod(Objects.hash(s.careerSeed,s.year,s.phase,round,a,b),2)==0?a:b]++;
        }
        var rows=new ArrayList<Standing>();for(int i=0;i<6;i++)rows.add(new Standing(names.get(i),played[i],wins[i],played[i]-wins[i],i==0));
        rows.sort(Comparator.comparingInt(Standing::wins).reversed().thenComparingInt(Standing::losses).thenComparingInt(r->Math.floorMod(Objects.hash(s.careerSeed,r.team()),10000)));return rows;
    }
    static String round(GameState s,GameState.Campaign c){return switch(c.part){
        case "league"->"Rodada "+(c.played+1)+" / "+quota(s);
        case "kickoff"->"Kickoff · "+c.wins+"V / "+c.losses+"D";
        case "groups"->"Grupos · rodada "+(c.groupWins+c.groupLosses+1)+" / 5";
        case "qualifier"->(s.phase==10?"Grupos GSL":"Fase suíça")+" · "+c.wins+"V / "+c.losses+"D";
        default->c.upperWins>=3||c.bracketLosses==1&&c.lowerRemaining==0?"Grande final":c.bracketLosses==1?(c.lowerRemaining==1?"Final inferior":"Chave inferior · sobrevivência"):c.upperWins==2?"Final superior":c.upperWins==1?"Semifinal superior":"Quartas superior";
    };}
    static boolean grandFinal(GameState.Campaign c){return c.part.equals("bracket")&&(c.upperWins>=3||c.bracketLosses==1&&c.lowerRemaining==0);}
    static String critical(GameState s,GameState.Campaign c){
        if(s.seasonSeriesWins+s.seasonSeriesLosses==0)return "Estreia da temporada";
        return switch(c.part){
            case "league"->c.played==quota(s)-1?"Encerramento da etapa": "";
            case "kickoff"->c.wins==2?"Vaga no Masters em jogo":c.losses==2?"Risco de eliminação":"";
            case "groups"->c.groupWins+c.groupLosses==4?"Última rodada: classificação aos playoffs":"";
            case "qualifier"->c.losses==1?"Risco de eliminação":c.wins==1?"Vaga nos playoffs":"";
            default->grandFinal(c)?"Decisão de título":c.bracketLosses==1?"Perdeu, está fora":c.upperWins>=1?"Vaga na próxima fase":"";
        };
    }
    static GameState.Series fixture(GameState s){
        if(s.currentSeries!=null)return s.currentSeries;var c=progress(s);var f=new GameState.Series();f.year=s.year;f.phase=s.phase;f.id=s.year+":"+s.phase+":"+String.format(Locale.ROOT,"%02d",c.played);f.competition=Calendar.reserve(s)?"Scrim — "+Calendar.competition(s):Calendar.competition(s);f.round=round(s,c);f.critical=critical(s,c);f.official=!Calendar.reserve(s);
        var names=pool(s);f.opponent=c.part.equals("groups")?names.get(groupOpponent(c.groupWins+c.groupLosses)-1):names.get(c.played%names.size());
        f.bestOf=grandFinal(c)||c.part.equals("bracket")&&c.bracketLosses==1&&c.lowerRemaining==1?5:3;return f;
    }
    public static Agenda agenda(GameState s){if(!Calendar.match(s)||s.legacyMap||s.pendingCards||s.ended)return null;var c=progress(s);var f=fixture(s);return new Agenda(f.competition,f.round,c.played,f.bestOf,f.opponent,f.critical,c.part.equals("groups")?standings(s,c):List.of(),elite(s)?"Trajetória simulada: grupos e chaves com eliminação; vagas dependem dos resultados.":"Calendário de jogo: 26 séries em três etapas (8 + 8 + 10). Não reproduz a tabela oficial do Challengers.");}
    /** Called only after a complete series has been recorded. */
    static void result(GameState s,boolean won){
        var c=s.campaigns.computeIfAbsent(s.phase,k->progress(s));c.played++;if(won)c.wins++;else c.losses++;
        switch(c.part){
            case "league"->{c.done=c.played>=quota(s);c.champion=c.done&&won&&c.wins>=Math.ceil(quota(s)*.75);}
            case "kickoff"->{c.done=c.wins==3||c.losses==3;c.qualified=c.wins==3;c.champion=c.done&&c.losses==0;}
            case "groups"->{if(won)c.groupWins++;else c.groupLosses++;if(c.groupWins+c.groupLosses==5){var rows=standings(s,c);c.seed=1;for(int i=0;i<rows.size();i++)if(rows.get(i).player())c.seed=i+1;c.qualified=c.seed<=4;if(s.phase==8||!c.qualified)c.done=true;else{c.part="bracket";c.qualified=false;seedBracket(c);}}}
            case "qualifier"->{if(c.wins==2){c.part="bracket";c.wins=0;c.losses=0;}else if(c.losses==2)c.done=true;}
            default->{if(grandFinal(c)){c.done=true;c.champion=won;c.qualified=true;}else if(!won){c.bracketLosses++;if(c.bracketLosses>=2)c.done=true;else c.lowerRemaining=c.upperWins==0?4:c.upperWins==1?3:1;}else if(c.bracketLosses==1)c.lowerRemaining--;else c.upperWins++;if(c.upperWins>=2||c.bracketLosses==1&&c.lowerRemaining<=1)c.qualified=true;}
        }
        if(Calendar.reserve(s))return;
        switch(s.phase){case 3->s.qualifiedMasters1=elite(s)&&c.qualified;case 5->s.qualifiedMasters2=elite(s)&&c.qualified;case 8->s.qualifiedPlayoffs=elite(s)&&c.qualified;case 9->s.qualifiedChampions=elite(s)&&c.qualified&&s.age>=18;default->{}}
    }
}
