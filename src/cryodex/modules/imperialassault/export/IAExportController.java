package cryodex.modules.imperialassault.export;

import java.awt.Desktop;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import cryodex.CryodexController;
import cryodex.Player;
import cryodex.modules.imperialassault.IAComparator;
import cryodex.modules.imperialassault.IAMatch;
import cryodex.modules.imperialassault.IAPlayer;
import cryodex.modules.imperialassault.IARound;
import cryodex.modules.imperialassault.IATournament;

public class IAExportController {

	public static String appendRankings(IATournament tournament) {
		List<IAPlayer> playerList = new ArrayList<IAPlayer>();
		List<IAPlayer> activePlayers = tournament.getIAPlayers();

		playerList.addAll(tournament.getAllIAPlayers());
		Collections.sort(playerList, new IAComparator(tournament,
				IAComparator.rankingCompare));

		String content = "<table border=\"1\"><tr><td>Rank</td><td>Name</td><td>Score</td><td>SoS</td><td>Ext SoS</td></tr>";

		for (IAPlayer p : playerList) {

			String name = p.getName();

			if (activePlayers.contains(p) == false) {
				name = "(D#" + p.getRoundDropped(tournament) + ")" + name;
			}

			content += "<tr><td>" + p.getRank(tournament) + "</td><td>" + name
					+ "</td><td>" + p.getScore(tournament) + "</td><td>"
					+ p.getAverageSoS(tournament) + "</td><td>"
					+ p.getExtendedStrengthOfSchedule(tournament)
					+ "</td></tr>";
		}

		content += "</table>";

		return content;
	}

	public static void exportRankings(IATournament tournament) {

		String content = appendRankings(tournament);

		displayHTML(content, "ExportRankings");
	}

	public static String appendMatches(IATournament tournament,
			List<IAMatch> matches) {
		String content = "";

		int counter = 1;
		for (IAMatch m : matches) {
			String matchString = "";
			if (m.getPlayer2() == null) {
				matchString += m.getPlayer1().getName() + " has a BYE";
			} else {
				matchString += m.getPlayer1().getName() + " VS "
						+ m.getPlayer2().getName();
				if (CryodexController.getOptions().isShowTableNumbers()) {
					matchString = counter + ": " + matchString;
					counter++;
				}

				if (m.isMatchComplete()) {
					matchString += " - Match Results: ";
					if (m.getWinner() != null) {
						matchString += m.getWinner().getName()
								+ " is the winner";
					}
				}
			}
			content += "<div>" + matchString + "</div>";
		}

		return content;
	}

	public static void exportMatches(IATournament tournament,
			List<IAMatch> matches, int roundNumber) {
		String content = "";

		if (roundNumber == 0) {
			content += "<h3>Top " + (matches.size() * 2) + "</h3>";
		} else {
			content += "<h3>Round " + roundNumber + "</h3>";
		}

		content += appendMatches(tournament, matches);

		displayHTML(content, "ExportMatch");
	}

	public static void exportTournamentReport(IATournament tournament) {
		String content = "";
		int roundNumber = 1;
		for (IARound r : tournament.getAllRounds()) {
			if (r.isSingleElimination()) {
				content += "<h3>Top " + (r.getMatches().size() * 2) + "</h3>";
			} else {
				content += "<h3>Round " + roundNumber + "</h3>";
			}
			content += appendMatches(tournament, r.getMatches());

			roundNumber++;
		}

		content += "<h3>Rankings</h3>";
		content += appendRankings(tournament);

		displayHTML(content, "TournamentReport");
	}

