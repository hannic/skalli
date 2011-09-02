package org.eclipse.skalli.core.utils;

import java.io.InputStream;
import java.text.MessageFormat;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.eclipse.skalli.common.Consts;
import org.eclipse.skalli.core.internal.persistence.xstream.FileStorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConfigurationProperties {

    private static final Logger LOG = LoggerFactory.getLogger(ConfigurationProperties.class);

    public static String getProperty(String propertyName) {
        return getProperty(propertyName, null);
    }

    public static String getProperty(String propertyName, String defaultValue) {
        String propertyValue = System.getProperty(propertyName);
        if (StringUtils.isBlank(propertyValue)) {
            LOG.info(MessageFormat.format("System property ''{0}'' is undefined", propertyName)); //$NON-NLS-1$
            try {
                // search property in /skalli.properties
                Properties properties = new Properties();
                InputStream skalliPropertiesStream = ConfigurationProperties.class
                        .getResourceAsStream(Consts.PROPERTIES_RESOURCE);
                if (skalliPropertiesStream != null) {
                    properties.load(skalliPropertiesStream);
                    propertyValue = (String) properties.get(propertyName);
                }
            } catch (Exception e) {
                LOG.info(MessageFormat.format("Failed to retrieve property ''{0}'' from resource file ''{1}''", //$NON-NLS-1$
                        propertyName, Consts.PROPERTIES_RESOURCE));
            }
        }

        if (StringUtils.isBlank(propertyValue)) {
            // fall back: use the given default value
            propertyValue = defaultValue;
        }
        return propertyValue;
    }

    public static String getConfiguredStorageService() {
        return getProperty(Consts.PROPERTY_STORAGE_SERVICE, FileStorageService.class.getName());
    }
}
