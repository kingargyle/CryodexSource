package cryodex.modules.xwing.export;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import cryodex.CryodexController;
import cryodex.modules.Tournament;
import cryodex.modules.xwing.XWingComparator;
import cryodex.modules.xwing.XWingPlayer;
import cryodex.modules.xwing.XWingPlayer.Faction;
import cryodex.modules.xwing.XWingTournament;

public class CACReport {

	// Bloodstripes Award (Imp killing most Reb and S&Vpoints)
	// Rogue Squadron Award (Reb killing most Imp and S&V points)
	// Rebel Secret Agent Award (Imp killing most Imp points) (TBD)
	// Imperial Backstabber Award (Reb killing most Reb points)
	// Most Feared Award (S&V killing most Rebel and S&V points)
	// Lawlessness Award (S&V killing most Imperial and Rebel points)

	public enum KillLabel {
		impVSimp("Rebel Secret Agent - Imperial VS Imperial", Faction.IMPERIAL, null),
		impVSother("Bloodstripes - Imperial VS Other", Faction.REBEL, Faction.SCUM),
		rebVSother("Rogue Squadron - Rebel VS Other", Faction.REBEL, Faction.SCUM),
		rebVSreb("Imperial Backstabber - Rebel VS Rebel", Faction.REBEL, null),
		mostFeared("Most Feared - Scum VS Rebel and Scum", Faction.REBEL, Faction.SCUM),
		lawlessness("Lawlessness - Scum VS Imperial and Rebel", Faction.IMPERIAL, Faction.REBEL);

		private String label = "";
		private final Faction f1;
		private final Faction f2;

		private KillLabel(String label, Faction f1, Faction f2) {
			this.label = label;
			this.f1 = f1;
			this.f2 = f2;
		}

		public String getLabel() {
			return label;
		}

		public Faction getFaction1() {
			return f1;
		}

		public Faction getFaction2() {
			return f2;
		}
	}

	public static final int RESULTS_TO_SHOW = 3;

	public static String generateCACReport() {

		StringBuilder sb = new StringBuilder();
		List<XWingPlayer> imperials = new ArrayList<XWingPlayer>();
		List<XWingPlayer> rebels = new ArrayList<XWingPlayer>();
		List<XWingPlayer> sav = new ArrayList<XWingPlayer>();

		TreeSet<XWingPlayer> playerList = new TreeSet<XWingPlayer>();

		for (Tournament t : CryodexController.getAllTournaments()) {
			if (t instanceof XWingTournament) {
				playerList.addAll(((XWingTournament) t).getAllXWingPlayers());
			}

		}

		for (XWingPlayer p : playerList) {
			p.clearKillMap();
			switch (p.getFaction()) {
			case IMPERIAL:
				imperials.add(p);
				break;
			case REBEL:
				rebels.add(p);
				break;
			case SCUM:
				sav.add(p);
				break;
			}
		}

		appendKillResults(imperials, KillLabel.impVSimp, sb);
		appendKillResults(imperials, KillLabel.impVSother, sb);
		appendKillResults(rebels, KillLabel.rebVSreb, sb);
		appendKillResults(rebels, KillLabel.rebVSother, sb);
		appendKillResults(sav, KillLabel.mostFeared, sb);
		appendKillResults(sav, KillLabel.lawlessness, sb);

		// //Overall Ranking
		getCACRankings(sb, playerList);

		return sb.toString();
	}

