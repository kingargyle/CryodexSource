package cryodex.modules.starwarslcg;

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

public class SWLCGPlayer implements Comparable<ModulePlayer>, XMLObject,
		ModulePlayer {

	private Player player;
	private String seedValue;
	private boolean firstRoundBye = false;

	public SWLCGPlayer(Player p) {
		player = p;
		seedValue = String.valueOf(Math.random());
	}

	public SWLCGPlayer(Player p, Element e) {
		this.player = p;
		this.seedValue = e.getStringFromChild("SEEDVALUE");
		this.firstRoundBye = e.getBooleanFromChild("FIRSTROUNDBYE");
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

	public List<SWLCGMatch> getMatches(SWLCGTournament t) {

		List<SWLCGMatch> matches = new ArrayList<SWLCGMatch>();

		if (t != null) {

			rounds: for (SWLCGRound r : t.getAllRounds()) {
				if (r.isElimination()) {
					continue;
				}
				for (SWLCGMatch m : r.getMatches()) {
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

	public int getScore(SWLCGTournament t) {
		int score = 0;
		for (SWLCGMatch match : getMatches(t)) {
			if (match.getPlayer1() == this) {
				score += match.getPlayer1Points();
			} else if(match.getPlayer2() == this){
				score += match.getPlayer2Points();
			}
		}

		return score;
	}

	public double getAverageScore(SWLCGTournament t) {
		return getScore(t) * 1.0 / getMatches(t).size();
	}

	public double getAverageSoS(SWLCGTournament t) {
		double sos = 0.0;
		List<SWLCGMatch> matches = getMatches(t);

		int numOpponents = 0;
		for (SWLCGMatch m : matches) {
			if (m.isBye() == false && (m.isMatchComplete(m.getMatchLabel() != null))) {
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

	public int getByes(SWLCGTournament t) {
		int byes = 0;
		for (SWLCGMatch match : getMatches(t)) {
			if (match.isBye()) {
				byes++;
			}
		}
		return byes;
	}

	public double getExtendedStrengthOfSchedule(SWLCGTournament t) {
		double sos = 0;
		List<SWLCGMatch> matches = getMatches(t);

		int numOpponents = 0;
		for (SWLCGMatch m : matches) {
			if (m.isBye() == false && m.isMatchComplete(m.getMatchLabel() != null)) {
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

	public int getRank(SWLCGTournament t) {
		List<SWLCGPlayer> players = new ArrayList<SWLCGPlayer>();
		players.addAll(t.getSWLCGPlayers());
		Collections.sort(players, new SWLCGComparator(t,
				SWLCGComparator.rankingCompare));

		for (int i = 0; i < players.size(); i++) {
			if (players.get(i) == this) {
				return i + 1;
			}
		}

		return 0;
	}

	public int getRoundDropped(SWLCGTournament t) {
		for (int i = t.getAllRounds().size(); i > 0; i--) {

			boolean found = false;
			SWLCGRound r = t.getAllRounds().get(i - 1);
			for (SWLCGMatch m : r.getMatches()) {
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
		return Modules.SWLCG.getName();
	}

	public String toXML() {
		StringBuilder sb = new StringBuilder();

		appendXML(sb);

		return sb.toString();
	}

	@Override
	public StringBuilder appendXML(StringBuilder sb) {

		XMLUtils.appendObject(sb, "MODULE", Modules.SWLCG.getName());
		XMLUtils.appendObject(sb, "SEEDVALUE", getSeedValue());
		XMLUtils.appendObject(sb, "FIRSTROUNDBYE", isFirstRoundBye());

		return sb;
	}

	@Override
	public int compareTo(ModulePlayer arg0) {
		return this.getPlayer().getName().toUpperCase()
				.compareTo(arg0.getPlayer().getName().toUpperCase());
	}
}
