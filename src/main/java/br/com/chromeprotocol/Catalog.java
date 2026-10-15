package br.com.chromeprotocol;

import java.util.*;

/** The calendar, contracts, academy names and financial values are fictional simulation content. */
public final class Catalog {
    private Catalog() {}
    public record Country(String id,String name,String region) {}
    public record Role(String id,String name,String attribute,List<String> agents) {}
    public record Organization(String id,String name,String region,int tier,int baseSalary,String color) {}
    public record Offer(String id,String orgId,String name,int tier,int salary,int years,String position,int pressure,String detail) {}
    public static final Map<String,String> ATTRIBUTES = labels("aim","Mira","tactics","Leitura de jogo","communication","Comunicação","discipline","Disciplina","composure","Controle emocional");
    public static final Map<String,String> BACKGROUNDS = labels("ranked","Revelação da ranked","school","Equipe da escola","content","Criador de conteúdo");
    public static final List<Country> COUNTRIES = List.of(
        new Country("BR","Brasil","AMERICAS"),new Country("AR","Argentina","AMERICAS"),new Country("US","Estados Unidos","AMERICAS"),new Country("CA","Canadá","AMERICAS"),
        new Country("PT","Portugal","EMEA"),new Country("ES","Espanha","EMEA"),new Country("KR","Coreia do Sul","PACIFIC"),new Country("JP","Japão","PACIFIC"));
    public static final List<Role> ROLES = List.of(
        new Role("duelist","Duelista","aim",List.of("Jett","Raze","Neon")),
        new Role("initiator","Iniciador","communication",List.of("Sova","Fade","Skye")),
        new Role("controller","Controlador","tactics",List.of("Omen","Viper","Brimstone")),
        new Role("sentinel","Sentinela","composure",List.of("Killjoy","Cypher","Sage")));
    public static final List<Organization> ORGS = List.of(
        new Organization("aurora","Aurora Academy","AMERICAS",1,450,"teal"),new Organization("atlas","Atlas Academy","EMEA",1,500,"blue"),new Organization("pulse","Pulse Academy","PACIFIC",1,480,"pink"),
        new Organization("nexa","Nexa Esports","AMERICAS",2,2400,"blue"),new Organization("vertex","Vertex Gaming","EMEA",2,2600,"pink"),new Organization("orbit","Orbit Five","PACIFIC",2,2500,"teal"),
        new Organization("loud","LOUD","AMERICAS",3,10500,"green"),new Organization("furia","FURIA","AMERICAS",3,9500,"blue"),new Organization("mibr","MIBR","AMERICAS",3,10000,"teal"),new Organization("sentinels","Sentinels","AMERICAS",3,12500,"red"));
    public static final List<String> PHASES = List.of("Preparação","Peneiras","Contratos","Etapa 1","Fora do servidor","Etapa 2","Decisão da temporada","Balanço anual");
    public static final List<String> MONTHS = List.of("Janeiro","Fevereiro","Março","Abril–Junho","Julho","Agosto–Outubro","Novembro","Dezembro");
    public static final int[] MONTH_START = {0,1,2,3,6,7,10,11,12};
    public static final List<String> MAPS = List.of("Ascent","Haven","Bind","Split","Lotus","Sunset");
    static Map<String,String> labels(String... values) { var map=new LinkedHashMap<String,String>();for(int i=0;i<values.length;i+=2)map.put(values[i],values[i+1]);return Collections.unmodifiableMap(map); }
    public static Country country(String id) { return COUNTRIES.stream().filter(c->c.id().equals(id)).findFirst().orElseThrow(()->new IllegalArgumentException("Escolha uma nacionalidade válida.")); }
    public static Role role(String id) { return ROLES.stream().filter(r->r.id().equals(id)).findFirst().orElseThrow(()->new IllegalArgumentException("Escolha uma função válida.")); }
    public static int overall(GameState s) { return (int)Math.round(s.attributes.values().stream().mapToInt(Integer::intValue).average().orElse(0)); }
    public static String circuit(GameState s) { int tier=s.contract==null?1:s.contract.tier();return tier==3?"Circuito Elite":tier==2?"Challengers simulado":"Circuito de Base"; }
    public static String team(GameState s) { return s.contract==null?"Mix "+country(s.nationality).name():s.contract.name(); }
    public static List<Offer> offers(GameState s) {
        String region=country(s.nationality).region();
        int eligible=s.age>=18 && s.reputation>=55 && overall(s)>=48 ? 3 : s.age>=17 && s.reputation>=18 ? 2 : 1;
        var pool=new ArrayList<>(ORGS.stream().filter(o->o.tier()==eligible).toList());
        pool.sort(Comparator.comparingInt((Organization o)->o.region().equals(region)?0:1).thenComparing(Organization::id));
        // Rotate the market each season while keeping GET requests deterministic.
        Collections.rotate(pool,-Math.floorMod(s.year-2026,pool.size()));
        var result=new ArrayList<Offer>();
        for(int i=0;i<3;i++) {
            Organization o=pool.get(i%pool.size()); boolean reserve=i==2;
            int salary=o.baseSalary()+s.reputation*22+(reserve?o.baseSalary()/3:0);
            int years=i==1?2:1;
            result.add(new Offer("offer-"+i,o.id(),o.name(),o.tier(),salary,years,reserve?"Reserva":"Titular",o.tier()*12+(reserve?5:0),
                (o.region().equals(region)?"Na sua região":"Mudança de região")+" · "+(reserve?"Mais renda; scrims e menor exposição":"Vaga no quinteto e partidas do circuito")));
        }
        return List.copyOf(result);
    }
}
