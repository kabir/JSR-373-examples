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

import org.jboss.spec.jsr373.apiexample.resource.Attribute;
import org.jboss.spec.jsr373.apiexample.resource.AttributeType;
import org.jboss.spec.jsr373.apiexample.resource.ResourceTemplate;

/**
 * @author Kabir Khan
 */
public class WebModuleType extends ModuleType {

    public static final String SERVLETS = "servlets";

    public static final WebModuleType INSTANCE = new WebModuleType();

    private WebModuleType() {
        super("WebModule", "web-module", "Identifies a deployed WAR module");
    }

    @Override
    public Set<ManagedObjectType> getParents() {
        Set<ManagedObjectType> parents = super.getParents();
        parents.add(ApplicationType.INSTANCE);
        return parents;
    }

    @Override
    public void addAttributeDescriptions(ResourceTemplate.Builder builder) {
        super.addAttributeDescriptions(builder);

        builder.addAttribute(
                Attribute.createBuilder(SERVLETS, AttributeType.LIST, "A list of servlets contained in this web module")
                        .setValueType(AttributeType.URL)
                        .addHandledChildTypes(ServletType.class)
                        .build());
    }
}
