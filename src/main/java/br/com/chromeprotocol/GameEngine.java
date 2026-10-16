package br.com.chromeprotocol;

import java.util.*;
import java.util.random.RandomGenerator;

/** Server-owned decisions, permanent progression and a 2026-inspired competition calendar. */
public final class GameEngine {
    private final RandomGenerator random;
    public GameEngine(RandomGenerator random){this.random=Objects.requireNonNull(random);}
    public record Action(String type,String target,int revision) {}
    public record Choice(String id,String title,String description,String attribute,int chance,String consequence,boolean available) {}
    public record ActivityView(PersonalLife.Activity item,Map<String,Integer> gains,boolean available) {}
    public record GoalView(String id,String name,String description,int target,int progress,int cash,Map<String,Integer> gains,boolean chosen,boolean available) {}
    public record Scene(String tag,String title,String description,String date,List<Choice> choices) {}
    public record InvestmentView(Catalog.Investment item,int level,int price,Map<String,Integer> gains,boolean available) {}
    public record CardView(String id,String name,String rarity,Map<String,Integer> gains,int chanceBonus,String detail) {}
    public record CalendarEntry(int phase,String name,String period,String status) {}
    public record Statistics(int maps,int kills,int deaths,int assists,int rounds,double acs,String kd) {}
    public record View(GameState character,Scene scene,List<Catalog.Offer> offers,Map<String,String> attributes,List<String> phases,int overall,String team,String circuit,
                       GameState.Match activeMatch,List<InvestmentView> investments,List<CardView> cards,List<CalendarEntry> calendar,Statistics statistics,Statistics seasonStatistics,List<ActivityView> activities,String activityStatus,List<GoalView> goals,Tour.Agenda agenda,List<CareerHonors.ClubLegacy> clubLegacy,List<CareerHonors.SeasonFinal> seasonFinals,TeamRatings.Profile teamRating,TeamRatings.Matchup matchup,List<TeamRatings.Profile> teamRatings) {}

