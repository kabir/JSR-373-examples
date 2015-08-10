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

import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jboss.dmr.ModelNode;
import org.jboss.dmr.ModelType;
import org.jboss.dmr.Property;
import org.jboss.spec.jsr373.apiexample.UrlUtil;
import org.jboss.spec.jsr373.apiexample.resource.objects.ManagedObjectType;

/**
 * @author <a href="mailto:kabir.khan@jboss.com">Kabir Khan</a>
 */
public class ResourceInstance {
    private final UrlUtil urlUtil;
    private final URL url;
    private final ResourceTemplate template;
    private final ResourceInstance parent;
    private final String name;
    private final Map<String, ModelNode> attributes;
    private final Map<String, Set<ResourceInstance>> children = new LinkedHashMap<>();

    private ResourceInstance(UrlUtil urlUtil, URL url, ResourceTemplate template, ResourceInstance parent,
                             String name, Map<String, ModelNode> attributes) throws IOException, URISyntaxException {
        this.urlUtil = urlUtil;
        this.url = url;
        this.template = template;
        this.parent = parent;
        this.name = name;
        this.attributes = attributes;
    }

    static Builder createRootBuilder(UrlUtil urlUtil, ResourceTemplate template, String name) throws IOException, URISyntaxException {
        Builder builder = new Builder(urlUtil, template, null, name);
        return builder;
    }

    URL getUrl() {
       return url;
    }

    public void serialize() throws IOException {
        if (parent != null) {
            throw new IllegalStateException("Can only serialize a parent");
        }
        internalSerialize();
    }

    private void internalSerialize() throws IOException {
        ModelNode output = new ModelNode();
        ModelNode item = output.get(template.getResourceTypeName());
        addLink(item, "self", url);
        addLink(item, "help", template.getUrl());

        item.get("name").set(name);
        for (Map.Entry<String, ModelNode> entry : attributes.entrySet()) {
            item.get(entry.getKey()).set(entry.getValue());
        }

        for (Map.Entry<String, Set<ResourceInstance>> child : children.entrySet()) {
            ModelNode list = item.get(child.getKey()).setEmptyList();
            for (ResourceInstance resourceInstance : child.getValue()) {
                ModelNode link = new ModelNode();
                link.get("rel").set(resourceInstance.template.getUrl().toExternalForm());
                link.get("href").set(resourceInstance.getUrl().toExternalForm());
                list.add(link);
            }
        }

        try (final PrintWriter exampleWriter = urlUtil.getWriter(url)) {
            output.writeJSONString(exampleWriter, false);
        }

        for (Set<ResourceInstance> childSet : children.values()) {
            for (ResourceInstance instance : childSet) {
                instance.internalSerialize();
            }
        }
    }

    private void addLink(ModelNode output, String rel, URL url) {
        ModelNode link = new ModelNode();
        link.get("rel").set(rel);
        link.get("href").set(url.toExternalForm());
        output.get("links").add(link);
    }

    private void addChild(ResourceInstance instance) {
        String attributeName = this.template.getAttributeForChildType(instance.template.getResourceType().getClass());
        Set<ResourceInstance> childInstances = children.get(attributeName);
        if (childInstances == null) {
            childInstances = new LinkedHashSet<>();
            children.put(attributeName, childInstances);
        }
        childInstances.add(instance);
    }

    public static class Builder {
        private final UrlUtil urlUtil;
        private final ResourceTemplate template;
        private final ResourceInstance.Builder parent;
        private final String name;
        private final Map<String, ModelNode> attributes = new LinkedHashMap<>();
        private final Map<String, Set<Builder>> children = new LinkedHashMap<>();
        private final URL url;
        private volatile ResourceInstance builtInstance;


        private Builder(UrlUtil urlUtil, ResourceTemplate template, ResourceInstance.Builder parent, String name) throws IOException, URISyntaxException {
            this(urlUtil, template, null, parent, name);
        }

        private Builder(UrlUtil urlUtil, ResourceTemplate template, String attributeName, ResourceInstance.Builder parent, String name) throws IOException, URISyntaxException {
            this.urlUtil = urlUtil;
            this.template = template;
            this.parent = parent;
            this.name = name;
            String attr = attributeName != null ? attributeName : template.getResourceType().getPath();
            url = urlUtil.createInstanceUrl(attr, parent == null ? null : parent.url, name);
            template.getResourceType().setDefaultAttributeValues(this);
        }

        public Builder setAttribute(String name, ModelNode value) {
            attributes.put(name, value);
            return this;
        }

        public Builder createChildBuilder(ResourceTemplate template, String name) throws IOException, URISyntaxException {
            return createChildBuilder(template, null, name);
        }

