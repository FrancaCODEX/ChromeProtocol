package br.com.chromeprotocol;

import java.util.*;

/** Player-owned activities and goals. The organization keeps control of its roster and finances. */
public final class PersonalLife {
 private PersonalLife() {}
 public record Activity(String id,String category,String name,String description,Map<String,Integer> bonuses,int cash,int fans,int energy,int stress,int chemistry,int form) {}
 public record Goal(String id,String name,String description,int target,int cash,Map<String,Integer> bonuses) {}
 public static final List<Activity> ACTIVITIES=List.of(
  new Activity("vod","ESTUDO","Revisar meu VOD","Estudar seus posicionamentos com a comissão.",Catalog.bonus("tactics",2),0,0,-6,-3,4,3),
  new Activity("routine","FUNDAMENTOS","Um bloco de treino individual","Trabalhar consistência sem comandar o treino de toda a equipe.",Catalog.bonus("aim",2,"discipline",1),0,0,-12,3,0,4),
  new Activity("stream","COMUNIDADE","Abrir uma transmissão","Conversar com a comunidade e mostrar sua rotina.",Catalog.bonus("communication",1),250,350,-10,4,0,0),
  new Activity("family","VIDA PESSOAL","Desconectar com a família","Reservar um tempo para as pessoas que acompanham sua trajetória.",Catalog.bonus("composure",1),0,0,25,-15,2,0),
  new Activity("duo","VESTIÁRIO","Chamar um colega para uma duo","Fortalecer comunicação e confiança fora do treino oficial.",Catalog.bonus("communication",1),0,50,-8,-2,10,2),
  new Activity("school","FORMAÇÃO","Organizar estudos e horários","Manter os estudos e combinar uma rotina sustentável.",Catalog.bonus("discipline",2),0,0,8,-8,2,0),
  new Activity("language","CARREIRA","Praticar outro idioma","Preparar suas próprias chamadas para oportunidades internacionais.",Catalog.bonus("communication",2),0,0,-5,-2,3,0),
  new Activity("mentor","COMUNIDADE","Ajudar quem está começando","Compartilhar seus fundamentos em uma sessão aberta.",Catalog.bonus("communication",1,"discipline",1),150,200,-7,-4,4,0),
  new Activity("reset","RECUPERAÇÃO","Cuidar do sono e da rotina","Recuperar a condição para o próximo compromisso.",Catalog.bonus("composure",1),0,0,30,-12,0,3)
 );
 public static final List<Goal> GOALS=List.of(
  new Goal("contract","Conquistar meu espaço","Terminar a temporada com um contrato ativo.",1,1000,Catalog.bonus("discipline",2)),
  new Goal("wins","Uma campanha para lembrar","Vencer ao menos três mapas oficiais nesta temporada.",3,1800,Catalog.bonus("aim",2)),
  new Goal("impact","Impacto no servidor","Fechar ao menos três mapas oficiais com ACS médio de 230.",230,2000,Catalog.bonus("tactics",2)),
  new Goal("voice","Encontrar minha voz","Evoluir comunicação em oito pontos a partir da escolha da meta.",8,1200,Catalog.bonus("communication",2)),
  new Goal("community","Uma comunidade comigo","Conquistar mais 1.500 seguidores após escolher a meta.",1500,1500,Catalog.bonus("composure",2))
 );
 public static String slot(GameState s){return s.year+":"+s.phase;}
 public static boolean available(GameState s){return !s.ended&&!s.pendingCards&&s.outcome==null&&s.match==null&&!s.tourEvent&&!s.activitySlots.contains(slot(s));}
 public static List<Activity> activities(GameState s){var pool=new ArrayList<>(ACTIVITIES.stream().filter(a->!a.id().equals("school")||s.age<19).toList());Collections.shuffle(pool,new Random(Objects.hash(s.careerSeed,s.year,s.phase,"routine")));return List.copyOf(pool.subList(0,4));}
 public static int initial(GameState s,String id){return switch(id){case "voice"->s.attributes.get("communication");case "community"->s.followers;default->0;};}
 public static int progress(GameState s,Goal goal){return switch(goal.id()){
  case "contract"->s.contract==null?0:1;
  case "wins"->s.seasonWins;
  case "voice"->Math.max(0,s.attributes.get("communication")-s.goalStart);
  case "community"->Math.max(0,s.followers-s.goalStart);
  default->{var stats=GameEngine.statistics(s.maps.stream().filter(m->m.official()&&m.year()==s.year).toList());yield stats.maps()<3?0:(int)Math.floor(stats.acs());}
 };}
 public static Goal goal(String id){return GOALS.stream().filter(g->g.id().equals(id)).findFirst().orElseThrow(()->new IllegalArgumentException("Objetivo inválido."));}
}
