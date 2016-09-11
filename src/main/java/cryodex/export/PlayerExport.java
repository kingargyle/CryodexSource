package cryodex.export;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import cryodex.CryodexController;
import cryodex.Player;
import cryodex.modules.xwing.XWingComparator;
import cryodex.modules.xwing.XWingPlayer;
import cryodex.modules.xwing.XWingTournament;

public class PlayerExport {

    public static void exportPlayersDetail(){
        List<Player> players = CryodexController.getPlayers();
        
        StringBuilder sb = new StringBuilder();
        
        ExportUtils.addTableStart(sb);
        
        ExportUtils.addTableHeader(sb, "Name", "Group", "Email");
        
        for(Player p : players){
            ExportUtils.addTableRow(sb, p.getName(), p.getGroupName(), p.getEmail());
        }
        
        ExportUtils.addTableEnd(sb);
        
        ExportUtils.displayHTML(sb.toString(), "players");
    }
    
//    public static String appendRankings(XWingTournament tournament) {
//        List<XWingPlayer> playerList = new ArrayList<XWingPlayer>();
//        List<XWingPlayer> activePlayers = tournament.getXWingPlayers();
//
//        playerList.addAll(tournament.getAllXWingPlayers());
//        Collections.sort(playerList, new XWingComparator(tournament, XWingComparator.rankingCompare));
//
//        String content = "<table border=\"1\"><tr><th>Rank</th><th>Name</th><th>Score</th><th>MoV</th><th>SoS</th></tr>";
//
//        for (XWingPlayer p : playerList) {
//
//            String name = p.getName();
//
//            if (activePlayers.contains(p) == false) {
//                name = "(D#" + p.getRoundDropped(tournament) + ")" + name;
//            }
//
//            content += "<tr><td>" + p.getRank(tournament) + "</td><td>" + name + "</td><td>" + p.getScore(tournament)
//                    + "</td><td>" + p.getMarginOfVictory(tournament) + "</td><td>" + p.getAverageSoS(tournament)
//                    + "</td></tr>";
//        }
//
//        content += "</table>";
//
//        return content;
//    }
}
