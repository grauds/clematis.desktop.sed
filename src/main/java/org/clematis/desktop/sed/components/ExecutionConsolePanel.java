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
import java.awt.Font;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JToolBar;
import javax.swing.SwingWorker;


public class ExecutionConsolePanel extends JPanel {
    private final JTextArea multilineInputField;
    private final JTextArea consoleArea;
    private final StatusPanel statusPanel;

    @SuppressWarnings("checkstyle:MagicNumber")
    public ExecutionConsolePanel(StatusPanel statusPanel) {
        this.statusPanel = statusPanel;
        setLayout(new BorderLayout());

        JSplitPane executionSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        executionSplit.setOneTouchExpandable(true);
        executionSplit.setContinuousLayout(true);
        executionSplit.setResizeWeight(0.40);

        // Multiline Input Pad
        multilineInputField = new JTextArea(4, 80);
        JScrollPane inputScroll = new JScrollPane(multilineInputField);
        inputScroll.setBorder(BorderFactory.createTitledBorder("Program Multiline Input Pad"));

        // Execution Logs Panel
        JPanel consolePanel = new JPanel(new BorderLayout());
        JToolBar runConsoleBar = new JToolBar();
        runConsoleBar.setFloatable(false);

        consoleArea = new JTextArea(6, 80);
        consoleArea.setEditable(false);
        JScrollPane consoleScroll = new JScrollPane(consoleArea);
        consoleScroll.setBorder(BorderFactory.createTitledBorder("Execution Runtime Logs"));

        JButton clearBtn = new JButton("Clear Logs");
        clearBtn.addActionListener(_ -> consoleArea.setText(""));
        runConsoleBar.add(clearBtn);

        consolePanel.add(runConsoleBar, BorderLayout.NORTH);
        consolePanel.add(consoleScroll, BorderLayout.CENTER);

        executionSplit.setLeftComponent(inputScroll);
        executionSplit.setRightComponent(consolePanel);
        add(executionSplit, BorderLayout.CENTER);
    }

    public void updateFont(Font font) {
        multilineInputField.setFont(font);
        consoleArea.setFont(font);
    }

    /**
     * Spawns an isolated background process thread to run the target code safely.
     */
    @SuppressWarnings("checkstyle:AnonInnerLength")
    public void runCodePipeline(File currentSourceFile, String codeBuffer) {
        if (currentSourceFile == null) {
            JOptionPane.showMessageDialog(this,
                "Please Save your file to disk before trying to execute it.",
                "File Not Saved",
                JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        consoleArea.setText("Compiling and Running program...\n");
        statusPanel.setProcessRunning(true);

        final String rawInputTextLines = multilineInputField.getText();

        SwingWorker<Void, String> worker = new SwingWorker<>() {
            @SuppressWarnings("checkstyle:MultipleStringLiterals")
            @Override
            protected Void doInBackground() throws Exception {
                Files.writeString(currentSourceFile.toPath(), codeBuffer);

                String javaBin = System.getProperty("java.home") + File.separator + "bin" + File.separator + "java";
                ProcessBuilder processBuilder = new ProcessBuilder(javaBin, currentSourceFile.getAbsolutePath());
                processBuilder.redirectErrorStream(true);
                Process process = processBuilder.start();

                try (BufferedWriter stdin = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));
                     BufferedReader stdout = new BufferedReader(new InputStreamReader(process.getInputStream()))) {

                    if (!rawInputTextLines.isEmpty()) {
                        stdin.write(rawInputTextLines);
                        if (!rawInputTextLines.endsWith("\n")) {
                            stdin.newLine();
                        }
                        stdin.flush();
                    }

                    String outputLine;
                    while ((outputLine = stdout.readLine()) != null) {
                        publish(outputLine);
                    }
                }
                int exitCode = process.waitFor();
                publish("\nProcess finished with exit code " + exitCode);
                return null;
            }

            @Override
            protected void process(List<String> chunks) {
                for (String chunk : chunks) {
                    consoleArea.append(chunk + "\n");
                }
            }

            @Override
            protected void done() {
                statusPanel.setProcessRunning(false); // Switch circle back to transparent
            }
        };
        worker.execute();
    }
}
