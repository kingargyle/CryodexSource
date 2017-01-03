package cryodex.modules.destiny;

import java.util.ArrayList;
import java.util.List;

import cryodex.xml.XMLObject;
import cryodex.xml.XMLUtils;
import cryodex.xml.XMLUtils.Element;

public class DestinyRound implements XMLObject {
	private List<DestinyMatch> matches;
	private DestinyRoundPanel panel;
	private Boolean isSingleElimination = false;

	public DestinyRound(Element roundElement, DestinyTournament t) {
		this.isSingleElimination = roundElement
				.getBooleanFromChild("ISSINGLEELIMINATION");

		Element matchElement = roundElement.getChild("MATCHES");

		if (matchElement != null) {
			matches = new ArrayList<DestinyMatch>();
			for (Element e : matchElement.getChildren()) {
				matches.add(new DestinyMatch(e));
			}
		}

		this.panel = new DestinyRoundPanel(t, matches);
	}

	public DestinyRound(List<DestinyMatch> matches, DestinyTournament t,
			Integer roundNumber) {
		this.matches = matches;
		this.panel = new DestinyRoundPanel(t, matches);
	}

	public List<DestinyMatch> getMatches() {
		return matches;
	}

	public void setMatches(List<DestinyMatch> matches) {
		this.matches = matches;
	}

	public DestinyRoundPanel getPanel() {
		return panel;
	}

	public void setPanel(DestinyRoundPanel panel) {
		this.panel = panel;
	}

	public void setSingleElimination(boolean isSingleElimination) {
		this.isSingleElimination = isSingleElimination;
	}

	public boolean isSingleElimination() {
		return isSingleElimination == null ? false : isSingleElimination;
	}

	@Override
	public StringBuilder appendXML(StringBuilder sb) {

		XMLUtils.appendObject(sb, "ISSINGLEELIMINATION", isSingleElimination());
		XMLUtils.appendList(sb, "MATCHES", "MATCH", getMatches());

		return sb;
	}

	public boolean isComplete() {
		for (DestinyMatch m : getMatches()) {
			if (m.isMatchComplete() == false) {
				return false;
			}
		}
		return true;
	}

	public boolean isValid() {
		boolean result = true;
		for (DestinyMatch m : getMatches()) {
			if (m.isValidResult() == false) {
				result = false;
				break;
			}
		}

		panel.markInvalid();

		return result;
	}
}
