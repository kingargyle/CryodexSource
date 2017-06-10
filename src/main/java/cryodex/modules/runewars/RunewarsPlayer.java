package cryodex.modules.runewars;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import cryodex.CryodexController.Modules;
import cryodex.Player;
import cryodex.modules.ModulePlayer;
import cryodex.xml.XMLObject;
import cryodex.xml.XMLUtils;
import cryodex.xml.XMLUtils.Element;

public class RunewarsPlayer implements Comparable<ModulePlayer>, XMLObject, ModulePlayer {

	public static final int BYE_MOV = 100;
	public static final int MAX_MOV = 200;
	public static final int CONCEDE_MOV = 200;
	public static final int CONCEDE_WIN_SCORE = 1;
	public static final int CONCEDE_LOSE_SCORE = 0;

        public static enum Faction {
                DAQAN, WAIQAR;
        }

        private Faction faction;

	/**
	 * This enum represents the table of match score to tournament points
	 */
	public enum ScoreTableEnum {
                THRESHOLD_0(0,0,0,0), THRESHOLD_1(0,600,1,0);
/*		THRESHOLD_6(0, 59, 6, 5), THRESHOLD_7(60, 139, 7, 4), THRESHOLD_8(140, 219, 8, 3), THRESHOLD_9(220, 299, 9,
				2), THRESHOLD_10(300, 400, 10, 1); */

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

		private static ScoreTableEnum getResultByMOV(int mov) {
			ScoreTableEnum result = ScoreTableEnum.THRESHOLD_1;

                        if (mov < 0) {
                               result = ScoreTableEnum.THRESHOLD_0; 
                        }

/*			if (mov >= ScoreTableEnum.THRESHOLD_6.getLowerLimit()
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
			} */

			return result;
		}

		public static int getWinScore(int mov) {
			return getResultByMOV(mov).getWinScore();
		}

		public static int getLoseScore(int mov) {
			return getResultByMOV(mov).getLoseScore();
		}
	}

	private Player player;
	private String seedValue;
	private boolean firstRoundBye = false;
	private String squadId;

	public RunewarsPlayer(Player p) {
		player = p;
		seedValue = String.valueOf(Math.random());
	}

