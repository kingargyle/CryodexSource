package cryodex.modules.runewars;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import cryodex.CryodexController.Modules;
import cryodex.Player;
import cryodex.modules.ModulePlayer;
import cryodex.modules.RegistrationPanel;
import cryodex.modules.runewars.RunewarsPlayer.Faction;

public class RunewarsRegistrationPanel implements RegistrationPanel {

	// private JTextField squadField;
	private JCheckBox firstRoundByeCheckbox;
	private JPanel panel;
        private JComboBox<Faction> rwFactionCombo;

	@Override
	public JPanel getPanel() {
		if (panel == null) {
			panel = new JPanel(new GridBagLayout());

			GridBagConstraints gbc = new GridBagConstraints();

			gbc.gridx = 0;
			gbc.gridy = 0;
			gbc.fill = GridBagConstraints.BOTH;
			gbc.weightx = 1;
			gbc.anchor = GridBagConstraints.EAST;
			panel.add(new JLabel("<html><b>" + Modules.RUNEWARS.getName()
					+ "</b></html>"), gbc);

			gbc.gridy++;
			panel.add(getFirstRoundByeCheckbox(), gbc);

                        gbc.gridy++;
                        panel.add(getFactionCombo(), gbc);
		}

		return panel;
	}

	private JCheckBox getFirstRoundByeCheckbox() {
		if (firstRoundByeCheckbox == null) {
			firstRoundByeCheckbox = new JCheckBox("First Round Bye");
		}
		return firstRoundByeCheckbox;
	}

        private JComboBox<Faction> getFactionCombo() {
                if (rwFactionCombo == null) {
                        rwFactionCombo = new JComboBox<RunewarsPlayer.Faction>();
                        rwFactionCombo.addItem(Faction.DAQAN);
                        rwFactionCombo.addItem(Faction.WAIQAR);
                        rwFactionCombo.setSelectedIndex(-1);
                }
                return rwFactionCombo;
        }

	@Override
	public void save(Player player) {

		RunewarsPlayer xp = null;

		// get module information
		if (player.getModuleInfo() != null
				&& player.getModuleInfo().isEmpty() == false) {
			for (ModulePlayer mp : player.getModuleInfo()) {
				if (mp instanceof RunewarsPlayer) {
					xp = (RunewarsPlayer) mp;
					break;
				}
			}
		}

		// if no module information, create one and add it to player
		if (xp == null) {
			xp = new RunewarsPlayer(player);
			player.getModuleInfo().add(xp);
		}

		// update module information
		// xp.setSquadId(getSquadField().getText());
		xp.setFirstRoundBye(getFirstRoundByeCheckbox().isSelected());
                xp.setFaction((Faction) getFactionCombo().getSelectedItem());
	}

	@Override
	public void load(Player player) {
		RunewarsPlayer xp = null;

		// get module information
		if (player != null && player.getModuleInfo() != null
				&& player.getModuleInfo().isEmpty() == false) {
			for (ModulePlayer mp : player.getModuleInfo()) {
				if (mp instanceof RunewarsPlayer) {
					xp = (RunewarsPlayer) mp;
					break;
				}
			}
		}

		// if no module information, create one and add it to player
		if (xp != null) {
			// getSquadField().setText(xp.getSquadId());
			getFirstRoundByeCheckbox().setSelected(xp.isFirstRoundBye());
                        getFactionCombo().setSelectedItem(xp.getFaction());
		} else {
			clearFields();
		}
	}

	@Override
	public void clearFields() {
		getFirstRoundByeCheckbox().setSelected(false);
                getFactionCombo().setSelectedIndex(-1);
	}

}
