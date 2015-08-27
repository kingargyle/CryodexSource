package cryodex.modules.xwing;

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

public class XWingRoundPanel extends JPanel {

	private static final long serialVersionUID = 1L;

	private final List<XWingMatch> matches;
	private final List<GamePanel> gamePanels = new ArrayList<GamePanel>();
	private JPanel quickEntryPanel;
	private JPanel quickEntrySubPanel;
	private JTextField roundNumber;
	private JComboBox<XWingPlayer> playerCombo;
	private final JScrollPane scroll;

	private final XWingTournament tournament;

	public XWingRoundPanel(XWingTournament t, List<XWingMatch> matches) {

		super(new BorderLayout());

		this.tournament = t;
		this.matches = matches;
		this.setBorder(BorderFactory.createLineBorder(Color.black));

		int counter = 1;
		for (XWingMatch match : matches) {
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

			List<XWingPlayer> playerList = new ArrayList<XWingPlayer>();

			playerList.add(new XWingPlayer(new Player()));
			playerList.addAll(tournament.getXWingPlayers());

			Collections.sort(playerList);

			playerCombo = new JComboBox<XWingPlayer>(
					playerList.toArray(new XWingPlayer[playerList.size()]));

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
		ComponentUtils.repaint(XWingRoundPanel.this);

		Integer i = null;
		try {
			i = Integer.parseInt(roundNumber.getText());
		} catch (NumberFormatException e) {

		}

		XWingPlayer player = playerCombo.getSelectedIndex() == 0 ? null
				: (XWingPlayer) playerCombo.getSelectedItem();

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
			panel.add(gamePanel.getPlayer1KillLabel(), gbc);

			gbc.gridx = 2;
			panel.add(gamePanel.getPlayer1KillPointsField(), gbc);

			gbc.gridy++;
			gbc.gridwidth = 1;

			gbc.gridx = 1;
			panel.add(gamePanel.getPlayer2KillLabel(), gbc);

			gbc.gridx = 2;
			panel.add(gamePanel.getPlayer2KillPointsField(), gbc);
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
				panel.add(gp.getPlayer1KillLabel(), gbc);

				gbc.gridx = 2;
				panel.add(gp.getPlayer1KillPointsField(), gbc);

				gbc.gridy++;
				gbc.gridwidth = 1;

				gbc.gridx = 1;
				panel.add(gp.getPlayer2KillLabel(), gbc);

				gbc.gridx = 2;
				panel.add(gp.getPlayer2KillPointsField(), gbc);
			}
		}