	public RunewarsPlayer(Player p, Element e) {
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

        public Faction getFaction() {
		return faction;
	}

        public void setFaction(Faction faction) {
                this.faction = faction;
        }

	public List<RunewarsMatch> getMatches(RunewarsTournament t) {

		List<RunewarsMatch> matches = new ArrayList<RunewarsMatch>();

		if (t != null) {

			rounds: for (RunewarsRound r : t.getAllRounds()) {
				if (r.isSingleElimination()) {
					continue;
				}
				for (RunewarsMatch m : r.getMatches()) {
					if (m.getPlayer1() == this || (m.getPlayer2() != null && m.getPlayer2() == this)) {
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

	public int getScore(RunewarsTournament t) {
		int score = 0;
		for (RunewarsMatch match : getMatches(t)) {
			if (match.isBye()) {
				score += 1;
			} else {

				if (match.getWinner() == null) {
					continue;
				}

				int mov = 0;

				try {

					int player1Score = match.getPlayer1Score() == null ? 0 : match.getPlayer1Score();
					int player2Score = match.getPlayer2Score() == null ? 0 : match.getPlayer2Score();

					if (match.getPlayer1() == match.getWinner()) {
						mov = player1Score - player2Score;
					} else {
						mov = player2Score - player1Score;
					}
				} catch (Exception e) {
				}

				// Check to see if MOV is outside the min/max
				mov = mov < 0 ? 0 : mov;
				mov = mov > MAX_MOV ? MAX_MOV : mov;

				int matchScore = 0;
				if (match.getWinner() == this) {
					if(match.isConcede()){
						matchScore = CONCEDE_WIN_SCORE;
					} else {
						matchScore = ScoreTableEnum.getWinScore(mov);	
					}
				} else {
					if(match.isConcede()){
						matchScore = CONCEDE_LOSE_SCORE;
					} else {
						matchScore = ScoreTableEnum.getLoseScore(mov);	
					}
				}
				score += matchScore;
			}
		}

		return score;
	}

	public double getAverageScore(RunewarsTournament t) {
		return getScore(t) * 1.0 / getMatches(t).size();
	}

	public double getAverageSoS(RunewarsTournament t) {
		double sos = 0.0;
		List<RunewarsMatch> matches = getMatches(t);

		for (RunewarsMatch m : matches) {
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

	public int getWins(RunewarsTournament t) {
		int score = 0;
		for (RunewarsMatch match : getMatches(t)) {
			if (match.getWinner() == this || match.isBye()) {
				score++;
			}
		}
		return score;
	}

	public int getLosses(RunewarsTournament t) {
		int score = 0;
		for (RunewarsMatch match : getMatches(t)) {
			if (match.getWinner() != null && match.getWinner() != this) {
				score++;
			}
		}
		return score;
	}

	public int getByes(RunewarsTournament t) {
		int byes = 0;
		for (RunewarsMatch match : getMatches(t)) {
			if (match.isBye()) {
				byes++;
			}
		}
		return byes;
	}

	public int getRank(RunewarsTournament t) {
		List<RunewarsPlayer> players = new ArrayList<RunewarsPlayer>();
		players.addAll(t.getRunewarsPlayers());
		Collections.sort(players, new RunewarsComparator(t, RunewarsComparator.rankingCompare));

		for (int i = 0; i < players.size(); i++) {
			if (players.get(i) == this) {
				return i + 1;
			}
		}

		return 0;
	}

	public int getEliminationRank(RunewarsTournament t) {

		int rank = 0;

		for (RunewarsRound r : t.getAllRounds()) {
			if (r.isSingleElimination()) {
				for (RunewarsMatch m : r.getMatches()) {
					if ((m.getPlayer1() == this || m.getPlayer2() == this)
							&& (m.getWinner() != null && m.getWinner() != this)) {
						return r.getMatches().size() * 2;
					}

					if (r.getMatches().size() == 1 && m.getWinner() != null && m.getWinner() == this) {
						return 1;
					}
				}
			}
		}

		return rank;
	}

	public int getMarginOfVictory(RunewarsTournament t) {

		int roundNumber = 0;

		Integer totalMov = 0;

		for (RunewarsMatch match : getMatches(t)) {

			roundNumber++;

			Integer tournamentPoints = t.getPoints();
			if (tournamentPoints == null && t.getEscalationPoints() != null
					&& t.getEscalationPoints().isEmpty() == false) {

				tournamentPoints = t.getEscalationPoints().size() >= roundNumber
						? t.getEscalationPoints().get(roundNumber - 1)
						: t.getEscalationPoints().get(t.getEscalationPoints().size() - 1);
			}

			if (match.isBye()) {
				totalMov += BYE_MOV;

				continue;
			} else if (match.getWinner() == null) {
				continue;
			}

			if (match.getWinner() == this) {
				int mov = 0;
				try {

					int player1Score = match.getPlayer1Score() == null ? 0 : match.getPlayer1Score();
					int player2Score = match.getPlayer2Score() == null ? 0 : match.getPlayer2Score();

					if (match.getPlayer1() == this) {
						mov = player1Score - player2Score;
					} else {
						mov = player2Score - player1Score;
					}
				} catch (Exception e) {
				}

				if (match.isConcede()) {
					mov = CONCEDE_MOV;
				}

				// Check to see if MOV is outside the min/max
				mov = mov < 0 ? 0 : mov;
				mov = mov > MAX_MOV ? MAX_MOV : mov;

				totalMov += mov;
			} else {
				totalMov += 0;
			}
		}
		return totalMov;
	}

	public boolean isHeadToHeadWinner(RunewarsTournament t) {

		if (t != null) {
			int score = getScore(t);
			List<RunewarsPlayer> players = new ArrayList<RunewarsPlayer>();
			for (RunewarsPlayer p : t.getRunewarsPlayers()) {
				if (p != this && p.getScore(t) == score) {
					players.add(p);
				}
			}

			if (players.isEmpty()) {
				return false;
			}

			playerLoop: for (RunewarsPlayer p : players) {
				for (RunewarsMatch m : p.getMatches(t)) {
					if (m.getPlayer1() == p && m.getPlayer2() == this && m.getWinner() == this) {
						continue playerLoop;
					} else if (m.getPlayer2() == p && m.getPlayer1() == this && m.getWinner() == p) {
						continue playerLoop;
					}
				}
				return false;
			}
		}

		return true;
	}

	public int getRoundDropped(RunewarsTournament t) {
		for (int i = t.getAllRounds().size(); i > 0; i--) {

			boolean found = false;
			RunewarsRound r = t.getAllRounds().get(i - 1);
			for (RunewarsMatch m : r.getMatches()) {
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
		return Modules.RUNEWARS.getName();
	}

	public String toXML() {
		StringBuilder sb = new StringBuilder();

		appendXML(sb);

		return sb.toString();
	}

	@Override
	public StringBuilder appendXML(StringBuilder sb) {

		XMLUtils.appendObject(sb, "MODULE", Modules.RUNEWARS.getName());
		XMLUtils.appendObject(sb, "SEEDVALUE", getSeedValue());
		XMLUtils.appendObject(sb, "FIRSTROUNDBYE", isFirstRoundBye());
		XMLUtils.appendObject(sb, "SQUADID", getSquadId());

		return sb;
	}

	@Override
	public int compareTo(ModulePlayer arg0) {
		return this.getPlayer().getName().toUpperCase().compareTo(arg0.getPlayer().getName().toUpperCase());
	}
}
