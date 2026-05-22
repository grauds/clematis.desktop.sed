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
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.function.Consumer;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import lombok.Setter;

@SuppressWarnings("checkstyle:MagicNumber")
public class StatusPanel extends JPanel {
    private final DefaultTableModel errorTableModel;
    private final JTable errorTable;
    private final JLabel statusSummaryLabel;
    private final ProcessStatusIndicator statusIndicator;
    @Setter
    private Consumer<Integer> onDiagnosticRowDoubleClicked; // Clean listener hookup

    public StatusPanel() {
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(100, 140));
        setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Color.GRAY));

        String[] columnHeaders = {"Classification", "Line", "Diagnostic Message Description Log Details"};
        errorTableModel = new DefaultTableModel(columnHeaders, 0) {
            @Override public boolean isCellEditable(int row, int col) {
                return false;
            }
        };

        errorTable = new JTable(errorTableModel);
        errorTable.getColumnModel().getColumn(0).setPreferredWidth(100);
        errorTable.getColumnModel().getColumn(1).setPreferredWidth(60);
        errorTable.getColumnModel().getColumn(2).setPreferredWidth(600);

        errorTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && onDiagnosticRowDoubleClicked != null) {
                    int row = errorTable.getSelectedRow();
                    if (row != -1) {
                        int targetLine = (int) errorTableModel.getValueAt(row, 1) - 1;
                        onDiagnosticRowDoubleClicked.accept(targetLine);
                    }
                }
            }
        });

        JPanel bottomRow = new JPanel(new BorderLayout());
        statusSummaryLabel = new JLabel(" Workspace Status: Ready. 0 Errors Pending.");
        statusSummaryLabel.setBorder(BorderFactory.createEmptyBorder(3, 5, 3, 5));

        statusIndicator = new ProcessStatusIndicator();

        bottomRow.add(statusSummaryLabel, BorderLayout.CENTER);
        bottomRow.add(statusIndicator, BorderLayout.EAST);

        add(new JScrollPane(errorTable), BorderLayout.CENTER);
        add(bottomRow, BorderLayout.SOUTH);
    }

    public void clearDiagnostics() {
        errorTableModel.setRowCount(0);
    }

    public void addDiagnostic(String classification, int line, String message) {
        errorTableModel.addRow(new Object[]{classification, line, message});
    }

    public void updateStatusSummary(String message) {
        statusSummaryLabel.setText(message);
    }

    public void setProcessRunning(boolean running) {
        statusIndicator.setRunning(running);
    }

    /**
     * Encapsulated micro-component for drawing the thread runtime ring marker
     */
    private static class ProcessStatusIndicator extends JComponent {
        private boolean isRunning = false;

        ProcessStatusIndicator() {
            setPreferredSize(new Dimension(30, 20));
        }

        public void setRunning(boolean running) {
            this.isRunning = running;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int diameter = 12;
            int x = (getWidth() - diameter) / 2;
            int y = (getHeight() - diameter) / 2;

            if (isRunning) {
                g2.setColor(new Color(46, 204, 113));
                g2.fillOval(x, y, diameter, diameter);
            } else {
                g2.setColor(Color.GRAY);
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawOval(x, y, diameter, diameter);
            }
            g2.dispose();
        }
    }
}
