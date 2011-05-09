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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.eclipse.equinox.security.storage.ISecurePreferences;
import org.eclipse.equinox.security.storage.SecurePreferencesFactory;
import org.eclipse.equinox.security.storage.StorageException;
import org.osgi.service.component.ComponentContext;

import org.eclipse.skalli.api.java.EventService;
import org.eclipse.skalli.api.java.events.EventConfigUpdate;
import org.eclipse.skalli.api.java.events.EventCustomizingUpdate;
import org.eclipse.skalli.common.configuration.ConfigKey;
import org.eclipse.skalli.common.configuration.ConfigTransaction;
import org.eclipse.skalli.common.configuration.ConfigurationService;
import org.eclipse.skalli.core.internal.persistence.CompositeEntityClassLoader;
import org.eclipse.skalli.log.Log;
import com.thoughtworks.xstream.XStream;

public class ConfigurationComponent implements ConfigurationService {
    private static final Logger LOG = Log.getLogger(ConfigurationComponent.class);
    private static final String SECURESTORE = "storage/securestore.txt"; //$NON-NLS-1$
    private static final String CUSTOMIZATION_FOLDER = "customization/"; //$NON-NLS-1$
    private ISecurePreferences factory;
    private EventService eventService;

    private final Map<ConfigTransaction, Map<ConfigKey, String>> transactions = new HashMap<ConfigTransaction, Map<ConfigKey, String>>(
            0);

    protected void activate(ComponentContext context) {
        LOG.info("Configuration service activated"); //$NON-NLS-1$
    }

    protected void deactivate(ComponentContext context) {
        LOG.info("Configuration service deactivated"); //$NON-NLS-1$
    }

    protected void bindEventService(EventService eventService) {
        this.eventService = eventService;
    }

    protected void unbindEventService(EventService eventService) {
        this.eventService = null;
    }

    public ConfigurationComponent() {
        factory = getFactory();
    }

    ISecurePreferences getFactory() {
        File file = getWorkdirFile(SECURESTORE);
        try {
            return SecurePreferencesFactory.open(file.getAbsoluteFile().toURI().toURL(), null);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String readString(ConfigKey key) {
        try {
            return factory.get(key.getKey(), key.getDefaultValue());
        } catch (StorageException e) {
            throw new RuntimeException(e);
        }
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
        try {
            for (Entry<ConfigKey, String> entry : tx.entrySet()) {
                factory.put(entry.getKey().getKey(), entry.getValue(), entry.getKey().isEncrypted());
            }
            factory.flush();
        } catch (StorageException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        if (eventService != null) {
            eventService.fireEvent(new EventConfigUpdate(tx.keySet().toArray(new ConfigKey[tx.size()])));
        }
    }

    @Override
    public void rollback(ConfigTransaction tx) {
        transactions.remove(tx);
    }

    private XStream getXStream(Class<?> customizationClass) {
        XStream xstream = new XStream();
        xstream.setClassLoader(new CompositeEntityClassLoader(
                Collections.singleton(customizationClass.getClassLoader())));
        return xstream;
    }

    private String getFilenameFromCustomizationName(String customizationName) {
        File path = getWorkdirFile(CUSTOMIZATION_FOLDER);
        if (!path.exists()) {
            path.mkdirs();
        }
        return path.toString() + IOUtils.DIR_SEPARATOR + customizationName + ".xml"; //$NON-NLS-1$
    }

    @Override
    public <T> void writeCustomization(String customizationKey, T customization) {
        FileOutputStream os = null;
        try {
            os = new FileOutputStream(getFilenameFromCustomizationName(customizationKey));
            getXStream(customization.getClass()).toXML(customization, os);
            if (eventService != null) {
                eventService.fireEvent(new EventCustomizingUpdate(customizationKey));
            }
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } finally {
            IOUtils.closeQuietly(os);
        }
    };

    @Override
    public <T> T readCustomization(String customizationKey, Class<T> customizationClass) {
        FileInputStream is = null;
        try {
            File file = new File(getFilenameFromCustomizationName(customizationKey));
            if (!file.exists()) {
                return null;
            }
            is = new FileInputStream(file);
            T ret = customizationClass.cast(getXStream(customizationClass).fromXML(is));
            return ret;
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } finally {
            IOUtils.closeQuietly(is);
        }
    }

    @Override
    public File getWorkdirFile(String filename) {
        File parent = null;
        String workDirProperty = FilenameUtils.separatorsToSystem(System.getProperty("workdir")); //$NON-NLS-1$
        if (workDirProperty != null) {
            if (!workDirProperty.endsWith(String.valueOf(IOUtils.DIR_SEPARATOR))) {
                workDirProperty += IOUtils.DIR_SEPARATOR;
            }
            parent = new File(workDirProperty);
            if (!parent.exists() || !parent.isDirectory()) {
                LOG.warning(String
                        .format("Working directory (%s) not found. Using current directory as fallback.", parent.getAbsolutePath())); //$NON-NLS-1$
                parent = null;
            }
        }
        File ret = new File(parent, filename);
        return ret;
    }

}
