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
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.Arrays;
import java.util.function.Consumer;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.SwingWorker;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import lombok.Getter;
import lombok.Setter;

public class FileBrowserTreePanel extends JPanel {

    private final JTree tree;
    private final DefaultTreeModel treeModel;
    private final DefaultMutableTreeNode rootNode;

    @Getter
    @Setter
    private File currentProjectDirectory;

    @Getter
    @Setter
    private Consumer<File> onFileDoubleClicked;

    @SuppressWarnings("checkstyle:MagicNumber")
    public FileBrowserTreePanel() {
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(220, 0)); // Set a clean default width for the sidebar
        setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, Color.GRAY));

        // 1. Set up a placeholder root node
        rootNode = new DefaultMutableTreeNode("No Project Open");
        treeModel = new DefaultTreeModel(rootNode);
        tree = new JTree(treeModel);
        tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);

        // 2. Add an action toolbar to open folders
        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);
        JButton openDirBtn = new JButton("Open Project...");
        openDirBtn.addActionListener(_ -> chooseAndLoadDirectory());
        toolBar.add(openDirBtn);

        // 3. Listen for double-clicks on files
        tree.addMouseListener(new MouseAdapter() {
            @SuppressWarnings("checkstyle:NestedIfDepth")
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && getOnFileDoubleClicked() != null) {
                    TreePath path = tree.getPathForLocation(e.getX(), e.getY());
                    if (path != null) {
                        DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
                        if (node.getUserObject() instanceof FileNodeWrapper) {
                            File file = ((FileNodeWrapper) node.getUserObject()).file();
                            if (file.isFile()) {
                                getOnFileDoubleClicked().accept(file);
                            }
                        }
                    }
                }
            }
        });

        add(toolBar, BorderLayout.NORTH);
        add(new JScrollPane(tree), BorderLayout.CENTER);
    }

    private void chooseAndLoadDirectory() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setDialogTitle("Select Project Root Directory");

        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            currentProjectDirectory = chooser.getSelectedFile();
            loadDirectoryStructure(currentProjectDirectory);
        }
    }

    /**
     * Rebuilds the visual JTree representation based on the selected target folder.
     */
    public void loadDirectoryStructure(File rootDir) {
        rootNode.removeAllChildren();
        rootNode.setUserObject(new FileNodeWrapper(rootDir, true));

        // Populate node hierarchies asynchronously using a safe worker thread
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                buildTreeHierarchy(rootNode, rootDir);
                return null;
            }

            @Override
            protected void done() {
                treeModel.nodeStructureChanged(rootNode);
                // Auto-expand the root folder for a better user experience
                tree.expandPath(new TreePath(rootNode.getPath()));
            }
        };
        worker.execute();
    }

    @SuppressWarnings("checkstyle:ReturnCount")
    private void buildTreeHierarchy(DefaultMutableTreeNode parentNode, File currentFile) {
        File[] files = currentFile.listFiles();
        if (files == null) {
            return;
        }

        Arrays.sort(files, (f1, f2) -> {
            if (f1.isDirectory() && !f2.isDirectory()) {
                return -1;
            }
            if (!f1.isDirectory() && f2.isDirectory()) {
                return 1;
            }
            return f1.getName().compareToIgnoreCase(f2.getName());
        });

        for (File file : files) {
            // Skip hidden system files (like .git or .DS_Store)
            if (file.getName().startsWith(".")) {
                continue;
            }

            DefaultMutableTreeNode childNode = new DefaultMutableTreeNode(new FileNodeWrapper(file, false));
            parentNode.add(childNode);

            if (file.isDirectory()) {
                buildTreeHierarchy(childNode, file);
            }
        }
    }

    /**
     * An elegant wrapper that overrides toString() so nodes display crisp filenames
     * while retaining full access to underlying java.io.File metadata.
     */
    record FileNodeWrapper(File file, boolean isRoot) {

        @Override
        public String toString() {
            return isRoot ? file.getAbsolutePath() : file.getName();
        }
    }
}
