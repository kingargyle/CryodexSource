package cryodex.modules.imperialassault;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.SpringLayout;

import cryodex.CryodexController;
import cryodex.CryodexController.Modules;
import cryodex.Main;
import cryodex.Player;
import cryodex.modules.Tournament;
import cryodex.modules.imperialassault.IATournament.InitialSeedingEnum;
import cryodex.widget.ComponentUtils;
import cryodex.widget.DoubleList;
import cryodex.widget.SpringUtilities;

public class IATournamentCreationWizard extends JDialog {

	private static final long serialVersionUID = 1L;

	private final WizardOptions wizardOptions = new WizardOptions();

	private JPanel mainPanel = null;
	private JPanel contentPanel = null;
	private JPanel buttonPanel = null;

	private JButton previousButton = null;
	private JButton nextButton = null;
	private JButton finishButton = null;
	private JButton cancelButton = null;

	private final List<Page> pages = new ArrayList<Page>();

	public IATournamentCreationWizard() {
		super(Main.getInstance(), "Tournament Wizard", true);

		this.add(getMainPanel());
		setCurrentPage(new MainPage());
		IATournamentCreationWizard.this.pack();
		this.setMinimumSize(new Dimension(450, 500));
	}

	private void setCurrentPage(Page page) {
		pages.add(page);

		getContentPanel().removeAll();
		getContentPanel().add(page.getPanel(), BorderLayout.CENTER);
		getContentPanel().validate();
		getContentPanel().repaint();
	}

	private Page getCurrentPage() {
		return pages.isEmpty() ? null : pages.get(pages.size() - 1);
	}

	private Page getPreviousPage() {
		return pages.size() > 1 ? pages.get(pages.size() - 2) : null;
	}

	private void goToPrevious() {

		if (getPreviousPage() != null) {
			pages.remove(pages.get(pages.size() - 1));

			getContentPanel().removeAll();
			getContentPanel().add(pages.get(pages.size() - 1).getPanel(),
					BorderLayout.CENTER);
			getContentPanel().validate();
			getContentPanel().repaint();
		} else {
			IATournamentCreationWizard.this.setVisible(false);
		}
	}

	private JPanel getMainPanel() {
		if (mainPanel == null) {
			mainPanel = new JPanel(new BorderLayout());
			mainPanel.add(getContentPanel(), BorderLayout.CENTER);
			mainPanel.add(getButtonPanel(), BorderLayout.SOUTH);
		}

		return mainPanel;
	}

	private JPanel getContentPanel() {
		if (contentPanel == null) {
			contentPanel = new JPanel(new BorderLayout());
		}

		return contentPanel;
	}

	private JPanel getButtonPanel() {
		if (buttonPanel == null) {
			buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
			buttonPanel.add(getPreviousButton());
			buttonPanel.add(getNextButton());
			buttonPanel.add(getFinishButton());
			buttonPanel.add(getCancelButton());
		}

		return buttonPanel;
	}

	private void setButtonVisibility(Boolean previous, Boolean next,
			Boolean finish) {
		getPreviousButton().setVisible(previous == null ? false : previous);
		getPreviousButton().setEnabled(previous != null);

		getNextButton().setVisible(next == null ? false : next);
		getNextButton().setEnabled(next != null);

		getFinishButton().setVisible(finish == null ? false : finish);
		getFinishButton().setEnabled(finish != null);
	}

