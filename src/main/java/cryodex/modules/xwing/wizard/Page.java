package cryodex.modules.xwing.wizard;

import javax.swing.JPanel;

public interface Page {
        public JPanel getPanel();

        public void onNext();

        public void onPrevious();

        public void onFinish();
    }