	public static void exportTournamentSlipsWithStats(IATournament tournament,
			List<IAMatch> matches, int roundNumber) {

		String content = "";

		int counter = 1;
		for (IAMatch m : matches) {
			String matchString = "";
			if (m.getPlayer2() != null) {
				matchString += "<table width=100%><tr><td><h4>Round "
						+ roundNumber
						+ " - Table "
						+ counter
						+ "</h4></td><td vAlign=bottom align=left><h4>"
						+ m.getPlayer1().getName()
						+ "</h4></td><td vAlign=bottom align=left><h4>"
						+ m.getPlayer2().getName()
						+ "</h4></td></tr><tr><td><table border=\"1\"><tr><td>Name</td><td>Rank</td><td>Score</td><td>SoS</td><td>Ext SoS</td></tr><tr>"
						+ "<td class=\"smallFont\">"
						+ m.getPlayer1().getName()
						+ "</td><td class=\"smallFont\">"
						+ m.getPlayer1().getRank(tournament)
						+ "</td><td class=\"smallFont\">"
						+ m.getPlayer1().getScore(tournament)
						+ "</td><td class=\"smallFont\">"
						+ m.getPlayer1().getAverageSoS(tournament)
						+ "</td><td class=\"smallFont\">"
						+ m.getPlayer1().getExtendedStrengthOfSchedule(
								tournament)
						+ "</td></tr><tr><td class=\"smallFont\">"
						+ m.getPlayer2().getName()
						+ "</td><td class=\"smallFont\">"
						+ m.getPlayer2().getRank(tournament)
						+ "</td><td class=\"smallFont\">"
						+ m.getPlayer2().getScore(tournament)
						+ "</td><td class=\"smallFont\">"
						+ m.getPlayer2().getAverageSoS(tournament)
						+ "</td><td class=\"smallFont\">"
						+ m.getPlayer2().getExtendedStrengthOfSchedule(
								tournament)
						+ "</td></tr></table>"
						+ "</td><td class=\"smallFont\">"
						+ "</br>"
						+ "<div style=\"vertical-align: top; height: 100%;\"><input type=\"checkbox\">I wish to drop</input></div>"
						+ "</td><td class=\"smallFont\">"
						+ "</br>"
						+ "<div style=\"vertical-align: top; height: 100%;\"><input type=\"checkbox\">I wish to drop</input></div>"
						+ "</td></tr></table>";

				if (counter % 6 == 0) {
					matchString += "<hr class=\"pagebreak\">";
				} else {
					matchString += "<hr>";
				}

				content += matchString;
				counter++;
			}
		}

		displayHTML(content, "ExportMatchSlips");
	}

	public static void exportTournamentSlips(IATournament tournament,
			List<IAMatch> matches, int roundNumber) {

		String content = "";

		int counter = 1;
		for (IAMatch m : matches) {
			String matchString = "";
			if (m.getPlayer2() != null) {
				matchString += "<table width=100%><tr><td><h4>Round "
						+ roundNumber
						+ " - Table "
						+ counter
						+ "</h4></td><td vAlign=bottom align=left><h4>"
						+ m.getPlayer1().getName()
						+ "</h4></td><td vAlign=bottom align=left><h4>"
						+ m.getPlayer2().getName()
						+ "</h4></td></tr><tr><td>"
						+ "</td><td class=\"smallFont\">"
						+ "</br>"
						+ "<div style=\"vertical-align: top; height: 100%;\"><input type=\"checkbox\">I wish to drop</input></div>"
						+ "</td><td class=\"smallFont\">"
						+ "</br>"
						+ "<div style=\"vertical-align: top; height: 100%;\"><input type=\"checkbox\">I wish to drop</input></div>"
						+ "</td></tr></table>";

				if (counter % 6 == 0) {
					matchString += "<hr class=\"pagebreak\">";
				} else {
					matchString += "<hr>";
				}

				content += matchString;
				counter++;
			}
		}

		displayHTML(content, "ExportMatchSlips");
	}

	public static void playerList(List<Player> players) {
		Set<Player> sortedPlayers = new TreeSet<Player>();
		sortedPlayers.addAll(players);

		StringBuilder sb = new StringBuilder();

		for (Player p : sortedPlayers) {
			sb.append(p.getName()).append("<br>");
		}

		displayHTML(sb.toString(), "Player List");
	}

	public static void displayHTML(String content, String filename) {
		String html = "<html><head><style type=\"text/css\">.pagebreak {page-break-after: always;}.smallFont{font-size:10px}</style></head><body>"
				+ content + "</body></html>";

		try {
			File file = File.createTempFile(filename, ".html");

			FileOutputStream stream = new FileOutputStream(file);

			stream.write(html.getBytes());
			stream.flush();
			stream.close();

			Desktop.getDesktop().open(file);
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
}
