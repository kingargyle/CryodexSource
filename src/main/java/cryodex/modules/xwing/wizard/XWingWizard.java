package cryodex.modules.xwing.wizard;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;

import cryodex.Language;
import cryodex.Main;
import cryodex.modules.xwing.wizard.common.MainPage;

public class XWingWizard extends JDialog {

    private static final long serialVersionUID = 1L;

    private static XWingWizard wizard;

    public static XWingWizard getInstance() {
        if (wizard == null) {
            wizard = new XWingWizard();
            wizard.setupWizard();
        }
        return wizard;
    }

    private WizardOptions wizardOptions = new WizardOptions();

    private JPanel mainPanel = null;
    private JPanel contentPanel = null;
    private JPanel buttonPanel = null;

    private JButton previousButton = null;
    private JButton nextButton = null;
    private JButton finishButton = null;
    private JButton cancelButton = null;

    private final List<Page> pages = new ArrayList<Page>();

    private XWingWizard() {
        super(Main.getInstance(), Language.tournament_wizard, true);
    }

    protected void setupWizard() {
        this.add(getMainPanel());
        setCurrentPage(new MainPage());
        XWingWizard.this.pack();
        this.setMinimumSize(new Dimension(450, 300));
    }

    @Override
    public void setVisible(boolean b) {
        super.setVisible(b);
        if (b) {
            wizardOptions = new WizardOptions();
            setCurrentPage(new MainPage());
            XWingWizard.this.pack();
            this.setMinimumSize(new Dimension(450, 300));
        }
    }

    public void setCurrentPage(Page page) {
        pages.add(page);

        getContentPanel().removeAll();
        getContentPanel().add(page.getPanel(), BorderLayout.CENTER);
        getContentPanel().validate();
        getContentPanel().repaint();
    }

    private Page getCurrentPage() {
        return pages.isEmpty() ? null : pages.get(pages.size() - 1);
    }

    private Page getPreviousPage() {
        return pages.size() > 1 ? pages.get(pages.size() - 2) : null;
    }

    public void goToPrevious() {

        if (getPreviousPage() != null) {
            pages.remove(pages.get(pages.size() - 1));

            getContentPanel().removeAll();
            getContentPanel().add(pages.get(pages.size() - 1).getPanel(), BorderLayout.CENTER);
            getContentPanel().validate();
            getContentPanel().repaint();
        } else {
            XWingWizard.this.setVisible(false);
        }
    }

    private JPanel getMainPanel() {
        if (mainPanel == null) {
            mainPanel = new JPanel(new BorderLayout());
            mainPanel.add(getContentPanel(), BorderLayout.CENTER);
            mainPanel.add(getButtonPanel(), BorderLayout.SOUTH);
        }

        return mainPanel;
    }

    private JPanel getContentPanel() {
        if (contentPanel == null) {
            contentPanel = new JPanel(new BorderLayout());
        }

        return contentPanel;
    }

    private JPanel getButtonPanel() {
        if (buttonPanel == null) {
            buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
            buttonPanel.add(getPreviousButton());
            buttonPanel.add(getNextButton());
            buttonPanel.add(getFinishButton());
            buttonPanel.add(getCancelButton());
        }

        return buttonPanel;
    }

    public void setButtonVisibility(Boolean previous, Boolean next, Boolean finish) {
        getPreviousButton().setVisible(previous == null ? false : previous);
        getPreviousButton().setEnabled(previous != null);

        getNextButton().setVisible(next == null ? false : next);
        getNextButton().setEnabled(next != null);

        getFinishButton().setVisible(finish == null ? false : finish);
        getFinishButton().setEnabled(finish != null);
    }

    private JButton getPreviousButton() {
        if (previousButton == null) {
            previousButton = new JButton(Language.previous);
            previousButton.setEnabled(false);
            previousButton.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    getCurrentPage().onPrevious();
                }
            });
        }

        return previousButton;
    }

    private JButton getNextButton() {
        if (nextButton == null) {
            nextButton = new JButton(Language.next);
            nextButton.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    getCurrentPage().onNext();
                }
            });
        }

        return nextButton;
    }

    private JButton getFinishButton() {
        if (finishButton == null) {
            finishButton = new JButton(Language.finish);
            finishButton.setVisible(false);
            finishButton.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    getCurrentPage().onFinish();
                }
            });
        }

        return finishButton;
    }

    private JButton getCancelButton() {
        if (cancelButton == null) {
            cancelButton = new JButton(Language.cancel);
            cancelButton.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent arg0) {
                    XWingWizard.this.setVisible(false);
                }
            });
        }

        return cancelButton;
    }

    public WizardOptions getWizardOptions() {
        return wizardOptions;
    }
}
