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
package org.eclipse.skalli.model.ext.devinf;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.skalli.common.configuration.ConfigurationService;
import org.eclipse.skalli.common.util.ComparatorUtils;
import org.eclipse.skalli.model.ext.AbstractLinkMapper;
import org.eclipse.skalli.model.ext.LinkMappingConfig;
import org.eclipse.skalli.model.ext.devinf.internal.config.ScmLocationMappingResource;

public class ScmLocationMapper extends AbstractLinkMapper {

    /** Purpose filter for mappings that allow to browse content of source locations. */
    public static final String PURPOSE_BROWSE = "browse"; //$NON-NLS-1$

    /** Purpose filter for mappings that allow to review content of source locations. */
    public static final String PURPOSE_REVIEW = "review"; //$NON-NLS-1$

    /** Purpose filter for mappings that provide activity information for a project. */
    public static final String PURPOSE_ACTIVITY = "activity"; //$NON-NLS-1$

    /** Purpose filter for mappings that provide activity details for a project. */
    public static final String PURPOSE_ACTIVITY_DETAILS = "activitydetails"; //$NON-NLS-1$

    /** Purpose filter for mappings that provide the createBug url for the bug tracking system. */
    public static final String PURPOSE_CREATE_BUG = "create_bug"; //$NON-NLS-1$

    public static final String PURPOSE_FEED = "feed"; //$NON-NLS-1$

    public static final String ALL_PROVIDERS = "*"; //$NON-NLS-1$

    public List<ScmLocationMappingConfig> getMappings(ConfigurationService configService, String provider,
            String... purposes) {
        List<ScmLocationMappingConfig> mappings = new ArrayList<ScmLocationMappingConfig>();
        if (configService != null) {
            List<? extends LinkMappingConfig> allMappings = getAllMappings(configService);
            if (allMappings != null) {
                for (LinkMappingConfig mapping : allMappings) {
                    if (matches((ScmLocationMappingConfig) mapping, provider, purposes)) {
                        mappings.add((ScmLocationMappingConfig) mapping);
                    }
                }
            }
        }
        return mappings;
    }

    private boolean matches(ScmLocationMappingConfig mapping, String provider, String... purposes) {
        if (provider == null) {
            provider = ALL_PROVIDERS;
        }
        if (!ALL_PROVIDERS.equals(provider) && !ComparatorUtils.equals(provider, mapping.getProvider())) {
            return false;
        }
        return super.matches(mapping, purposes);
    }

    @Override
    protected List<? extends LinkMappingConfig> getAllMappings(ConfigurationService configService) {
        ScmLocationMappingsConfig mappingsConfig =
                configService.readCustomization(ScmLocationMappingResource.MAPPINGS_KEY,
                        ScmLocationMappingsConfig.class);
        return mappingsConfig != null ? mappingsConfig.getScmMappings() : null;
    }
}
