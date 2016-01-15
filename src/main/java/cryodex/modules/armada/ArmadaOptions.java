package cryodex.modules.armada;

import cryodex.CryodexController;
import cryodex.modules.Tournament;
import cryodex.xml.XMLObject;
import cryodex.xml.XMLUtils;
import cryodex.xml.XMLUtils.Element;

public class ArmadaOptions implements XMLObject {
	boolean showScore = true;

	// boolean enterOnlyPoints = true;

	public ArmadaOptions() {

	}

	public ArmadaOptions(Element e) {

		if (e != null) {
			showScore = e.getBooleanFromChild("SHOWSCORE", true);
			// enterOnlyPoints = e.getBooleanFromChild("ENTERONLYPOINTS", true);
		}
	}

	public boolean isShowScore() {
		return showScore;
	}

	public void setShowScore(boolean showScore) {
		this.showScore = showScore;
		updateTournamentVisuals();
	}

	// public boolean isEnterOnlyPoints() {
	// return enterOnlyPoints;
	// }
	//
	// public void setEnterOnlyPoints(boolean enterOnlyPoints) {
	// this.enterOnlyPoints = enterOnlyPoints;
	// updateTournamentVisuals();
	// }

	private void updateTournamentVisuals() {
		if (CryodexController.isLoading == false
				&& CryodexController.getAllTournaments() != null) {
			for (Tournament t : CryodexController.getAllTournaments()) {
				if (t instanceof ArmadaTournament) {
					t.updateVisualOptions();
				}
			}
		}

		CryodexController.saveData();
	}

	@Override
	public StringBuilder appendXML(StringBuilder sb) {
		XMLUtils.appendObject(sb, "SHOWSCORE", showScore);
		// XMLUtils.appendObject(sb, "ENTERONLYPOINTS", enterOnlyPoints);

		return sb;
	}
}