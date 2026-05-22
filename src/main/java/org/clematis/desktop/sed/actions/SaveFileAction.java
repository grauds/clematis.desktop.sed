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
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.nio.file.Files;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JFileChooser;
import javax.swing.KeyStroke;

import org.clematis.desktop.sed.SourceEditor;

import jworkspace.ui.api.dialog.StackTraceError;

public class SaveFileAction extends AbstractAction {

    private final SourceEditor editor;

    public SaveFileAction(SourceEditor editor) {
        this.editor = editor;
        putValue(Action.NAME, "Save File...");
        putValue(ACTION_COMMAND_KEY, "save");
        putValue(Action.ACCELERATOR_KEY,
            KeyStroke.getKeyStroke(KeyEvent.VK_S, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx())
        );
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (editor.getCurrentSourceFile() == null) {
            if (editor.getFileChooser().showSaveDialog(editor) == JFileChooser.APPROVE_OPTION) {
                editor.setCurrentSourceFile(
                    editor.getFileChooser().getSelectedFile()
                );
                editor.setCurrentFileName(
                    editor.getCurrentSourceFile().getName()
                );
            } else {
                return;
            }
        }
        try {
            Files.writeString(editor.getCurrentSourceFile().toPath(), editor.getTextArea().getText());
            editor.updateWindowFrameTitle();
            if (editor.isLiveCompilationEnabled()) {
                editor.getTextArea().forceReparsing(0);
            }
        } catch (Exception ex) {
            StackTraceError.exception(editor,
                "Error saving file",
                ex
            );
        }
    }
}
