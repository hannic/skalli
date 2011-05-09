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

import org.eclipse.skalli.common.configuration.ConfigKey;

public enum ConfigKeyUserStore implements ConfigKey {

    TYPE("userstore.type", "local", false), //$NON-NLS-1$ //$NON-NLS-2$
    USE_LOCAL_FALLBACK("userstore.use-local-fallback", "true", false); //$NON-NLS-1$ //$NON-NLS-2$

    private final String key;
    private final String defaultValue;
    private final boolean isEncrypted;

    private ConfigKeyUserStore(String key, String defaultValue, boolean isEncrypted) {
        this.key = key;
        this.defaultValue = defaultValue;
        this.isEncrypted = isEncrypted;
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public String getDefaultValue() {
        return defaultValue;
    }

    @Override
    public boolean isEncrypted() {
        return isEncrypted;
    }

}
