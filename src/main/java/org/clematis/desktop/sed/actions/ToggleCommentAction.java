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
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import static org.clematis.desktop.sed.SourceEditor.PREFIX;
import org.clematis.desktop.sed.SourceEditor;

import jworkspace.ui.api.dialog.StackTraceError;

public class ToggleCommentAction extends AbstractAction {

    private final SourceEditor editor;

    public ToggleCommentAction(SourceEditor editor) {
        this.editor = editor;
        putValue(NAME, "Comment Current Line");
        putValue(ACTION_COMMAND_KEY, "toggle_comments");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        try {
            int caret = editor.getTextArea().getCaretPosition();
            int line = editor.getTextArea().getLineOfOffset(caret);
            int start = editor.getTextArea().getLineStartOffset(line);
            String lineText = editor.getTextArea().getText(
                start,
                editor.getTextArea().getLineEndOffset(line) - start
            );
            if (lineText.trim().startsWith(PREFIX)) {
                editor.getTextArea().replaceRange("",
                    start + lineText.indexOf(PREFIX),
                    start + lineText.indexOf(PREFIX) + 2);
            } else {
                editor.getTextArea().insert(PREFIX, start);
            }
        } catch (Exception ex) {
            StackTraceError.exception(editor,
                "Error toggling comment",
                ex
            );
        }
    }
}
