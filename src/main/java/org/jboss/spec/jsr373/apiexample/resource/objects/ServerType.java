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
package org.jboss.spec.jsr373.apiexample.resource.objects;

import java.util.Set;

import org.jboss.dmr.ModelNode;
import org.jboss.spec.jsr373.apiexample.resource.Attribute;
import org.jboss.spec.jsr373.apiexample.resource.AttributeType;
import org.jboss.spec.jsr373.apiexample.resource.ResourceInstance;
import org.jboss.spec.jsr373.apiexample.resource.ResourceTemplate;

/**
 * @author Kabir Khan
 */
public class ServerType extends ManagedObjectType {
    public static final String SERVER_VENDOR = "serverVendor";
    public static final String SERVER_VERSION = "serverVersion";
    public static final String JAVA_VMS = "javaVMs";
    public static final String DEPLOYED_OBJECTS = "deployedObjects";
    public static ManagedObjectType INSTANCE = new ServerType();

    private ServerType() {
        super("JEEServer", "server", "Represents a Java EE server");
    }

    @Override
    public Set<ManagedObjectType> getParents() {
        return parents(NullType.INSTANCE, DomainType.INSTANCE);
    }

    @Override
    public void addAttributeDescriptions(ResourceTemplate.Builder builder) {
        super.addAttributeDescriptions(builder);
        builder.addAttribute(
                Attribute.createBuilder(SERVER_VENDOR, AttributeType.STRING, "The server vendor")
                        .build());
        builder.addAttribute(
                Attribute.createBuilder(SERVER_VERSION, AttributeType.STRING, "The server version")
                        .build());
        builder.addAttribute(
                Attribute.createBuilder(JAVA_VMS, AttributeType.LIST, "A list of all JVMs on the server")
                        .setValueType(AttributeType.URL)
                        .addHandledChildTypes(JvmType.class)
                        .build());
        builder.addAttribute(
                Attribute.createBuilder(DEPLOYED_OBJECTS, AttributeType.LIST, "A list of all JVMs on the server")
                        .setValueType(AttributeType.URL)
                        .addHandledChildTypes(ApplicationType.class, AppClientModuleType.class,
                                WebModuleType.class, EJBModuleType.class)
                        .build());
    }

    public void setDefaultAttributeValues(ResourceInstance.Builder builder) {
        super.setDefaultAttributeValues(builder);
        builder.setAttribute(ServerType.SERVER_VENDOR, new ModelNode("Server Co"));
        builder.setAttribute(ServerType.SERVER_VERSION, new ModelNode("3.7.3"));
    }

}
