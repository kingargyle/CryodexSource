package cryodex.widget;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SpringLayout;

import cryodex.Main;

/**
 * Basic panel for displaying information on how the program got started.
 * 
 * @author cbrown
 *
 */
public class AboutPanel extends JPanel {

	private static final long serialVersionUID = 1L;

	private JButton closeButton;
	private final JDialog parent;

	public static void showAboutPanel() {
		JDialog manualModificationPanel = new JDialog(Main.getInstance(),
				"About", true);
		JPanel panel = new JPanel();
		manualModificationPanel.getContentPane().add(panel);
		panel.add(new AboutPanel(manualModificationPanel));
		manualModificationPanel.setLocationRelativeTo(Main.getInstance());
		manualModificationPanel.pack();

		manualModificationPanel.setVisible(true);
	}

	private AboutPanel(JDialog parent) {
		super(new SpringLayout());

		this.parent = parent;

		String aboutText = "<HTML>This program was created for the Campaign Against Cancer Tournament. A special thanks to Chad Hoefle and Anthony Lullig for their encouragement and testing during that time. I would also like to thank all of those who encouraged me to make it better and distribute it after that tournament was complete. My goal is to have a program that #1 follows the rules and #2 is easy to use. You are welcome to contact me with any comments or concerns you have about the program. My email is Chris.Brown.SPE@gmail.com. You can also use that email to send a donation via Paypal if you feel so inclined.</HTML>";

		JLabel aboutLabel = new JLabel(aboutText);
		ComponentUtils.forceSize(aboutLabel, 400, 175);

		this.add(aboutLabel);

		this.add(ComponentUtils.addToFlowLayout(getCloseButton(),
				FlowLayout.CENTER));

		SpringUtilities.makeCompactGrid(this, this.getComponentCount(), 1, 1,
				1, 1, 1);
	}

	private JButton getCloseButton() {
		if (closeButton == null) {
			closeButton = new JButton("Close");

			closeButton.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					parent.setVisible(false);
				}
			});

		}
		return closeButton;
	}
}
