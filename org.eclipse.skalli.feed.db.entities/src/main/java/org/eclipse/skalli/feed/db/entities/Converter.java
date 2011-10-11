/*******************************************************************************
 * Copyright (c) 2010 - 2011 SAP AG and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * SAP AG - initial API and implementation
 *******************************************************************************/
package org.eclipse.skalli.feed.db.entities;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.skalli.api.java.feeds.Entry;
import org.eclipse.skalli.common.util.CollectionUtils;

import com.sun.syndication.feed.synd.SyndContent;
import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.feed.synd.SyndLink;
import com.sun.syndication.feed.synd.SyndPerson;

public class Converter {

    static public List<Entry> syndFeed2Feed(SyndFeed syndEntry, String feedId) {
        if (syndEntry == null) {
            return Collections.emptyList();
        }
        List<Entry> entries = new ArrayList<Entry>();
        for (Object o : syndEntry.getEntries()) {
            entries.add(syndEntry2Entry((SyndEntry) o));
        }
        return entries;
    }

    private static Entry syndEntry2Entry(SyndEntry syndEntry) {
        if (syndEntry == null) {
            return null;
        }
        EntryJPA entry = new EntryJPA();

        entry.setTitle(syndEntry.getTitle());

        if (CollectionUtils.isNotBlank(syndEntry.getLinks())) {
            entry.setLink(syndLink2Link((SyndLink) syndEntry.getLinks().get(0)));
        }

        if (CollectionUtils.isNotBlank(syndEntry.getContents())) {
            entry.setContent(syndContent2Content((SyndContent) syndEntry.getContents().get(0)));
        }

        if (CollectionUtils.isNotBlank(syndEntry.getAuthors())) {
            entry.setAuthor(syndPerson2Person((SyndPerson) syndEntry.getAuthors().get(0)));
        }

        entry.setPublished(syndEntry.getPublishedDate());
        return entry;
    }

    private static PersonJPA syndPerson2Person(SyndPerson syndEntry) {
        PersonJPA p = new PersonJPA();
        p.setName(syndEntry.getName());
        p.setEmail(p.getEmail());
        return p;
    }

    private static LinkJPA syndLink2Link(SyndLink syndEntry) {
        LinkJPA l = new LinkJPA();
        l.setHref(syndEntry.getHref());
        l.setTitle(syndEntry.getTitle());
        return l;
    }

    private static ContentJPA syndContent2Content(SyndContent syndEntry) {
        ContentJPA c = new ContentJPA();
        c.setType(syndEntry.getType());
        c.setValue(syndEntry.getValue());
        return c;
    }
}
