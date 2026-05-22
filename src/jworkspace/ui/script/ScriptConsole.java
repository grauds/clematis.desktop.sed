package jworkspace.ui.script;

/* ----------------------------------------------------------------------------
   Java Workspace
   Copyright (C) 2000 Anton Troshin

   This file is part of Java Workspace.

   This program is free software; you can redistribute it and/or
   modify it under the terms of the GNU General Public License as
   published by the Free Software Foundation; either version 2 of
   the License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
   General Public License for more details.

   You should have received a copy of the GNU  General Public
   License along with this library; if not, write to the Free
   Software Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.

   Authors may be contacted at:

   tysinsh@comail.ru
   ----------------------------------------------------------------------------
*/

import bsh.Interpreter;
import bsh.util.JConsole;
import jworkspace.ui.editor.LangResource;

public class ScriptConsole extends JConsole {
    private boolean multiline = false;
    private static int counter = 1;
    private Interpreter interpreter = null;

    public ScriptConsole() {
        this.interpreter = new Interpreter(this);
        Thread thread = new Thread(this.interpreter);
        thread.start();
        this.setName(String.valueOf(String.valueOf((new StringBuffer(String.valueOf(String.valueOf(LangResource.getString("message#255"))))).append(" : ").append((new Integer(counter)).toString()))));
        ++counter;
    }

    public void setMultiline(boolean multiline) {
        this.multiline = multiline;
    }

    public boolean isMultiline() {
        return this.multiline;
    }
}