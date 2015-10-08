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

import java.util.Set;

import org.jboss.dmr.ModelNode;
import org.jboss.spec.jsr373.apiexample.resource.Attribute;
import org.jboss.spec.jsr373.apiexample.resource.AttributeType;
import org.jboss.spec.jsr373.apiexample.resource.ResourceInstance;
import org.jboss.spec.jsr373.apiexample.resource.ResourceTemplate;

/**
 * @author Kabir Khan
 */
public abstract class DeployedObjectType extends ManagedObjectType {
    public static final String DEPLOYMENT_DESCRIPTOR = "deployment-descriptor";
    public static final String SERVER_ATTR = "deployed-objects";
    public static final String JEE_MODULE_ATTR = "modules";

    protected DeployedObjectType(String name, String path, String description) {
        super(name, description, null);
    }

    @Override
    public void addAttributeDescriptions(ResourceTemplate.Builder builder) {
        super.addAttributeDescriptions(builder);
        builder.addAttribute(
                Attribute.createBuilder(DEPLOYMENT_DESCRIPTOR, AttributeType.STRING, "The deployment descriptor used")
                .build());
    }

    @Override
    public void setDefaultAttributeValues(ResourceInstance.Builder builder) {
        super.setDefaultAttributeValues(builder);
        builder.setAttribute(DEPLOYMENT_DESCRIPTOR, new ModelNode("This seems a bit pointless to me? If not we need to flesh out the format"));
    }

    @Override
    public Set<ManagedObjectType> getParents() {
        Set<ManagedObjectType> parents = parents(ServerType.INSTANCE, ApplicationType.INSTANCE);
        return parents;
    }
}
