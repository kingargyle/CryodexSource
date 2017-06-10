package cryodex.modules.runewars;

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
import cryodex.modules.runewars.RunewarsTournamentCreationWizard.WizardOptions;
import cryodex.xml.XMLUtils;
import cryodex.xml.XMLUtils.Element;

public class RunewarsModule implements Module {

	private static RunewarsModule module;

	public static RunewarsModule getInstance() {
		if (module == null) {
			module = new RunewarsModule();
		}

		return module;
	}

	private JCheckBoxMenuItem viewMenuItem;
	private RunewarsRegistrationPanel registrationPanel;
	private RunewarsMenu menu;
	private RunewarsOptions options;

	private boolean isEnabled = true;

	private RunewarsModule() {

	}

	@Override
	public Menu getMenu() {
		if (menu == null) {
			menu = new RunewarsMenu();
		}
		return menu;
	}

	@Override
	public RegistrationPanel getRegistration() {
		if (registrationPanel == null) {
			registrationPanel = new RunewarsRegistrationPanel();
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
		JDialog wizard = new RunewarsTournamentCreationWizard();
		wizard.setVisible(true);

	}

	public static void makeTournament(WizardOptions wizardOptions) {

		RunewarsTournament tournament = new RunewarsTournament(
				wizardOptions.getName(), wizardOptions.getPlayerList(),
				wizardOptions.getInitialSeedingEnum(),
				wizardOptions.getPoints(), wizardOptions.getEscalationPoints(),
				wizardOptions.isSingleElimination());

		CryodexController.registerTournament(tournament);

		tournament.startTournament();

		MenuBar.getInstance().resetMenuBar();

		CryodexController.saveData();
	}

	public RunewarsOptions getOptions() {
		if (options == null) {
			options = new RunewarsOptions();
		}
		return options;
	}

	@Override
	public StringBuilder appendXML(StringBuilder sb) {
		XMLUtils.appendXMLObject(sb, "OPTIONS", getOptions());
		XMLUtils.appendObject(sb, "NAME", Modules.RUNEWARS.getName());
		return sb;
	}

	@Override
	public ModulePlayer loadPlayer(Player p, Element element) {
		return new RunewarsPlayer(p, element);
	}

	@Override
	public Tournament loadTournament(Element element) {
		return new RunewarsTournament(element);
	}

	@Override
	public void loadModuleData(Element element) {
		options = new RunewarsOptions(element.getChild("OPTIONS"));
	}

	@Override
	public ModulePlayer getNewModulePlayer(Player player) {
		return new RunewarsPlayer(player);
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
