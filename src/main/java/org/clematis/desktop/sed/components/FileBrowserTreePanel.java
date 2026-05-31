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
import java.util.Set;
import java.util.function.Consumer;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
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

    @Getter private final JTree tree;
    @Getter private final DefaultTreeModel treeModel;
    @Getter private final DefaultMutableTreeNode rootNode;

    @Getter @Setter private File currentProjectDirectory;
    @Getter @Setter private Consumer<File> onFileDoubleClicked;

    @SuppressWarnings("checkstyle:MagicNumber")
    public FileBrowserTreePanel() {
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(220, 0));
        setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, Color.GRAY));

        rootNode = new DefaultMutableTreeNode("No Project Open");
        treeModel = new DefaultTreeModel(rootNode);
        tree = new JTree(treeModel);
        tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);

        // Req 2 & 3: Custom external renderer
        tree.setCellRenderer(new FileTreeCellRenderer());

        // Setup Toolbar
        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);

        JButton openDirBtn = new JButton("Open Project...");
        openDirBtn.addActionListener(_ -> chooseAndLoadDirectory());
        toolBar.add(openDirBtn);
        toolBar.addSeparator();

        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.addActionListener(_ -> refreshTree());
        toolBar.add(refreshBtn);

        JButton newFolderBtn = new JButton("New Folder");
        newFolderBtn.addActionListener(_ -> createNewFolder(null));
        toolBar.add(newFolderBtn);

        // Mouse listeners for double-clicks AND the new right-click context menu
        setupMouseListeners();

        add(toolBar, BorderLayout.NORTH);
        add(new JScrollPane(tree), BorderLayout.CENTER);
    }

    @SuppressWarnings("checkstyle:AnonInnerLength")
    private void setupMouseListeners() {

        tree.addMouseListener(new MouseAdapter() {
            @SuppressWarnings("checkstyle:NestedIfDepth")
            @Override
            public void mouseClicked(MouseEvent e) {
                // Double click behavior
                if (e.getClickCount() == 2 && getOnFileDoubleClicked() != null) {
                    TreePath path = tree.getPathForLocation(e.getX(), e.getY());
                    if (path != null) {
                        DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
                        if (node.getUserObject() instanceof FileNodeWrapper wrapper) {
                            if (wrapper.file().isFile()) {
                                getOnFileDoubleClicked().accept(wrapper.file());
                            }
                        }
                    }
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {
                handleContextMenu(e);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                handleContextMenu(e);
            }

            private void handleContextMenu(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    TreePath path = tree.getPathForLocation(e.getX(), e.getY());
                    if (path != null) {
                        // Force tree selection to the right-clicked row
                        tree.setSelectionPath(path);
                        DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();

                        // Instantiate and show the separate popup menu class
                        FileTreePopupMenu popup = new FileTreePopupMenu(FileBrowserTreePanel.this, node);
                        popup.show(tree, e.getX(), e.getY());
                    }
                }
            }
        });
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

    public void refreshTree() {
        if (currentProjectDirectory != null) {
            loadDirectoryStructure(currentProjectDirectory);
        }
    }

    @SuppressWarnings({"checkstyle:NestedIfDepth", "checkstyle:ReturnCount"})
    public void createNewFolder(File forcedParentDir) {
        if (currentProjectDirectory == null) {
            JOptionPane.showMessageDialog(
                this,
                "Please open a project directory first.",
                "No Project",
                JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        File parentDir = forcedParentDir;
        if (parentDir == null) {
            parentDir = currentProjectDirectory;
            TreePath selectedPath = tree.getSelectionPath();
            if (selectedPath != null) {
                DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) selectedPath.getLastPathComponent();
                if (selectedNode.getUserObject() instanceof FileNodeWrapper wrapper) {
                    File selectedFile = wrapper.file();
                    parentDir = selectedFile.isDirectory() ? selectedFile : selectedFile.getParentFile();
                }
            }
        }

        String folderName = JOptionPane.showInputDialog(
            this,
            "Enter name for the new folder:",
            "Create New Folder",
            JOptionPane.PLAIN_MESSAGE
        );
        if (folderName == null || folderName.trim().isEmpty()) {
            return;
        }

        File newFolder = new File(parentDir, folderName.trim());
        if (newFolder.exists()) {
            JOptionPane.showMessageDialog(
                this,
                "A folder or file with that name already exists.",
                "Creation Error",
                JOptionPane.ERROR_MESSAGE
            );
            return;
        }

        if (newFolder.mkdirs()) {
            refreshTree();
        } else {
            JOptionPane.showMessageDialog(this,
                "Failed to create folder.",
                "IO Error",
                JOptionPane.ERROR_MESSAGE
            );
        }
    }

    public void loadDirectoryStructure(File rootDir) {
        // Save state externally using helper class
        Set<String> expandedPaths = FileTreeStateManager.getExpandedAbsolutePaths(tree, rootNode);

        rootNode.removeAllChildren();
        rootNode.setUserObject(new FileNodeWrapper(rootDir, true));

        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                buildTreeHierarchy(rootNode, rootDir);
                return null;
            }

            @Override
            protected void done() {
                treeModel.nodeStructureChanged(rootNode);
                // Restore state externally
                FileTreeStateManager.restoreExpandedPaths(tree, rootNode, expandedPaths);
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
            if (file.getName().startsWith(".")) {
                continue;
            }

            DefaultMutableTreeNode childNode = new DefaultMutableTreeNode(
                new FileNodeWrapper(file, false)
            );
            parentNode.add(childNode);

            if (file.isDirectory()) {
                buildTreeHierarchy(childNode, file);
            }
        }
    }
}
