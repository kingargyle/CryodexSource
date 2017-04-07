package cryodex.modules.runewars;

import java.util.ArrayList;
import java.util.List;

import cryodex.xml.XMLObject;
import cryodex.xml.XMLUtils;
import cryodex.xml.XMLUtils.Element;

public class RunewarsRound implements XMLObject {
	private List<RunewarsMatch> matches;
	private RunewarsRoundPanel panel;
	private Boolean isSingleElimination = false;

	public RunewarsRound(Element roundElement, RunewarsTournament t) {
		this.isSingleElimination = roundElement
				.getBooleanFromChild("ISSINGLEELIMINATION");

		Element matchElement = roundElement.getChild("MATCHES");

		if (matchElement != null) {
			matches = new ArrayList<RunewarsMatch>();
			for (Element e : matchElement.getChildren()) {
				matches.add(new RunewarsMatch(e));
			}
		}

		this.panel = new RunewarsRoundPanel(t, matches);
	}

	public RunewarsRound(List<RunewarsMatch> matches, RunewarsTournament t,
			Integer roundNumber) {
		this.matches = matches;
		this.panel = new RunewarsRoundPanel(t, matches);
	}

	public List<RunewarsMatch> getMatches() {
		return matches;
	}

	public void setMatches(List<RunewarsMatch> matches) {
		this.matches = matches;
	}

	public RunewarsRoundPanel getPanel() {
		return panel;
	}

	public void setPanel(RunewarsRoundPanel panel) {
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
		for (RunewarsMatch m : getMatches()) {
			if (m.isMatchComplete() == false) {
				return false;
			}
		}
		return true;
	}

	public boolean isValid() {
		boolean result = true;
		for (RunewarsMatch m : getMatches()) {
			if (m.isValidResult() == false) {
				result = false;
				break;
			}
		}

		panel.markInvalid();

		return result;
	}
}
