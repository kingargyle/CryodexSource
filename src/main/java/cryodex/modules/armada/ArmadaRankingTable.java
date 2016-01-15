package cryodex.modules.armada;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Font;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;

import cryodex.CryodexController;
import cryodex.widget.ComponentUtils;
import cryodex.widget.TimerPanel;

public class ArmadaRankingTable extends JPanel {

	private static final long serialVersionUID = 5587297504827909147L;

	private JTable table;
	private RankingTableModel model;
	private final ArmadaTournament tournament;
	private JLabel title;
	private JLabel statsLabel;

	public ArmadaRankingTable(ArmadaTournament tournament) {
		super(new BorderLayout());
		JScrollPane scrollPane = new JScrollPane(getTable());
		ComponentUtils.forceSize(this, 400, 300);
		this.tournament = tournament;

		getTable().setFillsViewportHeight(true);

		updateLabel();
		JPanel labelPanel = ComponentUtils.addToVerticalBorderLayout(
				getTitleLabel(), getStatsLabel(), null);
		labelPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

		this.add(labelPanel, BorderLayout.NORTH);
		this.add(scrollPane, BorderLayout.CENTER);
		this.add(new TimerPanel(), BorderLayout.SOUTH);
	}

	private JLabel getStatsLabel() {
		if (statsLabel == null) {
			statsLabel = new JLabel();
		}
		return statsLabel;
	}

	private JLabel getTitleLabel() {
		if (title == null) {
			title = new JLabel("Player Rankings");
			title.setFont(new Font(title.getFont().getName(), title.getFont()
					.getStyle(), 20));
		}

		return title;
	}

	public void updateLabel() {
		int total = tournament.getAllArmadaPlayers().size();
		int active = tournament.getArmadaPlayers().size();

		if (total == 0) {
			total = active;
		}

		int dropped = total - active;
		if (total == active) {
			getStatsLabel().setText("Total Players: " + total);
		} else {
			getStatsLabel().setText(
					"Total Players: " + total + " Active Players: " + active
							+ " Dropped Players: " + dropped);
		}

	}

	private JTable getTable() {
		if (table == null) {
			table = new JTable(getTableModel());
			table.setDefaultRenderer(Object.class,
					new NoCellSelectionRenderer());
			table.setDefaultRenderer(Integer.class,
					new NoCellSelectionRenderer());
			table.getColumnModel().getColumn(0).setPreferredWidth(200);

			NoCellSelectionRenderer centerRenderer = new NoCellSelectionRenderer();
			centerRenderer.setHorizontalAlignment(JLabel.CENTER);

			table.getColumnModel().getColumn(1).setCellRenderer(centerRenderer);
			table.getColumnModel().getColumn(2).setCellRenderer(centerRenderer);
			table.getColumnModel().getColumn(3).setCellRenderer(centerRenderer);
			table.getColumnModel().getColumn(4).setCellRenderer(centerRenderer);
			table.getColumnModel().getColumn(5).setCellRenderer(centerRenderer);
		}
		return table;
	}

	private RankingTableModel getTableModel() {
		if (model == null) {
			model = new RankingTableModel(new ArrayList<ArmadaPlayer>());
		}
		return model;
	}

	public void setPlayers(Set<ArmadaPlayer> players) {

		List<ArmadaPlayer> playerList = new ArrayList<ArmadaPlayer>();
		playerList.addAll(players);

		Collections.sort(playerList, new ArmadaComparator(tournament,
				ArmadaComparator.rankingCompare));

		if (this.isVisible() == false) {
			this.setVisible(true);
		}
		getTableModel().setData(playerList);
		CryodexController.saveData();
		updateLabel();
	}

	public void resetPlayers() {
		getTableModel().resetData();
		CryodexController.saveData();
		updateLabel();
	}

	private class RankingTableModel extends AbstractTableModel {

		private static final long serialVersionUID = -1591431777250055477L;

		private List<ArmadaPlayer> data;

		public RankingTableModel(List<ArmadaPlayer> data) {
			setData(data);
		}

		public void resetData() {
			Collections.sort(data, new ArmadaComparator(tournament,
					ArmadaComparator.rankingCompare));
			this.fireTableDataChanged();
		}

		public void setData(List<ArmadaPlayer> data) {
			this.data = data;

			Collections.sort(data, new ArmadaComparator(tournament,
					ArmadaComparator.rankingCompare));
			this.fireTableDataChanged();
		}

		@Override
		public String getColumnName(int column) {
			String value = null;
			switch (column) {
			case 0:
				value = "Name";
				break;
			case 1:
				value = "Score";
				break;
			case 2:
				value = "MoV";
				break;
			case 3:
				value = "SoS";
				break;
			case 4:
				value = "Record";
				break;
			case 5:
				value = "Byes";
				break;
			}
			return value;
		}

		@Override
		public int getColumnCount() {
			return 6;
		}

		@Override
		public int getRowCount() {
			return data.size();
		}

		@Override
		public Object getValueAt(int arg0, int arg1) {
			ArmadaPlayer user = data.get(arg0);
			Object value = null;
			switch (arg1) {
			case 0:
				value = user.getPlayer().getName();
				if (tournament.getArmadaPlayers().contains(user) == false) {
					value = "(D#" + user.getRoundDropped(tournament) + ")"
							+ value;
				}
				break;
			case 1:
				value = user.getScore(tournament);
				break;
			case 2:
				value = user.getMarginOfVictory(tournament);
				break;
			case 3:
				value = user.getAverageSoS(tournament);
				break;
			case 4:
				value = user.getWins(tournament) + " / "
						+ user.getLosses(tournament);
				break;
			case 5:
				value = user.getByes(tournament);
				break;

			}
			return value;
		}

	}

	public class NoCellSelectionRenderer extends DefaultTableCellRenderer {

		private static final long serialVersionUID = 1L;

		@Override
		public Component getTableCellRendererComponent(JTable table,
				Object value, boolean isSelected, boolean hasFocus, int row,
				int column) {
			super.getTableCellRendererComponent(table, value, isSelected,
					hasFocus, row, column);
			setBorder(noFocusBorder);
			return this;
		}
	}

}
