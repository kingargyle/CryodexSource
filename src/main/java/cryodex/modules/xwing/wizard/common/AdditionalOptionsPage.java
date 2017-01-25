package cryodex.modules.xwing.wizard.common;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.SpringLayout;

import cryodex.Language;
import cryodex.modules.xwing.XWingTournament.InitialSeedingEnum;
import cryodex.modules.xwing.wizard.Page;
import cryodex.modules.xwing.wizard.WizardOptions;
import cryodex.modules.xwing.wizard.WizardUtils;
import cryodex.modules.xwing.wizard.WizardUtils.SplitOptions;
import cryodex.modules.xwing.wizard.XWingWizard;
import cryodex.widget.ComponentUtils;
import cryodex.widget.SpringUtilities;

public class AdditionalOptionsPage implements Page {

    private JRadioButton randomRB;
    private JRadioButton byGroupRB;
    private JRadioButton byRankingRB;
    private JCheckBox nonSwiss;
    private JRadioButton singleElimination;
    private JRadioButton roundRobin;

    private JTextField customPointsTF;
    private JRadioButton standardRB;
    private JRadioButton escalationRB;
    private JRadioButton epicRB;
    private JRadioButton customRB;

    private JRadioButton splitRandomRB;
    private JRadioButton splitByGroupRB;
    private JRadioButton splitByRanking;

    private JPanel pagePanel;

    @Override
    public JPanel getPanel() {

        XWingWizard.getInstance().setButtonVisibility(true, null, true);

        XWingWizard.getInstance().setMinimumSize(new Dimension(450, 500));

        WizardOptions wizardOptions = XWingWizard.getInstance().getWizardOptions();

        if (pagePanel == null) {

            JPanel initialPairingPanel = new JPanel(new BorderLayout());

            JLabel header = new JLabel("<HTML><H3>" + Language.first_round_pairing + "</H3></HTML>");

            initialPairingPanel.add(ComponentUtils.addToFlowLayout(header, FlowLayout.LEFT), BorderLayout.NORTH);

            JPanel tournamentTypesPanel = new JPanel(new SpringLayout());

            ButtonGroup bg = new ButtonGroup();

            // Another radiobutton-group for Single Elim or Round Robin
            ButtonGroup bg2 = new ButtonGroup();

            randomRB = new JRadioButton(Language.random);
            byGroupRB = new JRadioButton(Language.seperate_by_group_name);
            byRankingRB = new JRadioButton(Language.by_ranking);

            nonSwiss = new JCheckBox("Non-Swiss alternatives");
            singleElimination = new JRadioButton("<HTML>" + Language.start_as_single_elimination + "</HTML>");
            roundRobin = new JRadioButton("<HTML>Start event as Round Robin</HTML>");

            bg.add(randomRB);
            bg.add(byGroupRB);
            bg.add(byRankingRB);

            // Adding buttons to buttongroup nr 2
            bg2.add(singleElimination);
            bg2.add(roundRobin);

            randomRB.setSelected(true);

            tournamentTypesPanel.add(randomRB);
            tournamentTypesPanel.add(byGroupRB);
            if (wizardOptions.getSelectedTournaments() != null && wizardOptions.getSelectedTournaments().isEmpty() == false) {
                tournamentTypesPanel.add(byRankingRB);
            }
            tournamentTypesPanel.add(nonSwiss);
            tournamentTypesPanel.add(singleElimination);
            tournamentTypesPanel.add(roundRobin);
            roundRobin.setEnabled(false);
            singleElimination.setEnabled(false);

            SpringUtilities.makeCompactGrid(tournamentTypesPanel, tournamentTypesPanel.getComponentCount(), 1, 0, 0, 0, 0);

            initialPairingPanel.add(ComponentUtils.addToFlowLayout(tournamentTypesPanel, FlowLayout.LEFT), BorderLayout.CENTER);

            JPanel centerPanel = new JPanel(new BorderLayout());

            JLabel pointHeader = new JLabel("<HTML><H3>" + Language.choose_point_type + "</H3></HTML>");

            centerPanel.add(ComponentUtils.addToFlowLayout(pointHeader, FlowLayout.LEFT), BorderLayout.NORTH);

            JPanel pointsPanel = new JPanel(new SpringLayout());

            ButtonGroup pointsBG = new ButtonGroup();

            standardRB = new JRadioButton(Language.standard_points);
            escalationRB = new JRadioButton(Language.escalation_points);
            epicRB = new JRadioButton(Language.epic_points);
            customRB = new JRadioButton(Language.custom_points);

            final JLabel customPointsInfo = new JLabel(Language.comma_separated);

            ActionListener customListener = new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent arg0) {
                    if (customRB.isSelected()) {
                        customPointsTF.setEnabled(true);
                    } else {
                        customPointsTF.setEnabled(false);
                    }
                    // Enable/Disable parts of UI
                    if (nonSwiss.isSelected()) {
                        singleElimination.setEnabled(true);
                        roundRobin.setEnabled(true);
                        if (roundRobin.isSelected()) {
                            byGroupRB.setEnabled(false);
                            randomRB.setEnabled(false);
                        } else {
                            byGroupRB.setEnabled(true);
                            randomRB.setEnabled(true);
                        }
                    } else {
                        singleElimination.setEnabled(false);
                        singleElimination.setSelected(false);
                        roundRobin.setEnabled(false);
                        roundRobin.setSelected(false);
                        byGroupRB.setEnabled(true);
                        randomRB.setEnabled(true);
                    }

                }
            };

