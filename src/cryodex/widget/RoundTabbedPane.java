package cryodex.widget;

import javax.swing.JPanel;

import cryodex.CryodexController;

public class RoundTabbedPane extends AddTabTabbedPane {

	private static final long serialVersionUID = 7883808610257649371L;

	public RoundTabbedPane() {
		super("Generate Next Round");
	}

	public void addSwissTab(int roundNumber, JPanel roundPanel) {
		setVisible(true);
		while (this.getTabCount() > roundNumber) {
			this.remove(roundNumber - 2);
		}

		this.addTab("Round " + roundNumber, roundPanel);

		this.validate();
		this.repaint();
	}

	public void addSingleEliminationTab(int numberOfPlayers, JPanel roundPanel) {
		setVisible(true);

		this.addTab("Top " + numberOfPlayers, roundPanel);

		this.validate();
		this.repaint();
	}

	@Override
	public void triggerEvent() {
		boolean successful = CryodexController.getActiveTournament()
				.generateNextRound();

		if (successful == false) {
			setSelectedIndex(getTabCount() - 2);
		}
	}
}
