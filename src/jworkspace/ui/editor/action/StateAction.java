package jworkspace.ui.editor.action;
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
import javax.swing.*;
import jworkspace.ui.editor.action.*;

public interface StateAction extends Action
{
    public static final String SELECTED    = "SELECTED";
    public static final String SELECTABLE  = "SELECTABLE";

    public boolean isSelected();
    public void    setSelected(boolean selected);
    public boolean isSelectable();
    public void    setSelectable(boolean selected);
}
