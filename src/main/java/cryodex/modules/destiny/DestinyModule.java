package cryodex.modules.destiny;

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
import cryodex.modules.destiny.DestinyTournamentCreationWizard.WizardOptions;
import cryodex.xml.XMLUtils;
import cryodex.xml.XMLUtils.Element;

public class DestinyModule implements Module {

	private static DestinyModule module;

	public static DestinyModule getInstance() {
		if (module == null) {
			module = new DestinyModule();
		}

		return module;
	}

	private JCheckBoxMenuItem viewMenuItem;
	private DestinyRegistrationPanel registrationPanel;
	private DestinyMenu menu;
	private DestinyOptions options;

	private boolean isEnabled = true;

	private DestinyModule() {

	}

	@Override
	public Menu getMenu() {
		if (menu == null) {
			menu = new DestinyMenu();
		}
		return menu;
	}

	@Override
	public RegistrationPanel getRegistration() {
		if (registrationPanel == null) {
			registrationPanel = new DestinyRegistrationPanel();
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
		JDialog wizard = new DestinyTournamentCreationWizard();
		wizard.setVisible(true);

	}

	public static void makeTournament(WizardOptions wizardOptions) {

		DestinyTournament tournament = new DestinyTournament(wizardOptions.getName(),
				wizardOptions.getPlayerList(),
				wizardOptions.getInitialSeedingEnum(),
				wizardOptions.isSingleElimination());

		CryodexController.registerTournament(tournament);

		tournament.startTournament();

		MenuBar.getInstance().resetMenuBar();

		CryodexController.saveData();
	}

	public DestinyOptions getOptions() {
		if (options == null) {
			options = new DestinyOptions();
		}
		return options;
	}

	@Override
	public StringBuilder appendXML(StringBuilder sb) {
		XMLUtils.appendXMLObject(sb, "OPTIONS", getOptions());
		XMLUtils.appendObject(sb, "NAME", Modules.DESTINY.getName());
		return sb;
	}

	@Override
	public ModulePlayer loadPlayer(Player p, Element element) {
		return new DestinyPlayer(p, element);
	}

	@Override
	public Tournament loadTournament(Element element) {
		return new DestinyTournament(element);
	}

	@Override
	public void loadModuleData(Element element) {
		options = new DestinyOptions(element.getChild("OPTIONS"));
	}

	@Override
	public ModulePlayer getNewModulePlayer(Player player) {
		return new DestinyPlayer(player);
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
