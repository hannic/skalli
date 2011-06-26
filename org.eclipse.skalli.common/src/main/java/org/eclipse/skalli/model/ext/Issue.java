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
package org.eclipse.skalli.model.ext;

import java.text.MessageFormat;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.eclipse.skalli.common.util.ComparatorUtils;

/**
 * Class for reporting of validation issues.
 * A validation issue always has a {@link Severity severity}, is assigned to a certain entity
 * and has a message. If no explicit message is defined, a default message is assigned to
 * an issue.<br>
 * Besides that an issue may be related to a certain extension and/or to a certain property
 * of that extension. Optionally an issue can have a description (e.g. a hint how to solve
 * the issue) and a timestamp.
 */
public class Issue implements Comparable<Issue> {

    private Severity severity;
    private Class<? extends Issuer> issuer;
    private UUID entityId;
    private Class<? extends ExtensionEntityBase> extension;
    private Object propertyId;
    private int item;
    private String message;
    private String description;
    private long timestamp;

    /**
     * Default constructor. Required for XML streaming.
     */
    public Issue() {
    }

    /**
     * Creates an issue with the given <code>severity</code> for an entity specified by
     * its unique identifier. The issue is created with a default message.
     *
     * @param severity  the severity of the issue.
     * @param issuer    the issuer that raises this issue, e.g. a validator.
     * @param entityId  the unique identifier of the entity that causes this validation issue.
     */
    public Issue(Severity severity, Class<? extends Issuer> issuer, UUID entityId) {
        this(severity, issuer, entityId, null, null, 0, null);
    }

    /**
     * Creates an issue with the given <code>severity</code> and custom message for an entity
     * specified by its unique identifier.
     *
     * @param severity  the severity of the issue.
     * @param issuer    the issuer that raises this issue, e.g. a validator.
     * @param entityId  the unique identifier of the entity that causes this validation issue.
     * @param message   the message of the the issue.
     */
    public Issue(Severity severity, Class<? extends Issuer> issuer, UUID entityId, String message) {
        this(severity, issuer, entityId, null, null, 0, message);
    }

    /**
     * Creates an issue with the given <code>severity</code> for a property of a model extension that is
     * assigned to an entity specified by its unique identifier. The issue is created with a default message.
     *
     * @param severity  the severity of the issue.
     * @param issuer    the issuer that raises this issue, e.g. a validator.
     * @param entityId  the unique identifier of an entity.
     * @param extension  the class of a model extension, or <code>null</code>.
     * @param propertyId  the property that causes this validation issue, or <code>null</code>.
     */
    public Issue(Severity severity, Class<? extends Issuer> issuer, UUID entityId,
            Class<? extends ExtensionEntityBase> extension, Object propertyId) {
        this(severity, issuer, entityId, extension, propertyId, 0, null);
    }

    /**
     * Creates an issue with the given <code>severity</code> and custom message for a property of a model
     * extension that is assigned to an entity specified by its unique identifier.
     *
     * @param severity  the severity of the issue.
     * @param issuer    the issuer that raises this issue, e.g. a validator.
     * @param entityId  the unique identifier of the entity that causes this validation issue.
     * @param extension  the class of a model extension, or <code>null</code>.
     * @param propertyId  the property that causes this validation issue, or <code>null</code>.
     * @param message   the message of the the issue.
     */
    public Issue(Severity severity, Class<? extends Issuer> issuer, UUID entityId,
            Class<? extends ExtensionEntityBase> extension, Object propertyId, String message) {
        this(severity, issuer, entityId, extension, propertyId, 0, message);
    }

    /**
     * Creates an issue with the given <code>severity</code> and custom message for a property of a model
     * extension that is assigned to an entity specified by its unique identifier.
     *
     * @param severity  the severity of the issue.
     * @param issuer    the issuer that raises this issue, e.g. a validator.
     * @param entityId  the unique identifier of the entity that causes this validation issue.
     * @param extension  the class of a model extension, or <code>null</code>.
     * @param propertyId  the property that causes this validation issue, or <code>null</code>.
     * @param item  a unique item number that distinguishes issues related to the
     * same property/extension/entity/issuer.
     * @param message   the message of the the issue.
     */
    public Issue(Severity severity, Class<? extends Issuer> issuer, UUID entityId,
            Class<? extends ExtensionEntityBase> extension, Object propertyId, int item, String message) {
        if (severity == null) {
            throw new IllegalArgumentException("argument 'severity' must not be null");
        }
        if (issuer == null) {
            throw new IllegalArgumentException("argument 'issuer' must not be null");
        }
        if (entityId == null) {
            throw new IllegalArgumentException("argument 'entityId' must not be null");
        }
        this.severity = severity;
        this.issuer = issuer;
        this.entityId = entityId;
        this.extension = extension;
        this.propertyId = propertyId;
        this.item = item;
        this.message = message;
    }

