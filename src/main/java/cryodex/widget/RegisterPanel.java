package cryodex.widget;

import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.*;

import cryodex.CryodexController;
import cryodex.Player;
import cryodex.modules.Module;

/**
 * Panel for the creation of players. Also contains the donation link.
 * 
 * @author cbrown
 * 
 */
public class RegisterPanel extends JPanel {

    private static final long serialVersionUID = 1L;
    private static final String filterHint = "Filter Player List";

    private JButton saveButton;
    private JButton deleteButton;
    private JButton cancelButton;
    private JList<Player> playerList;
    private JTextField nameField;
    private DefaultListModel<Player> userModel;
    private JLabel counterLabel;
    private JLabel donationLabel;

    private JTextField groupField;
    private JTextField emailField;
    private JPanel playerInfoPanel;
    private JPanel playerPanel;

    private JTextField playerSearchField;
    private JButton clearFilter;

    private boolean filtering = false;

    public RegisterPanel() {
        super(new BorderLayout());

        this.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        // Panel set up to push everything to the top
        this.add(getPlayerPanel(), BorderLayout.NORTH);
        this.add(new JPanel(), BorderLayout.CENTER);
    }

    /**
     * Main panel layout
     * 
     * @return
     */
    private JPanel getPlayerPanel() {
        if (playerPanel == null) {
            playerPanel = new JPanel(new BorderLayout());

            JLabel playersTitle = new JLabel("<html><b>Player Info</b></html>");
            playersTitle.setFont(new Font(playersTitle.getFont().getName(), playersTitle.getFont().getStyle(), 20));

            JScrollPane listScroller = new JScrollPane(getPlayerList());
            listScroller.setPreferredSize(new Dimension(150, 250));

            JPanel labelPanel = ComponentUtils.addToVerticalBorderLayout(getCounterLabel(), getDonationLabel(), null);

            JPanel southPanel = ComponentUtils.addToVerticalBorderLayout(null, listScroller, labelPanel);

            playerPanel.add(playersTitle, BorderLayout.NORTH);
            playerPanel.add(getPlayerInfoPanel(), BorderLayout.CENTER);
            playerPanel.add(southPanel, BorderLayout.SOUTH);
        }

        return playerPanel;
    }

    /**
     * Information input
     * 
     * @return
     */
    private JPanel getPlayerInfoPanel() {
        if (playerInfoPanel == null) {
            playerInfoPanel = new JPanel(new GridBagLayout());

            GridBagConstraints gbc = new GridBagConstraints();

            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.fill = GridBagConstraints.BOTH;
            gbc.weightx = 1;
            gbc.anchor = GridBagConstraints.WEST;
            playerInfoPanel.add(new JLabel("Name"), gbc);

            gbc.gridy++;
            gbc.anchor = GridBagConstraints.EAST;
            playerInfoPanel.add(getNameField(), gbc);

            gbc.gridy++;
            playerInfoPanel.add(new JLabel("Group Name"), gbc);

            gbc.gridy++;
            playerInfoPanel.add(getGroupNameField(), gbc);

            gbc.gridy++;
            playerInfoPanel.add(new JLabel("Email Address"), gbc);

            gbc.gridy++;
            playerInfoPanel.add(getEmailField(), gbc);

            for (Module m : CryodexController.getModules()) {
                gbc.gridy++;
                playerInfoPanel.add(m.getRegistration().getPanel(), gbc);
            }

            gbc.gridy++;
            playerInfoPanel.add(ComponentUtils.addToHorizontalBorderLayout(getSaveButton(), getCancelButton(), getDeleteButton()), gbc);

            gbc.gridy++;
            playerInfoPanel.add(ComponentUtils.addToHorizontalBorderLayout(getPlayerFilterTextField(), null, getClearFilterButton()), gbc);

        }
        return playerInfoPanel;
    }

    private JTextField getNameField() {
        if (nameField == null) {
            nameField = new JTextField();
        }
        return nameField;
    }

    private JTextField getGroupNameField() {
        if (groupField == null) {
            groupField = new JTextField();
        }
        return groupField;
    }

    private JTextField getEmailField() {
        if (emailField == null) {
            emailField = new JTextField();
        }
        return emailField;
    }

