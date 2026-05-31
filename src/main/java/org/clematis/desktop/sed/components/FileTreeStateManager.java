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
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

public class FileTreeStateManager {

    private FileTreeStateManager() {}

    public static Set<String> getExpandedAbsolutePaths(JTree tree, DefaultMutableTreeNode rootNode) {
        Set<String> expandedPaths = new HashSet<>();
        Enumeration<TreePath> expansionState = tree.getExpandedDescendants(new TreePath(rootNode.getPath()));
        if (expansionState != null) {
            while (expansionState.hasMoreElements()) {
                TreePath path = expansionState.nextElement();
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
                if (node.getUserObject() instanceof FileNodeWrapper wrapper) {
                    expandedPaths.add(wrapper.file().getAbsolutePath());
                }
            }
        }
        return expandedPaths;
    }

    public static void restoreExpandedPaths(JTree tree, DefaultMutableTreeNode node, Set<String> expandedPaths) {
        if (node.getUserObject() instanceof FileNodeWrapper wrapper) {
            if (expandedPaths.contains(wrapper.file().getAbsolutePath())) {
                tree.expandPath(new TreePath(node.getPath()));
            }
        }
        int childCount = node.getChildCount();
        for (int i = 0; i < childCount; i++) {
            DefaultMutableTreeNode child = (DefaultMutableTreeNode) node.getChildAt(i);
            restoreExpandedPaths(tree, child, expandedPaths);
        }
    }
}
