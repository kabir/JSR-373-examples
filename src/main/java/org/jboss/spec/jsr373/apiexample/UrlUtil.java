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
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.jboss.dmr.ModelNode;
import org.jboss.spec.jsr373.apiexample.resource.objects.ManagedObjectType;

/**
 * @author Kabir Khan
 */
public interface UrlUtil {

    URL createTemplateUrl(ManagedObjectType resourceType) throws IOException;

    URL createInstanceUrl(String attributeName, URL parentUrl, String name) throws IOException, URISyntaxException;

    PrintWriter getWriter(URL url) throws IOException ;

    List<URL> getAllTemplateUrls();

    List<URL> getAllInstanceUrls();

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
            final List<URL> templateUrls = new ArrayList<>();
            final List<URL> instanceUrls = new ArrayList<>();
            final URL root = SERVLET_ROOT_URL;
            return new UrlUtil() {
                @Override
                public URL createTemplateUrl(ManagedObjectType resourceType) throws IOException {
                    URL url = appendURL(root, "templates", resourceType.getName().toLowerCase(Locale.ENGLISH));
                    templateUrls.add(url);
                    return url;
                }

                @Override
                public URL createInstanceUrl(String attributeName, URL parentUrl, String name) throws IOException, URISyntaxException {
                    final URL parent = parentUrl == null ? root : parentUrl;
                    URL url =  appendURL(parent, attributeName, escape(name));
                    instanceUrls.add(url);
                    return url;
                }

                @Override
                public PrintWriter getWriter(URL url) throws IOException {
                    return urlRegistry.getWriter(url);
                }

                @Override
                public List<URL> getAllTemplateUrls() {
                    return templateUrls;
                }

                @Override
                public List<URL> getAllInstanceUrls() {
                    return instanceUrls;
                }

                private URL appendURL(URL parentURL, String... pathElements) throws IOException {
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
            Path path = Paths.get("src/main/resources/Marker").toAbsolutePath();
            if (!Files.exists(path)) {
                throw new IllegalStateException("Could not find marker");
            }
            final Path outputDir = path.getParent().resolve("site-contents");

            return new UrlUtil() {
                {
                    Files.walkFileTree(outputDir, new SimpleFileVisitor<Path>() {
                        @Override
                        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                            Files.delete(file);
                            return FileVisitResult.CONTINUE;
                        }

                        @Override
                        public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                            Files.delete(dir);
                            return FileVisitResult.CONTINUE;
                        }
                    });
                    if (Files.exists(outputDir)) {
                        Files.delete(outputDir);
                    }
                    Files.createDirectories(outputDir);
                    System.out.println("Output directory is " + outputDir.toAbsolutePath());

                }

                @Override
                public URL createTemplateUrl(ManagedObjectType resourceType) throws IOException {
                    return outputDir.resolve(createJsonFileName(resourceType.getName())).toUri().toURL();
                }

                @Override
                public URL createInstanceUrl(String attributeName, URL parentUrl, String name) throws IOException, URISyntaxException {
                    Path parent;
                    if (parentUrl != null) {
                        parent = Paths.get(parentUrl.toURI()).toAbsolutePath();
                        if (!Files.isDirectory(parent)) {
                            //TODO this sucks?
                            String parentName = parent.getFileName().toString();
                            int index = parentName.indexOf(".");
                            if (index >= 0) {
                                parentName = parentName.substring(0, index);
                                parent = parent.getParent().resolve(parentName);
                            }
                        }
                    } else {
                        parent = outputDir;
                    }
                    Path path = parent.resolve(attributeName);
                    if (!Files.exists(path)) {
                        Files.createDirectories(path);
                    }
                    return path.resolve(createJsonFileName(escape(name))).toUri().toURL();
                }

                @Override
                public PrintWriter getWriter(URL url) throws IOException {
                    try {
                        return new PrintWriter(new BufferedWriter(new FileWriter(Paths.get(url.toURI()).toFile())));
                    } catch (URISyntaxException e) {
                        throw new IOException(e);
                    }
                }

                @Override
                public List<URL> getAllTemplateUrls() {
                    //Only bother with this in the servlet case
                    return null;
                }

                @Override
                public List<URL> getAllInstanceUrls() {
                    //Only bother with this in the servlet case
                    return null;
                }

                private String createJsonFileName(String name) {
                    return name.toLowerCase(Locale.ENGLISH) + ".json";
                }
            };
        }

        static String escape(String s) {
            StringBuilder sb = new StringBuilder();
            char[] chars = s.toCharArray();
            for (int i = 0; i < s.length(); i++) {
                final char c = chars[i];

                if (i > 1) { // Windows colon will be here
                    if (!Character.isLetter(c) && !Character.isDigit(c) &&
                            c != '$' && c != '$' && c != '-' && c != '.' && c != '+' && c != '*' && c != '\'' &&
                            c != '(' && c != ')' && c != ',') {
                        String hex = String.format("%02d", (int) c);
                        sb.append("&");
                        sb.append(hex);
                        //sb.append(";");
                        continue;
                    }
                }
                sb.append(c);
            }
            return sb.toString();
        }
    }

    class ServletUrlRegistry {
        final Map<URL, String> jsonByUrl = new LinkedHashMap<>();

        private PrintWriter getWriter(final URL url) {
            final StringWriter stringWriter = new StringWriter();
            return new PrintWriter(stringWriter){
                @Override
                public void close() {
                    super.close();
                    synchronized (this) {
                        jsonByUrl.put(url, stringWriter.getBuffer().toString());
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
                jsonByUrl.entrySet().forEach( entry -> {
                    String current = entry.getKey().toExternalForm();
                    current = current.substring(0, current.lastIndexOf('/'));
                    if (current.equals(val)) {
                        list.add(ModelNode.fromJSONString(entry.getValue()));
                    }
                });

                return new StringReader(list.toJSONString(false));
            }
            return new StringReader(json);
        }

    }
}
