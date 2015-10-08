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
package org.jboss.spec.jsr373.apiexample.resource.objects;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Set;

import org.jboss.dmr.ModelNode;
import org.jboss.spec.jsr373.apiexample.resource.Attribute;
import org.jboss.spec.jsr373.apiexample.resource.AttributeType;
import org.jboss.spec.jsr373.apiexample.resource.ResourceInstance;
import org.jboss.spec.jsr373.apiexample.resource.ResourceTemplate;

/**
 * TODO Jvms are a bit funny since they are currently registered under servers, but referenced under e.g. deployed-objects
 * @author Kabir Khan
 */
public class JvmType extends ManagedObjectType {
    public static final String JAVA_VENDOR = "java-vendor";
    public static final String JAVA_VERSION = "java-version";
    public static final String NODE = "node";
    public static final JvmType INSTANCE = new JvmType("Identifies a JVM used by a server or a managed object");


    private JvmType(String description) {
        super("Jvm", description, null);
    }

    @Override
    public Set<ManagedObjectType> getParents() {
        return parents(ServerType.INSTANCE, AppClientModuleType.INSTANCE, EJBModuleType.INSTANCE, WebModuleType.INSTANCE);
    }

    @Override
    public Set<ManagedObjectType> getParentsForUriTemplate() {
        return parents(ServerType.INSTANCE);
    }

    @Override
    public void addAttributeDescriptions(ResourceTemplate.Builder builder) {
        super.addAttributeDescriptions(builder);
        builder.addAttribute(
                Attribute.createBuilder(JAVA_VENDOR, AttributeType.STRING, "The JVM vendor")
                        .build());
        builder.addAttribute(
                Attribute.createBuilder(JAVA_VERSION, AttributeType.STRING, "The JVM version")
                        .build());
        builder.addAttribute(
                Attribute.createBuilder(NODE, AttributeType.STRING, "The node the JVM is running on")
                        .build());
    }

    public void setDefaultAttributeValues(ResourceInstance.Builder builder) {
        super.setDefaultAttributeValues(builder);
        builder.setAttribute(JAVA_VENDOR, new ModelNode(System.getProperty("java.vendor")));
        builder.setAttribute(JAVA_VERSION, new ModelNode("java.version"));
        try {
            builder.setAttribute(NODE, new ModelNode(InetAddress.getLocalHost().getHostName()));
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }
}
