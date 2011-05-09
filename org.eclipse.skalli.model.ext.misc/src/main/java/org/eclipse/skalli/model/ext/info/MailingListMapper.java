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
package org.eclipse.skalli.model.ext.info;

import java.util.List;

import org.eclipse.skalli.common.configuration.ConfigurationService;
import org.eclipse.skalli.model.ext.AbstractLinkMapper;
import org.eclipse.skalli.model.ext.LinkMappingConfig;
import org.eclipse.skalli.model.ext.info.internal.config.MailingListMappingResource;

public class MailingListMapper extends AbstractLinkMapper {

    /** Purpose filter for mappings that allow to browse content of mailing lists. */
    public static final String PURPOSE_BROWSE = "browse"; //$NON-NLS-1$

    /** Purpose filter for mappings that allow to browse content of mailing list archives. */
    public static final String PURPOSE_BROWSE_ARCHIVE = "browse-archive"; //$NON-NLS-1$

    /** Purpose filter for mappings that allow to register to mailing lists. */
    public static final String PURPOSE_REGISTER = "register"; //$NON-NLS-1$

    /** Purpose filter for mappings that allow to unregister from mailing lists. */
    public static final String PURPOSE_UNREGISTER = "unregister"; //$NON-NLS-1$

    @Override
    protected List<? extends LinkMappingConfig> getAllMappings(ConfigurationService configService) {
        MailingListMappingsConfig mappingsConfig =
                configService.readCustomization(MailingListMappingResource.MAPPINGS_KEY,
                        MailingListMappingsConfig.class);
        return mappingsConfig != null ? mappingsConfig.getMailingListMappings() : null;
    }
}
