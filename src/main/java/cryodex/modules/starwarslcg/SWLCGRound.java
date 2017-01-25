package cryodex.modules.starwarslcg;

import java.util.ArrayList;
import java.util.List;

import cryodex.xml.XMLObject;
import cryodex.xml.XMLUtils;
import cryodex.xml.XMLUtils.Element;

public class SWLCGRound implements XMLObject {
	private List<SWLCGMatch> matches;
	private SWLCGRoundPanel panel;
	private Boolean isElimination = false;
	private SWLCGTournament parentTournament = null;

	public SWLCGRound(Element roundElement, SWLCGTournament t) {
		this.isElimination = roundElement
				.getBooleanFromChild("ISELIMINATION", false);

		Element matchElement = roundElement.getChild("MATCHES");

		if (matchElement != null) {
			matches = new ArrayList<SWLCGMatch>();
			for (Element e : matchElement.getChildren()) {
				matches.add(new SWLCGMatch(e));
			}
		}

		parentTournament = t;
	}

	public SWLCGRound(List<SWLCGMatch> matches, SWLCGTournament t,
			Integer roundNumber) {
		this.matches = matches;
		parentTournament = t;
	}

	public List<SWLCGMatch> getMatches() {
		return matches;
	}

	public void setMatches(List<SWLCGMatch> matches) {
		this.matches = matches;
	}

	public SWLCGRoundPanel getPanel() {
	    if(panel == null){
	        panel = new SWLCGRoundPanel(parentTournament, matches, isElimination);
	    }
		return panel;
	}

	public void setPanel(SWLCGRoundPanel panel) {
		this.panel = panel;
	}

	public void setElimination(boolean isElimination) {
		this.isElimination = isElimination;
	}

	public boolean isElimination() {
		return isElimination == null ? false : isElimination;
	}

	@Override
	public StringBuilder appendXML(StringBuilder sb) {

		XMLUtils.appendObject(sb, "ISELIMINATION", isElimination());
		XMLUtils.appendList(sb, "MATCHES", "MATCH", getMatches());

		return sb;
	}

	public boolean isComplete() {
		for (SWLCGMatch m : getMatches()) {
			if (m.isMatchComplete(isElimination()) == false) {
				return false;
			}
		}
		return true;
	}

	public boolean isValid() {
		boolean result = true;
		for (SWLCGMatch m : getMatches()) {
			if (m.isValidResult() == false) {
				result = false;
				break;
			}
		}

		panel.markInvalid();

		return result;
	}
}
