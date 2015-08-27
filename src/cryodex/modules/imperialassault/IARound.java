package cryodex.modules.imperialassault;

import java.util.ArrayList;
import java.util.List;

import cryodex.xml.XMLObject;
import cryodex.xml.XMLUtils;
import cryodex.xml.XMLUtils.Element;

public class IARound implements XMLObject {
	private List<IAMatch> matches;
	private IARoundPanel panel;
	private Boolean isSingleElimination = false;

	public IARound(Element roundElement, IATournament t) {
		this.isSingleElimination = roundElement
				.getBooleanFromChild("ISSINGLEELIMINATION");

		Element matchElement = roundElement.getChild("MATCHES");

		if (matchElement != null) {
			matches = new ArrayList<IAMatch>();
			for (Element e : matchElement.getChildren()) {
				matches.add(new IAMatch(e));
			}
		}

		this.panel = new IARoundPanel(t, matches);
	}

	public IARound(List<IAMatch> matches, IATournament t,
			Integer roundNumber) {
		this.matches = matches;
		this.panel = new IARoundPanel(t, matches);
	}

	public List<IAMatch> getMatches() {
		return matches;
	}

	public void setMatches(List<IAMatch> matches) {
		this.matches = matches;
	}

	public IARoundPanel getPanel() {
		return panel;
	}

	public void setPanel(IARoundPanel panel) {
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
		for (IAMatch m : getMatches()) {
			if (m.isMatchComplete() == false) {
				return false;
			}
		}
		return true;
	}

	public boolean isValid() {
		boolean result = true;
		for (IAMatch m : getMatches()) {
			if (m.isValidResult() == false) {
				result = false;
				break;
			}
		}

		panel.markInvalid();

		return result;
	}
}
