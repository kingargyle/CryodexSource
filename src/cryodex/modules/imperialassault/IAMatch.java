package cryodex.modules.imperialassault;

import java.util.List;

import cryodex.CryodexController;
import cryodex.CryodexController.Modules;
import cryodex.Player;
import cryodex.modules.Module;
import cryodex.xml.XMLObject;
import cryodex.xml.XMLUtils;
import cryodex.xml.XMLUtils.Element;

public class IAMatch implements XMLObject {

	public static final int WIN_POINTS = 1;
	public static final int BYE_POINTS = 1;
	public static final int LOSS_POINTS = 0;

	private IAPlayer player1;
	private IAPlayer player2;
	private IAPlayer winner;
	private boolean isBye = false;
	private boolean isDuplicate;

	public IAMatch() {

	}

	public IAMatch(IAPlayer player1, IAPlayer player2) {
		this.player1 = player1;
		this.player2 = player2;
	}

	public IAMatch(Element matchElement) {

		Module m = Modules.getModuleByName(getModuleName());

		String player1String = matchElement.getStringFromChild("PLAYER1");
		Player p = CryodexController.getPlayerByID(player1String);
		if (p != null) {
			player1 = (IAPlayer) p.getModuleInfoByModule(m);
		}

		String player2String = matchElement.getStringFromChild("PLAYER2");
		p = CryodexController.getPlayerByID(player2String);
		if (p != null) {
			player2 = (IAPlayer) p.getModuleInfoByModule(m);
		}

		String winnerString = matchElement.getStringFromChild("WINNER");
		p = CryodexController.getPlayerByID(winnerString);
		if (p != null) {
			winner = (IAPlayer) p.getModuleInfoByModule(m);
		}

		isBye = matchElement.getBooleanFromChild("ISBYE");
		isDuplicate = matchElement.getBooleanFromChild("ISDUPLICATE");
	}

	public IAPlayer getPlayer1() {
		return player1;
	}

	public void setPlayer1(IAPlayer player1) {
		this.player1 = player1;
	}

	public IAPlayer getPlayer2() {
		return player2;
	}

	public void setPlayer2(IAPlayer player2) {
		this.player2 = player2;
	}

	public IAPlayer getWinner() {
		return winner;
	}

	public void setWinner(IAPlayer winner) {
		this.winner = winner;
	}

	public boolean isBye() {
		return isBye;
	}

	public void setBye(boolean isBye) {
		this.isBye = isBye;
	}

	public boolean isDuplicate() {
		return isDuplicate;
	}

	public void setDuplicate(boolean isDuplicate) {
		this.isDuplicate = isDuplicate;
	}

	public boolean isMatchComplete() {
		return isBye || winner != null;
	}

	public boolean isValidResult() {
		return true;
	}

	public void checkDuplicate(List<IARound> rounds) {

		if (this.getPlayer2() == null) {
			this.setDuplicate(false);
			return;
		}

		for (IARound r : rounds) {
			if (r.isSingleElimination()) {
				continue;
			}

			for (IAMatch match : r.getMatches()) {
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
		XMLUtils.appendObject(sb, "ISDUPLICATE", isDuplicate());

		return sb;
	}

	public static boolean hasDuplicate(List<IAMatch> matches) {
		boolean duplicateFound = false;
		for (IAMatch dc : matches) {
			if (dc.isDuplicate()) {
				duplicateFound = true;
				break;
			}
		}

		return duplicateFound;
	}

	public String getModuleName() {
		return Modules.IA.getName();
	}
}
