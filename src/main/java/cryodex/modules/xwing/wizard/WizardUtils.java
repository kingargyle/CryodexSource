package cryodex.modules.xwing.wizard;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cryodex.modules.xwing.XWingComparator;
import cryodex.modules.xwing.XWingModule;
import cryodex.modules.xwing.XWingPlayer;
import cryodex.modules.xwing.XWingTournament;
import cryodex.modules.xwing.XWingTournament.InitialSeedingEnum;

public class WizardUtils {
    
    public static enum SplitOptions {
        GROUP,
        RANKING,
        RANDOM;
    }
    
    public static void createSplitTournament(SplitOptions splitOption){
        
        WizardOptions wizardOptions = XWingWizard.getInstance().getWizardOptions();
        
        if (wizardOptions.getSplit() > 1) {
            Integer splitNum = wizardOptions.getSplit();

            List<WizardOptions> wizardOptionsList = new ArrayList<WizardOptions>();

            for (int i = 1; i <= splitNum; i++) {
                WizardOptions newWizardOption = new WizardOptions(wizardOptions);

                wizardOptionsList.add(newWizardOption);

                newWizardOption.setName(wizardOptions.getName() + " " + i);
                newWizardOption.setPlayerList(new ArrayList<XWingPlayer>());
            }

            if (splitOption == SplitOptions.GROUP) {
                Map<String, List<XWingPlayer>> playerMap = new HashMap<String, List<XWingPlayer>>();

                // Add players to map
                for (XWingPlayer p : wizardOptions.getPlayerList()) {
                    List<XWingPlayer> playerList = playerMap.get(p.getPlayer().getGroupName());

                    if (playerList == null) {
                        playerList = new ArrayList<>();
                        String groupName = p.getPlayer().getGroupName() == null ? "" : p.getPlayer().getGroupName();
                        playerMap.put(groupName, playerList);
                    }

                    playerList.add(p);
                }

                int j = 0;
                for (String groupValue : playerMap.keySet()) {

                    List<XWingPlayer> playerList = playerMap.get(groupValue);

                    while (playerList.isEmpty() == false) {

                        wizardOptionsList.get(j).getPlayerList().add(playerList.get(0));
                        j = j == splitNum - 1 ? 0 : j + 1;
                        playerList.remove(0);
                    }
                }

                //
                int first = 0;
                int last = wizardOptionsList.size() - 1;
                
                    while (first < last) {

                        if (wizardOptionsList.get(last).getPlayerList().size() % 2 == 0) {
                            last--;
                        } else {
                            if (wizardOptionsList.get(first).getPlayerList().size() % 2 == 1
                                    && wizardOptionsList.get(last).getPlayerList().size() % 2 == 1) {
                                XWingPlayer p = wizardOptionsList.get(first).getPlayerList()
                                        .get(wizardOptionsList.get(first).getPlayerList().size() - 1);

                                wizardOptionsList.get(first).getPlayerList().remove(p);

                                wizardOptionsList.get(last).getPlayerList().add(p);
                            }
                            first++;
                        }
                    }
                
            } else if (splitOption == SplitOptions.RANKING) {
                List<XWingPlayer> tempPlayers = WizardUtils.rankMergedPlayers(wizardOptions);

                int ppt = tempPlayers.size() / splitNum;
                int overage = tempPlayers.size() % splitNum;
                for (int j = 0; j < splitNum; j++) {
                    int count = j >= splitNum - overage ? ppt + 1 : ppt;
                    wizardOptionsList.get(j).getPlayerList().addAll(tempPlayers.subList(0, count));
                    tempPlayers = tempPlayers.subList(count, tempPlayers.size());
                }

                for (int i = 0; i < wizardOptionsList.size(); i++) {
                    // if (wizardOptionsList.get(i).getPlayerList().size() %
                    // 2 == 0) {
                    // continue;
                    // }

                    while (i + 1 < wizardOptionsList.size() && (wizardOptionsList.get(i).getPlayerList().size() % 2 == 1
                            || wizardOptionsList.get(i).getPlayerList().size() > wizardOptionsList.get(i + 1).getPlayerList().size())) {
                        XWingPlayer t1 = wizardOptionsList.get(i).getPlayerList().get(wizardOptionsList.get(i).getPlayerList().size() - 1);
                        wizardOptionsList.get(i).getPlayerList().remove(t1);
                        List<XWingPlayer> temp = new ArrayList<XWingPlayer>();
                        temp.addAll(wizardOptionsList.get(i + 1).getPlayerList());
                        wizardOptionsList.get(i + 1).getPlayerList().clear();
                        wizardOptionsList.get(i + 1).getPlayerList().add(t1);
                        wizardOptionsList.get(i + 1).getPlayerList().addAll(temp);
                    }

                }
            } else if(splitOption == SplitOptions.RANDOM){
                List<XWingPlayer> playerList = wizardOptions.getPlayerList();
                Collections.shuffle(playerList);
                int j = 0;
                while (playerList.isEmpty() == false) {

                    wizardOptionsList.get(j).getPlayerList().add(playerList.get(0));
                    j = j == splitNum - 1 ? 0 : j + 1;
                    playerList.remove(0);
                }

                //
                int first = 0;
                int last = wizardOptionsList.size() - 1;
                
                    while (first < last) {

                        if (wizardOptionsList.get(last).getPlayerList().size() % 2 == 0) {
                            last--;
                        } else {
                            if (wizardOptionsList.get(first).getPlayerList().size() % 2 == 1
                                    && wizardOptionsList.get(last).getPlayerList().size() % 2 == 1) {
                                XWingPlayer p = wizardOptionsList.get(first).getPlayerList()
                                        .get(wizardOptionsList.get(first).getPlayerList().size() - 1);

                                wizardOptionsList.get(first).getPlayerList().remove(p);

                                wizardOptionsList.get(last).getPlayerList().add(p);
                            }
                            first++;
                        }
                    }
                
            }

            XWingWizard.getInstance().setVisible(false);

            for (WizardOptions wo : wizardOptionsList) {
                if (wo.getInitialSeedingEnum() == InitialSeedingEnum.IN_ORDER) {
                    List<XWingPlayer> tempList = WizardUtils.rankMergedPlayers(wo);
                    wo.setPlayerList(tempList);
                }
                XWingModule.makeTournament(wo);
            }
        }
    }
    
    
    public static void createTournament(){
            XWingWizard.getInstance().setVisible(false);
            XWingModule.makeTournament(XWingWizard.getInstance().getWizardOptions());
        
    }
    
    public static List<XWingPlayer> rankMergedPlayers(WizardOptions wizardOptions) {
        
        XWingTournament mergeTournament = new XWingTournament("", wizardOptions.getPlayerList(), null, wizardOptions.getSelectedTournaments().get(0).getPoints(), false,
                false);
        
        for (XWingTournament t : wizardOptions.getSelectedTournaments()) {
            mergeTournament.getAllRounds().addAll(t.getAllRounds());
        }

        List<XWingPlayer> tempPlayers = new ArrayList<XWingPlayer>();
        tempPlayers.addAll(wizardOptions.getPlayerList());
        Collections.sort(tempPlayers, new XWingComparator(mergeTournament, XWingComparator.rankingCompare));
        return tempPlayers;
    }
    
    public static XWingTournament getMergedTournament(WizardOptions wizardOptions) {
        
        XWingTournament mergeTournament = new XWingTournament("", wizardOptions.getPlayerList(), null, wizardOptions.getSelectedTournaments().get(0).getPoints(), false,
                false);
        
        for (XWingTournament t : wizardOptions.getSelectedTournaments()) {
            mergeTournament.getAllRounds().addAll(t.getAllRounds());
        }
        return mergeTournament;
    }
}
