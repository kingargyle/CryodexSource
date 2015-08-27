package cryodex.modules.armada;

import java.util.ArrayList;
import java.util.List;

import cryodex.xml.XMLObject;
import cryodex.xml.XMLUtils;
import cryodex.xml.XMLUtils.Element;

public class ArmadaRound implements XMLObject {
	private List<ArmadaMatch> matches;
	private ArmadaRoundPanel panel;
	private Boolean isSingleElimination = false;

	public ArmadaRound(Element roundElement, ArmadaTournament t) {
		this.isSingleElimination = roundElement
				.getBooleanFromChild("ISSINGLEELIMINATION");

		Element matchElement = roundElement.getChild("MATCHES");

		if (matchElement != null) {
			matches = new ArrayList<ArmadaMatch>();
			for (Element e : matchElement.getChildren()) {
				matches.add(new ArmadaMatch(e));
			}
		}

		this.panel = new ArmadaRoundPanel(t, matches);
	}

	public ArmadaRound(List<ArmadaMatch> matches, ArmadaTournament t,
			Integer roundNumber) {
		this.matches = matches;
		this.panel = new ArmadaRoundPanel(t, matches);
	}

	public List<ArmadaMatch> getMatches() {
		return matches;
	}

	public void setMatches(List<ArmadaMatch> matches) {
		this.matches = matches;
	}

	public ArmadaRoundPanel getPanel() {
		return panel;
	}

	public void setPanel(ArmadaRoundPanel panel) {
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
		for (ArmadaMatch m : getMatches()) {
			if (m.isMatchComplete() == false) {
				return false;
			}
		}
		return true;
	}

	public boolean isValid() {
		boolean result = true;
		for (ArmadaMatch m : getMatches()) {
			if (m.isValidResult() == false) {
				result = false;
				break;
			}
		}

		panel.markInvalid();

		return result;
	}
}
