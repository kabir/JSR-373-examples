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

import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.jboss.spec.jsr373.apiexample.resource.Attribute;
import org.jboss.spec.jsr373.apiexample.resource.AttributeType;
import org.jboss.spec.jsr373.apiexample.resource.ResourceTemplate;

/**
 * A JEEDomain.
 *
 * @author <a href="mailto:kabir.khan@jboss.com">Kabir Khan</a>
 */
public class DomainType extends ManagedObjectType {
    public static final String SERVERS = "servers";
    public static ManagedObjectType INSTANCE = new DomainType();

    private final List<ManagedObjectType> parents = Collections.emptyList();

    private DomainType() {
        super("JEEDomain", "The domain is the root concept, and there must be at least one", "domains");
    }

    @Override
    public Set<ManagedObjectType> getParents() {
        return Collections.singleton(NullType.INSTANCE);
    }

    @Override
    public void addAttributeDescriptions(ResourceTemplate.Builder builder) {
        super.addAttributeDescriptions(builder);

        builder.addAttribute(
                Attribute.createBuilder(SERVERS, AttributeType.LIST, "A list of servers belonging to this domain")
                        .setValueType(AttributeType.URL)
                        .addHandledChildTypes(ServerType.class)
                        .build());
    }
}