		return panel;
	}

	public List<XWingMatch> getMatches() {
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
		private final XWingMatch match;
		private JLabel playersTitle;
		private JComboBox<String> resultsCombo;
		private JTextField player1KillPoints;
		private JTextField player2KillPoints;
		private JLabel player1KillLabel;
		private JLabel player2KillLabel;
		private boolean isLoading = false;
		private int tableNumber = -1;

		public GamePanel(int tableNumber, XWingMatch match) {

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
					if (match.isDraw()) {
						getResultCombo().setSelectedIndex(5);
					} else if (match.getWinner() == match.getPlayer1()) {
						if (match.isModified()) {
							getResultCombo().setSelectedIndex(3);
						} else {
							getResultCombo().setSelectedIndex(1);
						}
					} else if (match.getWinner() == match.getPlayer2()) {
						if (match.isModified()) {
							getResultCombo().setSelectedIndex(4);
						} else {
							getResultCombo().setSelectedIndex(2);
						}
					}
				}
			}
			if (match.getPlayer2() != null) {
				if (match.getPlayer1PointsDestroyed() != null) {
					getPlayer1KillPointsField().setText(
							String.valueOf(match.getPlayer1PointsDestroyed()));
				}
				if (match.getPlayer2PointsDestroyed() != null) {
					getPlayer2KillPointsField().setText(
							String.valueOf(match.getPlayer2PointsDestroyed()));
				}
			}
			isLoading = false;
		}

		private XWingMatch getMatch() {
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

		private JLabel getPlayer1KillLabel() {
			if (player1KillLabel == null) {
				player1KillLabel = new JLabel();
			}
			return player1KillLabel;
		}

		private JLabel getPlayer2KillLabel() {
			if (player2KillLabel == null) {
				player2KillLabel = new JLabel();
			}
			return player2KillLabel;
		}

		private String[] getComboValues() {

			if (match.getPlayer2() == null) {
				String[] values = { "Select a result", "BYE" };
				return values;
			} else {
				String generic = XWingModule.getInstance().getOptions()
						.isEnterOnlyPoints() ? "Enter results"
						: "Select a result";
				String[] values = { generic,
						"WIN - " + match.getPlayer1().getName(),
						"WIN - " + match.getPlayer2().getName(),
						"MODIFIED WIN - " + match.getPlayer1().getName(),
						"MODIFIED WIN - " + match.getPlayer2().getName(),
						"DRAW" };
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
				match.setModified(false);
				match.setDraw(false);
				match.setWinner(null);
				match.setBye(false);
				break;
			case 1:
				if (match.getPlayer2() == null) {
					match.setBye(true);
				} else {
					match.setModified(false);
					match.setDraw(false);
					match.setWinner(match.getPlayer1());
				}
				break;
			case 2:
				match.setModified(false);
				match.setDraw(false);
				match.setWinner(match.getPlayer2());
				break;
			case 3:
				match.setModified(true);
				match.setDraw(false);
				match.setWinner(match.getPlayer1());
				break;
			case 4:
				match.setModified(true);
				match.setDraw(false);
				match.setWinner(match.getPlayer2());
				break;
			case 5:
				match.setModified(false);
				match.setDraw(true);
				match.setWinner(null);
				break;
			default:
				break;
			}
			tournament.getTournamentGUI().getRankingTable().resetPlayers();

		}

		public JTextField getPlayer1KillPointsField() {
			if (player1KillPoints == null) {
				player1KillPoints = new JTextField();
				player1KillPoints.addFocusListener(new FocusListener() {

					@Override
					public void focusLost(FocusEvent arg0) {
						Integer points = null;
						try {
							points = Integer.valueOf(player1KillPoints
									.getText());
						} catch (Exception e) {

						}

						match.setPlayer1PointsDestroyed(points);
						tournament.getTournamentGUI().getRankingTable()
								.resetPlayers();

						if (XWingModule.getInstance().getOptions()
								.isEnterOnlyPoints()) {
							setResultsCombo();
						}
					}

					@Override
					public void focusGained(FocusEvent arg0) {
					}
				});
				ComponentUtils.forceSize(player1KillPoints, 50, 25);
			}

			return player1KillPoints;
		}

		public JTextField getPlayer2KillPointsField() {
			if (player2KillPoints == null) {
				player2KillPoints = new JTextField();
				player2KillPoints.addFocusListener(new FocusListener() {

					@Override
					public void focusLost(FocusEvent arg0) {
						Integer points = null;
						try {
							points = Integer.valueOf(player2KillPoints
									.getText());
						} catch (Exception e) {

						}

						match.setPlayer2PointsDestroyed(points);
						tournament.getTournamentGUI().getRankingTable()
								.resetPlayers();

						if (XWingModule.getInstance().getOptions()
								.isEnterOnlyPoints()) {
							setResultsCombo();
						}
					}

					@Override
					public void focusGained(FocusEvent arg0) {
					}
				});
				ComponentUtils.forceSize(player2KillPoints, 50, 25);
			}

			return player2KillPoints;
		}

		public void reset(boolean isTextOnly) {

			String titleText = null;

			boolean showKillPoints = XWingModule.getInstance().getOptions()
					.isShowKillPoints();
			boolean enterOnlyPoints = XWingModule.getInstance().getOptions()
					.isEnterOnlyPoints();

			getPlayer1KillLabel().setVisible(showKillPoints);
			getPlayer1KillPointsField().setVisible(showKillPoints);
			getPlayer2KillLabel().setVisible(showKillPoints);
			getPlayer2KillPointsField().setVisible(showKillPoints);

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

				getPlayer1KillLabel().setText(
						match.getPlayer1().getName() + " kill points");
				getPlayer2KillLabel().setText(
						match.getPlayer2().getName() + " kill points");
			}

			getPlayerTitle().setText(titleText);

			if (isTextOnly == false) {
				getResultCombo().removeAllItems();
				for (String s : getComboValues()) {
					getResultCombo().addItem(s);
				}
			}

			getResultCombo().setEnabled(!enterOnlyPoints);

			if (enterOnlyPoints) {
				if (getComboValues().length == 2) {
					getResultCombo().setSelectedIndex(1);
					match.setBye(true);
				} else {
					setResultsCombo();
				}
			}
		}

		public void setResultsCombo() {
			if (match.getPlayer1PointsDestroyed() != null
					|| match.getPlayer2PointsDestroyed() != null) {
				Integer p1points = match.getPlayer1PointsDestroyed() == null ? 0
						: match.getPlayer1PointsDestroyed();
				Integer p2points = match.getPlayer2PointsDestroyed() == null ? 0
						: match.getPlayer2PointsDestroyed();

				if (p1points.equals(p2points)) {
					getResultCombo().setSelectedIndex(5);
				}
				if (p1points > p2points) {
					if (p1points - p2points >= 12) {
						getResultCombo().setSelectedIndex(1);
					} else {
						getResultCombo().setSelectedIndex(3);
					}
				}

				if (p2points > p1points) {
					if (p2points - p1points >= 12) {
						getResultCombo().setSelectedIndex(2);
					} else {
						getResultCombo().setSelectedIndex(4);
					}
				}
			} else {
				getResultCombo().setSelectedIndex(0);
			}
			comboChange();
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
