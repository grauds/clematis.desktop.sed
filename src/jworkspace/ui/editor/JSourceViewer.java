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
import java.awt.*;
import java.util.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import javax.swing.*;

import com.hyperrealm.kiwi.io.ConfigFile;
import jworkspace.ui.editor.jedit.*;
import jworkspace.kernel.Workspace;
/**
 * This is a primary text viewer for Java Workspace.
 */
public class JSourceViewer extends AbstractEditor
{
  public JSourceViewer()
  {
    super();
    try
    {
        resources = ResourceBundle.getBundle("resources.viewer",
                                          Locale.getDefault());
    }
    catch (MissingResourceException mre)
    {
        System.err.println("viewer.properties not found");
    }

    this.actions = new EditorActions(this);
    setLayout(new BorderLayout());
    add(getTextPane(), BorderLayout.CENTER);
    add(getToolBar(), BorderLayout.NORTH);

    actions.enableActions(true, EditorActions.APPLICATION_ACTION);
    actions.enableActions(true, EditorActions.DOCUMENT_ACTION);
    actions.enableActions(true, EditorActions.TEXT_ACTION);
    actions.enablePasteAction();

    updateTokenMarker();
  }
  /**
   * Get nested text area component
   */
  public JEditTextArea getTextPane()
  {
     if ( pane == null )
     {
        pane = new JEditTextArea(paneSettings);
        paneSettings.inputHandler.addKeyBinding("F3", actions.getAction( EditorActions.findMoreActionName ));
        if (pane.getDocument() != null)
        {
          pane.getDocument().getDocumentProperties().
             put(javax.swing.text.PlainDocument.tabSizeAttribute,
                             new Integer(tabSize) );
            try
            {
                config = new ConfigFile(new File(Workspace.getUserHome() + ".seditor"), "GUI Definition");
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
        pane.setElectricScroll(1);
        pane.setName("Source Viewer");
        pane.setEditable(false);
     }
     return pane;
  }
  /**
   * Get a menu bar for source editor
   */
  public JMenuBar getMenuBar()
  {
      return menuBar;
  }
}