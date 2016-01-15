package cryodex;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

import cryodex.CryodexController.Modules;
import cryodex.modules.Module;
import cryodex.widget.AboutPanel;

public class MenuBar extends JMenuBar {

	private static final long serialVersionUID = 1L;

	private JMenu fileMenu;
	private JMenu viewMenu;
	private JMenu helpMenu;

	private JCheckBoxMenuItem showTableNumbers;
	private JCheckBoxMenuItem showQuickFind;

	private static MenuBar instance;

	public static MenuBar getInstance() {
		if (instance == null) {
			instance = new MenuBar();
			instance.resetMenuBar();
		}
		return instance;
	}

	private MenuBar() {

		this.add(getFileMenu());
		this.add(getViewMenu());

		for (final Module m : CryodexController.getModules()) {
			this.add(m.getMenu().getMenu());
		}

		this.add(getHelpMenu());
	}

	public JMenu getFileMenu() {
		if (fileMenu == null) {
			fileMenu = new JMenu("File");
			fileMenu.setMnemonic('F');

			JMenuItem exit = new JMenuItem("Exit");
			exit.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					Main.getInstance().dispose();
				}
			});

			fileMenu.add(exit);
		}

		return fileMenu;
	}

	public JMenu getViewMenu() {
		if (viewMenu == null) {
			viewMenu = new JMenu("View");
			viewMenu.setMnemonic('V');

			showTableNumbers = new JCheckBoxMenuItem("Show Table Numbers");
			showTableNumbers.addItemListener(new ItemListener() {

				@Override
				public void itemStateChanged(ItemEvent e) {
					CryodexController.getOptions().setShowTableNumbers(
							showTableNumbers.isSelected());
				}
			});

			showQuickFind = new JCheckBoxMenuItem("Show Quick Table Search");
			showQuickFind.setSelected(false);
			showQuickFind.addItemListener(new ItemListener() {

				@Override
				public void itemStateChanged(ItemEvent e) {
					CryodexController.getOptions().setShowQuickFind(
							showQuickFind.isSelected());
				}
			});

			final JCheckBoxMenuItem showRegistrationPanel = new JCheckBoxMenuItem(
					"Show Registration Panel");
			showRegistrationPanel.setSelected(true);
			showRegistrationPanel.addItemListener(new ItemListener() {

				@Override
				public void itemStateChanged(ItemEvent arg0) {
					Main.getInstance().getRegisterPane()
							.remove(Main.getInstance().getRegisterPanel());
					if (showRegistrationPanel.isSelected()) {
						Main.getInstance().getRegisterPane()
								.add(Main.getInstance().getRegisterPanel());
					}

					Main.getInstance().validate();
					Main.getInstance().repaint();
				}
			});

			viewMenu.add(showQuickFind);
			viewMenu.add(showTableNumbers);
			viewMenu.add(showRegistrationPanel);

			for (final Module m : CryodexController.getModules()) {
				final JCheckBoxMenuItem moduleItem = new JCheckBoxMenuItem(
						Modules.getNameByModule(m));
				moduleItem.setSelected(m.isModuleEnabled());
				moduleItem.addItemListener(new ItemListener() {

					@Override
					public void itemStateChanged(ItemEvent arg0) {
						m.setModuleEnabled(moduleItem.isSelected());
						Modules moduleEnum = Modules.getEnumByName(Modules
								.getNameByModule(m));
						if (moduleItem.isSelected()) {
							CryodexController.getOptions()
									.getNonVisibleModules().remove(moduleEnum);
						} else {
							CryodexController.getOptions()
									.getNonVisibleModules().add(moduleEnum);
						}
						CryodexController.saveData();
					}
				});

				viewMenu.add(moduleItem);
			}
		}

		return viewMenu;
	}

	@Override
	public JMenu getHelpMenu() {
		if (helpMenu == null) {
			helpMenu = new JMenu("Help");
			helpMenu.setMnemonic('H');

			JMenuItem about = new JMenuItem("About");
			about.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent arg0) {
					AboutPanel.showAboutPanel();
				}
			});
			JMenuItem whereIsSave = new JMenuItem("Where is my save file?");
			whereIsSave.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent arg0) {
					File path = new File(System.getProperty("user.dir"));
					if (path.exists() == false) {
						System.out.println("Error with user directory");
					}
					File file = new File(path, "XWingTournament.save");

					if (file.exists()) {
						JOptionPane.showMessageDialog(
								Main.getInstance(),
								"<html>Save file can be found at <b>"
										+ file.getAbsolutePath()
										+ "</b></html>");
					} else if (path.exists() == false) {
						JOptionPane.showMessageDialog(
								Main.getInstance(),
								"Save location could not be determined. Check permissions to allow a Java application to save a file.");
					} else if (file.exists() == false) {
						JOptionPane.showMessageDialog(
								Main.getInstance(),
								"<html>A save file could not be found. It should be called <b>XWingTournament.save</b> and should be located in folder <b>"
										+ path.getAbsolutePath()
										+ "</b></html>");
					}
				}
			});

			helpMenu.add(about);
			helpMenu.add(whereIsSave);
		}
		return helpMenu;
	}

	public void resetMenuBar() {

		showTableNumbers.setSelected(CryodexController.getOptions()
				.isShowTableNumbers());
		showQuickFind.setSelected(CryodexController.getOptions()
				.isShowQuickFind());

		for (Module m : CryodexController.getModules()) {
			m.getMenu().resetMenuBar();
		}
	}

	public void updateTournamentOptions(CryodexOptions options) {
		options.setShowTableNumbers(showTableNumbers.isSelected());
		options.setShowQuickFind(showQuickFind.isSelected());
	}

}
