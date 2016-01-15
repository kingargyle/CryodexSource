package cryodex.modules.armada;

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
import cryodex.modules.armada.ArmadaTournamentCreationWizard.WizardOptions;
import cryodex.xml.XMLUtils;
import cryodex.xml.XMLUtils.Element;

public class ArmadaModule implements Module {

	private static ArmadaModule module;

	public static ArmadaModule getInstance() {
		if (module == null) {
			module = new ArmadaModule();
		}

		return module;
	}

	private ArmadaRegistrationPanel registrationPanel;
	private ArmadaMenu menu;
	private ArmadaOptions options;

	private boolean isEnabled = true;

	private ArmadaModule() {

	}

	@Override
	public Menu getMenu() {
		if (menu == null) {
			menu = new ArmadaMenu();
		}
		return menu;
	}

	@Override
	public RegistrationPanel getRegistration() {
		if (registrationPanel == null) {
			registrationPanel = new ArmadaRegistrationPanel();
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
		JDialog wizard = new ArmadaTournamentCreationWizard();
		wizard.setVisible(true);

	}

	public static void makeTournament(WizardOptions wizardOptions) {

		ArmadaTournament tournament = new ArmadaTournament(
				wizardOptions.getName(), wizardOptions.getPlayerList(),
				wizardOptions.getInitialSeedingEnum(),
				wizardOptions.getPoints(), wizardOptions.getEscalationPoints(),
				wizardOptions.isSingleElimination());

		CryodexController.registerTournament(tournament);

		tournament.startTournament();

		MenuBar.getInstance().resetMenuBar();

		CryodexController.saveData();
	}

	public ArmadaOptions getOptions() {
		if (options == null) {
			options = new ArmadaOptions();
		}
		return options;
	}

	@Override
	public StringBuilder appendXML(StringBuilder sb) {
		XMLUtils.appendXMLObject(sb, "OPTIONS", getOptions());
		XMLUtils.appendObject(sb, "NAME", Modules.ARMADA.getName());
		return sb;
	}

	@Override
	public ModulePlayer loadPlayer(Player p, Element element) {
		return new ArmadaPlayer(p, element);
	}

	@Override
	public Tournament loadTournament(Element element) {
		return new ArmadaTournament(element);
	}

	@Override
	public void loadModuleData(Element element) {
		options = new ArmadaOptions(element.getChild("OPTIONS"));
	}

	@Override
	public ModulePlayer getNewModulePlayer(Player player) {
		return new ArmadaPlayer(player);
	}
}
