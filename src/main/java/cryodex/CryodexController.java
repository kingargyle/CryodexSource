package cryodex;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.Frame;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;

import cryodex.modules.Module;
import cryodex.modules.Tournament;
import cryodex.modules.armada.ArmadaModule;
import cryodex.modules.imperialassault.IAModule;
import cryodex.modules.xwing.XWingModule;
import cryodex.widget.ComponentUtils;
import cryodex.xml.XMLUtils;
import cryodex.xml.XMLUtils.Element;

public class CryodexController {

	public static enum Modules {
		XWING("X-Wing", XWingModule.getInstance()), IA("Imperial Assault",
				IAModule.getInstance()), ARMADA("Armada", ArmadaModule
				.getInstance());

		Module module;
		String name;

		private Modules(String name, Module m) {
			this.module = m;
			this.name = name;
		}

		public Module getModule() {
			return module;
		}

		public String getName() {
			return name;
		}

		public static String getNameByModule(Module m) {
			for (Modules me : values()) {
				if (me.getModule() == m) {
					return me.getName();
				}
			}
			return null;
		}

		public static Module getModuleByName(String name) {
			for (Modules me : values()) {
				if (me.getName().equals(name)) {
					return me.getModule();
				}
			}
			return null;
		}

		public static Modules getEnumByName(String name) {
			for (Modules me : values()) {
				if (me.getName().equals(name)) {
					return me;
				}
			}
			return null;
		}
	}

	public static final String CRYODEX_SAVE = "Cryodex.save";
	
	private static List<Tournament> tournaments = new ArrayList<Tournament>();
	private static List<Player> players = new ArrayList<Player>();
	private static List<Module> modules;

	private static CryodexOptions options;

	public static boolean isLoading;

	public static List<Module> getModules() {
		if (modules == null) {
			modules = new ArrayList<Module>();

			for (Modules m : Modules.values()) {
				modules.add(m.getModule());
			}
		}

		return modules;
	}

	public static int getTournamentCount() {
		if (tournaments == null) {
			return 0;
		}
		return tournaments.size();
	}

	public static List<Player> getPlayers() {
		if (players == null) {
			players = new ArrayList<Player>();
		}
		return players;
	}

	public static Player getPlayerByID(String id) {
		for (Player p : getPlayers()) {
			if (p.getSaveId() != null && p.getSaveId().equals(id)) {
				return p;
			}
		}
		return null;
	}

	public static void registerTournament(Tournament t) {
		tournaments.add(t);
		display();
	}

	public static Tournament getActiveTournament() {
		if (tournaments == null || tournaments.isEmpty()) {
			return null;
		} else if (tournaments.size() == 1) {
			return tournaments.get(0);
		} else {
			int index = Main.getInstance().getMultipleTournamentTabbedPane()
					.getSelectedIndex();
			return tournaments.get(index);
		}
	}

	public static List<Tournament> getAllTournaments() {
		return tournaments;
	}

	public static void deleteTournament(boolean check) {

		if (getActiveTournament() == null) {
			JOptionPane.showMessageDialog(Main.getInstance(),
					"There are no tournaments to cancel.");
			return;
		}

		int result = JOptionPane.YES_OPTION;

		if (check) {
			result = JOptionPane
					.showConfirmDialog(
							Main.getInstance(),
							"This action will cancel the entire tournament. Are you sure you want to do this?");
		}

		if (result == JOptionPane.YES_OPTION) {

			Tournament tournament = getActiveTournament();

			if (tournaments.size() == 1) {
				Main.getInstance().getSingleTournamentPane().removeAll();
				ComponentUtils.repaint(Main.getInstance()
						.getSingleTournamentPane());
			} else {
				int selectedIndex = Main.getInstance()
						.getMultipleTournamentTabbedPane().getSelectedIndex();
				Main.getInstance().getMultipleTournamentTabbedPane()
						.remove(selectedIndex);
				ComponentUtils.repaint(Main.getInstance()
						.getMultipleTournamentTabbedPane());
			}

			tournaments.remove(tournament);

			display();

			saveData();
			MenuBar.getInstance().resetMenuBar();

			Main.getInstance().validate();
			Main.getInstance().repaint();
		}
	}

	public static CryodexOptions getOptions() {
		if (options == null) {
			options = new CryodexOptions();
		}

		return options;
	}

	public static void display() {
		if (tournaments.isEmpty()) {
			// Do nothing!
		} else if (tournaments.size() == 1) {

			// If there is only one tournament we do a single pane

			Tournament t = tournaments.get(0);
			Main.getInstance()
					.getSingleTournamentPane()
					.add(t.getTournamentGUI().getDisplay(), BorderLayout.CENTER);
			Main.getInstance().setMultiple(false);
			ComponentUtils
					.repaint(Main.getInstance().getSingleTournamentPane());
		} else if (tournaments.size() == 2) {

			// At two tournaments we switch to the multiple tabbed pane

			Tournament t0 = tournaments.get(0);
			Tournament t1 = tournaments.get(1);

			String t0Name = t0.getName() == null ? "Event 1" : t0.getName();
			String t1Name = t1.getName() == null ? "Event 2" : t1.getName();

			Main.getInstance()
					.getMultipleTournamentTabbedPane()
					.addTab(t0Name, t0.getIcon(),
							t0.getTournamentGUI().getDisplay());

			Main.getInstance()
					.getMultipleTournamentTabbedPane()
					.addTab(t1Name, t1.getIcon(),
							t1.getTournamentGUI().getDisplay());

			Main.getInstance().setMultiple(true);
			ComponentUtils.repaint(Main.getInstance()
					.getMultipleTournamentTabbedPane());
		} else if (tournaments.size() > 2) {

			// Each tournament after 2 just adds another tab

			Tournament tn = tournaments.get(tournaments.size() - 1);
			String tnName = tn.getName() == null ? "Event "
					+ tournaments.size() : tn.getName();
			Main.getInstance()
					.getMultipleTournamentTabbedPane()
					.addTab(tnName, tn.getIcon(),
							tn.getTournamentGUI().getDisplay());
			ComponentUtils.repaint(Main.getInstance()
					.getMultipleTournamentTabbedPane());
		}

	}

