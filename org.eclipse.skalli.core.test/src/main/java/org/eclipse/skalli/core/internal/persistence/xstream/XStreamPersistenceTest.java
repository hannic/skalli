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

import static org.junit.Assert.*;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;

import javax.xml.bind.DatatypeConverter;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.easymock.EasyMock;
import org.eclipse.skalli.common.util.XMLUtils;
import org.eclipse.skalli.model.ext.DataMigration;
import org.eclipse.skalli.model.ext.EntityBase;
import org.eclipse.skalli.testutil.PropertyHelperUtils;
import org.eclipse.skalli.testutil.TestExtensibleEntityBase;
import org.eclipse.skalli.testutil.TestExtension;
import org.eclipse.skalli.testutil.TestExtension1;
import org.eclipse.skalli.testutil.TestUtils;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

@SuppressWarnings("nls")
public class XStreamPersistenceTest {

    private static final String ALIAS_EXT1 = "ext1";
    private static final String ALIAS_EXT2 = "ext2";
    private static final String TIME0 = "2002-12-05T05:22:11Z";
    private static final String TIME1 = "2011-02-25T14:33:48";
    private static final String TIME2 = "2010-06-17T21:00:23Z";
    private static final String USER0 = "hugo";
    private static final String USER1 = "homer";
    private static final String USER2 = "marge";


    private static final String XML_WITH_VERSION = "<bla version=\"42\"><uuid>"+PropertyHelperUtils.TEST_UUIDS[0]+"</uuid><hello>world</hello><blubb>noop</blubb></bla>";
    private static final String XML_WITHOUT_VERSION = "<bla><uuid>"+PropertyHelperUtils.TEST_UUIDS[0]+"</uuid><hello>world</hello><blubb>noop</blubb></bla>";

    private static final String XML_WITH_MODIFIED_ATTRIBUTES =
            "<root lastModified=\"" + TIME0 + "\" modifiedBy=\"" + USER0 + "\"><extensions>" +
                    "<" + ALIAS_EXT1 + " lastModified=\"" + TIME1 + "\" modifiedBy=\"" + USER1 + "\"></" + ALIAS_EXT1
                    + ">" +
                    "<" + ALIAS_EXT2 + " lastModified=\"" + TIME2 + "\" modifiedBy=\"" + USER2 + "\"></" + ALIAS_EXT2
                    + ">" +
                    "</extensions></root>";

    private static final String XML_WITH_EXTENSIONS =
            "<root><extensions>" +
                    "<" + ALIAS_EXT1 + "><string>string</string></" + ALIAS_EXT1 + ">" +
                    "<" + ALIAS_EXT2 + "></" + ALIAS_EXT2 + ">" +
                    "</extensions></root>";

    private static final String XML_WITH_EXTENSIONS_MODIFIED =
            "<root><extensions>" +
                    "<" + ALIAS_EXT1 + "><string>modified_string</string></" + ALIAS_EXT1 + ">" +
                    "<" + ALIAS_EXT2 + "></" + ALIAS_EXT2 + ">" +
                    "</extensions></root>";

    private static Map<String, Class<?>> getAliases() {
        Map<String, Class<?>> aliases = new HashMap<String, Class<?>>();
        aliases.put(ALIAS_EXT1, TestExtension.class);
        aliases.put(ALIAS_EXT2, TestExtension1.class);
        return aliases;
    }

    private static Map<String, Class<?>> getNotMatchingAliases() {
        Map<String, Class<?>> aliases = new HashMap<String, Class<?>>();
        aliases.put("notext1", TestExtension.class);
        aliases.put("notext2", TestExtension1.class);
        return aliases;
    }

    private static TestExtensibleEntityBase getExtensibleEntity() {
        TestExtensibleEntityBase entity = new TestExtensibleEntityBase(PropertyHelperUtils.TEST_UUIDS[0]);
        entity.addExtension(new TestExtension());
        entity.addExtension(new TestExtension1());
        return entity;
    }

    private static DataMigration getMigrationMock() throws Exception {
        DataMigration mockMigration = EasyMock.createMock(DataMigration.class);
        EasyMock.reset(mockMigration);
        mockMigration.handlesType(EasyMock.isA(String.class));
        EasyMock.expectLastCall().andReturn(true).anyTimes();
        mockMigration.getFromVersion();
        EasyMock.expectLastCall().andReturn(42).anyTimes();
        mockMigration.migrate(EasyMock.isA(Document.class));
        EasyMock.expectLastCall();
        return mockMigration;
    }

