package cryodex.modules.runewars;

import java.util.List;

import cryodex.CryodexController;
import cryodex.CryodexController.Modules;
import cryodex.Player;
import cryodex.modules.Module;
import cryodex.xml.XMLObject;
import cryodex.xml.XMLUtils;
import cryodex.xml.XMLUtils.Element;

public class RunewarsMatch implements XMLObject {

	private RunewarsPlayer player1;
	private RunewarsPlayer player2;
	private RunewarsPlayer winner;
	private boolean isBye = false;
	private Integer player1Score;
	private Integer player2Score;
	private boolean isDuplicate;
	private boolean isConcede;

	public RunewarsMatch() {

	}

	public RunewarsMatch(RunewarsPlayer player1, RunewarsPlayer player2) {
		this.player1 = player1;
		this.player2 = player2;
	}

	public RunewarsMatch(Element matchElement) {

		Module m = Modules.getModuleByName(getModuleName());

		String player1String = matchElement.getStringFromChild("PLAYER1");
		Player p = CryodexController.getPlayerByID(player1String);
		if (p != null) {
			player1 = (RunewarsPlayer) p.getModuleInfoByModule(m);
		}

		String player2String = matchElement.getStringFromChild("PLAYER2");
		p = CryodexController.getPlayerByID(player2String);
		if (p != null) {
			player2 = (RunewarsPlayer) p.getModuleInfoByModule(m);
		}

		String winnerString = matchElement.getStringFromChild("WINNER");
		p = CryodexController.getPlayerByID(winnerString);
		if (p != null) {
			winner = (RunewarsPlayer) p.getModuleInfoByModule(m);
		}

		isBye = matchElement.getBooleanFromChild("ISBYE");
		isDuplicate = matchElement.getBooleanFromChild("ISDUPLICATE");
		isConcede = matchElement.getBooleanFromChild("ISCONCEDE", false);

		player1Score = matchElement.getIntegerFromChild("PLAYER1POINTS");
		player2Score = matchElement.getIntegerFromChild("PLAYER2POINTS");

	}

	public RunewarsPlayer getPlayer1() {
		return player1;
	}

	public void setPlayer1(RunewarsPlayer player1) {
		this.player1 = player1;
	}

	public RunewarsPlayer getPlayer2() {
		return player2;
	}

	public void setPlayer2(RunewarsPlayer player2) {
		this.player2 = player2;
	}

	public RunewarsPlayer getWinner() {
		return winner;
	}

	public void setWinner(RunewarsPlayer winner) {
		this.winner = winner;
	}

	public boolean isBye() {
		return isBye;
	}

	public void setBye(boolean isBye) {
		this.isBye = isBye;
	}

	public Integer getPlayer1Score() {
		return player1Score;
	}

	public void setPlayer1Score(Integer player1Score) {
		this.player1Score = player1Score;
	}

	public Integer getPlayer2Score() {
		return player2Score;
	}

	public void setPlayer2Score(Integer player2Score) {
		this.player2Score = player2Score;
	}

	public boolean isDuplicate() {
		return isDuplicate;
	}

	public void setDuplicate(boolean isDuplicate) {
		this.isDuplicate = isDuplicate;
	}
	
	public boolean isConcede() {
		return isConcede;
	}
	
	public void setConcede(boolean isConcede) {
		this.isConcede = isConcede;
	}

	public boolean isMatchComplete() {
		return isBye || winner != null;
	}

	public boolean isValidResult() {
		return true;
	}

	public void checkDuplicate(List<RunewarsRound> rounds) {

		if (this.getPlayer2() == null) {
			this.setDuplicate(false);
			return;
		}

		for (RunewarsRound r : rounds) {
			if (r.isSingleElimination()) {
				continue;
			}

			for (RunewarsMatch match : r.getMatches()) {
				if (match.getPlayer2() == null || match == this) {
					continue;
				}

				if ((match.getPlayer1() == this.getPlayer1() && match
						.getPlayer2() == this.getPlayer2())
						|| (match.getPlayer1() == this.getPlayer2() && match
								.getPlayer2() == this.getPlayer1())) {
					this.setDuplicate(true);
					return;
				}
			}
		}

		this.setDuplicate(false);
	}

	@Override
	public String toString() {
		return getPlayer1() + " vs " + getPlayer2() + " : isDuplicate="
				+ isDuplicate();
	}

	@Override
	public StringBuilder appendXML(StringBuilder sb) {

		XMLUtils.appendObject(sb, "PLAYER1", getPlayer1().getPlayer()
				.getSaveId());
		XMLUtils.appendObject(sb, "PLAYER2", getPlayer2() == null ? ""
				: getPlayer2().getPlayer().getSaveId());
		XMLUtils.appendObject(sb, "WINNER", getWinner() == null ? ""
				: getWinner().getPlayer().getSaveId());
		XMLUtils.appendObject(sb, "ISBYE", isBye());
		XMLUtils.appendObject(sb, "PLAYER1POINTS", getPlayer1Score());
		XMLUtils.appendObject(sb, "PLAYER2POINTS", getPlayer2Score());
		XMLUtils.appendObject(sb, "ISDUPLICATE", isDuplicate());
		XMLUtils.appendObject(sb, "ISCONCEDE", isConcede());

		return sb;
	}

	public static boolean hasDuplicate(List<RunewarsMatch> matches) {
		boolean duplicateFound = false;
		for (RunewarsMatch dc : matches) {
			if (dc.isDuplicate()) {
				duplicateFound = true;
				break;
			}
		}

		return duplicateFound;
	}

	public String getModuleName() {
		return Modules.RUNEWARS.getName();
	}
}
