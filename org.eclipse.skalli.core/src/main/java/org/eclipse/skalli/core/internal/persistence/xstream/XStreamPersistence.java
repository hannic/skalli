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
package org.eclipse.skalli.core.internal.persistence.xstream;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TimeZone;
import java.util.TreeMap;
import java.util.logging.Logger;

import javax.xml.bind.DatatypeConverter;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.apache.commons.lang.StringUtils;
import org.custommonkey.xmlunit.DifferenceEngine;
import org.custommonkey.xmlunit.ElementNameQualifier;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import org.eclipse.skalli.common.util.XMLUtils;
import org.eclipse.skalli.core.internal.persistence.CompositeEntityClassLoader;
import org.eclipse.skalli.core.internal.persistence.EntityHelper;
import org.eclipse.skalli.log.Log;
import org.eclipse.skalli.model.ext.DataMigration;
import org.eclipse.skalli.model.ext.EntityBase;
import org.eclipse.skalli.model.ext.ExtensibleEntityBase;
import org.eclipse.skalli.model.ext.ExtensionEntityBase;
import org.eclipse.skalli.model.ext.Historized;
import org.eclipse.skalli.model.ext.ValidationException;
import com.thoughtworks.xstream.XStream;

public class XStreamPersistence {

    private static final Logger LOG = Log.getLogger(XStreamPersistence.class);

    private static final String TAG_VERSION = "version"; //$NON-NLS-1$
    private static final String TAG_MODIFIED_BY = "modifiedBy"; //$NON-NLS-1$
    private static final String TAG_LAST_MODIFIED = "lastModified"; //$NON-NLS-1$

    private final File storageBase;

    // TODO "central" versioning together with extensibility sucks!
    /*
     * Alternative idea: Instead of a version number just use unique IDs. A
     * migration then defines a set of other migration's ids which have to be
     * applied as a prerequisite. The Migrator then first collects all registered
     * migrations an then sorts them by their prerequisites. Hypothetis: A
     * migration logically cannot depend on another migration if the unique id of
     * that migration is not known. Open issues: - What should be used as a
     * "version" information in the persisted xml? => Maybe something like a
     * checksum (must be stable with respect to the order of calculation)
     */
    public static final int CURRENT_VERISON = 16;

    int getCurrentVersion() {
        return CURRENT_VERISON;
    }

    public XStreamPersistence(File storageBase) {
        this.storageBase = storageBase;
    }

    XStream getXStreamInstance(Set<ClassLoader> entityClassLoaders, Map<String, Class<?>> aliases) {
        XStream xstream = new IgnoreUnknownElementsXStream();
        xstream.registerConverter(new NoopConverter());
        xstream.registerConverter(new UUIDListConverter());
        xstream.registerConverter(new ExtensionsMapConverter());
        if (entityClassLoaders != null) {
            xstream.setClassLoader(new CompositeEntityClassLoader(entityClassLoaders));
        }
        for (Entry<String, Class<?>> entry : aliases.entrySet()) {
            xstream.alias(entry.getKey(), entry.getValue());
        }
        return xstream;
    }

    File getFile(EntityBase entity) {
        File path = new File(storageBase, entity.getClass().getSimpleName());
        if (!path.exists()) {
            path.mkdirs();
        }
        return new File(path, entity.getUuid() + ".xml"); //$NON-NLS-1$
    }

    File saveToFile(EntityBase entity, String userId, Map<String, Class<?>> aliases) {
        File file = getFile(entity);
        if (entity.getClass().isAnnotationPresent(Historized.class)) {
            new Historian().historize(file, true);
        }

        XStream xstream = getXStreamInstance(null, aliases);
        String xml = xstream.toXML(entity);
        Document newDoc = documentFromString(xml);
        Document oldDoc = null;
        if (file.exists() && file.isFile()) {
            oldDoc = documentFromFile(file);
        }
        try {
            postProcessXML(newDoc, oldDoc, aliases, userId);
        } catch (ValidationException e) {
            throw new RuntimeException(
                    MessageFormat.format("Could not save entity {0} to {1}:\n{2}",
                            entity.getUuid(), file.getAbsolutePath(), e.getMessage()));
        }
        documentToFile(newDoc, file);
        return file;
    }

