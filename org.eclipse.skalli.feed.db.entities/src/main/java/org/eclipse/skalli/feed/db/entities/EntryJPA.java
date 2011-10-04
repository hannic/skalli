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

import java.util.Date;
import java.util.UUID;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.eclipse.skalli.api.java.feeds.Content;
import org.eclipse.skalli.api.java.feeds.Entry;
import org.eclipse.skalli.api.java.feeds.Link;
import org.eclipse.skalli.api.java.feeds.Person;
import org.eclipse.skalli.common.util.UUIDUtils;

@Table(name = "Entry")
@Entity
public class EntryJPA implements Entry {

    public static final int ID_LENGTH = 40;
    public static final int URI_LENGTH = 512;
    public static final int TITLE_LENGTH = 256;
    public static final int TITLE_EX_LENGTH = 512;
    public static final int TYPE_LENGTH = 32;
    public static final int SOURCE_LENGTH = 16;

    @Id
    @Column(length = ID_LENGTH)
    private String id;

    @Column(length = TITLE_LENGTH, nullable = false)
    private String title;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "href", column = @Column(name = "link_href", length = LinkJPA.HREF_LENGTH)),
            @AttributeOverride(name = "title", column = @Column(name = "link_title", length = LinkJPA.TITLE_LENGHT))
    })
    private LinkJPA link;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "type", column = @Column(name = "content_type", length = ContentJPA.TYPE_LENGTH)),
            @AttributeOverride(name = "value", column = @Column(name = "content_value", length = ContentJPA.VALUE_LENGTH))
    })
    private ContentJPA content;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false)
    private Date published;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "name", column = @Column(name = "author_name", length = PersonJPA.NAME_LENGTH)),
            @AttributeOverride(name = "email", column = @Column(name = "author_email", length = PersonJPA.EMAIL_LENGTH))
    })
    private PersonJPA author;

    @Column(nullable = false)
    private String projectId;

    @Column(length = SOURCE_LENGTH, nullable = false)
    private String source;

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public Link getLink() {
        if (link == null) {
            link = new LinkJPA();
        }
        return link;
    }

    // do we need this setter at all?
    public void setLink(LinkJPA link) {
        this.link = link;
    }

    @Override
    public Content getContent() {
        if (content == null) {
            content = new ContentJPA();
        }
        return content;
    }

    // do we need this setter at all?
    public void setContent(ContentJPA content) {
        this.content = content;
    }

    @Override
    public Date getPublished() {
        return published;
    }

    @Override
    public void setPublished(Date published) {
        this.published = published;
    }

    @Override
    public Person getAuthor() {
        if (author == null) {
            author = new PersonJPA();
        }
        return author;
    }

    // do we need this setter at all?
    public void setAuthor(PersonJPA author) {
        this.author = author;
    }

    @Override
    public UUID getProjectId() {
        return UUIDUtils.asUUID(projectId);
    }

    @Override
    public void setProjectId(UUID projectId) {
        this.projectId = projectId.toString();
    }

    @Override
    public String getSource() {
        return source;
    }

    @Override
    public void setSource(String source) {
        this.source = source;
    }
}
