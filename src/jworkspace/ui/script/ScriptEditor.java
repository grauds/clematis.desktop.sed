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
import com.hyperrealm.kiwi.io.StreamUtils;
import com.hyperrealm.kiwi.util.ResourceLoader;
import jworkspace.kernel.Workspace;
import jworkspace.ui.WorkspaceClassCache;
import jworkspace.ui.api.action.UISwitchListener;
import jworkspace.ui.editor.LangResource;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rtextarea.RTextScrollPane;

import javax.swing.*;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;

/**
 * Simple colored scripts editor
 */
public class ScriptEditor extends JInternalFrame implements ActionListener,
        PropertyChangeListener {
    /**
     * Commands.
     */
    public static final String SAVE = "SAVE";
    public static final String SAVE_AS = "SAVE_AS";
    public static final String SELECT_ALL = "SELECT_ALL";
    public static final String CLEAR_ALL = "CLEAR_ALL";
    public static final String COPY = "COPY";
    public static final String CUT = "CUT";
    public static final String PASTE = "PASTE";
    public static final String OPEN = "OPEN";
    public static final String EVALUATE = "EVALUATE";
    public static final String EVALUATE_SELECTED = "EVALUATE_SELECTED";
    /**
     * Scroll pane for text area
     */
    private RTextScrollPane pane;
    /**
     * Syntax text area
     */
    private RSyntaxTextArea syntaxTextArea;
    /**
     * Current file
     */
    private File current_file = null;
    /**
     * Menu bar
     */
    private EditorMenuBar menuBar = new EditorMenuBar(this);
    /**
     * Popup menu
     */
    private EditorPopupMenu popup = new EditorPopupMenu(this);
    /**
     * Modified flag
     */
    private boolean modified = false;

    public void setModified(boolean modified) {
        this.modified = modified;
    }

    /**
     * Menu bar
     */
    class EditorMenuBar extends JMenuBar implements MenuListener {

        JMenu edit = new JMenu(LangResource.getString("message#555"));
        ActionListener listener = null;
        JMenuItem open = null;
        JMenuItem save = null;
        JMenuItem save_as = null;
        JMenuItem clear_all = null;
        JMenuItem select_all = null;
        JMenuItem copy = null;
        JMenuItem cut = null;
        JMenuItem paste = null;
        JMenuItem evaluate = null;
        JMenuItem evaluate_selected = null;

        EditorMenuBar(ActionListener listener) {
            super();
            this.listener = listener;
            createItems();
            edit.addMenuListener(this);
        }

        void createItems() {
            open = edit.add(createMenuItem(LangResource.getString("message#265"), OPEN, null));
            save = edit.add(createMenuItem(LangResource.getString("Save"), SAVE, null));
            save_as = edit.add(createMenuItem(LangResource.getString("message#260"), SAVE_AS, null));
            edit.addSeparator();
            select_all = edit.add(createMenuItem(LangResource.getString("message#126"), SELECT_ALL, null));
            clear_all = edit.add(createMenuItem(LangResource.getString("message#258"), CLEAR_ALL, null));
            edit.addSeparator();
            cut = edit.add(createMenuItem(LangResource.getString("Cut"), CUT, null));
            copy = edit.add(createMenuItem(LangResource.getString("Copy"), COPY, null));
            paste = edit.add(createMenuItem(LangResource.getString("Paste"), PASTE, null));
            edit.addSeparator();
            evaluate = edit.add(createMenuItem(LangResource.getString("Execute"), EVALUATE, null));
            evaluate_selected = edit.add(createMenuItem(LangResource.getString("message#256"), EVALUATE_SELECTED, null));
            add(edit);
        }

        JMenuItem createMenuItem(String name,
                                 String actionCommand, Icon icon) {
            JMenuItem menu_item = new JMenuItem(name,
                    icon);
            menu_item.addActionListener(listener);
            menu_item.setActionCommand(actionCommand);
            return menu_item;
        }

        public void menuSelected(MenuEvent e) {
            try {
                Object contents = getToolkit().getSystemClipboard().
                        getContents(this).getTransferData(DataFlavor.stringFlavor);
                if (contents == null) {
                    paste.setEnabled(false);
                } else if (contents instanceof String) {
                    paste.setEnabled(true);
                } else {
                    paste.setEnabled(false);
                }
            } catch (Exception ex) {
            }
            if (syntaxTextArea.getSelectedText() == null ||
                    syntaxTextArea.getSelectedText().equals("")) {
                copy.setEnabled(false);
                cut.setEnabled(false);
                evaluate_selected.setEnabled(false);
            } else {
                copy.setEnabled(true);
                cut.setEnabled(true);
                evaluate_selected.setEnabled(true);
            }
            if (isModified()) {
                save.setEnabled(true);
            } else {
                save.setEnabled(false);
            }
        }

        public void menuDeselected(MenuEvent e) {
        }

        public void menuCanceled(MenuEvent e) {
        }
    }

    class EditorPopupMenu extends JPopupMenu {

        ActionListener listener = null;
        JMenuItem open = null;
        JMenuItem save = null;
        JMenuItem save_as = null;
        JMenuItem clear_all = null;
        JMenuItem select_all = null;
        JMenuItem copy = null;
        JMenuItem cut = null;
        JMenuItem paste = null;
        JMenuItem evaluate = null;
        JMenuItem evaluate_selected = null;

        EditorPopupMenu(ActionListener listener) {
            super();
            this.listener = listener;
            createItems();
        }

        void createItems() {
            open = add(createMenuItem(LangResource.getString("message#265"), OPEN, null));
            save = add(createMenuItem(LangResource.getString("Save"), SAVE, null));
            save_as = add(createMenuItem(LangResource.getString("message#260"), SAVE_AS, null));
            addSeparator();
            select_all = add(createMenuItem(LangResource.getString("message#126"), SELECT_ALL, null));
            clear_all = add(createMenuItem(LangResource.getString("message#258"), CLEAR_ALL, null));
            addSeparator();
            cut = add(createMenuItem(LangResource.getString("Cut"), CUT, null));
            copy = add(createMenuItem(LangResource.getString("Copy"), COPY, null));
            paste = add(createMenuItem(LangResource.getString("Paste"), PASTE, null));
            addSeparator();
            evaluate = add(createMenuItem(LangResource.getString("Execute"), EVALUATE, null));
            evaluate_selected = add(createMenuItem(LangResource.getString("message#256"), EVALUATE_SELECTED, null));
        }

        JMenuItem createMenuItem(String name,
                                 String actionCommand, Icon icon) {
            JMenuItem menu_item = new JMenuItem(name,
                    icon);
            menu_item.addActionListener(listener);
            menu_item.setActionCommand(actionCommand);
            return menu_item;
        }

        public void setVisible(boolean flag) {
            try {
                Object contents = getToolkit().getSystemClipboard().
                        getContents(this).getTransferData(DataFlavor.stringFlavor);
                if (contents == null) {
                    paste.setEnabled(false);
                } else if (contents instanceof String) {
                    paste.setEnabled(true);
                } else {
                    paste.setEnabled(false);
                }
            } catch (Exception ex) {

            }

            if (syntaxTextArea.getSelectedText() == null ||
                    syntaxTextArea.getSelectedText().equals("")) {
                copy.setEnabled(false);
                cut.setEnabled(false);
                evaluate_selected.setEnabled(false);
            } else {
                copy.setEnabled(true);
                cut.setEnabled(true);
                evaluate_selected.setEnabled(true);
            }
            if (isModified()) {
                save.setEnabled(true);
            } else {
                save.setEnabled(false);
            }
            super.setVisible(flag);
        }
    }

    public ScriptEditor() {
        this("", null);
    }

    public ScriptEditor(String text, File current_file) {

        super(LangResource.getString("message#259"), true, true, true, true);

        this.current_file = current_file;
        this.setJMenuBar(this.menuBar);
        this.getContentPane().setBackground(Color.lightGray);
        this.getContentPane().setLayout(new BorderLayout());
        this.getContentPane().add(getTextScrollPane(), BorderLayout.CENTER);

        getSyntaxTextArea().setText(text);

        Image icon = new ResourceLoader(WorkspaceScriptEngine.class)
                .getResourceAsImage("images/editor.png");

        this.setFrameIcon(new ImageIcon(icon.getScaledInstance(18, 18, Image.SCALE_DEFAULT)));

        UIManager.addPropertyChangeListener(new UISwitchListener(popup));
        setDefaultCloseOperation(JInternalFrame.DO_NOTHING_ON_CLOSE);

        this.addComponentListener(new ComponentAdapter() {
            public void componentShown(ComponentEvent e) {
                update();
            }
        });
        syntaxTextArea.addPropertyChangeListener(this);
        this.addInternalFrameListener(new InternalFrameAdapter() {
            public void internalFrameClosing(InternalFrameEvent e) {
                handleDisposal(e);
            }
        });
    }

    public void actionPerformed(java.awt.event.ActionEvent e) {
        String command = e.getActionCommand();
        if (command.equals(OPEN)) {
            if (isModified()) {
                int result = JOptionPane.showConfirmDialog(Workspace.getUI().getFrame(),
                        LangResource.getString("message#266"),
                        LangResource.getString("message#1"), JOptionPane.YES_NO_CANCEL_OPTION);
                if (result == JOptionPane.OK_OPTION)
                    save();
            }

            setModified(false);
            update();

            JFileChooser chooser = WorkspaceClassCache.getFileChooser(LangResource.getString("message#113"),
                    new String[]{"bsh"}, LangResource.getString("message#154"));
            chooser.setCurrentDirectory(new File(System.getProperty("user.home")));
            chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            chooser.setMultiSelectionEnabled(false);

            if (chooser.showOpenDialog(Workspace.getUI().getFrame())
                    == JFileChooser.APPROVE_OPTION) {
                current_file = chooser.getSelectedFile();
                try {
                    java.io.InputStream is = new java.io.FileInputStream(current_file);
                    byte data[] = StreamUtils.readStreamToByteArray(is);
                    is.close();

                    syntaxTextArea.setText(new String(data));
                    update();
                } catch (java.io.IOException ex) {
                    Workspace.logException(LangResource.getString("message#269") +
                            ex.toString());
                    JOptionPane.showMessageDialog(Workspace.getUI().getFrame(),
                            LangResource.getString("message#269") + ex.toString());
                }
            }
        } else if (command.equals(SAVE)) {
            if (current_file == null)
                saveAs();
            else
                save();
        } else if (command.equals(SAVE_AS)) {
            saveAs();
        } else if (command.equals(CUT)) {
            syntaxTextArea.cut();
            setModified(true);
            update();
        } else if (command.equals(COPY)) {
            syntaxTextArea.copy();
            setModified(true);
            update();
        } else if (command.equals(PASTE)) {
            syntaxTextArea.paste();
            setModified(true);
            update();
        } else if (command.equals(SELECT_ALL)) {
            syntaxTextArea.selectAll();
        } else if (command.equals(CLEAR_ALL)) {
            syntaxTextArea.setText("");
            setModified(true);
            update();
        } else if (command.equals(EVALUATE)) {
            Object return_value = null;
            try {
                if (syntaxTextArea.getText() != null)
                    return_value = new Interpreter().eval(syntaxTextArea.getText());
            } catch (bsh.EvalError err) {
                JOptionPane.showMessageDialog(Workspace.getUI().getFrame(),
                        err.toString());
            }
        } else if (command.equals(EVALUATE_SELECTED)) {
            Object return_value = null;
            try {
                if (syntaxTextArea.getSelectedText() != null) {
                    return_value = new Interpreter().eval(syntaxTextArea.getSelectedText());
                }
            } catch (bsh.EvalError err) {
                JOptionPane.showMessageDialog(Workspace.getUI().getFrame(),
                        err.toString());
            }
        }
    }

    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getSource() == syntaxTextArea) {
            if (evt.getPropertyName().equals("MODIFIED")) {
                update();
            }
        }
    }

    public void handleDisposal(InternalFrameEvent e) {
        if (isModified() && e.getInternalFrame() == this) {
            String name = getTitle();

            if (getTitle().lastIndexOf('*') != -1)
                name = getTitle().substring(0, getTitle().lastIndexOf('*'));

            int result = JOptionPane.showConfirmDialog(Workspace.getUI().getFrame(),
                    LangResource.getString("message#266"),
                    name, JOptionPane.YES_NO_CANCEL_OPTION);
            if (result == JOptionPane.OK_OPTION) {
                if (getCurrentFile() == null)
                    saveAs();
                else
                    save();
                dispose();
            } else if (result == JOptionPane.NO_OPTION) {
                dispose();
            }
        } else if (!isModified() && e.getInternalFrame() == this) {
            dispose();
        }
    }

    public boolean isModified() {
        return modified;
    }

    public File getCurrentFile() {
        return current_file;
    }

    public void update() {
        if (!isModified()) {
            if (current_file == null) {
                setTitle(LangResource.getString("message#259") + " - " + LangResource.getString("message#267"));
                setName(LangResource.getString("message#259") + " - " + LangResource.getString("message#267"));
            } else {
                setTitle(LangResource.getString("message#259") + " - " + current_file.getName());
                setName(LangResource.getString("message#259") + " - " + current_file.getName());
            }
        } else {
            if (current_file == null) {
                setTitle(LangResource.getString("message#259") + " - " + LangResource.getString("message#263"));
                setName(LangResource.getString("message#259") + " - " +LangResource.getString("message#267"));
            } else {
                setTitle(LangResource.getString("message#259") + " - " + current_file.getName() + " * ");
                setName(LangResource.getString("message#259") + " - " + current_file.getName());
            }
        }
        revalidate();
        repaint();
    }

    protected void save() {
        try {
            java.io.OutputStream os = new java.io.FileOutputStream(current_file);
            os.write(syntaxTextArea.getText().getBytes());
            os.close();
            setModified(false);
            update();
        } catch (java.io.IOException ex) {
            Workspace.logException(LangResource.getString("message#264") +
                    ex.toString());
            JOptionPane.showMessageDialog(Workspace.getUI().
                    getFrame(), LangResource.getString("message#262") +
                    ex.toString(), LangResource.getString("message#259"), JOptionPane.ERROR_MESSAGE);
        }
    }

    protected void saveAs() {
        JFileChooser chooser = WorkspaceClassCache.getFileChooser(LangResource.getString("message#260"),
                new String[]{"bsh"}, LangResource.getString("message#154"));
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        chooser.setMultiSelectionEnabled(false);

        if (chooser.showSaveDialog(Workspace.getUI().getFrame())
                != JFileChooser.APPROVE_OPTION)
            return;

        current_file = chooser.getSelectedFile();
        try {
            String file_name = current_file.getAbsolutePath();
            if (!file_name.endsWith(".bsh")) file_name = file_name + ".bsh";
            current_file = new File(file_name);
            java.io.OutputStream os = new java.io.FileOutputStream(current_file);
            os.write(syntaxTextArea.getText().getBytes());
            os.close();
            setModified(false);
            update();
        } catch (java.io.IOException ex) {
            Workspace.logException(LangResource.getString("message#264") +
                    ex.toString());
            JOptionPane.showMessageDialog(Workspace.getUI().
                    getFrame(), LangResource.getString("message#262") +
                    ex.toString(), LangResource.getString("message#259"), JOptionPane.ERROR_MESSAGE);
        }
    }


    public RSyntaxTextArea getSyntaxTextArea() {

        if (syntaxTextArea == null) {
            syntaxTextArea = new RSyntaxTextArea();
            syntaxTextArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JAVA);
            syntaxTextArea.setCodeFoldingEnabled(true);
            syntaxTextArea.setAntiAliasingEnabled(true);
        }
        return syntaxTextArea;
    }

    public RTextScrollPane getTextScrollPane() {

        if (pane == null) {
            pane = new RTextScrollPane(getSyntaxTextArea());
            pane.setFoldIndicatorEnabled(true);
        }
        return pane;
    }

}