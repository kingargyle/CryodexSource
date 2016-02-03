package cryodex.modules.xwing.export;

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
import cryodex.modules.xwing.XWingComparator;
import cryodex.modules.xwing.XWingMatch;
import cryodex.modules.xwing.XWingPlayer;
import cryodex.modules.xwing.XWingRound;
import cryodex.modules.xwing.XWingTournament;

public class XWingExportController {

	public static String appendRankings(XWingTournament tournament) {
		List<XWingPlayer> playerList = new ArrayList<XWingPlayer>();
		List<XWingPlayer> activePlayers = tournament.getXWingPlayers();

		playerList.addAll(tournament.getAllXWingPlayers());
		Collections.sort(playerList, new XWingComparator(tournament,
				XWingComparator.rankingCompare));

		String content = "<table border=\"1\"><tr><th>Rank</th><th>Name</th><th>Score</th><th>MoV</th><th>SoS</th></tr>";

		for (XWingPlayer p : playerList) {

			String name = p.getName();

			if (activePlayers.contains(p) == false) {
				name = "(D#" + p.getRoundDropped(tournament) + ")" + name;
			}

			content += "<tr><td>" + p.getRank(tournament) + "</td><td>" + name
					+ "</td><td>" + p.getScore(tournament) + "</td><td>"
					+ p.getMarginOfVictory(tournament) + "</td><td>"
					+ p.getAverageSoS(tournament) + "</td></tr>";
		}

		content += "</table>";

		return content;
	}

	public static void exportRankings(XWingTournament tournament) {

		String content = appendRankings(tournament);

		displayHTML(content, "ExportRankings");
	}

	public static String appendMatches(XWingTournament tournament,
			List<XWingMatch> matches) {
		String content = "";

		int counter = 1;
		for (XWingMatch m : matches) {
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
					if (m.isDraw()) {
						matchString += "Draw";
					} else if (m.getWinner() != null) {
						matchString += m.getWinner().getName()
								+ " is the winner";
					}

					if (m.getPlayer1PointsDestroyed() != null
							&& m.getPlayer2PointsDestroyed() != null) {
						matchString += " " + m.getPlayer1PointsDestroyed()
								+ " to " + m.getPlayer2PointsDestroyed();
					}
				}
			}
			content += "<div>" + matchString + "</div>";
		}

		return content;
	}

	public static void exportMatches(XWingTournament tournament,
			List<XWingMatch> matches, int roundNumber) {
		String content = "";

		if (roundNumber == 0) {
			content += "<h3>Top " + (matches.size() * 2) + "</h3>";
		} else {
			content += "<h3>Round " + roundNumber + "</h3>";
		}

		content += appendMatches(tournament, matches);

		displayHTML(content, "ExportMatch");
	}

	public static void exportTournamentReport(XWingTournament tournament) {
		String content = "";
		int roundNumber = 1;
		for (XWingRound r : tournament.getAllRounds()) {
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

	public static void exportTournamentSlipsWithStats(
			XWingTournament tournament, List<XWingMatch> matches,
			int roundNumber) {

		String content = "";

		int counter = 1;
		for (XWingMatch m : matches) {
			String matchString = "";
			if (m.getPlayer2() != null) {
				matchString += "<table width=100%><tr><th><h4>Round "
						+ roundNumber
						+ " - Table "
						+ counter
						+ "</h4></th><th vAlign=bottom align=left><h4>"
						+ m.getPlayer1().getName()
						+ "</h4></th><th vAlign=bottom align=left><h4>"
						+ m.getPlayer2().getName()
						+ "</h4></th></tr><tr><td><table border=\"1\"><tr><th>Name</th><th>Rank</td><th>Score</th><th>MoV</th><th>SoS</th></tr><tr>"
						+ "<td class=\"smallFont\">"
						+ m.getPlayer1().getName()
						+ "</td><td class=\"smallFont\">"
						+ m.getPlayer1().getRank(tournament)
						+ "</td><td class=\"smallFont\">"
						+ m.getPlayer1().getScore(tournament)
						+ "</td><td class=\"smallFont\">"
						+ m.getPlayer1().getMarginOfVictory(tournament)
						+ "</td><td class=\"smallFont\">"
						+ m.getPlayer1().getAverageSoS(tournament)
						+ "</td></tr><tr><td class=\"smallFont\">"
						+ m.getPlayer2().getName()
						+ "</td><td class=\"smallFont\">"
						+ m.getPlayer2().getRank(tournament)
						+ "</td><td class=\"smallFont\">"
						+ m.getPlayer2().getScore(tournament)
						+ "</td><td class=\"smallFont\">"
						+ m.getPlayer2().getMarginOfVictory(tournament)
						+ "</td><td class=\"smallFont\">"
						+ m.getPlayer2().getAverageSoS(tournament)
						+ "</td></tr></table>"
						+ "</td><td class=\"smallFont\">"
						+ "<div style=\"vertical-align: bottom; height: 100%;\">Points Killed ____________</div>"
						+ "</br>"
						+ "<div style=\"vertical-align: top; height: 100%;\"><input type=\"checkbox\">I wish to drop</input></div>"
						+ "</td><td class=\"smallFont\">"
						+ "<div style=\"vertical-align: bottom; height: 100%;\">Points Killed ____________</div>"
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

	public static void exportTournamentSlips(XWingTournament tournament,
			List<XWingMatch> matches, int roundNumber) {

		String content = "";

		int counter = 1;
		for (XWingMatch m : matches) {
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
						+ "<div style=\"vertical-align: bottom; height: 100%;\">Points Killed ____________</div>"
						+ "</br>"
						+ "<div style=\"vertical-align: top; height: 100%;\"><input type=\"checkbox\">I wish to drop</input></div>"
						+ "</td><td class=\"smallFont\">"
						+ "<div style=\"vertical-align: bottom; height: 100%;\">Points Killed ____________</div>"
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
		String fancyCss="table{border-collapse: collapse;margin: 20px;}th{color:white; background-color:DarkSlateGray; font-size:120%;} tr:nth-child(odd){	background-color:lightgray;}";
		String internationalCharacters="<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\" />";
		String html = "<html><head><style type=\"text/css\">.pagebreak {page-break-after: always;}.smallFont{font-size:10px}"
				+ fancyCss+"</style>"+internationalCharacters+"</head><body>"
				+ content + "</body></html>";

		try {
			File file = File.createTempFile(filename, ".html");

			FileOutputStream stream = new FileOutputStream(file);

			stream.write(html.getBytes("UTF-8"));
			stream.flush();
			stream.close();

			Desktop.getDesktop().open(file);
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
}