    public GameState create(String name,String nationality,String role,String background,String agent,Appearance appearance){return create(name,nationality,role,background,agent,appearance,"standard",false,agent==null?List.of():List.of(agent));}
    public GameState create(String name,String nationality,String role,String background,String agent,Appearance appearance,String mode,boolean igl,List<String> favorites){
        require(name!=null&&!name.isBlank()&&name.strip().length()<=24&&name.codePoints().noneMatch(Character::isISOControl),"Use um nick de 1 a 24 caracteres.");
        Catalog.country(nationality);Catalog.Role selected=Catalog.role(role);
        require(Catalog.BACKGROUNDS.containsKey(background),"Escolha uma trajetória válida.");
        require(Catalog.AGENTS.stream().anyMatch(a->a.name().equals(agent)),"Escolha um agente válido.");
        mode=mode==null?"standard":mode;require(Set.of("standard","prodigy").contains(mode),"Escolha um modo válido.");
        var favoriteSet=new LinkedHashSet<String>();
        if(favorites!=null){require(favorites.size()<=Catalog.AGENTS.size(),"Muitos favoritos.");for(String a:favorites){require(Catalog.AGENTS.stream().anyMatch(v->v.name().equals(a)),"Agente favorito inválido.");favoriteSet.add(a);}}
        favoriteSet.add(agent);
        GameState s=new GameState();s.name=name.strip();s.nationality=nationality;s.role=role;s.background=background;s.agent=agent;s.favoriteAgents.addAll(favoriteSet);s.igl=igl;s.developmentMode=mode;s.careerSeed=random.nextLong();s.appearance=appearance==null?Appearance.defaults():appearance;
        Catalog.ATTRIBUTES.keySet().forEach(k->s.attributes.put(k,30));s.attributes.merge(selected.attribute(),7,Integer::sum);
        s.attributes.merge(background.equals("ranked")?"aim":background.equals("school")?"discipline":"communication",5,Integer::sum);
        if(igl){s.attributes.merge("communication",3,Integer::sum);s.attributes.merge("tactics",2,Integer::sum);}
        if(mode.equals("prodigy")){s.attributes.replaceAll((k,v)->v+20);s.reputation=20;s.followers=1000;}
        else if(background.equals("content"))s.followers=600;
        return s;
    }
    public void customize(GameState s,Appearance appearance,int revision){require(revision==s.revision,"Sua carreira mudou. Atualize a página.");require(appearance!=null,"Escolha um visual.");s.appearance=appearance;s.revision++;}
    public View view(GameState s){
        return new View(s,s.ended?null:scene(s),s.phase==2?Catalog.offers(s):List.of(),Catalog.ATTRIBUTES,Catalog.PHASES,Catalog.overall(s),Catalog.team(s),Catalog.circuit(s),
            !s.ended&&!s.pendingCards&&Calendar.match(s)&&(s.match!=null||s.legacyMap)?matchFor(s):null,
            Catalog.INVESTMENTS.stream().map(i->{int level=s.investments.getOrDefault(i.id(),0);return new InvestmentView(i,level,i.price()*(level+1),previewGains(s,i.bonuses()),!s.ended&&level<i.maxLevel()&&s.money>=i.price()*(level+1));}).toList(),
            s.pendingCards?cardOffers(s):List.of(),calendar(s),statistics(s.maps.stream().filter(GameState.MapStats::official).toList()),statistics(s.maps.stream().filter(m->m.official()&&m.year()==s.year).toList()),
            PersonalLife.activities(s).stream().map(a->new ActivityView(a,previewGains(s,a.bonuses()),PersonalLife.available(s))).toList(),
            s.ended?"Carreira encerrada":s.pendingCards?"Escolha sua carta anual antes de retomar a rotina":s.match!=null?"Conclua o mapa em andamento":s.activitySlots.contains(PersonalLife.slot(s))?"Atividade deste período concluída":s.outcome!=null?"Leia o resultado pendente":"Uma atividade opcional disponível neste período",
            PersonalLife.GOALS.stream().map(g->new GoalView(g.id(),g.name(),g.description(),g.target(),g.id().equals(s.seasonGoal)?PersonalLife.progress(s,g):0,g.cash(),previewGains(s,g.bonuses()),g.id().equals(s.seasonGoal),s.seasonGoal==null&&s.phase<=2&&!s.ended&&!s.pendingCards&&s.outcome==null&&(!g.id().equals("voice")||s.attributes.get("communication")<=91))).toList(),Tour.agenda(s),CareerHonors.legacy(s),CareerHonors.finals(s),TeamRatings.profile(s,Catalog.team(s),true),TeamRatings.matchup(s),Catalog.ORGS.stream().map(o->TeamRatings.profile(s,o.name(),true)).sorted(Comparator.comparingInt(TeamRatings.Profile::overall).reversed()).toList());
    }
    private int growth(GameState s,int amount){return amount<=0?0:s.developmentMode.equals("prodigy")?(int)Math.ceil(amount*1.5):amount;}
    private Map<String,Integer> previewGains(GameState s,Map<String,Integer> bonuses){var result=new LinkedHashMap<String,Integer>();bonuses.forEach((key,value)->result.put(key,Math.min(99-s.attributes.get(key),growth(s,value))));return result;}
    private String gainsText(GameState s,Map<String,Integer> bonuses){return String.join(" · ",previewGains(s,bonuses).entrySet().stream().map(e->Catalog.ATTRIBUTES.get(e.getKey())+" +"+e.getValue()).toList());}
    private void permanent(GameState s,Map<String,Integer> bonuses){bonuses.forEach((key,value)->{int before=s.attributes.get(key);skill(s,key,value);s.permanentBonuses.merge(key,s.attributes.get(key)-before,Integer::sum);});}
    private List<CardView> cardOffers(GameState s){var pool=new ArrayList<>(Catalog.CARDS);Collections.shuffle(pool,new Random(Objects.hash(s.careerSeed,s.year,"cards")));var cards=new ArrayList<>(pool.subList(0,3));if(s.developmentMode.equals("prodigy")&&cards.stream().noneMatch(c->c.id().equals("phenom")))cards.set(2,Catalog.CARDS.get(5));return cards.stream().map(c->new CardView(c.id(),c.name(),c.rarity(),previewGains(s,c.bonuses()),s.cardChanceBonus<15?1:0,c.detail())).toList();}
    private Choice option(GameState s,String id,String title,String description,String attribute,int difficulty,String consequence){
        int odds=attribute.isEmpty()?100:clamp(51+s.attributes.get(attribute)/2-difficulty+(s.energy-50)/12+(s.form-50)/15-s.stress/15+(s.chemistry-50)/15+s.cardChanceBonus+(s.developmentMode.equals("prodigy")?8:0),20,98);
        if(!attribute.isEmpty()&&s.match!=null&&s.match.mapDecision&&s.currentSeries!=null){
            int adjustment=(s.attributes.get(attribute)-Catalog.overall(s))/5+(s.igl&&id.equals("match-igl")?3:0);
            odds=clamp(TeamRatings.chance(s,s.currentSeries)+adjustment-(difficulty-4)/3,10,88);
        }else if(!attribute.isEmpty()&&Calendar.match(s)){if(s.match!=null)odds+=s.match.roundsWon*2;if(Calendar.reserve(s))odds-=4;if(s.igl&&id.equals("match-igl"))odds+=5;odds=clamp(odds,20,98);}
        return new Choice(id,title,description,attribute,odds,consequence,true);
    }
    private Scene scene(GameState s){
        var c=new ArrayList<Choice>();String title,description,tag;
        if(s.pendingCards){for(var card:cardOffers(s)){String benefits=String.join(" · ",card.gains().entrySet().stream().map(e->Catalog.ATTRIBUTES.get(e.getKey())+" +"+e.getValue()).toList());c.add(option(s,"card:"+card.id(),card.name(),card.rarity()+" · "+card.detail(),"",0,benefits+" · chance permanente +"+card.chanceBonus()+"%"));}return new Scene("CARTAS DE EVOLUÇÃO","A temporada acaba. O talento fica.","Escolha uma das três cartas de "+s.year+". Os ganhos são permanentes, conhecidos antes da escolha e não expiram. Só uma carta pode ser escolhida por temporada.",date(s),List.copyOf(c));}
        if(s.pace.equals("season")&&s.phase==0){
            c.add(option(s,"season-focus:aim","Lapidar minha mecânica","Priorizar precisão e consistência nos duelos.","",0,gainsText(s,Catalog.bonus("aim",4,"discipline",2))));
            c.add(option(s,"season-focus:tactics","Evoluir minha leitura","Revisar VODs, utilitários e adaptações.","",0,gainsText(s,Catalog.bonus("tactics",4,"composure",2))));
            c.add(option(s,"season-focus:communication","Crescer como líder","Melhorar chamadas e confiança no quinteto.","",0,gainsText(s,Catalog.bonus("communication",4,"discipline",2))));
            if(s.age>=23)c.add(option(s,"season-retire","Encerrar minha trajetória","Guardar a carreira e todas as suas conquistas.","",0,"Encerramento da carreira"));
            return new Scene("TEMPORADA AUTOMÁTICA","Qual será seu próximo salto?","Escolha seu desenvolvimento e seu time. Partidas, classificações e premiações são simuladas; você decide as oportunidades e a carta anual.",date(s),List.copyOf(c));
        }
        if(s.tourEvent){var event=Events.life(s);for(var o:event.choices())c.add(option(s,"life:"+o.id(),o.title(),o.description(),o.attribute(),o.difficulty(),gainsText(s,o.bonuses())+" · retorno varia com o resultado"));return new Scene("ENTRE AS SÉRIES",event.title(),event.description(),date(s),List.copyOf(c));}
        if(s.pace.equals("run")&&s.phase==0){c.add(option(s,"run-prepare","Preparar e abrir o mercado","Sua rotina combina treino e um teste de consistência. Você escolhe o próximo contrato.","",0,"Leitura +3 · disciplina +5 · reputação +4"));return new Scene("RUN / NOVA TEMPORADA","O próximo capítulo começa agora.","Avance por blocos de séries, tome decisões de carreira e acompanhe o legado. Você ainda pode assumir qualquer momento decisivo.",date(s),List.copyOf(c));}
        if(Calendar.match(s)&&s.match==null&&!s.legacyMap){
            var f=Tour.fixture(s);String detail=f.critical.isBlank()?"Rodada regular. Você pode simular ou entrar no servidor.":"MOMENTO DECISIVO: "+f.critical+".";
            c.add(option(s,"series-play","Assumir o momento decisivo","Uma decisão por mapa. Sucesso vence o mapa; o primeiro a duas vitórias na MD3 ou três na MD5 fecha a série.","",0,"MD"+f.bestOf+" · todos os mapas ficam no histórico"));
            c.add(option(s,"series-sim","Simular esta série","Seu jogador atua com seus atributos, condição e bônus atuais.","",0,"Resultado completo · sem gastar escolhas em rounds comuns"));
            c.add(option(s,s.pace.equals("run")?"series-run":"series-next",s.pace.equals("run")?"Avançar a run":"Avançar até uma decisão",s.pace.equals("run")?"Simular até quatro séries e parar em um dilema ou no fim do período.":"Simular rodadas comuns e parar antes de classificação, eliminação ou título.","",0,"Nenhum contrato, carta anual ou dilema será escolhido por você"));
            return new Scene(s.pace.equals("run")?"RUN / AGENDA":"AGENDA DE SÉRIES",f.competition,Catalog.team(s)+" x "+f.opponent+" · "+f.round+" · MD"+f.bestOf+". "+detail,date(s),List.copyOf(c));
        }
        if(Calendar.match(s)){
            GameState.Match m=matchFor(s);var event=Events.round(s,m.mapDecision?m.mapNumber-1:m.stage);tag="MATCH DAY";title=m.competition;description=Catalog.team(s)+" x "+m.opponent+" · "+m.map+(m.mapDecision?" · Mapa "+m.mapNumber+" · Série "+s.currentSeries.wins+"–"+s.currentSeries.losses+" · MD"+s.currentSeries.bestOf:".")+". "+event.title()+" "+event.description();
            for(String requested:event.plays()){String id=requested.equals("match-igl")&&!s.igl?"match-support":requested;var play=Events.play(id);if(c.stream().noneMatch(x->x.id().equals(id)))c.add(option(s,id,play.title(),play.description(),play.attribute(),play.difficulty(),(m.mapDecision?"Uma decisão resolve este mapa · ":"Round-chave · ")+(s.igl&&id.equals("match-igl")?"bônus de IGL · ":"")+"energia −"+Math.max(0,play.energy()-s.investments.getOrDefault("nutrition",0))));}
            if(m.mapDecision)c.add(new Choice("map-sim","Simular este mapa","Deixar o quinteto resolver o mapa atual.","",TeamRatings.chance(s,s.currentSeries),"Uma vitória ou derrota registrada · a série continua até fechar",true));
            return new Scene(tag,title,description,date(s),List.copyOf(c));
        }
        switch(s.phase){
            case 0->{tag="PRÉ-TEMPORADA";String[] openings={"Um novo repertório para a temporada.","A rotina que sustenta um talento.","O próximo salto começa no treino.","Um novo ano. A mesma ambição."};title=s.age==15?"Toda carreira começa no quarto.":openings[Math.floorMod(s.year,openings.length)];description="Você tem "+s.age+" anos. Prepare a temporada sem perder habilidades: a evolução adquirida acompanha toda a carreira.";
                c.add(option(s,"prep-aim","Focar na mecânica","Trabalhar precisão e duelos.","",0,gainsText(s,Catalog.bonus("aim",4))+" · energia −12"));c.add(option(s,"prep-study","Estudar mapas e rotinas","Revisar VODs e organizar a preparação.","",0,gainsText(s,Catalog.bonus("tactics",3,"discipline",3))));c.add(option(s,"prep-team","Treinar comunicação","Alinhar chamadas, trocas e utilitários.","",0,gainsText(s,Catalog.bonus("communication",4))+" · química +8"));
                if(s.igl)c.add(option(s,"prep-igl","Desenhar o playbook como IGL","Preparar planos A, B e adaptações.","",0,gainsText(s,Catalog.bonus("tactics",3,"communication",3))));else c.add(option(s,"become-igl","Assumir a liderança das chamadas","Acumular a função de IGL sem trocar sua função de agente.","",0,"IGL ativado · "+gainsText(s,Catalog.bonus("communication",3,"tactics",2))));}
            case 1->{tag="PENEIRA";title=s.age<18?"Uma nova chance de mostrar seu jogo.":"A comissão acompanha seu teste.";description="Destaque, leitura e consistência abrem propostas. As chances refletem atributos, condição e seus bônus permanentes.";
                c.add(option(s,"trial-highlight","Buscar o highlight","Assumir duelos de maior risco para chamar atenção.","aim",10,"Sucesso: reputação +12 · nenhuma perda de habilidade"));c.add(option(s,"trial-team","Jogar pelo coletivo","Demonstrar informação e execução.","communication",2,"Sucesso: reputação +10 · química +8"));c.add(option(s,"trial-steady","Mostrar consistência","Priorizar fundamentos e rotina.","",0,"Reputação +4 · "+gainsText(s,Catalog.bonus("discipline",2))));c.add(option(s,"trial-vod","Apresentar uma leitura tática","Explicar como você adaptaria o quinteto ao rival.","tactics",3,"Sucesso: reputação +10 · "+gainsText(s,Catalog.bonus("tactics",2))));}
            case 2->{tag="MERCADO";title=s.contract==null?"O mundo quer o seu jogo.":"Um contrato também é uma escolha.";description=s.contract==null?"Compare clubes de todas as regiões, valorização e espaço no quinteto. Clubes do VCT e equipes de desenvolvimento; as ofertas pertencem a esta carreira simulada.":"Seu vínculo com "+s.contract.name()+" vale até dezembro de "+s.contract.endYear()+". Você pode cumprir, renegociar ou pagar a liberação.";
                if(s.contract==null){for(var o:Catalog.offers(s))c.add(option(s,o.id(),(o.id().equals("renew")?"Renovar com ":o.id().startsWith("promote:")?"Subir para ":"Assinar com ")+o.name(),o.position()+" · R$ "+o.salary()+"/mês · "+o.years()+" temporada(s)","",0,o.detail()));c.add(option(s,"free-agent","Continuar com um mix","Manter a liberdade e jogar o Premier.","",0,"Sem salário fixo · preparação por conta própria"));}
                else{for(var o:Catalog.offers(s))c.add(option(s,o.id(),"Subir para "+o.name(),o.position()+" · R$ "+o.salary()+"/mês · "+o.years()+" temporadas","",0,o.detail()));c.add(option(s,"honor","Cumprir o contrato","Fortalecer a rotina e sua posição.","",0,"Química +10 · "+gainsText(s,Catalog.bonus("composure",2))));c.add(option(s,"renegotiate","Negociar valorização","Pedir reajuste sem mudar o prazo.","communication",12,"Sucesso: salário +15% · falha afeta só o estresse"));int fee=s.contract.salary()*2;c.add(new Choice("release","Pedir liberação","Rescindir e ouvir propostas nesta mesma janela.","",100,"Custo: R$ "+fee,s.money>=fee));}}
            case 11->{tag="PRÊMIOS INDIVIDUAIS";title="Seu nome além do placar.";var awards=annualAwards(s);description=awards.isEmpty()?"Os números da temporada estão fechados. Você não atingiu os critérios individuais neste ano, mas suas habilidades continuam intactas.":"Reconhecimentos desta temporada: "+String.join(" · ",awards.stream().map(GameState.Award::title).toList())+". Os critérios aparecem na sala de prêmios.";
                c.add(option(s,"awards-team","Dividir o mérito com o quinteto","Valorizar quem esteve nas suas execuções.","",0,"Química +8 · registrar prêmios"));c.add(option(s,"awards-fans","Celebrar com a comunidade","Agradecer a torcida que acompanhou a campanha.","",0,"Seguidores +400 · registrar prêmios"));c.add(option(s,"awards-study","Usar os números para evoluir","Traçar metas sem diminuir os atributos atuais.","",0,gainsText(s,Catalog.bonus("discipline",2))+" · registrar prêmios"));}
            case 12->{tag="FIM DA TEMPORADA";title="Feche o ano. Escolha seu próximo salto.";description="A temporada de "+s.year+" termina com "+s.seasonWins+" vitória(s), "+s.seasonLosses+" derrota(s) e suas estatísticas registradas. Depois do balanço, você escolherá uma carta permanente.";
                c.add(option(s,"year-develop","Voltar mais completo","Manter um bloco de fundamentos na pausa.","",0,gainsText(s,Catalog.bonus("aim",2,"tactics",2,"communication",2,"discipline",2,"composure",2))));c.add(option(s,"year-brand","Construir sua marca","Trabalhar conteúdo, contatos e projetos.","",0,"Seguidores +500 · renda extra · "+gainsText(s,Catalog.bonus("communication",2))));c.add(option(s,"year-rest","Recuperar o ritmo","Passar a pausa longe da pressão.","",0,"Energia 100 · estresse −35 · "+gainsText(s,Catalog.bonus("composure",3))));if(s.age>=23)c.add(option(s,"retire","Encerrar a carreira","Escolher uma última carta e guardar seu legado.","",0,"Aposentadoria após a recompensa anual"));}
            default->{var life=Events.life(s);tag=s.phase==7?"ACONTECIMENTO INESPERADO":"ENTRE COMPETIÇÕES";title=life.title();description=(s.phase==7?"":Calendar.competition(s)+": você não tem vaga nesta competição. ")+life.description();for(var o:life.choices())c.add(option(s,"life:"+o.id(),o.title(),o.description(),o.attribute(),o.difficulty(),gainsText(s,o.bonuses())+" · sucesso: até R$ "+o.cash()+" / +"+o.followers()+" seguidores"));}
        }
        return new Scene(tag,title,description,date(s),List.copyOf(c));
    }
    /** One server transaction resolves a choice and all routine competition until the next meaningful pause. */
    public void act(GameState s,Action action){
        var before=snapshot(s);String when=date(s);int startYear=s.year;
        actOne(s,action);
        if(!s.pace.equals("season")||s.ended||s.pendingCards||s.match!=null||!(action.type().equals("choice")||action.type().equals("pace")||action.type().equals("continue")))return;
        if(s.phase==0||s.phase==2||s.phase==7)return;
        var original=s.outcome;var batch=autoSeason(s);if(batch.isEmpty()&&s.phase!=12&&!s.pendingCards)return;
        normalize(s);String narrative=(original==null?"":original.narrative()+" ")+"A agenda foi simulada automaticamente: "+batch.size()+" séries, com todos os mapas e estatísticas salvos. "+(s.pendingCards?"Temporada concluída. Escolha sua carta de evolução para o próximo ano.":"Um acontecimento importante pede sua escolha.");
        var changes=changes(before,snapshot(s));String title=s.pendingCards?"Temporada "+startYear+" concluída":"Seu próximo passo fora do servidor";
        String decisionTone=original==null?"neutral":original.tone();
        s.outcome=new GameState.Outcome(title,narrative,decisionTone,when,changes,null,List.copyOf(batch));
        if(original!=null&&!s.history.isEmpty())s.history.remove(0);
        s.history.add(0,new GameState.History(when,title,narrative,decisionTone,changes));
    }
    private List<GameState.SeriesResult> autoSeason(GameState s){
        var batch=new ArrayList<GameState.SeriesResult>();int guard=0;
        while(!s.ended&&!s.pendingCards&&s.phase!=0&&s.phase!=2&&s.phase!=7){
            require(++guard<100,"A simulação atingiu o limite de segurança.");
            s.tourEvent=false;
            if(Calendar.match(s)){
                s.currentSeries=Tour.fixture(s);var f=s.currentSeries;while(f.wins<f.bestOf/2+1&&f.losses<f.bestOf/2+1)simulateMap(s,f,0);batch.add(finishSeries(s,"automatic"));normalize(s);
            }else if(s.phase==1){skill(s,"discipline",2);s.reputation+=4;advance(s);}
            else if(s.phase==11){for(var award:annualAwards(s))addAward(s,award);advance(s);}
            else if(s.phase==12){Catalog.ATTRIBUTES.keySet().forEach(k->skill(s,k,2));s.energy=90;s.stress-=15;settle(s,1);settleGoal(s);finishYear(s);s.pendingCards=true;}
            else advance(s);
        }
        s.tourEvent=false;return batch;
    }
    private void actOne(GameState s,Action action){
        require(action!=null&&action.revision()==s.revision,"Sua carreira mudou em outra aba. Atualize antes de continuar.");
        if("continue".equals(action.type())){require(s.outcome!=null,"Nenhum resultado pendente.");s.outcome=null;s.revision++;return;}
        require(s.outcome==null,"Leia o resultado antes de fazer outra escolha.");require(!s.ended,"Sua carreira terminou. Comece outra história.");
        var before=snapshot(s);String when=date(s);
        if("pace".equals(action.type())){require(Set.of("detailed","run","season").contains(action.target()),"Ritmo inválido.");require(s.match==null&&!s.pendingCards,"Conclua o momento ou a recompensa antes de mudar o ritmo.");s.pace=action.target();record(s,"Ritmo: "+(s.pace.equals("season")?"Temporada automática":s.pace.equals("run")?"Run em blocos":"Detalhado"),"O ritmo muda o avanço e as pausas. Atributos, chances, contratos e progresso permanecem os mesmos.","good",when,before,null);return;}
        if("activity".equals(action.type())){
            require(PersonalLife.available(s),"Você já usou este período ou tem um mapa/recompensa em andamento.");
            var item=PersonalLife.activities(s).stream().filter(a->a.id().equals(action.target())).findFirst().orElseThrow(()->new IllegalArgumentException("Atividade indisponível neste período."));
            item.bonuses().forEach((k,v)->skill(s,k,v));earn(s,item.cash());s.followers+=item.fans();s.energy+=item.energy();s.stress+=item.stress();s.chemistry+=item.chemistry();s.form+=item.form();s.activitySlots.add(PersonalLife.slot(s));normalize(s);
            record(s,item.name(),item.description()+" Os ganhos de habilidade ficam com você. O calendário não avançou; sua próxima atividade fica disponível no próximo período.","good",when,before,null);return;
        }
        if("goal".equals(action.type())){
            var goal=PersonalLife.goal(action.target());require(s.seasonGoal==null&&s.phase<=2&&!s.pendingCards,"Escolha apenas um objetivo na pré-temporada, antes do primeiro mapa.");require(!goal.id().equals("voice")||s.attributes.get("communication")<=91,"Sua comunicação já está perto do limite. Escolha outra meta.");s.seasonGoal=goal.id();s.goalStart=PersonalLife.initial(s,goal.id());
            record(s,"Objetivo: "+goal.name(),goal.description()+" A avaliação e a recompensa são automáticas no balanço anual. A meta não pode ser trocada nesta temporada; não cumprir nunca reduz habilidades.","good",when,before,null);return;
        }
        if("invest".equals(action.type())){invest(s,action.target());normalize(s);record(s,"Investimento permanente",investmentNarrative(s,action.target()),"good",when,before,null);return;}
        require("choice".equals(action.type()),"Ação inválida.");
        var choice=scene(s).choices().stream().filter(c->c.id().equals(action.target())).findFirst().orElseThrow(()->new IllegalArgumentException("Escolha inválida para esta ocasião."));require(choice.available(),"Saldo insuficiente.");
        boolean success=choice.chance()==100||random.nextInt(100)<choice.chance();String narrative,heading=choice.title(),tone=success?"good":"bad";boolean advance=true;GameState.MapStats mapStats=null;
        String id=choice.id();
        if(s.match!=null&&s.match.mapDecision&&(id.startsWith("match-")||id.equals("map-sim"))){resolveSeriesMap(s,id,success,when,before);return;}
        if(id.equals("season-retire")){s.ended=true;s.ending="Seu legado no competitivo";record(s,"Carreira encerrada","Você escolheu encerrar sua trajetória aos "+s.age+" anos. Resultados, estatísticas e conquistas continuam no histórico.","good",when,before,null);return;}
        if(id.startsWith("season-focus:")){String key=id.substring(13);skill(s,key,4);skill(s,key.equals("tactics")?"composure":"discipline",2);s.energy=90;s.stress=Math.max(0,s.stress-10);advance(s);skill(s,"discipline",2);s.reputation+=4;advance(s);normalize(s);record(s,"Desenvolvimento definido","Seu foco de evolução foi aplicado. Escolha o contrato; a primeira metade da temporada será simulada automaticamente.","good",when,before,null);return;}
        if(id.equals("run-prepare")){skill(s,"tactics",3);skill(s,"discipline",5);s.reputation+=4;s.energy+=10;advance(s);advance(s);normalize(s);record(s,"Pronto para o mercado","Treino e teste de consistência concluídos. Agora a escolha de contrato é sua.","good",when,before,null);return;}
        if(id.startsWith("series-")){seriesAction(s,id,when,before);return;}
        if(id.startsWith("card:")){
            String selected=id.substring(5);require(s.pendingCards&&cardOffers(s).stream().anyMatch(c->c.id().equals(selected)),"Esta carta não está disponível.");var card=Catalog.CARDS.stream().filter(c->c.id().equals(selected)).findFirst().orElseThrow();permanent(s,card.bonuses());s.cardChanceBonus=Math.min(15,s.cardChanceBonus+1);s.cards.add(new GameState.SelectedCard(s.year,card.id(),card.name()));s.pendingCards=false;narrative="Carta adquirida: "+card.name()+". Os bônus são permanentes e as habilidades não caem com decisões ou idade. ";
            if(s.age>=35||s.retiring){s.ended=true;s.ending=s.titles>=5?"Uma carreira de lenda":s.reputation>=80?"Um nome que ficou no cenário":"Seu caminho no competitivo";heading="Carreira encerrada";narrative+="Você encerrou sua trajetória aos "+s.age+" anos, com "+s.wins+" vitórias e "+s.titles+" título(s).";}
            else{startYear(s);narrative+="Você completa "+s.age+" anos e entra em "+s.year+". "+(s.contract==null?"A próxima janela pode trazer outro escudo.":"Seu contrato com "+s.contract.name()+" segue em vigor.");}advance=false;
        }else if(id.startsWith("life:")){
            var event=Events.life(s);var option=event.choices().stream().filter(o->("life:"+o.id()).equals(id)).findFirst().orElseThrow();s.seenLifeEvents.add(Events.LIFE.indexOf(event));if(s.tourEvent){s.tourEvent=false;advance=false;}if(success)option.bonuses().forEach((k,v)->skill(s,k,v));earn(s,success?option.cash():option.cash()/4);s.followers+=success?option.followers():option.followers()/4;s.chemistry+=success?option.chemistry():0;s.energy+=option.energy();s.stress+=option.stress()+(success?0:5);narrative=success?"A escolha trouxe o resultado esperado: "+option.description()+" Os ganhos de habilidade ficam com você.":"A situação não saiu como você esperava. Houve menos retorno e mais pressão, mas nenhuma habilidade ou OVR foi perdido.";
        }else if(id.startsWith("match-")){
            var m=matchFor(s);s.match=m;var play=Events.play(id);s.energy-=Math.max(0,play.energy()-s.investments.getOrDefault("nutrition",0));boolean reserve=Calendar.reserve(s);
            if(success){m.roundsWon++;s.form+=4;s.reputation+=reserve?1:play.reputation();s.followers+=reserve?20:80;skill(s,play.attribute(),1);s.stress-=3;}else{s.form-=3;s.stress+=Math.max(1,5-s.investments.getOrDefault("psychology",0));}
            if(play.attribute().equals("communication"))s.chemistry+=4;m.stage++;
            narrative=success?"A decisão funcionou e o time levou o round-chave. ":"O rival respondeu melhor e levou o round-chave. Suas habilidades e OVR foram preservados. ";
            if(m.stage<3){m.score=m.stage==1?(success?5:4):(m.roundsWon>=1?9:7);m.opponentScore=m.stage==1?(success?4:5):(m.roundsWon>=2?7:9);if(s.currentSeries!=null){m.score=m.stage==1?(success?10:9):(m.roundsWon>=2?12:m.roundsWon==1?11:10);m.opponentScore=m.stage==1?(success?9:10):(m.roundsWon>=2?10:m.roundsWon==1?11:12);}heading=success?"Round conquistado":"Round perdido";narrative+="O mapa continua: "+m.score+"–"+m.opponentScore+".";advance=false;}
            else{boolean won=m.roundsWon>=2;int loser=m.roundsWon==3?8:m.roundsWon==2?11:m.roundsWon==1?10:8;if(s.currentSeries!=null)loser=m.roundsWon==3?10:m.roundsWon>=1?11:10;m.score=won?13:loser;m.opponentScore=won?loser:13;heading=(won?"VITÓRIA ":"DERROTA ")+m.score+"–"+m.opponentScore;tone=won?"good":"bad";
                if(!reserve){if(won){s.wins++;s.seasonWins++;s.reputation+=6;earn(s,35*Calendar.tier(s));}else{s.losses++;s.seasonLosses++;s.reputation++;}}
                mapStats=mapStatistics(s,m,won);s.maps.add(0,mapStats);narrative+="Fim de "+m.map+": "+Catalog.team(s)+" "+m.score+" x "+m.opponentScore+" "+m.opponent+". ";
                if(s.currentSeries!=null){
                    var f=s.currentSeries;if(won)f.wins++;else f.losses++;s.match=null;
                    while(f.wins<f.bestOf/2+1&&f.losses<f.bestOf/2+1)simulateMap(s,f,won?5:-5);
                    var result=finishSeries(s,"moment");normalize(s);record(s,(result.won()?"SÉRIE VENCIDA ":"SÉRIE PERDIDA ")+result.wins()+"–"+result.losses(),narrative+" A intervenção influenciou a série; os demais mapas foram simulados. "+seriesSummary(s,List.of(result)),result.won()?"good":"bad",when,before,mapStats,List.of(result));return;
                }
                // A save with an old, unfinished single map is completed once without inventing a series.
                s.legacyMap=false;
                if(won&&Calendar.trophy(s)){s.titles++;s.trophies.add(new GameState.Trophy(s.year,m.competition,Catalog.team(s)));earn(s,(s.phase==10?15000:s.phase==4||s.phase==6?8000:3000)*Calendar.tier(s));narrative+="CAMPEÃO de "+m.competition+"! ";}
                Calendar.qualification(s,won);if(won&&(s.phase==3&&s.qualifiedMasters1||s.phase==5&&s.qualifiedMasters2))narrative+="Você garantiu vaga no próximo Masters. ";if(s.phase==8&&s.qualifiedPlayoffs)narrative+="Vaga nos Playoffs do VCT garantida. ";if(s.phase==9&&s.qualifiedChampions)narrative+="Sua equipe se classificou para o Champions. ";
                if(!reserve&&won&&mapStats.acs()>=260&&ratio(mapStats.kills(),mapStats.deaths())>=1.15){var award=new GameState.Award(s.year,"MVP — "+m.competition,"Mapa decisivo vencido com ACS ≥ 260 e K/D ≥ 1,15.");addAward(s,award);narrative+="Você recebeu o MVP desta decisão. ";}
                if(reserve)narrative+="Scrim de avaliação: menor exposição, sem premiação nem troféu do circuito.";s.match=null;
            }
        }else switch(id){
            case "prep-aim"->{skill(s,"aim",4);s.energy-=12;s.stress+=7;narrative="A mecânica evoluiu. O desgaste é temporário; a habilidade é permanente.";}
            case "prep-study"->{skill(s,"tactics",3);skill(s,"discipline",3);s.energy+=10;s.stress-=5;narrative="A revisão de mapas trouxe leitura e disciplina para a temporada.";}
            case "prep-team"->{skill(s,"communication",4);s.chemistry+=8;narrative="O grupo alinhou chamadas e execução.";}
            case "prep-igl","become-igl"->{s.igl=true;skill(s,"communication",3);skill(s,"tactics",id.equals("prep-igl")?3:2);s.chemistry+=6;narrative="Você acumulou a liderança das chamadas. Decisões de IGL recebem um bônus específico de chance.";}
            case "trial-highlight","trial-team","trial-vod"->{s.energy-=8;if(success){s.reputation+=id.equals("trial-highlight")?12:10;s.followers+=200;if(id.equals("trial-team"))s.chemistry+=8;skill(s,choice.attribute(),2);narrative="O teste chamou atenção positiva. Os olheiros passaram a acompanhar seu desenvolvimento.";}else{s.form-=4;s.stress+=6;narrative="O teste teve pontos a ajustar. A forma e o estresse mudaram, mas nenhum atributo ou OVR caiu.";}}
            case "trial-steady"->{s.reputation+=4;skill(s,"discipline",2);s.stress-=5;narrative="A comissão reconheceu seus fundamentos. Um passo consistente rumo ao mercado.";}
            case "free-agent"->{s.expiredContract=null;s.chemistry-=5;narrative="Você vai disputar o Premier com um mix, mantendo liberdade e sem salário fixo.";}
            case "honor"->{s.chemistry+=10;skill(s,"composure",2);narrative="O vínculo foi mantido até "+s.contract.endYear()+", com mais confiança no coletivo.";}
            case "renegotiate"->{if(success){var old=s.contract;s.contract=new GameState.Contract(old.orgId(),old.name(),old.tier(),old.salary()*115/100,old.endYear(),old.position(),old.pressure());narrative="A organização aceitou um aumento de 15%, sem mudar o prazo.";}else{s.stress+=8;narrative="O reajuste não foi aprovado. O vínculo continua; nenhuma habilidade foi perdida.";}}
            case "release"->{s.money-=s.contract.salary()*2;s.contract=null;s.chemistry=40;advance=false;narrative="Rescisão paga. Você continua nesta janela para escolher outra proposta.";}
            case "awards-team","awards-fans","awards-study"->{var awards=annualAwards(s);for(var award:awards)addAward(s,award);if(id.equals("awards-team"))s.chemistry+=8;if(id.equals("awards-fans"))s.followers+=400;if(id.equals("awards-study"))skill(s,"discipline",2);narrative=awards.isEmpty()?"Os números foram registrados. Esta temporada não trouxe um prêmio anual, mas sua evolução permanece.":"Prêmios registrados: "+String.join(" · ",awards.stream().map(GameState.Award::title).toList())+". Veja os critérios no histórico.";}
            case "year-develop","year-brand","year-rest","retire"->{if(id.equals("year-develop")){Catalog.ATTRIBUTES.keySet().forEach(k->skill(s,k,2));s.energy=90;s.stress-=15;}if(id.equals("year-brand")){s.followers+=500;earn(s,700+s.followers/5);skill(s,"communication",2);s.energy=85;}if(id.equals("year-rest")){s.energy=100;s.stress-=35;skill(s,"composure",3);}settle(s,1);String goalSummary=settleGoal(s);finishYear(s);s.retiring=id.equals("retire");s.pendingCards=true;advance=false;narrative=goalSummary+"A temporada de "+s.year+" foi registrada. Agora escolha uma das três cartas de evolução permanente antes de "+(s.retiring||s.age>=35?"encerrar sua trajetória.":"começar o próximo ano.");}
            default->{var offer=Catalog.offers(s).stream().filter(o->o.id().equals(id)).findFirst().orElseThrow(()->new IllegalArgumentException("Proposta inválida."));s.contract=new GameState.Contract(offer.orgId(),offer.name(),offer.tier(),offer.salary(),s.year+offer.years()-1,offer.position(),offer.pressure());s.expiredContract=null;s.chemistry=id.equals("renew")?Math.min(100,s.chemistry+10):45;s.stress+=offer.pressure()/3;narrative="Você assinou com "+offer.name()+" como "+offer.position().toLowerCase()+" até dezembro de "+s.contract.endYear()+", por R$ "+offer.salary()+"/mês. "+(Calendar.reserve(s)?"Sua experiência será focada em scrims de avaliação.":"Seu calendário passa a seguir "+Catalog.circuit(s)+".");}
        }
        if(advance)advance(s);normalize(s);record(s,heading,narrative,tone,when,before,mapStats);
    }
    private void invest(GameState s,String id){var item=Catalog.INVESTMENTS.stream().filter(i->i.id().equals(id)).findFirst().orElseThrow(()->new IllegalArgumentException("Investimento inválido."));int level=s.investments.getOrDefault(id,0);require(level<item.maxLevel(),"Este investimento já chegou ao nível máximo.");int price=item.price()*(level+1);require(s.money>=price,"Saldo insuficiente para este investimento.");s.money-=price;s.investments.put(id,level+1);permanent(s,item.bonuses());}
    private String investmentNarrative(GameState s,String id){var item=Catalog.INVESTMENTS.stream().filter(i->i.id().equals(id)).findFirst().orElseThrow();return item.name()+" chegou ao nível "+s.investments.get(id)+". Os ganhos são permanentes. "+item.extra()+" Nenhum mês avançou.";}
    private void record(GameState s,String title,String narrative,String tone,String when,Map<String,String> before,GameState.MapStats stats){var changes=changes(before,snapshot(s));s.outcome=new GameState.Outcome(title,narrative,tone,when,changes,stats);s.history.add(0,new GameState.History(when,title,narrative,tone,changes));s.revision++;}
    private void record(GameState s,String title,String narrative,String tone,String when,Map<String,String> before,GameState.MapStats stats,List<GameState.SeriesResult> series){var changes=changes(before,snapshot(s));s.outcome=new GameState.Outcome(title,narrative,tone,when,changes,stats,List.copyOf(series));s.history.add(0,new GameState.History(when,title,narrative,tone,changes));s.revision++;}
    private void advance(GameState s){settle(s,Catalog.MONTH_START[s.phase+1]-Catalog.MONTH_START[s.phase]);s.phase++;s.match=null;s.eventVariant=random.nextInt(100000);}
    private void settle(GameState s,int months){if(s.contract!=null)earn(s,s.contract.salary()*months);s.money-=Math.min(s.money,(s.age<18?50:300)*months);s.energy+=(8+s.investments.getOrDefault("fitness",0))*months;s.stress+=months==0||s.contract==null?0:s.contract.pressure()/12;}
    private void startYear(GameState s){s.campaigns.clear();s.currentSeries=null;s.tourEvent=false;s.legacyMap=false;s.seasonSeriesWins=0;s.seasonSeriesLosses=0;s.runEventCount=0;s.seenLifeEvents.clear();s.seasonGoal=null;s.goalStart=0;s.goalSettled=false;s.age++;s.year++;s.phase=0;s.seasonWins=0;s.seasonLosses=0;s.match=null;s.qualifiedMasters1=false;s.qualifiedMasters2=false;s.qualifiedPlayoffs=false;s.qualifiedChampions=false;s.qualifiedAscension=false;s.eventVariant=random.nextInt(100000);s.expiredContract=null;Catalog.refreshContractTier(s);if(s.contract!=null&&s.contract.endYear()<s.year){s.expiredContract=s.contract;s.contract=null;}}
    private GameState.Match matchFor(GameState s){if(s.match!=null)return s.match;var m=new GameState.Match();var candidates=Catalog.ORGS.stream().filter(o->o.tier()==Calendar.tier(s)&&o.region().equals(Calendar.region(s))&&!o.name().equals(Catalog.team(s))).toList();if(candidates.isEmpty())candidates=Catalog.ORGS.stream().filter(o->o.tier()==Calendar.tier(s)&&!o.name().equals(Catalog.team(s))).toList();m.opponent=candidates.isEmpty()?"Mix regional":candidates.get(Math.floorMod(Objects.hash(s.careerSeed,s.year,s.phase),candidates.size())).name();m.map=Catalog.MAPS.get(Math.floorMod(Objects.hash(s.careerSeed,s.year,s.phase,"map"),Catalog.MAPS.size()));m.competition=Calendar.reserve(s)?"Scrim — "+Calendar.competition(s):Calendar.competition(s)+" — mapa decisivo";m.finalEligible=Calendar.trophy(s);return m;}
    private GameState.Match seriesMap(GameState s,GameState.Series f){var m=new GameState.Match();m.seriesId=f.id;m.mapNumber=f.wins+f.losses+1;m.opponent=f.opponent;m.competition=f.competition;m.mapDecision=true;m.agent=mapAgent(s,f,m.mapNumber);if(f.teamOvr==null)f.teamOvr=TeamRatings.profile(s,Catalog.team(s),true).overall();if(f.opponentOvr==null)f.opponentOvr=TeamRatings.profile(s,f.opponent,false).overall();m.score=9;m.opponentScore=9;var pool=new ArrayList<>(Catalog.MAPS);Collections.shuffle(pool,new Random(Objects.hash(s.careerSeed,f.id,"veto")));m.map=pool.get((m.mapNumber-1)%pool.size());return m;}
    private int simulationChance(GameState s,GameState.Series f){return TeamRatings.chance(s,f);}
    private void simulateMap(GameState s,GameState.Series f,int momentum){var m=seriesMap(s,f);boolean won=random.nextInt(100)<clamp(simulationChance(s,f)+momentum,12,85);int loser=clamp(11-Math.abs(f.teamOvr-f.opponentOvr)/7+random.nextInt(5)-2,3,12);m.score=won?13:loser;m.opponentScore=won?loser:13;m.roundsWon=won?2:1;var stats=mapStatistics(s,m,won);s.maps.add(0,stats);if(won)f.wins++;else f.losses++;if(f.official){if(won){s.wins++;s.seasonWins++;earn(s,35*Calendar.tier(s));}else{s.losses++;s.seasonLosses++;}}s.energy-=2;}
    private String mapAgent(GameState s,GameState.Series f,int number){
        var favorites=s.favoriteAgents==null||s.favoriteAgents.isEmpty()?List.of(s.agent):s.favoriteAgents;
        var rng=new Random(Objects.hash(s.careerSeed,f.id,number,"agent"));
        return rng.nextInt(100)<60?s.agent:favorites.get(rng.nextInt(favorites.size()));
    }
    private void resolveSeriesMap(GameState s,String id,boolean won,String when,Map<String,String> before){
        var f=s.currentSeries;var m=s.match;boolean simulated=id.equals("map-sim");
        if(!simulated){var play=Events.play(id);s.energy-=Math.max(0,play.energy()-s.investments.getOrDefault("nutrition",0));if(won){skill(s,play.attribute(),1);s.reputation+=f.official?play.reputation():1;}if(play.attribute().equals("communication"))s.chemistry+=3;}
        else s.energy-=2;
        int loser=clamp(11-Math.abs(f.teamOvr-f.opponentOvr)/7+random.nextInt(5)-2,3,12);
        m.score=won?13:loser;m.opponentScore=won?loser:13;m.roundsWon=won?2:1;
        var stats=mapStatistics(s,m,won);if(simulated)stats=new GameState.MapStats(stats.year(),stats.phase(),stats.competition(),stats.map(),stats.opponent(),stats.team(),stats.won(),stats.score(),stats.opponentScore(),stats.kills(),stats.deaths(),stats.assists(),stats.combatScore(),stats.rounds(),stats.acs(),stats.kd(),stats.official(),stats.seriesId(),stats.mapNumber(),"simulated",stats.agent(),stats.teamOvr(),stats.opponentOvr());
        s.maps.add(0,stats);if(won)f.wins++;else f.losses++;
        if(f.official){if(won){s.wins++;s.seasonWins++;earn(s,35*Calendar.tier(s));}else{s.losses++;s.seasonLosses++;}}
        s.form+=won?3:-3;s.stress+=won?-3:4;
        String narrative="Mapa "+m.mapNumber+" · "+m.map+" · "+m.agent+": "+m.score+"–"+m.opponentScore+". "+(won?(simulated?"O quinteto venceu o mapa. ":"Sua decisão garantiu o mapa. "):"O adversário levou o mapa. ")+"Série: "+f.wins+"–"+f.losses+" (MD"+f.bestOf+"). ";
        boolean done=f.wins==f.bestOf/2+1||f.losses==f.bestOf/2+1;
        if(done){var result=finishSeries(s,"moment");normalize(s);record(s,(result.won()?"SÉRIE VENCIDA ":"SÉRIE PERDIDA ")+result.wins()+"–"+result.losses(),narrative+seriesSummary(s,List.of(result)),result.won()?"good":"bad",when,before,stats,List.of(result));}
        else{s.match=seriesMap(s,f);normalize(s);record(s,(won?"MAPA VENCIDO ":"MAPA PERDIDO ")+m.score+"–"+m.opponentScore,narrative+"Próximo: "+s.match.map+" · "+s.match.agent+". Uma nova decisão, em outro mapa.",won?"good":"bad",when,before,stats);}
    }
    private void seriesAction(GameState s,String id,String when,Map<String,String> before){
        require(Calendar.match(s)&&s.match==null&&!s.tourEvent,"Conclua a decisão em andamento.");var batch=new ArrayList<GameState.SeriesResult>();int phase=s.phase;
        if(id.equals("series-play")){s.currentSeries=Tour.fixture(s);var f=s.currentSeries;s.match=seriesMap(s,f);normalize(s);record(s,"Entre no servidor",f.competition+" · "+f.round+" · MD"+f.bestOf+". Uma decisão por mapa: precisa vencer "+(f.bestOf/2+1)+" mapas. Mapa 1: "+s.match.map+" · "+s.match.agent+".","neutral",when,before,null);return;}
        boolean run=id.equals("series-run"),next=id.equals("series-next");
        if(next&&!Tour.fixture(s).critical.isBlank()){record(s,"Chegou a hora de decidir",Tour.fixture(s).critical+". Assuma o momento ou escolha simular esta série.","good",when,before,null);return;}
        int limit=run?4:next?26:1;
        for(int i=0;i<limit;i++){
            s.currentSeries=Tour.fixture(s);var f=s.currentSeries;while(f.wins<f.bestOf/2+1&&f.losses<f.bestOf/2+1)simulateMap(s,f,0);batch.add(finishSeries(s,"simulated"));
            if(s.phase!=phase||s.tourEvent||!Calendar.match(s)||next&&!Tour.fixture(s).critical.isBlank())break;
        }
        normalize(s);record(s,batch.size()==1?"Série simulada":"Bloco de "+batch.size()+" séries concluído",seriesSummary(s,batch),batch.stream().filter(GameState.SeriesResult::won).count()*2>=batch.size()?"good":"bad",when,before,null,batch);
    }
    private GameState.SeriesResult finishSeries(GameState s,String mode){
        var f=s.currentSeries;boolean won=f.wins>f.losses;var maps=s.maps.stream().filter(m->f.id.equals(m.seriesId())).sorted(Comparator.comparingInt(m->m.mapNumber())).toList();
        if(mode.equals("moment")&&maps.stream().noneMatch(m->"moment".equals(m.mode())))mode="simulated";
        var result=new GameState.SeriesResult(f.id,s.year,s.phase,f.competition,f.round,f.opponent,Catalog.team(s),f.bestOf,f.wins,f.losses,won,f.official,mode,maps);s.series.add(0,result);
        if(f.official&&Calendar.tier(s)==3&&s.tierOneDebutYear==null)s.tierOneDebutYear=s.year;
        if(f.official){if(won){s.seriesWins++;s.seasonSeriesWins++;s.reputation+=3;earn(s,150*Calendar.tier(s));s.followers+=120;}else{s.seriesLosses++;s.seasonSeriesLosses++;s.reputation++;s.followers+=30;}}
        s.runEventCount++;if(s.runEventCount%4==0){skill(s,Catalog.role(s.role).attribute(),1);skill(s,"discipline",1);}s.energy+=8+s.investments.getOrDefault("fitness",0);s.stress-=4;s.form+=won?2:-1;
        Tour.result(s,won);var c=Tour.progress(s);s.match=null;s.currentSeries=null;
        if(c.done){if(c.champion&&f.official){s.titles++;s.trophies.add(new GameState.Trophy(s.year,f.competition,Catalog.team(s)));earn(s,(s.phase==10?15000:s.phase==4||s.phase==6?8000:2000)*Calendar.tier(s));}
            if(s.age<=23)Catalog.ATTRIBUTES.keySet().forEach(k->skill(s,k,1));advance(s);
        }else if(s.runEventCount%4==0){s.tourEvent=true;s.eventVariant=random.nextInt(100000);}
        if(f.official&&won&&maps.stream().anyMatch(m->m.acs()>=260&&ratio(m.kills(),m.deaths())>=1.15))addAward(s,new GameState.Award(s.year,"MVP — "+f.competition,"Série vencida com ao menos um mapa de ACS ≥ 260 e K/D ≥ 1,15."));
        return result;
    }
    private String seriesSummary(GameState s,List<GameState.SeriesResult> results){var last=results.get(results.size()-1);String text=String.join(" · ",results.stream().map(r->r.team()+" "+r.wins()+"–"+r.losses()+" "+r.opponent()).toList())+". Todos os mapas e estatísticas foram salvos. ";var c=s.campaigns.get(last.phase());if(c!=null&&c.done){text+=c.champion?"CAMPEÃO de "+last.competition()+"! ":"Participação nesta etapa concluída. ";if(last.phase()==3&&s.qualifiedMasters1||last.phase()==5&&s.qualifiedMasters2)text+="Vaga no próximo Masters garantida. ";if(last.phase()==8&&s.qualifiedPlayoffs)text+="Classificado aos playoffs regionais. ";if(last.phase()==9&&s.qualifiedChampions)text+="Vaga no Champions garantida. ";if(last.phase()==9&&s.contract!=null&&s.vctPromotions.getOrDefault(s.contract.orgId(),0)==s.year+1)text+="Acesso conquistado! "+Catalog.team(s)+" disputará o VCT "+Calendar.regionName(Calendar.region(s))+" a partir de "+(s.year+1)+". ";}else if(s.tourEvent)text+="Um acontecimento entre as séries pede sua decisão. ";else if(Calendar.match(s)&&!Tour.fixture(s).critical.isBlank())text+="Próxima pausa: "+Tour.fixture(s).critical+". ";return text;}
    private GameState.MapStats mapStatistics(GameState s,GameState.Match m,boolean won){
        int rounds=m.score+m.opponentScore,kills=0,deaths=0,assists=0,combat=0;var rng=new Random(Objects.hash(s.careerSeed,s.year,s.phase,m.map,m.roundsWon,m.seriesId,m.mapNumber));
        for(int r=0;r<rounds;r++){
            double aimChance=Math.min(.9,.32+s.attributes.get("aim")*.004+(won?.05:0));int k=rng.nextDouble()<aimChance?1:0;if(k>0&&rng.nextDouble()<.16+s.attributes.get("aim")*.001)k++;if(k>1&&rng.nextDouble()<.08)k++;
            int a=rng.nextDouble()<(.28+(s.role.equals("initiator")||s.role.equals("controller")?.15:0))?1:0;if(rng.nextDouble()<.08)a++;
            if(rng.nextDouble()<.73-s.attributes.get("composure")*.003+(won?-.04:.04))deaths++;
            int damage=Math.min(750,k*95+rng.nextInt(100));int roundScore=damage;for(int n=0;n<k;n++)roundScore+=150-20*Math.min(4,rng.nextInt(3)+n);roundScore+=Math.max(0,k-1)*50+a*25;
            kills+=k;assists+=a;combat+=roundScore;
        }
        return new GameState.MapStats(s.year,s.phase,Calendar.competition(s),m.map,m.opponent,Catalog.team(s),won,m.score,m.opponentScore,kills,deaths,assists,combat,rounds,round1((double)combat/rounds),kd(kills,deaths),!Calendar.reserve(s),m.seriesId,m.mapNumber,m.seriesId==null?"legacy":s.match==m?"moment":"simulated",m.agent,s.currentSeries==null?null:s.currentSeries.teamOvr,s.currentSeries==null?null:s.currentSeries.opponentOvr);
    }
    public static Statistics statistics(List<GameState.MapStats> maps){int kills=maps.stream().mapToInt(GameState.MapStats::kills).sum(),deaths=maps.stream().mapToInt(GameState.MapStats::deaths).sum(),assists=maps.stream().mapToInt(GameState.MapStats::assists).sum(),rounds=maps.stream().mapToInt(GameState.MapStats::rounds).sum(),score=maps.stream().mapToInt(GameState.MapStats::combatScore).sum();return new Statistics(maps.size(),kills,deaths,assists,rounds,rounds==0?0:round1((double)score/rounds),kd(kills,deaths));}
    private List<GameState.Award> annualAwards(GameState s){var stats=statistics(s.maps.stream().filter(m->m.official()&&m.year()==s.year).toList());var list=new ArrayList<GameState.Award>();if(stats.maps()>=2&&CareerHonors.rookieEligible(s)&&stats.acs()>=200)list.add(new GameState.Award(s.year,"Revelação do ano","Até 19 anos ou primeira temporada no tier 1, ao menos 2 mapas oficiais e ACS ≥ 200."));if(stats.maps()>=3&&stats.acs()>=245&&ratio(stats.kills(),stats.deaths())>=1.15)list.add(new GameState.Award(s.year,"MVP da temporada","Ao menos 3 mapas, ACS ≥ 245 e K/D ≥ 1,15."));if(stats.maps()>=3&&s.igl&&s.seasonWins>=3&&s.attributes.get("communication")>=55)list.add(new GameState.Award(s.year,"IGL do ano","IGL, comunicação ≥ 55, ao menos 3 mapas e 3 vitórias."));if(stats.maps()>=3&&stats.kills()>=80&&ratio(stats.kills(),stats.deaths())>=1.2)list.add(new GameState.Award(s.year,"Destaque da função — "+Catalog.role(s.role).name(),"Ao menos 3 mapas, 80 abates e K/D ≥ 1,20."));return list;}
    private void addAward(GameState s,GameState.Award award){if(s.awards.stream().noneMatch(a->a.year()==award.year()&&a.title().equals(award.title()))){s.awards.add(0,award);s.reputation+=3;s.followers+=250;}}
    private List<CalendarEntry> calendar(GameState s){var entries=new ArrayList<CalendarEntry>();int current=s.phase;for(int i=0;i<Catalog.PHASES.size();i++){String name=Catalog.PHASES.get(i);if(Set.of(3,4,5,6,8,9,10).contains(i)){var shadow=new GameState();shadow.contract=s.contract;shadow.nationality=s.nationality;shadow.year=s.year;shadow.vctPromotions=s.vctPromotions;shadow.phase=i;name=Calendar.competition(shadow);}String status=i<current?"Concluído":i==current?"Atual":"A seguir";if(i>=current&&(i==4||i==6||i==9||i==10)){boolean eligible=switch(i){case 4->s.qualifiedMasters1;case 6->s.qualifiedMasters2;case 9->Calendar.tier(s)==3?s.qualifiedPlayoffs:s.qualifiedAscension;default->s.qualifiedChampions;};int qualifier=i==4?3:i==6?5:i==9?8:9;status=eligible?"Classificado":current>qualifier?"Sem vaga":"Depende de classificação";}entries.add(new CalendarEntry(i,name,Catalog.MONTHS.get(i),status));}return List.copyOf(entries);}
    private String settleGoal(GameState s){if(s.seasonGoal==null||s.goalSettled)return "";var goal=PersonalLife.goal(s.seasonGoal);boolean completed=PersonalLife.progress(s,goal)>=goal.target();s.goalSettled=true;if(completed){earn(s,goal.cash());permanent(s,goal.bonuses());}s.goals.add(0,new GameState.GoalAchievement(s.year,goal.name(),completed,completed?goal.cash():0));return completed?"Objetivo alcançado: "+goal.name()+". Recompensa de R$ "+goal.cash()+" e bônus permanentes entregues. ":"O objetivo anual não foi alcançado. Nenhuma habilidade foi reduzida. ";}
    private void finishYear(GameState s){s.seasons.add(0,new GameState.Season(s.year,s.age,Catalog.team(s),s.seasonWins,s.seasonLosses,(int)s.trophies.stream().filter(t->t.year()==s.year).count(),s.earnings-s.seasons.stream().mapToInt(GameState.Season::earnings).sum(),Catalog.overall(s),s.seasonSeriesWins,s.seasonSeriesLosses));}
    private void earn(GameState s,int amount){s.money+=Math.max(0,amount);s.earnings+=Math.max(0,amount);}
    private void skill(GameState s,String key,int amount){s.attributes.compute(key,(k,v)->Math.min(99,v+growth(s,amount)));Catalog.ensureStarter(s);}
    private void normalize(GameState s){Catalog.refreshContractTier(s);Catalog.ensureStarter(s);s.energy=clamp(s.energy,0,100);s.form=clamp(s.form,0,100);s.stress=clamp(s.stress,0,100);s.chemistry=clamp(s.chemistry,0,100);s.reputation=clamp(s.reputation,0,200);s.money=Math.max(0,s.money);}
    private String date(GameState s){return Catalog.MONTHS.get(s.phase)+" de "+s.year+" · "+s.age+" anos";}
    private Map<String,String> snapshot(GameState s){var m=new LinkedHashMap<String,String>();m.put("OVR",""+Catalog.overall(s));m.put("Saldo","R$ "+s.money);m.put("Energia",""+s.energy);m.put("Forma",""+s.form);m.put("Estresse",""+s.stress);m.put("Química",""+s.chemistry);m.put("Reputação",""+s.reputation);m.put("Seguidores",""+s.followers);Catalog.ATTRIBUTES.forEach((k,v)->m.put(v,""+s.attributes.get(k)));m.put("Organização",Catalog.team(s));m.put("Posição",s.contract==null?"Agente livre":s.contract.position());m.put("Salário mensal",s.contract==null?"Sem salário":"R$ "+s.contract.salary());m.put("Títulos",""+s.titles);m.put("Mapas vencidos",""+s.wins);m.put("Mapas perdidos",""+s.losses);m.put("Séries vencidas",""+s.seriesWins);m.put("Séries perdidas",""+s.seriesLosses);m.put("Prêmios individuais",""+s.awards.size());m.put("Bônus de chance permanente",s.cardChanceBonus+"%");m.put("IGL",s.igl?"Sim":"Não");m.put("Idade",""+s.age);m.put("Temporada",""+s.year);return m;}
    private List<GameState.Change> changes(Map<String,String> a,Map<String,String> b){var list=new ArrayList<GameState.Change>();a.forEach((key,value)->{if(!value.equals(b.get(key)))list.add(new GameState.Change(key,value,b.get(key)));});return List.copyOf(list);}
    private static int clamp(int n,int min,int max){return Math.max(min,Math.min(max,n));}
    private static double ratio(int kills,int deaths){return (double)kills/Math.max(1,deaths);}
    private static String kd(int kills,int deaths){return deaths==0?(kills==0?"0.00":"∞"):String.format(Locale.ROOT,"%.2f",(double)kills/deaths);}
    private static double round1(double value){return Math.round(value*10.0)/10.0;}
    private static void require(boolean condition,String message){if(!condition)throw new IllegalArgumentException(message);}
}
