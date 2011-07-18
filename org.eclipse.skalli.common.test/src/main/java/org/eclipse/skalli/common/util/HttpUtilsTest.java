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
package org.eclipse.skalli.common.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.net.URL;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import org.eclipse.skalli.testutil.BundleManager;
import org.eclipse.skalli.testutil.HttpServerMock;

@SuppressWarnings("nls")
public class HttpUtilsTest {

    private static HttpServerMock mmus;
    private static final String TEST_CONTENT = "BODY";

    @BeforeClass
    public static void setUpOnce() throws Exception {
        new BundleManager(HttpUtilsTest.class).startBundles();
        mmus = new HttpServerMock();
        mmus.start();
    }

    @AfterClass
    public static void tearDownOnce() throws Exception {
        mmus.stop();
    }

    @Test
    public void testGetContent() throws Exception {
        mmus.addContent("testGetContent", TEST_CONTENT);
        assertGetRequest(HttpUtils.HTTP, 200, "testGetContent", TEST_CONTENT);
    }

    private void assertGetRequest(String protocol, int responseCode, String contentId, String content) throws Exception {
        URL url = new URL(protocol + "://" + mmus.getHost() + ":" + mmus.getPort() + "/" + contentId + "/"
                + responseCode);
        HttpClient client = HttpUtils.getClient(url);
        assertNotNull(client);
        GetMethod method = new GetMethod(url.toExternalForm());
        assertEquals(200, client.executeMethod(method));
        if (content != null) {
            assertEquals(content, method.getResponseBodyAsString());
        }
    }
}
