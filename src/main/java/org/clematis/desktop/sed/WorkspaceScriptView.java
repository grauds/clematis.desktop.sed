package org.clematis.desktop.sed;

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
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.util.HashMap;
import java.util.Map;

import javax.swing.ImageIcon;

import com.hyperrealm.kiwi.util.ResourceLoader;

import jworkspace.config.ServiceLocator;
import jworkspace.runtime.plugin.WorkspacePluginContext;
import jworkspace.ui.api.IView;
import jworkspace.ui.api.cpanel.CButton;
import jworkspace.ui.api.views.DefaultCompoundView;
import lombok.Getter;


public class WorkspaceScriptView extends DefaultCompoundView {
    /**
     * Editor properties
     */
    public static final String CK_FONT_FACE = "font.face",
        CK_FONT_SIZE = "font.size",
        CK_FONT_STYLE = "font.style",
        CK_TAB_SIZE = "tab.size";

    private final SourceEditor workspace = new SourceEditor();

    @Getter
    private final WorkspacePluginContext pluginContext;

    public WorkspaceScriptView(WorkspacePluginContext pluginContext) {
        super();
        this.pluginContext = pluginContext;
    }

    public void actionPerformed(ActionEvent e) {
        Map<String, Object> lparam = new HashMap<>();
        lparam.put("view", workspace);
        lparam.put("display", Boolean.TRUE);
        lparam.put("register", Boolean.TRUE);
        ServiceLocator.getInstance()
            .getEventsDispatcher().fireEvent(IView.DISPLAY_IN_DESKTOP_EVENT, lparam, null);
    }

    @SuppressWarnings("checkstyle:MultipleStringLiterals")
    public CButton[] getButtons() {

        Image normal = new ResourceLoader(WorkspaceScriptView.class)
            .getResourceAsImage("images/editor.png");
        Image hover = new ResourceLoader(WorkspaceScriptView.class)
            .getResourceAsImage("images/editor.png");

        CButton bEditor = CButton.create(
            this,
            new ImageIcon(normal),
            new ImageIcon(hover),
            SHOW,
            LangResource.getString("message#259")
        );

        return new CButton[] {bEditor};
    }

    @Override
    public void load() {
        this.workspace.setWorkingDirectory(
            this.pluginContext.getUserDir().toFile()
        );
    }

    @Override
    public void save() {

    }

    @Override
    public void reset() {

    }
}
