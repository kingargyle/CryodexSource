package cryodex.modules.xwing;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import cryodex.Player;
import cryodex.modules.ModulePlayer;
import cryodex.modules.RegistrationPanel;

public class XWingRegistrationPanel implements RegistrationPanel {

	private JTextField squadField;
	private JCheckBox firstRoundByeCheckbox;
	private JPanel panel;

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
			panel.add(new JLabel("<html><b>X-Wing</b></html>"), gbc);

			gbc.gridy++;
			panel.add(new JLabel("Squadron Builder ID"), gbc);

			gbc.gridy++;
			panel.add(getSquadField(), gbc);

			gbc.gridy++;
			panel.add(getFirstRoundByeCheckbox(), gbc);
		}

		return panel;
	}

	private JTextField getSquadField() {
		if (squadField == null) {
			squadField = new JTextField();
		}
		return squadField;
	}

	private JCheckBox getFirstRoundByeCheckbox() {
		if (firstRoundByeCheckbox == null) {
			firstRoundByeCheckbox = new JCheckBox("First Round Bye");
		}
		return firstRoundByeCheckbox;
	}

	@Override
	public void save(Player player) {

		XWingPlayer xp = null;

		// get module information
		if (player.getModuleInfo() != null
				&& player.getModuleInfo().isEmpty() == false) {
			for (ModulePlayer mp : player.getModuleInfo()) {
				if (mp instanceof XWingPlayer) {
					xp = (XWingPlayer) mp;
					break;
				}
			}
		}

		// if no module information, create one and add it to player
		if (xp == null) {
			xp = new XWingPlayer(player);
			player.getModuleInfo().add(xp);
		}

		// update module information
		xp.setSquadId(getSquadField().getText());
		xp.setFirstRoundBye(getFirstRoundByeCheckbox().isSelected());
	}

	@Override
	public void load(Player player) {
		XWingPlayer xp = null;

		// get module information
		if (player != null && player.getModuleInfo() != null
				&& player.getModuleInfo().isEmpty() == false) {
			for (ModulePlayer mp : player.getModuleInfo()) {
				if (mp instanceof XWingPlayer) {
					xp = (XWingPlayer) mp;
					break;
				}
			}
		}

		// if no module information, create one and add it to player
		if (xp != null) {
			getSquadField().setText(xp.getSquadId());
			getFirstRoundByeCheckbox().setSelected(xp.isFirstRoundBye());
		} else {
			clearFields();
		}
	}

	@Override
	public void clearFields() {
		getSquadField().setText("");
		getFirstRoundByeCheckbox().setSelected(false);
	}

}
