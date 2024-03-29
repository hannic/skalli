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
package org.eclipse.skalli.core.internal.configuration;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.eclipse.skalli.api.java.EventService;
import org.eclipse.skalli.api.java.StorageException;
import org.eclipse.skalli.api.java.StorageService;
import org.eclipse.skalli.api.java.events.EventConfigUpdate;
import org.eclipse.skalli.api.java.events.EventCustomizingUpdate;
import org.eclipse.skalli.common.configuration.ConfigKey;
import org.eclipse.skalli.common.configuration.ConfigTransaction;
import org.eclipse.skalli.common.configuration.ConfigurationService;
import org.eclipse.skalli.core.internal.persistence.CompositeEntityClassLoader;
import org.eclipse.skalli.core.internal.persistence.xstream.IgnoreUnknownElementsMapperWrapper;
import org.eclipse.skalli.core.utils.ConfigurationProperties;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.mapper.MapperWrapper;

public class ConfigurationComponent implements ConfigurationService {
    private static final Logger LOG = LoggerFactory.getLogger(ConfigurationComponent.class);

    private static final String CATEGORY_CUSTOMIZATION = "customization"; //$NON-NLS-1$
    private static final String KEY_PROPERTYSTORE = "PROPERTYSTORE"; //$NON-NLS-1$

    private EventService eventService;
    private StorageService storageService;
    private String storageServiceClassName;

    private final Map<ConfigTransaction, Map<ConfigKey, String>> transactions =
            new HashMap<ConfigTransaction, Map<ConfigKey, String>>(0);

    public ConfigurationComponent() {
        storageServiceClassName = ConfigurationProperties.getConfiguredStorageService();
    }

    /**
     * Constructor for testing purposes: set the class name of the StorageService implementation
     * you want to bind with bindStorageService!
     */
    ConfigurationComponent(String storageServiceClassName) {
        this.storageServiceClassName = storageServiceClassName;
    }

    protected void activate(ComponentContext context) {
        LOG.info("Configuration service activated"); //$NON-NLS-1$
    }

    protected void deactivate(ComponentContext context) {
        LOG.info("Configuration service deactivated"); //$NON-NLS-1$
    }

    protected void bindEventService(EventService eventService) {
        this.eventService = eventService;
        LOG.info(MessageFormat.format("bindEventService({0})", eventService)); //$NON-NLS-1$
    }

    protected void unbindEventService(EventService eventService) {
        LOG.info(MessageFormat.format("unbindEventService({0})", eventService)); //$NON-NLS-1$
        this.eventService = null;
    }

    protected void bindStorageService(StorageService storageService) {
        if (storageServiceClassName.equals(storageService.getClass().getName())) {
            this.storageService = storageService;
            notifyCustomizationChanged(storageService);
            LOG.info(MessageFormat.format("bindStorageService({0})", storageService)); //$NON-NLS-1$
        }
    }

    protected void unbindStorageService(StorageService storageService) {
        if (storageServiceClassName.equals(storageService.getClass().getName())) {
            LOG.info(MessageFormat.format("unbindStorageService({0})", storageService)); //$NON-NLS-1$
            this.storageService = null;
            notifyCustomizationChanged(storageService);
        }
    }

    private void notifyCustomizationChanged(StorageService storageService) {
        if (eventService != null) {
            List<String> customizationKeys = Collections.emptyList();
            try {
                customizationKeys = storageService.keys(CATEGORY_CUSTOMIZATION);
            } catch (StorageException e) {

            }
            for (String customizationKey: customizationKeys) {
                eventService.fireEvent(new EventCustomizingUpdate(customizationKey));
            }
        }
    }

    @Override
    public String readString(ConfigKey key) {
        @SuppressWarnings("unchecked")
        TreeMap<String,String> changes = readCustomization(KEY_PROPERTYSTORE, TreeMap.class);
        String value = null;
        if (changes != null) {
            value = changes.get(key.getKey());
        }
        return value != null? value : key.getDefaultValue();
    }

    @Override
    public void writeString(ConfigTransaction configTransaction, ConfigKey key, String value) {
        Map<ConfigKey, String> tx = getChanges(configTransaction);
        tx.put(key, value);
    }

