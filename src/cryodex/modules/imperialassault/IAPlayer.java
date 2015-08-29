package cryodex.modules.imperialassault;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import cryodex.CryodexController.Modules;
import cryodex.Player;
import cryodex.modules.ModulePlayer;
import cryodex.xml.XMLObject;
import cryodex.xml.XMLUtils;
import cryodex.xml.XMLUtils.Element;

public class IAPlayer implements Comparable<ModulePlayer>, XMLObject,
		ModulePlayer {

	private Player player;
	private String seedValue;
	private boolean firstRoundBye = false;
	private String squadId;

	public IAPlayer(Player p) {
		player = p;
		seedValue = String.valueOf(Math.random());
	}

	public IAPlayer(Player p, Element e) {
		this.player = p;
		this.seedValue = e.getStringFromChild("SEEDVALUE");
		this.firstRoundBye = e.getBooleanFromChild("FIRSTROUNDBYE");
		this.squadId = e.getStringFromChild("SQUADID");
	}

	@Override
	public Player getPlayer() {
		return player;
	}

	@Override
	public void setPlayer(Player player) {
		this.player = player;
	}

	public String getSeedValue() {
		return seedValue;
	}

	public void setSeedValue(String seedValue) {
		this.seedValue = seedValue;
	}

	public boolean isFirstRoundBye() {
		return firstRoundBye;
	}

	public void setFirstRoundBye(boolean firstRoundBye) {
		this.firstRoundBye = firstRoundBye;
	}

	public String getSquadId() {
		return squadId;
	}

	public void setSquadId(String squadId) {
		this.squadId = squadId;
	}

	public List<IAMatch> getMatches(IATournament t) {

		List<IAMatch> matches = new ArrayList<IAMatch>();

		if (t != null) {

			rounds: for (IARound r : t.getAllRounds()) {
				if (r.isSingleElimination()) {
					continue;
				}
				for (IAMatch m : r.getMatches()) {
					if (m.getPlayer1() == this
							|| (m.getPlayer2() != null && m.getPlayer2() == this)) {
						matches.add(m);
						continue rounds;
					}
				}
			}
		}
		return matches;
	}

	@Override
	public String toString() {
		return getPlayer().getName();
	}

	public int getScore(IATournament t) {
		int score = 0;
		for (IAMatch match : getMatches(t)) {
			if (match.getWinner() == this) {
				score += IAMatch.WIN_POINTS;
			} else if (match.isDraw()) {
				score += IAMatch.DRAW_POINTS;
			} else if (match.isBye()) {
				score += IAMatch.BYE_POINTS;
			} else {
				score += IAMatch.LOSS_POINTS;
			}
		}

		return score;
	}

	public double getAverageScore(IATournament t) {
		return getScore(t) * 1.0 / getMatches(t).size();
	}

	public double getAverageSoS(IATournament t) {
		double sos = 0.0;
		List<IAMatch> matches = getMatches(t);

		for (IAMatch m : matches) {
			if (m.isBye() == false && (m.getWinner() != null || m.isDraw())) {
				if (m.getPlayer1() == this) {
					sos += m.getPlayer2().getAverageScore(t);
				} else {
					sos += m.getPlayer1().getAverageScore(t);
				}
			}

			if (isFirstRoundBye() && m.isBye() && m == matches.get(0)) {
				sos += (getMatches(t).size() - 1) * 5;
			}
		}

		return sos / matches.size();
	}

	public int getWins(IATournament t) {
		int score = 0;
		for (IAMatch match : getMatches(t)) {
			if (match.getWinner() == this || match.isBye()) {
				score++;
			}
		}
		return score;
	}

	public int getLosses(IATournament t) {
		int score = 0;
		for (IAMatch match : getMatches(t)) {
			if (match.getWinner() != null && match.getWinner() != this) {
				score++;
			}
		}
		return score;
	}

	public int getDraws(IATournament t) {
		int score = 0;
		for (IAMatch match : getMatches(t)) {
			if (match.isDraw()) {
				score++;
			}
		}
		return score;
	}

	public int getByes(IATournament t) {
		int byes = 0;
		for (IAMatch match : getMatches(t)) {
			if (match.isBye()) {
				byes++;
			}
		}
		return byes;
	}

	// public int getStrengthOfSchedule(IATournament t) {
	// Integer sos = 0;
	// List<IAMatch> matches = getMatches(t);
	//
	// for (IAMatch m : matches) {
	// if (m.isBye() == false & (m.getWinner() != null || m.isDraw())) {
	// if (m.getPlayer1() == this) {
	// sos += m.getPlayer2().getScore(t);
	// } else {
	// sos += m.getPlayer1().getScore(t);
	// }
	// }
	//
	// if (isFirstRoundBye() && m.isBye() && m == matches.get(0)) {
	// sos += (getMatches(t).size() - 1) * 5;
	// }
	// }
	//
	// return sos;
	// }

	public double getExtendedStrengthOfSchedule(IATournament t) {
		double sos = 0;
		List<IAMatch> matches = getMatches(t);

		for (IAMatch m : matches) {
			if (m.isBye() == false & (m.getWinner() != null || m.isDraw())) {
				if (m.getPlayer1() == this) {
					sos += m.getPlayer2().getAverageSoS(t);
				} else {
					sos += m.getPlayer1().getAverageSoS(t);
				}
			}
		}

		return sos / matches.size();
	}

	public int getRank(IATournament t) {
		List<IAPlayer> players = new ArrayList<IAPlayer>();
		players.addAll(t.getIAPlayers());
		Collections.sort(players, new RankingComparator(t));

		for (int i = 0; i < players.size(); i++) {
			if (players.get(i) == this) {
				return i + 1;
			}
		}

		return 0;
	}

	public int getEliminationRank(IATournament t) {

		int rank = 0;

		for (IARound r : t.getAllRounds()) {
			if (r.isSingleElimination()) {
				for (IAMatch m : r.getMatches()) {
					if ((m.getPlayer1() == this || m.getPlayer2() == this)
							&& (m.getWinner() != null && m.getWinner() != this)) {
						return r.getMatches().size() * 2;
					}

					if (r.getMatches().size() == 1 && m.getWinner() != null
							&& m.getWinner() == this) {
						return 1;
					}
				}
			}
		}

		return rank;
	}

	public boolean isHeadToHeadWinner(IATournament t) {

		if (t != null) {
			int score = getScore(t);
			List<IAPlayer> players = new ArrayList<IAPlayer>();
			for (IAPlayer p : t.getIAPlayers()) {
				if (p != this && p.getScore(t) == score) {
					players.add(p);
				}
			}

			playerLoop: for (IAPlayer p : players) {
				for (IAMatch m : p.getMatches(t)) {
					if (m.getPlayer1() == p && m.getPlayer2() == this
							&& m.getWinner() == this) {
						continue playerLoop;
					} else if (m.getPlayer2() == p && m.getPlayer1() == this
							&& m.getWinner() == p) {
						continue playerLoop;
					}
				}
				return false;
			}
		}

		return true;
	}

	public int getRoundDropped(IATournament t) {
		for (int i = t.getAllRounds().size(); i > 0; i--) {

			boolean found = false;
			IARound r = t.getAllRounds().get(i - 1);
			for (IAMatch m : r.getMatches()) {
				if (m.getPlayer1() == this) {
					found = true;
					break;
				} else if (m.isBye() == false && m.getPlayer2() == this) {
					found = true;
					break;
				}
			}

			if (found) {
				return i + 1;
			}
		}

		return 0;
	}

	public String getName() {
		return getPlayer().getName();
	}

	public static class RankingComparator implements Comparator<IAPlayer> {

		private final IATournament t;

		public RankingComparator(IATournament t) {
			this.t = t;
		}

		@Override
		public int compare(IAPlayer o1, IAPlayer o2) {

			int result = compareInt(o1.getScore(t), o2.getScore(t));

			if (result == 0) {
				result = compareDouble(o1.getAverageSoS(t), o2.getAverageSoS(t));
			}

			if (result == 0) {
				result = compareDouble(o1.getExtendedStrengthOfSchedule(t),
						o2.getExtendedStrengthOfSchedule(t));
			}

			if (result == 0) {
				String seedValue1 = o1.getSeedValue();
				String seedValue2 = o2.getSeedValue();

				try {
					Double d1 = Double.valueOf(seedValue1);
					Double d2 = Double.valueOf(seedValue2);

					result = d1.compareTo(d2);
				} catch (NumberFormatException e) {
					result = seedValue1.compareTo(seedValue2);
				}
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

		private int compareDouble(double a, double b) {
			if (a == b) {
				return 0;
			} else if (a > b) {
				return -1;
			} else {
				return 1;
			}
		}
	}

	public static class PairingComparator implements Comparator<IAPlayer> {

		private final IATournament t;

		public PairingComparator(IATournament t) {
			this.t = t;
		}

		@Override
		public int compare(IAPlayer o1, IAPlayer o2) {

			int result = compareInt(o1.getScore(t), o2.getScore(t));

			if (result == 0) {
				result = compareDouble(o1.getAverageSoS(t), o2.getAverageSoS(t));
			}

			if (result == 0) {
				result = compareDouble(o1.getExtendedStrengthOfSchedule(t),
						o2.getExtendedStrengthOfSchedule(t));
			}

			if (result == 0) {
				String seedValue1 = o1.getSeedValue();
				String seedValue2 = o2.getSeedValue();

				try {
					Double d1 = Double.valueOf(seedValue1);
					Double d2 = Double.valueOf(seedValue2);

					result = d1.compareTo(d2);
				} catch (NumberFormatException e) {
					result = seedValue1.compareTo(seedValue2);
				}
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

		private int compareDouble(double a, double b) {
			if (a == b) {
				return 0;
			} else if (a > b) {
				return -1;
			} else {
				return 1;
			}
		}
	}

	@Override
	public String getModuleName() {
		return Modules.IA.getName();
	}

	public String toXML() {
		StringBuilder sb = new StringBuilder();

		appendXML(sb);

		return sb.toString();
	}

	@Override
	public StringBuilder appendXML(StringBuilder sb) {

		XMLUtils.appendObject(sb, "MODULE", Modules.IA.getName());
		XMLUtils.appendObject(sb, "SEEDVALUE", getSeedValue());
		XMLUtils.appendObject(sb, "FIRSTROUNDBYE", isFirstRoundBye());
		XMLUtils.appendObject(sb, "SQUADID", getSquadId());

		return sb;
	}

	@Override
	public int compareTo(ModulePlayer arg0) {
		return this.getPlayer().getName().toUpperCase()
				.compareTo(arg0.getPlayer().getName().toUpperCase());
	}
}
