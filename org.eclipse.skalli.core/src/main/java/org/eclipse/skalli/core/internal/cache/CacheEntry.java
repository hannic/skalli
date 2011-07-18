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
package org.eclipse.skalli.core.internal.cache;

/* package */class CacheEntry<T_META, T_VALUE> {

    private T_META metaInfo;
    private final T_VALUE value;

    public T_META getMetaInfo() {
        return metaInfo;
    }

    public void setMetaInfo(T_META metaInfo) {
        this.metaInfo = metaInfo;
    }

    public T_VALUE getValue() {
        return value;
    }

    /* package */CacheEntry(T_META metaInfo, T_VALUE value) {
        super();
        this.metaInfo = metaInfo;
        this.value = value;
    }
}
