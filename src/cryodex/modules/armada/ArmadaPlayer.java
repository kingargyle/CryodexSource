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

public class ArmadaPlayer implements Comparable<ModulePlayer>, XMLObject,
		ModulePlayer {

	public static final int BYE_MOV = 140;
	
	/**
	 *This enum represents the table of match score to tournament points
	 */
	public enum ScoreTableEnum {
		THRESHOLD_6(0, 59, 6, 5),
		THRESHOLD_7(60, 139, 7, 4),
		THRESHOLD_8(140, 219, 8, 3),
		THRESHOLD_9(220, 299, 9, 2),
		THRESHOLD_10(300, 400, 10, 1);

		private int lowerLimit;
		private int upperLimit;
		private int winScore;
		private int loseScore;

		private ScoreTableEnum(int lowerLimit, int upperLimit, int winScore, int loseScore) {
			this.lowerLimit = lowerLimit;
			this.upperLimit = upperLimit;
			this.winScore = winScore;
			this.loseScore = loseScore;
		}

		public int getLowerLimit() {
			return lowerLimit;
		}

		public int getUpperLimit() {
			return upperLimit;
		}

		public int getWinScore() {
			return winScore;
		}
		
		public int getLoseScore() {
			return loseScore;
		}
		
		private static ScoreTableEnum getResultByMOV(int mov){
			ScoreTableEnum result = ScoreTableEnum.THRESHOLD_6;

			if (mov >= ScoreTableEnum.THRESHOLD_6.getLowerLimit()
					&& mov <= ScoreTableEnum.THRESHOLD_6.getUpperLimit()) {
				result = ScoreTableEnum.THRESHOLD_6;

			} else if (mov >= ScoreTableEnum.THRESHOLD_7.getLowerLimit()
					&& mov <= ScoreTableEnum.THRESHOLD_7.getUpperLimit()) {
				result = ScoreTableEnum.THRESHOLD_7;

			} else if (mov >= ScoreTableEnum.THRESHOLD_8.getLowerLimit()
					&& mov <= ScoreTableEnum.THRESHOLD_8.getUpperLimit()) {
				result = ScoreTableEnum.THRESHOLD_8;

			} else if (mov >= ScoreTableEnum.THRESHOLD_9.getLowerLimit()
					&& mov <= ScoreTableEnum.THRESHOLD_9.getUpperLimit()) {
				result = ScoreTableEnum.THRESHOLD_9;

			} else if (mov >= ScoreTableEnum.THRESHOLD_10.getLowerLimit()) {
				result = ScoreTableEnum.THRESHOLD_10;
			}
			
			return result;
		}
		
		public static int getWinScore(int mov){
			return getResultByMOV(mov).getWinScore();
		}
		
		public static int getLoseScore(int mov){
			return getResultByMOV(mov).getLoseScore();
		}
	}

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

				if (match.getWinner() == this)
					score += ScoreTableEnum.getWinScore(mov);
				else
					score += ScoreTableEnum.getLoseScore(mov);
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
				movPoints += BYE_MOV;

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
