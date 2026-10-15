package br.com.chromeprotocol;

import java.util.*;

/** Domain regressions, runnable without Spring or Maven. */
public final class EngineChecks {
    static int assertions;
    static final GameEngine win=new GameEngine(new Random(1){@Override public int nextInt(int bound){return 0;}});
    static final GameEngine lose=new GameEngine(new Random(1){@Override public int nextInt(int bound){return bound-1;}});
    static GameState fresh(){return win.create("Nova","BR","duelist","ranked","Jett",null);}
    static void check(boolean condition,String message){assertions++;if(!condition)throw new AssertionError(message);}
    static void rejects(Runnable r,String message){assertions++;try{r.run();}catch(IllegalArgumentException expected){return;}throw new AssertionError(message);}
    static void choose(GameEngine e,GameState s,String target){e.act(s,new GameEngine.Action("choice",target,s.revision));}
    static void ack(GameEngine e,GameState s){e.act(s,new GameEngine.Action("continue","",s.revision));}
    static void step(GameEngine e,GameState s,String target){choose(e,s,target);ack(e,s);}
    public static void run(){
        assertions=0;
        for(var country:Catalog.COUNTRIES)for(var role:Catalog.ROLES){GameState s=win.create("A",country.id(),role.id(),"school",role.agents().get(0),null);check(s.age==15&&s.year==2026&&s.attributes.size()==5,"All country and role combinations");}
        rejects(()->win.create("","BR","duelist","ranked","Jett",null),"Empty nick");
        rejects(()->win.create("A","XX","duelist","ranked","Jett",null),"Country whitelist");
        rejects(()->win.create("A","BR","bad","ranked","Jett",null),"Role whitelist");
        rejects(()->win.create("A","BR","duelist","bad","Jett",null),"Background whitelist");
        rejects(()->win.create("A","BR","duelist","ranked","Sage",null),"Role agent mismatch");
        GameState s=fresh();choose(win,s,"prep-aim");
        check(s.phase==1&&s.outcome!=null&&s.attributes.get("aim")==46,"Choice progresses and exposes immediate consequences");
        check(s.outcome.changes().stream().anyMatch(c->c.label().equals("Mira")),"Outcome contains actual attribute delta");
        rejects(()->choose(win,s,"trial-steady"),"Must acknowledge before next decision");
        rejects(()->win.act(s,new GameEngine.Action("continue","",0)),"Stale acknowledgement rejected");ack(win,s);
        check(s.outcome==null,"Acknowledgement clears result");
        step(win,s,"trial-steady");check(win.view(s).offers().size()==3,"Three contract offers");
        int salary=win.view(s).offers().get(1).salary();step(win,s,"offer-1");
        check(s.contract!=null&&s.contract.endYear()==2027&&s.contract.salary()==salary,"Two season agreement persisted");
        choose(win,s,"match-support");check(s.match.stage==1&&s.phase==3&&s.outcome.title().contains("Round"),"First key round is not a whole map");ack(win,s);
        step(win,s,"match-support");step(win,s,"match-support");check(s.wins==1&&s.phase==4&&s.match==null,"Three key rounds resolve a map");
        GameState bad=fresh();bad.phase=3;for(int i=0;i<3;i++)step(lose,bad,"match-aggressive");check(bad.losses==1&&bad.phase==4&&bad.money>=0,"Failures continue career");
        GameState appearance=fresh();var custom=new Appearance("soft","ebony","braids","cyan","natural","nomad","magenta");win.customize(appearance,custom,0);check(appearance.appearance.equals(custom)&&appearance.age==15&&appearance.money==500,"Cosmetics preserve career");
        rejects(()->win.customize(appearance,custom,0),"Stale appearance edit rejected");
        GameState release=fresh();release.phase=2;release.contract=new GameState.Contract("aurora","Aurora Academy",1,500,2028,"Titular",12);release.money=999;rejects(()->choose(win,release,"release"),"Cannot pay unaffordable release");release.money=1000;step(win,release,"release");check(release.contract==null&&release.phase==2&&release.money==0,"Release stays in window and charges exact fee");
        GameState elite=fresh();elite.age=18;elite.year=2029;elite.reputation=80;elite.attributes.replaceAll((k,v)->60);elite.phase=2;check(win.view(elite).offers().stream().allMatch(o->o.tier()==3),"Elite opportunities depend on age reputation and skills");elite.age=15;check(Catalog.offers(elite).stream().allMatch(o->o.tier()==1),"Start at academy circuit");
        GameState reserve=fresh();reserve.phase=3;reserve.contract=new GameState.Contract("aurora","Aurora Academy",1,500,2028,"Reserva",12);check(win.view(reserve).scene().title().contains("Scrim"),"Reserve plays evaluations");
        GameState stable=fresh();stable.phase=2;var first=win.view(stable).offers();check(first.equals(win.view(stable).offers()),"GET does not reroll market");
        for(GameEngine engine:List.of(win,lose)){
            GameState career=fresh();int decisions=0;
            while(!career.ended){
                var choices=engine.view(career).scene().choices();String target;
                if(career.phase==2)target=career.contract==null?"offer-0":"honor";
                else if(career.phase==7)target="year-develop";
                else if(career.phase==4)target="life-rest";
                else if(career.phase==1)target="trial-steady";
                else target=choices.get(0).id();
                step(engine,career,target);decisions++;
                check(career.energy>=0&&career.energy<=100&&career.stress>=0&&career.stress<=100&&career.money>=0,"Career resource bounds");
                check(decisions<400,"Career must terminate");
            }
            check(career.age==35&&career.year==2046&&career.seasons.size()==21,"Full 21-year career 15–35");
            check(career.wins+career.losses==63,"Three maps per season");
            check(career.titles==(engine==win?21:0),"Final qualification and titles reflect earlier maps");
            check(career.earnings==career.seasons.stream().mapToInt(GameState.Season::earnings).sum(),"Annual income balances lifetime earnings");
            rejects(()->choose(engine,career,"prep-aim"),"Ended career cannot advance");
        }
        GameState early=fresh();early.age=23;early.phase=7;step(win,early,"retire");check(early.ended&&early.age==23,"Optional retirement");
        GameState unqualified=fresh();unqualified.phase=6;for(int i=0;i<3;i++)step(win,unqualified,"match-support");check(unqualified.titles==0,"Winning repescagem does not award a title");
        System.out.println("Domain checks: "+assertions+" passed, 0 failures.");
    }
    public static void main(String[] args){run();}
}