    private static class TestXStreamPersistence extends XStreamPersistence {

        private final int CURRENT_VERSION = 43;

        public TestXStreamPersistence() {
            super(new FileStorageService(new File("")));
        }

        public TestXStreamPersistence(File storageBase) {
            super(new FileStorageService(storageBase));
        }

        @Override
        public int getCurrentVersion() {
            return CURRENT_VERSION;
        }

    }

    @Test
    public void testSaveLoadCycle() throws Exception {
        File stroageBaseDir = null;
        try {
            TestExtensibleEntityBase entity = getExtensibleEntity();
            Map<String, Class<?>> aliases = getAliases();

            stroageBaseDir = TestUtils.createTempDir("testSaveLoadCycle");
            XStreamPersistence xp = new TestXStreamPersistence(stroageBaseDir);
            xp.saveEntity(entity, USER0, aliases);

            File entityFile = new File(new File(stroageBaseDir, entity.getClass().getSimpleName()), entity.getUuid() + ".xml");
            Document savedDoc = XMLUtils.documentFromFile(entityFile);
            String lastModified = xp.getLastModifiedAttribute(savedDoc.getDocumentElement());
            assertNotNull(lastModified);
            SortedMap<String, Element> extensions = xp.getExtensionsByAlias(savedDoc, aliases);
            String lastModifiedExt1 = xp.getLastModifiedAttribute(extensions.get(ALIAS_EXT1));
            assertIsXsdDateTime(lastModifiedExt1);
            String lastModifiedExt2 = xp.getLastModifiedAttribute(extensions.get(ALIAS_EXT1));
            assertIsXsdDateTime(lastModifiedExt2);

            Set<ClassLoader> entityClassLoaders = new HashSet<ClassLoader>();
            entityClassLoaders.add(TestExtensibleEntityBase.class.getClassLoader());
            entityClassLoaders.add(TestExtension.class.getClassLoader());
            entityClassLoaders.add(TestExtension1.class.getClassLoader());
            DataMigration mockMigration = getMigrationMock();
            EasyMock.replay(mockMigration);
            Document doc = xp.readEntityAsDom(entity.getClass(), entity.getUuid());
            xp.preProcessXML(doc, Collections.singleton(mockMigration));
            EntityBase entity1 = xp.domToEntity(entityClassLoaders, aliases, doc);
            xp.postProcessEntity(doc, entity1, aliases);
            EntityBase loadedEntity = entity1;
            assertNotNull(loadedEntity);
            assertTrue(loadedEntity instanceof TestExtensibleEntityBase);
            assertEquals(lastModified, loadedEntity.getLastModified());
            assertEquals(USER0, loadedEntity.getLastModifiedBy());
            TestExtension ext1 = ((TestExtensibleEntityBase) loadedEntity).getExtension(TestExtension.class);
            assertNotNull(ext1);
            assertEquals(lastModifiedExt1, ext1.getLastModified());
            assertEquals(USER0, ext1.getLastModifiedBy());
            TestExtension1 ext2 = ((TestExtensibleEntityBase) loadedEntity).getExtension(TestExtension1.class);
            assertNotNull(ext2);
            assertEquals(lastModifiedExt2, ext2.getLastModified());
            assertEquals(USER0, ext2.getLastModifiedBy());

        } finally {
            FileUtils.deleteDirectory(stroageBaseDir);
        }
    }

    @Test
    public void testPreProcessXML() throws Exception {
        XStreamPersistence xp = new TestXStreamPersistence();
        DataMigration mockMigration = getMigrationMock();
        EasyMock.replay(mockMigration);
        Document doc = XMLUtils.documentFromString(XML_WITH_VERSION);
        xp.preProcessXML(doc, Collections.singleton(mockMigration));
        String res = XMLUtils.documentToString(doc);
        assertFalse(res.contains("version=\"3\""));
        EasyMock.verify(mockMigration);
    }

    @Test
    public void testPostProcessEntity() throws Exception {
        XStreamPersistence xp = new TestXStreamPersistence();
        Document doc = XMLUtils.documentFromString(XML_WITH_MODIFIED_ATTRIBUTES);
        Map<String, Class<?>> aliases = getAliases();
        TestExtensibleEntityBase entity = getExtensibleEntity();
        xp.postProcessEntity(doc, entity, aliases);
        assertEquals(TIME0, entity.getLastModified());
        assertEquals(USER0, entity.getLastModifiedBy());
        TestExtension ext1 = entity.getExtension(TestExtension.class);
        assertNotNull(ext1);
        assertEquals(TIME1, ext1.getLastModified());
        assertEquals(USER1, ext1.getLastModifiedBy());
        TestExtension ext2 = entity.getExtension(TestExtension1.class);
        assertNotNull(ext2);
        assertEquals(TIME2, ext2.getLastModified());
        assertEquals(USER2, ext2.getLastModifiedBy());
    }

