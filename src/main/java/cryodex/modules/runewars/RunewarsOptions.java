package cryodex.modules.runewars;

import cryodex.CryodexController;
import cryodex.modules.Tournament;
import cryodex.xml.XMLObject;
import cryodex.xml.XMLUtils;
import cryodex.xml.XMLUtils.Element;

public class RunewarsOptions implements XMLObject {
	boolean showScore = true;

	public RunewarsOptions() {

	}

	public RunewarsOptions(Element e) {

		if (e != null) {
			showScore = e.getBooleanFromChild("SHOWSCORE", true);
		}
	}

	public boolean isShowScore() {
		return showScore;
	}

	public void setShowScore(boolean showScore) {
		this.showScore = showScore;
		updateTournamentVisuals();
	}

	private void updateTournamentVisuals() {
		if (CryodexController.isLoading == false
				&& CryodexController.getAllTournaments() != null) {
			for (Tournament t : CryodexController.getAllTournaments()) {
				if (t instanceof RunewarsTournament) {
					t.updateVisualOptions();
				}
			}
		}

		CryodexController.saveData();
	}

	@Override
	public StringBuilder appendXML(StringBuilder sb) {
		XMLUtils.appendObject(sb, "SHOWSCORE", showScore);

		return sb;
	}
}
