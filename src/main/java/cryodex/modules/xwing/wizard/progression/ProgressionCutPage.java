package cryodex.modules.xwing.wizard.progression;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SpringLayout;

import cryodex.CryodexController;
import cryodex.Language;
import cryodex.modules.Tournament;
import cryodex.modules.xwing.XWingPlayer;
import cryodex.modules.xwing.XWingTournament;
import cryodex.modules.xwing.wizard.Page;
import cryodex.modules.xwing.wizard.WizardOptions;
import cryodex.modules.xwing.wizard.WizardUtils;
import cryodex.modules.xwing.wizard.XWingWizard;
import cryodex.modules.xwing.wizard.common.AdditionalOptionsPage;
import cryodex.widget.ComponentUtils;
import cryodex.widget.SpringUtilities;

public class ProgressionCutPage implements Page {

    private JPanel pagePanel = null;
    private final Map<XWingTournament, JCheckBox> checkBoxMap = new HashMap<XWingTournament, JCheckBox>();
    private JLabel maxPlayersLabel = null;
    private JLabel minPointsLabel = null;
    private JTextField maxPlayersTF = null;
    private JTextField minPointsTF = null;

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

            JLabel playersFromLabel = new JLabel("<HTML><H3>" + Language.additional_information + "</H3></HTML>");

            maxPlayersLabel = new JLabel(Language.max_players);
            minPointsLabel = new JLabel(Language.min_points);

            maxPlayersTF = new JTextField(3);
            minPointsTF = new JTextField(3);

            JPanel maxPlayer = ComponentUtils.addToHorizontalBorderLayout(maxPlayersLabel, maxPlayersTF, null);
            JPanel minPoints = ComponentUtils.addToHorizontalBorderLayout(minPointsLabel, minPointsTF, null);

            JPanel infoPanel = ComponentUtils.addToVerticalBorderLayout(playersFromLabel, maxPlayer, minPoints);

            pagePanel.add(ComponentUtils.addToFlowLayout(header, FlowLayout.LEFT), BorderLayout.NORTH);
            pagePanel.add(ComponentUtils.addToFlowLayout(listPanel, FlowLayout.LEFT), BorderLayout.CENTER);
            pagePanel.add(ComponentUtils.addToFlowLayout(infoPanel, FlowLayout.CENTER), BorderLayout.SOUTH);
        }
        return ComponentUtils.addToFlowLayout(pagePanel, FlowLayout.CENTER);
    }

    @Override
    public void onNext() {
        WizardOptions wizardOptions = XWingWizard.getInstance().getWizardOptions();
        List<XWingTournament> tournamentList = new ArrayList<XWingTournament>();
        Set<XWingPlayer> playerList = new TreeSet<XWingPlayer>();
        Integer playerCount = null;
        Integer minPoints = null;

        try {
            playerCount = Integer.parseInt(maxPlayersTF.getText());
        } catch (NumberFormatException e) {
            // Leave it as null
        }

        try {
            minPoints = Integer.parseInt(minPointsTF.getText());
        } catch (NumberFormatException e) {
            // Leave it as null
        }

        for (XWingTournament t : checkBoxMap.keySet()) {
            if (checkBoxMap.get(t).isSelected()) {
                tournamentList.add(t);
                playerList.addAll(t.getXWingPlayers());
            }
        }
        
        if(tournamentList.isEmpty()){
            return;
        }

        wizardOptions.setPlayerList(new ArrayList<XWingPlayer>(playerList));
        wizardOptions.setSelectedTournaments(tournamentList);

        List<XWingPlayer> rankedPlayers = WizardUtils.rankMergedPlayers(wizardOptions);
        XWingTournament mergedTournament = WizardUtils.getMergedTournament(wizardOptions);

        List<XWingPlayer> playersToAdd = new ArrayList<XWingPlayer>();

        for (XWingPlayer p : rankedPlayers) {
            if (playerCount != null && playersToAdd.size() >= playerCount) {
                break;
            }

            if (minPoints != null && p.getScore(mergedTournament) < minPoints) {
                continue;
            }
            
            playersToAdd.add(p);
        }

        wizardOptions.setPlayerList(playersToAdd);

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