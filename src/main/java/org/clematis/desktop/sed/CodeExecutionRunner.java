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
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.util.List;

import javax.swing.SwingWorker;

public class CodeExecutionRunner {

    private final ExecutionListener listener;
    private SwingWorker<Void, String> activeWorker;
    private Process activeProcess;

    public CodeExecutionRunner(ExecutionListener listener) {
        this.listener = listener;
    }

    public synchronized boolean isRunning() {
        return activeWorker != null && !activeWorker.isDone();
    }

    @SuppressWarnings("checkstyle:LeftCurly")
    public synchronized void stopCurrentPipeline() {

        if (activeWorker != null && !activeWorker.isDone()) {
            activeWorker.cancel(true);
        }

        if (activeProcess != null) {
            activeProcess.destroyForcibly();

            try { activeProcess.getInputStream().close(); } catch (IOException ignored) {}
            try { activeProcess.getOutputStream().close(); } catch (IOException ignored) {}
            try { activeProcess.getErrorStream().close(); } catch (IOException ignored) {}
        }
    }

    @SuppressWarnings("checkstyle:AnonInnerLength")
    public synchronized void runCodePipeline(File currentSourceFile, String codeBuffer, String rawInputTextLines) {
        if (isRunning()) {
            return;
        }

        listener.onOutputReceived("Compiling and Running program...\n");
        listener.onStatusChanged(true);

        activeWorker = new SwingWorker<>() {
            @SuppressWarnings({"checkstyle:MultipleStringLiterals", "checkstyle:ReturnCount"})
            @Override
            protected Void doInBackground() throws Exception {
                Files.writeString(currentSourceFile.toPath(), codeBuffer);

                if (isCancelled()) {
                    return null;
                }

                String javaBin = System.getProperty("java.home") + File.separator + "bin" + File.separator + "java";
                ProcessBuilder processBuilder = new ProcessBuilder(javaBin, currentSourceFile.getAbsolutePath());
                processBuilder.redirectErrorStream(true);
                publish(javaBin + " " + currentSourceFile.getAbsolutePath());

                synchronized (CodeExecutionRunner.this) {
                    if (isCancelled()) {
                        return null;
                    }
                    activeProcess = processBuilder.start();
                }

                try (
                    BufferedWriter stdin = new BufferedWriter(new OutputStreamWriter(activeProcess.getOutputStream()));
                    BufferedReader stdout = new BufferedReader(new InputStreamReader(activeProcess.getInputStream()))
                ) {
                    if (!rawInputTextLines.isEmpty()) {
                        stdin.write(rawInputTextLines);
                        if (!rawInputTextLines.endsWith("\n")) {
                            stdin.newLine();
                        }
                        stdin.flush();
                    }

                    String outputLine;
                    // Check cancellation state prior to evaluating the blocking read
                    while (!isCancelled() && (outputLine = stdout.readLine()) != null) {
                        publish(outputLine);
                    }
                } catch (IOException e) {
                    // Catch stream closure from stopCurrentPipeline() gracefully
                    if (!isCancelled()) {
                        publish("\nProcess I/O interrupted.");
                    }
                }

                if (!isCancelled()) {
                    try {
                        int exitCode = activeProcess.waitFor();
                        publish("\nProcess finished with exit code " + exitCode);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
                return null;
            }

            @Override
            protected void process(List<String> chunks) {
                for (String chunk : chunks) {
                    listener.onOutputReceived(chunk + "\n");
                }
            }

            @Override
            protected void done() {
                synchronized (CodeExecutionRunner.this) {
                    activeProcess = null;
                    activeWorker = null;
                }
                listener.onStatusChanged(false);
            }
        };
        activeWorker.execute();
    }

    public interface ExecutionListener {
        void onOutputReceived(String text);
        void onStatusChanged(boolean isRunning);
    }
}
