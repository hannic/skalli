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

import java.util.HashSet;
import java.util.Set;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import org.osgi.service.log.LogService;


public class JdkLogManager {

  private final Set<String> loggerNames;
  private final Set<LogService> logServices;
  private final Handler defaultHandler;
  private Handler lastUsedHandler;
  private Level defaultLogLevel;

  private static JdkLogManager instance = new JdkLogManager();

  private JdkLogManager() {
    defaultHandler = new ConsoleHandler();
    defaultHandler.setFormatter(new SimpleFormatter());
    lastUsedHandler = defaultHandler;
    loggerNames = new HashSet<String>();
    logServices = new HashSet<LogService>();
    String defaultLogLevelProperty = System.getProperty("defaultLogLevel"); //$NON-NLS-1$
    if (defaultLogLevelProperty != null) {
      this.defaultLogLevel = Level.parse(defaultLogLevelProperty);
    }
  }

  /**
   * Used by {@link Log}.
   * @return  the (singleton) <code>JdkLogManager</code> instance.
   */
  public static JdkLogManager getInstance() {
    return instance;
  }

  /**
   * Returns a {@link Logger} for the given location.
   * @param name  the location, e.g. a class name.
   */
  public synchronized Logger getLogger(String name) {
    Logger logger = Logger.getLogger(name);
    if (!loggerNames.contains(name)) {
      logger.setUseParentHandlers(false);
      logger.addHandler(lastUsedHandler);
      if (defaultLogLevel != null) {
        logger.setLevel(defaultLogLevel);
      }
      loggerNames.add(name);
    }
    return logger;
  }

  /**
   * Sets the default log level for all loggers provided
   * by this log manager.
   * @param defaultLogLevel  the default log level to set.
   */
  public synchronized void setDefaultLogLevel(Level defaultLogLevel) {
    this.defaultLogLevel = defaultLogLevel;
  }

  synchronized void bindLogService(LogService logService) {
    if (logService != null) {
      logServices.add(logService);
      Handler logHandler = new OsgiLogDelegateHandler();
      for (String loggerName : loggerNames) {
        Logger logger = Logger.getLogger(loggerName);
        logger.removeHandler(lastUsedHandler);
        logger.addHandler(logHandler);
      }
      lastUsedHandler = logHandler;
    }
  }

  synchronized void unbindLogService(LogService logService) {
    if (logService != null) {
      logServices.remove(logService);
      if (logServices.isEmpty()) {
        for (String loggerName : loggerNames) {
          Logger logger = Logger.getLogger(loggerName);
          logger.removeHandler(lastUsedHandler);
          logger.addHandler(defaultHandler);
        }
        lastUsedHandler = defaultHandler;
      }
    }
  }

  private class OsgiLogDelegateHandler extends Handler {

    @Override
    public void publish(LogRecord record) {
      if (record.getLevel() == Level.OFF) {
        return;
      }
      if (record.getThrown() != null) {
        logThrowable(record);
      } else {
        log(record);
      }
    }

    private void logThrowable(LogRecord record) {
      for (LogService logService: logServices) {
        logService.log(asOsgiLogLevel(record.getLevel()), record.getMessage(), record.getThrown());
      }
    }

    private void log(LogRecord record) {
      for (LogService logService: logServices) {
        logService.log(asOsgiLogLevel(record.getLevel()), record.getMessage());
      }
    }

    @Override
    public void close() throws SecurityException {
    }

    @Override
    public void flush() {
    }

    private int asOsgiLogLevel(Level level) {
      if (level == Level.SEVERE) {
        return LogService.LOG_ERROR;
      } else if (level == Level.WARNING) {
        return LogService.LOG_WARNING;
      } else if (level == Level.INFO || level == Level.CONFIG
          || level == Level.FINE) {
        return LogService.LOG_INFO;
      } else {
        return LogService.LOG_DEBUG;
      }
    }
  }
}

