package cryodex.modules.starwarslcg;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JDialog;

import cryodex.CryodexController;
import cryodex.CryodexController.Modules;
import cryodex.MenuBar;
import cryodex.Player;
import cryodex.modules.Menu;
import cryodex.modules.Module;
import cryodex.modules.ModulePlayer;
import cryodex.modules.RegistrationPanel;
import cryodex.modules.Tournament;
import cryodex.modules.starwarslcg.SWLCGTournamentCreationWizard.WizardOptions;
import cryodex.xml.XMLUtils;
import cryodex.xml.XMLUtils.Element;

public class SWLCGModule implements Module {

	private static SWLCGModule module;

	public static SWLCGModule getInstance() {
		if (module == null) {
			module = new SWLCGModule();
		}

		return module;
	}

	private JCheckBoxMenuItem viewMenuItem;
	private SWLCGRegistrationPanel registrationPanel;
	private SWLCGMenu menu;
	private SWLCGOptions options;

	private boolean isEnabled = true;

	private SWLCGModule() {

	}

	@Override
	public Menu getMenu() {
		if (menu == null) {
			menu = new SWLCGMenu();
		}
		return menu;
	}

	@Override
	public RegistrationPanel getRegistration() {
		if (registrationPanel == null) {
			registrationPanel = new SWLCGRegistrationPanel();
		}
		return registrationPanel;
	}

	@Override
	public void setModuleEnabled(Boolean enabled) {
		isEnabled = enabled;

		getRegistration().getPanel().setVisible(enabled);
		getMenu().getMenu().setVisible(enabled);
	}

	@Override
	public boolean isModuleEnabled() {
		return isEnabled;
	}

	public static void createTournament() {
		JDialog wizard = new SWLCGTournamentCreationWizard();
		wizard.setVisible(true);

	}

	public static void makeTournament(WizardOptions wizardOptions) {

		SWLCGTournament tournament = new SWLCGTournament(wizardOptions.getName(),
				wizardOptions.getPlayerList(),
				wizardOptions.getInitialSeedingEnum(),
				wizardOptions.isSingleElimination());

		CryodexController.registerTournament(tournament);

		tournament.startTournament();

		MenuBar.getInstance().resetMenuBar();

		CryodexController.saveData();
	}

	public SWLCGOptions getOptions() {
		if (options == null) {
			options = new SWLCGOptions();
		}
		return options;
	}

	@Override
	public StringBuilder appendXML(StringBuilder sb) {
		XMLUtils.appendXMLObject(sb, "OPTIONS", getOptions());
		XMLUtils.appendObject(sb, "NAME", Modules.SWLCG.getName());
		return sb;
	}

	@Override
	public ModulePlayer loadPlayer(Player p, Element element) {
		return new SWLCGPlayer(p, element);
	}

	@Override
	public Tournament loadTournament(Element element) {
		return new SWLCGTournament(element);
	}

	@Override
	public void loadModuleData(Element element) {
		options = new SWLCGOptions(element.getChild("OPTIONS"));
	}

	@Override
	public ModulePlayer getNewModulePlayer(Player player) {
		return new SWLCGPlayer(player);
	}

	@Override
	public JCheckBoxMenuItem getViewMenuItem() {
		return viewMenuItem;
	}

	@Override
	public void setViewMenuItem(JCheckBoxMenuItem viewMenuItem) {
		this.viewMenuItem = viewMenuItem;
	}
}
