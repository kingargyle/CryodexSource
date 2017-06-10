package cryodex.modules.runewars;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import cryodex.CryodexController;
import cryodex.Player;
import cryodex.widget.ComponentUtils;

public class RunewarsRoundPanel extends JPanel {

	private static final long serialVersionUID = 1L;

	private final List<RunewarsMatch> matches;
	private final List<GamePanel> gamePanels = new ArrayList<GamePanel>();
	private JPanel quickEntryPanel;
	private JPanel quickEntrySubPanel;
	private JTextField roundNumber;
	private JComboBox<RunewarsPlayer> playerCombo;
	private final JScrollPane scroll;

	private final RunewarsTournament tournament;

	public RunewarsRoundPanel(RunewarsTournament t, List<RunewarsMatch> matches) {

		super(new BorderLayout());

		this.tournament = t;
		this.matches = matches;
		this.setBorder(BorderFactory.createLineBorder(Color.black));

		int counter = 1;
		for (RunewarsMatch match : matches) {
			GamePanel gpanel = new GamePanel(counter, match);
			gamePanels.add(gpanel);
			counter++;
		}

		scroll = new JScrollPane(ComponentUtils.addToFlowLayout(buildPanel(),
				FlowLayout.CENTER));
		scroll.setBorder(BorderFactory.createEmptyBorder());

		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				scroll.getVerticalScrollBar().setValue(0);
				scroll.getVerticalScrollBar().setUnitIncrement(15);
			}
		});

		this.add(getQuickEntryPanel(), BorderLayout.NORTH);
		this.add(scroll, BorderLayout.CENTER);
	}

	public JPanel getQuickEntryPanel() {
		if (quickEntryPanel == null) {
			quickEntryPanel = new JPanel(new BorderLayout());
			quickEntryPanel.setVisible(CryodexController.getOptions()
					.isShowQuickFind());
			ComponentUtils.forceSize(quickEntryPanel, 405, 135);

			quickEntrySubPanel = new JPanel(new BorderLayout());
			ComponentUtils.forceSize(quickEntrySubPanel, 400, 130);

			roundNumber = new JTextField(5);

			roundNumber.getDocument().addDocumentListener(
					new DocumentListener() {
						@Override
						public void changedUpdate(DocumentEvent e) {
							update();
						}

						@Override
						public void removeUpdate(DocumentEvent e) {
							update();
						}

						@Override
						public void insertUpdate(DocumentEvent e) {
							update();
						}
					});

			List<RunewarsPlayer> playerList = new ArrayList<RunewarsPlayer>();

			playerList.add(new RunewarsPlayer(new Player()));
			playerList.addAll(tournament.getRunewarsPlayers());

			Collections.sort(playerList);

			playerCombo = new JComboBox<RunewarsPlayer>(
					playerList.toArray(new RunewarsPlayer[playerList.size()]));

			playerCombo.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent arg0) {
					update();
				}
			});

			quickEntryPanel.add(ComponentUtils.addToFlowLayout(ComponentUtils
					.addToHorizontalBorderLayout(new JLabel(
							"Enter table number"), roundNumber, ComponentUtils
							.addToHorizontalBorderLayout(new JLabel(
									"or choose a player"), playerCombo, null)),
					FlowLayout.CENTER), BorderLayout.NORTH);

			quickEntryPanel.add(quickEntrySubPanel);
		}

		return quickEntryPanel;
	}

	public void update() {

		scroll.getViewport().removeAll();
		scroll.getViewport()
				.add(ComponentUtils.addToFlowLayout(buildPanel(),
						FlowLayout.CENTER));
		ComponentUtils.repaint(RunewarsRoundPanel.this);

		Integer i = null;
		try {
			i = Integer.parseInt(roundNumber.getText());
		} catch (NumberFormatException e) {

		}

		RunewarsPlayer player = playerCombo.getSelectedIndex() == 0 ? null
				: (RunewarsPlayer) playerCombo.getSelectedItem();

		if (player != null) {
			roundNumber.setEnabled(false);
		} else if (i != null) {
			playerCombo.setEnabled(false);
		} else {
			roundNumber.setEnabled(true);
			playerCombo.setEnabled(true);
		}

		GamePanel gamePanel = null;
		if (i != null) {
			if (i > gamePanels.size()) {
				return;
			}

			gamePanel = gamePanels.get(i - 1);
		} else if (player != null) {
			for (GamePanel g : gamePanels) {
				if (g.getMatch().getPlayer1() == player) {
					gamePanel = g;
					break;
				} else if (g.getMatch().getPlayer2() != null
						&& g.getMatch().getPlayer2() == player) {
					gamePanel = g;
					break;
				}
			}
		}

		if (gamePanel == null) {
			return;
		}

		quickEntrySubPanel.add(gamePanel.getPlayerTitle(), BorderLayout.CENTER);

		quickEntrySubPanel.removeAll();

		JPanel panel = new JPanel(new GridBagLayout());

		GridBagConstraints gbc = new GridBagConstraints();

		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 2;
		gbc.anchor = GridBagConstraints.EAST;
		panel.add(gamePanel.getPlayerTitle(), gbc);

		gbc.gridx = 2;
		gbc.fill = GridBagConstraints.BOTH;
		panel.add(gamePanel.getResultCombo(), gbc);

		if (gamePanel.getMatch().getPlayer2() != null) {
			gbc.gridy++;
			gbc.gridwidth = 1;
			gbc.fill = GridBagConstraints.NONE;

			gbc.gridx = 1;
			panel.add(gamePanel.getPlayer1ScoreLabel(), gbc);

			gbc.gridx = 2;
			panel.add(gamePanel.getPlayer1ScoreField(), gbc);

			gbc.gridy++;
			gbc.gridwidth = 1;

			gbc.gridx = 1;
			panel.add(gamePanel.getPlayer2ScoreLabel(), gbc);

			gbc.gridx = 2;
			panel.add(gamePanel.getPlayer2ScoreField(), gbc);
		}

		quickEntrySubPanel.add(panel, BorderLayout.CENTER);

		ComponentUtils.repaint(quickEntrySubPanel);
		ComponentUtils.repaint(quickEntryPanel);
	}

	public JPanel buildPanel() {

		JPanel panel = new JPanel(new GridBagLayout());

		GridBagConstraints gbc = new GridBagConstraints();

		gbc.gridx = 0;
		gbc.gridy = -1;

		for (GamePanel gp : gamePanels) {
			gbc.gridy++;
			gbc.gridx = 0;
			gbc.gridwidth = 2;
			gbc.anchor = GridBagConstraints.EAST;
			panel.add(gp.getPlayerTitle(), gbc);

			gbc.gridx = 2;
			gbc.fill = GridBagConstraints.BOTH;
			panel.add(gp.getResultCombo(), gbc);

			if (gp.getMatch().getPlayer2() != null) {
				gbc.gridy++;
				gbc.gridwidth = 1;
				gbc.fill = GridBagConstraints.NONE;

				gbc.gridx = 1;
				panel.add(gp.getPlayer1ScoreLabel(), gbc);

				gbc.gridx = 2;
				panel.add(gp.getPlayer1ScoreField(), gbc);

				gbc.gridy++;
				gbc.gridwidth = 1;

				gbc.gridx = 1;
				panel.add(gp.getPlayer2ScoreLabel(), gbc);

				gbc.gridx = 2;
				panel.add(gp.getPlayer2ScoreField(), gbc);
			}
		}

		return panel;
	}

	public List<RunewarsMatch> getMatches() {
		return matches;
	}

	public void resetGamePanels(boolean isTextOnly) {
		for (GamePanel gp : gamePanels) {
			gp.reset(isTextOnly);
		}
		getQuickEntryPanel().setVisible(
				CryodexController.getOptions().isShowQuickFind());
		ComponentUtils.repaint(this);
	}

	private class GamePanel {
		private final RunewarsMatch match;
		private JLabel playersTitle;
		private JComboBox<String> resultsCombo;
		private JTextField player1Score;
		private JTextField player2Score;
		private JLabel player1ScoreLabel;
		private JLabel player2ScoreLabel;
		private boolean isLoading = false;
		private int tableNumber = -1;

		public GamePanel(int tableNumber, RunewarsMatch match) {

			this.tableNumber = tableNumber;

			isLoading = true;

			this.match = match;

			reset(false);

			init();
		}

		private void init() {
			if (match.isMatchComplete()) {
				if (match.isBye()) {
					getResultCombo().setSelectedIndex(1);
				} else {
					if (match.getWinner() == match.getPlayer1()) {
						if(match.isConcede()){
							getResultCombo().setSelectedIndex(4);
						} else {
							getResultCombo().setSelectedIndex(1);	
						}
					} else if (match.getWinner() == match.getPlayer2()) {
						if(match.isConcede()){
							getResultCombo().setSelectedIndex(3);
						} else {
							getResultCombo().setSelectedIndex(2);	
						}
					}
				}
			}
			if (match.getPlayer2() != null) {
				if (match.getPlayer1Score() != null) {
					getPlayer1ScoreField().setText(
							String.valueOf(match.getPlayer1Score()));
				}
				if (match.getPlayer2Score() != null) {
					getPlayer2ScoreField().setText(
							String.valueOf(match.getPlayer2Score()));
				}
			}
			isLoading = false;
		}

		private RunewarsMatch getMatch() {
			return match;
		}

		private JLabel getPlayerTitle() {
			if (playersTitle == null) {

				playersTitle = new JLabel("");
				playersTitle.setFont(new Font(playersTitle.getFont().getName(),
						playersTitle.getFont().getStyle(), 20));
				playersTitle.setHorizontalAlignment(SwingConstants.RIGHT);
			}
			return playersTitle;
		}

		private JLabel getPlayer1ScoreLabel() {
			if (player1ScoreLabel == null) {
				player1ScoreLabel = new JLabel();
			}
			return player1ScoreLabel;
		}

		private JLabel getPlayer2ScoreLabel() {
			if (player2ScoreLabel == null) {
				player2ScoreLabel = new JLabel();
			}
			return player2ScoreLabel;
		}

		private String[] getComboValues() {

			if (match.getPlayer2() == null) {
				String[] values = { "Select a result", "BYE" };
				return values;
			} else {
				String generic = "Select a result";
				String[] values = { generic,
						"WIN - " + match.getPlayer1().getName(),
						"WIN - " + match.getPlayer2().getName(),
						match.getPlayer1().getName() + " Conceded", 
						match.getPlayer2().getName() + " Conceded"};
				return values;
			}
		}

		private JComboBox<String> getResultCombo() {
			if (resultsCombo == null) {

				resultsCombo = new JComboBox<String>(getComboValues());

				resultsCombo.setRenderer(new DefaultListCellRenderer() {
					private static final long serialVersionUID = 1L;

					@Override
					public void paint(Graphics g) {
						setForeground(Color.BLACK);
						super.paint(g);
					}
				});

				resultsCombo.addActionListener(new ActionListener() {

					@Override
					public void actionPerformed(ActionEvent arg0) {
						comboChange();
					}
				});
			}
			return resultsCombo;
		}

		private void comboChange() {

			if (isLoading) {
				return;
			}

			switch (resultsCombo.getSelectedIndex()) {
			case 0:
				match.setWinner(null);
				match.setBye(false);
				match.setConcede(false);
				break;
			case 1:
				if (match.getPlayer2() == null) {
					match.setBye(true);
				} else {
					match.setWinner(match.getPlayer1());
				}
				match.setConcede(false);
				break;
			case 2:
				match.setWinner(match.getPlayer2());
				match.setConcede(false);
				break;
			case 3:
				match.setWinner(match.getPlayer2());
				match.setConcede(true);
				break;
			case 4:
				match.setWinner(match.getPlayer1());
				match.setConcede(true);
				break;
			default:
				break;
			}
			tournament.getTournamentGUI().getRankingTable().resetPlayers();

		}

		public JTextField getPlayer1ScoreField() {
			if (player1Score == null) {
				player1Score = new JTextField();
				player1Score.addFocusListener(new FocusListener() {

					@Override
					public void focusLost(FocusEvent arg0) {
						Integer points = null;
						try {
							points = Integer.valueOf(player1Score.getText());
						} catch (Exception e) {

						}

						match.setPlayer1Score(points);
						tournament.getTournamentGUI().getRankingTable()
								.resetPlayers();
					}

					@Override
					public void focusGained(FocusEvent arg0) {
					}
				});
				ComponentUtils.forceSize(player1Score, 50, 25);
			}

			return player1Score;
		}

		public JTextField getPlayer2ScoreField() {
			if (player2Score == null) {
				player2Score = new JTextField();
				player2Score.addFocusListener(new FocusListener() {

					@Override
					public void focusLost(FocusEvent arg0) {
						Integer points = null;
						try {
							points = Integer.valueOf(player2Score.getText());
						} catch (Exception e) {

						}

						match.setPlayer2Score(points);
						tournament.getTournamentGUI().getRankingTable()
								.resetPlayers();
					}

					@Override
					public void focusGained(FocusEvent arg0) {
					}
				});
				ComponentUtils.forceSize(player2Score, 50, 25);
			}

			return player2Score;
		}

		public void reset(boolean isTextOnly) {

			String titleText = null;

			boolean showScore = RunewarsModule.getInstance().getOptions()
					.isShowScore();

			getPlayer1ScoreLabel().setVisible(showScore);
			getPlayer1ScoreField().setVisible(showScore);
			getPlayer2ScoreLabel().setVisible(showScore);
			getPlayer2ScoreField().setVisible(showScore);

			if (match.getPlayer2() == null) {
				titleText = match.getPlayer1().getName() + " has a BYE";
			} else {
				titleText = match.getPlayer1().getName() + " VS "
						+ match.getPlayer2().getName();
				if (match.isDuplicate()) {
					titleText = "(Duplicate)" + titleText;
				}

				if (CryodexController.getOptions().isShowTableNumbers()) {
					titleText = tableNumber + ": " + titleText;
				}

				getPlayer1ScoreLabel().setText(
						match.getPlayer1().getName() + " score");
				getPlayer2ScoreLabel().setText(
						match.getPlayer2().getName() + " score");
			}

			getPlayerTitle().setText(titleText);

			if (isTextOnly == false) {
				getResultCombo().removeAllItems();
				for (String s : getComboValues()) {
					getResultCombo().addItem(s);
				}
			}
		}

		public void markInvalid() {
			if (match.isValidResult() == false) {
				getPlayerTitle().setForeground(Color.red);
			} else {
				getPlayerTitle().setForeground(Color.black);
			}
		}
	}

	public void markInvalid() {
		for (GamePanel gamePanel : gamePanels) {
			gamePanel.markInvalid();
		}
	}

}
