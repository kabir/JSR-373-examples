/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2015, Red Hat, Inc., and individual contributors
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
package org.jboss.spec.jsr373.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.net.URL;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jboss.spec.jsr373.apiexample.ExampleGenerator;
import org.jboss.spec.jsr373.apiexample.UrlUtil;

/**
 * @author Kabir Khan
 */
@WebServlet(urlPatterns = {"/contents/*", "/index.html"})
public class JSR373Servlet extends HttpServlet {
    private static volatile UrlUtil.ServletUrlRegistry urlRegistry;
    private static volatile List<URL> templateUrls;
    private static volatile List<URL> instanceUrls;
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (urlRegistry == null) {
            synchronized (this) {
                if (urlRegistry == null) {
                    UrlUtil.ServletUrlRegistry reg = new UrlUtil.ServletUrlRegistry();
                    UrlUtil urlUtil = UrlUtil.Factory.createServletInstance(reg);
                    ExampleGenerator generator = new ExampleGenerator(urlUtil);
                    try {
                        generator.generate();
                    } catch (Exception e) {
                        throw new ServletException("Initialising the data failed", e);
                    }
                    urlRegistry = reg;
                    templateUrls = urlUtil.getAllTemplateUrls();
                    instanceUrls = urlUtil.getAllInstanceUrls();
                }
            }
        }

        if (req.getServletPath().equals("/index.html")) {
            indexHtml(resp);
            return;
        }

        final URL url = new URL(req.getRequestURL().toString());
        final Reader reader = urlRegistry.getReader(url);
        if (reader == null) {
            resp.sendError(resp.SC_NOT_FOUND, url.toExternalForm() + " could not be found");
            return;
        }
        try {
            resp.setContentType("application/json; charset=utf-8");
            PrintWriter writer = resp.getWriter();
            char[] buffer = new char[1024];
            int len;
            while ((len = reader.read(buffer)) >= 0) {
                writer.write(buffer, 0, len);
            }
        } finally {
            reader.close();
        }
    }

    private void indexHtml(HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("text/html; charset=utf8");
        PrintWriter writer = resp.getWriter();
        writer.write("<html>");
        writer.write("<head>");
        writer.write("<title>JSR 373 API Example</title>");
        writer.write("</head>");
        writer.write("<body>");
        writer.write("<h1>API Examples</h1>");
        writer.write("<p>This is a mock-up of what could be returned for a running server based on the object types in JSR 77. " +
                "The source code can be found at <a href=\"https://github.com/kabir/JSR-373-examples\">https://github.com/kabir/JSR-373-examples</a></p>");
        writer.write("<p>To navigate the examples, I recommend using Chrome and the JSON Formatter plug-in which gives you clickable links" +
                "(although there are probably loads of other ways!)</p>");
        writer.write("<h2>Templates</h2>");
        writer.write("<p>This is the list of the 'templates' which describe what is available at each instance address/type. " +
                "The instances in the next section link to these via their 'help' links.</p>");
        writer.write("<ul>");
        printUrls(templateUrls, writer);
        writer.write("</ul>");
        writer.write("<h2>Instances</h2>");
        writer.write("<p>This is the list of the instances in the mock-up. You would probably normally start at the domain or " +
                "the server, and navigate through the tree with links. The list is included here for ease of navigation</p>");
        writer.write("<p>All instances belong to a collection, so where the instance url is e.g. at the path <i>/domains/main</i>, " +
                "the collection url can be found at <i>/domains</i></p>");
        writer.write("<ul>");
        printUrls(instanceUrls, writer);
        writer.write("</ul>");
        writer.write("</body>");
        writer.write("</html>");
    }

    private void printUrls(List<URL> urls,  PrintWriter writer) {
        for (URL url : urls) {
            writer.write("<li><a href=\"" + url + "\">" + url + "</a></li>");
        }
    }
}
