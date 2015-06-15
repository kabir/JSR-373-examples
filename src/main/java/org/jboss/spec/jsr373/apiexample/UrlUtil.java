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
package org.jboss.spec.jsr373.apiexample;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

import org.jboss.dmr.ModelNode;
import org.jboss.spec.jsr373.apiexample.resource.ResourceTemplate;
import org.jboss.spec.jsr373.apiexample.resource.objects.ManagedObjectType;

/**
 * @author Kabir Khan
 */
public interface UrlUtil {

    URL createTemplateUrl(ManagedObjectType resourceType) throws IOException;

    URL createInstanceUrl(ResourceTemplate template, URL parentUrl, String name) throws IOException, URISyntaxException;

    PrintWriter getWriter(URL url) throws IOException ;

    class Factory {
        public static final URL SERVLET_ROOT_URL;
        static {
            String rootString = System.getProperty("jsr.373.servlet.root", "http://localhost:8080/jsr373example/contents");
            if (rootString.endsWith("/")) {
                rootString.substring(0, rootString.length());
            }
            try {
                SERVLET_ROOT_URL = new URL(rootString);
            } catch (Exception e){
                throw new RuntimeException(e);
            }

        }

        public static UrlUtil createServletInstance(final ServletUrlRegistry urlRegistry) throws  IOException {
            final URL root = SERVLET_ROOT_URL;
            return new UrlUtil() {
                @Override
                public URL createTemplateUrl(ManagedObjectType resourceType) throws IOException {
                    return appendURL(root, "templates", resourceType.getName().toLowerCase(Locale.ENGLISH));
                }

                @Override
                public URL createInstanceUrl(ResourceTemplate template, URL parentUrl, String name) throws IOException, URISyntaxException {
                    final URL parent = parentUrl == null ? root : parentUrl;
                    return appendURL(parent, template.getResourceType().getPath(), name);
                }

                @Override
                public PrintWriter getWriter(URL url) throws IOException {
                    return urlRegistry.getWriter(url);
                }

                private URL appendURL(URL parentURL, String...pathElements) throws IOException {
                    StringBuilder parent = new StringBuilder(parentURL.toExternalForm());
                    boolean hasSlash = parent.charAt(parent.length() - 1) == '/';

                    for (String element : pathElements) {
                        if (!hasSlash) {
                            parent.append('/');
                        }
                        parent.append(element);
                        hasSlash = false;
                    }
                    return new URL(parent.toString());
                }
            };
        }

        public static UrlUtil createFileInstance() throws IOException {
            File file = Paths.get("src/main/resources/index.html").toAbsolutePath().toFile();
            if (!file.exists()) {
                throw new IllegalStateException("Could not find marker");
            }
            final File outputDirectory = new File(file.getParentFile(), "site-contents");

            return new UrlUtil() {
                {
                    if (outputDirectory.exists()) {
                        delete(outputDirectory);
                    }
                    createDir(outputDirectory);
                    System.out.println("Output directory is " + outputDirectory);

                }

                @Override
                public URL createTemplateUrl(ManagedObjectType resourceType) throws IOException {
                    File file = new File(outputDirectory, createJsonFileName(resourceType.getName()));
                    return file.toURI().toURL();
                }

                @Override
                public URL createInstanceUrl(ResourceTemplate template, URL parentUrl, String name) throws IOException, URISyntaxException {
                    File parentFile;
                    if (parentUrl != null) {
                        parentFile = new File(parentUrl.toURI());
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

                @Override
                public PrintWriter getWriter(URL url) throws IOException {
                    try {
                        return new PrintWriter(new BufferedWriter(new FileWriter(new File(url.toURI()))));
                    } catch (URISyntaxException e) {
                        throw new IOException(e);
                    }
                }

                private void delete(File file) {
                    if (file.isDirectory()) {
                        for (File entry : file.listFiles()) {
                            delete(entry);
                        }
                    }
                    file.delete();
                }

                private File createDir(File dir) throws IOException {
                    Path path = Paths.get(dir.getAbsolutePath());
                    File file = path.toFile();
                    if (!file.exists()) {
                        Files.createDirectories(path);
                    }
                    return file;
                }

                private String createJsonFileName(String name) {
                    return name.toLowerCase(Locale.ENGLISH) + ".json";
                }
            };
        }
    }

    public class ServletUrlRegistry {
        final Map<URL, String> jsonByUrl = new LinkedHashMap<>();

        private PrintWriter getWriter(final URL url) {
            final StringWriter stringWriter = new StringWriter();
            return new PrintWriter(stringWriter){
                @Override
                public void close() {
                    super.close();
                    synchronized (this) {
                        jsonByUrl.put(url, stringWriter.getBuffer().toString());
                        System.out.println("Adding " + url.toExternalForm() + " to registry");
                    }
                }
            };
        }

        public synchronized Reader getReader(URL url) {
            String json = jsonByUrl.get(url);
            if (json == null) {
                //Try the parent
                String val = url.toExternalForm();
                ModelNode list = new ModelNode().setEmptyList();
                for (Map.Entry<URL, String> entry : jsonByUrl.entrySet()) {
                    String current = entry.getKey().toExternalForm();
                    current = current.substring(0, current.lastIndexOf('/'));

                    if (current.equals(val)) {
                        list.add(ModelNode.fromJSONString(entry.getValue()));
                    }
                }

                return new StringReader(list.toJSONString(false));
            }
            return new StringReader(json);
        }

    }
}
