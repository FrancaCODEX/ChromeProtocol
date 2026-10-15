package br.com.chromeprotocol;

import java.util.*;

/** Official reference names and local logos; contract terms and progression are simulated. */
public final class Catalog {
    private Catalog() {}
    public record Country(String id,String name,String region) {}
    public record Role(String id,String name,String attribute,List<String> agents) {}
    public record Organization(String id,String name,String region,int tier,int baseSalary,String color,String logo,boolean academy) {}
    public record Agent(String name,String role) {}
    public record Investment(String id,String name,String category,int price,int maxLevel,Map<String,Integer> bonuses,String extra) {}
    public record DevelopmentCard(String id,String name,String rarity,Map<String,Integer> bonuses,String detail) {}
    public record Offer(String id,String orgId,String name,int tier,int salary,int years,String position,int pressure,String detail) {}
    public static final Map<String,String> ATTRIBUTES = labels("aim","Mira","tactics","Leitura de jogo","communication","Comunicação","discipline","Disciplina","composure","Controle emocional");
    public static final Map<String,String> BACKGROUNDS = labels("ranked","Revelação da ranked","school","Equipe da escola","content","Criador de conteúdo");
    public static final List<Country> COUNTRIES = List.of(
        new Country("BR","Brasil","AMERICAS"),new Country("AR","Argentina","AMERICAS"),new Country("US","Estados Unidos","AMERICAS"),new Country("CA","Canadá","AMERICAS"),
        new Country("PT","Portugal","EMEA"),new Country("ES","Espanha","EMEA"),new Country("KR","Coreia do Sul","PACIFIC"),new Country("JP","Japão","PACIFIC"),new Country("CN","China","CN"));
    public static final List<Role> ROLES = List.of(
        new Role("duelist","Duelista","aim",List.of("Jett","Raze","Neon")),
        new Role("initiator","Iniciador","communication",List.of("Sova","Fade","Skye")),
        new Role("controller","Controlador","tactics",List.of("Omen","Viper","Brimstone")),
        new Role("sentinel","Sentinela","composure",List.of("Killjoy","Cypher","Sage")));
    public static final List<Agent> AGENTS = List.of(
        new Agent("Iso","duelist"),
        new Agent("Jett","duelist"),
        new Agent("Neon","duelist"),
        new Agent("Phoenix","duelist"),
        new Agent("Raze","duelist"),
        new Agent("Reyna","duelist"),
        new Agent("Waylay","duelist"),
        new Agent("Yoru","duelist"),
        new Agent("Breach","initiator"),
        new Agent("Fade","initiator"),
        new Agent("Gekko","initiator"),
        new Agent("KAY/O","initiator"),
        new Agent("Skye","initiator"),
        new Agent("Sova","initiator"),
        new Agent("Tejo","initiator"),
        new Agent("Astra","controller"),
        new Agent("Brimstone","controller"),
        new Agent("Clove","controller"),
        new Agent("Harbor","controller"),
        new Agent("Miks","controller"),
        new Agent("Omen","controller"),
        new Agent("Viper","controller"),
        new Agent("Chamber","sentinel"),
        new Agent("Cypher","sentinel"),
        new Agent("Deadlock","sentinel"),
        new Agent("Killjoy","sentinel"),
        new Agent("Sage","sentinel"),
        new Agent("Veto","sentinel"),
        new Agent("Vyse","sentinel"));
    public static final List<Organization> ORGS = List.of(
        new Organization("xilai-gaming","Xi Lai Gaming","CN",3,9500,"red","/teams/xilai-gaming.png",false),
        new Organization("geng-esports","Gen.G","PACIFIC",3,10250,"red","/teams/geng-esports.png",false),
        new Organization("dragon-ranger-gaming","DRAGON RANGER GAMING","CN",3,11000,"red","/teams/dragon-ranger-gaming.png",false),
        new Organization("team-heretics","Team Heretics","EMEA",3,11750,"red","/teams/team-heretics.png",false),
        new Organization("mibr","MIBR","AMERICAS",3,12500,"red","/teams/mibr.png",false),
        new Organization("trace-esports","Trace Esports","CN",3,9500,"red","/teams/trace-esports.png",false),
        new Organization("detonation-gaming","DetonatioN FocusMe","PACIFIC",3,10250,"red","/teams/detonation-gaming.png",false),
        new Organization("nova-esports","NOVA ESPORTS","CN",3,11000,"red","/teams/nova-esports.png",false),
        new Organization("g2-esports","G2 Esports","AMERICAS",3,11750,"red","/teams/g2-esports.png",false),
        new Organization("zeta-division","ZETA DIVISION","PACIFIC",3,12500,"red","/teams/zeta-division.png",false),
        new Organization("karmine-corp","Karmine Corp","EMEA",3,9500,"red","/teams/karmine-corp.png",false),
        new Organization("team-envy","ENVY","AMERICAS",3,10250,"red","/teams/team-envy.png",false),
        new Organization("rex-regum-qeon","Rex Regum Qeon","PACIFIC",3,11000,"red","/teams/rex-regum-qeon.png",false),
        new Organization("paper-rex","Paper Rex","PACIFIC",3,11750,"red","/teams/paper-rex.png",false),
        new Organization("bilibili-gaming","Bilibili Gaming","CN",3,12500,"red","/teams/bilibili-gaming.png",false),
        new Organization("furia","FURIA","AMERICAS",3,9500,"red","/teams/furia.png",false),
        new Organization("varrel","VARREL","PACIFIC",3,10250,"red","/teams/varrel.png",false),
        new Organization("all-gamers","ALL GAMERS","CN",3,11000,"red","/teams/all-gamers.png",false),
        new Organization("funplus-phoenix","FunPlus Phoenix","CN",3,11750,"red","/teams/funplus-phoenix.png",false),
        new Organization("100-thieves","100 Thieves","AMERICAS",3,12500,"red","/teams/100-thieves.png",false),
        new Organization("team-liquid","Team Liquid","EMEA",3,9500,"red","/teams/team-liquid.png",false),
        new Organization("full-sense","FULL SENSE","PACIFIC",3,10250,"red","/teams/full-sense.png",false),
        new Organization("loud","LOUD","AMERICAS",3,11000,"red","/teams/loud.png",false),
        new Organization("team-secret","Team Secret","PACIFIC",3,11750,"red","/teams/team-secret.png",false),
        new Organization("fnatic","FNATIC","EMEA",3,12500,"red","/teams/fnatic.png",false),
        new Organization("kru-visa","KRÜ Visa","AMERICAS",3,9500,"red","/teams/kru-visa.png",false),
        new Organization("cloud9","Cloud9","AMERICAS",3,10250,"red","/teams/cloud9.png",false),
        new Organization("jdg-esports","JD GAMING","CN",3,11000,"red","/teams/jdg-esports.png",false),
        new Organization("nrg","NRG","AMERICAS",3,11750,"red","/teams/nrg.png",false),
        new Organization("global-esports","GLOBAL ESPORTS","PACIFIC",3,12500,"red","/teams/global-esports.png",false),
        new Organization("wolves-esports","WOLVES ESPORTS","CN",3,9500,"red","/teams/wolves-esports.png",false),
        new Organization("leviatan-esports","Leviatán Esports","AMERICAS",3,10250,"red","/teams/leviatan-esports.png",false),
        new Organization("nongshim-redforce","NONGSHIM REDFORCE","PACIFIC",3,11000,"red","/teams/nongshim-redforce.png",false),
        new Organization("t1","T1","PACIFIC",3,11750,"red","/teams/t1.png",false),
        new Organization("pcific-esports","PCIFIC Esports","EMEA",3,12500,"red","/teams/pcific-esports.png",false),
        new Organization("bbl","BBL Esports","EMEA",3,9500,"red","/teams/bbl.png",false),
        new Organization("drx","Kiwoom DRX","PACIFIC",3,10250,"red","/teams/drx.png",false),
        new Organization("team-vitality","Team Vitality","EMEA",3,11750,"red","/teams/team-vitality.png",false),
        new Organization("evil-geniuses","Evil Geniuses","AMERICAS",3,12500,"red","/teams/evil-geniuses.png",false),
        new Organization("edward-gaming","EDWARD Gaming","CN",3,9500,"red","/teams/edward-gaming.png",false),
        new Organization("sentinels","Sentinels","AMERICAS",3,10250,"red","/teams/sentinels.png",false),
        new Organization("futbolist","FUT Esports","EMEA",3,11000,"red","/teams/futbolist.png",false),
        new Organization("tyloo-gaming","TYLOO GAMING","CN",3,11750,"red","/teams/tyloo-gaming.png",false),
        new Organization("titan-esports-club","TITAN ESPORTS CLUB","CN",3,12500,"red","/teams/titan-esports-club.png",false),
        new Organization("giants","GIANTX","EMEA",3,9500,"red","/teams/giants.png",false),
        new Organization("eternal-fire","Eternal Fire","EMEA",3,10250,"red","/teams/eternal-fire.png",false),
        new Organization("supra-beaux-gosses","Gentle Mates","EMEA",3,11000,"red","/teams/supra-beaux-gosses.png",false),
        new Organization("natus-vincere","NAVI","EMEA",3,11750,"red","/teams/natus-vincere.png",false),
        new Organization("furia-academy","FURIA Academy","AMERICAS",1,500,"blue","/teams/furia.png",true),
        new Organization("mibr-academy","MIBR Academy","AMERICAS",1,500,"teal","/teams/mibr.png",true),
        new Organization("red-canids","RED Canids Kalunga","AMERICAS",2,2600,"red","",false),
        new Organization("2game","2GAME","AMERICAS",2,2400,"blue","",false),
        new Organization("intz","INTZ","AMERICAS",2,2500,"teal","",false));
    public static final List<String> PHASES = List.of("Preparação","Peneiras","Contratos","Kickoff / Etapa 1","Masters I","Stage 1 / Etapa 2","Masters II","Evento inesperado","Stage 2","Playoffs regionais","Champions","Prêmios individuais","Balanço anual");
    public static final List<String> MONTHS = List.of("Janeiro","Janeiro","Janeiro","Janeiro–Fevereiro","Março","Abril–Maio","Junho","Junho–Julho","Julho–Agosto","Agosto–Setembro","Setembro–Outubro","Novembro","Dezembro");
    public static final int[] MONTH_START = {0,0,0,0,2,3,5,6,6,7,8,10,11,12};
    public static final List<String> MAPS = List.of("Ascent","Haven","Bind","Split","Lotus","Sunset");
    public static final List<Investment> INVESTMENTS=List.of(
        new Investment("aim-routine","Treino de mira","TREINAMENTO",800,5,bonus("aim",2),"Rotina permanente de fundamentos."),
        new Investment("coach","Coach particular","TREINAMENTO",1500,5,bonus("tactics",3,"communication",2),"Leitura e revisão de VODs."),
        new Investment("nutrition","Acompanhamento nutricional","SAÚDE",1000,3,bonus("discipline",2,"composure",1),"Reduz o desgaste de energia por decisão de mapa."),
        new Investment("psychology","Psicólogo esportivo","SAÚDE",1200,5,bonus("composure",3,"discipline",1),"Cada nível reduz o estresse adicional por falha."),
        new Investment("mouse","Mouse e mousepad","EQUIPAMENTO",650,3,bonus("aim",2,"tactics",1),"Controle e consistência mecânica."),
        new Investment("monitor","Monitor de alta frequência","EQUIPAMENTO",1100,3,bonus("aim",3),"Percepção e precisão."),
        new Investment("pc","Setup de performance","EQUIPAMENTO",2500,3,bonus("aim",2,"tactics",2),"Estabilidade para treino e competição."),
        new Investment("bootcamp","Bootcamp especializado","TREINAMENTO",3000,4,bonus("aim",2,"tactics",2,"communication",2,"discipline",2,"composure",2),"Evolução completa e permanente."),
        new Investment("languages","Idiomas e comunicação","CARREIRA",900,4,bonus("communication",3),"Mais repertório para liderar e jogar fora."),
        new Investment("fitness","Preparação física","SAÚDE",1100,5,bonus("discipline",2,"composure",2),"Cada nível melhora a recuperação mensal de energia.")
    );
    public static final List<DevelopmentCard> CARDS=List.of(
        new DevelopmentCard("precision","Precisão de outro nível","ÉPICA",bonus("aim",7,"tactics",2),"Mecânica que acompanha toda a carreira."),
        new DevelopmentCard("captain","Voz de capitão","ÉPICA",bonus("communication",7,"tactics",3),"Liderança para os rounds mais difíceis."),
        new DevelopmentCard("ice","Sangue frio","ÉPICA",bonus("composure",8,"discipline",3),"Controle emocional que não expira."),
        new DevelopmentCard("routine","Rotina de campeão","ÉPICA",bonus("discipline",7,"aim",3),"Fundamentos construídos para durar."),
        new DevelopmentCard("meta","Uma jogada à frente","ÉPICA",bonus("tactics",8,"communication",2),"Leitura para antecipar o adversário."),
        new DevelopmentCard("phenom","Talento inesquecível","LENDÁRIA",bonus("aim",4,"tactics",4,"communication",4,"discipline",4,"composure",4),"Um salto em todos os fundamentos.")
    );
    public static Map<String,Integer> bonus(Object... values){var map=new LinkedHashMap<String,Integer>();for(int i=0;i<values.length;i+=2)map.put((String)values[i],(Integer)values[i+1]);return Collections.unmodifiableMap(map);}
    static Map<String,String> labels(String... values) { var map=new LinkedHashMap<String,String>();for(int i=0;i<values.length;i+=2)map.put(values[i],values[i+1]);return Collections.unmodifiableMap(map); }
    public static Country country(String id) { return COUNTRIES.stream().filter(c->c.id().equals(id)).findFirst().orElseThrow(()->new IllegalArgumentException("Escolha uma nacionalidade válida.")); }
    public static Role role(String id) { return ROLES.stream().filter(r->r.id().equals(id)).findFirst().orElseThrow(()->new IllegalArgumentException("Escolha uma função válida.")); }
    public static int overall(GameState s) { return (int)Math.round(s.attributes.values().stream().mapToInt(Integer::intValue).average().orElse(0)); }
    public static String circuit(GameState s) { int tier=s.contract==null?1:s.contract.tier();return tier==3?"VCT "+Calendar.regionName(Calendar.region(s)):tier==2||Calendar.academy(s)?Calendar.challengers(s):"Premier / Base"; }
    public static String team(GameState s) { return s.contract==null?"Mix "+country(s.nationality).name():s.contract.name(); }
    public static final int STARTER_OVR = 80;
    /** Reconcile older saves and promote on permanent growth without changing pay or term. */
    public static void ensureStarter(GameState s) {
        var old=s.contract;
        if(old!=null && s.match==null && s.currentSeries==null && overall(s)>=STARTER_OVR && old.position().equals("Reserva")) {
            s.contract=new GameState.Contract(old.orgId(),old.name(),old.tier(),old.salary(),old.endYear(),"Titular",old.pressure());
            // Scrims stay in history, but cannot seed the official table or earn a trophy.
            var previous=s.campaigns.remove(s.phase);
            if(previous!=null) {
                var official=Tour.progress(s);
                official.seriesOffset=previous.seriesOffset+previous.played;
                s.campaigns.put(s.phase,official);
            }
        }
    }
    public static List<Offer> offers(GameState s) {
        String region=country(s.nationality).region();
        int eligible=s.age>=18 && s.reputation>=40 && overall(s)>=45 ? 3 : s.age>=16 && s.reputation>=14 ? 2 : 1;
        var pool=new ArrayList<>(ORGS.stream().filter(o->o.tier()==eligible).toList());
        pool.sort(Comparator.comparingInt((Organization o)->o.region().equals(region)?0:1).thenComparing(Organization::id));
        var local=pool.stream().filter(o->o.region().equals(region)).toList();
        if(local.size()>=3)pool=new ArrayList<>(local);
        Collections.rotate(pool,-Math.floorMod(s.year-2026,pool.size()));
        var result=new ArrayList<Offer>();
        for(int i=0;i<3;i++) {
            Organization o=pool.get(i%pool.size()); boolean reserve=i==2 && overall(s)<STARTER_OVR;
            int salary=o.baseSalary()+s.reputation*22+(reserve?o.baseSalary()/3:0);
            int years=i==1?2:1;
            result.add(new Offer("offer-"+i,o.id(),o.name(),o.tier(),salary,years,reserve?"Reserva":"Titular",o.tier()*12+(reserve?5:0),
                (o.region().equals(region)?"Na sua região":"Mudança de região")+" · "+(reserve?"Mais renda; scrims e menor exposição":"Vaga no quinteto e partidas do circuito")));
        }
        var old=s.expiredContract;
        if(old!=null && old.endYear()==s.year-1 && s.contract==null) {
            String position=overall(s)>=STARTER_OVR?"Titular":old.position();
            int salary=Math.max(old.salary(),ORGS.stream().filter(o->o.id().equals(old.orgId())).mapToInt(Organization::baseSalary).findFirst().orElse(0)+s.reputation*22);
            result.add(0,new Offer("renew",old.orgId(),old.name(),old.tier(),salary,1,position,old.pressure(),"Renovação · mais uma temporada com o mesmo clube · sem multa"));
        }
        return List.copyOf(result);
    }
}
