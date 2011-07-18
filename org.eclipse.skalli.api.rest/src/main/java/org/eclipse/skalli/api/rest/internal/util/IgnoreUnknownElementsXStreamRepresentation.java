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
package org.eclipse.skalli.api.rest.internal.util;

import org.restlet.data.MediaType;
import org.restlet.ext.xstream.XstreamRepresentation;
import org.restlet.representation.Representation;

import org.eclipse.skalli.model.ext.AliasedConverter;
import com.thoughtworks.xstream.XStream;

public class IgnoreUnknownElementsXStreamRepresentation<T> extends XstreamRepresentation<T> {

    private final Class<?>[] classes;
    private final AliasedConverter[] converters;

    public IgnoreUnknownElementsXStreamRepresentation(Representation representation, AliasedConverter[] converters,
            Class<?>[] classes) {
        super(representation);
        this.converters = converters;
        this.classes = classes;
    }

    public IgnoreUnknownElementsXStreamRepresentation(T object, AliasedConverter[] converters) {
        super(object);
        this.converters = converters;
        if (object != null) {
            this.classes = new Class<?>[] { object.getClass() };
        } else {
            this.classes = new Class<?>[0];
        }
    }

    @Override
    protected XStream createXstream(MediaType arg0) {
        XStream xstream = new IgnoreUnknownElementsXStream();
        for (AliasedConverter converter : converters) {
            xstream.registerConverter(converter);
            xstream.alias(converter.getAlias(), converter.getConversionClass());
        }
        for (Class<?> clazz : classes) {
            xstream.processAnnotations(clazz);
        }
        return xstream;
    }

}