    @Test
    public void testPostProcessXML() throws Exception {
        XStreamPersistence xp = new TestXStreamPersistence();
        Document oldDoc = XMLUtils.documentFromString(XML_WITH_EXTENSIONS);
        Document newDoc = XMLUtils.documentFromString(XML_WITH_EXTENSIONS_MODIFIED);
        Map<String, Class<?>> aliases = getAliases();
        xp.postProcessXML(newDoc, oldDoc, aliases, USER0);
        Element documentElement = newDoc.getDocumentElement();
        assertIsXsdDateTime(xp.getLastModifiedAttribute(documentElement));
        assertEquals(USER0, xp.getLastModifiedByAttribute(documentElement));
        SortedMap<String, Element> extensions = xp.getExtensionsByAlias(newDoc, aliases);
        Element ext1 = extensions.get(ALIAS_EXT1);
        assertIsXsdDateTime(xp.getLastModifiedAttribute(ext1));
        assertEquals(USER0, xp.getLastModifiedByAttribute(ext1));
        Element ext2 = extensions.get(ALIAS_EXT2);
        assertNull(xp.getLastModifiedAttribute(ext2));
        assertNull(xp.getLastModifiedByAttribute(ext2));
        assertEquals(xp.getCurrentVersion(), xp.getVersionAttribute(newDoc));
    }

    @Test
    public void testPostProcessXMLUnchangedDocument() throws Exception {
        XStreamPersistence xp = new TestXStreamPersistence();
        Document doc = XMLUtils.documentFromString(XML_WITH_EXTENSIONS);
        Map<String, Class<?>> aliases = getAliases();
        xp.postProcessXML(doc, doc, aliases, USER0);
        Element documentElement = doc.getDocumentElement();
        assertNull(xp.getLastModifiedAttribute(documentElement));
        assertNull(xp.getLastModifiedByAttribute(documentElement));
        SortedMap<String, Element> extensions = xp.getExtensionsByAlias(doc, aliases);
        for (Element element : extensions.values()) {
            assertNull(xp.getLastModifiedAttribute(element));
            assertNull(xp.getLastModifiedByAttribute(element));
        }
        assertEquals(xp.getCurrentVersion(), xp.getVersionAttribute(doc));
    }

    @Test
    public void testGetExtensionsByAlias() throws Exception {
        XStreamPersistence xp = new TestXStreamPersistence();
        Document doc = XMLUtils.documentFromString(XML_WITH_MODIFIED_ATTRIBUTES);
        Map<String, Class<?>> aliases = getAliases();
        SortedMap<String, Element> extensions = xp.getExtensionsByAlias(doc, aliases);
        assertEquals(2, extensions.size());
        for (String alias : extensions.keySet()) {
            assertTrue(aliases.containsKey(alias));
            assertEquals(alias, extensions.get(alias).getNodeName());
        }
    }

