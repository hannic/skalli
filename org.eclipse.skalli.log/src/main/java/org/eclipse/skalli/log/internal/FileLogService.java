/*******************************************************************************
 * Copyright (c) 2010, 2011 SAP AG and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     SAP AG - initial API and implementation
 *******************************************************************************/
package org.eclipse.skalli.log.internal;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

import org.eclipse.skalli.log.DefaultLogFormatter;
import org.eclipse.skalli.log.LogService;
import org.osgi.service.component.ComponentContext;

/**
 * File based implementation of {@link LogService}.
 * The name of the log file can be set with the system property <tt>logfile</tt>.
 */
public class FileLogService implements LogService {

    private PrintStream logFile;
    private Formatter formatter = new DefaultLogFormatter();

    protected void activate(ComponentContext context) {
        try {
            String logfileName = "log/skalli.log"; //$NON-NLS-1$
            String logfileProperty = System.getProperty("logfile"); //$NON-NLS-1$
            if (logfileProperty != null) {
                logfileName = logfileProperty;
            }
            logFile = new PrintStream(new FileOutputStream(logfileName), true);
        } catch (FileNotFoundException e) {
            throw new RuntimeException("failed to open log file");
        }
    }

    protected void deactivate(ComponentContext context) {
        logFile.flush();
        logFile.close();
    }

    @Override
    public synchronized void log(LogRecord record) {
        logFile.println(formatter.format(record));
    }

    @Override
    public void close() {
        logFile.close();
    }

    @Override
    public void flush() {
        logFile.flush();
    }
}
