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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.restlet.data.Disposition;
import org.restlet.data.MediaType;
import org.restlet.representation.OutputRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



/**
 *
 */
public class ProjectBackupResource extends ServerResource {

    private static final String FILE_NAME = "backup.zip"; //$NON-NLS-1$

    @Get
    public Representation retrieve() {
        ZipRepresentation zipRepresentation = new ZipRepresentation();
        Disposition disposition = new Disposition(Disposition.TYPE_ATTACHMENT);
        disposition.setFilename(FILE_NAME);
        zipRepresentation.setDisposition(disposition);

        return zipRepresentation;
    }

    private static class ZipRepresentation extends OutputRepresentation {
        private static final Logger LOG = LoggerFactory.getLogger(ZipRepresentation.class);

        private static final String STORAGE_LOCATION = "storage" + IOUtils.DIR_SEPARATOR; //$NON-NLS-1$
        private final int BUFFER = 2048;

        public ZipRepresentation() {
            super(MediaType.APPLICATION_ZIP);
        }

        @Override
        public void write(OutputStream paramOutputStream) throws IOException {
            ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(paramOutputStream));
            byte data[] = new byte[BUFFER];

            File parent = null;
            String workDirProperty = FilenameUtils.separatorsToSystem(System.getProperty("workdir")); //$NON-NLS-1$
            if (workDirProperty != null) {
                if (!workDirProperty.endsWith(String.valueOf(IOUtils.DIR_SEPARATOR))) {
                    workDirProperty += IOUtils.DIR_SEPARATOR;
                }
                parent = new File(workDirProperty);
                if (!parent.exists() || !parent.isDirectory()) {
                    LOG.warn(String
                            .format("Working directory (%s) not found. Using current directory as fallback.", parent.getAbsolutePath())); //$NON-NLS-1$
                    parent = null;
                }
            }

            workDirProperty = FilenameUtils.separatorsToUnix(workDirProperty);
            File storage = new File(parent, STORAGE_LOCATION);
            @SuppressWarnings("unchecked")
            Iterator<File> files = FileUtils.iterateFiles(storage, null, true);

            BufferedInputStream origin = null;
            try {
                while (files.hasNext()) {
                    File file = files.next();
                    FileInputStream fi = new FileInputStream(file);
                    origin = new BufferedInputStream(fi, BUFFER);
                    ZipEntry entry = new ZipEntry(StringUtils.substringAfter(
                            FilenameUtils.separatorsToUnix(file.getPath()),
                            workDirProperty != null ? workDirProperty : StringUtils.EMPTY));
                    out.putNextEntry(entry);
                    int count;
                    while ((count = origin.read(data, 0, BUFFER)) != -1) {
                        out.write(data, 0, count);
                    }
                    origin.close();
                }
            } finally {
                IOUtils.closeQuietly(origin);
                IOUtils.closeQuietly(out);
            }
        }
    }
}