	private JButton getPreviousButton() {
		if (previousButton == null) {
			previousButton = new JButton("Previous");
			previousButton.setEnabled(false);
			previousButton.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					getCurrentPage().onPrevious();
				}
			});
		}

		return previousButton;
	}

	private JButton getNextButton() {
		if (nextButton == null) {
			nextButton = new JButton("Next");
			nextButton.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					getCurrentPage().onNext();
				}
			});
		}

		return nextButton;
	}

	private JButton getFinishButton() {
		if (finishButton == null) {
			finishButton = new JButton("Finish");
			finishButton.setVisible(false);
			finishButton.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					getCurrentPage().onFinish();
				}
			});
		}

		return finishButton;
	}

	private JButton getCancelButton() {
		if (cancelButton == null) {
			cancelButton = new JButton("Cancel");
			cancelButton.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent arg0) {
					IATournamentCreationWizard.this.setVisible(false);
				}
			});
		}

		return cancelButton;
	}

	private interface Page {
		public JPanel getPanel();

		public void onNext();

		public void onPrevious();

		public void onFinish();
	}

	private class MainPage implements Page {

		JTextField nameTextField;

		JCheckBox mergeCB;

		JCheckBox splitCB;
		JTextField numSubs;

		JPanel pagePanel;

		@Override
		public JPanel getPanel() {

			setButtonVisibility(null, true, null);

			if (pagePanel == null) {
				JPanel namePanel = new JPanel(new BorderLayout());

				JLabel nameHeader = new JLabel(
						"<HTML><H1>Name Event</H1></HTML>");

				nameTextField = new JTextField(10);

				namePanel.add(ComponentUtils.addToFlowLayout(nameHeader,
						FlowLayout.LEFT), BorderLayout.NORTH);
				namePanel.add(ComponentUtils.addToFlowLayout(nameTextField,
						FlowLayout.LEFT));

				JPanel creationOptionsPanel = new JPanel(new BorderLayout());

				JLabel additionalOptionsHeader = new JLabel(
						"<HTML><H1>Additional Options</H1></HTML>");

				creationOptionsPanel.add(ComponentUtils.addToFlowLayout(
						additionalOptionsHeader, FlowLayout.LEFT),
						BorderLayout.NORTH);

				JPanel splitEntryPanel = new JPanel(new BorderLayout());
				ComponentUtils.forceSize(splitEntryPanel, 210, 60);

				splitCB = new JCheckBox("Split into subtournaments");
				final JLabel splitLabel = new JLabel(
						"Number of sub tournaments:");
				numSubs = new JTextField(3);

				splitLabel.setVisible(false);
				numSubs.setVisible(false);

				splitCB.addActionListener(new ActionListener() {

					@Override
					public void actionPerformed(ActionEvent arg0) {
						splitLabel.setVisible(splitCB.isSelected());
						numSubs.setVisible(splitCB.isSelected());
					}
				});

				splitEntryPanel.add(splitCB, BorderLayout.NORTH);
				splitEntryPanel.add(ComponentUtils.addToFlowLayout(
						ComponentUtils.addToHorizontalBorderLayout(splitLabel,
								numSubs, null), FlowLayout.LEFT),
						BorderLayout.CENTER);

				creationOptionsPanel.add(ComponentUtils.addToFlowLayout(
						splitEntryPanel, FlowLayout.LEFT), BorderLayout.SOUTH);

				mergeCB = new JCheckBox("Merge multiple tournaments into one");

				creationOptionsPanel.add(ComponentUtils.addToFlowLayout(
						mergeCB, FlowLayout.LEFT), BorderLayout.CENTER);

				pagePanel = ComponentUtils.addToFlowLayout(ComponentUtils
						.addToVerticalBorderLayout(namePanel,
								creationOptionsPanel, null), FlowLayout.CENTER);
			}
			return pagePanel;
		}

		@Override
		public void onNext() {

			wizardOptions.setName(nameTextField.getText());

			if (splitCB.isSelected()) {
				int subs = Integer.parseInt(numSubs.getText());
				wizardOptions.setSplit(subs);
			}

			if (mergeCB.isSelected()) {
				setCurrentPage(new MergeTournamentSelectionPage());
			} else {
				setCurrentPage(new PlayerSelectionPage());
			}
		}

		@Override
		public void onPrevious() {
			// Do nothing
		}

		@Override
		public void onFinish() {
			// Do nothing
		}
	}

	private class PlayerSelectionPage implements Page {

		private DoubleList<Player> playerList;
		private JCheckBox removeCurrentlyPlaying;

		private JPanel pagePanel;

		@Override
		public JPanel getPanel() {

			setButtonVisibility(true, true, null);

			if (pagePanel == null) {

				pagePanel = new JPanel(new BorderLayout());

				JLabel header = new JLabel(
						"<HTML><H1>Select Players</H1></HTML>");

				pagePanel.add(ComponentUtils.addToFlowLayout(header,
						FlowLayout.CENTER), BorderLayout.NORTH);

				playerList = new DoubleList<Player>(
						CryodexController.getPlayers(), null,
						"Available Players", "Event Players");

				pagePanel.add(playerList, BorderLayout.CENTER);

				removeCurrentlyPlaying = new JCheckBox(
						"Remove players currently in an event");
				removeCurrentlyPlaying.addActionListener(new ActionListener() {

					@Override
					public void actionPerformed(ActionEvent arg0) {
						if (removeCurrentlyPlaying.isSelected()) {
							List<Tournament> tournaments = CryodexController
									.getAllTournaments();
							List<Player> players1 = playerList.getList1Values();
							List<Player> players2 = playerList.getList2Values();
							for (Tournament t : tournaments) {
								players1.removeAll(t.getPlayers());
								players2.removeAll(t.getPlayers());
							}
							playerList.setValues(players1, players2);
						} else {
							List<Tournament> tournaments = CryodexController
									.getAllTournaments();
							List<Player> players = new ArrayList<Player>();

							for (Tournament t : tournaments) {
								for (Player p : t.getPlayers()) {
									if (players.contains(p) == false) {
										players.add(p);
									}
								}
							}

							List<Player> players1 = playerList.getList1Values();
							players1.addAll(players);
							List<Player> players2 = playerList.getList2Values();
							playerList.setValues(players1, players2);
						}
					}
				});

				pagePanel.add(ComponentUtils.addToFlowLayout(
						removeCurrentlyPlaying, FlowLayout.CENTER),
						BorderLayout.SOUTH);

			}

			return pagePanel;
		}

		@Override
		public void onNext() {
			List<IAPlayer> iaPlayerList = new ArrayList<>();
			for (Player p : playerList.getList2Values()) {
				iaPlayerList.add((IAPlayer) p.getModuleInfoByModule(Modules.IA
						.getModule()));
			}
			wizardOptions.setPlayerList(iaPlayerList);
			setCurrentPage(new AdditionalOptionsPage());
		}

		@Override
		public void onPrevious() {
			goToPrevious();
		}

		@Override
		public void onFinish() {
			// Do Nothing
		}

	}

	private class AdditionalOptionsPage implements Page {

		private JRadioButton randomRB;
		private JRadioButton byGroupRB;
		private JRadioButton byRankingRB;
		private JCheckBox singleElimination;

		private JRadioButton splitRandomRB;
		private JRadioButton splitByGroupRB;
		private JRadioButton splitByRanking;

		private JPanel pagePanel;

		@Override
		public JPanel getPanel() {

			setButtonVisibility(true, null, true);

			if (pagePanel == null) {

				JPanel initialPairingPanel = new JPanel(new BorderLayout());

				JLabel header = new JLabel(
						"<HTML><H3>First Round Pairing</H3></HTML>");

				initialPairingPanel
						.add(ComponentUtils.addToFlowLayout(header,
								FlowLayout.LEFT), BorderLayout.NORTH);

				JPanel tournamentTypesPanel = new JPanel(new SpringLayout());

				ButtonGroup bg = new ButtonGroup();

				randomRB = new JRadioButton("Random");
				byGroupRB = new JRadioButton("Seperate By Group Name");
				byRankingRB = new JRadioButton("By Ranking");
				singleElimination = new JCheckBox(
						"<HTML>Start event as single elimination<br>(only for 2/4/8/16/32 players)</HTML>");

				bg.add(randomRB);
				bg.add(byGroupRB);
				bg.add(byRankingRB);

				randomRB.setSelected(true);

				tournamentTypesPanel.add(randomRB);
				tournamentTypesPanel.add(byGroupRB);
				if (wizardOptions.getSelectedTournaments() != null
						&& wizardOptions.getSelectedTournaments().isEmpty() == false) {
					tournamentTypesPanel.add(byRankingRB);
				}
				tournamentTypesPanel.add(singleElimination);

				SpringUtilities
						.makeCompactGrid(tournamentTypesPanel,
								tournamentTypesPanel.getComponentCount(), 1, 0,
								0, 0, 0);

				initialPairingPanel.add(ComponentUtils.addToFlowLayout(
						tournamentTypesPanel, FlowLayout.LEFT),
						BorderLayout.CENTER);

				JPanel splitOptionsPanel = new JPanel(new BorderLayout());

				JLabel splitOptionsHeader = new JLabel(
						"<HTML><H3>How To Split Tournament</H3></HTML>");

				splitOptionsPanel.add(ComponentUtils.addToFlowLayout(
						splitOptionsHeader, FlowLayout.LEFT),
						BorderLayout.NORTH);

				JPanel splitOptionsSubPanel = new JPanel(new SpringLayout());

				ButtonGroup splitOptionsBG = new ButtonGroup();

				splitRandomRB = new JRadioButton("Random");
				splitByGroupRB = new JRadioButton("Seperate By Group Name");
				splitByRanking = new JRadioButton("Split by ranking");

				splitOptionsBG.add(splitRandomRB);
				splitOptionsBG.add(splitByGroupRB);
				splitOptionsBG.add(splitByRanking);

				splitOptionsSubPanel.add(splitRandomRB);
				splitOptionsSubPanel.add(splitByGroupRB);
				if (wizardOptions.getSelectedTournaments() != null
						&& wizardOptions.getSelectedTournaments().isEmpty() == false) {
					splitOptionsSubPanel.add(splitByRanking);
				}

				SpringUtilities
						.makeCompactGrid(splitOptionsSubPanel,
								splitOptionsSubPanel.getComponentCount(), 1, 0,
								0, 0, 0);

				splitOptionsPanel.add(ComponentUtils.addToFlowLayout(
						splitOptionsSubPanel, FlowLayout.LEFT),
						BorderLayout.CENTER);

				pagePanel = new JPanel(new FlowLayout());

				pagePanel
						.add(ComponentUtils.addToVerticalBorderLayout(
								initialPairingPanel, null, wizardOptions
										.getSplit() > 1 ? splitOptionsPanel
										: null));
			}

			return pagePanel;
		}

		@Override
		public void onNext() {
			// Do nothing
		}

		@Override
		public void onPrevious() {
			goToPrevious();
		}

		@Override
		public void onFinish() {

			if (randomRB.isSelected()) {
				wizardOptions.setInitialSeedingEnum(InitialSeedingEnum.RANDOM);
			} else if (byGroupRB.isSelected()) {
				wizardOptions
						.setInitialSeedingEnum(InitialSeedingEnum.BY_GROUP);
			} else if (byRankingRB.isSelected()) {
				wizardOptions
						.setInitialSeedingEnum(InitialSeedingEnum.IN_ORDER);
			}

			if (singleElimination.isSelected()) {
				wizardOptions.setSingleElimination(true);
			}

			boolean fixByes = true;

			if (wizardOptions.getSplit() > 1) {
				Integer splitNum = wizardOptions.getSplit();

				List<WizardOptions> wizardOptionsList = new ArrayList<WizardOptions>();

				for (int i = 1; i <= splitNum; i++) {
					WizardOptions newWizardOption = new WizardOptions(
							wizardOptions);

					wizardOptionsList.add(newWizardOption);

					newWizardOption.setName(wizardOptions.getName() + " " + i);
					newWizardOption.setPlayerList(new ArrayList<IAPlayer>());
				}

				if (splitByGroupRB.isSelected()) {
					Map<String, List<IAPlayer>> playerMap = new HashMap<String, List<IAPlayer>>();

					// Add players to map
					for (IAPlayer p : wizardOptions.getPlayerList()) {
						List<IAPlayer> playerList = playerMap.get(p.getPlayer()
								.getGroupName());

						if (playerList == null) {
							playerList = new ArrayList<>();
							String groupName = p.getPlayer().getGroupName() == null ? ""
									: p.getPlayer().getGroupName();
							playerMap.put(groupName, playerList);
						}

						playerList.add(p);
					}

					int j = 0;
					for (String groupValue : playerMap.keySet()) {

						List<IAPlayer> playerList = playerMap.get(groupValue);

						while (playerList.isEmpty() == false) {

							wizardOptionsList.get(j).getPlayerList()
									.add(playerList.get(0));
							j = j == splitNum - 1 ? 0 : j + 1;
							playerList.remove(0);
						}
					}

					//
					int first = 0;
					int last = wizardOptionsList.size() - 1;
					if (fixByes) {
						while (first < last) {

							if (wizardOptionsList.get(last).getPlayerList()
									.size() % 2 == 0) {
								last--;
							} else {
								if (wizardOptionsList.get(first)
										.getPlayerList().size() % 2 == 1
										&& wizardOptionsList.get(last)
												.getPlayerList().size() % 2 == 1) {
									IAPlayer p = wizardOptionsList
											.get(first)
											.getPlayerList()
											.get(wizardOptionsList.get(first)
													.getPlayerList().size() - 1);

									wizardOptionsList.get(first)
											.getPlayerList().remove(p);

									wizardOptionsList.get(last).getPlayerList()
											.add(p);
									first++;
								}
							}
						}
					}
				} else if (splitByRanking.isSelected()) {
					List<IAPlayer> tempPlayers = rankMergedPlayers(wizardOptions
							.getPlayerList());

					int ppt = tempPlayers.size() / splitNum;
					int overage = tempPlayers.size() % splitNum;
					for (int j = 0; j < splitNum; j++) {
						int count = j >= splitNum - overage ? ppt + 1 : ppt;
						wizardOptionsList.get(j).getPlayerList()
								.addAll(tempPlayers.subList(0, count));
						tempPlayers = tempPlayers.subList(count,
								tempPlayers.size());
					}

					for (int i = 0; i < wizardOptionsList.size(); i++) {
						// if (wizardOptionsList.get(i).getPlayerList().size() %
						// 2 == 0) {
						// continue;
						// }

						while (i + 1 < wizardOptionsList.size()
								&& (wizardOptionsList.get(i).getPlayerList()
										.size() % 2 == 1 || wizardOptionsList
										.get(i).getPlayerList().size() > wizardOptionsList
										.get(i + 1).getPlayerList().size())) {
							IAPlayer t1 = wizardOptionsList
									.get(i)
									.getPlayerList()
									.get(wizardOptionsList.get(i)
											.getPlayerList().size() - 1);
							wizardOptionsList.get(i).getPlayerList().remove(t1);
							List<IAPlayer> temp = new ArrayList<IAPlayer>();
							temp.addAll(wizardOptionsList.get(i + 1)
									.getPlayerList());
							wizardOptionsList.get(i + 1).getPlayerList()
									.clear();
							wizardOptionsList.get(i + 1).getPlayerList()
									.add(t1);
							wizardOptionsList.get(i + 1).getPlayerList()
									.addAll(temp);
						}

					}
				} else {
					List<IAPlayer> playerList = wizardOptions.getPlayerList();
					Collections.shuffle(playerList);
					int j = 0;
					while (playerList.isEmpty() == false) {

						wizardOptionsList.get(j).getPlayerList()
								.add(playerList.get(0));
						j = j == splitNum - 1 ? 0 : j + 1;
						playerList.remove(0);
					}

					//
					int first = 0;
					int last = wizardOptionsList.size() - 1;
					if (fixByes) {
						while (first < last) {

							if (wizardOptionsList.get(last).getPlayerList()
									.size() % 2 == 0) {
								last--;
							} else {
								if (wizardOptionsList.get(first)
										.getPlayerList().size() % 2 == 1
										&& wizardOptionsList.get(last)
												.getPlayerList().size() % 2 == 1) {
									IAPlayer p = wizardOptionsList
											.get(first)
											.getPlayerList()
											.get(wizardOptionsList.get(first)
													.getPlayerList().size() - 1);

									wizardOptionsList.get(first)
											.getPlayerList().remove(p);

									wizardOptionsList.get(last).getPlayerList()
											.add(p);
									first++;
								}
							}
						}
					}
				}

				IATournamentCreationWizard.this.setVisible(false);

				for (WizardOptions wo : wizardOptionsList) {
					if (wo.getInitialSeedingEnum() == InitialSeedingEnum.IN_ORDER) {
						List<IAPlayer> tempList = rankMergedPlayers(wo
								.getPlayerList());
						wo.setPlayerList(tempList);
					}
					IAModule.makeTournament(wo);
				}
			} else {
				IATournamentCreationWizard.this.setVisible(false);
				IAModule.makeTournament(wizardOptions);
			}
		}
	}

	private class MergeTournamentSelectionPage implements Page {

		private JPanel pagePanel = null;
		private final Map<IATournament, JCheckBox> checkBoxMap = new HashMap<IATournament, JCheckBox>();
		private JRadioButton all;
		private JRadioButton manual;
		private JTextField manualInput;

		@Override
		public JPanel getPanel() {
			setButtonVisibility(true, true, false);

			if (pagePanel == null) {
				pagePanel = new JPanel(new BorderLayout());

				JLabel header = new JLabel(
						"<HTML><H3>Select Tournaments</H3></HTML>");

				JPanel listPanel = new JPanel(new SpringLayout());

				for (Tournament t : CryodexController.getAllTournaments()) {
					JCheckBox cb = new JCheckBox(t.getName());
					if (t instanceof IATournament) {
						checkBoxMap.put((IATournament) t, cb);
					}

					listPanel.add(cb);
				}

				SpringUtilities.makeCompactGrid(listPanel,
						listPanel.getComponentCount(), 1, 0, 0, 0, 0);

				JLabel playersFromLabel = new JLabel(
						"<HTML><H3>How many players From Each Event?</H3></HTML>");
				ButtonGroup pf = new ButtonGroup();

				all = new JRadioButton("All Players");
				manual = new JRadioButton("Let me pick:");
				manualInput = new JTextField(3);
				manualInput.setEnabled(false);

				ActionListener playersFromListener = new ActionListener() {

					@Override
					public void actionPerformed(ActionEvent arg0) {
						manualInput.setEnabled(manual.isSelected());
					}
				};

				all.addActionListener(playersFromListener);
				manual.addActionListener(playersFromListener);

				pf.add(all);
				pf.add(manual);
				all.setSelected(true);

				JPanel manualPanel = ComponentUtils
						.addToHorizontalBorderLayout(manual, ComponentUtils
								.addToFlowLayout(manualInput, FlowLayout.LEFT),
								null);

				JPanel howManyPlayersPanel = ComponentUtils
						.addToVerticalBorderLayout(playersFromLabel, all,
								manualPanel);

				pagePanel
						.add(ComponentUtils.addToFlowLayout(header,
								FlowLayout.LEFT), BorderLayout.NORTH);
				pagePanel.add(ComponentUtils.addToFlowLayout(listPanel,
						FlowLayout.LEFT), BorderLayout.CENTER);
				pagePanel.add(ComponentUtils.addToFlowLayout(
						howManyPlayersPanel, FlowLayout.CENTER),
						BorderLayout.SOUTH);
			}
			return ComponentUtils.addToFlowLayout(pagePanel, FlowLayout.CENTER);
		}

		@Override
		public void onNext() {
			wizardOptions.setMerge(true);
			List<IATournament> tournamentList = new ArrayList<IATournament>();
			Set<IAPlayer> playerList = new TreeSet<IAPlayer>();
			Integer playerCount = null;

			if (manual.isSelected()) {
				playerCount = Integer.parseInt(manualInput.getText());
			}

			for (IATournament t : checkBoxMap.keySet()) {
				if (checkBoxMap.get(t).isSelected()) {

					tournamentList.add(t);
					List<IAPlayer> thisTournamentPlayers = new ArrayList<IAPlayer>();
					thisTournamentPlayers.addAll(t.getIAPlayers());

					if (playerCount == null
							|| thisTournamentPlayers.size() <= playerCount) {
						playerList.addAll(thisTournamentPlayers);
					} else {
						Collections
								.sort(thisTournamentPlayers, new IAComparator(
										t, IAComparator.rankingCompare));
						playerList.addAll(thisTournamentPlayers.subList(0,
								playerCount));
					}
				}
			}

			List<IAPlayer> addingList = new ArrayList<IAPlayer>();
			addingList.addAll(playerList);
			wizardOptions.setPlayerList(addingList);
			wizardOptions.setSelectedTournaments(tournamentList);

			setCurrentPage(new AdditionalOptionsPage());

		}

		@Override
		public void onPrevious() {
			goToPrevious();
		}

		@Override
		public void onFinish() {
			// Do nothing
		}
	}

	public static class WizardOptions {

		private String name;
		private InitialSeedingEnum initialSeedingEnum;
		private List<IAPlayer> playerList;
		private int split = 1;
		private boolean isMerge = false;
		private List<IATournament> selectedTournaments;
		private boolean isSingleElimination = false;

		public WizardOptions() {

		}

		public WizardOptions(WizardOptions wizardOptions) {
			this.name = wizardOptions.getName();
			this.initialSeedingEnum = wizardOptions.getInitialSeedingEnum();
			this.isSingleElimination = wizardOptions.isSingleElimination();
		}

		public InitialSeedingEnum getInitialSeedingEnum() {
			return initialSeedingEnum;
		}

		public void setInitialSeedingEnum(InitialSeedingEnum initialSeedingEnum) {
			this.initialSeedingEnum = initialSeedingEnum;
		}

		public List<IAPlayer> getPlayerList() {
			return playerList;
		}

		public void setPlayerList(List<IAPlayer> playerList) {
			this.playerList = playerList;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public int getSplit() {
			return split;
		}

		public void setSplit(int split) {
			this.split = split;
		}

		public boolean isMerge() {
			return isMerge;
		}

		public void setMerge(boolean isMerge) {
			this.isMerge = isMerge;
		}

		public List<IATournament> getSelectedTournaments() {
			return selectedTournaments;
		}

		public void setSelectedTournaments(
				List<IATournament> selectedTournaments) {
			this.selectedTournaments = selectedTournaments;
		}

		public boolean isSingleElimination() {
			return isSingleElimination;
		}

		public void setSingleElimination(boolean isSingleElimination) {
			this.isSingleElimination = isSingleElimination;
		}
	}

	public List<IAPlayer> rankMergedPlayers(List<IAPlayer> playerList) {
		IATournament mergeTournament = new IATournament("",
				wizardOptions.getPlayerList(), null, false);
		for (IATournament t : wizardOptions.getSelectedTournaments()) {
			mergeTournament.getAllRounds().addAll(t.getAllRounds());
		}

		List<IAPlayer> tempPlayers = new ArrayList<IAPlayer>();
		tempPlayers.addAll(playerList);
		Collections.sort(tempPlayers, new IAComparator(mergeTournament,
				IAComparator.rankingCompare));
		return tempPlayers;
	}
}
