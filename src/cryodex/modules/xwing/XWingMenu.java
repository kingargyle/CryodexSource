package cryodex.modules.xwing;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import cryodex.CryodexController;
import cryodex.CryodexController.Modules;
import cryodex.Main;
import cryodex.Player;
import cryodex.modules.Menu;
import cryodex.modules.xwing.export.XWingExportController;
import cryodex.modules.xwing.export.XWingJSONBuilder;
import cryodex.widget.ComponentUtils;

@SuppressWarnings("serial")
public class XWingMenu implements Menu {

	private JMenu mainMenu;

	private JMenu viewMenu;
	private JMenu tournamentMenu;
	private JMenu roundMenu;
	private JMenu exportMenu;

	private JMenuItem deleteTournament;

	private JCheckBoxMenuItem showKillPoints;
	private JCheckBoxMenuItem onlyEnterPoints;

	private JMenuItem cutPlayers;

	@Override
	public JMenu getMenu() {

		if (mainMenu == null) {

			mainMenu = new JMenu(Modules.XWING.getName());
			mainMenu.setMnemonic('X');

			JMenuItem createNewTournament = new JMenuItem(
					"Create New Tournament");
			createNewTournament.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					Main.getInstance().setExtendedState(Frame.MAXIMIZED_BOTH);
					XWingModule.createTournament();
				}
			});

			deleteTournament = new JMenuItem("Delete Tournament");
			deleteTournament.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					CryodexController.deleteTournament(true);
				}
			});

			mainMenu.add(createNewTournament);
			mainMenu.add(deleteTournament);
			mainMenu.add(getViewMenu());
			mainMenu.add(getTournamentMenu());
			mainMenu.add(getRoundMenu());
			mainMenu.add(getExportMenu());
		}

		return mainMenu;
	}

	public JMenu getViewMenu() {
		if (viewMenu == null) {
			viewMenu = new JMenu("View");

			showKillPoints = new JCheckBoxMenuItem("Show Kill Points");
			showKillPoints.setSelected(XWingModule.getInstance().getOptions()
					.isShowKillPoints());
			showKillPoints.addItemListener(new ItemListener() {

				@Override
				public void itemStateChanged(ItemEvent e) {
					XWingModule.getInstance().getOptions()
							.setShowKillPoints(showKillPoints.isSelected());
				}
			});

			onlyEnterPoints = new JCheckBoxMenuItem("Only Enter Points");
			onlyEnterPoints.setSelected(XWingModule.getInstance().getOptions()
					.isEnterOnlyPoints());
			onlyEnterPoints.addItemListener(new ItemListener() {

				@Override
				public void itemStateChanged(ItemEvent e) {
					XWingModule.getInstance().getOptions()
							.setEnterOnlyPoints(onlyEnterPoints.isSelected());
				}
			});

			viewMenu.add(showKillPoints);
			viewMenu.add(onlyEnterPoints);
		}

		return viewMenu;
	}

	public JMenu getTournamentMenu() {
		if (tournamentMenu == null) {
			tournamentMenu = new JMenu("Tournament");

			JMenuItem addPlayer = new JMenuItem("Add Player");
			addPlayer.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent arg0) {

					List<Player> players = new ArrayList<Player>();
					players.addAll(CryodexController.getPlayers());

					for (Player p : CryodexController.getActiveTournament()
							.getPlayers()) {
						players.remove(p);
					}

					if (players.isEmpty()) {
						JOptionPane.showMessageDialog(Main.getInstance(),
								"All players are already in the tournament.");
					} else {

						PlayerSelectionDialog<Player> d = new PlayerSelectionDialog<Player>(
								players) {

							@Override
							public void playerSelected(Player p) {
								CryodexController.getActiveTournament()
										.addPlayer(p);

								CryodexController.getActiveTournament()
										.resetRankingTable();
							}
						};

						d.setVisible(true);
					}
				}
			});

			JMenuItem dropPlayer = new JMenuItem("Drop Player");
			dropPlayer.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent arg0) {

					JDialog d = new PlayerSelectionDialog<Player>(
							CryodexController.getActiveTournament()
									.getPlayers()) {

						@Override
						public void playerSelected(Player p) {
							CryodexController.getActiveTournament().dropPlayer(
									p);
						}
					};
					d.setVisible(true);

				}
			});

			JMenuItem generateNextRound = new JMenuItem("Generate Next Round");
			generateNextRound.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					CryodexController.getActiveTournament().generateNextRound();
				}
			});

			tournamentMenu.add(generateNextRound);
			tournamentMenu.add(addPlayer);
			tournamentMenu.add(dropPlayer);
			tournamentMenu.add(getCutPlayers());
		}

		return tournamentMenu;
	}

	public JMenu getRoundMenu() {
		if (roundMenu == null) {
			roundMenu = new JMenu("Round");

			JMenuItem generateNextRound = new JMenuItem("Regenerate Round");
			generateNextRound.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {

					XWingTournament tournament = (XWingTournament) CryodexController
							.getActiveTournament();

					int index = tournament.getTournamentGUI()
							.getRoundTabbedPane().getSelectedIndex();

					int result = JOptionPane.showConfirmDialog(
							Main.getInstance(),
							"Regenerating a round will cancel all results and destroy any subsequent rounds. Are you sure you want to do this?");

					if (result == JOptionPane.OK_OPTION) {
						XWingRound r = tournament.getRound(index);
						if (r.isSingleElimination()) {
							int playerCount = r.getMatches().size() * 2;
							tournament.cancelRound(tournament.getRoundNumber(r));
							tournament
									.generateSingleEliminationMatches(playerCount);
						} else {
							tournament.generateRound(index + 1);
						}

						tournament.getTournamentGUI().getRoundTabbedPane()
								.validate();
						tournament.getTournamentGUI().getRoundTabbedPane()
								.repaint();
					}

				}
			});

			JMenuItem cancelRound = new JMenuItem("Cancel Round");
			cancelRound.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {

					XWingTournament tournament = (XWingTournament) CryodexController
							.getActiveTournament();

					int index = tournament.getTournamentGUI()
							.getRoundTabbedPane().getSelectedIndex();

					if (index == 0) {
						CryodexController.deleteTournament(true);
						return;
					}

					int result = JOptionPane.showConfirmDialog(
							Main.getInstance(),
							"Cancelling a round will cancel all results and destroy any subsequent rounds. Are you sure you want to do this?");

					if (result == JOptionPane.OK_OPTION) {
						tournament.cancelRound(index + 1);

						tournament.getTournamentGUI().getRoundTabbedPane()
								.setSelectedIndex(index - 1);

						CryodexController.saveData();

						resetMenuBar();
					}
				}
			});

			JMenuItem swapPlayers = new JMenuItem("Swap Players");
			swapPlayers.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {

					XWingTournament tournament = (XWingTournament) CryodexController
							.getActiveTournament();

					if (tournament.getSelectedRound().isComplete()) {
						JOptionPane.showMessageDialog(Main.getInstance(),
								"Current round is complete. Players cannot be swapped.");
						return;
					}

					XWingSwapPanel.showSwapPanel();
				}
			});

			roundMenu.add(generateNextRound);
			roundMenu.add(cancelRound);
			roundMenu.add(swapPlayers);
		}
		return roundMenu;
	}

	public JMenuItem getCutPlayers() {
		if (cutPlayers == null) {
			cutPlayers = new JMenuItem("Cut To Top Players");
			cutPlayers.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent arg0) {

					XWingTournament tournament = (XWingTournament) CryodexController
							.getActiveTournament();

					if (tournament.getLatestRound().isComplete() == false) {
						JOptionPane.showMessageDialog(Main.getInstance(),
								"Current round is not complete. Please complete all matches before continuing.");
						return;
					}

					JDialog d = new CutPlayersDialog();
					d.setVisible(true);
				}
			});
		}
		return cutPlayers;
	}

	public JMenu getExportMenu() {
		if (exportMenu == null) {
			exportMenu = new JMenu("Export");

			JMenuItem exportPlayerList = new JMenuItem("Player List");
			exportPlayerList.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent arg0) {

					XWingExportController.playerList(CryodexController
							.getActiveTournament().getPlayers());
				}
			});

			JMenuItem exportMatches = new JMenuItem("Export Matches");
			exportMatches.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent arg0) {

					XWingTournament tournament = (XWingTournament) CryodexController
							.getActiveTournament();

					XWingRound round = tournament.getLatestRound();

					int roundNumber = round.isSingleElimination() ? 0
							: tournament.getRoundNumber(round);

					XWingExportController.exportMatches(tournament,
							round.getMatches(), roundNumber);
				}
			});

			JMenuItem exportMatchSlips = new JMenuItem("Export Match Slips");
			exportMatchSlips.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent arg0) {
					XWingTournament tournament = (XWingTournament) CryodexController
							.getActiveTournament();
					XWingRound round = tournament.getLatestRound();

					int roundNumber = round.isSingleElimination() ? 0
							: tournament.getRoundNumber(round);

					XWingExportController.exportTournamentSlips(tournament,
							round.getMatches(), roundNumber);
				}
			});

			JMenuItem exportMatchSlipsWithStats = new JMenuItem(
					"Export Match Slips With Stats");
			exportMatchSlipsWithStats.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent arg0) {
					XWingTournament tournament = (XWingTournament) CryodexController
							.getActiveTournament();
					XWingRound round = tournament.getLatestRound();

					int roundNumber = round.isSingleElimination() ? 0
							: tournament.getRoundNumber(round);

					XWingExportController.exportTournamentSlipsWithStats(
							tournament, round.getMatches(), roundNumber);
				}
			});

			JMenuItem exportRankings = new JMenuItem("Export Rankings");
			exportRankings.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent arg0) {
					XWingExportController
							.exportRankings((XWingTournament) CryodexController
									.getActiveTournament());
				}
			});

			JMenuItem exportTournamentReport = new JMenuItem(
					"Export Tournament Report");
			exportTournamentReport.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent arg0) {
					XWingExportController
							.exportTournamentReport((XWingTournament) CryodexController
									.getActiveTournament());
				}
			});

			JMenuItem saveJSON = new JMenuItem("X-Wing List Jugger Data");
			saveJSON.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent arg0) {
					XWingJSONBuilder
							.buildTournament((XWingTournament) CryodexController
									.getActiveTournament());
				}
			});
			
			JMenuItem cacReport = new JMenuItem(
					"Campaign Against Cancer Report");
			cacReport.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent arg0) {
					XWingExportController
							.cacReport();
				}
			});

			exportMenu.add(exportPlayerList);
			exportMenu.add(exportMatches);
			exportMenu.add(exportMatchSlips);
			exportMenu.add(exportMatchSlipsWithStats);
			exportMenu.add(exportRankings);
			exportMenu.add(exportTournamentReport);
			exportMenu.add(saveJSON);
			exportMenu.add(cacReport);
		}
		return exportMenu;
	}

	@Override
	public void resetMenuBar() {

		boolean isXWingTournament = CryodexController.getActiveTournament() != null
				&& CryodexController.getActiveTournament() instanceof XWingTournament;

		showKillPoints.setSelected(XWingModule.getInstance().getOptions()
				.isShowKillPoints());
		onlyEnterPoints.setSelected(XWingModule.getInstance().getOptions()
				.isEnterOnlyPoints());

		deleteTournament.setEnabled(isXWingTournament);
		getTournamentMenu().setEnabled(isXWingTournament);
		getRoundMenu().setEnabled(isXWingTournament);
		getExportMenu().setEnabled(isXWingTournament);

		if (isXWingTournament) {
			boolean isSingleElimination = ((XWingTournament) CryodexController
					.getActiveTournament()).getLatestRound()
					.isSingleElimination();
			getCutPlayers().setEnabled(!isSingleElimination);
		}
	}

	private abstract class PlayerSelectionDialog<K extends Comparable<K>>
			extends JDialog {

		private static final long serialVersionUID = 1945413167979638452L;

		private final JComboBox<K> userCombo;

		@SuppressWarnings("unchecked")
		public PlayerSelectionDialog(List<K> players) {
			super(Main.getInstance(), "Select Player", true);

			Collections.sort(players);

			JPanel mainPanel = new JPanel(new BorderLayout());

			userCombo = new JComboBox<K>();
			for (K k : players) {
				userCombo.addItem(k);
			}
			ComponentUtils.forceSize(userCombo, 20, 25);

			JButton ok = new JButton("Ok");
			ok.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent arg0) {
					K p = (K) userCombo.getSelectedItem();

					playerSelected(p);

					PlayerSelectionDialog.this.setVisible(false);
				}
			});

			JButton cancel = new JButton("Cancel");
			cancel.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					PlayerSelectionDialog.this.setVisible(false);
				}
			});

			mainPanel.add(userCombo, BorderLayout.CENTER);
			mainPanel.add(ComponentUtils.addToHorizontalBorderLayout(ok, null,
					cancel), BorderLayout.SOUTH);

			this.add(mainPanel);

			PlayerSelectionDialog.this
					.setLocationRelativeTo(Main.getInstance());
			PlayerSelectionDialog.this.pack();
			this.setMinimumSize(new Dimension(200, 0));
		}

		public abstract void playerSelected(K p);
	}

	private class CutPlayersDialog extends JDialog {

		private static final long serialVersionUID = 1945413167979638452L;

		private final JComboBox<Integer> cutCombo;

		public CutPlayersDialog() {
			super(Main.getInstance(), "Cut Players", true);

			JPanel mainPanel = new JPanel(new BorderLayout());

			int currentPlayers = CryodexController.getActiveTournament()
					.getPlayers().size();

			Integer[] options = { 2, 4, 8, 16, 32, 64 };

			while (options[options.length - 1] > currentPlayers) {
				Integer[] tempOptions = new Integer[options.length - 1];
				for (int i = 0; i < tempOptions.length; i++) {
					tempOptions[i] = options[i];
				}
				options = tempOptions;
			}

			cutCombo = new JComboBox<Integer>(options);
			ComponentUtils.forceSize(cutCombo, 10, 25);

			JButton add = new JButton("Make Cut");
			add.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent arg0) {

					Integer p = (Integer) cutCombo.getSelectedItem();

					CryodexController.getActiveTournament()
							.generateSingleEliminationMatches(p);

					CutPlayersDialog.this.setVisible(false);

					getCutPlayers().setEnabled(false);
				}
			});

			JButton cancel = new JButton("Cancel");
			cancel.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					CutPlayersDialog.this.setVisible(false);
				}
			});

			mainPanel.add(ComponentUtils.addToHorizontalBorderLayout(
					new JLabel("Cut to top: "), cutCombo, null),
					BorderLayout.CENTER);
			mainPanel.add(ComponentUtils.addToHorizontalBorderLayout(add, null,
					cancel), BorderLayout.SOUTH);

			this.add(mainPanel);

			CutPlayersDialog.this.setLocationRelativeTo(Main.getInstance());
			CutPlayersDialog.this.pack();
			this.setMinimumSize(new Dimension(200, 0));
		}
	}
}
