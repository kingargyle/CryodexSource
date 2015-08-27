package cryodex;

import cryodex.modules.Tournament;
import cryodex.xml.XMLObject;
import cryodex.xml.XMLUtils;
import cryodex.xml.XMLUtils.Element;

public class CryodexOptions implements XMLObject {
	boolean showTableNumbers = false;
	boolean showQuickFind = false;

	public CryodexOptions() {

	}

	public CryodexOptions(Element e) {
		showTableNumbers = e.getBooleanFromChild("SHOWTABLENUMBERS", false);
		showQuickFind = e.getBooleanFromChild("SHOWQUICKFIND", false);
	}

	public boolean isShowTableNumbers() {
		return showTableNumbers;
	}

	public void setShowTableNumbers(boolean showTableNumbers) {
		this.showTableNumbers = showTableNumbers;
		updateTournamentVisuals();
	}

	public boolean isShowQuickFind() {
		return showQuickFind;
	}

	public void setShowQuickFind(boolean showQuickFind) {
		this.showQuickFind = showQuickFind;
		updateTournamentVisuals();
	}

	private void updateTournamentVisuals() {
		if (CryodexController.isLoading == false
				&& CryodexController.getAllTournaments() != null) {
			for (Tournament t : CryodexController.getAllTournaments()) {
				t.updateVisualOptions();
			}
			CryodexController.saveData();
		}
	}

	@Override
	public StringBuilder appendXML(StringBuilder sb) {
		XMLUtils.appendObject(sb, "SHOWQUICKFIND", showQuickFind);
		XMLUtils.appendObject(sb, "SHOWTABLENUMBERS", showTableNumbers);

		return sb;
	}
}