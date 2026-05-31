package org.clematis.desktop.sed.components;
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

import java.awt.Component;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

import javax.swing.Icon;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;

import org.clematis.desktop.sed.WorkspaceScriptView;


public class FileTreeCellRenderer extends DefaultTreeCellRenderer {

    private final Map<String, Icon> extensionIconMap = new HashMap<>();

    public FileTreeCellRenderer() {
        extensionIconMap.put("java", WorkspaceScriptView.getResourceManager().getIcon("java_src.png"));
        extensionIconMap.put("cfg", WorkspaceScriptView.getResourceManager().getIcon("document.png"));
    }

    @SuppressWarnings("checkstyle:NestedIfDepth")
    @Override
    public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel,
                                                  boolean expanded, boolean leaf, int row, boolean hasFocus) {
        super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);

        if (value instanceof DefaultMutableTreeNode node) {
            if (node.getUserObject() instanceof FileNodeWrapper wrapper) {
                File file = wrapper.file();

                if (file.isDirectory()) {
                    // Enforce folder icon representation even if it is an empty directory leaf node
                    setIcon(expanded ? getOpenIcon() : getClosedIcon());
                } else {
                    // Extension checking logic sequence
                    String name = file.getName();
                    int lastDot = name.lastIndexOf('.');
                    if (lastDot != -1) {
                        String ext = name.substring(lastDot + 1).toLowerCase();
                        Icon customIcon = extensionIconMap.get(ext);
                        setIcon(customIcon != null ? customIcon : getDefaultLeafIcon());
                    } else {
                        setIcon(getDefaultLeafIcon());
                    }
                }
            }
        }
        return this;
    }
}

