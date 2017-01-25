package cryodex.modules.xwing.wizard;

import java.util.ArrayList;
import java.util.List;

import cryodex.modules.xwing.XWingPlayer;
import cryodex.modules.xwing.XWingTournament;
import cryodex.modules.xwing.XWingTournament.InitialSeedingEnum;

public class WizardOptions {

    private String name;
    private InitialSeedingEnum initialSeedingEnum;
    private List<XWingPlayer> playerList;
    private List<Integer> points;
    private int split = 1;
    private boolean isMerge = false;
    private List<XWingTournament> selectedTournaments;
    private boolean isSingleElimination = false;
    private boolean isRoundRobin = false;
    private int minPoints = 0;
    private int maxPlayers = 0;

    public WizardOptions() {

    }

    public WizardOptions(WizardOptions wizardOptions) {
        this.name = wizardOptions.getName();
        this.initialSeedingEnum = wizardOptions.getInitialSeedingEnum();
        this.points = wizardOptions.getPoints();
        this.isSingleElimination = wizardOptions.isSingleElimination();
        this.isRoundRobin = wizardOptions.isRoundRobin();
    }

    public boolean isRoundRobin() {
        return isRoundRobin;
    }

    public void setRoundRobin(boolean isRoundRobin) {
        this.isRoundRobin = isRoundRobin;
    }

    public InitialSeedingEnum getInitialSeedingEnum() {
        return initialSeedingEnum;
    }

    public void setInitialSeedingEnum(InitialSeedingEnum initialSeedingEnum) {
        this.initialSeedingEnum = initialSeedingEnum;
    }

    public List<XWingPlayer> getPlayerList() {
        return playerList;
    }

    public void setPlayerList(List<XWingPlayer> playerList) {
        this.playerList = playerList;
    }

    public void setPoints(Integer points){
        this.points = new ArrayList<>();
        this.points.add(points);
    }
    
    public List<Integer> getPoints() {
        return points;
    }

    public void setPoints(List<Integer> points) {
        this.points = points;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getSplit() {
        return split;
    }

    public void setSplit(int split) {
        this.split = split;
    }

    public boolean isMerge() {
        return isMerge;
    }

    public void setMerge(boolean isMerge) {
        this.isMerge = isMerge;
    }

    public List<XWingTournament> getSelectedTournaments() {
        return selectedTournaments;
    }

    public void setSelectedTournaments(List<XWingTournament> selectedTournaments) {
        this.selectedTournaments = selectedTournaments;
    }

    public boolean isSingleElimination() {
        return isSingleElimination;
    }

    public void setSingleElimination(boolean isSingleElimination) {
        this.isSingleElimination = isSingleElimination;
    }

    public int getMinPoints() {
        return minPoints;
    }

    public void setMinPoints(int minPoints) {
        this.minPoints = minPoints;
    }

    public int getMaxPlayers() {
        return maxPlayers;
    }

    public void setMaxPlayers(int maxPlayers) {
        this.maxPlayers = maxPlayers;
    }
}