    private void writeObject(ConfigTransaction configTransaction, ConfigKey key, Object value) {
        if (value == null) {
            writeString(configTransaction, key, null);
        } else {
            writeString(configTransaction, key, value.toString());
        }
    }

    @Override
    public Integer readInteger(ConfigKey key) {
        String value = readString(key);
        if (StringUtils.isEmpty(value)) {
            return null;
        } else {
            return Integer.parseInt(value);
        }
    }

    @Override
    public void writeInteger(ConfigTransaction configTransaction, ConfigKey key, Integer value) {
        writeObject(configTransaction, key, value);
    }

    @Override
    public Boolean readBoolean(ConfigKey key) {
        String value = readString(key);
        if (StringUtils.isEmpty(value)) {
            return null;
        } else {
            return BooleanUtils.toBooleanObject(value);
        }
    }

    @Override
    public void writeBoolean(ConfigTransaction configTransaction, ConfigKey key, Boolean value) {
        writeObject(configTransaction, key, value);
    }

    private Map<ConfigKey, String> getChanges(ConfigTransaction tx) {
        Map<ConfigKey, String> ret = transactions.get(tx);
        if (ret == null) {
            ret = new HashMap<ConfigKey, String>(0);
            transactions.put(tx, ret);
        }
        return ret;
    }

    @Override
    public ConfigTransaction startTransaction() {
        return new ConfigTransaction() {
        };
    }

    @Override
    public void commit(ConfigTransaction configTransaction) {
        Map<ConfigKey, String> tx = getChanges(configTransaction);
        @SuppressWarnings("unchecked")
        TreeMap<String,String> changes = readCustomization(KEY_PROPERTYSTORE, TreeMap.class);
        if (changes == null) {
            changes = new TreeMap<String,String>();
        }
        for (ConfigKey key: tx.keySet()) {
            String value = tx.get(key);
            if (value != null) {
                changes.put(key.getKey(), value);
            } else {
                changes.remove(key.getKey());
            }
        }
        writeCustomization(KEY_PROPERTYSTORE, changes);
        if (eventService != null) {
            eventService.fireEvent(new EventConfigUpdate(tx.keySet().toArray(new ConfigKey[tx.size()])));
        }
    }

    @Override
    public void rollback(ConfigTransaction tx) {
        transactions.remove(tx);
    }

    private XStream getXStream(Class<?> customizationClass) {
        XStream xstream = new XStream() {
            @Override
            protected MapperWrapper wrapMapper(MapperWrapper next) {
                return new IgnoreUnknownElementsMapperWrapper(next);
            }
        };
        ClassLoader classLoader = customizationClass.getClassLoader();
        if (classLoader != null) {
            xstream.setClassLoader(new CompositeEntityClassLoader(Collections.singleton(classLoader)));
        }
        return xstream;
    }

    @Override
    public <T> void writeCustomization(String customizationKey, T customization) {
        if (storageService == null) {
            throw new IllegalStateException("StorageService not available");
        }
        String xml = getXStream(customization.getClass()).toXML(customization);
        InputStream is = null;
        try {
            is = new ByteArrayInputStream(xml.getBytes("UTF-8")); //$NON-NLS-1$
            storageService.write(CATEGORY_CUSTOMIZATION, customizationKey, is);
            if (eventService != null) {
                eventService.fireEvent(new EventCustomizingUpdate(customizationKey));
            }
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException(e);
        } catch (StorageException e) {
            throw new RuntimeException(e);
        } finally {
            IOUtils.closeQuietly(is);
        }
    };

    @Override
    public <T> T readCustomization(String customizationKey, Class<T> customizationClass) {
        if (storageService == null) {
            LOG.warn("Cannot load customization for key " + customizationKey + ": StorageService not available");
            return null;
        }
        InputStream is = null;
        try {
            is = storageService.read(CATEGORY_CUSTOMIZATION, customizationKey);
            if (is == null) {
                return null;
            }
            T ret = customizationClass.cast(getXStream(customizationClass).fromXML(is));
            return ret;
        } catch (StorageException e) {
            throw new RuntimeException(e);
        } finally {
            IOUtils.closeQuietly(is);
        }
    }
}
