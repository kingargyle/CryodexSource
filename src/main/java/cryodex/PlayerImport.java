package cryodex;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JFileChooser;

import cryodex.modules.xwing.XWingPlayer;

public class PlayerImport {

	public static void importPlayers(){
		BufferedReader reader = null;
		try {
			JFileChooser fc = new JFileChooser();
			
			int returnVal = fc.showOpenDialog(Main.getInstance());

			if (returnVal == JFileChooser.APPROVE_OPTION) {
			    File file = fc.getSelectedFile();
			    
				reader = new BufferedReader(new FileReader(file));
				
				String line = reader.readLine();
				
				Map<String, Integer> headerMap = new HashMap<String, Integer>();
				
				String[] headers = line.split(",");
				int i = 0;
				for(String s : headers){
					headerMap.put(s, i);
					i++;
				}
				
				Integer firstName = headerMap.get("\"First Name\"");
				Integer lastName = headerMap.get("\"Last Name\"");
				Integer name = headerMap.get("\"Name\"");
				Integer emailAddress = headerMap.get("\"Email Address\"");
                Integer group = headerMap.get("\"Group\"");
                Integer squad = headerMap.get("\"Squad\"");
				
				List<Player> players = new ArrayList<Player>();

				line = reader.readLine();
				
				while(line != null){
					
					String[] playerLine = line.split(",");
					
					String playerName = null;
					
					if(firstName != null && lastName != null){
						playerName = playerLine[firstName] + " " + playerLine[lastName];
					} else if(name != null){
						playerName = playerLine[name];
					}
					
					playerName = playerName.replace("\"", "");
					
					Player p = new Player(playerName);
					
					p.setEmail(getString(emailAddress, playerLine));
					p.setGroupName(getString(group, playerLine));
					
					XWingPlayer xp = new XWingPlayer(p);
					xp.setSquadId(getString(squad, playerLine));
					
					p.getModuleInfo().add(xp);
					
					players.add(p);

					line = reader.readLine();
				}

				Main.getInstance().getRegisterPanel().importPlayers(players);
			}
		} catch (Exception e) {
			try {
				if(reader != null){
					reader.close();
				}
			} catch (IOException e1) {}
			e.printStackTrace();
		}
	}
	
	private static String getString(Integer column, String[] playerLine) {
	    String returnString = null;
	    
	    if(column != null){
            returnString = playerLine[column];
            returnString = returnString.replace("\"", "");
        }
	    
	    return returnString;
    }
}
