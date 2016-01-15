package cryodex.modules.xwing;

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

public class XWingPlayer implements Comparable<ModulePlayer>, XMLObject,
		ModulePlayer {

	private Player player;
	private String seedValue;
	private boolean firstRoundBye = false;
	private String squadId;

	public XWingPlayer(Player p) {
		player = p;
		seedValue = String.valueOf(Math.random());
	}

	public XWingPlayer(Player p, Element e) {
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

	public List<XWingMatch> getMatches(XWingTournament t) {

		List<XWingMatch> matches = new ArrayList<XWingMatch>();

		if (t != null) {

			rounds: for (XWingRound r : t.getAllRounds()) {
				if (r.isSingleElimination()) {
					continue;
				}
				for (XWingMatch m : r.getMatches()) {
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

	public int getScore(XWingTournament t) {
		int score = 0;
		for (XWingMatch match : getMatches(t)) {
			if (match.getWinner() == this) {
				score += match.isModified() ? XWingMatch.MOD_WIN_POINTS
						: XWingMatch.WIN_POINTS;
			} else if (match.isDraw()) {
				score += XWingMatch.DRAW_POINTS;
			} else if (match.isBye()) {
				score += XWingMatch.BYE_POINTS;
			} else {
				score += XWingMatch.LOSS_POINTS;
			}
		}

		return score;
	}

	public double getAverageScore(XWingTournament t) {
		return getScore(t) * 1.0 / getMatches(t).size();
	}

	public double getAverageSoS(XWingTournament t) {
		double sos = 0.0;
		List<XWingMatch> matches = getMatches(t);

		for (XWingMatch m : matches) {
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

		double averageSos = sos / matches.size();
		if (Double.isNaN(averageSos) != true) {
			BigDecimal bd = new BigDecimal(averageSos);
			bd = bd.setScale(2, RoundingMode.HALF_UP);
			return bd.doubleValue();
		}
		return averageSos;
	}

	public int getWins(XWingTournament t) {
		int score = 0;
		for (XWingMatch match : getMatches(t)) {
			if (match.getWinner() == this || match.isBye()) {
				score++;
			}
		}
		return score;
	}

	public int getLosses(XWingTournament t) {
		int score = 0;
		for (XWingMatch match : getMatches(t)) {
			if (match.getWinner() != null && match.getWinner() != this) {
				score++;
			}
		}
		return score;
	}

	public int getDraws(XWingTournament t) {
		int score = 0;
		for (XWingMatch match : getMatches(t)) {
			if (match.isDraw()) {
				score++;
			}
		}
		return score;
	}

	public int getByes(XWingTournament t) {
		int byes = 0;
		for (XWingMatch match : getMatches(t)) {
			if (match.isBye()) {
				byes++;
			}
		}
		return byes;
	}

	// public int getStrengthOfSchedule(XWingTournament t) {
	// Integer sos = 0;
	// List<XWingMatch> matches = getMatches(t);
	//
	// for (XWingMatch m : matches) {
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

	public int getRank(XWingTournament t) {
		List<XWingPlayer> players = new ArrayList<XWingPlayer>();
		players.addAll(t.getXWingPlayers());
		Collections.sort(players, new XWingComparator(t,
				XWingComparator.rankingCompare));

		for (int i = 0; i < players.size(); i++) {
			if (players.get(i) == this) {
				return i + 1;
			}
		}

		return 0;
	}

	public int getEliminationRank(XWingTournament t) {

		int rank = 0;

		for (XWingRound r : t.getAllRounds()) {
			if (r.isSingleElimination()) {
				for (XWingMatch m : r.getMatches()) {
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

	public int getMarginOfVictory(XWingTournament t) {

		int roundNumber = 0;

		Integer movPoints = 0;

		for (XWingMatch match : getMatches(t)) {

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
				if (roundNumber == 1 && isFirstRoundBye()) {
					movPoints += tournamentPoints * 2;
				} else {
					movPoints += tournamentPoints + (tournamentPoints / 2);
				}

				continue;
			} else if (match.isDraw()) {
				movPoints += tournamentPoints;
				continue;
			} else if (match.getWinner() == null) {
				continue;
			}

			boolean isPlayer1 = match.getPlayer1() == this;

			int player1Points = match.getPlayer1PointsDestroyed() == null ? 0
					: match.getPlayer1PointsDestroyed();
			int player2Points = match.getPlayer2PointsDestroyed() == null ? 0
					: match.getPlayer2PointsDestroyed();

			int diff = player1Points - player2Points;

			movPoints += isPlayer1 ? tournamentPoints + diff : tournamentPoints
					- diff;
		}
		return movPoints;
	}

	/**
	 * Returns true if the player has defeated every other person in their score
	 * group.
	 * 
	 * @param t
	 * @return
	 */
	public boolean isHeadToHeadWinner(XWingTournament t) {

		if (t != null) {
			int score = getScore(t);
			List<XWingPlayer> players = new ArrayList<XWingPlayer>();
			for (XWingPlayer p : t.getXWingPlayers()) {
				if (p != this && p.getScore(t) == score) {
					players.add(p);
				}
			}

			if (players.isEmpty()) {
				return false;
			}

			playerLoop: for (XWingPlayer p : players) {
				for (XWingMatch m : p.getMatches(t)) {
					if (m.getWinner() != null && m.getWinner() == this) {
						continue playerLoop;
					}
					// if (m.getPlayer1() == p && m.getPlayer2() == this
					// && m.getWinner() == this) {
					// continue playerLoop;
					// } else if (m.getPlayer2() == p && m.getPlayer1() == this
					// && m.getWinner() == p) {
					// continue playerLoop;
					// }
				}
				return false;
			}
		}

		return true;
	}

	public int getRoundDropped(XWingTournament t) {
		for (int i = t.getAllRounds().size(); i > 0; i--) {

			boolean found = false;
			XWingRound r = t.getAllRounds().get(i - 1);
			for (XWingMatch m : r.getMatches()) {
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

	@Override
	public String getModuleName() {
		return Modules.XWING.getName();
	}

	public String toXML() {
		StringBuilder sb = new StringBuilder();

		appendXML(sb);

		return sb.toString();
	}

	@Override
	public StringBuilder appendXML(StringBuilder sb) {

		XMLUtils.appendObject(sb, "MODULE", Modules.XWING.getName());
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
