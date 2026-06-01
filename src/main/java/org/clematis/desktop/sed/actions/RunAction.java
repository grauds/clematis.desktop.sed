package org.clematis.desktop.sed.actions;
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
import java.awt.Component;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.function.Supplier;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

import org.clematis.desktop.sed.CodeExecutionRunner;
import org.clematis.desktop.sed.WorkspaceScriptView;

public class RunAction extends AbstractAction {

    Image run = WorkspaceScriptView.getResourceManager().getImage("run.gif");
    Image stop = WorkspaceScriptView.getResourceManager().getImage("delete.gif");

    private final CodeExecutionRunner runner;
    private final Component parentComponent;
    private final Supplier<File> fileSupplier;
    private final Supplier<String> codeSupplier;
    private final Supplier<String> inputSupplier;

    @SuppressWarnings("checkstyle:MultipleStringLiterals")
    public RunAction(
        Component parentComponent,
        CodeExecutionRunner runner,
        Supplier<File> fileSupplier,
        Supplier<String> codeSupplier,
        Supplier<String> inputSupplier
    ) {

        super("Run Code");
        this.parentComponent = parentComponent;
        this.runner = runner;
        this.fileSupplier = fileSupplier;
        this.codeSupplier = codeSupplier;
        this.inputSupplier = inputSupplier;
        updateState(false);
    }

    @SuppressWarnings("checkstyle:ReturnCount")
    @Override
    public void actionPerformed(ActionEvent e) {
        if (runner.isRunning()) {
            runner.stopCurrentPipeline();
            return;
        }

        File file = fileSupplier.get();
        if (file == null) {
            JOptionPane.showMessageDialog(parentComponent,
                "Please save your file to disk before trying to execute it.",
                "File Not Saved",
                JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        runner.runCodePipeline(file, codeSupplier.get(), inputSupplier.get());
    }

    /**
     * Updates the text label dynamically based on running state.
     */
    public void updateState(boolean isRunning) {
        if (isRunning) {
            putValue(NAME, "Stop");
            putValue(Action.SMALL_ICON, new ImageIcon(stop));
        } else {
            putValue(NAME, "Run Code");
            putValue(Action.SMALL_ICON, new ImageIcon(run));
        }
    }
}
