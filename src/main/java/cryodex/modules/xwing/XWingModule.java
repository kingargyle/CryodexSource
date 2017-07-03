package cryodex.modules.xwing;

import javax.naming.InitialContext;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JDialog;

import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.HandlerCollection;

import cryodex.CryodexController;
import cryodex.CryodexController.Modules;
import cryodex.MenuBar;
import cryodex.Player;
import cryodex.modules.Menu;
import cryodex.modules.Module;
import cryodex.modules.ModulePlayer;
import cryodex.modules.RegistrationPanel;
import cryodex.modules.Tournament;
import cryodex.modules.xwing.server.XWingExportRankingsHandler;
import cryodex.modules.xwing.wizard.WizardOptions;
import cryodex.modules.xwing.wizard.XWingWizard;
import cryodex.server.WebServer;
import cryodex.xml.XMLUtils;
import cryodex.xml.XMLUtils.Element;

public class XWingModule implements Module {

	private static XWingModule module;

	public static XWingModule getInstance() {
		if (module == null) {
			module = new XWingModule();
		}

		return module;
	}

	private JCheckBoxMenuItem viewMenuItem;
	private XWingRegistrationPanel registrationPanel;
	private XWingMenu menu;
	private XWingOptions options;

	private boolean isEnabled = true;

	private XWingModule() {
	   init();
	}
	
	private void init() {
	   
	   ContextHandler xwingContextHandler = new ContextHandler();
	   xwingContextHandler.setContextPath("/xwing");
	   HandlerCollection handlerCollection = new HandlerCollection();
	   XWingExportRankingsHandler exportRankingsHandler = new XWingExportRankingsHandler();
	   handlerCollection.addHandler(exportRankingsHandler);
	   xwingContextHandler.setHandler(handlerCollection);
	   
	   WebServer.getInstance().addContextHandler(xwingContextHandler); 
	}

	@Override
	public Menu getMenu() {
		if (menu == null) {
			menu = new XWingMenu();
		}
		return menu;
	}

	@Override
	public RegistrationPanel getRegistration() {
		if (registrationPanel == null) {
			registrationPanel = new XWingRegistrationPanel();
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
		JDialog wizard = XWingWizard.getInstance();
		wizard.setVisible(true);

	}

	public static void makeTournament(WizardOptions wizardOptions) {

		XWingTournament tournament = new XWingTournament(
				wizardOptions.getName(), wizardOptions.getPlayerList(),
				wizardOptions.getInitialSeedingEnum(),
				wizardOptions.getPoints(),
				wizardOptions.isSingleElimination(),
                                wizardOptions.isRoundRobin());

		// Add dependent events from a progressive cut
		if(wizardOptions.isMerge() == false && wizardOptions.getSelectedTournaments() != null && wizardOptions.getSelectedTournaments().isEmpty() == false){
		    tournament.addDependentTournaments(wizardOptions.getSelectedTournaments());
		}
		
		CryodexController.registerTournament(tournament);

		tournament.startTournament();

		MenuBar.getInstance().resetMenuBar();

		CryodexController.saveData();
	}

	public XWingOptions getOptions() {
		if (options == null) {
			options = new XWingOptions();
		}
		return options;
	}

	@Override
	public StringBuilder appendXML(StringBuilder sb) {
		XMLUtils.appendXMLObject(sb, "OPTIONS", getOptions());
		XMLUtils.appendObject(sb, "NAME", Modules.XWING.getName());
		return sb;
	}

	@Override
	public ModulePlayer loadPlayer(Player p, Element element) {
		return new XWingPlayer(p, element);
	}

	@Override
	public Tournament loadTournament(Element element) {
		return new XWingTournament(element);
	}

	@Override
	public void loadModuleData(Element element) {
		options = new XWingOptions(element.getChild("OPTIONS"));
	}

	@Override
	public ModulePlayer getNewModulePlayer(Player player) {
		return new XWingPlayer(player);
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