    private JButton getSaveButton() {

        if (saveButton == null) {

            saveButton = new JButton("Save");

            saveButton.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent arg0) {
                    // /////////////////////////////
                    // Update general information
                    // /////////////////////////////
                    String name = getNameField().getText();
                    String groupName = getGroupNameField().getText();
                    String email = getEmailField().getText();

                    if (name == null || name.isEmpty()) {
                        JOptionPane.showMessageDialog((Component) null, "Name is required", "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    // Create new player or get previous player instance
                    Player player;
                    if (getPlayerList().getSelectedIndex() == -1) {
                        for (Player p : CryodexController.getPlayers()) {
                            if (p.getName().equals(name)) {
                                JOptionPane.showMessageDialog((Component) null, "This player name already exists.", "Error",
                                        JOptionPane.ERROR_MESSAGE);
                                return;
                            }
                        }

                        player = new Player();
                        CryodexController.getPlayers().add(player);
                        getUserModel().addElement(player);
                        sortPlayer();

                    } else {
                        player = getPlayerList().getSelectedValue();
                    }

                    player.setName(name);
                    if (groupName != null) {
                        player.setGroupName(groupName);
                    }
                    player.setEmail(email);

                    for (Module m : CryodexController.getModules()) {
                        m.getRegistration().save(player);
                    }

                    clearFields();
                    CryodexController.saveData();

                    EventQueue.invokeLater(new Runnable() {

                        @Override
                        public void run() {
                            getNameField().grabFocus();
                            getNameField().requestFocus();
                        }
                    });

                    updateCounterLabel();
                }
            });
        }
        return saveButton;
    }

    private JButton getDeleteButton() {

        if (deleteButton == null) {
            deleteButton = new JButton("Delete");
            deleteButton.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent arg0) {
                    CryodexController.getPlayers().remove(getPlayerList().getSelectedValue());

                    getUserModel().removeElement(getPlayerList().getSelectedValue());

                    clearFields();
                    CryodexController.saveData();

                    updateCounterLabel();
                    
                    // Fix to prevent the filter textbox from stealing focus after a delete
                    playerSearchField.transferFocus();
                }

            });
            deleteButton.setEnabled(false);
        }

        return deleteButton;
    }

    private JButton getCancelButton() {
        if (cancelButton == null) {
            cancelButton = new JButton("Cancel");
            cancelButton.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    clearFields();

                    updateCounterLabel();
                }
            });
        }
        return cancelButton;
    }

    private void clearFields() {
        getNameField().setText("");
        getPlayerList().clearSelection();
        getDeleteButton().setEnabled(false);
        getNameField().setRequestFocusEnabled(true);
        getGroupNameField().setText("");
        getEmailField().setText("");

        for (Module m : CryodexController.getModules()) {
            m.getRegistration().clearFields();
        }
    }

    public void addPlayers(List<Player> players) {
        for (Player p : players) {
            getUserModel().addElement(p);
        }
        sortPlayer();
    }

    public void sortPlayer() {
        List<Player> players = Collections.list(getUserModel().elements());
        Collections.sort(players);

        getUserModel().removeAllElements();

        for (Player p : players) {
            getUserModel().addElement(p);
        }
    }

    private DefaultListModel<Player> getUserModel() {
        if (userModel == null) {
            userModel = new DefaultListModel<Player>();
            userModel.addListDataListener(new ListDataListener() {

                @Override
                public void intervalRemoved(ListDataEvent e) {
                    updateCounterLabel();
                }

                @Override
                public void intervalAdded(ListDataEvent e) {
                    updateCounterLabel();
                }

                @Override
                public void contentsChanged(ListDataEvent e) {
                    updateCounterLabel();
                }
            });
        }

        return userModel;
    }

    private JList<Player> getPlayerList() {
        if (playerList == null) {
            playerList = new JList<Player>(getUserModel());
            playerList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            playerList.setLayoutOrientation(JList.VERTICAL);
            playerList.setVisibleRowCount(-1);
            playerList.addListSelectionListener(new ListSelectionListener() {

                @Override
                public void valueChanged(ListSelectionEvent arg0) {
                    Player player = playerList.getSelectedValue();

                    if (player == null) {
                        getDeleteButton().setEnabled(false);
                    } else {
                        getDeleteButton().setEnabled(true);
                        getNameField().setText(player.getName());
                        getGroupNameField().setText(player.getGroupName());
                        getEmailField().setText(player.getEmail());

                        for (Module m : CryodexController.getModules()) {
                            m.getRegistration().load(player);
                        }
                    }

                    updateCounterLabel();
                }
            });
        }

        return playerList;
    }

    public void updateCounterLabel() {
        getCounterLabel().setText("Player Count: " + getUserModel().getSize());
    }

    public JLabel getCounterLabel() {
        if (counterLabel == null) {
            counterLabel = new JLabel();
        }

        return counterLabel;
    }

    public JLabel getDonationLabel() {
        if (donationLabel == null) {
            donationLabel = new JLabel("<html><a href=\"\">Donate To Cryodex</a></html>");
            donationLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
            donationLabel.addMouseListener(new MouseListener() {

                @Override
                public void mouseClicked(MouseEvent arg0) {
                    CryodexController.sendDonation();
                }

                @Override
                public void mouseEntered(MouseEvent arg0) {
                }

                @Override
                public void mouseExited(MouseEvent arg0) {
                }

                @Override
                public void mousePressed(MouseEvent arg0) {
                }

                @Override
                public void mouseReleased(MouseEvent arg0) {
                }

            });
        }
        return donationLabel;
    }

    public void registerButton() {
        this.getRootPane().setDefaultButton(getSaveButton());
    }

    public void importPlayers(List<Player> players) {
        for (Player p : players) {
            CryodexController.getPlayers().add(p);
            getUserModel().addElement(p);
        }
        sortPlayer();
        CryodexController.saveData();
    }

    public JTextField getPlayerFilterTextField() {
        if (playerSearchField == null) {
            playerSearchField = new JTextField(filterHint, 10);
            playerSearchField.addFocusListener(new FocusListener() {
                @Override
                public void focusGained(FocusEvent e) {
                    if (playerSearchField.getText().equals(filterHint)) {
                        playerSearchField.setText("");
                    }
                }

                @Override
                public void focusLost(FocusEvent e) {
                    if (playerSearchField.getText().trim().equals("")) {
                        playerSearchField.setText(filterHint);
                    }
                }
            });

            playerSearchField.getDocument().addDocumentListener(new DocumentListener() {
                @Override
                public void insertUpdate(DocumentEvent e) {
                    filterPlayerList(playerSearchField.getText().trim().toLowerCase());
                }

                @Override
                public void removeUpdate(DocumentEvent e) {
                    filterPlayerList(playerSearchField.getText().trim().toLowerCase());
                }

                @Override
                public void changedUpdate(DocumentEvent e) {
                    filterPlayerList(playerSearchField.getText().trim().toLowerCase());
                }
            });
        }
        return playerSearchField;
    }

    /**
     * Filter list to only include elements whose toString function contains the filterText. Elements that have a word
     * that starts with the filter text are given priority.
     */
    private void filterPlayerList(String filterText) {

        // Prevent the function from multiple simultaneous executions
        if (!filtering) {
            filtering = true;

            List<Player> preFilteredPlayerList = new ArrayList<Player>(CryodexController.getPlayers());
            Collections.sort(preFilteredPlayerList);
            List<Player> filteredPlayerList = new ArrayList<>();

            getUserModel().removeAllElements();

            // Reset list for blank filter
            if (filterText.equals("") || filterText.equalsIgnoreCase(filterHint)) {
                for (Player element : preFilteredPlayerList) {
                    getUserModel().addElement(element);
                }
                updateCounterLabel();
            } else {

                filteredPlayerList.clear();

                // Check the beginning of each word
                for (Player element : preFilteredPlayerList) {
                    String[] name = element.toString().toLowerCase().split(" ");
                    for (String s : name) {
                        if (s.startsWith(filterText)) {
                            filteredPlayerList.add(element);
                            break;
                        }
                    }
                }

                // Check if the element contains the text
                for (Player element : preFilteredPlayerList) {
                    if (element.toString().contains(filterText) && !filteredPlayerList.contains(element)) {
                        filteredPlayerList.add(element);
                    }
                }

                // Add all matching elements to the list
                for (Player element : filteredPlayerList) {
                    getUserModel().addElement(element);
                }
                updateCounterLabel();
            }
            filtering = false;
        }
    }

    public JButton getClearFilterButton() {

        if (clearFilter == null) {

            clearFilter = new JButton("Clear Filter");

            clearFilter.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent arg0) {
                    playerSearchField.setText(filterHint);
                    filterPlayerList(filterHint);
                }
            });
        }

        return clearFilter;
    }
}
