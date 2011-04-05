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
package org.eclipse.skalli.view.component;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

import org.eclipse.skalli.model.ext.Taggable;

@SuppressWarnings("nls")
public class TagCloudTest {

  private static class TestTaggable implements Taggable {
    @Override
    public Set<String> getTags() {
      return null;
    }
    @Override
    public void addTag(String tag) {
    }
    @Override
    public void removeTag(String tag) {
    }
    @Override
    public boolean hasTag(String tag) {
      return false;
    }
  }

  private Map<String, Set<Taggable>> getTestTags() {
    Map<String, Set<Taggable>> tags = new HashMap<String, Set<Taggable>>();
    tags.put("a", createTestTaggables(4));
    tags.put("b", createTestTaggables(14));
    tags.put("c", createTestTaggables(1));
    tags.put("d", createTestTaggables(4));
    tags.put("e", createTestTaggables(7));
    tags.put("f", createTestTaggables(1));
    tags.put("g", createTestTaggables(4));
    tags.put("h", createTestTaggables(25));
    tags.put("i", createTestTaggables(1));
    tags.put("j", createTestTaggables(1));
    tags.put("k", createTestTaggables(2));
    tags.put("l", createTestTaggables(12));
    tags.put("m", createTestTaggables(14));
    tags.put("n", createTestTaggables(1));
    tags.put("o", createTestTaggables(6));
    tags.put("p", createTestTaggables(4));
    return tags;
  }

  private Set<Taggable> createTestTaggables(int n) {
    HashSet<Taggable> result = new HashSet<Taggable>(n);
    for (int i=0; i<n; ++i)  {
      result.add(new TestTaggable());
    }
    return result;
  }

  @Test
  public void testShow3MostPopular() throws IOException {
    TagCloud tagCloud = new TagCloud(getTestTags(), 3);
    String xhtml = tagCloud.doLayout();

    Assert.assertEquals("xhtml",
        "<center>" +
        "<a href='/projects?tag=b'><font class='tag1'>b</font></a> " +
        "<a href='/projects?tag=h'><font class='tag6'>h</font></a> " +
        "<a href='/projects?tag=m'><font class='tag1'>m</font></a> " +
        "</center>", xhtml);
  }

  @Test
  public void testShow25MostPopular() throws IOException {
    TagCloud tagCloud = new TagCloud(getTestTags(), 25);
    String xhtml = tagCloud.doLayout();

    Assert.assertEquals("xhtml",
        "<center>" +
        "<a href='/projects?tag=a'><font class='tag2'>a</font></a> " +
        "<a href='/projects?tag=b'><font class='tag4'>b</font></a> " +
        "<a href='/projects?tag=c'><font class='tag1'>c</font></a> " +
        "<a href='/projects?tag=d'><font class='tag2'>d</font></a> " +
        "<a href='/projects?tag=e'><font class='tag3'>e</font></a> " +
        "<a href='/projects?tag=f'><font class='tag1'>f</font></a> " +
        "<a href='/projects?tag=g'><font class='tag2'>g</font></a> " +
        "<a href='/projects?tag=h'><font class='tag6'>h</font></a> " +
        "<a href='/projects?tag=i'><font class='tag1'>i</font></a> " +
        "<a href='/projects?tag=j'><font class='tag1'>j</font></a> " +
        "<a href='/projects?tag=k'><font class='tag2'>k</font></a> " +
        "<a href='/projects?tag=l'><font class='tag4'>l</font></a> " +
        "<a href='/projects?tag=m'><font class='tag4'>m</font></a> " +
        "<a href='/projects?tag=n'><font class='tag1'>n</font></a> " +
        "<a href='/projects?tag=o'><font class='tag3'>o</font></a> " +
        "<a href='/projects?tag=p'><font class='tag2'>p</font></a> " +
        "</center>", xhtml);
  }

  @Test
  public void testShowAllTags() throws IOException {
    TagCloud tagCloud = new TagCloud(getTestTags(), Integer.MAX_VALUE);
    String xhtml = tagCloud.doLayout();

    Assert.assertEquals("xhtml",
        "<center>" +
        "<a href='/projects?tag=a'><font class='tag2'>a</font></a> " +
        "<a href='/projects?tag=b'><font class='tag4'>b</font></a> " +
        "<a href='/projects?tag=c'><font class='tag1'>c</font></a> " +
        "<a href='/projects?tag=d'><font class='tag2'>d</font></a> " +
        "<a href='/projects?tag=e'><font class='tag3'>e</font></a> " +
        "<a href='/projects?tag=f'><font class='tag1'>f</font></a> " +
        "<a href='/projects?tag=g'><font class='tag2'>g</font></a> " +
        "<a href='/projects?tag=h'><font class='tag6'>h</font></a> " +
        "<a href='/projects?tag=i'><font class='tag1'>i</font></a> " +
        "<a href='/projects?tag=j'><font class='tag1'>j</font></a> " +
        "<a href='/projects?tag=k'><font class='tag2'>k</font></a> " +
        "<a href='/projects?tag=l'><font class='tag4'>l</font></a> " +
        "<a href='/projects?tag=m'><font class='tag4'>m</font></a> " +
        "<a href='/projects?tag=n'><font class='tag1'>n</font></a> " +
        "<a href='/projects?tag=o'><font class='tag3'>o</font></a> " +
        "<a href='/projects?tag=p'><font class='tag2'>p</font></a> " +
        "</center>", xhtml);
  }

  @Test
  public void testNoTags() throws IOException {
    TagCloud tagCloud = new TagCloud(null, 3);
    String xhtml = tagCloud.doLayout();

    Assert.assertEquals("xhtml",
        "<center>no tags at the moment</center>", xhtml);
  }

  @Test
  public void testShow0MostPopular() throws IOException {
    TagCloud tagCloud = new TagCloud(getTestTags(), 0);
    String xhtml = tagCloud.doLayout();

    Assert.assertEquals("xhtml",
        "<center>no tags at the moment</center>", xhtml);
  }

}

