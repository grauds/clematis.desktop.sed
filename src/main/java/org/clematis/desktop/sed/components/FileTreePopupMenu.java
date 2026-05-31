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
import java.io.File;

import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.tree.DefaultMutableTreeNode;

public class FileTreePopupMenu extends JPopupMenu {

    @SuppressWarnings("checkstyle:MultipleStringLiterals")
    public FileTreePopupMenu(FileBrowserTreePanel parentPanel, DefaultMutableTreeNode targetNode) {
        if (!(targetNode.getUserObject() instanceof FileNodeWrapper wrapper)) {
            return;
        }

        File selectedFile = wrapper.file();

        // 1. Context Aware option: Create new folder
        JMenuItem createItem = new JMenuItem("New Folder...");
        createItem.addActionListener(_ -> {
            File targetedParent = selectedFile.isDirectory() ? selectedFile : selectedFile.getParentFile();
            parentPanel.createNewFolder(targetedParent);
        });
        add(createItem);

        // 2. Action Option: Rename File or Directory
        JMenuItem renameItem = new JMenuItem("Rename...");
        renameItem.addActionListener(_ -> {
            String newName = JOptionPane.showInputDialog(
                parentPanel, "Enter new name:", "Rename Item",
                JOptionPane.PLAIN_MESSAGE, null, null, selectedFile.getName()
            ).toString();
            if (newName != null && !newName.trim().isEmpty()) {
                File destination = new File(selectedFile.getParentFile(), newName.trim());
                if (selectedFile.renameTo(destination)) {
                    parentPanel.refreshTree();
                } else {
                    JOptionPane.showMessageDialog(
                        parentPanel, "Rename operation failed.", "IO Error", JOptionPane.ERROR_MESSAGE
                    );
                }
            }
        });
        add(renameItem);
        addSeparator();

        // 3. Action Option: Delete target asset from drive storage arrays
        JMenuItem deleteItem = new JMenuItem("Delete");
        deleteItem.addActionListener(_ -> {
            int confirmation = JOptionPane.showConfirmDialog(
                parentPanel,
                "Are you sure you want to permanently delete: " + selectedFile.getName() + "?",
                "Confirm Deletion",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
            );

            if (confirmation == JOptionPane.YES_OPTION) {
                if (deleteRecursively(selectedFile)) {
                    parentPanel.refreshTree();
                } else {
                    JOptionPane.showMessageDialog(
                        parentPanel,
                        "Failed to delete files entirely.",
                        "IO Error",
                        JOptionPane.ERROR_MESSAGE
                    );
                }
            }
        });
        add(deleteItem);
    }

    /**
     * Recursively scrub data elements so folders with items in them can cleanly drop off disk.
     */
    @SuppressWarnings("checkstyle:NestedIfDepth")
    private boolean deleteRecursively(File target) {
        if (target.isDirectory()) {
            File[] files = target.listFiles();
            if (files != null) {
                for (File child : files) {
                    if (!deleteRecursively(child)) {
                        return false;
                    }
                }
            }
        }
        return target.delete();
    }
}