            standardRB.addActionListener(customListener);
            escalationRB.addActionListener(customListener);
            epicRB.addActionListener(customListener);
            customRB.addActionListener(customListener);
            nonSwiss.addActionListener(customListener);
            roundRobin.addActionListener(customListener);
            singleElimination.addActionListener(customListener);

            customPointsTF = new JTextField();
            customPointsTF.setColumns(12);
            customPointsTF.setEnabled(false);

            pointsBG.add(standardRB);
            pointsBG.add(escalationRB);
            pointsBG.add(epicRB);
            pointsBG.add(customRB);

            standardRB.setSelected(true);

            pointsPanel.add(standardRB);
            pointsPanel.add(escalationRB);
            pointsPanel.add(epicRB);
            pointsPanel.add(customRB);
            pointsPanel.add(customPointsInfo);

            pointsPanel.add(
                    ComponentUtils.addToHorizontalBorderLayout(null, ComponentUtils.addToFlowLayout(customPointsTF, FlowLayout.LEFT), new JPanel()));

            SpringUtilities.makeCompactGrid(pointsPanel, 6, 1, 0, 0, 0, 0);

            centerPanel.add(ComponentUtils.addToFlowLayout(pointsPanel, FlowLayout.LEFT), BorderLayout.CENTER);

            JPanel splitOptionsPanel = new JPanel(new BorderLayout());

            JLabel splitOptionsHeader = new JLabel("<HTML><H3>" + Language.how_to_split_tournament + "</H3></HTML>");

            splitOptionsPanel.add(ComponentUtils.addToFlowLayout(splitOptionsHeader, FlowLayout.LEFT), BorderLayout.NORTH);

            JPanel splitOptionsSubPanel = new JPanel(new SpringLayout());

            ButtonGroup splitOptionsBG = new ButtonGroup();

            splitRandomRB = new JRadioButton(Language.random);
            splitByGroupRB = new JRadioButton(Language.separate_by_group_name);
            splitByRanking = new JRadioButton(Language.split_by_ranking);

            splitOptionsBG.add(splitRandomRB);
            splitOptionsBG.add(splitByGroupRB);
            splitOptionsBG.add(splitByRanking);

            splitOptionsSubPanel.add(splitRandomRB);
            splitOptionsSubPanel.add(splitByGroupRB);
            if (wizardOptions.getSelectedTournaments() != null && wizardOptions.getSelectedTournaments().isEmpty() == false) {
                splitOptionsSubPanel.add(splitByRanking);
                XWingWizard.getInstance().setMinimumSize(new Dimension(450, 550));
            }

            SpringUtilities.makeCompactGrid(splitOptionsSubPanel, splitOptionsSubPanel.getComponentCount(), 1, 0, 0, 0, 0);

            splitOptionsPanel.add(ComponentUtils.addToFlowLayout(splitOptionsSubPanel, FlowLayout.LEFT), BorderLayout.CENTER);

            pagePanel = new JPanel(new FlowLayout());

            pagePanel.add(ComponentUtils.addToVerticalBorderLayout(initialPairingPanel, centerPanel,
                    wizardOptions.getSplit() > 1 ? splitOptionsPanel : null));
        }

        return pagePanel;
    }

    @Override
    public void onNext() {
        // Do nothing
    }

    @Override
    public void onPrevious() {
        XWingWizard.getInstance().goToPrevious();
    }

    @Override
    public void onFinish() {

        WizardOptions wizardOptions = XWingWizard.getInstance().getWizardOptions();

        if (standardRB.isSelected()) {
            wizardOptions.setPoints(100);
        } else if (escalationRB.isSelected()) {
            List<Integer> points = new ArrayList<Integer>();
            points.add(60);
            points.add(90);
            points.add(120);
            points.add(150);
            wizardOptions.setPoints(points);
        } else if (epicRB.isSelected()) {
            wizardOptions.setPoints(300);
        } else if (customRB.isSelected()) {
            try {
                Integer points = Integer.parseInt(customPointsTF.getText());
                wizardOptions.setPoints(points);
            } catch (Exception e) {
                String[] rounds = customPointsTF.getText().split(",");
                List<Integer> points = new ArrayList<Integer>();
                for (String s : rounds) {
                    points.add(Integer.parseInt(s.trim()));
                }
                wizardOptions.setPoints(points);
            }

        }

        if (randomRB.isSelected()) {
            wizardOptions.setInitialSeedingEnum(InitialSeedingEnum.RANDOM);
        } else if (byGroupRB.isSelected()) {
            wizardOptions.setInitialSeedingEnum(InitialSeedingEnum.BY_GROUP);
        } else if (byRankingRB.isSelected()) {
            wizardOptions.setInitialSeedingEnum(InitialSeedingEnum.IN_ORDER);
        }

        if (nonSwiss.isSelected()) {
            if (singleElimination.isSelected()) {
                wizardOptions.setSingleElimination(true);
            } else if (roundRobin.isSelected()) {
                wizardOptions.setRoundRobin(true);
            }
        }

        if (wizardOptions.getSplit() > 1) {
            SplitOptions splitOption = null;

            if (splitByGroupRB.isSelected()) {
                splitOption = SplitOptions.GROUP;
            } else if (splitByRanking.isSelected()) {
                splitOption = SplitOptions.RANKING;
            } else if (splitRandomRB.isSelected()) {
                splitOption = SplitOptions.RANDOM;
            }

            WizardUtils.createSplitTournament(splitOption);
        } else {
            WizardUtils.createTournament();
        }
    }
}