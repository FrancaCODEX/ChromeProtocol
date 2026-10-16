package br.com.chromeprotocol;

import java.util.*;

/** Read-only achievements derived from recorded official results; reloading never grants points. */
public final class CareerHonors {
    private CareerHonors() {}
    public record ClubLegacy(String orgId,String name,int seasons,int wins,int titles,int finals,int points,String status,int nextThreshold) {}
    public record SeasonFinal(int year,String competition,String team,String opponent,int wins,int losses,boolean won) {}

    public static List<SeasonFinal> finals(GameState s) {
        return s.series.stream().filter(r->r.official()&&"Grande final".equals(r.round()))
            .map(r->new SeasonFinal(r.year(),r.competition(),r.team(),r.opponent(),r.wins(),r.losses(),r.won())).toList();
    }
    public static List<ClubLegacy> legacy(GameState s) {
        var clubs=new LinkedHashSet<String>();
        if(s.contract!=null)clubs.add(s.contract.name());
        s.seasons.forEach(y->clubs.add(y.organization()));
        s.series.stream().filter(GameState.SeriesResult::official).forEach(r->clubs.add(r.team()));
        s.trophies.forEach(t->clubs.add(t.organization()));
        var result=new ArrayList<ClubLegacy>();
        for(String name:clubs){
            var org=Catalog.ORGS.stream().filter(o->o.name().equals(name)).findFirst().orElse(null);
            if(org==null)continue; // A mix has no organization-specific idol status.
            var years=new HashSet<Integer>();
            s.seasons.stream().filter(y->name.equals(y.organization())).forEach(y->years.add(y.year()));
            var matches=s.series.stream().filter(r->r.official()&&name.equals(r.team())).toList();
            matches.forEach(r->years.add(r.year()));
            int wins=(int)matches.stream().filter(GameState.SeriesResult::won).count();
            int titles=(int)s.trophies.stream().filter(t->name.equals(t.organization())).count();
            int finals=(int)matches.stream().filter(r->"Grande final".equals(r.round())).count();
            int points=years.size()*40+wins*2+titles*120+finals*30;
            String status=points>=1200?"Lenda":points>=650?"Ídolo":points>=300?"Referência":points>=100?"Nome da torcida":"Promessa";
            int next=points>=1200?0:points>=650?1200:points>=300?650:points>=100?300:100;
            result.add(new ClubLegacy(org.id(),name,years.size(),wins,titles,finals,points,status,next));
        }
        return List.copyOf(result);
    }
    public static boolean rookieEligible(GameState s) {
        restoreDebut(s);
        return s.age<=19||Objects.equals(s.tierOneDebutYear,s.year);
    }
    public static void restoreDebut(GameState s) {
        if(s.tierOneDebutYear!=null)return;
        s.tierOneDebutYear=s.series.stream().filter(r->r.official()&&isTierOne(r.competition()))
            .mapToInt(GameState.SeriesResult::year).min().stream().boxed().findFirst().orElse(null);
    }
    private static boolean isTierOne(String competition) {
        String c=competition.toLowerCase(Locale.ROOT);
        return !c.contains("ascension")&&(c.startsWith("vct ")||c.contains("masters")||c.contains("champions"));
    }
}
