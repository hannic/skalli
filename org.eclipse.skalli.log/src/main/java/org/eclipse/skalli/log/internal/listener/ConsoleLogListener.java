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
package org.eclipse.skalli.log.internal.listener;

import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.osgi.service.log.LogEntry;
import org.osgi.service.log.LogListener;
import org.osgi.service.log.LogReaderService;
import org.osgi.service.log.LogService;

/**
 * This listener once attached to a {@link LogReaderService} traces all log entries written
 * to the OSGi logging service and prints them to the console.
 * Note, you must start <tt>org.eclipse.equinox.log</tt> or another sutiable {@link LogService}
 * in order to make that work.
 */
public class ConsoleLogListener implements LogListener {

  private final static SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss"); //$NON-NLS-1$
  private final static PrintStream console = System.out;


  @Override
  public void logged(LogEntry entry) {
    if (entry.getMessage() != null) {
      console.println(sdf.format(new Date(entry.getTime()))+ " "  + levelToString(entry.getLevel()) + " [" + entry.getBundle().getSymbolicName() + "] " + entry.getMessage()); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }
  }

  private String levelToString(int level) {
    switch (level) {
    case LogService.LOG_DEBUG:
      return "DEBUG"; //$NON-NLS-1$
    case LogService.LOG_INFO:
      return "INFO"; //$NON-NLS-1$
    case LogService.LOG_WARNING:
      return "WARNING"; //$NON-NLS-1$
    case LogService.LOG_ERROR:
      return "ERROR"; //$NON-NLS-1$
    default:
      return "UNKNOWN"; //$NON-NLS-1$
    }
  }
}

