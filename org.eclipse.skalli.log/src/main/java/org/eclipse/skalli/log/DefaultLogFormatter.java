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
package org.eclipse.skalli.log;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateFormatUtils;

public class DefaultLogFormatter extends Formatter {

    @SuppressWarnings("nls")
    @Override
    public String format(LogRecord record) {
        StringBuilder sb = new StringBuilder();

        sb.append(DateFormatUtils.formatUTC(record.getMillis(), "yyyy-MM-dd'T'HH:mm:ss")).append("Z");
        sb.append('#').append(record.getMillis());
        sb.append('#').append(record.getThreadID());
        sb.append('#').append(record.getLevel());

        if (StringUtils.isNotBlank(record.getSourceClassName())) {
            sb.append('#').append(record.getSourceClassName());
        } else if (StringUtils.isNotBlank(record.getLoggerName())) {
            sb.append('#').append(record.getLoggerName());
        }
        if (StringUtils.isNotBlank(record.getSourceMethodName())) {
            sb.append('#').append(record.getSourceMethodName()).append("()");
        }

        sb.append('#').append(formatMessage(record));

        Throwable t = record.getThrown();
        if (t != null) {
            sb.append('\n');
            if (StringUtils.isNotBlank(t.getMessage())) {
                sb.append(t.getMessage());
            }
            PrintWriter pw = null;
            try {
                StringWriter sw = new StringWriter();
                pw = new PrintWriter(sw);
                t.printStackTrace(pw);
                sb.append(sw.toString());
            } finally {
                pw.close();
            }
        }
        return sb.toString();
    }

}
