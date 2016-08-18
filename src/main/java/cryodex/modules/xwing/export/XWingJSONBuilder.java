package cryodex.modules.xwing.export;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Set;

import javax.swing.JOptionPane;

import cryodex.Main;
import cryodex.modules.xwing.XWingMatch;
import cryodex.modules.xwing.XWingPlayer;
import cryodex.modules.xwing.XWingRound;
import cryodex.modules.xwing.XWingTournament;

public class XWingJSONBuilder {

	private static final String COMMA_NEWLINE = ",\n";

	private static XWingTournament tournament;

	public static void buildTournament(XWingTournament t) {

		tournament = t;

		StringBuilder sb = new StringBuilder();

		sb.append("{\n");

		addValue(sb, "name", t.getName());

		sb.append(COMMA_NEWLINE);

		addPlayers(sb, t.getAllXWingPlayers());

		sb.append(COMMA_NEWLINE);

		addRounds(sb, t.getAllRounds());

		sb.append("\n}");

		try {
			File path = new File(System.getProperty("user.dir"));
			if (path.exists() == false) {
				System.out.println("Error with user directory");
			}
			File file = new File(path, "XWingTournament.json");
			if (file.exists() == false) {
				file.createNewFile();
			} else {
				file.delete();
				file.createNewFile();
			}

			FileOutputStream stream = new FileOutputStream(file);

			stream.write(sb.toString().getBytes());
			stream.flush();
			stream.close();

			JOptionPane.showMessageDialog(
					Main.getInstance(),
					"<html>List Juggler output can be found at <b>"
							+ file.getAbsolutePath() + "</b></html>");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void addRounds(StringBuilder sb, List<XWingRound> rounds) {
		sb.append("\"rounds\": [\n");

		for (int i = 0; i < rounds.size(); i++) {
			addRound(sb, rounds.get(i), i + 1);
			if (i != rounds.size() - 1) {
				sb.append(COMMA_NEWLINE);
			}
		}

		sb.append("\n]");
	}

	private static void addRound(StringBuilder sb, XWingRound round,
			int roundNumber) {
		sb.append("{\n");

		addValue(sb, "round-type", round.isSingleElimination() ? "elimination"
				: "swiss");

		sb.append(COMMA_NEWLINE);

		addValue(sb, "round-number", round.isSingleElimination() ? round
				.getMatches().size() * 2 : roundNumber);

		sb.append(COMMA_NEWLINE);

		addMatches(sb, round.getMatches());

		sb.append("\n}");
	}

	private static void addMatches(StringBuilder sb, List<XWingMatch> matches) {
		sb.append("\"matches\": [\n");

		for (int i = 0; i < matches.size(); i++) {
			addMatch(sb, matches.get(i));
			if (i != matches.size() - 1) {
				sb.append(COMMA_NEWLINE);
			}
		}

		sb.append("\n]");
	}

	private static void addMatch(StringBuilder sb, XWingMatch match) {
		sb.append("{\n");

		addValue(sb, "player1", match.getPlayer1().getName());
		sb.append(COMMA_NEWLINE);

		addValue(
				sb,
				"player1points",
				match.getPlayer1PointsDestroyed() == null ? 0 : match
						.getPlayer1PointsDestroyed());
		sb.append(COMMA_NEWLINE);

		if (match.getPlayer2() != null) {
			addValue(sb, "player2", match.getPlayer2().getName());
			sb.append(COMMA_NEWLINE);
			addValue(
					sb,
					"player2points",
					match.getPlayer2PointsDestroyed() == null ? 0 : match
							.getPlayer2PointsDestroyed());
			sb.append(COMMA_NEWLINE);
		}

		String result = "win";
		if (match.isBye()) {
			result = "bye";
		}

		addValue(sb, "result", result);

		sb.append("\n}");
	}

	private static void addPlayers(StringBuilder sb, Set<XWingPlayer> players) {
		sb.append("\"players\": [\n");

		boolean addComma = false;
		for (XWingPlayer p : players) {
			if (addComma) {
				sb.append(COMMA_NEWLINE);
			}
			addPlayer(sb, p);
			addComma = true;
		}

		sb.append("\n]");
	}

	private static void addPlayer(StringBuilder sb, XWingPlayer player) {
		sb.append("{\n");

		addValue(sb, "name", player.getName());
		sb.append(COMMA_NEWLINE);
		if (player.getPlayer().getEmail() != null) {
			addValue(sb, "email", player.getPlayer().getEmail());
			sb.append(COMMA_NEWLINE);
		}
		if (player.getSquadId() != null) {
			addValue(sb, "list-id", player.getSquadId());
			sb.append(COMMA_NEWLINE);
		}
		addValue(sb, "mov", player.getMarginOfVictory(tournament));
		sb.append(COMMA_NEWLINE);
		addValue(sb, "score", player.getScore(tournament));
		sb.append(COMMA_NEWLINE);
		addValue(sb, "sos", player.getAverageSoS(tournament));
		sb.append(COMMA_NEWLINE);
		if (tournament.getXWingPlayers().contains(player) == false) {
			addValue(sb, "dropped", player.getRoundDropped(tournament));
			sb.append(COMMA_NEWLINE);
		}

		int rank = player.getRank(tournament);
		int eliminationRank = player.getEliminationRank(tournament);
		if (eliminationRank != 0) {
			sb.append("\"rank\": {\"swiss\": " + rank + ",\"elimination\": "
					+ eliminationRank + "}");
		} else {
			sb.append("\"rank\": {\"swiss\": " + rank + "}");
		}

		sb.append("\n}");
	}

	private static void addValue(StringBuilder sb, String key, int value) {
		addStringValue(sb, key, String.valueOf(value), true);
	}

	private static void addValue(StringBuilder sb, String key, double value) {
		addStringValue(sb, key, String.valueOf(value), true);
	}

	private static void addValue(StringBuilder sb, String key, String value) {
		addStringValue(sb, key, value, false);
	}

	private static void addStringValue(StringBuilder sb, String key,
			String value, boolean isNumber) {
		sb.append("\"" + key + "\": ");

		if (isNumber) {
			sb.append(value);
		} else {
			if (value != null) {
				value = value.replaceAll("\"", "\\\\\"");
			}
			sb.append("\"" + value + "\"");
		}
	}

}
