package cryodex.widget;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;

import cryodex.CryodexController;
import cryodex.Main;
import cryodex.Player;

public class MassDropPanel extends JDialog {

    private static final long serialVersionUID = 1L;

    public MassDropPanel() {
        super(Main.getInstance(), "Mass Drop Players", true);

        JTabbedPane tabPanel = new JTabbedPane();

        tabPanel.add("By Points", new ByPointsPanel());
        tabPanel.add("Player Select", new PlayerSelect());

        this.add(tabPanel);

        MassDropPanel.this.setLocationRelativeTo(Main.getInstance());
        MassDropPanel.this.pack();
        this.setMinimumSize(new Dimension(300, 300));
    }

    private class PlayerSelect extends JPanel {

        private static final long serialVersionUID = 1L;

        private DoubleList<Player> playerList;
        private JButton ok;
        private JButton cancel;

        public PlayerSelect() {
            super(new BorderLayout());

            init();
            buildPanel();
        }

        private void init() {

            List<Player> players = CryodexController.getActiveTournament().getPlayers();
            playerList = new DoubleList<Player>(players, null, "Player List", "Dropping");

            ok = new JButton("Ok");
            ok.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent arg0) {

                    List<Player> playersToDrop = playerList.getList2Values();

                    CryodexController.getActiveTournament().massDropPlayers(playersToDrop);

                    MassDropPanel.this.setVisible(false);
                }
            });

            cancel = new JButton("Cancel");
            cancel.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    MassDropPanel.this.setVisible(false);
                }
            });
        }

        private void buildPanel() {
            this.add(playerList, BorderLayout.CENTER);

            JPanel buttonPanel = ComponentUtils.addToHorizontalBorderLayout(ok, null, cancel);

            this.add(ComponentUtils.addToFlowLayout(buttonPanel, FlowLayout.CENTER), BorderLayout.SOUTH);
        }
    }

    private class ByPointsPanel extends JPanel {

        private static final long serialVersionUID = 1L;

        JLabel minPointsLabel;
        JLabel maxPlayersLabel;

        JTextField minPointsTF;
        JTextField maxPlayersTF;

        JButton ok;
        JButton cancel;

        public ByPointsPanel() {

            super(new BorderLayout());

            init();
            buildPanel();

        }

        private void init() {
            minPointsLabel = new JLabel("Min Points");
            maxPlayersLabel = new JLabel("Max Players");

            minPointsTF = new JTextField(3);
            maxPlayersTF = new JTextField(3);

            ok = new JButton("Ok");
            ok.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent arg0) {

                    Integer minScore = null;
                    Integer maxPlayers = null;

                    try {
                        minScore = Integer.parseInt(minPointsTF.getText());
                        maxPlayers = Integer.parseInt(maxPlayersTF.getText());
                    } catch (Exception e) {
                        MassDropPanel.this.setVisible(false);
                        return;
                    }

                    CryodexController.getActiveTournament().massDropPlayers(minScore, maxPlayers);

                    MassDropPanel.this.setVisible(false);
                }
            });

            cancel = new JButton("Cancel");
            cancel.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    MassDropPanel.this.setVisible(false);
                }
            });
        }

        private void buildPanel() {

            JPanel optionPanel = new JPanel(new BorderLayout());

            optionPanel.add(ComponentUtils.addToHorizontalBorderLayout(minPointsLabel, minPointsTF, null), BorderLayout.NORTH);

            optionPanel.add(ComponentUtils.addToHorizontalBorderLayout(maxPlayersLabel, maxPlayersTF, null), BorderLayout.CENTER);

            this.add(ComponentUtils.addToFlowLayout(optionPanel, FlowLayout.CENTER), BorderLayout.NORTH);

            JPanel buttonPanel = ComponentUtils.addToHorizontalBorderLayout(ok, null, cancel);

            this.add(ComponentUtils.addToFlowLayout(buttonPanel, FlowLayout.CENTER), BorderLayout.CENTER);

        }
    }
}
