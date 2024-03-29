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
package org.eclipse.skalli.api.rest.internal.admin;

import org.eclipse.skalli.api.rest.internal.util.ResourceRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;

public class StatusResource extends ServerResource {

    @Get
    public Representation retrieve() {
        return new ResourceRepresentation<Object>(new Object(),
                new StatusConverter(getRequest().getResourceRef().getHostIdentifier()));
    }

}
