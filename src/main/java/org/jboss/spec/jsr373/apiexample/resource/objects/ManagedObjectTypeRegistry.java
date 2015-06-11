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
        return instances;
        //This does not work in a server environment since it uses vfs
//        try {
//            Map<Class<? extends ManagedObjectType>, ManagedObjectType> instances = new HashMap<>();
//            Package pkg = ManagedObjectType.class.getPackage();
//            URL url = ManagedObjectType.class.getResource(ManagedObjectType.class.getSimpleName() + ".class");
//            File packageDir = new File(url.toURI()).getParentFile();
//            for (File file : packageDir.listFiles()) {
//                String name = file.getName();
//                int index = name.indexOf(".class");
//                if (index == -1) {
//                    continue;
//                }
//                name = name.substring(0, index);
//                Class<?> clazz = Class.forName(pkg.getName() + "." + name);
//                if ((clazz.getModifiers() & Modifier.ABSTRACT) == Modifier.ABSTRACT) {
//                    continue;
//                }
//                boolean inheritsManagedObjectType = false;
//                Class<?> current = clazz;
//                while (current != Object.class) {
//                    if (current == ManagedObjectType.class) {
//                        inheritsManagedObjectType = true;
//                        break;
//                    }
//                    current = current.getSuperclass();
//                }
//                if (inheritsManagedObjectType) {
//                    Field field = clazz.getDeclaredField("INSTANCE");
//                    ManagedObjectType type = (ManagedObjectType)field.get(null);
//                    instances.put((Class<? extends ManagedObjectType>)clazz, type);
//                }
//            }
//            return instances;
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }
    }

    private static void addType(Map<Class<? extends ManagedObjectType>, ManagedObjectType> instances, ManagedObjectType type) {
        instances.put(type.getClass(), type);
    }

    public static ManagedObjectType getInstanceForClass(Class<? extends ManagedObjectType> type) {
        return INSTANCES.get(type);
    }
}
