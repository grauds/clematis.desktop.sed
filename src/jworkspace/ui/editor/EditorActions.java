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
import java.awt.event.*;
import java.awt.datatransfer.*;
import java.util.*;
import java.io.IOException;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;

import com.hyperrealm.kiwi.util.ResourceManager;
import jworkspace.ui.editor.action.*;
/**
 * Basic editor actions.
 */
public class EditorActions implements CaretListener
{
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
  * Action property - this action work for opened document and governed
  * by document properties
  */
  public static final String DOCUMENT_GOVERNED_ACTION = "DOCUMENT_GOVERNED_ACTION";
 /**
  * Action property - this action work for selected text element only
  */
  public static final String TEXT_ACTION = "TEXT_ACTION";
 /**
  * Application instance
  */
  protected AbstractEditor editor = null;
 /**
  * All actions
  */
  protected Hashtable actions = new Hashtable();
  /**
   * Open document action name
   */
  public static final String openActionName = "open";
  /**
   * Show toolbar action name
   */
  public static final String showToolbarActionName = "show_toolbar";
  /**
   * Show tree view action name
   */
  public static final String showTreeViewActionName = "show_tree_view";
  /**
   * Save document action name
   */
  public static final String saveActionName = "save";
  /**
   * Save as document action name
   */
  public static final String saveAsActionName = "save_as";
  /**
   * Select all action name
   */
  public static final String selectAllActionName = "select_all";
  /**
   * Close document action name
   */
  public static final String closeActionName = "close";
  /**
   * Exit editor action name
   */
  public static final String exitActionName = "exit";
  /**
   * Undo action name
   */
  public static final String undoActionName = "undo";
  /**
   * Cut action name
   */
  public static final String cutActionName = "cut";
  /**
   * Copy action name
   */
  public static final String copyActionName = "copy";
  /**
   * Choose font action name
   */
  public static final String chooseFontActionName = "font";
  /**
   * Paste action name
   */
  public static final String pasteActionName = "paste";
  /**
   * Search action name
   */
  public static final String findActionName = "find";
  /**
   * Search more action name
   */
  public static final String findMoreActionName = "findMore";
  /**
   * Replace action name
   */
  public static final String replaceActionName = "replace";
  /**
   * Evaluate action name
   */
  public static final String evalActionName = "evaluate";
  /**
   * Evaluate all action name
   */
  public static final String evalAllActionName = "evaluateAll";
 /**
   * About action name
   */
  public static final String aboutActionName = "about";
 /**
  * Help action name
  */
  public static final String helpActionName = "help_contents";
  /**
   * Open document action
   */
  protected Action openAction;
  /**
   * Show toolbar action
   */
  protected Action showToolbarAction;
  /**
   * Show tree view action
   */
  protected Action showTreeViewAction;
  /**
   * Save document action
   */
  protected Action saveAction;
  /**
   * Save as document action
   */
  protected Action saveAsAction;
  /**
   * Select all action
   */
  protected Action selectAllAction;
  /**
   * Close document action
   */
  protected Action closeAction;
  /**
   * Choose font action
   */
  protected Action chooseFontAction;
  /**
   * Exit editor action
   */
  protected Action exitAction;
  /**
   * Undo action
   */
  protected Action undoAction;
  /**
   * Cut action
   */
  protected Action cutAction;
  /**
   * Copy action
   */
  protected Action copyAction;
  /**
   * Paste action
   */
  protected Action pasteAction;
  /**
   * Search action
   */
  protected Action findAction;
  /**
   * Search more action
   */
  protected Action findMoreAction;
  /**
   * Replace action
   */
  protected Action replaceAction;
  /**
   * Evaluate action
   */
  protected Action evalAction;
  /**
   * Evaluate all action
   */
  protected Action evalAllAction;
  /**
   * Help action
   */
  protected Action helpAction;
  /**
   * About action
   */
  protected Action aboutAction;
  //**************************** document actions *****************************
  /**
   * Action to perform when the user deletes a node
   */
  public static String deleteActionName = "delete";
  /**
   * Action to perform when the user deletes a node
   */
  protected Action deleteAction;
  /**
   * Delete node from XML file
   */
  protected class DeleteAction extends AbstractAction
  {
      public DeleteAction ()
      {
        super(deleteActionName);
        putValue(ACTION_TYPE, SELECTION_ACTION);
        setEnabled(false);
      }
      /**
       * Do we really need to confirm user the deletion of
       * selected nodes? Maybe just relay on undo?
       */
      public void actionPerformed(ActionEvent evt)
      {
         editor.delete();
      }
  }
  //***************************************************************************
  /**
   * Open action
   */
  protected class OpenAction extends AbstractAction
  {
     OpenAction()
     {
        super(openActionName);
        putValue(ACTION_TYPE, APPLICATION_ACTION);
     }
     public void actionPerformed(ActionEvent e)
     {
        editor.open();
     }
  }
 /**
  * Save action
  */
  protected class SaveAction extends AbstractAction
  {
     SaveAction()
     {
       super(saveActionName);
       putValue(ACTION_TYPE, DOCUMENT_GOVERNED_ACTION);
      setEnabled(false);
     }
     public void actionPerformed(ActionEvent e)
     {
       editor.save();
     }
  }
 /**
  * Save as action
  */
  protected class SaveAsAction extends AbstractAction
  {
     SaveAsAction()
     {
       super(saveActionName);
       putValue(ACTION_TYPE, DOCUMENT_ACTION);
     }
     public void actionPerformed(ActionEvent e)
     {
       editor.saveAs();
     }
  }
 /**
  * Select all action
  */
  protected class SelectAllAction extends AbstractAction
  {
     public SelectAllAction()
     {
       super(selectAllActionName);
       putValue(ACTION_TYPE, DOCUMENT_ACTION);
     }
     public void actionPerformed(ActionEvent e)
     {
       editor.selectAll();
     }
  }
 /**
  * Show toolbar action
  */
  protected class ShowToolbarAction extends AbstractStateAction
  {
     public ShowToolbarAction ()
     {
       super(showToolbarActionName);
       /**
        * Toolbar is visible initially
        */
       setSelected(true);
       putValue(ACTION_TYPE, APPLICATION_ACTION);
     }
     public void actionPerformed (ActionEvent evt)
     {
        editor.showToolBar();
        setSelected(editor.getToolBar().isVisible());
     }
  }
 /**
  * Show toolbar action
  */
  protected class ShowTreeViewAction extends AbstractStateAction
  {
     public ShowTreeViewAction ()
     {
       super(showTreeViewActionName);
       putValue(ACTION_TYPE, DOCUMENT_ACTION);
     }
     public void actionPerformed (ActionEvent evt)
     {
       editor.showTreeView();
     }
  }
 /**
  * Exit action
  */
  protected class ExitAction extends AbstractAction
  {
     ExitAction()
     {
        super(exitActionName);
        putValue(ACTION_TYPE, APPLICATION_ACTION);
     }
     public void actionPerformed(ActionEvent e)
     {
        editor.close();
     }
  }
  /**
   * Undo action
   */
  protected class UndoAction extends AbstractAction
  {
    public UndoAction ()
    {
      super(undoActionName);
      putValue(ACTION_TYPE, DOCUMENT_GOVERNED_ACTION);
    }
    public void actionPerformed (ActionEvent evt)
    {
      editor.undo();
    }
  }
  /**
   * Cut action
   */
  protected class CutAction extends TextAction
  {
    public CutAction ()
    {
      super(cutActionName);
      putValue(ACTION_TYPE, SELECTION_ACTION);
      setEnabled(false);
    }
    public void actionPerformed (ActionEvent evt)
    {
      editor.cut();
    }
  }
  /**
   * Copy action
   */
  protected class CopyAction extends TextAction
  {
    public CopyAction ()
    {
      super(copyActionName);
      putValue(ACTION_TYPE, SELECTION_ACTION);
      setEnabled(false);
    }
    public void actionPerformed (ActionEvent evt)
    {
      editor.copy();
    }
  }
  /**
   * Choose font action
   */
  protected class ChooseFontAction extends AbstractAction
  {
    public ChooseFontAction ()
    {
      super(chooseFontActionName);
      putValue(ACTION_TYPE, DOCUMENT_ACTION);
    }
    public void actionPerformed (ActionEvent evt)
    {
      editor.editProperties();
    }
  }
 /**
  * Paste action
  */
  protected class PasteAction extends TextAction
  {
    public PasteAction ()
    {
      super(pasteActionName);
      putValue(ACTION_TYPE, DOCUMENT_GOVERNED_ACTION);
      setEnabled(false);
    }
    public void actionPerformed (ActionEvent evt)
    {
      editor.paste();
    }
  }
 /**
  * Replace action
  */
  protected class ReplaceAction extends AbstractAction
  {
    public ReplaceAction ()
    {
      super(replaceActionName);
      putValue(ACTION_TYPE, DOCUMENT_ACTION);
      setEnabled(false);
    }
    public void actionPerformed (ActionEvent evt)
    {
      editor.doSearch(null, null, true, false, true);
    }
  }
 /**
  * Search action
  */
  protected class FindAction extends TextAction
  {
    public FindAction ()
    {
      super(findActionName);
      putValue(ACTION_TYPE, DOCUMENT_ACTION);
    }
    public void actionPerformed (ActionEvent evt)
    {
      editor.doSearch(null, null, false, false, true);
    }
  }
 /**
  * Search more action
  */
  protected class FindMoreAction extends TextAction
  {
    public FindMoreAction ()
    {
      super(findMoreActionName);
      putValue(ACTION_TYPE, DOCUMENT_ACTION);
    }
    public void actionPerformed (ActionEvent evt)
    {
      editor.doSearchMore(null, null, false, false, false);
    }
  }
 /**
  * Evaluate action
  */
  protected class EvalAction extends AbstractAction
  {
    public EvalAction ()
    {
      super(evalActionName);
      putValue(ACTION_TYPE, SELECTION_ACTION);
      setEnabled(false);
    }
    public void actionPerformed (ActionEvent evt)
    {
      editor.evaluate();
    }
  }
 /**
  * Evaluate all action
  */
  protected class EvalAllAction extends AbstractAction
  {
    public EvalAllAction ()
    {
      super(evalAllActionName);
      putValue(ACTION_TYPE, DOCUMENT_ACTION);
      setEnabled(false);
    }
    public void actionPerformed (ActionEvent evt)
    {
      editor.evaluateAll();
    }
  }
 /**
  * About action
  */
  protected class AboutAction extends AbstractAction
  {
     public AboutAction ()
     {
       super(aboutActionName);
       putValue(ACTION_TYPE, APPLICATION_ACTION);
     }
     public void actionPerformed (ActionEvent evt)
     {
       ImageIcon icon = new ImageIcon(
         new ResourceManager(AbstractEditor.class).getImage("editor.png") );
       StringBuffer info = new StringBuffer();
       info.append("<html><font color=\"#000000\">");
       info.append("Clematis Source Editor 1.0.3, (c) 2002 - 2003");
       info.append("<br>");
       info.append("jEdit Component, (c) 1999 Slava Pestov");
       info.append("</font><html>");
       JOptionPane.showMessageDialog(
              editor,
              info.toString(),
              "About Source Editor",
              JOptionPane.INFORMATION_MESSAGE, icon);
     }
  }
 /**
  * Help action
  */
  protected class HelpAction extends AbstractAction
  {
    public HelpAction ()
    {
      super(helpActionName);
      putValue(ACTION_TYPE, APPLICATION_ACTION);
    }
    public void actionPerformed (ActionEvent evt)
    {
    }
  }
  /**
   * Public constructor
   */
  public EditorActions(AbstractEditor editor)
  {
    super();
    this.editor = editor;
    createActions();
  }
  public Action getAction(String name)
  {
    return  (Action) actions.get(name);
  }
  /**
   * Enable actions of specified type
   */
  public void enableActions(boolean flag, String type)
  {
     Action[] actions = getActions();
     for (int i = 0; i < actions.length; i++)
     {
        String stype = (String) actions[i].getValue(ACTION_TYPE);
        if (stype == null) continue;
        else if (stype.equals(type))
        {
          actions[i].setEnabled(flag);
        }
     }
  }
  /**
   * Enable paste action
   */
  public void enablePasteAction()
  {
     Clipboard sc = Toolkit.getDefaultToolkit().getSystemClipboard();
     Transferable tr = sc.getContents(this);
     if (tr != null)
     {
        try
        {
          if ( tr.getTransferData(DataFlavor.stringFlavor) != null )
          {
            getAction(EditorActions.pasteActionName).setEnabled(true);
            return;
          }
        }
        catch (UnsupportedFlavorException e)
        {
           // do nothing
        }
        catch (IOException e)
        {
           // do nothing
        }
     }
     getAction(EditorActions.pasteActionName).setEnabled(false);
  }
  /**
   * Enable save action
   */
  public void enableSaveAction(boolean flag)
  {
     getAction(EditorActions.saveActionName).setEnabled(flag);
  }
  public Action[] getActions()
  {
    Enumeration en = actions.elements();
    Action[] temp = new Action[actions.size()];
    for (int i = 0; i < temp.length; i++)
    {
      temp[i] = (Action) en.nextElement();
    }
    return temp;
  }
  public void caretUpdate(CaretEvent e)
  {
     String sel = editor.getTextPane().getSelectedText();
     if (sel != null && !sel.trim().equals(""))
     {
        enableActions(true, SELECTION_ACTION);
     }
     else
     {
        enableActions(false, SELECTION_ACTION);
     }
     enablePasteAction();
  }
  protected Hashtable createActions()
  {
       openAction = new OpenAction();
       saveAction = new SaveAction();
       saveAsAction   = new SaveAsAction();
       exitAction     = new ExitAction();

       showToolbarAction = new ShowToolbarAction();
       showTreeViewAction = new ShowTreeViewAction();

       undoAction      = new UndoAction();
       cutAction       = new CutAction();
       copyAction      = new CopyAction();
       pasteAction     = new PasteAction();
       deleteAction    = new DeleteAction();
       selectAllAction = new SelectAllAction();

       findAction      = new FindAction();
       findMoreAction  = new FindMoreAction();
       replaceAction   = new ReplaceAction();
       evalAction      = new EvalAction();
       evalAllAction   = new EvalAllAction();
       chooseFontAction = new ChooseFontAction();

       helpAction     = new HelpAction();
       aboutAction    = new AboutAction();

       actions.put(openActionName, openAction);
       actions.put(saveActionName, saveAction);
       actions.put(saveAsActionName, saveAsAction);
       actions.put(exitActionName, exitAction);

       actions.put(undoActionName, undoAction);
       actions.put(cutActionName, cutAction);
       actions.put(copyActionName, copyAction);
       actions.put(pasteActionName, pasteAction);
       actions.put(selectAllActionName, selectAllAction);
       actions.put(deleteActionName, deleteAction);
       actions.put(findActionName, findAction);
       actions.put(findMoreActionName, findMoreAction);
       actions.put(replaceActionName, replaceAction);
       actions.put(evalActionName, evalAction);
       actions.put(evalAllActionName, evalAllAction);
       actions.put(chooseFontActionName, chooseFontAction);
       actions.put(helpActionName, helpAction);
       actions.put(aboutActionName, aboutAction);

       actions.put(showToolbarActionName, showToolbarAction);
       actions.put(showTreeViewActionName, showTreeViewAction);

       return actions;
   }
}