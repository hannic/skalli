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

import java.io.IOException;

import org.easymock.EasyMock;
import org.eclipse.equinox.security.storage.ISecurePreferences;
import org.eclipse.equinox.security.storage.StorageException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import org.eclipse.skalli.api.java.EventService;
import org.eclipse.skalli.api.java.events.EventCustomizingUpdate;
import org.eclipse.skalli.common.configuration.ConfigKey;
import org.eclipse.skalli.common.configuration.ConfigTransaction;

@SuppressWarnings("nls")
public class ConfigurationComponentTest {

    private ISecurePreferences mockFactory;
    private Object[] mocks;
    private ConfigurationComponent cc;

    class TestCC extends ConfigurationComponent {
        public TestCC() {
        }

        @Override
        ISecurePreferences getFactory() {
            return mockFactory;
        }
    }

    ConfigKey testConfigKey1 = new ConfigKey() {
        @Override
        public boolean isEncrypted() {
            return false;
        }

        @Override
        public String getKey() {
            return "key1";
        }

        @Override
        public String getDefaultValue() {
            return "default1";
        }
    };
    ConfigKey testConfigKeyInt = new ConfigKey() {
        @Override
        public boolean isEncrypted() {
            return false;
        }

        @Override
        public String getKey() {
            return "keyInt";
        }

        @Override
        public String getDefaultValue() {
            return "42";
        }
    };
    ConfigKey testConfigKeyBoolean = new ConfigKey() {
        @Override
        public boolean isEncrypted() {
            return false;
        }

        @Override
        public String getKey() {
            return "keyBool";
        }

        @Override
        public String getDefaultValue() {
            return "false";
        }
    };

    @Before
    public void setup() {
        mockFactory = EasyMock.createMock(ISecurePreferences.class);
        mocks = new Object[] { mockFactory };
        cc = new TestCC();

        EasyMock.reset(mocks);
    }

    @Test
    public void testCustomization() {
        EventService mockEventService = EasyMock.createMock(EventService.class);

        EasyMock.reset(mockEventService);

        mockEventService.fireEvent(EasyMock.isA(EventCustomizingUpdate.class));

        EasyMock.replay(mockEventService);

        CustomizationData c1 = new CustomizationData();
        c1.prop1 = "Hello";
        c1.prop2 = "World";

        ConfigurationComponent ccOrig = new ConfigurationComponent();
        ccOrig.bindEventService(mockEventService);

        ccOrig.writeCustomization("key1", c1);
        EasyMock.verify(mockEventService);
        CustomizationData res = ccOrig.readCustomization("key1", CustomizationData.class);
        Assert.assertNotNull(res);
        Assert.assertEquals(c1.prop1, res.prop1);
        Assert.assertEquals(c1.prop2, res.prop2);
    }

    @Test
    public void testRWString() throws StorageException, IOException {
        mockFactory.get(EasyMock.eq("key1"), EasyMock.eq("default1"));
        EasyMock.expectLastCall().andReturn("default1");

        mockFactory.put(EasyMock.eq("key1"), EasyMock.eq("value1"), EasyMock.eq(false));
        EasyMock.expectLastCall();
        mockFactory.flush();
        EasyMock.expectLastCall();

        mockFactory.get(EasyMock.eq("key1"), EasyMock.eq("default1"));
        EasyMock.expectLastCall().andReturn("value1");

        EasyMock.replay(mocks);

        String res1 = cc.readString(testConfigKey1);
        Assert.assertEquals("default1", res1);

        ConfigTransaction tx = cc.startTransaction();
        cc.writeString(tx, testConfigKey1, "value1");
        cc.commit(tx);

        String res2 = cc.readString(testConfigKey1);
        Assert.assertEquals("value1", res2);

        EasyMock.verify(mocks);
    }

    @Test
    public void testReadInteger() throws StorageException {
        mockFactory.get(EasyMock.eq(testConfigKeyInt.getKey()), EasyMock.eq(testConfigKeyInt.getDefaultValue()));
        EasyMock.expectLastCall().andReturn("42");

        mockFactory.get(EasyMock.eq(testConfigKeyInt.getKey()), EasyMock.eq(testConfigKeyInt.getDefaultValue()));
        EasyMock.expectLastCall().andReturn("1");

        mockFactory.get(EasyMock.eq(testConfigKeyInt.getKey()), EasyMock.eq(testConfigKeyInt.getDefaultValue()));
        EasyMock.expectLastCall().andReturn("");

        mockFactory.get(EasyMock.eq(testConfigKeyInt.getKey()), EasyMock.eq(testConfigKeyInt.getDefaultValue()));
        EasyMock.expectLastCall().andReturn(null);

        EasyMock.replay(mocks);

        Assert.assertEquals(42, (int) cc.readInteger(testConfigKeyInt));
        Assert.assertEquals(1, (int) cc.readInteger(testConfigKeyInt));
        Assert.assertNull(cc.readInteger(testConfigKeyInt));
        Assert.assertNull(cc.readInteger(testConfigKeyInt));

        EasyMock.verify(mocks);
    }