    EntityBase loadFromFile(Set<ClassLoader> entityClassLoaders, Set<DataMigration> migrations,
            Map<String, Class<?>> aliases, File file) {
        String fileName = file.getAbsolutePath();
        Document doc = documentFromFile(file);
        preProcessXML(doc, migrations, fileName);
        String xml = documentToString(doc);
        XStream xstream = getXStreamInstance(entityClassLoaders, aliases);
        EntityBase entity = (EntityBase) xstream.fromXML(xml);
        if (entity == null) {
            throw new RuntimeException(MessageFormat.format("Could not load entity from {0}", fileName));
        }
        try {
            postProcessEntity(doc, entity, aliases);
        } catch (ValidationException e) {
            throw new RuntimeException(MessageFormat.format("Could not load entity from {0}:\n{1}", fileName,
                    e.getMessage()));
        }
        LOG.info(MessageFormat.format("Loaded entity {0} from {1}", entity.getUuid(), fileName));
        return entity;
    }

    void preProcessXML(Document doc, Set<DataMigration> migrations, String fileName) {
        int version = getVersionAttribute(doc);
        if (migrations != null) {
            DataMigrator migrator = new DataMigrator(migrations);
            try {
                migrator.migrate(doc, version, getCurrentVersion(), fileName);
            } catch (ValidationException e) {
                throw new RuntimeException(e);
            }
        }
    }

    void postProcessXML(Document newDoc, Document oldDoc, Map<String, Class<?>> aliases, String userId)
            throws ValidationException {
        Element newDocElement = newDoc.getDocumentElement();
        Element oldDocElement = oldDoc != null ? oldDoc.getDocumentElement() : null;
        if (!identical(newDocElement, oldDocElement)) {
            setLastModifiedAttribute(newDocElement);
            setLastModifiedByAttribute(newDocElement, userId);
            SortedMap<String, Element> newExts = getExtensionsByAlias(newDoc, aliases);
            SortedMap<String, Element> oldExts = oldDoc != null ? getExtensionsByAlias(oldDoc, aliases) : null;
            for (String alias : newExts.keySet()) {
                Element newExt = newExts.get(alias);
                Element oldExt = oldExts != null ? oldExts.get(alias) : null;
                if (!identical(newExt, oldExt)) {
                    setLastModifiedAttribute(newExt);
                    setLastModifiedByAttribute(newExt, userId);
                } else if (oldExt != null) {
                    setLastModifiedAttribute(newExt, getLastModifiedAttribute(oldExt));
                    setLastModifiedByAttribute(newExt, getLastModifiedByAttribute(oldExt));
                }
            }
        }
        setVersionAttribute(newDoc);
    }

    void postProcessEntity(Document doc, EntityBase entity, Map<String, Class<?>> aliases)
            throws ValidationException {
        EntityHelper.normalize(entity);
        Element docElement = doc.getDocumentElement();
        entity.setLastModified(getLastModifiedAttribute(docElement));
        entity.setLastModifiedBy(getLastModifiedByAttribute(docElement));
        if (entity instanceof ExtensibleEntityBase) {
            ExtensibleEntityBase extensible = (ExtensibleEntityBase) entity;
            Map<String, Element> extensionElements = getExtensionsByClassName(doc, aliases);
            SortedSet<ExtensionEntityBase> extensions = extensible.getAllExtensions();
            for (ExtensionEntityBase extension : extensions) {
                String extensionClassName = extension.getClass().getName();
                Element extensionElement = extensionElements.get(extensionClassName);
                if (extensionElement != null) {
                    extension.setLastModified(getLastModifiedAttribute(extensionElement));
                    extension.setLastModifiedBy(getLastModifiedByAttribute(extensionElement));
                }
            }
        }
    }

    SortedMap<String, Element> getExtensionsByClassName(Document doc, Map<String, Class<?>> aliases)
            throws ValidationException {
        return getExtensions(doc, aliases, true);
    }

    SortedMap<String, Element> getExtensionsByAlias(Document doc, Map<String, Class<?>> aliases)
            throws ValidationException {
        return getExtensions(doc, aliases, false);
    }

