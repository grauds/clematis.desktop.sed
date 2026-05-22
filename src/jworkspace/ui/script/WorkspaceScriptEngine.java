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

import com.hyperrealm.kiwi.io.StreamUtils;
import com.hyperrealm.kiwi.util.ResourceLoader;
import jworkspace.WorkspaceResourceAnchor;
import jworkspace.kernel.Workspace;
import jworkspace.ui.IView;
import jworkspace.ui.WorkspaceClassCache;
import jworkspace.ui.cpanel.CButton;
import jworkspace.ui.editor.LangResource;
import jworkspace.ui.views.DefaultCompoundView;
import kiwi.util.plugin.Plugin;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Hashtable;

/**
 * Workspace script engine.
 */
public class WorkspaceScriptEngine extends DefaultCompoundView implements ActionListener {

    /**
     * Parent plugin
     */
    private Plugin plugin = null;
    /**
     * Save path. Relative to user.home
     */

    private String path = "";
    /**
     * Commands
     */
    public static final String SHOW = "SHOW";
    public static final String ADD_SCRIPT = "ADD_SCRIPT";
    public static final String DELETE_SCRIPT = "DELETE_SCRIPT";
    public static final String EDIT_SCRIPT = "EDIT_SCRIPT";
    public static final String VIEW_HELP = "VIEW_HELP";

    public WorkspaceScriptEngine() {
        super();
    }

    public WorkspaceScriptEngine(Plugin plugin)
    {
        this();
        this.plugin = plugin;
    }

    public void actionPerformed(java.awt.event.ActionEvent e) {
        String command = e.getActionCommand();
        if (command.equals(SHOW)) {
            console();
        } else if (command.equals(WorkspaceScriptEngine.ADD_SCRIPT)) {
            ScriptEditor editor = new ScriptEditor();
            /**
             * Send message to workspace gui with request to add to layout?
             */
            Hashtable lparam = new Hashtable();
            lparam.put("view", editor);
            lparam.put("display", new Boolean(true));
            lparam.put("register", new Boolean(false));
            Workspace.fireEvent(new Integer(DISPLAY_VIEW_EVENT), lparam, null);
        } else if (command.equals(WorkspaceScriptEngine.EDIT_SCRIPT)) {
            JFileChooser chooser = WorkspaceClassCache.getFileChooser(LangResource.getString("message#113"),
                    new String[]{"bsh"}, LangResource.getString("message#154"));
            chooser.setCurrentDirectory(new File(System.getProperty("user.dir") +
                    File.separator + "bsh" + File.separator + "commands"));
            chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            chooser.setMultiSelectionEnabled(false);

            if (chooser.showOpenDialog(Workspace.getUI().getFrame()) != JFileChooser.APPROVE_OPTION) {
                return;
            }

            File file = chooser.getSelectedFile();
            try {
                java.io.InputStream is = new java.io.FileInputStream(file);
                byte data[] = StreamUtils.readStreamToByteArray(is);
                is.close();

                ScriptEditor editor = new ScriptEditor(new String(data), file);
                /**
                 * Send message to workspace gui with request to add to layout
                 */
                Hashtable lparam = new Hashtable();
                lparam.put("view", editor);
                lparam.put("display", new Boolean(true));
                lparam.put("register", new Boolean(false));
                Workspace.fireEvent(DISPLAY_VIEW_EVENT, lparam, null);
            } catch (java.io.IOException ex) {
                Workspace.logException(LangResource.getString("message#269") + ex.toString());
                JOptionPane.showMessageDialog(Workspace.getUI().getFrame(),
                        LangResource.getString("message#269") + ex.toString());
            }
        } else if (command.equals(WorkspaceScriptEngine.DELETE_SCRIPT)) {
            JFileChooser chooser = WorkspaceClassCache.getFileChooser(LangResource.getString("message#261"),
                    new String[]{"bsh"}, LangResource.getString("message#154"));
            chooser.setCurrentDirectory(new File(System.getProperty("user.dir") +
                    File.separator + "bsh" + File.separator + "commands"));
            chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            chooser.setMultiSelectionEnabled(true);

            if (chooser.showOpenDialog(Workspace.getUI().getFrame())
                    != JFileChooser.APPROVE_OPTION)
                return;

            File[] files = chooser.getSelectedFiles();

            if (JOptionPane.showConfirmDialog(Workspace.getUI().getFrame(),
                    LangResource.getString("message#254"),
                    LangResource.getString("message#255"), JOptionPane.YES_NO_OPTION) ==
                    JOptionPane.YES_OPTION) {
                for (int i = 0; i < files.length; i++) {
                    files[i].delete();
                }
            }
        }
    }

    /**
     * Shows console
     */
    public void console() {
        ScriptConsole console = new ScriptConsole();
        JInternalFrame nest = new JInternalFrame(console.getName(),
                true, true, true, true);
        nest.getContentPane().add(console);
        Image icon = new ResourceLoader(WorkspaceScriptEngine.class)
                .getResourceAsImage("images/script.png");

        nest.setFrameIcon(new ImageIcon(icon.getScaledInstance(18, 18, Image.SCALE_DEFAULT)));

        /**
         * Send message to workspace gui with request
         * to add to layout?
         */
        Hashtable lparam = new Hashtable();
        lparam.put("view", nest);
        lparam.put("display", new Boolean(true));
        lparam.put("register", new Boolean(isUnique()));
        Workspace.fireEvent(DISPLAY_VIEW_EVENT, lparam, null);
    }

    /**
     * Create component from the scratch. Used for
     * default assemble of ui components.
     */
    public void create() {
    }

    /**
     * Return buttons for control panel
     */
    public CButton[] getButtons() {
        /**
         * Start button.
         */
        Image normal = new ResourceLoader(WorkspaceScriptEngine.class)
                .getResourceAsImage("images/script.png");
        Image hover = new ResourceLoader(WorkspaceScriptEngine.class)
                .getResourceAsImage("images/script.png");

        CButton b_show = CButton.create(this, new ImageIcon(normal),
                new ImageIcon(hover), WorkspaceScriptEngine.SHOW, LangResource.getString("message#257"));

        /**
         * Editor button.
         */
        normal = new ResourceLoader(WorkspaceScriptEngine.class)
                .getResourceAsImage("images/editor.png");
        hover = new ResourceLoader(WorkspaceScriptEngine.class)
                .getResourceAsImage("images/editor.png");

        CButton b_editor = CButton.create(this, new ImageIcon(normal),
                new ImageIcon(hover), WorkspaceScriptEngine.ADD_SCRIPT, LangResource.getString("message#259"));

        return new CButton[]{b_show, b_editor};
    }

    /**
     * Get menu for installer.
     * Still there is no menus.
     */
    public JMenu[] getMenu() {
        return null;
    }

    /**
     * Get option panel for installer
     */
    public JPanel[] getOptionPanels() {
        return null;
    }

    /**
     * Script engine cannot be modified.
     */
    public boolean isModified() {
        return false;
    }

    /**
     * Returns relative path for saving component data.
     */
    public String getPath() {
        return path;
    }

    /**
     * Set this flag to true, if you want component
     * to be unique among all workspace views.
     * This component will be registered.
     *
     * @return boolean
     */
    public boolean isUnique() {
        return false;
    }

    public void activated(boolean flag) {
    }

    public void load() throws java.io.IOException {
    }

    public void reset() {
    }

    public void save() throws java.io.IOException {
    }

    public void setPath(String path) {
        this.path = new String(path);
    }

    public void update() {
    }

}