    @Test(expected = NumberFormatException.class)
    public void testReadInteger_invalid() throws StorageException {
        mockFactory.get(EasyMock.eq(testConfigKeyInt.getKey()), EasyMock.eq(testConfigKeyInt.getDefaultValue()));
        EasyMock.expectLastCall().andReturn("hello");

        EasyMock.replay(mocks);

        cc.readInteger(testConfigKeyInt);

        EasyMock.verify(mocks);
    }

    @Test
    public void testWriteInteger() throws StorageException, IOException {
        mockFactory.put(EasyMock.eq(testConfigKeyInt.getKey()), EasyMock.eq("2"), EasyMock.eq(false));
        EasyMock.expectLastCall();
        mockFactory.flush();
        EasyMock.expectLastCall();

        mockFactory.put(EasyMock.eq(testConfigKeyInt.getKey()), EasyMock.eq((String) null), EasyMock.eq(false));
        EasyMock.expectLastCall();
        mockFactory.flush();
        EasyMock.expectLastCall();

        EasyMock.replay(mocks);

        ConfigTransaction tx;

        tx = cc.startTransaction();
        cc.writeInteger(tx, testConfigKeyInt, 2);
        cc.commit(tx);

        tx = cc.startTransaction();
        cc.writeInteger(tx, testConfigKeyInt, null);
        cc.commit(tx);

        EasyMock.verify(mocks);
    }

    @Test
    public void testReadBoolean() throws StorageException {
        mockFactory
                .get(EasyMock.eq(testConfigKeyBoolean.getKey()), EasyMock.eq(testConfigKeyBoolean.getDefaultValue()));
        EasyMock.expectLastCall().andReturn(Boolean.FALSE.toString());

        mockFactory
                .get(EasyMock.eq(testConfigKeyBoolean.getKey()), EasyMock.eq(testConfigKeyBoolean.getDefaultValue()));
        EasyMock.expectLastCall().andReturn(Boolean.TRUE.toString());

        mockFactory
                .get(EasyMock.eq(testConfigKeyBoolean.getKey()), EasyMock.eq(testConfigKeyBoolean.getDefaultValue()));
        EasyMock.expectLastCall().andReturn("true");

        mockFactory
                .get(EasyMock.eq(testConfigKeyBoolean.getKey()), EasyMock.eq(testConfigKeyBoolean.getDefaultValue()));
        EasyMock.expectLastCall().andReturn("yes");

        mockFactory
                .get(EasyMock.eq(testConfigKeyBoolean.getKey()), EasyMock.eq(testConfigKeyBoolean.getDefaultValue()));
        EasyMock.expectLastCall().andReturn("");

        mockFactory
                .get(EasyMock.eq(testConfigKeyBoolean.getKey()), EasyMock.eq(testConfigKeyBoolean.getDefaultValue()));
        EasyMock.expectLastCall().andReturn(null);

        mockFactory
                .get(EasyMock.eq(testConfigKeyBoolean.getKey()), EasyMock.eq(testConfigKeyBoolean.getDefaultValue()));
        EasyMock.expectLastCall().andReturn("hello");

        EasyMock.replay(mocks);

        Assert.assertEquals(false, cc.readBoolean(testConfigKeyBoolean));
        Assert.assertEquals(true, cc.readBoolean(testConfigKeyBoolean));
        Assert.assertEquals(true, cc.readBoolean(testConfigKeyBoolean));
        Assert.assertEquals(true, cc.readBoolean(testConfigKeyBoolean));
        Assert.assertNull(cc.readBoolean(testConfigKeyBoolean));
        Assert.assertNull(cc.readBoolean(testConfigKeyBoolean));
        Assert.assertNull(cc.readBoolean(testConfigKeyBoolean));

        EasyMock.verify(mocks);
    }

    @Test
    public void testWriteBoolean() throws StorageException, IOException {
        mockFactory.put(EasyMock.eq(testConfigKeyBoolean.getKey()), EasyMock.eq("true"), EasyMock.eq(false));
        EasyMock.expectLastCall();
        mockFactory.flush();
        EasyMock.expectLastCall();

        mockFactory.put(EasyMock.eq(testConfigKeyBoolean.getKey()), EasyMock.eq("false"), EasyMock.eq(false));
        EasyMock.expectLastCall();
        mockFactory.flush();
        EasyMock.expectLastCall();

        mockFactory.put(EasyMock.eq(testConfigKeyBoolean.getKey()), EasyMock.eq((String) null), EasyMock.eq(false));
        EasyMock.expectLastCall();
        mockFactory.flush();
        EasyMock.expectLastCall();

        EasyMock.replay(mocks);

        ConfigTransaction tx;

        tx = cc.startTransaction();
        cc.writeBoolean(tx, testConfigKeyBoolean, true);
        cc.commit(tx);

        tx = cc.startTransaction();
        cc.writeBoolean(tx, testConfigKeyBoolean, false);
        cc.commit(tx);

        tx = cc.startTransaction();
        cc.writeBoolean(tx, testConfigKeyBoolean, null);
        cc.commit(tx);

        EasyMock.verify(mocks);
    }

}
