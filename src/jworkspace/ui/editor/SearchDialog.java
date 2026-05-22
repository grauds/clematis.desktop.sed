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
import java.beans.*;
import javax.swing.*;

import com.hyperrealm.kiwi.ui.dialog.KDialog;
import com.hyperrealm.kiwi.util.ResourceManager;
/**
 * Class for providing a dialog that lets the user specify arguments for
 * the Search Find/Replace functions
 */
public class SearchDialog extends KDialog
{
  private String inputFindTerm    = null;
  private String inputReplaceTerm = null;
  private boolean bCaseSensitive  = false;
  private boolean bStartAtTop     = false;
  private boolean bReplaceAll     = false;
  private JOptionPane jOptionPane;

  public SearchDialog(Frame parent, String title, boolean bModal,
       boolean bIsReplace, boolean bCaseSetting, boolean bTopSetting)
  {
    super(parent, title, bModal);
    final boolean isReplaceDialog = bIsReplace;
    final JTextField jtxfFindTerm    = new JTextField(3);
    final JTextField jtxfReplaceTerm = new JTextField(3);
    final JCheckBox  jchkCase        = new JCheckBox("Case Sensitive",
                                                     bCaseSetting);
    final JCheckBox  jchkTop         = new JCheckBox("Start At Top",
                                                     bTopSetting);
    final JCheckBox  jchkAll         = new JCheckBox("Replace All", false);
    final Object[]   buttonLabels    = { "Accept", "Cancel" };
    if(bIsReplace)
    {
      Object[] panelContents = {
        "Find",
        jtxfFindTerm,
        "Replace",
        jtxfReplaceTerm,
        jchkAll,
        jchkCase,
        jchkTop
      };
      jOptionPane = new JOptionPane(panelContents,
      JOptionPane.QUESTION_MESSAGE, JOptionPane.OK_CANCEL_OPTION, null,
      buttonLabels, buttonLabels[0]);
      ImageIcon icon = new ImageIcon( new ResourceManager(AbstractEditor.class).
                         getImage("replace_big.gif") );
      jOptionPane.setIcon(icon);
    }
    else
    {
      Object[] panelContents = { "Find", jtxfFindTerm, jchkCase, jchkTop  };
      jOptionPane = new JOptionPane(panelContents,
         JOptionPane.QUESTION_MESSAGE, JOptionPane.OK_CANCEL_OPTION,
         null, buttonLabels, buttonLabels[0]);
      ImageIcon icon = new ImageIcon( new ResourceManager(AbstractEditor.class).
                         getImage("find_big.gif") );
      jOptionPane.setIcon(icon);
    }
    setContentPane(jOptionPane);
    setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

    addWindowListener(new WindowAdapter() {
        public void windowClosing(WindowEvent we) {
            jOptionPane.setValue(new Integer(JOptionPane.CLOSED_OPTION));
        }
    });

    jOptionPane.addPropertyChangeListener(new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent e) {
            String prop = e.getPropertyName();
            if (isVisible()
                    && (e.getSource() == jOptionPane)
                    && (prop.equals(JOptionPane.VALUE_PROPERTY) ||
                    prop.equals(JOptionPane.INPUT_VALUE_PROPERTY))) {
                Object value = jOptionPane.getValue();
                if (value == JOptionPane.UNINITIALIZED_VALUE) {
                    return;
                }
                jOptionPane.setValue(JOptionPane.UNINITIALIZED_VALUE);
                if (value.equals(buttonLabels[0])) {
                    inputFindTerm = jtxfFindTerm.getText();
                    bCaseSensitive = jchkCase.isSelected();
                    bStartAtTop = jchkTop.isSelected();
                    if (isReplaceDialog) {
                        inputReplaceTerm = jtxfReplaceTerm.getText();
                        bReplaceAll = jchkAll.isSelected();
                    }
                    setVisible(false);
                } else {
                    inputFindTerm = null;
                    inputReplaceTerm = null;
                    bCaseSensitive = false;
                    bStartAtTop = false;
                    bReplaceAll = false;
                    setVisible(false);
                }
            }
        }
    });
      this.pack();
      if (getParent()!= null) {
          setLocationRelativeTo(getParent());
      }
      this.setVisible(true);
      jtxfFindTerm.requestFocus();
    }

    public String  getFindTerm()      { return inputFindTerm; }
    public String  getReplaceTerm()   { return inputReplaceTerm; }
    public boolean getCaseSensitive() { return bCaseSensitive; }
    public boolean getStartAtTop()    { return bStartAtTop; }
    public boolean getReplaceAll()    { return bReplaceAll; }
  }
