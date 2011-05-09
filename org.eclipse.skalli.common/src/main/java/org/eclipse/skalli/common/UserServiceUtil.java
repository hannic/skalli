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
package org.eclipse.skalli.common;

import java.util.logging.Logger;

import org.apache.commons.lang.BooleanUtils;
import org.osgi.framework.Constants;

import org.eclipse.skalli.common.configuration.ConfigurationService;
import org.eclipse.skalli.log.Log;

public class UserServiceUtil {
    private static final Logger LOG = Log.getLogger(UserServiceUtil.class);

    private static UserServiceUtil instance = null;

    UserServiceUtil() {
        // prevent instantiation
    }

    public static final UserService getUserService() {
        if (instance == null) {
            instance = new UserServiceUtil();
        }
        return instance.getConfiguredUserService();
    }

    ConfigurationService getConfigService() {
        return Services.getService(ConfigurationService.class);
    }

    UserService getConfiguredUserService() {
        ConfigurationService configService = getConfigService();
        UserService ret = null;
        String type = "(undefined)"; //$NON-NLS-1$
        if (configService != null) {
            type = configService.readString(ConfigKeyUserStore.TYPE);
            ret = getUserServiceByType(type);
        }
        // TODO replace by configService.readBoolean(...)
        if (ret == null
                && (configService == null || BooleanUtils.toBoolean(configService
                        .readString(ConfigKeyUserStore.USE_LOCAL_FALLBACK)))) {
            LOG.info("User service '" + type + "' not found, trying fallback 'local'"); //$NON-NLS-1$ //$NON-NLS-2$
            ret = getUserServiceByType("local"); //$NON-NLS-1$
        }
        if (ret == null) {
            throw new IllegalStateException("No user service registered"); //$NON-NLS-1$
        }
        return ret;
    }

    UserService getUserServiceByType(String type) {
        String filter = "(&(" + Constants.OBJECTCLASS + "=" + UserService.class.getName() + ")(userService.type=" + type + "))"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
        UserService userService = Services.getService(UserService.class, filter);
        return userService;
    }

}
