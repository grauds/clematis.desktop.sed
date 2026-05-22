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
import java.awt.Insets;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.MissingResourceException;

import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.KeyStroke;

import org.clematis.desktop.sed.ResourceAnchor;
import org.clematis.desktop.sed.SourceEditor;

import com.hyperrealm.kiwi.ui.KButton;

import lombok.extern.java.Log;

@Log
public class EditorActionsCollection {
    /**
     * Action property label
     */
    public static final String ACTION_TYPE = "ACTION_TYPE";
    /**
     * Action property - this action work for editor
     */
    public static final String APPLICATION_ACTION = "APPLICATION_ACTION";
    /**
     * Action property - this action work for opened document only
     */
    public static final String DOCUMENT_ACTION = "DOCUMENT_ACTION";
    /**
     * Action property - this action work for selected fragment of
     * document
     */
    public static final String SELECTION_ACTION = "SELECTION_ACTION";
    /**
     * Action property - this action work for selected text element only
     */
    public static final String TEXT_ACTION = "TEXT_ACTION";
    /**
     * Suffix applied to the key used in resource file
     * lookups for an image.
     */
    public static final String IMAGE_SUFFIX = "Image";
    /**
     * Suffix applied to the key used in resource file
     * lookups for a label.
     */
    public static final String LABEL_SUFFIX = "Label";
    /**
     * Suffix applied to the key used in resource file
     * lookups for an action.
     */
    public static final String ACTION_SUFFIX = "Action";
    /**
     * Suffix applied to the key used in resource file
     * lookups for tooltip text.
     */
    public static final String TIP_SUFFIX = "Tooltip";

    protected final Map<String, Action> actions = new HashMap<>();

    public EditorActionsCollection(SourceEditor editor) {
        register(new NewFileAction(editor));
        register(new OpenFileAction(editor));
        register(new SaveFileAction(editor));
        register(new ToggleCommentAction(editor));
        register(new FontChooserAction(editor));
        register(new ToggleLiveCompilationAction(editor));
        register(new FindAction(editor));
        register(new ReplaceAction(editor));
    }

    protected void register(Action action) {
        String key = (String) action.getValue(Action.ACTION_COMMAND_KEY);
        actions.put(key, action);
    }

    protected String getResourceString(String nm) {
        String str;
        try {
            str = ResourceAnchor.getString(nm);
        } catch (MissingResourceException mre) {
            str = null;
        }
        return str;
    }

    protected URL getResource(String key) {
        String name = getResourceString(key);
        if (name != null) {
            return ResourceAnchor.class.getResource(name);
        }
        return null;
    }

    /**
     * Enable actions of specified type
     */
    public void enableActions(boolean flag, String type) {
        for (Action action : actions.values()) {
            String stype = (String) action.getValue(ACTION_TYPE);
            if (stype != null && stype.equals(type)) {
                action.setEnabled(flag);
            }
        }
    }

    public AbstractButton createToolbarButton(String key) {
        String astr = getResourceString(key);
        if (astr == null) {
            astr = key;
        }
        Action a = actions.get(astr);
        URL url = getResource(key + IMAGE_SUFFIX);
        AbstractButton b;

        if (a != null) {
            if (url != null) {
                b = new KButton(new ImageIcon(url));
            } else {
                b = new KButton((String) a.getValue(Action.NAME));
            }
            b.setRequestFocusEnabled(false);
            b.setMargin(new Insets(1, 1, 1, 1));
            b.setActionCommand(astr);
            b.addActionListener(a);
            b.setEnabled(a.isEnabled());
        } else {
            b = new KButton(key);
            b.setRequestFocusEnabled(false);
            b.setMargin(new Insets(1, 1, 1, 1));
            b.setEnabled(false);
        }
        String tip = getResourceString(key + TIP_SUFFIX);
        if (tip != null) {
            b.setToolTipText(tip);
        }
        return b;
    }

    protected void initKeyBindings(JComponent component) {
        InputMap im = component.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap am = component.getActionMap();

        for (Action action : actions.values()) {
            Object key = action.getValue(Action.ACTION_COMMAND_KEY);
            KeyStroke ks = (KeyStroke) action.getValue(Action.ACCELERATOR_KEY);

            am.put(key, action);

            if (ks != null) {
                im.put(ks, key);
            }
        }
    }


    public Action getAction(String key) {
        return this.actions.get(key);
    }
}
