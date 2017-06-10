package cryodex.modules.starwarslcg;

import java.util.List;

import cryodex.CryodexController;
import cryodex.CryodexController.Modules;
import cryodex.Player;
import cryodex.modules.Module;
import cryodex.xml.XMLObject;
import cryodex.xml.XMLUtils;
import cryodex.xml.XMLUtils.Element;

public class SWLCGMatch implements XMLObject {

    public static final int WIN_POINTS = 3;
    public static final int BYE_POINTS = 6;
    public static final int LOSS_POINTS = 0;
    public static final int DRAW_POINTS = 1;
    
    public static enum GameResult {
        PLAYER_1_WINS,
        PLAYER_2_WINS,
        DRAW;
    }

    private SWLCGPlayer player1;
    private SWLCGPlayer player2;
    private GameResult game1Result;
    private GameResult game2Result;
    private boolean isBye = false;
    private boolean isDuplicate;
    private String matchLabel;

    public SWLCGMatch() {

    }

    public SWLCGMatch(SWLCGPlayer player1, SWLCGPlayer player2) {
        this.player1 = player1;
        this.player2 = player2;
    }

    public SWLCGMatch(Element matchElement) {

        Module m = Modules.getModuleByName(getModuleName());

        String player1String = matchElement.getStringFromChild("PLAYER1");
        Player p = CryodexController.getPlayerByID(player1String);
        if (p != null) {
            player1 = (SWLCGPlayer) p.getModuleInfoByModule(m);
        }

        String player2String = matchElement.getStringFromChild("PLAYER2");
        p = CryodexController.getPlayerByID(player2String);
        if (p != null) {
            player2 = (SWLCGPlayer) p.getModuleInfoByModule(m);
        }

        isBye = matchElement.getBooleanFromChild("ISBYE");
        isDuplicate = matchElement.getBooleanFromChild("ISDUPLICATE");
        
        String game1ResultString = matchElement.getStringFromChild("GAME1RESULT");
        if(game1ResultString != null){
            game1Result = GameResult.valueOf(game1ResultString);
        }
        
        String game2ResultString = matchElement.getStringFromChild("GAME2RESULT");
        if(game2ResultString != null){
            game2Result = GameResult.valueOf(game2ResultString);
        }
        
        matchLabel = matchElement.getStringFromChild("MATCHLABEL");
    }

    public SWLCGPlayer getPlayer1() {
        return player1;
    }

    public void setPlayer1(SWLCGPlayer player1) {
        this.player1 = player1;
    }

    public SWLCGPlayer getPlayer2() {
        return player2;
    }

    public void setPlayer2(SWLCGPlayer player2) {
        this.player2 = player2;
    }

    public GameResult getGame1Result() {
        return game1Result;
    }

    public void setGame1Result(GameResult game1Result) {
        this.game1Result = game1Result;
    }

    public GameResult getGame2Result() {
        return game2Result;
    }

    public void setGame2Result(GameResult game2Result) {
        this.game2Result = game2Result;
    }

    public boolean isBye() {
        return isBye;
    }

    public void setBye(boolean isBye) {
        this.isBye = isBye;
    }

    public boolean isDuplicate() {
        return isDuplicate;
    }

    public void setDuplicate(boolean isDuplicate) {
        this.isDuplicate = isDuplicate;
    }

    public boolean isMatchComplete(boolean isElimination) {
        if(isElimination){
            return isBye || (game1Result != null);
        } else {
            return isBye || (game1Result != null && game2Result != null);
        }
    }
    
    public SWLCGPlayer getGame1Winner(){
        if(isBye){
            return getPlayer1();
        }
        if(getGame1Result() == GameResult.PLAYER_1_WINS){
            return getPlayer1();
        } else if(getGame1Result() == GameResult.PLAYER_2_WINS){
            return getPlayer2();
        } else {
            return null;
        }
    }
    
    public SWLCGPlayer getGame1Loser(){
        if(getGame1Result() == GameResult.PLAYER_1_WINS){
            return getPlayer2();
        } else if(getGame1Result() == GameResult.PLAYER_2_WINS){
            return getPlayer1();
        } else {
            return null;
        }
    }
    
    public SWLCGPlayer getGame2Winner(){
        if(getGame2Result() == GameResult.PLAYER_1_WINS){
            return getPlayer1();
        } else if(getGame2Result() == GameResult.PLAYER_2_WINS){
            return getPlayer2();
        } else {
            return null;
        }
    }
    
    public int getPlayer1Points(){
        int points = 0;
        
        if(isBye || getGame1Result() == GameResult.PLAYER_1_WINS){
            points += WIN_POINTS;
        } else if(getGame1Result() == GameResult.DRAW){
            points += DRAW_POINTS;
        }
        
        if(isBye || getGame2Result() == GameResult.PLAYER_1_WINS){
            points += WIN_POINTS;
        } else if(getGame2Result() == GameResult.DRAW){
            points += DRAW_POINTS;
        }
        
        return points;
    }
    
    public int getPlayer2Points(){
int points = 0;
        
        if(getGame1Result() == GameResult.PLAYER_2_WINS){
            points += WIN_POINTS;
        } else if(getGame1Result() == GameResult.DRAW){
            points += DRAW_POINTS;
        }
        
        if(getGame2Result() == GameResult.PLAYER_2_WINS){
            points += WIN_POINTS;
        } else if(getGame2Result() == GameResult.DRAW){
            points += DRAW_POINTS;
        }
        
        return points;
    }

    public String getMatchLabel() {
        return matchLabel;
    }

    public void setMatchLabel(String matchLabel) {
        this.matchLabel = matchLabel;
    }

    public boolean isValidResult() {
        return true;
    }

    public void checkDuplicate(List<SWLCGRound> rounds) {

        if (this.getPlayer2() == null) {
            this.setDuplicate(false);
            return;
        }

        for (SWLCGRound r : rounds) {
            if (r.isElimination()) {
                continue;
            }

            for (SWLCGMatch match : r.getMatches()) {
                if (match.getPlayer2() == null || match == this) {
                    continue;
                }

                if ((match.getPlayer1() == this.getPlayer1() && match.getPlayer2() == this.getPlayer2())
                        || (match.getPlayer1() == this.getPlayer2() && match.getPlayer2() == this.getPlayer1())) {
                    this.setDuplicate(true);
                    return;
                }
            }
        }

        this.setDuplicate(false);
    }

    @Override
    public String toString() {
        return getPlayer1() + " vs " + getPlayer2() + " : isDuplicate=" + isDuplicate();
    }

    @Override
    public StringBuilder appendXML(StringBuilder sb) {

        XMLUtils.appendObject(sb, "PLAYER1", getPlayer1().getPlayer().getSaveId());
        XMLUtils.appendObject(sb, "PLAYER2", getPlayer2() == null ? "" : getPlayer2().getPlayer().getSaveId());
        XMLUtils.appendObject(sb, "GAME1RESULT", game1Result);
        XMLUtils.appendObject(sb, "GAME2RESULT", game2Result);
        XMLUtils.appendObject(sb, "ISBYE", isBye());
        XMLUtils.appendObject(sb, "ISDUPLICATE", isDuplicate());
        XMLUtils.appendObject(sb, "MATCHLABEL", matchLabel);

        return sb;
    }

    public static boolean hasDuplicate(List<SWLCGMatch> matches) {
        boolean duplicateFound = false;
        for (SWLCGMatch dc : matches) {
            if (dc.isDuplicate()) {
                duplicateFound = true;
                break;
            }
        }

        return duplicateFound;
    }

    public String getModuleName() {
        return Modules.SWLCG.getName();
    }
}
