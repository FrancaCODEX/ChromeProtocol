package br.com.chromeprotocol;

import java.util.*;

/** Executable domain regression suite, also invoked by JUnit. No dependencies needed. */
public final class EngineChecks {
    static int assertions;
    static final GameEngine success = new GameEngine(new Random(1) { @Override public int nextInt(int bound) { return 0; } });
    static final GameEngine failure = new GameEngine(new Random(1) { @Override public int nextInt(int bound) { return bound - 1; } });
    static void check(boolean result, String description) {
        assertions++; if (!result) throw new AssertionError(description);
    }
    static void rejects(Runnable action, String description) {
        assertions++; try { action.run(); } catch (IllegalArgumentException expected) { return; }
        throw new AssertionError(description);
    }
    static GameState newState() { return success.create("V", "street", "netrunner"); }
    static void act(GameEngine engine, GameState state, String type, String target) { engine.act(state, new GameEngine.Action(type, target, state.revision)); }
    static void prepare(GameState s, int mission) { s.missionIndex=mission; s.energy=100; s.health=100; s.money=10000; }
    public static void run() {
        assertions = 0;
        for (String origin : Catalog.ORIGINS.keySet()) for (String specialty : Catalog.SPECIALTIES.keySet()) {
            GameState s = success.create("  Nova  ", origin, specialty);
            check(s.name.equals("Nova") && s.attributes.size()==5 && s.factions.size()==3, "All character combinations initialize");
        }
        rejects(() -> success.create(" ","street","solo"), "Blank names rejected");
        rejects(() -> success.create("X".repeat(25),"street","solo"), "Long names rejected");
        rejects(() -> success.create("V\nX","street","solo"), "Control characters rejected");
        rejects(() -> success.create("V","invalid","solo"), "Invalid origins rejected");
        rejects(() -> success.create("V","street","invalid"), "Invalid specialties rejected");
        GameState s = newState();
        int money=s.money;
        rejects(() -> act(success,s,"choice","unknown"), "Unknown choices rejected");
        check(s.money==money && s.day==1 && s.revision==0, "Invalid action cannot mutate state");
        rejects(() -> success.act(s,new GameEngine.Action("work","",9)), "Stale revision rejected");
        act(success,s,"choice","alley");
        check(s.missionIndex==1 && s.day==2 && s.flags.contains("delivery"), "Successful choice advances and sets flag");
        check(s.money==780 && s.energy==85 && s.xp==55, "Reward and energy apply");
        act(success,s,"choice","protect");
        check(s.flags.contains("lia") && s.level==2 && s.skillPoints==1 && s.xp==10, "Ally flag and level rewards persist");
        check(success.view(s).mission().choices().stream().filter(c -> c.id().equals("lia_route")).findFirst().orElseThrow().available(), "Earlier ally opens future route");
        act(success,s,"choice","lia_route");
        check(s.flags.contains("clinic"), "Ally route recovers clinic cargo");
        GameState failed = newState(); act(failure,failed,"choice","roof");
        check(failed.missionIndex==1 && failed.health==80 && failed.money==600 && !failed.flags.contains("delivery"), "Failure continues story with reduced reward");
        GameState locked = newState(); prepare(locked,2);
        rejects(() -> act(success,locked,"choice","lia_route"), "Locked narrative route rejected");
        prepare(locked,3); rejects(() -> act(success,locked,"choice","network"), "Missing implant route rejected");
        act(success,locked,"buy","deck");
        check(success.effective(locked,"intelligence")==7 && locked.implants.size()==1, "Implant improves effective attribute");
        check(locked.attributes.get("intelligence")==5, "Implant does not overwrite base stat");
        rejects(() -> act(success,locked,"buy","deck"), "Duplicate implants rejected");
        act(success,locked,"buy","arms"); act(success,locked,"buy","legs");
        rejects(() -> act(success,locked,"buy","skin"), "Three slot limit enforced");
        act(success,locked,"choice","network"); check(locked.flags.contains("audit"), "Cyberdeck unlocks mission");
        GameState broke = newState(); broke.money=0; broke.energy=0;
        rejects(() -> act(success,broke,"work",""), "Energy requirement enforced");
        rejects(() -> act(success,broke,"train","tech"), "Training resource requirement enforced");
        rejects(() -> act(success,broke,"buy","deck"), "Purchase affordability enforced");
        act(success,broke,"rest",""); act(success,broke,"work","");
        check(broke.energy==30 && broke.money==180 && broke.day==3, "Free rest and work prevent resource softlock");
        act(success,broke,"train","tech"); check(broke.attributes.get("tech")==4 && broke.money==30 && broke.energy==10, "Training charges resources and raises stat");
        GameState rent = newState(); rent.day=7; rent.money=20; act(success,rent,"rest","");
        check(rent.day==8 && rent.money==0 && rent.debt==100, "Weekly rent cannot make balance negative");
        rejects(() -> act(success,rent,"debt",""), "Debt needs sufficient balance");
        act(success,rent,"work",""); act(success,rent,"debt","");
        check(rent.money==80 && rent.debt==0, "Debt payment is exact");
        GameState critical = newState(); prepare(critical,6); critical.health=10; critical.money=0;
        act(failure,critical,"choice","hunt");
        check(critical.health==40 && critical.money==0 && critical.debt==50 && critical.missionIndex==7, "Emergency rescue continues career and records unpaid cost");
        GameState hunted = newState(); hunted.heat=80; hunted.energy=10; act(success,hunted,"rest","");
        check(hunted.heat==72 && hunted.energy==50, "High heat applies daily energy penalty");
        GameState stats = newState(); stats.skillPoints=1; act(success,stats,"upgrade","body");
        check(stats.attributes.get("body")==4 && stats.skillPoints==0 && stats.day==1, "Level point does not cost a day");
        rejects(() -> act(success,stats,"upgrade","body"), "Cannot spend absent points");
        stats.attributes.put("body",10); stats.skillPoints=1;
        rejects(() -> act(success,stats,"upgrade","body"), "Attribute cap enforced");
        for (int i=0;i<Catalog.MISSIONS.size();i++) {
            Catalog.Mission mission=Catalog.MISSIONS.get(i);
            check(mission.choices().stream().map(Catalog.Choice::id).distinct().count()==mission.choices().size(), "Choice IDs unique in " + mission.id());
            for (Catalog.Choice c:mission.choices()) {
                GameState trial=newState(); prepare(trial,i);
                if (!c.flagRequired().isEmpty()) trial.flags.add(c.flagRequired());
                if (!c.implant().isEmpty()) trial.implants.add(c.implant());
                act(success,trial,"choice",c.id());
                check(trial.missionIndex==i+1 && trial.money>=0 && trial.energy>=0 && trial.health>0, "Success path valid: " + mission.id()+"/"+c.id());
                GameState miss=newState(); prepare(miss,i);
                if (!c.flagRequired().isEmpty()) miss.flags.add(c.flagRequired());
                if (!c.implant().isEmpty()) miss.implants.add(c.implant());
                act(failure,miss,"choice",c.id());
                check(miss.missionIndex==i+1 && miss.money>=0 && miss.energy>=0 && miss.health>0, "Failure path valid: " + mission.id()+"/"+c.id());
            }
        }
        GameState memory=newState(); prepare(memory,2);
        int before=success.view(memory).mission().choices().get(0).chance(); memory.flags.add("lia");
        check(success.view(memory).mission().choices().get(0).chance()>before && !success.view(memory).mission().memory().isEmpty(), "Previous choices affect odds and narrative");
        for (String ending:List.of("free","power","leave")) {
            GameState finalState=newState(); prepare(finalState,9); finalState.flags.add("key"); finalState.flags.add("pledge_helix");
            act(success,finalState,"choice",ending);
            check(finalState.ended && finalState.ending!=null && success.view(finalState).mission()==null, "Ending available: "+ending);
            rejects(() -> act(success,finalState,"work",""), "Ended career immutable");
        }
        GameState campaign=newState();
        String[] route={"bribe","protect","lia_route","cover","repair","toll","erase","kira","district","free"};
        for (String choice:route) {
            while (campaign.energy<40) act(success,campaign,"rest","");
            while (campaign.money<300) { if(campaign.energy<15)act(success,campaign,"rest",""); act(success,campaign,"work",""); }
            act(success,campaign,"choice",choice);
        }
        check(campaign.ended && campaign.ending.equals("A cidade sem coleira") && campaign.missionIndex==10, "Full campaign completes with economy and recovery");
        System.out.println("Domain checks: " + assertions + " passed, 0 failures.");
    }
    public static void main(String[] args) { run(); }
}
