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

import org.eclipse.skalli.log.LogService;
import org.osgi.service.component.ComponentContext;

/**
 * Manager for {@link Logger loggers}. Binds to {@link LogService log services}
 * registered with the OSGi runtime and redirects {@link LogRecord log messages}
 * to these log services. If no log service is available, it logs to the console.
 */
public class DefaultLoggerManager implements LoggerManager {

    private final Set<String> loggerNames;
    private final Set<LogService> logServices;
    private final Handler defaultHandler;
    private Handler lastUsedHandler;
    private Level defaultLogLevel;

    private static DefaultLoggerManager instance;

    public DefaultLoggerManager() {
        defaultHandler = new ConsoleHandler();
        defaultHandler.setFormatter(new SimpleFormatter());
        lastUsedHandler = defaultHandler;

        loggerNames = new HashSet<String>();
        logServices = new HashSet<LogService>();

        this.defaultLogLevel = Level.INFO;
        String defaultLogLevelProperty = System.getProperty("defaultLogLevel"); //$NON-NLS-1$
        if (defaultLogLevelProperty != null) {
            this.defaultLogLevel = Level.parse(defaultLogLevelProperty);
        }
    }

    protected void activate(ComponentContext context) {
        instance = this;
    }

    protected void deactivate(ComponentContext context) {
        instance = null;
    }

    /**
     * Returns the registered instance of this log manager implementation, or
     * <code>null</code> if currently no instance is registered with the OSGi framework.
     */
    public static DefaultLoggerManager getInstance() {
        return instance;
    }

    /**
     * Returns a {@link Logger} for the given class.
     */
    @Override
    public synchronized Logger getLogger(Class<?> c) {
        Logger logger = Logger.getLogger(c.getName());
        if (!loggerNames.contains(logger.getName())) {
            addLogger(logger);
        }
        return logger;
    }

    /**
     * Sets the default log level for all loggers provided by this logger manager.
     * @param defaultLogLevel  the default log level to set.
     */
    @Override
    public synchronized void setDefaultLogLevel(Level defaultLogLevel) {
        this.defaultLogLevel = defaultLogLevel;
    }

    protected synchronized void bindLogService(LogService logService) {
        if (logService != null) {
            logServices.add(logService);
            if (lastUsedHandler == defaultHandler) {
                updateLoggers(new DelegateHandler());
            }
        }
    }

    protected synchronized void unbindLogService(LogService logService) {
        if (logService != null) {
            logServices.remove(logService);
            logService.flush();
            logService.close();
            if (logServices.isEmpty()) {
                updateLoggers(defaultHandler);
            }
        }
    }

    private void addLogger(Logger logger) {
        logger.setUseParentHandlers(false);
        logger.addHandler(lastUsedHandler);
        if (defaultLogLevel != null) {
            logger.setLevel(defaultLogLevel);
        }
        loggerNames.add(logger.getName());
    }

    private void updateLoggers(Handler newHandler) {
        for (String loggerName : loggerNames) {
            Logger logger = Logger.getLogger(loggerName);
            logger.removeHandler(lastUsedHandler);
            logger.addHandler(newHandler);
        }
        lastUsedHandler = newHandler;
    }

    private class DelegateHandler extends Handler {
        @Override
        public void publish(LogRecord record) {
            if (record.getLevel() == Level.OFF) {
                return;
            }
            for (LogService logService : logServices) {
                logService.log(record);
            }
        }

        @Override
        public void close() throws SecurityException {
            for (LogService logService : logServices) {
                logService.close();
            }
        }

        @Override
        public void flush() {
            for (LogService logService : logServices) {
                logService.flush();
            }
        }
    }
}
