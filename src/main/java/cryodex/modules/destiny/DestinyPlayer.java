package cryodex.modules.destiny;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import cryodex.CryodexController.Modules;
import cryodex.Player;
import cryodex.modules.ModulePlayer;
import cryodex.xml.XMLObject;
import cryodex.xml.XMLUtils;
import cryodex.xml.XMLUtils.Element;

public class DestinyPlayer implements Comparable<ModulePlayer>, XMLObject,
		ModulePlayer {

	private Player player;
	private String seedValue;
	private boolean firstRoundBye = false;
	private String squadId;

	public DestinyPlayer(Player p) {
		player = p;
		seedValue = String.valueOf(Math.random());
	}

	public DestinyPlayer(Player p, Element e) {
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

	public List<DestinyMatch> getMatches(DestinyTournament t) {

		List<DestinyMatch> matches = new ArrayList<DestinyMatch>();

		if (t != null) {

			rounds: for (DestinyRound r : t.getAllRounds()) {
				if (r.isSingleElimination()) {
					continue;
				}
				for (DestinyMatch m : r.getMatches()) {
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

	public int getScore(DestinyTournament t) {
		int score = 0;
		for (DestinyMatch match : getMatches(t)) {
			if (match.getWinner() == this) {
				score += DestinyMatch.WIN_POINTS;
			} else if (match.isBye()) {
				score += DestinyMatch.BYE_POINTS;
			} else {
				score += DestinyMatch.LOSS_POINTS;
			}
		}

		return score;
	}

	public double getAverageScore(DestinyTournament t) {
		return getScore(t) * 1.0 / getMatches(t).size();
	}

	public double getAverageSoS(DestinyTournament t) {
		double sos = 0.0;
		List<DestinyMatch> matches = getMatches(t);

		int numOpponents = 0;
		for (DestinyMatch m : matches) {
			if (m.isBye() == false && (m.getWinner() != null)) {
				if (m.getPlayer1() == this) {
					sos += m.getPlayer2().getAverageScore(t);
					numOpponents++;
				} else {
					sos += m.getPlayer1().getAverageScore(t);
					numOpponents++;
				}
			}
		}

		// if they don't have any opponents recorded yet, don't divide by 0
		double averageSos = numOpponents>0 ? sos / numOpponents : 0;
        if (Double.isNaN(averageSos) != true) {
            BigDecimal bd = new BigDecimal(averageSos);
            bd = bd.setScale(3, RoundingMode.HALF_UP);
            return bd.doubleValue();
        }
        return averageSos;
	}

	public int getWins(DestinyTournament t) {
		int score = 0;
		for (DestinyMatch match : getMatches(t)) {
			if (match.getWinner() == this || match.isBye()) {
				score++;
			}
		}
		return score;
	}

	public int getLosses(DestinyTournament t) {
		int score = 0;
		for (DestinyMatch match : getMatches(t)) {
			if (match.getWinner() != null && match.getWinner() != this) {
				score++;
			}
		}
		return score;
	}

	public int getByes(DestinyTournament t) {
		int byes = 0;
		for (DestinyMatch match : getMatches(t)) {
			if (match.isBye()) {
				byes++;
			}
		}
		return byes;
	}

	public double getExtendedStrengthOfSchedule(DestinyTournament t) {
		double sos = 0;
		List<DestinyMatch> matches = getMatches(t);

		int numOpponents = 0;
		for (DestinyMatch m : matches) {
			if (m.isBye() == false & m.getWinner() != null) {
				if (m.getPlayer1() == this) {
					sos += m.getPlayer2().getAverageSoS(t);
					numOpponents++;
				} else {
					sos += m.getPlayer1().getAverageSoS(t);
					numOpponents++;
				}
			}
		}

		// if they don't have any opponents recorded yet, don't divide by 0
		double averageSos = numOpponents>0 ? sos / numOpponents : 0;
        if (Double.isNaN(averageSos) != true) {
            BigDecimal bd = new BigDecimal(averageSos);
            bd = bd.setScale(3, RoundingMode.HALF_UP);
            return bd.doubleValue();
        }
        return averageSos;
	}

	public int getRank(DestinyTournament t) {
		List<DestinyPlayer> players = new ArrayList<DestinyPlayer>();
		players.addAll(t.getDestinyPlayers());
		Collections.sort(players, new DestinyComparator(t,
				DestinyComparator.rankingCompare));

		for (int i = 0; i < players.size(); i++) {
			if (players.get(i) == this) {
				return i + 1;
			}
		}

		return 0;
	}

	public int getEliminationRank(DestinyTournament t) {

		int rank = 0;

		for (DestinyRound r : t.getAllRounds()) {
			if (r.isSingleElimination()) {
				for (DestinyMatch m : r.getMatches()) {
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

	public boolean isHeadToHeadWinner(DestinyTournament t) {

		if (t != null) {
			int score = getScore(t);
			List<DestinyPlayer> players = new ArrayList<DestinyPlayer>();
			for (DestinyPlayer p : t.getDestinyPlayers()) {
				if (p != this && p.getScore(t) == score) {
					players.add(p);
				}
			}

			if (players.isEmpty()) {
				return false;
			}

			playerLoop: for (DestinyPlayer p : players) {
				for (DestinyMatch m : p.getMatches(t)) {
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

	public int getRoundDropped(DestinyTournament t) {
		for (int i = t.getAllRounds().size(); i > 0; i--) {

			boolean found = false;
			DestinyRound r = t.getAllRounds().get(i - 1);
			for (DestinyMatch m : r.getMatches()) {
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

	// public static class RankingComparator extends
	// TournamentComparator<IAPlayer> {
	//
	// private final IATournament t;
	//
	// public RankingComparator(IATournament t) {
	// this.t = t;
	// }
	//
	// @Override
	// public int compare(IAPlayer o1, IAPlayer o2) {
	//
	// int result = compareInt(o1.getScore(t), o2.getScore(t));
	//
	// if (result == 0) {
	// result = compareDouble(o1.getAverageSoS(t), o2.getAverageSoS(t));
	// }
	//
	// if (result == 0) {
	// result = compareDouble(o1.getExtendedStrengthOfSchedule(t),
	// o2.getExtendedStrengthOfSchedule(t));
	// }
	//
	// if (result == 0) {
	// String seedValue1 = o1.getSeedValue();
	// String seedValue2 = o2.getSeedValue();
	//
	// try {
	// Double d1 = Double.valueOf(seedValue1);
	// Double d2 = Double.valueOf(seedValue2);
	//
	// result = d1.compareTo(d2);
	// } catch (NumberFormatException e) {
	// result = seedValue1.compareTo(seedValue2);
	// }
	// }
	//
	// return result;
	// }
	// }

	// public static class PairingComparator extends
	// TournamentComparator<IAPlayer> {
	//
	// private final IATournament t;
	//
	// public PairingComparator(IATournament t) {
	// this.t = t;
	// }
	//
	// @Override
	// public int compare(IAPlayer o1, IAPlayer o2) {
	//
	// int result = compareInt(o1.getScore(t), o2.getScore(t));
	//
	// if (result == 0) {
	// result = compareDouble(o1.getAverageSoS(t), o2.getAverageSoS(t));
	// }
	//
	// if (result == 0) {
	// result = compareDouble(o1.getExtendedStrengthOfSchedule(t),
	// o2.getExtendedStrengthOfSchedule(t));
	// }
	//
	// if (result == 0) {
	// String seedValue1 = o1.getSeedValue();
	// String seedValue2 = o2.getSeedValue();
	//
	// try {
	// Double d1 = Double.valueOf(seedValue1);
	// Double d2 = Double.valueOf(seedValue2);
	//
	// result = d1.compareTo(d2);
	// } catch (NumberFormatException e) {
	// result = seedValue1.compareTo(seedValue2);
	// }
	// }
	//
	// return result;
	// }
	// }

	@Override
	public String getModuleName() {
		return Modules.DESTINY.getName();
	}

	public String toXML() {
		StringBuilder sb = new StringBuilder();

		appendXML(sb);

		return sb.toString();
	}

	@Override
	public StringBuilder appendXML(StringBuilder sb) {

		XMLUtils.appendObject(sb, "MODULE", Modules.DESTINY.getName());
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
