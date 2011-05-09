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
package org.eclipse.skalli.core.internal.users;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("userStore")
public class UserStoreConfig {

    private String type;
    private String useLocalFallback;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getUseLocalFallback() {
        return useLocalFallback;
    }

    public void setUseLocalFallback(String useLocalFallback) {
        this.useLocalFallback = useLocalFallback;
    }

}
