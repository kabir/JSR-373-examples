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
@WebServlet(urlPatterns = "/contents/*")
public class JSR373Servlet extends HttpServlet {
    private static volatile UrlUtil.ServletUrlRegistry urlRegistry;
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        System.out.println("-- request");
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
                }
            }
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
}