    private SortedMap<String, Element> getExtensions(Document doc, Map<String, Class<?>> aliases, boolean byClassName)
            throws ValidationException {
        TreeMap<String, Element> result = new TreeMap<String, Element>();
        if (aliases != null && aliases.size() > 0) {
            List<Element> extensionElements = XMLUtils.getExtensions(doc, aliases.keySet());
            for (Element extensionElement : extensionElements) {
                String name = extensionElement.getNodeName();
                if (byClassName) {
                    Class<?> extensionClass = aliases.get(name);
                    if (extensionClass != null) {
                        result.put(extensionClass.getName(), extensionElement);
                    }
                } else {
                    result.put(name, extensionElement);
                }
            }
        }
        return result;
    }

    Document documentFromString(String xml) {
        Document doc = null;
        try {
            doc = XMLUtils.documentFromString(xml);
        } catch (ParserConfigurationException e) {
            throw new RuntimeException(e);
        } catch (SAXException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return doc;
    }

    String documentToString(Document doc) {
        String result = null;
        try {
            result = XMLUtils.documentToString(doc);
        } catch (TransformerException e) {
            throw new RuntimeException(e);
        }
        return result;
    }

    Document documentFromFile(File file) {
        Document doc = null;
        try {
            doc = XMLUtils.documentFromFile(file);
        } catch (ParserConfigurationException e) {
            throw new RuntimeException(e);
        } catch (SAXException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return doc;
    }

    void documentToFile(Document doc, File file) {
        try {
            XMLUtils.documentToFile(doc, file);
        } catch (TransformerException e) {
            throw new RuntimeException(e);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    int getVersionAttribute(Document doc) {
        int version = 0;
        String versionAttr = doc.getDocumentElement().getAttribute(TAG_VERSION);
        if (StringUtils.isNotBlank(versionAttr)) {
            version = Integer.parseInt(versionAttr);
        }
        return version;
    }

    void setVersionAttribute(Document doc) {
        Element documentElement = doc.getDocumentElement();
        if (doc.getDocumentElement().hasAttribute(TAG_VERSION)) {
            throw new RuntimeException(MessageFormat.format("<{0}> element already has a ''{1}'' attribute",
                    documentElement.getNodeName(), TAG_VERSION));
        }
        documentElement.setAttribute(TAG_VERSION, Integer.toString(getCurrentVersion()));
    }

    String getLastModifiedAttribute(Element element) {
        String value = element.getAttribute(TAG_LAST_MODIFIED);
        return StringUtils.isNotBlank(value) ? value : null;
    }

    void setLastModifiedAttribute(Element element) {
        if (element.hasAttribute(TAG_LAST_MODIFIED)) {
            throw new RuntimeException(MessageFormat.format("<{0}> element already has a ''{1}'' attribute",
                    element.getNodeName(), TAG_LAST_MODIFIED));
        }
        Calendar now = Calendar.getInstance(TimeZone.getTimeZone("UTC"), Locale.ENGLISH); //$NON-NLS-1$
        String lastModified = DatatypeConverter.printDateTime(now);
        setLastModifiedAttribute(element, lastModified);
    }

    void setLastModifiedAttribute(Element element, String lastModified) {
        if (StringUtils.isNotBlank(lastModified)) {
            element.setAttribute(TAG_LAST_MODIFIED, lastModified);
        }
    }

    String getLastModifiedByAttribute(Element element) {
        String value = element.getAttribute(TAG_MODIFIED_BY);
        return StringUtils.isNotBlank(value) ? value : null;
    }

    void setLastModifiedByAttribute(Element element, String userId) {
        if (element.hasAttribute(TAG_MODIFIED_BY)) {
            throw new RuntimeException(MessageFormat.format("<{0}> element already has a ''{1}'' attribute",
                    element.getNodeName(), TAG_MODIFIED_BY));
        }
        if (StringUtils.isNotBlank(userId)) {
            element.setAttribute(TAG_MODIFIED_BY, userId);
        }
    }

    boolean identical(Element newElement, Element oldElement) {
        if (newElement != null && oldElement == null || newElement == null && oldElement != null) {
            return false;
        }
        XMLDiff diff = new XMLDiff();
        DifferenceEngine engine = new DifferenceEngine(diff);
        engine.compare(newElement, oldElement, diff, new ElementNameQualifier());
        return diff.identical();
    }
}
