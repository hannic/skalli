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
package org.eclipse.skalli.testutil;

import java.io.InputStream;
import java.io.StringWriter;
import java.net.URL;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.XMLUnit;
import org.junit.Assert;
import org.w3c.dom.Document;

import org.eclipse.skalli.model.ext.DataMigration;

@SuppressWarnings("nls")
public class MigrationTestUtil {

  /**
   * Tests a {@link DataMigration} by applying the migration to a file and comparing it to a control file.
   *
   * <p>
   * Naming conventions for the test data:
   * <li>in the test fragment, use a folder called <code>/res/migrations/&lt;MigrationClassName&gt;/</code>
   * <li>add an original file <code>&lt;filenamePrefix&gt;.xml.before</code>
   * <li>add the manually converted control file <code>&lt;filenamePrefix&gt;.xml.after</code>
   * As an example, have a look at the DataMigration11 test in the model.core bundle.
   * </p>
   *
   * @param migration  the data migration to execute.
   * @param filenamePrefix  the prefix of the <code>&lt;filenamePrefix&gt;.xml.before</code> and
   * <code>&lt;filenamePrefix&gt;.xml.after</code> files.
   */
  public static void testMigration(DataMigration migration, String filenamePrefix) throws Exception {
    String pathPrefix = "/res/migrations/" +  migration.getClass().getSimpleName() + "/" + filenamePrefix ;
    Document docBefore = getAsDocument(migration,  pathPrefix + ".xml.before");
    migration.migrate(docBefore);
    Document docAfter = getAsDocument(migration, pathPrefix + ".xml.after");

    Assert.assertEquals(String.valueOf(migration.getFromVersion()), docBefore.getDocumentElement().getAttribute("version"));
    assertEquals(docBefore, docAfter, true);
  }

  /**
   * Asserts that the given {@link Document documents} are equal.
   */
  public static void assertEquals(Document docLeft, Document docRight, boolean ignoreWhitespace) throws Exception {
    XMLUnit.setIgnoreWhitespace(ignoreWhitespace);
    Diff diff = XMLUnit.compareXML(docRight, docLeft);
    Assert.assertTrue(detailsToString(diff, docRight, docLeft), diff.similar());
    Assert.assertTrue(detailsToString(diff, docRight, docLeft), diff.identical());
  }

  private static String detailsToString(Diff diff, Document controlDoc, Document modifiedDoc) throws Exception {
    StringBuffer sb = new StringBuffer();
    diff.appendMessage(sb);

    if(controlDoc != null) {
      sb.append("\nControl document:\n");
      sb.append(toString(controlDoc)).append("\n");
    }
    if(modifiedDoc != null) {
      sb.append("\nModified document:\n");
      sb.append(toString(modifiedDoc)).append("\n");
    }

    return sb.toString();
  }

  private static String toString(Document doc) throws Exception {
    TransformerFactory factory = TransformerFactory.newInstance();
    Transformer transformer = factory.newTransformer();
    StringWriter writer = new StringWriter();
    Result result = new StreamResult(writer);
    Source source = new DOMSource(doc);
    transformer.transform(source, result);
    writer.close();

    return writer.toString();
  }

  private static Document getAsDocument(DataMigration migration, String filename) throws Exception {
    URL urlBefore = migration.getClass().getResource(filename);
    InputStream isBefore = urlBefore.openStream();
    try {
      DocumentBuilder docBuilder = getDocumentBuilder();
      Document doc = docBuilder.parse(isBefore);
      return doc;
    } finally {
      if (isBefore != null) {
        isBefore.close();
      }
    }
  }

  private static DocumentBuilder getDocumentBuilder() throws ParserConfigurationException {
    DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
    DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
    return docBuilder;
  }

}

