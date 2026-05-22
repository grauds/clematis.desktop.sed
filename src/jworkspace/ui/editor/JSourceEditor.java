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
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import javax.swing.BorderFactory;
import javax.swing.JDesktopPane;
import javax.swing.JLabel;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;

import jworkspace.ui.editor.jedit.JEditTextArea;

/**
 * This is a primary text editor for Java Workspace and helps to edit
 * Bean Shell scripts, native C/C++ code or view sources of open
 * java applications and libraries. It utilizes Java Workspace set
 * of libraries, therefore cannot be used as standalone application.
 * However, the package <code>jworkspace.ui.editor.jedit</code> does
 * not rely on anything else and can be used as a basis for any other
 * custom source editor.
 */
public class JSourceEditor extends AbstractEditor
                                            implements CaretListener
{
  /**
   * Status bar to dispay current cursor position and
   * format of edited document.
   */
//  protected StatusLine status = null;
  /**
   * Label to show column of cursor
   */
  protected JLabel col = null;
  /**
   * Label to show row of cursor
   */
  protected JLabel row = null;
  /**
   * Default constructor
   */
  public JSourceEditor()
  {
    this( null );
  }
  /**
   * Constructor for internal frame
   */
  public JSourceEditor( JDesktopPane desktop)
  {
    super();
    /**
     * Find resources
     */
    try
    {
        resources = ResourceBundle.getBundle("resources.editor",
                                          Locale.getDefault());
    }
    catch (MissingResourceException mre)
    {
        System.err.println("editor.properties not found");
    }

    this.actions = new EditorActions(this);
    getTextPane().addCaretListener( this.actions );

    actions.enableActions(true, EditorActions.APPLICATION_ACTION);
    actions.enableActions(true, EditorActions.APPLICATION_ACTION);
    actions.enableActions(true, EditorActions.DOCUMENT_ACTION);
    actions.enableActions(true, EditorActions.TEXT_ACTION);
    actions.enablePasteAction();

    setLayout(new BorderLayout());

    add(getTextPane(), BorderLayout.CENTER);
    if ( desktop != null )
    {
        add(getInternalToolBar("Source Editor",
               InternalToolBar.HORIZONTAL, desktop),
               BorderLayout.NORTH);
    }
    else
    {
        add(getToolBar(), BorderLayout.NORTH);
    }
//    add(getStatusLine(), BorderLayout.SOUTH);
    updateTokenMarker();
    getTextPane().addCaretListener(this);
  }
 /**
  * Listener for caret events on text pane.
  */
  public void caretUpdate(CaretEvent evt)
  {
    row.setText("Row: " +
              Integer.toString(getTextPane().getCaretLine() + 1));
    col.setText("Offset: " +
              Integer.toString(getTextPane().getCaretPosition()));
  }
 /**
  * Get status line for editor
  */
//  public StatusLine getStatusLine()
//  {
//     if (status == null)
//     {
//        status = new StatusLine();
//        status.setPermanentMessage("Ready");
//        KPanel holder = new KPanel();
//        holder.setLayout(new BorderLayout());
//
//        holder.add(getFormatLabel(), BorderLayout.WEST);
//        holder.add(getColumnsLabel(), BorderLayout.CENTER);
//        holder.add(getRowsLabel(), BorderLayout.EAST);
//
//        status.add(holder, BorderLayout.EAST);
//     }
//     return status;
//  }
 /**
  * Create columns monitor
  */
 public JLabel getColumnsLabel()
 {
     if (col == null)
     {
        col = new JLabel("Offset: ");
        Dimension d = col.getPreferredSize();
        col.setPreferredSize(new Dimension(d.width + 50, 20));
        col.setHorizontalAlignment(JLabel.CENTER);
        col.setToolTipText("Current Offset");
        col.setBorder(BorderFactory.createLoweredBevelBorder());
     }
     return col;
 }
 /**
  * Get name of this editor
  */
 public String getName()
 {
    if (current_file == null)
    {
      if (isModified())
        return "Untitled" + "*";
      else
        return "Untitled";
    }
    else
    {
      if (isModified())
        return current_file.getName() + "*";
      else
        return current_file.getName();
    }
 }
 /**
  * Create row monitor
  */
 public JLabel getRowsLabel()
 {
     if (row == null)
     {
        row = new JLabel("Row: ");
        Dimension d = row.getPreferredSize();
        row.setPreferredSize(new Dimension(d.width + 50, 20));
        row.setHorizontalAlignment(JLabel.CENTER);
        row.setToolTipText("Current Row");
        row.setBorder(BorderFactory.createLoweredBevelBorder());
     }
     return row;
 }
 /**
   * Get nested text area component
   */
  public JEditTextArea getTextPane()
  {
     if ( pane == null )
     {
        pane = new JEditTextArea(paneSettings);
        paneSettings.inputHandler.addKeyBinding("C+S", actions.getAction( EditorActions.saveActionName ));
        paneSettings.inputHandler.addKeyBinding("F3", actions.getAction( EditorActions.findMoreActionName ));
        if (pane.getDocument() != null)
        {
            pane.getDocument().getDocumentProperties().
            put(javax.swing.text.PlainDocument.tabSizeAttribute, new Integer(tabSize) );
            try
            {
                config = new ConfigFile(new File( Workspace.getUserHome() + ".seditor"), "GUI Definition");
                config.load();
                String font_face = config.getString(CK_FONT_FACE, "arial");
                int font_size = config.getInt(CK_FONT_SIZE, 12);
                int font_style = config.getInt(CK_FONT_STYLE, Font.PLAIN);
                Font font = new Font( font_face, font_style, font_size);
                pane.getPainter().setFont( font );
            }
            catch (FileNotFoundException ex)
            {
                // do nothing
            }
            catch (IOException e)
            {
                // do nothing
            }
        }
        pane.setTokenMarker(new JavaTokenMarker());
        pane.addPropertyChangeListener(this);
        pane.setElectricScroll(1);
        pane.setName("Source Editor");
     }
     return pane;
  }
  /**
   * Undo the last operation
   */
  public void undo()
  {
      if ( pane.getDocument().getUndoManager().canUndo() )
      {
         pane.getDocument().getUndoManager().undo();
      }
  }
}