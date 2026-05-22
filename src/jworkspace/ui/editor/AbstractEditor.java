package jworkspace.ui.editor;
/* ----------------------------------------------------------------------------
   Java Workspace
   Copyright (C) 1999-2016 Anton Troshin

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
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.Hashtable;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDesktopPane;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.text.BadLocationException;

import jworkspace.ui.editor.jedit.JEditTextArea;
import jworkspace.ui.editor.jedit.TextAreaDefaults;

/**
 * Abstract class for all common stuff for editors or viewers.
 */
public abstract class AbstractEditor extends JPanel
    implements PropertyChangeListener {
    /**
     * Suffix applied to the key used in resource file
     * lookups for an image.
     */
    public static final String imageSuffix = "Image";
    /**
     * Suffix applied to the key used in resource file
     * lookups for a label.
     */
    public static final String labelSuffix = "Label";
    /**
     * Suffix applied to the key used in resource file
     * lookups for an action.
     */
    public static final String actionSuffix = "Action";
    /**
     * Suffix applied to the key used in resource file
     * lookups for tooltip text.
     */
    public static final String tipSuffix = "Tooltip";
    /**
     * Editor properties
     */
    public static final String CK_FONT_FACE = "font.face",
        CK_FONT_SIZE = "font.size",
        CK_FONT_STYLE = "font.style",
        CK_TAB_SIZE = "tab.size";
    /**
     * International resources
     */
    protected static ResourceBundle resources;
    /**
     * Nested JEdit pane
     */
    protected JEditTextArea pane = null;
    /**
     * Editor actions
     */
    protected EditorActions actions = null;
    /**
     * Toolbar for application
     */
    protected JToolBar toolBar = null;
    /**
     * Toolbar for application
     */
    protected InternalToolBar internalToolBar = null;
    /**
     * Menu for application
     */
    protected JMenuBar menuBar = null;
    /**
     * Label to show format
     */
    protected JLabel format = null;
    /**
     * Tab size
     */
    protected int tabSize = 2;
    /**
     * Font
     */
    protected Font font = null;
    /**
     * Current file
     */
    protected File current_file = null;
    /**
     * Text area defaults
     */
    protected TextAreaDefaults paneSettings = new TextAreaDefaults().getDefaults();
    /**
     * Editor configuration
     */
    protected ConfigFile config = null;
    /**
     * Common file types
     */
    protected String[] commonExt = new String[]
        {
            "java", "bsh", "c", "bat", "xml", "xsl", "html", "htm", "shtml"
        };
    /**
     * Menu items
     */
    private Hashtable menuItems = new Hashtable();
    /**
     * Last search item
     */
    private String lastSearch = null;

    /**
     * Cut selected text
     */
    public void cut() {
        getTextPane().cut();
        actions.enablePasteAction();
        getTextPane().setModified(true);
    }

    /**
     * Delete selected text fragment
     */
    public void delete() {
        int startSel = getTextPane().getSelectionStart();
        int endSel = getTextPane().getSelectionEnd();
        try {
            getTextPane().getDocument().remove(startSel, endSel - startSel);
        } catch (BadLocationException ex) {
            //
        }
    }

    /**
     * Copy selected text
     */
    public void copy() {
        getTextPane().copy();
        actions.enablePasteAction();
    }

    /**
     * Paste text at specified location
     */
    public void paste() {
        getTextPane().paste();
        getTextPane().setModified(true);
    }

    /**
     * Set text
     */
    public void setText(String text) {
        getTextPane().setText(text);
        getTextPane().setOrigin(0, 0);
        updateTokenMarker();
    }

    /**
     * Select all
     */
    public void selectAll() {
        getTextPane().selectAll();
    }

    /**
     * Shows or hides toolbar
     */
    public void showToolBar() {
        getToolBar().setVisible(!getToolBar().isVisible());
        revalidate();
        repaint();
    }

    /**
     * Show tree view with outline of methods
     */
    public void showTreeView() {
    }

    protected URL getResource(String key) {
        String name = getResourceString(key);
        if (name != null) {
            URL url = this.getClass().getResource(name);
            return url;
        }
        return null;
    }

    /**
     * Hook through which every toolbar item is created.
     */
    protected Component createTool(String key) {
        return createToolbarButton(key);
    }

    /**
     * Create a button to go inside of the toolbar.  By default this
     * will load an image resource.  The image filename is relative to
     * the classpath (including the '.' directory if its a part of the
     * classpath), and may either be in a JAR file or a separate file.
     *
     * @param key The key in the resource file to serve as the basis
     *  of lookups.
     */
    protected AbstractButton createToolbarButton(String key) {
        String astr = getResourceString(key + actionSuffix);
        if (astr == null) {
            astr = key;
        }
        Action a = actions.getAction(astr);
        URL url = getResource(key + imageSuffix);
        AbstractButton b = null;

        if (a != null) {
            b = new KButton(new ImageIcon(url));
            b.setRequestFocusEnabled(false);
            b.setMargin(new Insets(1, 1, 1, 1));
            b.setActionCommand(astr);
            b.addActionListener(a);
            b.setEnabled(a.isEnabled());
        } else {
            b = new KButton(new ImageIcon(url));
            b.setRequestFocusEnabled(false);
            b.setMargin(new Insets(1, 1, 1, 1));
            b.setEnabled(false);
        }
        String tip = getResourceString(key + tipSuffix);
        if (tip != null) {
            b.setToolTipText(tip);
        }
        return b;
    }

    /**
     * Create a menu for the app.  By default this pulls the
     * definition of the menu from the associated resource file.
     */
    protected JMenu createMenu(String key) {
        String[] itemKeys = tokenize(getResourceString(key));
        JMenu menu = new JMenu(getResourceString(key + "Label"));
        for (int i = 0; i < itemKeys.length; i++) {
            if (itemKeys[i].equals("-")) {
                menu.addSeparator();
            } else {
                JMenuItem mi = createMenuItem(itemKeys[i]);
                menu.add(mi);
            }
        }
        return menu;
    }

    /**
     * This is the hook through which all menu items are
     * created.  It registers the result with the menuitem
     * hashtable so that it can be fetched with getMenuItem().
     */
    protected JMenuItem createMenuItem(String cmd) {
        /**
         * Get an action from the resource file.
         */
        String astr = getResourceString(cmd + actionSuffix);
        if (astr == null) {
            astr = cmd;
        }
        Action a = actions.getAction(astr);
        JMenuItem mi = new JMenuItem(getResourceString(cmd + labelSuffix));
        URL url = getResource(cmd + imageSuffix);
        if (url != null) {
            mi.setHorizontalTextPosition(JButton.RIGHT);
        }
        mi.setActionCommand(astr);

        if (a != null) {
            mi.addActionListener(a);
            mi.setEnabled(a.isEnabled());
        } else {
            mi.setEnabled(false);
        }
        menuItems.put(cmd, mi);
        return mi;
    }

    protected String getResourceString(String nm) {
        String str;
        try {
            str = resources.getString(nm);
        } catch (MissingResourceException mre) {
            str = null;
        }
        return str;
    }

    /**
     * Clear all
     */
    public void clearAll() {
        getTextPane().setText("");
        getTextPane().setModified(true);
    }

    /**
     * Evaluate all code in a file
     */
    public void evaluateAll() {
        try {
            if (getTextPane().getText() != null) {
                new Interpreter().eval(getTextPane().getText());
            }
        } catch (bsh.EvalError err) {
            JOptionPane.showMessageDialog(Workspace.getUI().getFrame(),
                err.toString());
        }
    }

    /**
     * Evaluate selected fragment of code
     */
    public void evaluate() {
        try {
            if (getTextPane().getSelectedText() != null) {
                new Interpreter().eval(getTextPane().getSelectedText());
            }
        } catch (bsh.EvalError err) {
            JOptionPane.showMessageDialog(Workspace.getUI().getFrame(),
                err.toString());
        }
    }

    /**
     * Open document
     */
    public void open() {
        /**
         * Save previously edited text if any
         */
        if (!checkClose()) {
            return;
        }
        /**
         * Present a file chooser for any of supported
         * file types.
         */
        JFileChooser chooser = WorkspaceClassCache.
            getFileChooser("Open File To Edit", commonExt, "Supported");
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        chooser.setMultiSelectionEnabled(false);

        if (chooser.showOpenDialog(Workspace.getUI().getFrame())
            == JFileChooser.APPROVE_OPTION) {
            current_file = chooser.getSelectedFile();
            updateTokenMarker();
            try {
                InputStream is = new FileInputStream(current_file);
                byte data[] = StreamUtils.readStreamToByteArray(is);
                is.close();

                setText(new String(data));
                firePropertyChange("NAME", getName(), current_file.getName());
                setName(current_file.getName());
                getTextPane().setOrigin(0, 0);
                getTextPane().setModified(false);
            } catch (IOException ex) {
                Workspace.getLogger().warning(LangResource.getString("message#269") +
                    ex.toString());
                JOptionPane.showMessageDialog(Workspace.getUI().getFrame(),
                    LangResource.getString("message#269") + ex.toString());
            }
        }
    }

    /**
     * Choose font
     */
    public void editProperties() {
        FontChooser chooser = new FontChooser(Workspace.getUI().getFrame(), getTextPane().getPainter().getFont());
        chooser.setVisible(true);
        getTextPane().getPainter().setFont(chooser.getSelectedFont());
        try {
            config = new ConfigFile(new File(Workspace.getUserHome() + ".seditor"), "Source Editor Properties");
            config.putString(CK_FONT_FACE, chooser.getSelectedFont().getName());
            config.putInt(CK_FONT_SIZE, chooser.getSelectedFont().getSize());
            config.putInt(CK_FONT_STYLE, chooser.getSelectedFont().getStyle());
            config.store();
        } catch (IOException e) {
            Workspace.getLogger().warning("Cannot save editor properties");
        }
    }

    /**
     * Close text editor
     */
    public void close() {
        if (getTextPane().isModified() && current_file != null) {
            int result = JOptionPane.
                showConfirmDialog(Workspace.getUI().getFrame(),
                    LangResource.getString("message#266"),
                    current_file.getName(),
                    JOptionPane.YES_NO_CANCEL_OPTION);
            if (result == JOptionPane.OK_OPTION) {
                if (getCurrentFile() == null) {
                    saveAs();
                } else {
                    save();
                }
            }
        } else if (getTextPane().isModified() && current_file == null) {
            int result = JOptionPane.
                showConfirmDialog(Workspace.getUI().getFrame(),
                    LangResource.getString("message#266"),
                    "Untitled",
                    JOptionPane.YES_NO_CANCEL_OPTION);
            if (result == JOptionPane.OK_OPTION) {
                if (getCurrentFile() == null) {
                    saveAs();
                } else {
                    save();
                }
            }
        }
    }

    /**
     * Get currently edited file.
     */
    public File getCurrentFile() {
        return current_file;
    }

    /**
     * Set currently edited file.
     */
    public void setCurrentFile(File current_file) {
        this.current_file = current_file;
    }

    /**
     * Get formats label
     */
    public JLabel getFormatLabel() {
        if (format == null) {
            format = new JLabel();
            Dimension d = format.getPreferredSize();
            format.setPreferredSize(new Dimension(d.width + 50, 20));
            format.setHorizontalAlignment(JLabel.CENTER);
            format.setToolTipText("Current Row");
            format.setBorder(BorderFactory.createLoweredBevelBorder());
        }
        return format;
    }

    /**
     * Get nested text area component
     */
    public abstract JEditTextArea getTextPane();

    /**
     * Get a menu bar for source editor
     */
    public JMenuBar getMenuBar() {
        if (menuBar == null) {
            menuBar = new JMenuBar();
            String[] menuKeys = tokenize(getResourceString("Menubar"));
            for (int i = 0; i < menuKeys.length; i++) {
                JMenu m = createMenu(menuKeys[i]);
                if (m != null) {
                    menuBar.add(m);
                }
            }
        }
        return menuBar;
    }

    /**
     * Get a toolbar for source editor
     */
    public JToolBar getToolBar() {
        if (toolBar == null) {
            toolBar = new JToolBar();
            toolBar.setOpaque(false);
            toolBar.setFloatable(false);
            String[] toolKeys = tokenize(getResourceString("toolbar"));
            for (int i = 0; i < toolKeys.length; i++) {
                if (toolKeys[i].equals("-")) {
                    toolBar.addSeparator();
                } else {
                    toolBar.add(createTool(toolKeys[i]));
                }
            }
            toolBar.add(Box.createHorizontalGlue());
        }
        return toolBar;
    }

    /**
     * Get internal toolbar
     */
    public InternalToolBar getInternalToolBar(String title,
                                              int orientation, JDesktopPane desktop) {
        if (internalToolBar == null) {
            internalToolBar = new InternalToolBar(title, orientation, desktop);
            String[] toolKeys = tokenize(getResourceString("toolbar"));
            for (int i = 0; i < toolKeys.length; i++) {
                if (toolKeys[i].equals("-")) {
                    internalToolBar.addSeparator(2);
                } else {
                    internalToolBar.add(createTool(toolKeys[i]));
                }
            }
            internalToolBar.add(Box.createHorizontalGlue());
        }
        return internalToolBar;
    }

    /**
     * Method to initiate a find/replace operation
     */
    public void doSearchMore(String searchFindTerm, String searchReplaceTerm,
                             boolean bIsFindReplace, boolean bCaseSensitive, boolean bStartAtTop) {
        boolean bReplaceAll = false;
        SearchDialog sdSearchInput = null;
        if (searchFindTerm == null) {
            searchFindTerm = lastSearch;
        }
        if (searchFindTerm == null && !bIsFindReplace) {
            sdSearchInput = new SearchDialog(
                Workspace.getUI().getFrame(),
                "Search", true, bIsFindReplace, bCaseSensitive, bStartAtTop);
        } else if (bIsFindReplace && searchReplaceTerm == null) {
            sdSearchInput = new SearchDialog(
                Workspace.getUI().getFrame(),
                "Replace", true, bIsFindReplace, bCaseSensitive, bStartAtTop);
        }
        if (sdSearchInput != null) {
            searchFindTerm = sdSearchInput.getFindTerm();
            searchReplaceTerm = sdSearchInput.getReplaceTerm();
            bCaseSensitive = sdSearchInput.getCaseSensitive();
            bStartAtTop = sdSearchInput.getStartAtTop();
            bReplaceAll = sdSearchInput.getReplaceAll();
        }
        if (searchFindTerm != null && (!bIsFindReplace || searchReplaceTerm != null)) {
            if (bReplaceAll) {
                int results = findText(searchFindTerm, searchReplaceTerm, bCaseSensitive, 0);
                int findOffset = results;
                if (results > -1) {
                    findOffset = ((findOffset - searchFindTerm.length())
                        + searchReplaceTerm.length()) + 1;
                    while (results > -1) {
                        results = findText(searchFindTerm, searchReplaceTerm,
                            bCaseSensitive, findOffset);
                        findOffset = ((results - searchFindTerm.length()) +
                            searchReplaceTerm.length()) + 1;
                    }
                } else {
                    JOptionPane.showMessageDialog(Workspace.getUI().getFrame(),
                        "No occurrences found: " + searchFindTerm, "Search",
                        JOptionPane.INFORMATION_MESSAGE);
                }
            } else {
                int results = findText(searchFindTerm, searchReplaceTerm,
                    bCaseSensitive, (bStartAtTop ? 0 : getTextPane().getCaretPosition()));
                if (results == -1) {
                    JOptionPane.showMessageDialog(Workspace.getUI().getFrame(),
                        "No match found: " + searchFindTerm, "Search",
                        JOptionPane.INFORMATION_MESSAGE);
                }
            }
        }
        lastSearch = searchFindTerm;
    }

    /**
     * Method to initiate a find/replace operation
     */
    public void doSearch(String searchFindTerm, String searchReplaceTerm,
                         boolean bIsFindReplace, boolean bCaseSensitive, boolean bStartAtTop) {
        lastSearch = null;
        doSearchMore(searchFindTerm, searchReplaceTerm, bIsFindReplace, bCaseSensitive, bStartAtTop);
    }

    /**
     * Returns true if the document can be closed, false otherwise
     */
    public boolean checkClose() {
        boolean ret = true;
        if (getTextPane().isModified()) {
            String ques = new String("Document is not saved. Save it now?");
            String title = getName();
            int save = JOptionPane.
                showConfirmDialog(this, ques, title, JOptionPane.YES_NO_CANCEL_OPTION);
            switch (save) {
                case JOptionPane.CANCEL_OPTION:
                    ret = false;
                    break;
                case JOptionPane.YES_OPTION:
                    save();
                    break;
                case JOptionPane.NO_OPTION:
                    ret = true;
                    break;
                default:
                    // There is a bug in the current JDK Yes/No/Cancel
                    // key mappings, ESC is mapped to something
                    // odd - value is NONE OF THE ABOVE, assume
                    // cancel behavior.  On Linux - save = -1
                    // Assume cancel behavior for cross-platform.
                    ret = false;
                    break;
            }
            current_file = null;
        }
        return ret;
    }

    /**
     *  Method for finding (and optionally replacing)
     *  a string in the text
     */
    private int findText(String findTerm, String replaceTerm,
                         boolean bCaseSenstive, int iOffset) {
        int searchPlace = (bCaseSenstive ?
            getTextPane().getText().indexOf(findTerm, iOffset) :
            getTextPane().getText().toLowerCase().indexOf(findTerm.toLowerCase(), iOffset)
        );
        if (searchPlace > -1) {
            getTextPane().select(searchPlace, searchPlace + findTerm.length());
            if (replaceTerm != null) {
                getTextPane().setSelectedText(replaceTerm);
            }
        }
        return searchPlace;
    }

    /**
     * Take the given string and chop it up into a series
     * of strings on whitespace boundries.  This is useful
     * for trying to get an array of strings out of the
     * resource file.
     */
    protected String[] tokenize(String input) {
        Vector v = new Vector();
        String cmd[] = new String[]{};
        if (input == null) {
            return cmd;
        }

        StringTokenizer t = new StringTokenizer(input);
        while (t.hasMoreTokens()) {
            v.addElement(t.nextToken());
        }
        cmd = new String[v.size()];

        for (int i = 0; i < cmd.length; i++) {
            cmd[i] = (String) v.elementAt(i);
        }

        return cmd;
    }

    /**
     * Is this editor contains changed text?
     */
    public boolean isModified() {
        return getTextPane().isModified();
    }

    /**
     * Save current text
     */
    protected void save() {
        if (current_file == null) {
            saveAs();
        } else {
            try {
                OutputStream os = new FileOutputStream(current_file);
                os.write(getTextPane().getText().getBytes());
                os.close();
                getTextPane().setModified(false);
            } catch (IOException ex) {
                Workspace.getLogger().warning(LangResource.getString("message#264") +
                    ex.toString());
                JOptionPane.showMessageDialog(Workspace.getUI().
                        getFrame(), LangResource.getString("message#262") +
                        ex.toString(), LangResource.getString("message#259"),
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Save current text into another file.
     */
    protected void saveAs() {
        JFileChooser chooser = WorkspaceClassCache.
            getFileChooser(LangResource.getString("message#260"),
                null, LangResource.getString("message#154"));
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        chooser.setMultiSelectionEnabled(false);

        if (chooser.showSaveDialog(Workspace.getUI().getFrame())
            != JFileChooser.APPROVE_OPTION) {
            return;
        }

        current_file = chooser.getSelectedFile();
        try {
            String file_name = current_file.getAbsolutePath();
            current_file = new File(file_name);
            OutputStream os = new FileOutputStream(current_file);
            os.write(getTextPane().getText().getBytes());
            os.close();
            getTextPane().setModified(false);
        } catch (IOException ex) {
            Workspace.getLogger().warning(LangResource.getString("message#264") +
                ex.toString());
            JOptionPane.showMessageDialog(Workspace.getUI().
                getFrame(), LangResource.getString("message#262") +
                ex.toString(), LangResource.getString("message#259"), JOptionPane.ERROR_MESSAGE);
        }
    }

    public void undo() {
    }

    /**
     * Update token marker
     */
    public void updateTokenMarker() {
        if (current_file == null) {
            getTextPane().setTokenMarker(new JavaTokenMarker());
            getFormatLabel().setText("java");
        } else {
            String file_name = current_file.getName();
            if (file_name.toLowerCase().endsWith("java")) {
                getTextPane().setTokenMarker(new JavaTokenMarker());
                getFormatLabel().setText("java");
            } else if (file_name.toLowerCase().endsWith("c")) {
                getTextPane().setTokenMarker(new CTokenMarker());
                getFormatLabel().setText("C");
            } else if (file_name.toLowerCase().endsWith("bat")) {
                getTextPane().setTokenMarker(new BatchFileTokenMarker());
                getFormatLabel().setText("batch");
            } else if (file_name.toLowerCase().endsWith("js")) {
                getTextPane().setTokenMarker(new JavaScriptTokenMarker());
                getFormatLabel().setText("js");
            } else if (file_name.toLowerCase().startsWith("make")) {
                getTextPane().setTokenMarker(new MakefileTokenMarker());
            } else if (file_name.toLowerCase().endsWith("xml")
                || file_name.toLowerCase().endsWith("xsl")) {
                getTextPane().setTokenMarker(new XMLTokenMarker());
                getFormatLabel().setText("XML");
            } else if (file_name.toLowerCase().endsWith("html")
                || file_name.toLowerCase().endsWith("htm")
                || file_name.toLowerCase().endsWith("shtml")) {
                getTextPane().setTokenMarker(new HTMLTokenMarker());
                getFormatLabel().setText("HTML");
            } else if (file_name.toLowerCase().endsWith("bsh")) {
                getTextPane().setTokenMarker(new JavaTokenMarker());
                getFormatLabel().setText("bsh");
            } else {
                getTextPane().setTokenMarker(null);
                getFormatLabel().setText("");
            }
        }
    }

    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getSource() == pane) {
            if (evt.getPropertyName().equalsIgnoreCase("MODIFIED")) {
                actions.enableSaveAction(pane.isModified());
                actions.getAction(EditorActions.saveActionName)
                    .setEnabled(pane.isModified());
            }
        }
    }
}