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

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

import org.jboss.dmr.ModelNode;
import org.jboss.spec.jsr373.apiexample.resource.Attribute;
import org.jboss.spec.jsr373.apiexample.resource.AttributeType;
import org.jboss.spec.jsr373.apiexample.resource.ResourceInstance;
import org.jboss.spec.jsr373.apiexample.resource.ResourceTemplate;

/**
 * @author <a href="mailto:kabir.khan@jboss.com">Kabir Khan</a>
 */
public abstract class ManagedObjectType {
    public static final String NAME = "name";

    //ManagedObject attributes
    public static final String STATE_MANAGEABLE = "state-manageable";
    public static final String STATISTICS_PROVIDER = "statistics-provider";
    public static final String EVENT_PROVIDER = "event-provider";

    //StateManageable attributes
    public static final String STATE = "state";
    public static final String START_TIME = "start-time";

    private final String name;
    private final String description;
    private ResourceTemplate template;
    /** The name of this if it is a root object with noone */
    private final String rootName;


    protected ManagedObjectType(String name, String description, String rootName) {
        this.name = name;
        this.description = description;
        this.rootName = rootName;
    }

    public static ManagedObjectType getInstanceForClass(Class<? extends ManagedObjectType> type) {
        return ManagedObjectTypeRegistry.getInstanceForClass(type);
    }

    public final String getName() {
        return name;
    }

    public final String getDescription() {
        return description;
    }


    public abstract Set<ManagedObjectType> getParents();

    public Set<ManagedObjectType> getParentsForUriTemplate() {
        return getParents();
    }

    public ResourceTemplate getTemplate() {
        return template;
    }

    public String getRootName() {
        return rootName;
    }

    public void addAttributeDescriptions(ResourceTemplate.Builder builder) {
        //ManagedObject attributes
        builder.addAttribute(
                Attribute.createBuilder(NAME, AttributeType.STRING, "The name of this " + builder.getType())
                        .build());
        builder.addAttribute(
                Attribute.createBuilder(STATE_MANAGEABLE, AttributeType.BOOLEAN, "Whether the object is state manageable.")
                        .build());
        builder.addAttribute(
                Attribute.createBuilder(STATISTICS_PROVIDER, AttributeType.BOOLEAN, "Whether the object supports the generation of statistics.")
                        .build());
        builder.addAttribute(
                Attribute.createBuilder(EVENT_PROVIDER, AttributeType.BOOLEAN, "Whether the object is state manageable.")
                        .build());

        //StateManageable attributes
        builder.addAttribute(
                Attribute.createBuilder(STATE, AttributeType.STRING, "The state of the managed object. Will only be available if state-manageable=true. ")
                        .setNillable()
                        .build());
        builder.addAttribute(
                Attribute.createBuilder(START_TIME, AttributeType.DATE, "The time represented as in RFC 3339. Will only be available if state-manageable=true.")
                        .setNillable()
                        .build());
    }

    public void setDefaultAttributeValues(ResourceInstance.Builder builder) {
        builder.setAttribute(NAME, new ModelNode(builder.getName()));
        builder.setAttribute(STATE_MANAGEABLE, new ModelNode(false));
        builder.setAttribute(STATISTICS_PROVIDER, new ModelNode(false));
        builder.setAttribute(EVENT_PROVIDER, new ModelNode(false));
    }

    public final void setTemplate(ResourceTemplate template) {
        if (this.template != null) {
            throw new IllegalStateException("Already built a template for " + name);
        }
        this.template = template;
    }

    static Set<ManagedObjectType> parents(ManagedObjectType...parents) {
        Set<ManagedObjectType> set = new LinkedHashSet<>();
        Arrays.asList(parents).forEach(parent -> set.add(parent));
        return set;
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj instanceof ManagedObjectType == false) {
            return false;
        }
        return name.equals(((ManagedObjectType)obj).name);
    }
}
