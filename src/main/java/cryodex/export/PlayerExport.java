package cryodex.export;

import java.util.List;

import cryodex.CryodexController;
import cryodex.Player;

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
}
