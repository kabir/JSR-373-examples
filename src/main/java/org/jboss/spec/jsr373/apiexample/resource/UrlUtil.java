/*
 * JBoss, Home of Professional Open Source.
 * Copyright ${year}, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.spec.jsr373.apiexample.resource;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;

import org.jboss.spec.jsr373.apiexample.resource.objects.ManagedObjectType;

/**
 * @author Kabir Khan
 */
public class UrlUtil {
    private final File outputDirectory;

    public UrlUtil() throws  IOException {
        File file = Paths.get("src/main/resources/index.html").toAbsolutePath().toFile();
        if (!file.exists()) {
            throw new IllegalStateException("Could not find marker");
        }
        outputDirectory = new File(file.getParentFile(), "site-contents");
        if (outputDirectory.exists()) {
            delete(outputDirectory);
        }
        createDir(outputDirectory);
        System.out.println("Output directory is " + outputDirectory);

    }

    private static void delete(File file) {
        if (file.isDirectory()) {
            for (File entry : file.listFiles()) {
                delete(entry);
            }
        }
        file.delete();
    }

    public static File createDir(File dir) throws IOException {
        Path path = Paths.get(dir.getAbsolutePath());
        File file = path.toFile();
        if (!file.exists()) {
            Files.createDirectories(path);
        }
        return file;
    }

    public URL createTemplateUrl(ManagedObjectType resourceType) throws IOException {
        File file = new File(outputDirectory, createJsonFileName(resourceType.getName()));
        return file.toURI().toURL();
    }


    public URL createInstanceUrl(ResourceTemplate template, URL parentUrl, String name) throws IOException, URISyntaxException {
        File parentFile;
        if (parentUrl != null) {
            parentFile = new File(parentUrl.toURI());
            //A parent like domain/main/server/one.json (file) should become domain/main/server/one (dir)
            //to be more 'rest-like' than this example
            if (!parentFile.isDirectory()) {
                String parentName = parentFile.getName();
                int index = parentName.indexOf(".");
                if (index >= 0) {
                    parentName = parentName.substring(0, index);
                    parentFile = new File(parentFile.getParent(), parentName);
                }
            }
        } else {
            parentFile = outputDirectory;
        }
        Path path = Paths.get(parentFile.getAbsolutePath(), template.getResourceType().getPath());
        return new File(createDir(path.toFile()), createJsonFileName(name)).toURI().toURL();
    }

    private String createJsonFileName(String name) {
        return name.toLowerCase(Locale.ENGLISH) + ".json";
    }
}
