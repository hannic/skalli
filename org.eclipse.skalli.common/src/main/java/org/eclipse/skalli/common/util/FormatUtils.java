package org.eclipse.skalli.common.util;

import org.apache.commons.lang.time.DateFormatUtils;

public class FormatUtils {

    public static String formatUTCWithMillis(long millis) {
        return DateFormatUtils.formatUTC(millis, "yyyy-MM-dd'T'HH:mm:ss.SSSZ"); //$NON-NLS-1$
    }

}
