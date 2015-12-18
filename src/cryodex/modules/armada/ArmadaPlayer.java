package cryodex.modules.armada;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import cryodex.CryodexController.Modules;
import cryodex.Player;
import cryodex.modules.ModulePlayer;
import cryodex.xml.XMLObject;
import cryodex.xml.XMLUtils;
import cryodex.xml.XMLUtils.Element;


class ScoreTable
{
    public static final int THRESHOLD_6 = 20;
    public static final int THRESHOLD_7 = 50;
    public static final int THRESHOLD_8 = 90;
    public static final int THRESHOLD_9 = 150;
    public static final int THRESHOLD_10 = 220;
}
public class ArmadaPlayer implements Comparable<ModulePlayer>, XMLObject,
		ModulePlayer {

	private Player player;
	private String seedValue;
	private boolean firstRoundBye = false;
	private String squadId;

	public ArmadaPlayer(Player p) {
		player = p;
		seedValue = String.valueOf(Math.random());
	}

	public ArmadaPlayer(Player p, Element e) {
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

	public List<ArmadaMatch> getMatches(ArmadaTournament t) {

		List<ArmadaMatch> matches = new ArrayList<ArmadaMatch>();

		if (t != null) {

			rounds: for (ArmadaRound r : t.getAllRounds()) {
				if (r.isSingleElimination()) {
					continue;
				}
				for (ArmadaMatch m : r.getMatches()) {
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

	public int getScore(ArmadaTournament t) {
		int score = 0;
		for (ArmadaMatch match : getMatches(t)) {
			if (match.isBye()) {
				score += 8;
			} else {

				if (match.getWinner() == null) {
					continue;
				}

				int mov = 0;

				try {

					int player1Score = match.getPlayer1Score() == null ? 0
							: match.getPlayer1Score();
					int player2Score = match.getPlayer2Score() == null ? 0
							: match.getPlayer2Score();

					if (match.getPlayer1() == match.getWinner()) {
						mov = player1Score - player2Score;
					} else {
						mov = player2Score - player1Score;
					}
				} catch (Exception e) {
				}

				mov = mov < 0 ? 0 : mov;

                int increment = 0;

                if (mov >= 0 && mov <= ScoreTable.THRESHOLD_6) {
                    increment += 5;
                } else if (mov > ScoreTable.THRESHOLD_6 && mov <= ScoreTable.THRESHOLD_7) {
                    increment += 6;
                } else if (mov > ScoreTable.THRESHOLD_7 && mov <= ScoreTable.THRESHOLD_8) {
                    increment += 7;
                } else if (mov > ScoreTable.THRESHOLD_8 && mov <= ScoreTable.THRESHOLD_9) {
                    increment += 8;
                } else if (mov > ScoreTable.THRESHOLD_9 && mov <= ScoreTable.THRESHOLD_10) {
                    increment += 9;
                } else if (mov > ScoreTable.THRESHOLD_10) {
                    increment += 10;
                }
                
                if (match.getWinner() == this)
                    score += increment;
                else 
                    score += 10 - increment;
			}
		}

		return score;
	}

	public double getAverageScore(ArmadaTournament t) {
		return getScore(t) * 1.0 / getMatches(t).size();
	}

	public double getAverageSoS(ArmadaTournament t) {
		double sos = 0.0;
		List<ArmadaMatch> matches = getMatches(t);

		for (ArmadaMatch m : matches) {
			if (m.isBye() == false && (m.getWinner() != null)) {
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

	public int getWins(ArmadaTournament t) {
		int score = 0;
		for (ArmadaMatch match : getMatches(t)) {
			if (match.getWinner() == this || match.isBye()) {
				score++;
			}
		}
		return score;
	}

	public int getLosses(ArmadaTournament t) {
		int score = 0;
		for (ArmadaMatch match : getMatches(t)) {
			if (match.getWinner() != null && match.getWinner() != this) {
				score++;
			}
		}
		return score;
	}

	public int getByes(ArmadaTournament t) {
		int byes = 0;
		for (ArmadaMatch match : getMatches(t)) {
			if (match.isBye()) {
				byes++;
			}
		}
		return byes;
	}

	// public int getStrengthOfSchedule(ArmadaTournament t) {
	// Integer sos = 0;
	// List<ArmadaMatch> matches = getMatches(t);
	//
	// for (ArmadaMatch m : matches) {
	// if (m.isBye() == false & (m.getWinner() != null)) {
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

	public int getRank(ArmadaTournament t) {
		List<ArmadaPlayer> players = new ArrayList<ArmadaPlayer>();
		players.addAll(t.getArmadaPlayers());
		Collections.sort(players, new ArmadaComparator(t,
				ArmadaComparator.rankingCompare));

		for (int i = 0; i < players.size(); i++) {
			if (players.get(i) == this) {
				return i + 1;
			}
		}

		return 0;
	}

	public int getEliminationRank(ArmadaTournament t) {

		int rank = 0;

		for (ArmadaRound r : t.getAllRounds()) {
			if (r.isSingleElimination()) {
				for (ArmadaMatch m : r.getMatches()) {
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

	public int getMarginOfVictory(ArmadaTournament t) {

		int roundNumber = 0;

		Integer movPoints = 0;

		for (ArmadaMatch match : getMatches(t)) {

			roundNumber++;

			Integer tournamentPoints = t.getPoints();
			if (tournamentPoints == null && t.getEscalationPoints() != null
					&& t.getEscalationPoints().isEmpty() == false) {

				tournamentPoints = t.getEscalationPoints().size() >= roundNumber ? t
						.getEscalationPoints().get(roundNumber - 1) : t
						.getEscalationPoints().get(
								t.getEscalationPoints().size() - 1);
			}

			if (match.isBye()) {
				movPoints += 61;

				continue;
			} else if (match.getWinner() == null) {
				continue;
			}

			if (match.getWinner() == this) {
				int points = 0;
				try {

					int player1Score = match.getPlayer1Score() == null ? 0
							: match.getPlayer1Score();
					int player2Score = match.getPlayer2Score() == null ? 0
							: match.getPlayer2Score();

					if (match.getPlayer1() == this) {
						points = player1Score - player2Score;
					} else {
						points = player2Score - player1Score;
					}
				} catch (Exception e) {
				}

				movPoints += points < 0 ? 0 : points;
			} else {
				movPoints += 0;
			}
		}
		return movPoints;
	}

	public boolean isHeadToHeadWinner(ArmadaTournament t) {

		if (t != null) {
			int score = getScore(t);
			List<ArmadaPlayer> players = new ArrayList<ArmadaPlayer>();
			for (ArmadaPlayer p : t.getArmadaPlayers()) {
				if (p != this && p.getScore(t) == score) {
					players.add(p);
				}
			}

			if (players.isEmpty()) {
				return false;
			}

			playerLoop: for (ArmadaPlayer p : players) {
				for (ArmadaMatch m : p.getMatches(t)) {
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

	public int getRoundDropped(ArmadaTournament t) {
		for (int i = t.getAllRounds().size(); i > 0; i--) {

			boolean found = false;
			ArmadaRound r = t.getAllRounds().get(i - 1);
			for (ArmadaMatch m : r.getMatches()) {
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
	// TournamentComparator<ArmadaPlayer> {
	//
	// private final ArmadaTournament t;
	//
	// public RankingComparator(ArmadaTournament t) {
	// this.t = t;
	// }
	//
	// @Override
	// public int compare(ArmadaPlayer o1, ArmadaPlayer o2) {
	//
	// int result = compareInt(o1.getScore(t), o2.getScore(t));
	//
	// if (result == 0) {
	// result = compareDouble(o1.getAverageSoS(t), o2.getAverageSoS(t));
	// }
	//
	// if (result == 0) {
	// result = compareInt(o1.getMarginOfVictory(t),
	// o2.getMarginOfVictory(t));
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
	// TournamentComparator<ArmadaPlayer> {
	//
	// private final ArmadaTournament t;
	//
	// public PairingComparator(ArmadaTournament t) {
	// this.t = t;
	// }
	//
	// @Override
	// public int compare(ArmadaPlayer o1, ArmadaPlayer o2) {
	//
	// int result = compareInt(o1.getScore(t), o2.getScore(t));
	//
	// if (result == 0) {
	// result = compareDouble(o1.getAverageSoS(t), o2.getAverageSoS(t));
	// }
	//
	// if (result == 0) {
	// result = compareInt(o1.getMarginOfVictory(t),
	// o2.getMarginOfVictory(t));
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
		return Modules.ARMADA.getName();
	}

	public String toXML() {
		StringBuilder sb = new StringBuilder();

		appendXML(sb);

		return sb.toString();
	}

	@Override
	public StringBuilder appendXML(StringBuilder sb) {

		XMLUtils.appendObject(sb, "MODULE", Modules.ARMADA.getName());
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
