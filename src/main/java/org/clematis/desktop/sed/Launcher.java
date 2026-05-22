package org.clematis.desktop.sed;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

class Launcher {
    private Launcher() {}
    static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {}

            JFrame frame = new JFrame("Java Source Editor - Untitled.java");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            SourceEditor workspace = new SourceEditor();
            frame.add(workspace);

            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}