    /**
     * Returns the severity of this issue.
     */
    public Severity getSeverity() {
        return severity;
    }

    /**
     * Returns the issuer that raised this issue, e.g. a validator.
     */
    public Class<? extends Issuer> getIssuer() {
        return issuer;
    }

    /**
     * Returns the unique identifier of the entity that causes this issue.
     */
    public UUID getEntityId() {
        return entityId;
    }

    /**
     * Returns the message of this issue. If no custom message has been defined,
     * a default message is created from the other parameters of the issue.
     */
    public String getMessage() {
        if (StringUtils.isNotBlank(message)) {
            return message;
        }
        String msg = null;
        if (extension != null) {
            if (propertyId != null) {
                msg = MessageFormat.format("Property {0} of extension {1} of entity {2} is invalid",
                        propertyId, extension.getName(), entityId);
            } else {
                msg = MessageFormat.format("Extension {0} of entity {1} is invalid",
                        extension.getName(), entityId);
            }
        } else {
            msg = MessageFormat.format("Entity {0} is invalid", entityId);
        }
        return msg;
    }

    /**
     * Sets the message of this issue.
     */
    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * Returns the class of the model extension that causes this issue.
     */
    public Class<? extends ExtensionEntityBase> getExtension() {
        return extension;
    }

    /**
     * Sets the class of the model extension that causes this issue.
     */
    public void setExtension(Class<? extends ExtensionEntityBase> extension) {
        this.extension = extension;
    }

    /**
     * Returns the identifier of the property that causes this issue.
     * @see {@link org.eclipse.skalli.model.ext.PropertyName}
     */
    public Object getPropertyId() {
        return propertyId;
    }

    /**
     * Sets the identifier of the property that causes this issue.
     * @see {@link org.eclipse.skalli.model.ext.PropertyName}
     */
    public void setPropertyId(Object propertyId) {
        this.propertyId = propertyId;
    }

    /**
     * Returns a unique item number that distinguishes issues related
     * to the same property/extension/entity/issuer.
     */
    public int getItem() {
        return item;
    }

    /**
     * Sets a unique item number that distinguishes issues related
     * to the same property/extension/entity/issuer.
     */
    public void setItem(int item) {
        this.item = item;
    }

    /**
     * Returns the description of this issue.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the description of this issue.
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Returns the timestanp of this issue.
     */
    public long getTimestamp() {
        return timestamp;
    }

    /**
     * Sets the timestanp of this issue.
     */
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public int hashCode() {
        int result = 31 + severity.hashCode();
        result = 31 * result + issuer.hashCode();
        result = 31 * result + entityId.hashCode();
        if (extension != null) {
            result = 31 * result + extension.getName().hashCode();
        }
        if (propertyId != null) {
            result = 31 * result + propertyId.hashCode();
        }
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (obj instanceof Issue) {
            return compareTo((Issue) obj) == 0;
        }
        return false;
    }

    /**
     * Compares two issues according to their severity ({@link Severity.FATAL} first),
     * entity id, extension class name, property id and issuer (in that order).
     * Issues related to a whole entity (no extension, no property) are consider to be less
     * than issues related to a whole extension (extension set, but no property).
     * Issues related to a whole extension (extension set, but no property) are consider to be less
     * than issues related to a certain property (extension and property both).
     */
    @Override
    public int compareTo(Issue issue) {
        int result = severity.compareTo(issue.severity);
        if (result == 0) {
            result = entityId.compareTo(issue.entityId);
            if (result == 0) {
                result = ComparatorUtils.compareAsStrings(extension, issue.extension);
                if (result == 0) {
                    result = ComparatorUtils.compareAsStrings(propertyId, issue.propertyId);
                    if (result == 0) {
                        result = ComparatorUtils.compare(issuer.getName(), issue.issuer.getName());
                        if (result == 0) {
                            result = item < issue.item ? -1 : (item == issue.item ? 0 : 1);
                        }
                    }
                }
            }
        }
        return result;
    }

    /**
     * This method returns {@link #getMessage()}.
     */
    @Override
    public String toString() {
        return getMessage();
    }
}