        public Builder createChildBuilder(ResourceTemplate template, String attributeName, String name) throws IOException, URISyntaxException {
            if (!template.isValidParent(this.template)) {
                throw new IllegalArgumentException("Bad child type");
            }
            Builder childBuilder = new Builder(urlUtil, template, attributeName, this, name);
            addChildBuilder(childBuilder);

            return childBuilder;
        }


        public Builder createManagedObjectChildBuilder(ResourceTemplate template, String attributeName, String name, ResourceInstance.Builder jvmBuilder) throws IOException, URISyntaxException {
            if (template.getResourceType() instanceof ManagedObjectType == false) {
                throw new IllegalArgumentException("Type is not a managed object");
            }
            Builder childBuilder = createChildBuilder(template, attributeName, name);
            childBuilder.addChildBuilder(jvmBuilder);
            return childBuilder;
        }

        private void addChildBuilder(Builder childBuilder) {
            String attributeName = template.getAttributeForChildType(childBuilder.template.getResourceType().getClass());
            Set<Builder> childBuilders = children.get(attributeName);
            if (childBuilders == null) {
                childBuilders = new LinkedHashSet<>();
                children.put(attributeName, childBuilders);
            }
            childBuilders.add(childBuilder);

        }

        public ResourceInstance build() throws IOException, URISyntaxException {
            if (parent != null) {
                throw new IllegalStateException("Can only be called on the root instances");
            }
            return buildInternal(null);
        }

        private ResourceInstance buildInternal(ResourceInstance parent) throws IOException, URISyntaxException {

            if (builtInstance != null) {
                parent.addChild(builtInstance);
                return builtInstance;
            }
            final Map<String, Attribute> attributeMap = template.getAttributeMap();
            for (String attr : attributes.keySet()) {
                if (!attributeMap.containsKey(attr)) {
                    throw new IllegalStateException("The instance tries to use an attribute '" + attr +
                            "' which does not exist in the template for " + template.getResourceTypeName());
                }
            }
            for (String child : children.keySet()) {
                if (!attributeMap.containsKey(child)) {
                    throw new IllegalStateException("The instance tries to use an attribute '" + child +
                            "' which does not exist in the template for " + template.getResourceTypeName());
                }
            }

            for (Map.Entry<String, Attribute> entry : attributeMap.entrySet()) {
                final Attribute definition = entry.getValue();
                final ModelNode value = attributes.get(entry.getKey());
                if (value == null || !value.isDefined()) {
                    if (!definition.isNillable() && !children.containsKey(entry.getKey())) {
                        throw new IllegalStateException("Attribute '" + entry.getKey() +
                                "' is not nillable and has not been set as a child");
                    }
                    continue;
                }
                validateAttributeValue(definition, value);
            }
            final ResourceInstance instance = new ResourceInstance(urlUtil, url, template, parent, name, attributes);
            if (parent != null) {
                parent.addChild(instance);
            }
            for (Set<Builder> childBuilders : children.values()) {
                for (Builder child : childBuilders) {
                    child.buildInternal(instance);
                }
            }
            builtInstance = instance;

            return instance;
        }

        private void validateAttributeValue(Attribute definition, ModelNode value) {
            if (definition.getType().isSimple()) {
                validateSimpleValue(definition.getType(), definition.getName(), value);
            } else if (definition.getType() == AttributeType.LIST){
                if (value.getType() != ModelType.LIST) {
                    throw new IllegalStateException("'" + definition.getName() + "' is not a list");
                }
                List<ModelNode> list = value.asList();
                for (ModelNode entry : list) {
                    validateSimpleValue(definition.getValueType(), definition.getName(), entry);
                }
            } else if (definition.getType() == AttributeType.MAP) {
                if (value.getType() != ModelType.OBJECT) {
                    throw new IllegalStateException("'" + definition.getName() + "' is not a list");
                }
                for (Property prop : value.asPropertyList()) {
                    validateSimpleValue(definition.getValueType(), definition.getName(), prop.getValue());
                }
            } else {
                throw new IllegalStateException("Unknown type " + definition.getType());
            }
        }

        private void validateSimpleValue(AttributeType type, String attributeName, ModelNode value) {
            switch (type) {
                case BOOLEAN:
                    value.asBoolean();
                    break;
                case DOUBLE:
                case FLOAT:
                    value.asDouble();
                    break;
                case INT:
                    value.asInt();
                    break;
                case LONG:
                    value.asLong();
                    break;
                case STRING:
                    value.asString();
                    break;
                case URL:
                    try {
                        new URL(value.asString());
                    } catch (MalformedURLException e) {
                        throw new IllegalStateException("'" + attributeName + "' is not a valid URL");
                    }
                    break;
                default:
                    throw new IllegalStateException("Unknown type " + type);
            }
        }

        public String getName() {
            return name;
        }

        public URL getUrl() {
            return url;
        }
    }

}
