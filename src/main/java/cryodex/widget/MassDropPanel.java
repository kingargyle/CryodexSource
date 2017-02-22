package cryodex.widget;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import cryodex.CryodexController;
import cryodex.Main;

public class MassDropPanel extends JDialog{

    private static final long serialVersionUID = 1L;

    JLabel minPointsLabel;
    JLabel maxPlayersLabel;
    
    JTextField minPointsTF;
    JTextField maxPlayersTF;
    
    JButton ok;
    JButton cancel;
    
    public MassDropPanel() {
        super(Main.getInstance(), "Mass Drop Players", true);
        
init();
buildDialog();

    }
    
    private void init(){
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
                
                try{
                minScore = Integer.parseInt(minPointsTF.getText());
                maxPlayers = Integer.parseInt(maxPlayersTF.getText());
                } catch (Exception e){
                    MassDropPanel.this.setVisible(false);
                    return;
                }
                
                dropPlayers(minScore, maxPlayers);

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
    
    private void buildDialog(){
        
        JPanel mainPanel = new JPanel(new BorderLayout());

        mainPanel.add(ComponentUtils.addToHorizontalBorderLayout(minPointsLabel,
                minPointsTF, null), BorderLayout.NORTH);
        
        mainPanel.add(ComponentUtils.addToHorizontalBorderLayout(maxPlayersLabel,
                maxPlayersTF, null), BorderLayout.CENTER);
        
        mainPanel.add(ComponentUtils.addToHorizontalBorderLayout(ok, null,
                cancel), BorderLayout.SOUTH);
        
        this.add(mainPanel);

        MassDropPanel.this
                .setLocationRelativeTo(Main.getInstance());
        MassDropPanel.this.pack();
        this.setMinimumSize(new Dimension(200, 0));
    }
    
    public void dropPlayers(int minScore, int maxPlayers){
        CryodexController.getActiveTournament().massDropPlayers(minScore, maxPlayers);
    }
}
