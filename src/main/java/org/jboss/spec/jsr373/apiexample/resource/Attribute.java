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

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.jboss.dmr.ModelNode;
import org.jboss.spec.jsr373.apiexample.resource.objects.ManagedObjectType;

/**
 * @author <a href="mailto:kabir.khan@jboss.com">Kabir Khan</a>
 */
public class Attribute {
    private final String name;
    private final String description;
    private final AttributeType type;
    private final AttributeType valueType;
    private final boolean nillable;
    private final AttributeAccess access;
    private final Set<Class<? extends ManagedObjectType>> handledChildTypes;

    private Attribute(String name, String description, AttributeType type, AttributeType valueType, boolean nillable,
                      AttributeAccess access, Set<Class<? extends ManagedObjectType>> handledChildTypes) {
        this.name = name;
        this.description = description;
        this.type = type;
        this.valueType = valueType;
        this.nillable = nillable;
        this.access = access;
        this.handledChildTypes = Collections.unmodifiableSet(handledChildTypes);
    }

    public String getName() {
        return name;
    }

    public static Builder createBuilder(String name, AttributeType type, String description) {
        return new Builder(name, type, description);
    }

    AttributeType getType() {
        return type;
    }

    AttributeType getValueType() {
        return valueType;
    }

    boolean isNillable() {
        return nillable;
    }

    ModelNode toModelNode() {
        ModelNode model = new ModelNode();
        model.get("description").set(description);
        model.get("type").set(type.toString());
        if (!type.isSimple()) {
            model.get("value-type").set(valueType.toString());
        }
        model.get("access").set(access.toString());
        model.get("nillable").set(nillable);
        if (!handledChildTypes.isEmpty()) {
            ModelNode allowedTypes = model.get("allowed-types").setEmptyObject();
            for (Class<? extends ManagedObjectType> type : handledChildTypes) {
                Util.addTypeLinkToMap(allowedTypes, ManagedObjectType.getInstanceForClass(type));
            }
        }
        return model;
    }

    public Set<Class<? extends ManagedObjectType>> getHandledChildTypes() {
        return handledChildTypes;
    }

    public static class Builder {
        final String name;
        final AttributeType type;
        final String description;
        boolean nillable = false;
        AttributeAccess access = AttributeAccess.READ_ONLY;
        AttributeType valueType;
        private final Set<Class<? extends ManagedObjectType>> handledChildTypes = new HashSet<>();

        private Builder(String name, AttributeType type, String description) {
            this.name = name;
            this.type = type;
            this.description = description;
        }

        public Builder setNillable() {
            nillable = true;
            return this;
        }

        public Builder setAccess(AttributeAccess access) {
            this.access = access;
            return this;
        }

        public Builder addHandledChildTypes(Class<? extends ManagedObjectType>...types) {
            for (Class<? extends ManagedObjectType> type : types) {
                handledChildTypes.add(type);
            }
            return this;
        }

        public Builder setValueType(AttributeType valueType) {
            if (type.isSimple()) {
                throw new IllegalStateException("Can only set value type for collections");
            }
            if (!valueType.isSimple()) {
                throw new IllegalStateException("Nested collections not supported");
            }
            this.valueType = valueType;
            return this;
        }

        public Attribute build() {
            if (!type.isSimple() && valueType == null) {
                throw new IllegalStateException("Null value type for a " + type);
            }
            if (handledChildTypes.size() > 0) {
                if (nillable) {
                    throw new IllegalStateException("A child type must be non-nillable");
                }
                if (type == AttributeType.URL || (!type.isSimple() && valueType == AttributeType.URL)) {
                } else {
                    throw new IllegalStateException("A child type must be either a simple or a collection of urls");
                }
            }
            return new Attribute(name, description, type, valueType, nillable, access, handledChildTypes);
        }
    }
}
