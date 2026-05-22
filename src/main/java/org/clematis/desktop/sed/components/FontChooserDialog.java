package org.clematis.desktop.sed.components;
/* ----------------------------------------------------------------------------
   Java Workspace
   Copyright (C) 2026 Anton Troshin

   This file is part of Java Workspace.

   This application is free software; you can redistribute it and/or
   modify it under the terms of the GNU Library General Public
   License as published by the Free Software Foundation; either
   version 2 of the License, or (at your option) any later version.

   This application is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
   Library General Public License for more details.

   You should have received a copy of the GNU Library General Public
   License along with this application; if not, write to the Free
   Software Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.

   The author may be contacted at:

   anton.troshin@gmail.com
  ----------------------------------------------------------------------------
*/
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;

import lombok.Getter;

public class FontChooserDialog extends JDialog {

    private final JList<String> fontList;
    private final JList<String> styleList;
    private final JList<Integer> sizeList;

    private final JLabel previewLabel;

    @Getter
    private boolean approved = false;

    @SuppressWarnings("checkstyle:MagicNumber")
    public FontChooserDialog(JFrame parent, Font currentFont) {
        super(parent, "Font Configuration Selector", true);
        setLayout(new BorderLayout(10, 10));
        setSize(480, 350); setLocationRelativeTo(parent);

        String[] fonts = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
        String[] styles = {"Plain", "Bold", "Italic", "Bold Italic"};
        Integer[] sizes = {9, 10, 11, 12, 13, 14, 16, 18, 20, 22, 24, 28, 32, 36};

        fontList = new JList<>(fonts);
        styleList = new JList<>(styles);
        sizeList = new JList<>(sizes);
        fontList.setSelectedValue(currentFont.getFamily(), true);
        styleList.setSelectedIndex(currentFont.getStyle());
        sizeList.setSelectedValue(currentFont.getSize(), true);

        JPanel selectorsPanel = new JPanel(new GridLayout(1, 3, 5, 5));
        selectorsPanel.add(new JScrollPane(fontList));
        selectorsPanel.add(new JScrollPane(styleList));
        selectorsPanel.add(new JScrollPane(sizeList));

        previewLabel = new JLabel("public class HelloWorld { ... }", SwingConstants.CENTER);
        previewLabel.setPreferredSize(new Dimension(100, 70));
        previewLabel.setBorder(BorderFactory.createTitledBorder("Visual Typography Preview"));

        fontList.addListSelectionListener(_ -> refreshPreview());
        styleList.addListSelectionListener(_ -> refreshPreview());
        sizeList.addListSelectionListener(_ -> refreshPreview());
        refreshPreview();

        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton okBtn = new JButton("Apply");
        JButton cancelBtn = new JButton("Cancel");
        okBtn.addActionListener(_ -> {
            approved = true; dispose(); }
        );
        cancelBtn.addActionListener(_ -> dispose());
        controlPanel.add(okBtn); controlPanel.add(cancelBtn);

        add(selectorsPanel, BorderLayout.CENTER);
        JPanel bottomLayout = new JPanel(new BorderLayout());
        bottomLayout.add(previewLabel, BorderLayout.CENTER);
        bottomLayout.add(controlPanel, BorderLayout.SOUTH);
        add(bottomLayout, BorderLayout.SOUTH);
    }

    @SuppressWarnings("checkstyle:MagicNumber")
    private void refreshPreview() {
        if (fontList.getSelectedValue() != null && sizeList.getSelectedValue() != null) {
            int idx = styleList.getSelectedIndex();
            int val = idx == 1 ? Font.BOLD : (idx == 2 ? Font.ITALIC
                                           : (idx == 3 ? Font.BOLD | Font.ITALIC : Font.PLAIN)
            );
            previewLabel.setFont(new Font(fontList.getSelectedValue(), val, sizeList.getSelectedValue()));
        }
    }

    public Font getSelectedFont() {
        return previewLabel.getFont();
    }
}

