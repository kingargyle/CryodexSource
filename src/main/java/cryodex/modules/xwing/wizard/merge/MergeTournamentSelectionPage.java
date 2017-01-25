package cryodex.modules.xwing.wizard.merge;

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
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.SpringLayout;

import cryodex.CryodexController;
import cryodex.Language;
import cryodex.modules.Tournament;
import cryodex.modules.xwing.XWingComparator;
import cryodex.modules.xwing.XWingPlayer;
import cryodex.modules.xwing.XWingTournament;
import cryodex.modules.xwing.wizard.Page;
import cryodex.modules.xwing.wizard.XWingWizard;
import cryodex.modules.xwing.wizard.common.AdditionalOptionsPage;
import cryodex.widget.ComponentUtils;
import cryodex.widget.SpringUtilities;

public class MergeTournamentSelectionPage implements Page {

        private JPanel pagePanel = null;
        private final Map<XWingTournament, JCheckBox> checkBoxMap = new HashMap<XWingTournament, JCheckBox>();
        private JRadioButton all;
        private JRadioButton manual;
        private JTextField manualInput;

        @Override
        public JPanel getPanel() {

            XWingWizard.getInstance().setButtonVisibility(true, true, false);

            XWingWizard.getInstance().setMinimumSize(new Dimension(450, 500));

            if (pagePanel == null) {
                pagePanel = new JPanel(new BorderLayout());

                JLabel header = new JLabel("<HTML><H3>" + Language.select_tournaments + "</H3></HTML>");

                JPanel listPanel = new JPanel(new SpringLayout());

                for (Tournament t : CryodexController.getAllTournaments()) {
                    JCheckBox cb = new JCheckBox(t.getName());
                    if (t instanceof XWingTournament) {
                        checkBoxMap.put((XWingTournament) t, cb);
                    }

                    listPanel.add(cb);
                }

                SpringUtilities.makeCompactGrid(listPanel, listPanel.getComponentCount(), 1, 0, 0, 0, 0);

                JLabel playersFromLabel = new JLabel("<HTML><H3>" + Language.how_many_players_from_each_event + "</H3></HTML>");
                ButtonGroup pf = new ButtonGroup();

                all = new JRadioButton(Language.all_players);
                manual = new JRadioButton(Language.let_me_pick + ":");
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

                JPanel manualPanel = ComponentUtils.addToHorizontalBorderLayout(manual, ComponentUtils.addToFlowLayout(manualInput, FlowLayout.LEFT),
                        null);

                JPanel howManyPlayersPanel = ComponentUtils.addToVerticalBorderLayout(playersFromLabel,all, manualPanel);
                
                pagePanel.add(ComponentUtils.addToFlowLayout(header, FlowLayout.LEFT), BorderLayout.NORTH);
                pagePanel.add(ComponentUtils.addToFlowLayout(listPanel, FlowLayout.LEFT), BorderLayout.CENTER);
                pagePanel.add(ComponentUtils.addToFlowLayout(howManyPlayersPanel, FlowLayout.CENTER), BorderLayout.SOUTH);
            }
            return ComponentUtils.addToFlowLayout(pagePanel, FlowLayout.CENTER);
        }

        @Override
        public void onNext() {
            XWingWizard.getInstance().getWizardOptions().setMerge(true);
            List<XWingTournament> tournamentList = new ArrayList<XWingTournament>();
            Set<XWingPlayer> playerList = new TreeSet<XWingPlayer>();
            Integer playerCount = null;

            if (manual.isSelected()) {
                playerCount = Integer.parseInt(manualInput.getText());
            }
            

            for (XWingTournament t : checkBoxMap.keySet()) {
                if (checkBoxMap.get(t).isSelected()) {

                    tournamentList.add(t);
                    List<XWingPlayer> thisTournamentPlayers = new ArrayList<XWingPlayer>();
                    thisTournamentPlayers.addAll(t.getXWingPlayers());

                    if (playerCount == null || thisTournamentPlayers.size() <= playerCount) {
                        playerList.addAll(thisTournamentPlayers);
                    } else {
                        Collections.sort(thisTournamentPlayers, new XWingComparator(t, XWingComparator.rankingCompare));
                        playerList.addAll(thisTournamentPlayers.subList(0, playerCount));
                    }
                }
            }


            List<XWingPlayer> addingList = new ArrayList<XWingPlayer>();
            addingList.addAll(playerList);
            XWingWizard.getInstance().getWizardOptions().setPlayerList(addingList);
            XWingWizard.getInstance().getWizardOptions().setSelectedTournaments(tournamentList);

            XWingWizard.getInstance().setCurrentPage(new AdditionalOptionsPage());

        }

        @Override
        public void onPrevious() {
            XWingWizard.getInstance().goToPrevious();
        }

        @Override
        public void onFinish() {
            // Do nothing
        }
    }