	public static void saveData() {

		if (isLoading) {
			return;
		}

		int saveId = 0;
		for (Player p : getPlayers()) {
			p.setSaveId(String.valueOf(saveId));
			saveId++;
		}

		StringBuilder sb = new StringBuilder();

		sb.append("<CRYODEXDATA>\n");

		XMLUtils.appendList(sb, XMLUtils.PLAYERS, XMLUtils.PLAYER, getPlayers());
		XMLUtils.appendXMLObject(sb, XMLUtils.OPTIONS, getOptions());

		if (getAllTournaments().isEmpty() == false) {
			XMLUtils.appendList(sb, XMLUtils.TOURNAMENTS, XMLUtils.TOURNAMENT,
					getAllTournaments());
		}

		XMLUtils.appendList(sb, "MODULES", "MODULE", getModules());

		sb.append("</CRYODEXDATA>");

		try {
			File path = new File(System.getProperty("user.dir"));
			if (path.exists() == false) {
				System.out.println("Error with user directory");
				throw new IOException("Error with user directory");
			}
			File file = new File(path, CRYODEX_SAVE);
			if (file.exists() == false) {
				file.createNewFile();
			} else {
				file.delete();
				file.createNewFile();
			}

			FileOutputStream stream = new FileOutputStream(file);

			stream.write(sb.toString().getBytes());
			stream.flush();
			stream.close();
			Main.getInstance().setError(null);
		} catch (IOException e) {
			e.printStackTrace();
			Main.getInstance().setError("There is a problem with the save function. Your event is unprotected. Contact the developer for advice.");
		}
	}

	public static void loadData() {
		isLoading = true;
		File file = null;
		try {
			File path = new File(System.getProperty("user.dir"));
			if (path.exists() == false) {
				System.out.println("Error with user directory");
			}
			file = new File(path, CRYODEX_SAVE);

			if (file.exists() == false) {
				return;
			}

			BufferedReader reader = new BufferedReader(new FileReader(file));

			Element mainElement = XMLUtils.getItem(reader);

			if (mainElement == null) {
				if (file != null && file.exists()) {
					file.delete();
				}
				return;
			}

			Element optionsElement = mainElement.getChild(XMLUtils.OPTIONS);

			if (optionsElement != null) {
				options = new CryodexOptions(optionsElement);
			}

			Element playersElement = mainElement.getChild(XMLUtils.PLAYERS);

			if (playersElement != null) {
				List<Player> playersList = new ArrayList<Player>();

				for (Element element : playersElement.getChildren()) {
					playersList.add(new Player(element));
				}

				getPlayers().addAll(playersList);
			}

			Element modulesElement = mainElement.getChild("MODULES");

			if (modulesElement != null) {
				for (Element element : modulesElement.getChildren()) {
					String moduleName = element.getStringFromChild("NAME");

					Module module = null;
					if (moduleName == null) {
						module = Modules.XWING.getModule();
					} else {
						module = Modules.getModuleByName(moduleName);
					}

					module.loadModuleData(element);
				}
			}

			Element tournamentsElement = mainElement
					.getChild(XMLUtils.TOURNAMENTS);

			if (tournamentsElement != null) {
				Main.getInstance().setExtendedState(Frame.MAXIMIZED_BOTH);

				for (Element element : tournamentsElement.getChildren()) {

					String moduleName = element.getStringFromChild("MODULE");

					Module module = null;
					if (moduleName == null) {
						module = Modules.XWING.getModule();
					} else {
						module = Modules.getModuleByName(moduleName);
					}

					Tournament t = module.loadTournament(element);

					getAllTournaments().add(t);
					display();
				}
			}

			reader.close();
		} catch (Exception e) {
			e.printStackTrace();
			JOptionPane
					.showMessageDialog(
							Main.getInstance(),
							"There was an error loading data. The save file version is incompatable or the data is corrupt. You may have to delete it if Cryodex doesn't load completely.");
		} finally {
			isLoading = false;
		}
	}

	public static void sendDonation() {
		URL donationURL;
		try {
			donationURL = new URL(
					"https://www.paypal.com/cgi-bin/webscr?cmd=_donations&business=chris%2ebrown%2espe%40gmail%2ecom&lc=US&item_name=Cryodex&currency_code=USD&bn=PP%2dDonationsBF%3abtn_donateCC_LG%2egif%3aNonHosted");
			openWebpage(donationURL);
		} catch (Exception e) {
			JOptionPane
					.showMessageDialog(
							(Component) null,
							"Well you gave it a shot, and thanks for that. It looks like I'm having trouble opening your browser. If you're still determined, you can send any donations to chris.brown.spe@gmail.com.",
							"Error", JOptionPane.ERROR_MESSAGE);
		}
	}

	public static void openWebpage(URI uri) throws Exception {
		Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop()
				: null;
		if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
			desktop.browse(uri);
		} else {
			throw new Exception("");
		}
	}

	public static void openWebpage(URL url) throws Exception {
		openWebpage(url.toURI());
	}

}
