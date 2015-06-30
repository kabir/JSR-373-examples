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

import java.util.HashMap;
import java.util.Map;

/**
 * @author Kabir Khan
 */
class ManagedObjectTypeRegistry {
    private static final Map<Class<? extends ManagedObjectType>, ManagedObjectType> INSTANCES;
    static {
        Map<Class<? extends ManagedObjectType>, ManagedObjectType> instances = new HashMap<>();
        INSTANCES = initialiseInstances();
    }

    private static Map<Class<? extends ManagedObjectType>, ManagedObjectType> initialiseInstances() {
        Map<Class<? extends ManagedObjectType>, ManagedObjectType> instances = new HashMap<>();
        //Is only adding the concrete types ok?
        addType(instances, DomainType.INSTANCE);
        addType(instances, NullType.INSTANCE);
        addType(instances, ServerType.INSTANCE);
        addType(instances, JvmType.INSTANCE);
        addType(instances, ApplicationType.INSTANCE);
        addType(instances, AppClientModuleType.INSTANCE);
        return instances;
    }

    private static void addType(Map<Class<? extends ManagedObjectType>, ManagedObjectType> instances, ManagedObjectType type) {
        instances.put(type.getClass(), type);
    }

    public static ManagedObjectType getInstanceForClass(Class<? extends ManagedObjectType> type) {
        ManagedObjectType instance =  INSTANCES.get(type);
        if (instance == null) {
            throw new IllegalArgumentException("Make sure to add " + type.getSimpleName() + " to the registry");
        }
        return instance;
    }
}
