package org.clematis.desktop.sed;
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
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.OutputStream;
import java.net.URI;
import java.nio.file.Files;
import java.util.Collections;
import java.util.Locale;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import javax.swing.text.DefaultEditorKit;
import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.FileObject;
import javax.tools.ForwardingJavaFileManager;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

import org.clematis.desktop.sed.actions.EditorActionsCollection;
import org.clematis.desktop.sed.components.ExecutionConsolePanel;
import org.clematis.desktop.sed.components.FileBrowserTreePanel;
import org.clematis.desktop.sed.components.StatusPanel;
import org.fife.ui.rsyntaxtextarea.RSyntaxDocument;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rsyntaxtextarea.parser.AbstractParser;
import org.fife.ui.rsyntaxtextarea.parser.DefaultParseResult;
import org.fife.ui.rsyntaxtextarea.parser.DefaultParserNotice;
import org.fife.ui.rsyntaxtextarea.parser.ParseResult;
import org.fife.ui.rtextarea.RTextScrollPane;
import org.fife.ui.rtextarea.SearchContext;
import org.fife.ui.rtextarea.SearchEngine;

import jworkspace.ui.api.dialog.StackTraceError;
import lombok.Getter;
import lombok.Setter;

@SuppressWarnings("checkstyle:MagicNumber")
public class SourceEditor extends JPanel {

    public static final String FOCUS_SEARCH = "focusSearch";
    public static final String TOGGLE_COMMENT = "toggleComment";
    public static final String IO_ERROR = "IO Error";
    public static final String PREFIX = "//";
    public static final String UNTITLED_JAVA = "Untitled.java";

    private EditorActionsCollection editorActions;

    private RSyntaxTextArea textArea;
    private RTextScrollPane scrollPane;

    private StatusPanel statusPanel;
    private ExecutionConsolePanel consolePanel;
    private FileBrowserTreePanel fileBrowserTreePanel;

    @Getter
    private final JFileChooser fileChooser = new JFileChooser();

    @Getter
    private File workingDirectory;

    @Getter
    @Setter
    private File currentSourceFile = null;

    @Getter
    @Setter
    private String currentFileName = UNTITLED_JAVA;

    @Getter
    @Setter
    private boolean liveCompilationEnabled = true;

    private JToolBar actionToolBar;
    private JTextField findField;
    private JTextField replaceField;
    private JCheckBox caseCheck;
    private JToolBar searchToolBar;

    @SuppressWarnings("checkstyle:MagicNumber")
    public SourceEditor() {
        setLayout(new BorderLayout());

        JSplitPane innerVerticalSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        innerVerticalSplit.setOneTouchExpandable(true);
        innerVerticalSplit.setContinuousLayout(true);
        innerVerticalSplit.setResizeWeight(0.60);

        JPanel upperEditorContainer = new JPanel(new BorderLayout());
        upperEditorContainer.add(createToolbar(), BorderLayout.NORTH);
        upperEditorContainer.add(getScrollPane(), BorderLayout.CENTER);

        innerVerticalSplit.setLeftComponent(upperEditorContainer);
        innerVerticalSplit.setRightComponent(getConsolePanel());

        JSplitPane masterHorizontalSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        masterHorizontalSplit.setResizeWeight(0.18); // Give roughly 18% width to the file tree panel
        masterHorizontalSplit.setOneTouchExpandable(true);
        masterHorizontalSplit.setContinuousLayout(true);
        masterHorizontalSplit.setLeftComponent(getFileBrowserTreePanel());
        masterHorizontalSplit.setRightComponent(innerVerticalSplit);

        add(masterHorizontalSplit, BorderLayout.CENTER);
        add(getStatusPanel(), BorderLayout.SOUTH);

        applyKeyboardShortcuts();
        updateFont(new Font("Monospaced", Font.PLAIN, 13));
    }

    public void setWorkingDirectory(File workingDirectory) {
        this.workingDirectory = workingDirectory;
        this.getFileChooser().setCurrentDirectory(workingDirectory);
        this.getFileBrowserTreePanel().setCurrentProjectDirectory(workingDirectory);
        this.getFileBrowserTreePanel().loadDirectoryStructure(workingDirectory);
    }

    public EditorActionsCollection getEditorActions() {
        if (editorActions == null) {
            editorActions = new EditorActionsCollection(this);
        }
        return editorActions;
    }

