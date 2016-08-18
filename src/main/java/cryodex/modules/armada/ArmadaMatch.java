package cryodex.modules.armada;

import java.util.List;

import cryodex.CryodexController;
import cryodex.CryodexController.Modules;
import cryodex.Player;
import cryodex.modules.Module;
import cryodex.xml.XMLObject;
import cryodex.xml.XMLUtils;
import cryodex.xml.XMLUtils.Element;

public class ArmadaMatch implements XMLObject {

	private ArmadaPlayer player1;
	private ArmadaPlayer player2;
	private ArmadaPlayer winner;
	private boolean isBye = false;
	private Integer player1Score;
	private Integer player2Score;
	private boolean isDuplicate;
	private boolean isConcede;

	public ArmadaMatch() {

	}

	public ArmadaMatch(ArmadaPlayer player1, ArmadaPlayer player2) {
		this.player1 = player1;
		this.player2 = player2;
	}

	public ArmadaMatch(Element matchElement) {

		Module m = Modules.getModuleByName(getModuleName());

		String player1String = matchElement.getStringFromChild("PLAYER1");
		Player p = CryodexController.getPlayerByID(player1String);
		if (p != null) {
			player1 = (ArmadaPlayer) p.getModuleInfoByModule(m);
		}

		String player2String = matchElement.getStringFromChild("PLAYER2");
		p = CryodexController.getPlayerByID(player2String);
		if (p != null) {
			player2 = (ArmadaPlayer) p.getModuleInfoByModule(m);
		}

		String winnerString = matchElement.getStringFromChild("WINNER");
		p = CryodexController.getPlayerByID(winnerString);
		if (p != null) {
			winner = (ArmadaPlayer) p.getModuleInfoByModule(m);
		}

		isBye = matchElement.getBooleanFromChild("ISBYE");
		isDuplicate = matchElement.getBooleanFromChild("ISDUPLICATE");
		isConcede = matchElement.getBooleanFromChild("ISCONCEDE", false);

		player1Score = matchElement.getIntegerFromChild("PLAYER1POINTS");
		player2Score = matchElement.getIntegerFromChild("PLAYER2POINTS");

	}

	public ArmadaPlayer getPlayer1() {
		return player1;
	}

	public void setPlayer1(ArmadaPlayer player1) {
		this.player1 = player1;
	}

	public ArmadaPlayer getPlayer2() {
		return player2;
	}

	public void setPlayer2(ArmadaPlayer player2) {
		this.player2 = player2;
	}

	public ArmadaPlayer getWinner() {
		return winner;
	}

	public void setWinner(ArmadaPlayer winner) {
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

	public void checkDuplicate(List<ArmadaRound> rounds) {

		if (this.getPlayer2() == null) {
			this.setDuplicate(false);
			return;
		}

		for (ArmadaRound r : rounds) {
			if (r.isSingleElimination()) {
				continue;
			}

			for (ArmadaMatch match : r.getMatches()) {
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

	public static boolean hasDuplicate(List<ArmadaMatch> matches) {
		boolean duplicateFound = false;
		for (ArmadaMatch dc : matches) {
			if (dc.isDuplicate()) {
				duplicateFound = true;
				break;
			}
		}

		return duplicateFound;
	}

	public String getModuleName() {
		return Modules.ARMADA.getName();
	}
}
