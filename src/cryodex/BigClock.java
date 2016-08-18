package cryodex;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Graphics;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class BigClock extends JFrame{
	
	private static BigClock instance;
	
	public static BigClock getInstance() {
		if (instance == null) {

			instance = new BigClock();
			instance.setSize(700, 700);
			instance.setExtendedState(Frame.MAXIMIZED_BOTH);
		}

		return instance;
	}
	
	private static final long serialVersionUID = 1L;
	private JLabel label;
	
	public BigClock() {
		super("Clock");

		setLayout(new BorderLayout());
		
		JPanel mainPanel = (JPanel) this.getContentPane();
		
		mainPanel.add(getBigClockLabel(), BorderLayout.CENTER);
		
	}

	@Override
	public void paint(Graphics g) {
		super.paint(g);
		
		Font labelFont = getBigClockLabel().getFont();
		String labelText = getBigClockLabel().getText();

		int stringWidth = getBigClockLabel().getFontMetrics(labelFont).stringWidth(labelText);
		int componentWidth = getBigClockLabel().getWidth();

		// Find out how much the font can grow in width.
		double widthRatio = (double)componentWidth / (double)stringWidth;

		int newFontSize = (int)(labelFont.getSize() * widthRatio);
		int componentHeight = getBigClockLabel().getHeight();

		// Pick a new font size so it will not be larger than the height of label.
		int fontSizeToUse = Math.min(newFontSize, componentHeight)-1;

		// Set the label's font size to the newly determined size.
		getBigClockLabel().setFont(new Font(labelFont.getName(), Font.PLAIN, fontSizeToUse));
	}
	
	public JLabel getBigClockLabel(){
		if(label == null){
			label = new JLabel();
		}
		
		return label;
	}
	
}
