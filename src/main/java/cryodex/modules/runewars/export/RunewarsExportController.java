package cryodex.modules.runewars.export;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import cryodex.CryodexController;
import cryodex.Player;
import cryodex.export.ExportUtils;
import cryodex.modules.runewars.RunewarsComparator;
import cryodex.modules.runewars.RunewarsMatch;
import cryodex.modules.runewars.RunewarsPlayer;
import cryodex.modules.runewars.RunewarsRound;
import cryodex.modules.runewars.RunewarsTournament;

public class RunewarsExportController {

	public static String appendRankings(RunewarsTournament tournament) {
		List<RunewarsPlayer> playerList = new ArrayList<RunewarsPlayer>();
		List<RunewarsPlayer> activePlayers = tournament.getRunewarsPlayers();

		playerList.addAll(tournament.getAllRunewarsPlayers());
		Collections.sort(playerList, new RunewarsComparator(tournament,
				RunewarsComparator.rankingCompare));

		String content = "<table border=\"1\"><tr><td>Rank</td><td>Name</td><td>Score</td><td>MoV</td><td>SoS</td></tr>";

		for (RunewarsPlayer p : playerList) {

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

	public static void exportRankings(RunewarsTournament tournament) {

		String content = appendRankings(tournament);

		ExportUtils.displayHTML(content, "ExportRankings");
	}

	public static String appendMatches(RunewarsTournament tournament,
			List<RunewarsMatch> matches) {
		String content = "";

		int counter = 1;
		for (RunewarsMatch m : matches) {
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

					if (m.getPlayer1Score() != null
							&& m.getPlayer2Score() != null) {
						matchString += " " + m.getPlayer1Score() + " to "
								+ m.getPlayer2Score();
					}
				}
			}
			content += "<div>" + matchString + "</div>";
		}

		return content;
	}

	public static void exportMatches(RunewarsTournament tournament,
			List<RunewarsMatch> matches, int roundNumber) {
		String content = "";

		if (roundNumber == 0) {
			content += "<h3>Top " + (matches.size() * 2) + "</h3>";
		} else {
			content += "<h3>Round " + roundNumber + "</h3>";
		}

		content += appendMatches(tournament, matches);

		ExportUtils.displayHTML(content, "ExportMatch");
	}

	public static void exportTournamentReport(RunewarsTournament tournament) {
		String content = "";
		int roundNumber = 1;
		for (RunewarsRound r : tournament.getAllRounds()) {
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

		ExportUtils.displayHTML(content, "TournamentReport");
	}

	public static void exportTournamentSlipsWithStats(
			RunewarsTournament tournament, List<RunewarsMatch> matches,
			int roundNumber) {

		String content = "";

		int counter = 1;
		for (RunewarsMatch m : matches) {
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
						+ "</h4></td></tr><tr><td><table border=\"1\"><tr><td>Name</td><td>Rank</td><td>Score</td><td>MoV</td><td>SoS</td></tr><tr>"
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
						+ "<div style=\"vertical-align: bottom; height: 100%;\">Points ____________</div>"
						+ "</br>"
						+ "<div style=\"vertical-align: top; height: 100%;\"><input type=\"checkbox\">I wish to drop</input></div>"
						+ "</td><td class=\"smallFont\">"
						+ "<div style=\"vertical-align: bottom; height: 100%;\">Points ____________</div>"
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

		ExportUtils.displayHTML(content, "ExportMatchSlips");
	}

	public static void exportTournamentSlips(RunewarsTournament tournament,
			List<RunewarsMatch> matches, int roundNumber) {

		String content = "";

		int counter = 1;
		for (RunewarsMatch m : matches) {
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
						+ "<div style=\"vertical-align: bottom; height: 100%;\">Points ____________</div>"
						+ "</br>"
						+ "<div style=\"vertical-align: top; height: 100%;\"><input type=\"checkbox\">I wish to drop</input></div>"
						+ "</td><td class=\"smallFont\">"
						+ "<div style=\"vertical-align: bottom; height: 100%;\">Points ____________</div>"
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

		ExportUtils.displayHTML(content, "ExportMatchSlips");
	}

	public static void playerList(List<Player> players) {
		Set<Player> sortedPlayers = new TreeSet<Player>();
		sortedPlayers.addAll(players);

		StringBuilder sb = new StringBuilder();

		for (Player p : sortedPlayers) {
			sb.append(p.getName()).append("<br>");
		}

		ExportUtils.displayHTML(sb.toString(), "Player List");
	}
}
