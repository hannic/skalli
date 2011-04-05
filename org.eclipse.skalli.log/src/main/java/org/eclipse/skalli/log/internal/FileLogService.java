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

import org.osgi.framework.ServiceReference;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.log.LogService;

@SuppressWarnings("nls")
public class FileLogService implements LogService {

  private PrintStream logFile;


  protected void activate(ComponentContext context) {
    try {
      String logfileName = "log/log.txt";
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
  public synchronized void log(int level, String message) {
    logFile.println("[" + levelToString(level) + "] " + message);
  }

  @Override
  public synchronized void log(int level, String message, Throwable exception) {
    log(level, message);
    logException(exception);
  }

  @Override
  public void log(ServiceReference sr, int level, String message) {
    log(level, message);
    if (sr != null) {
      logFile.println("<" + sr.getBundle().getBundleId() + ":" + sr.getClass().getName() + "> ");
    }
  }

  @Override
  public void log(ServiceReference sr, int level, String message, Throwable exception) {
    log(sr, level, message);
    logException(exception);
  }

  private void logException(Throwable exception) {
    if (exception != null) {
      exception.printStackTrace(logFile);
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

