package br.com.chromeprotocol;

import java.util.*;

/** Fictional situations; selections are stable for a saved career, year, phase and round. */
public final class Events {
    private Events() {}
    public record Play(String id,String title,String description,String attribute,int difficulty,int energy,int reputation) {}
    public record LifeOption(String id,String title,String description,String attribute,int difficulty,Map<String,Integer> bonuses,int cash,int followers,int chemistry,int energy,int stress) {}
    public record Life(String title,String description,List<LifeOption> choices) {}
    public record Round(String title,String description,List<String> plays) {}
    public static final List<Play> PLAYS=List.of(
        new Play("match-aggressive","Assumir o duelo","Buscar a primeira eliminação e abrir espaço.","aim",12,12,4),
        new Play("match-control","Mudar a leitura","Alterar a execução com controle de mapa.","tactics",5,8,2),
        new Play("match-support","Combinar a troca","Manter distância de troca e jogar junto.","communication",0,5,2),
        new Play("match-eco","Planejar a economia","Preservar recursos para executar o próximo round.","discipline",1,4,2),
        new Play("match-hero","Arriscar a hero play","Tentar mudar o ritmo com um duelo de alto risco.","aim",17,13,6),
        new Play("match-clutch","Assumir o clutch","Respirar, isolar o primeiro duelo e jogar o tempo.","composure",8,10,5),
        new Play("match-utility","Guardar o utilitário","Usar a habilidade na janela decisiva do pós-plant.","tactics",2,5,3),
        new Play("match-save","Jogar pela segurança","Reduzir a exposição e valorizar a informação.","composure",0,3,1),
        new Play("match-retake","Organizar o retake","Sincronizar entradas por dois acessos.","communication",4,7,3),
        new Play("match-lurk","Explorar a rotação","Buscar uma lacuna no lado oposto do mapa.","tactics",7,8,4),
        new Play("match-op","Assumir o Operator","Confiar na precisão para controlar a linha longa.","aim",10,10,4),
        new Play("match-igl","Chamar uma adaptação como IGL","Ler o rival, pedir uma pausa e redesenhar a execução.","communication",1,6,4),
        new Play("match-fake","Vender uma execução falsa","Provocar a rotação antes de mudar de bombsite.","tactics",6,7,4)
    );
    public static final List<Round> ROUNDS=List.of(
        new Round("A economia apertou.","O adversário espera um avanço previsível. Vale a pena arriscar agora?",List.of("match-eco","match-hero","match-support")),
        new Round("Poucos segundos no pós-plant.","O utilitário é limitado e cada segundo conta.",List.of("match-clutch","match-utility","match-save")),
        new Round("A spike caiu no bombsite.","O quinteto precisa decidir como coordenar a retomada.",List.of("match-retake","match-support","match-igl")),
        new Round("Eles leram a sua defesa.","O adversário começou a antecipar a marcação das últimas rodadas.",List.of("match-lurk","match-control","match-op")),
        new Round("O público aumentou o volume.","Chegou um round importante, e o rival está tentando acelerar.",List.of("match-clutch","match-control","match-aggressive")),
        new Round("Uma adaptação inesperada.","O rival mudou o ritmo depois de uma pausa tática.",List.of("match-fake","match-support","match-igl")),
        new Round("A entrada ficou livre por um instante.","Uma janela curta separa a execução de uma rotação completa.",List.of("match-aggressive","match-utility","match-lurk")),
        new Round("O sniper adversário travou o avanço.","Seu time precisa responder à pressão da linha longa.",List.of("match-op","match-fake","match-retake")),
        new Round("O plano saiu do controle.","A informação inicial não se confirmou. Ainda dá tempo de adaptar.",List.of("match-save","match-igl","match-hero")),
        new Round("Uma decisão para o replay.","O próximo movimento pode definir a leitura do mapa inteiro.",List.of("match-control","match-clutch","match-eco"))
    );
    private static LifeOption o(String id,String title,String text,String attr,int difficulty,Map<String,Integer> stats,int cash,int fans,int chemistry,int energy,int stress){return new LifeOption(id,title,text,attr,difficulty,stats,cash,fans,chemistry,energy,stress);}
    private static Life l(String title,String text,LifeOption... choices){return new Life(title,text,List.of(choices));}
    public static final List<Life> LIFE=List.of(
        l("Uma marca quer o seu rosto.","Uma campanha de periféricos disputa espaço com seus treinos.",
            o("sponsor","Aceitar a campanha","Exposição e renda, com uma agenda mais cheia.","communication",4,Catalog.bonus("communication",2),1200,600,0,-12,8),
            o("schedule","Negociar uma agenda menor","Preservar a preparação sem fechar portas.","discipline",0,Catalog.bonus("discipline",2),500,250,3,-4,0),
            o("focus","Ficar com o treino","Deixar a campanha passar e focar no coletivo.","",0,Catalog.bonus("tactics",2),0,50,8,10,-5)),
        l("Um clipe virou assunto.","Sua última jogada está circulando. A torcida quer uma resposta.",
            o("analysis","Explicar a jogada","Mostrar seu raciocínio em uma análise pública.","tactics",3,Catalog.bonus("tactics",2),300,700,3,-6,3),
            o("humor","Levar com bom humor","Aproximar a comunidade com uma resposta leve.","communication",2,Catalog.bonus("communication",2),250,600,0,-5,-4),
            o("silence","Voltar para a rotina","Deixar o servidor responder nas próximas partidas.","",0,Catalog.bonus("discipline",2),0,100,4,10,-8)),
        l("Duas chamadas, um grupo.","Colegas discordaram de uma execução. O treino parou para conversar.",
            o("mediate","Ouvir os dois lados","Transformar o conflito em um plano comum.","communication",4,Catalog.bonus("communication",3),0,100,15,-5,-6),
            o("vod","Voltar ao VOD","Usar informação para alinhar a leitura.","tactics",2,Catalog.bonus("tactics",3),0,60,10,-4,-3),
            o("reset","Pedir uma pausa","Recomeçar a conversa com a cabeça mais tranquila.","",0,Catalog.bonus("composure",2),0,20,7,20,-15)),
        l("O meta mudou no meio da preparação.","Uma atualização pede novas composições e repertório.",
            o("lab","Testar composições novas","Criar soluções no servidor de treino.","tactics",6,Catalog.bonus("tactics",4),0,200,8,-10,5),
            o("specialist","Aprimorar seu agente","Transformar conforto em consistência.","aim",2,Catalog.bonus("aim",3),0,120,5,-6,2),
            o("learn","Trocar experiências","Reunir referências e estudar em grupo.","",0,Catalog.bonus("communication",2,"discipline",2),0,80,10,5,-3)),
        l("Um convite para uma showmatch.","Você foi chamado para uma partida com criadores de conteúdo.",
            o("show","Jogar para entreter","Highlights podem trazer público novo.","aim",7,Catalog.bonus("aim",2),800,1000,0,-12,4),
            o("teach","Comentar as decisões","Mostrar o lado tático da sua função.","communication",1,Catalog.bonus("communication",3),500,700,2,-5,0),
            o("decline","Preservar o descanso","Agradecer o convite e cuidar da rotina.","",0,Catalog.bonus("composure",2),0,50,3,30,-15)),
        l("Uma viagem ficou incerta.","A logística mudou e a equipe precisa reorganizar a preparação.",
            o("lead","Ajudar a organizar","Alinhar horários, documentos e expectativas.","discipline",3,Catalog.bonus("discipline",3),0,80,10,-5,-3),
            o("remote","Adaptar os treinos online","Manter a qualidade mesmo fora do plano.","tactics",4,Catalog.bonus("tactics",3),0,100,8,-8,2),
            o("trust","Confiar na comissão","Usar a espera para recuperar energia.","",0,Catalog.bonus("composure",2),0,30,5,25,-10)),
        l("Sua família pediu um fim de semana.","A competição ocupa espaço, mas há vida fora do servidor.",
            o("family","Passar a pausa com a família","Recarregar as energias sem abandonar o projeto.","",0,Catalog.bonus("discipline",2,"composure",2),0,20,3,35,-20),
            o("share","Mostrar como é sua rotina","Aproximar as pessoas da sua trajetória.","communication",0,Catalog.bonus("communication",3),200,350,4,15,-10),
            o("balance","Combinar um horário sustentável","Organizar treino e tempo pessoal.","",0,Catalog.bonus("discipline",3),0,50,6,25,-12)),
        l("Um rookie pediu sua ajuda.","Um jogador mais novo quer aprender com sua experiência.",
            o("mentor","Fazer uma sessão de mentoria","Ensinar fundamentos e aprender explicando.","communication",0,Catalog.bonus("communication",3),400,400,8,-5,-4),
            o("duo","Jogar uma fila em dupla","Trabalhar leitura e decisões em conjunto.","tactics",3,Catalog.bonus("tactics",3),100,250,5,-8,1),
            o("guide","Publicar um guia","Transformar seu repertório em conteúdo.","discipline",2,Catalog.bonus("discipline",2,"communication",1),500,500,0,-6,2)),
        l("A conexão caiu no treino.","O grupo perdeu a sessão planejada e precisa aproveitar o tempo.",
            o("offline","Treinar fundamentos offline","Não desperdiçar a janela de preparação.","",0,Catalog.bonus("aim",3),0,40,2,-5,0),
            o("review","Fazer uma revisão coletiva","Debater execuções sem depender do servidor.","communication",1,Catalog.bonus("communication",2,"tactics",2),0,60,12,-3,-4),
            o("recover","Adiantar a recuperação","Retornar descansado no próximo bloco.","",0,Catalog.bonus("composure",2),0,20,4,30,-15)),
        l("Uma entrevista antes do palco.","Uma conversa ao vivo pode levar sua história a outra audiência.",
            o("ambition","Falar sobre seus objetivos","Assumir a ambição diante da torcida.","communication",5,Catalog.bonus("communication",3),600,900,0,-5,5),
            o("credit","Valorizar a equipe","Compartilhar os méritos e os planos do grupo.","communication",0,Catalog.bonus("communication",2),300,450,12,-3,-2),
            o("short","Manter a entrevista curta","Preservar o foco e a tranquilidade.","",0,Catalog.bonus("composure",2),100,100,4,10,-8))
    );
    public static Round round(GameState s,int stage){return ROUNDS.get(Math.floorMod(Objects.hash(s.careerSeed,s.year,s.phase,stage),ROUNDS.size()));}
    public static Life life(GameState s){return LIFE.get(Math.floorMod(Objects.hash(s.careerSeed,s.year,s.phase,s.eventVariant),LIFE.size()));}
    public static Play play(String id){return PLAYS.stream().filter(p->p.id().equals(id)).findFirst().orElseThrow();}
}