    @Test
    public void testGetExtensionsByClassName() throws Exception {
        XStreamPersistence xp = new TestXStreamPersistence();
        Document doc = XMLUtils.documentFromString(XML_WITH_MODIFIED_ATTRIBUTES);
        Map<String, Class<?>> aliases = getAliases();
        SortedMap<String, Element> extensions = xp.getExtensionsByClassName(doc, aliases);
        assertEquals(2, extensions.size());
        for (String alias : aliases.keySet()) {
            String className = aliases.get(alias).getName();
            assertTrue(extensions.containsKey(className));
            assertEquals(alias, extensions.get(className).getNodeName());
        }
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testGetExtensionsNoAliases() throws Exception {
        XStreamPersistence xp = new TestXStreamPersistence();
        Document doc = XMLUtils.documentFromString(XML_WITH_MODIFIED_ATTRIBUTES);
        SortedMap<String, Element> extensions = xp.getExtensionsByAlias(doc, null);
        assertNotNull(extensions);
        assertTrue(extensions.isEmpty());
        extensions = xp.getExtensionsByAlias(doc, Collections.EMPTY_MAP);
        assertNotNull(extensions);
        assertTrue(extensions.isEmpty());
    }

    @Test
    public void testGetExtensionsNoMatchingAliases() throws Exception {
        XStreamPersistence xp = new TestXStreamPersistence();
        Document doc = XMLUtils.documentFromString(XML_WITH_MODIFIED_ATTRIBUTES);
        Map<String, Class<?>> notMatchingAliases = getNotMatchingAliases();
        SortedMap<String, Element> extensions = xp.getExtensionsByAlias(doc, notMatchingAliases);
        assertNotNull(extensions);
        assertTrue(extensions.isEmpty());
        extensions = xp.getExtensionsByClassName(doc, notMatchingAliases);
        assertNotNull(extensions);
        assertTrue(extensions.isEmpty());
    }

    @Test
    public void testVersionAttribute() throws Exception {
        XStreamPersistence xp = new TestXStreamPersistence();
        Document doc = XMLUtils.documentFromString(XML_WITHOUT_VERSION);
        xp.setVersionAttribute(doc);
        assertEquals(xp.getCurrentVersion(), xp.getVersionAttribute(doc));
    }

    @Test(expected = RuntimeException.class)
    public void testCallSetVersionAttributeTwice() throws Exception {
        XStreamPersistence xp = new TestXStreamPersistence();
        Document doc = XMLUtils.documentFromString(XML_WITHOUT_VERSION);
        xp.setVersionAttribute(doc);
        xp.setVersionAttribute(doc);
    }

    @Test
    public void testLastModified() throws Exception {
        XStreamPersistence xp = new TestXStreamPersistence();
        Document doc = XMLUtils.documentFromString(XML_WITHOUT_VERSION);
        Element documentElement = doc.getDocumentElement();
        xp.setLastModifiedAttribute(documentElement);
        assertIsXsdDateTime(xp.getLastModifiedAttribute(documentElement));
    }

    @Test(expected = RuntimeException.class)
    public void testCallSetLastModifiedTwice() throws Exception {
        XStreamPersistence xp = new TestXStreamPersistence();
        Document doc = XMLUtils.documentFromString(XML_WITHOUT_VERSION);
        Element documentElement = doc.getDocumentElement();
        xp.setLastModifiedAttribute(documentElement);
        xp.setLastModifiedAttribute(documentElement);
    }

    @Test
    public void testLastModifiedBy() throws Exception {
        XStreamPersistence xp = new TestXStreamPersistence();
        Document doc = XMLUtils.documentFromString(XML_WITHOUT_VERSION);
        Element documentElement = doc.getDocumentElement();
        xp.setLastModifiedByAttribute(documentElement, USER0);
        assertEquals(USER0, xp.getLastModifiedByAttribute(documentElement));
    }

    @Test(expected = RuntimeException.class)
    public void testCallSetLastModifiedByTwice() throws Exception {
        XStreamPersistence xp = new TestXStreamPersistence();
        Document doc = XMLUtils.documentFromString(XML_WITHOUT_VERSION);
        Element documentElement = doc.getDocumentElement();
        xp.setLastModifiedByAttribute(documentElement, USER0);
        xp.setLastModifiedByAttribute(documentElement, USER0);
    }

    @Test
    public void testNonIdenticalElements() throws Exception {
        XStreamPersistence xp = new TestXStreamPersistence();
        Document doc = XMLUtils.documentFromString(XML_WITHOUT_VERSION);
        Element documentElement = doc.getDocumentElement();
        Map<String, Class<?>> aliases = getAliases();
        SortedMap<String, Element> extensions = xp.getExtensionsByClassName(doc, aliases);
        for (Element element : extensions.values()) {
            assertFalse(XMLDiff.identical(documentElement, element));
        }
    }

    @Test
    public void testIdenticalWithNullParams() throws Exception {
        XStreamPersistence xp = new TestXStreamPersistence();
        Document doc = XMLUtils.documentFromString(XML_WITHOUT_VERSION);
        Element documentElement = doc.getDocumentElement();
        assertTrue(XMLDiff.identical(null, null));
        assertFalse(XMLDiff.identical(null, documentElement));
        assertFalse(XMLDiff.identical(documentElement, null));
    }

    @Test
    public void testIdenticalSameElement() throws Exception {
        XStreamPersistence xp = new TestXStreamPersistence();
        Document doc = XMLUtils.documentFromString(XML_WITHOUT_VERSION);
        Element documentElement = doc.getDocumentElement();
        assertTrue(XMLDiff.identical(documentElement, documentElement));
    }

    private void assertIsXsdDateTime(String lexicalXSDDateTime) {
        assertTrue(StringUtils.isNotBlank(lexicalXSDDateTime));
        DatatypeConverter.parseDateTime(lexicalXSDDateTime);
    }
}