	private static void getCACRankings(StringBuilder sb, TreeSet<XWingPlayer> pl) {
		Map<XWingPlayer, CACPlayer> playerData = new HashMap<XWingPlayer, CACPlayer>();

		for (XWingPlayer p : pl) {
			playerData.put(p, new CACPlayer(p));
		}

		for (Tournament t : CryodexController.getAllTournaments()) {
			if (t instanceof XWingTournament) {

				XWingTournament tournament = (XWingTournament) t;

				if (tournament.getName().toUpperCase().startsWith("R2")) {
					List<XWingPlayer> playerList = new ArrayList<XWingPlayer>();
					List<XWingPlayer> activePlayers = new ArrayList<XWingPlayer>();
					activePlayers.addAll(tournament.getXWingPlayers());

					playerList.addAll(tournament.getAllXWingPlayers());
					Collections.sort(playerList, new XWingComparator(
							tournament, XWingComparator.rankingCompare));

					String content = "<h3>Tournament Results</h3><table border=\"1\"><tr><td style=\"width: 20px;\">Rank</td><td style=\"width: 250px;\">Name</td><td style=\"width: 50px;\">Score</td><td style=\"width: 50px;\">MoV</td><td style=\"width: 50px;\">SoS</td><td style=\"width: 100px;\">Total Score</td><td style=\"width: 100px;\">Total MoV</td></tr>";

					for (XWingPlayer p : playerList) {

						String name = p.getName();

						if (activePlayers.contains(p) == false) {
							name = "(D#" + p.getRoundDropped(tournament) + ")"
									+ name;
						}
						CACPlayer cp = playerData.get(p);
						content += "<tr><td>" + p.getRank(tournament)
								+ "</td><td>" + name + "</td><td>"
								+ p.getScore(tournament) + "</td><td>"
								+ p.getMarginOfVictory(tournament)
								+ "</td><td>" + p.getAverageSoS(tournament)
								+ "</td><td>" + cp.getPoints() + "</td><td>"
								+ cp.getMOV() + "</td></tr>";
					}

					content += "</table>";

					sb.append(content);
				}
			}
		}

	}

	public static class CACPlayer implements Comparable<CACPlayer> {

		private Integer points = 0;
		private Integer mov = 0;
		private final XWingPlayer player;

		public CACPlayer(XWingPlayer p) {
			this.player = p;
			calculateStats();
		}

		private void calculateStats() {

			for (Tournament t : CryodexController.getAllTournaments()) {
				if (t instanceof XWingTournament) {
					XWingTournament tournament = (XWingTournament) t;

					points += player.getScore(tournament);
					mov += player.getMarginOfVictory(tournament);
				}
			}
		}

		public Integer getPoints() {
			return points;
		}

		public Integer getMOV() {
			return mov;
		}

		public XWingPlayer getPlayer() {
			return player;
		}

		@Override
		public int compareTo(CACPlayer o2) {
			CACPlayer o1 = this;
			int result = compareInt(o1.getPoints(), o2.getPoints());

			if (result == 0) {
				result = compareInt(o1.getMOV(), o2.getMOV());
			}

			if (result == 0) {
				result = o1.getPlayer().compareTo(o2.getPlayer());
			}

			return result;
		}

		private int compareInt(int a, int b) {
			if (a == b) {
				return 0;
			} else if (a > b) {
				return -1;
			} else {
				return 1;
			}
		}
	}

	public static void appendKillResults(List<XWingPlayer> playerList,
			KillLabel kl, StringBuilder sb) {
		int counter = 0;

		Collections.sort(playerList, new FactionKillComparator(kl));

		sb.append("<h3>").append(kl.label).append("</h3>");
		while (counter < RESULTS_TO_SHOW && playerList.size() > counter) {

			XWingPlayer p = playerList.get(counter);

			Map<Faction, Integer> killMap = p.getKillMap();

			Integer killCount = 0;

			if (kl.getFaction1() != null) {
				killCount += killMap.get(kl.getFaction1());
			}

			if (kl.getFaction2() != null) {
				killCount += killMap.get(kl.getFaction2());
			}

			sb.append(counter + 1).append(") ")
					.append(playerList.get(counter).getName()).append(" - ")
					.append(killCount).append("<br>");
			counter++;
		}
	}

	public static class FactionKillComparator implements
			Comparator<XWingPlayer> {

		private final KillLabel killLabel;

		public FactionKillComparator(KillLabel kl) {
			this.killLabel = kl;
		}

		@Override
		public int compare(XWingPlayer o1, XWingPlayer o2) {

			Map<Faction, Integer> killMap1 = o1.getKillMap();
			Map<Faction, Integer> killMap2 = o2.getKillMap();

			Integer killCount1 = 0;
			Integer killCount2 = 0;

			if (killLabel.getFaction1() != null) {
				killCount1 += killMap1.get(killLabel.getFaction1());
				killCount2 += killMap2.get(killLabel.getFaction1());
			}

			if (killLabel.getFaction2() != null) {
				killCount1 += killMap1.get(killLabel.getFaction2());
				killCount2 += killMap2.get(killLabel.getFaction2());
			}

			return killCount1.compareTo(killCount2) * -1;
		}
	}
}