    public RSyntaxTextArea getTextArea() {
        if (textArea == null) {
            textArea = new RSyntaxTextArea(20, 80);
            textArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JAVA);
            textArea.setCodeFoldingEnabled(true);
            textArea.setAntiAliasingEnabled(true);
            textArea.setBracketMatchingEnabled(true);
            textArea.setAnimateBracketMatching(true);
            textArea.setCloseCurlyBraces(true);
            textArea.setAutoIndentEnabled(true);

            textArea.addParser(new JavaDiagnosticsCompilerParser());

            scrollPane = new RTextScrollPane(textArea);
            getScrollPane().setLineNumbersEnabled(true);
        }
        return textArea;
    }

    public JToolBar getActionToolBar() {
        if (actionToolBar == null) {
            actionToolBar = new JToolBar();


            actionToolBar.setFloatable(false);
            actionToolBar.add(getEditorActions().createToolbarButton("new"));
            actionToolBar.add(getEditorActions().createToolbarButton("open"));
            actionToolBar.add(getEditorActions().createToolbarButton("save"));
            actionToolBar.addSeparator();
            actionToolBar.add(getEditorActions().createToolbarButton("font"));
            actionToolBar.addSeparator();

            JButton runBtn = new JButton("Run");
            runBtn.addActionListener(_ -> getConsolePanel().runCodePipeline(
                currentSourceFile, getTextArea().getText())
            );
            actionToolBar.add(runBtn);
            actionToolBar.addSeparator();
            actionToolBar.add(getEditorActions().createToolbarButton("toggle_comments"));

            JCheckBox toggleLiveBox = new JCheckBox(getEditorActions().getAction("toggle_live_compilation"));
            toggleLiveBox.setSelected(isLiveCompilationEnabled());
            actionToolBar.add(toggleLiveBox);

        }
        return actionToolBar;
    }

    public JToolBar getSearchToolBar() {
        if (searchToolBar == null) {
            searchToolBar = new JToolBar();
            searchToolBar.setFloatable(false);
            searchToolBar.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 2));

            findField = new JTextField(12);
            replaceField = new JTextField(12);

            searchToolBar.add(new JLabel(" Find: "));
            searchToolBar.add(findField);

            searchToolBar.add(new JLabel(" Replace: "));
            searchToolBar.add(replaceField);

            caseCheck = new JCheckBox("Match Case");
            searchToolBar.add(caseCheck);
            searchToolBar.addSeparator();

            searchToolBar.add(getEditorActions().createToolbarButton("find"));
            searchToolBar.add(getEditorActions().createToolbarButton("replace"));
            searchToolBar.addSeparator();
        }
        return searchToolBar;
    }

    private JPanel createToolbar() {
        JPanel headerPanel = new JPanel(new GridLayout(2, 1));
        headerPanel.add(getActionToolBar());
        headerPanel.add(getSearchToolBar());
        return headerPanel;
    }

    public void runToolBarSearch(boolean isReplace, boolean all) {
        SearchContext context = new SearchContext();
        context.setSearchFor(findField.getText());
        context.setReplaceWith(replaceField.getText());
        context.setMatchCase(caseCheck.isSelected());
        context.setSearchForward(true);

        boolean found = isReplace
            ? (all ? SearchEngine.replaceAll(getTextArea(), context).wasFound() : SearchEngine.replace(
                getTextArea(), context).wasFound()
        ) : SearchEngine.find(getTextArea(), context).wasFound();

        if (!found) {
            UIManager.getLookAndFeel().provideErrorFeedback(getTextArea());
        }
    }

    private void applyKeyboardShortcuts() {
        InputMap inputMap = getTextArea().getInputMap(JComponent.WHEN_FOCUSED);
        ActionMap actionMap = getTextArea().getActionMap();

        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_END, 0), DefaultEditorKit.endLineAction);
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_HOME, 0), DefaultEditorKit.beginLineAction);
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_END, KeyEvent.SHIFT_DOWN_MASK),
            DefaultEditorKit.selectionEndLineAction
        );
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_HOME, KeyEvent.SHIFT_DOWN_MASK),
            DefaultEditorKit.selectionBeginLineAction
        );

        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_F,
            Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()),
            FOCUS_SEARCH
        );
        actionMap.put(FOCUS_SEARCH, new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) {
                findField.requestFocusInWindow();
                findField.selectAll();
            }
        });

        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_SLASH,
            Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()),
            TOGGLE_COMMENT
        );
        actionMap.put(TOGGLE_COMMENT, getEditorActions().getAction("toggle_comment"));
    }

    public void updateWindowFrameTitle() {
        Component comp = this;

        // Bubble up through the parent containers until we hit a JInternalFrame or null
        while (comp != null && !(comp instanceof JInternalFrame)) {
            comp = comp.getParent();
        }

        // If found, safely cast and update the title
        if (comp != null) {
            ((JInternalFrame) comp).setTitle("Source Editor Workspace - " + currentFileName);
        }
    }

    public void updateFont(Font newFont) {
        getTextArea().setFont(newFont);
        getConsolePanel().updateFont(newFont);
        if (getScrollPane().getGutter() != null) {
            getScrollPane().getGutter().setLineNumberFont(newFont);
        }
        revalidate(); repaint();
    }

    public StatusPanel getStatusPanel() {
        if (statusPanel == null) {
            statusPanel = new StatusPanel();
            statusPanel.setOnDiagnosticRowDoubleClicked(targetLine -> {
                try {
                    if (targetLine >= 0 && targetLine < getTextArea().getLineCount()) {
                        getTextArea().setCaretPosition(getTextArea().getLineStartOffset(targetLine));
                        getTextArea().requestFocusInWindow();
                    }
                } catch (Exception ex) {
                    StackTraceError.exception(this, "Error creating status panel", ex);
                }
            });
        }
        return statusPanel;
    }

    public ExecutionConsolePanel getConsolePanel() {
        if (consolePanel == null) {
            consolePanel = new ExecutionConsolePanel(getStatusPanel());
        }
        return consolePanel;
    }

    public FileBrowserTreePanel getFileBrowserTreePanel() {
        if (fileBrowserTreePanel == null) {
            fileBrowserTreePanel = new FileBrowserTreePanel();
            fileBrowserTreePanel.setOnFileDoubleClicked(file -> {
                try {
                    String content = Files.readString(file.toPath());
                    getTextArea().setText(content);

                    this.currentSourceFile = file;
                    this.currentFileName = file.getName();
                    updateWindowFrameTitle();

                    if (liveCompilationEnabled) {
                        getTextArea().forceReparsing(0);
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this,
                        "Could not open file: " + ex.getMessage(),
                        IO_ERROR,
                        JOptionPane.ERROR_MESSAGE
                    );
                }
            });
        }
        return fileBrowserTreePanel;
    }

    public RTextScrollPane getScrollPane() {
        if (scrollPane == null) {
            scrollPane = new RTextScrollPane(getTextArea());
        }
        return scrollPane;
    }

    private class JavaDiagnosticsCompilerParser extends AbstractParser {

        private final DefaultParseResult parseResult = new DefaultParseResult(this);
        private final JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();

        @Override
        public ParseResult parse(RSyntaxDocument doc, String style) {
            parseResult.clearNotices();
            getStatusPanel().clearDiagnostics();

            if (!liveCompilationEnabled || compiler == null) {
                getStatusPanel().updateStatusSummary(" Workspace Status: Live Compilation Disabled.");
                return parseResult;
            }

            String sourceCode = getTextArea().getText();
            DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();

            // 1. Input Source Code Wrapper (InMemory)
            SimpleJavaFileObject sourceFileObject = new SimpleJavaFileObject(
                URI.create("string:///" + currentFileName), JavaFileObject.Kind.SOURCE
            ) {
                @Override
                public CharSequence getCharContent(boolean ignoreErrors) {
                    return sourceCode;
                }
            };

            // 2. Wrap standard File Manager to redirect compiled outputs into RAM
            StandardJavaFileManager standardFileManager = compiler.getStandardFileManager(
                diagnostics, null, null
            );
            JavaFileManager inMemoryFileManager = new ForwardingJavaFileManager<>(standardFileManager) {
                @Override
                public JavaFileObject getJavaFileForOutput(Location location,
                                                           String className,
                                                           JavaFileObject.Kind kind,
                                                           FileObject sibling
                ) {
                    // Intercept the compiler's output request and return a dummy memory block
                    return new SimpleJavaFileObject(URI.create("mem:///"
                        + className.replace('.', '/')
                        + kind.extension), kind
                    ) {
                        @Override
                        public OutputStream openOutputStream() {
                            // Throw byte streams away since we only want syntax diagnostic verification!
                            return new ByteArrayOutputStream();
                        }
                    };
                }
            };

            // 3. Fire compilation task using the custom file manager
            JavaCompiler.CompilationTask task = compiler.getTask(
                null,
                inMemoryFileManager, // Pass the RAM-backed file manager here
                diagnostics,
                Collections.singletonList("-Xlint:all"),
                null,
                Collections.singletonList(sourceFileObject)
            );
            task.call();

            // 4. Process errors and warnings as usual
            int errorCount = 0;
            for (Diagnostic<? extends JavaFileObject> diagnostic : diagnostics.getDiagnostics()) {
                long line = diagnostic.getLineNumber() - 1;
                long offsetStart = diagnostic.getStartPosition();
                int length = Math.max(1, (int) (diagnostic.getEndPosition() - offsetStart));

                DefaultParserNotice notice = new DefaultParserNotice(this,
                    diagnostic.getMessage(Locale.getDefault()),
                    (int) line,
                    (int) offsetStart,
                    length
                );
                boolean isError = diagnostic.getKind() == Diagnostic.Kind.ERROR;
                if (isError) {
                    errorCount++;
                }

                notice.setLevel(isError ? DefaultParserNotice.Level.ERROR : DefaultParserNotice.Level.WARNING);
                parseResult.addNotice(notice);

                getStatusPanel().addDiagnostic(isError ? "ERROR" : "WARNING",
                    (int) diagnostic.getLineNumber(),
                    diagnostic.getMessage(Locale.getDefault())
                );
            }

            getStatusPanel().updateStatusSummary(String.format(
                " Workspace Status: Evaluation Sync Complete. Detected %d Active Compilation Errors.",
                errorCount)
            );

            // Clean up internal standard handles safely
            try {
                inMemoryFileManager.close();
            } catch (Exception ignored) {}

            return parseResult;
        }
    }

}


