package br.com.chromeprotocol;

import java.util.*;
import java.util.random.RandomGenerator;

/** A finite 21-season career with persistent, mandatory result acknowledgement. */
public final class GameEngine {
    private final RandomGenerator random;
    public GameEngine(RandomGenerator random) { this.random=Objects.requireNonNull(random); }
    public record Action(String type,String target,int revision) {}
    public record Choice(String id,String title,String description,String attribute,int chance,String consequence,boolean available) {}
    public record Scene(String tag,String title,String description,String date,List<Choice> choices) {}
    public record View(GameState character,Scene scene,List<Catalog.Offer> offers,Map<String,String> attributes,List<String> phases,int overall,String team,String circuit) {}
    public GameState create(String name,String nationality,String role,String background,String agent,Appearance appearance) {
        require(name!=null&&!name.isBlank()&&name.strip().length()<=24&&name.codePoints().noneMatch(Character::isISOControl),"Use um nick de 1 a 24 caracteres.");
        Catalog.country(nationality);Catalog.Role selected=Catalog.role(role);
        require(Catalog.BACKGROUNDS.containsKey(background),"Escolha uma trajetória válida.");
        require(selected.agents().contains(agent),"Escolha um agente da sua função.");
        GameState s=new GameState();s.name=name.strip();s.nationality=nationality;s.role=role;s.background=background;s.agent=agent;s.appearance=appearance==null?Appearance.defaults():appearance;
        Catalog.ATTRIBUTES.keySet().forEach(k->s.attributes.put(k,30));s.attributes.merge(selected.attribute(),7,Integer::sum);
        s.attributes.merge(background.equals("ranked")?"aim":background.equals("school")?"discipline":"communication",5,Integer::sum);
        if(background.equals("content"))s.followers=600;
        return s;
    }
    public void customize(GameState s,Appearance appearance,int revision) { require(revision==s.revision,"Sua carreira mudou. Atualize a página.");require(appearance!=null,"Escolha um visual.");s.appearance=appearance;s.revision++; }
    public View view(GameState s) {
        return new View(s,s.ended?null:scene(s),s.phase==2&&s.contract==null?Catalog.offers(s):List.of(),Catalog.ATTRIBUTES,Catalog.PHASES,Catalog.overall(s),Catalog.team(s),Catalog.circuit(s));
    }
    private Choice option(GameState s,String id,String title,String description,String attr,int difficulty,String consequence) {
        int odds=attr.isEmpty()?100:clamp(51+s.attributes.get(attr)/2-difficulty+(s.energy-50)/12+(s.form-50)/15-s.stress/15+(s.chemistry-50)/15,20,94);
        if(isMatch(s)&&s.match!=null)odds=clamp(odds+(s.match.roundsWon*2)+(s.contract!=null&&s.contract.position().equals("Reserva")?-4:0),20,94);
        return new Choice(id,title,description,attr,odds,consequence,true);
    }
    private Scene scene(GameState s) {
        var c=new ArrayList<Choice>();String title,description,tag;
        switch(s.phase) {
            case 0 -> { tag="PRÉ-TEMPORADA";title=s.age==15?"Toda carreira começa no quarto.":"Um novo ano. O mesmo sonho.";
                description="Você tem "+s.age+" anos. "+(s.age==15?"Entre a escola e as primeiras filas ranqueadas, nasce a ideia de viver de Valorant.":"O mercado está observando. Sua preparação vai definir como você chega à nova temporada.");
                c.add(option(s,"prep-aim","Focar na mecânica","Rotina de mira e duelos individuais.","",0,"Mira +4 · energia −12 · estresse +7"));
                c.add(option(s,"prep-study","Estudar mapas e rotinas","Analisar VODs e organizar treino, estudo e sono.","",0,"Leitura +3 · disciplina +3 · energia +10"));
                c.add(option(s,"prep-team","Treinar comunicação","Montar um grupo fixo e praticar chamadas.","",0,"Comunicação +4 · química +8")); }
            case 1 -> {tag="PENEIRA";title=s.age<18?"Um olheiro entrou na sala.":"Seu nome chegou à comissão técnica.";
                description="Uma comissão acompanha seu teste. Não basta acertar tiros: o time quer saber como você reage sob pressão. Sua reputação e seus atributos definem o nível das propostas em março.";
                c.add(option(s,"trial-highlight","Buscar o highlight","Assumir duelos difíceis para chamar atenção.","aim",10,"Sucesso: reputação +12 · risco de perder confiança"));
                c.add(option(s,"trial-team","Jogar pelo coletivo","Comunicar informações e executar o plano do time.","communication",2,"Sucesso: reputação +10 · química +8"));
                c.add(option(s,"trial-steady","Mostrar consistência","Priorizar fundamentos e manter uma rotina sustentável.","",0,"Reputação +4 · disciplina +2 · estresse −5")); }
            case 2 -> {tag="MERCADO";title=s.contract==null?"Seu próximo escudo.":"Um contrato também é uma escolha.";
                description=s.contract==null?"As propostas desta janela estão na mesa. Compare salário mensal, duração e espaço para jogar. Os valores e calendários pertencem à simulação.":"Você tem contrato com "+s.contract.name()+" até dezembro de "+s.contract.endYear()+". Honre o acordo, tente renegociar ou pague a liberação para voltar ao mercado.";
                if(s.contract==null){ for(Catalog.Offer o:Catalog.offers(s))c.add(option(s,o.id(),"Assinar com "+o.name(),o.position()+" · R$ "+o.salary()+"/mês · "+o.years()+" temporada(s)","",0,o.detail()));c.add(option(s,"free-agent","Continuar sem organização","Disputar o circuito com um mix e manter liberdade.","",0,"Sem salário fixo · exposição por conta própria")); }
                else {c.add(option(s,"honor","Cumprir o contrato","Manter a rotina e fortalecer sua posição na equipe.","",0,"Química +10 · controle emocional +2"));c.add(option(s,"renegotiate","Pedir uma valorização","Negociar aumento sem trocar de organização.","communication",12,"Sucesso: salário +15% · falha: pressão +8"));int fee=s.contract.salary()*2;c.add(new Choice("release","Pedir liberação","Pagar R$ "+fee+" para encerrar o vínculo e ouvir novas propostas.","",100,"Custo da rescisão · permanece na janela",s.money>=fee));} }
            case 4 -> {tag="FORA DO SERVIDOR";String[][] events={{"Uma marca quer o seu rosto.","Uma marca de periféricos propõe uma campanha durante a pausa."},{"A torcida começou a cobrar.","Um corte da sua última partida viralizou. A internet exige uma resposta."},{"O grupo está rachado.","Dois colegas discordam das chamadas. A comissão quer saber sua posição."},{"O corpo pediu uma pausa.","A rotina apertou. Você precisa decidir onde colocar sua energia."},{"Uma entrevista pode mudar tudo.","Uma entrevista ao vivo pode levar sua história para mais gente."},{"A vida acontece fora do jogo.","Amigos e família sentem sua ausência. Existe uma pausa entre as etapas."}};
                String[] event=events[Math.floorMod(s.year-2026,events.length)];title=event[0];description=event[1]+" A escolha afeta sua imagem, seu grupo e como você chega à próxima partida.";
                c.add(option(s,"life-brand","Investir na sua imagem","Produzir conteúdo e aproveitar a oportunidade pública.","communication",5,"Renda e seguidores · menos energia · mais estresse"));
                c.add(option(s,"life-team","Conversar com a equipe","Alinhar expectativas sem expor os colegas.","",0,"Química +12 · comunicação +2 · estresse −5"));
                c.add(option(s,"life-rest","Priorizar sua vida pessoal","Reservar a pausa para estudo, família e descanso.","",0,"Energia +35 · estresse −20 · disciplina +2")); }
            case 7 -> {tag="FIM DA TEMPORADA";title="O que fica deste ano?";description="A temporada termina com "+s.seasonWins+" vitória(s) e "+s.seasonLosses+" derrota(s). "+(s.age>=35?"Você chegou à última temporada desta carreira. Escolha como encerrar seu ciclo.":"Antes de completar "+(s.age+1)+" anos, escolha como quer atravessar a pausa.");
                c.add(option(s,"year-develop","Voltar mais completo","Treinar seus fundamentos com acompanhamento.","",0,"Todos os atributos +2 · energia recuperada"));
                c.add(option(s,"year-brand","Construir sua marca","Trabalhar conteúdo, contatos e projetos pessoais.","",0,"Seguidores +500 · renda extra · comunicação +2"));
                c.add(option(s,"year-rest","Desligar e recuperar","Passar a pausa longe da competição.","",0,"Energia 100 · estresse −35 · controle emocional +3"));
                if(s.age>=23)c.add(option(s,"retire","Encerrar a carreira","Escolher a hora de sair e guardar o seu legado.","",0,"Aposentadoria · encerra esta carreira")); }
            default -> { tag="MATCH DAY";GameState.Match m=matchFor(s);title=m.competition;description=Catalog.team(s)+" x "+m.opponent+" · "+m.map+". "+List.of("O adversário muda o ritmo no primeiro tempo. Como você responde?","O jogo aperta. Uma chamada agora pode abrir vantagem.","Chegou o round que muda o mapa. A equipe espera sua decisão.").get(m.stage);
                c.add(option(s,"match-aggressive","Assumir o duelo","Pedir o espaço e buscar a primeira eliminação.","aim",12,"Mais destaque pessoal · risco maior · energia −12"));
                c.add(option(s,"match-control","Mudar a leitura","Adaptar a execução com informação e controle de mapa.","tactics",5,"Leitura e preparo · energia −8"));
                c.add(option(s,"match-support","Confiar no coletivo","Combinar utilitários e jogar pela troca.","communication",0,"Química e consistência · energia −5")); }
        }
        return new Scene(tag,title,description,date(s),List.copyOf(c));
    }
    public void act(GameState s,Action action) {
        require(action!=null&&action.revision()==s.revision,"Sua carreira mudou em outra aba. Atualize antes de continuar.");
        if("continue".equals(action.type())) {require(s.outcome!=null,"Nenhum resultado pendente.");s.outcome=null;s.revision++;return;}
        require(s.outcome==null,"Leia o resultado e continue antes de fazer outra escolha.");require(!s.ended,"Sua carreira terminou. Comece outra história.");require("choice".equals(action.type()),"Ação inválida.");
        Choice choice=scene(s).choices().stream().filter(c->c.id().equals(action.target())).findFirst().orElseThrow(()->new IllegalArgumentException("Escolha inválida para esta ocasião."));
        require(choice.available(),"Você não tem saldo para esta liberação.");
        Map<String,String> before=snapshot(s);String when=date(s);int oldPhase=s.phase;boolean success=choice.chance()==100||random.nextInt(100)<choice.chance();String narrative;String heading=choice.title();String tone=success?"good":"bad";boolean advance=true;
        switch(choice.id()) {
            case "prep-aim" -> {skill(s,"aim",4);s.energy-=12;s.stress+=7;narrative="Você ganhou precisão, mas a rotina intensa cobrou energia. Os primeiros testes estão chegando.";}
            case "prep-study" -> {skill(s,"tactics",3);skill(s,"discipline",3);s.energy+=10;s.stress-=5;narrative="O plano de estudos deixou sua rotina mais estável e sua leitura mais afiada.";}
            case "prep-team" -> {skill(s,"communication",4);s.chemistry+=8;narrative="As chamadas começaram a fluir. Você chega à peneira com mais confiança no grupo.";}
            case "trial-highlight" -> {s.energy-=12;if(success){s.reputation+=12;s.followers+=250;skill(s,"aim",2);narrative="Seu highlight circulou entre os olheiros. A reputação subiu antes da janela de contratos.";}else{s.form-=7;s.stress+=8;narrative="Você forçou duelos demais. A comissão gostou da coragem, mas quer mais consistência.";}}
            case "trial-team" -> {s.energy-=8;if(success){s.reputation+=10;s.chemistry+=8;skill(s,"communication",2);narrative="A comissão elogiou sua comunicação. O coletivo colocou seu nome nas conversas do mercado.";}else{s.form-=3;s.stress+=4;narrative="As informações chegaram tarde em momentos importantes. A avaliação foi discreta, e você segue com oportunidades de base.";}}
            case "trial-steady" -> {s.reputation+=4;skill(s,"discipline",2);s.stress-=5;narrative="Um teste sem grandes highlights, mas com fundamentos sólidos. Há espaço para crescer sem pular etapas.";}
            case "free-agent" -> {s.chemistry-=5;narrative="Você manteve a liberdade e vai disputar a temporada com um mix. Não haverá salário fixo até um novo contrato.";}
            case "honor" -> {s.chemistry+=10;skill(s,"composure",2);narrative="A comissão recebeu bem seu compromisso. O contrato segue até "+s.contract.endYear()+".";}
            case "renegotiate" -> {if(success){var c=s.contract;s.contract=new GameState.Contract(c.orgId(),c.name(),c.tier(),c.salary()*115/100,c.endYear(),c.position(),c.pressure());narrative="A organização aceitou sua valorização. O salário foi reajustado em 15%, sem alterar o prazo.";}else{s.stress+=8;narrative="A diretoria recusou o reajuste. O contrato continua, mas a conversa aumentou a pressão sobre você.";}}
            case "release" -> {s.money-=s.contract.salary()*2;s.contract=null;s.chemistry=40;advance=false;narrative="A rescisão foi paga. Você está livre para escolher entre as propostas desta mesma janela.";}
            case "life-brand" -> {int cash=success?400+s.followers/4:100;earn(s,cash);s.followers+=success?500:100;s.energy-=15;s.stress+=10;skill(s,"communication",1);narrative=success?"Sua presença fora do servidor rendeu dinheiro e novos seguidores. Você volta com menos energia para competir.":"A ação teve pouco alcance. Houve uma pequena renda, mas a agenda extra cobrou seu preço.";}
            case "life-team" -> {s.chemistry+=12;skill(s,"communication",2);s.stress-=5;narrative="A conversa evitou ruídos. A equipe volta mais alinhada para a segunda etapa.";}
            case "life-rest" -> {s.energy+=35;s.stress-=20;skill(s,"discipline",2);narrative="Você tirou a pressão dos ombros. O descanso devolveu energia para a próxima etapa.";}
            case "year-develop","year-brand","year-rest","retire" -> {
                if(choice.id().equals("year-develop")){Catalog.ATTRIBUTES.keySet().forEach(k->skill(s,k,2));s.energy=90;s.stress-=15;}
                if(choice.id().equals("year-brand")){s.followers+=500;earn(s,700+s.followers/5);skill(s,"communication",2);s.energy=85;}
                if(choice.id().equals("year-rest")){s.energy=100;s.stress-=35;skill(s,"composure",3);}
                settle(s,1);
                narrative="O balanço de "+s.year+" foi registrado no histórico. ";
                if(s.age>=35||choice.id().equals("retire")){finishYear(s);s.ended=true;s.ending=s.titles>=5?"Uma carreira de lenda":s.reputation>=80?"Um nome que ficou no cenário":"Seu caminho no competitivo";heading="Carreira encerrada";narrative+="Você encerrou sua trajetória aos "+s.age+" anos, com "+s.wins+" vitórias e "+s.titles+" título(s). Seu legado está no histórico.";advance=false;}
                else {finishYear(s);s.age++;s.year++;s.phase=0;s.seasonWins=0;s.seasonLosses=0;s.match=null;if(s.contract!=null&&s.contract.endYear()<s.year)s.contract=null;if(s.age>28)skill(s,"aim",-1);narrative+="Você completa "+s.age+" anos e entra em "+s.year+". "+(s.contract==null?"A próxima janela poderá trazer um novo escudo.":"Seu contrato com "+s.contract.name()+" segue em vigor.");advance=false;}
            }
            case "match-aggressive","match-control","match-support" -> {
                GameState.Match m=matchFor(s);s.match=m;s.energy-=choice.id().equals("match-aggressive")?12:choice.id().equals("match-control")?8:5;
                boolean reserve=s.contract!=null&&s.contract.position().equals("Reserva");
                if(success){m.roundsWon++;s.form+=4;s.reputation+=reserve?1:choice.id().equals("match-aggressive")?4:2;s.followers+=reserve?20:80;skill(s,choice.attribute(),1);s.stress-=3;}else{s.form-=3;s.stress+=5;}
                if(choice.id().equals("match-support"))s.chemistry+=4;
                narrative=success?"A chamada funcionou: seu time levou o round-chave. ":"O adversário leu a jogada e levou o round-chave. ";
                m.stage++;
                if(m.stage<3){m.score=m.stage==1?(success?5:4):(m.roundsWon>=1?9:7);m.opponentScore=m.stage==1?(success?4:5):(m.roundsWon>=2?7:9);heading=success?"Round conquistado":"Round perdido";narrative+="O mapa continua. Placar na próxima ocasião: "+m.score+"–"+m.opponentScore+".";advance=false;}
                else {boolean won=m.roundsWon>=2;int loser=m.roundsWon==3?8:m.roundsWon==2?11:m.roundsWon==1?10:8;m.score=won?13:loser;m.opponentScore=won?loser:13;
                    heading=(won?"VITÓRIA ":"DERROTA ")+m.score+"–"+m.opponentScore;tone=won?"good":"bad";if(won){s.wins++;s.seasonWins++;s.reputation+=reserve?3:6;if(!reserve)earn(s,250*(s.contract==null?1:s.contract.tier()));}else{s.losses++;s.seasonLosses++;s.reputation+=1;}
                    narrative+="Fim do mapa em "+m.map+": "+Catalog.team(s)+" "+m.score+" x "+m.opponentScore+" "+m.opponent+". ";
                    if(oldPhase==6&&won&&m.finalEligible){s.titles++;String trophy=Catalog.circuit(s)+" "+s.year;s.trophies.add(new GameState.Trophy(s.year,trophy,Catalog.team(s)));earn(s,3000*(s.contract==null?1:s.contract.tier()));narrative+="CAMPEÃO! A temporada perfeita terminou com um troféu e premiação.";}else if(oldPhase==6)narrative+="A colocação foi registrada. A próxima temporada oferece outra chance.";
                    if(s.contract!=null&&s.contract.position().equals("Reserva"))narrative+="Como reserva, seus mapas são scrims de avaliação; não valem troféu oficial do circuito.";
                    s.match=null;
                }
            }
            default -> {Catalog.Offer offer=Catalog.offers(s).stream().filter(o->o.id().equals(choice.id())).findFirst().orElseThrow();s.contract=new GameState.Contract(offer.orgId(),offer.name(),offer.tier(),offer.salary(),s.year+offer.years()-1,offer.position(),offer.pressure());s.chemistry=45;s.stress+=offer.pressure()/3;narrative="Você assinou com "+offer.name()+" como "+offer.position().toLowerCase()+" até dezembro de "+s.contract.endYear()+". O salário mensal é de R$ "+offer.salary()+". "+(offer.position().equals("Reserva")?"A renda é maior, mas sua exposição ficará nos scrims de avaliação.":"A vaga no quinteto abre espaço para disputar os próximos mapas.");}
        }
        if(advance)advance(s);
        normalize(s);
        var changes=changes(before,snapshot(s));s.outcome=new GameState.Outcome(heading,narrative,tone,when,changes);s.history.add(0,new GameState.History(when,heading,narrative,tone,changes));s.revision++;
    }
    private void advance(GameState s) {
        int months=Catalog.MONTH_START[s.phase+1]-Catalog.MONTH_START[s.phase];
        settle(s,months);s.phase++;s.match=null;
    }
    private void settle(GameState s,int months) {
        if(s.contract!=null)earn(s,s.contract.salary()*months);
        s.money-=Math.min(s.money,(s.age<18?50:300)*months);
        s.energy+=8*months;s.stress+=s.contract==null?0:s.contract.pressure()/12;
    }
    private GameState.Match matchFor(GameState s) {
        if(s.match!=null)return s.match;GameState.Match m=new GameState.Match();
        var opponents=Catalog.ORGS.stream().filter(o->o.tier()==(s.contract==null?1:s.contract.tier())).toList();
        int seed=Math.floorMod(Objects.hash(s.name,s.year,s.phase),opponents.size());var other=opponents.get(seed);
        m.opponent=other.name().equals(Catalog.team(s))?"Vertex Five":other.name();m.map=Catalog.MAPS.get(Math.floorMod(s.year+s.phase,Catalog.MAPS.size()));
        boolean reserve=s.contract!=null&&s.contract.position().equals("Reserva");m.finalEligible=s.phase==6&&s.seasonWins==2&&!reserve;
        m.competition=reserve?"Scrim de avaliação":s.phase==6?(m.finalEligible?"Final — ":"Repescagem — ")+Catalog.circuit(s):"Etapa "+(s.phase==3?1:2)+" — "+Catalog.circuit(s);return m;
    }
    private void finishYear(GameState s) {s.seasons.add(0,new GameState.Season(s.year,s.age,Catalog.team(s),s.seasonWins,s.seasonLosses,(int)s.trophies.stream().filter(t->t.year()==s.year).count(),s.earnings-s.seasons.stream().mapToInt(GameState.Season::earnings).sum()));}
    private void earn(GameState s,int amount){s.money+=amount;s.earnings+=amount;}
    private void skill(GameState s,String key,int amount){s.attributes.compute(key,(k,v)->clamp(v+amount,1,99));}
    private void normalize(GameState s){s.energy=clamp(s.energy,0,100);s.form=clamp(s.form,0,100);s.stress=clamp(s.stress,0,100);s.chemistry=clamp(s.chemistry,0,100);s.reputation=clamp(s.reputation,0,200);s.money=Math.max(0,s.money);}
    private boolean isMatch(GameState s){return s.phase==3||s.phase==5||s.phase==6;}
    private String date(GameState s){return Catalog.MONTHS.get(s.phase)+" de "+s.year+" · "+s.age+" anos";}
    private Map<String,String> snapshot(GameState s){var m=new LinkedHashMap<String,String>();m.put("Saldo","R$ "+s.money);m.put("Energia",""+s.energy);m.put("Forma",""+s.form);m.put("Estresse",""+s.stress);m.put("Química",""+s.chemistry);m.put("Reputação",""+s.reputation);m.put("Seguidores",""+s.followers);Catalog.ATTRIBUTES.forEach((k,v)->m.put(v,""+s.attributes.get(k)));m.put("Organização",Catalog.team(s));m.put("Salário mensal",s.contract==null?"Sem salário":"R$ "+s.contract.salary());m.put("Títulos",""+s.titles);m.put("Vitórias",""+s.wins);m.put("Derrotas",""+s.losses);m.put("Idade",""+s.age);m.put("Temporada",""+s.year);return m;}
    private List<GameState.Change> changes(Map<String,String>a,Map<String,String>b){var list=new ArrayList<GameState.Change>();a.forEach((k,v)->{if(!v.equals(b.get(k)))list.add(new GameState.Change(k,v,b.get(k)));});return List.copyOf(list);}
    private static int clamp(int value,int min,int max){return Math.max(min,Math.min(max,value));}
    private static void require(boolean condition,String message){if(!condition)throw new IllegalArgumentException(message);}
}
