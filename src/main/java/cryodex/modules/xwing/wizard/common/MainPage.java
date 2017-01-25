package cryodex.modules.xwing.wizard.common;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import cryodex.CryodexController;
import cryodex.Language;
import cryodex.Main;
import cryodex.modules.Tournament;
import cryodex.modules.xwing.wizard.Page;
import cryodex.modules.xwing.wizard.WizardOptions;
import cryodex.modules.xwing.wizard.XWingWizard;
import cryodex.modules.xwing.wizard.merge.MergeTournamentSelectionPage;
import cryodex.modules.xwing.wizard.progression.ProgressionCutPage;
import cryodex.widget.ComponentUtils;

public class MainPage implements Page {

    JTextField nameTextField;

    JCheckBox mergeCB;
    JCheckBox splitCB;
    JTextField numSubs;
    JCheckBox progressionCut;

    JPanel pagePanel;

    @Override
    public JPanel getPanel() {

        XWingWizard.getInstance().setButtonVisibility(null, true, null);

        XWingWizard.getInstance().setMinimumSize(new Dimension(450, 300));

        if (pagePanel == null) {
            JPanel namePanel = new JPanel(new BorderLayout());

            JLabel nameHeader = new JLabel("<HTML><H1>" + Language.name_event + "</H1></HTML>");

            nameTextField = new JTextField(10);

            namePanel.add(ComponentUtils.addToFlowLayout(nameHeader, FlowLayout.LEFT), BorderLayout.NORTH);
            namePanel.add(ComponentUtils.addToFlowLayout(nameTextField, FlowLayout.LEFT));

            JPanel creationOptionsPanel = new JPanel(new BorderLayout());
            JPanel creationOptionsContentPanel = new JPanel(new BorderLayout());

            JLabel additionalOptionsHeader = new JLabel("<HTML><H1>" + Language.additional_options + "</H1></HTML>");

            creationOptionsPanel.add(ComponentUtils.addToFlowLayout(additionalOptionsHeader, FlowLayout.LEFT), BorderLayout.NORTH);

            JPanel splitEntryPanel = new JPanel(new BorderLayout());
            ComponentUtils.forceSize(splitEntryPanel, 210, 60);

            splitCB = new JCheckBox(Language.split_into_subtournaments);
            final JLabel splitLabel = new JLabel(Language.number_of_sub_tournaments + ":");
            numSubs = new JTextField(3);

            splitLabel.setVisible(false);
            numSubs.setVisible(false);

            splitCB.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent arg0) {
                    splitLabel.setVisible(splitCB.isSelected());
                    numSubs.setVisible(splitCB.isSelected());
                }
            });

            splitEntryPanel.add(splitCB, BorderLayout.NORTH);
            splitEntryPanel.add(
                    ComponentUtils.addToFlowLayout(ComponentUtils.addToHorizontalBorderLayout(splitLabel, numSubs, null), FlowLayout.LEFT),
                    BorderLayout.CENTER);

            creationOptionsContentPanel.add(ComponentUtils.addToFlowLayout(splitEntryPanel, FlowLayout.LEFT), BorderLayout.SOUTH);

            mergeCB = new JCheckBox(Language.merge_multiple_tournaments_into_one);
            progressionCut = new JCheckBox(Language.progression_cut);
            progressionCut.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    if (progressionCut.isSelected()) {
                        mergeCB.setSelected(false);
                        mergeCB.setEnabled(false);
                        splitCB.setSelected(false);
                        splitCB.setEnabled(false);
                    } else {
                        mergeCB.setEnabled(true);
                        splitCB.setEnabled(true);
                    }
                }
            });

            creationOptionsContentPanel.add(ComponentUtils.addToFlowLayout(mergeCB, FlowLayout.LEFT), BorderLayout.CENTER);
            creationOptionsContentPanel.add(ComponentUtils.addToFlowLayout(progressionCut, FlowLayout.LEFT), BorderLayout.NORTH);

            creationOptionsPanel.add(ComponentUtils.addToFlowLayout(creationOptionsContentPanel, FlowLayout.LEFT), BorderLayout.CENTER);
            
            pagePanel = ComponentUtils.addToFlowLayout(ComponentUtils.addToVerticalBorderLayout(namePanel, creationOptionsPanel, null),
                    FlowLayout.CENTER);
        }
        return pagePanel;
    }

    @Override
    public void onNext() {

        WizardOptions wizardOptions = XWingWizard.getInstance().getWizardOptions();

        wizardOptions.setName(nameTextField.getText());
        
        for(Tournament t : CryodexController.getAllTournaments()){
            if(wizardOptions.getName().equals(t.getName())){
                JOptionPane.showMessageDialog(Main.getInstance(),
                        "Tournament name already used. Please pick something different.");
                return;
            }
        }

        if (splitCB.isSelected()) {
            int subs = Integer.parseInt(numSubs.getText());
            wizardOptions.setSplit(subs);
        }

        if (progressionCut.isSelected()) {
            XWingWizard.getInstance().setCurrentPage(new ProgressionCutPage());
        } else if (mergeCB.isSelected()) {
            XWingWizard.getInstance().setCurrentPage(new MergeTournamentSelectionPage());
        } else {
            XWingWizard.getInstance().setCurrentPage(new PlayerSelectionPage());
        }
    }

    @Override
    public void onPrevious() {
        // Do nothing
    }

    @Override
    public void onFinish() {
        // Do nothing
    }
}