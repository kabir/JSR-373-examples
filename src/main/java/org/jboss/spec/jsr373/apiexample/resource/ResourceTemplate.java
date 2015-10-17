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
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jboss.dmr.ModelNode;
import org.jboss.spec.jsr373.apiexample.UrlUtil;
import org.jboss.spec.jsr373.apiexample.resource.objects.ManagedObjectType;
import org.jboss.spec.jsr373.apiexample.resource.objects.NullType;


/**
 * @author <a href="mailto:kabir.khan@jboss.com">Kabir Khan</a>
 */
public class ResourceTemplate {
    private final UrlUtil urlUtil;
    private final ManagedObjectType resourceType;
    private final Map<String, Attribute> attributeMap;
    private final Map<String, Set<Class<? extends ManagedObjectType>>> children;
    private final Map<Class<? extends ManagedObjectType>, String> childrenByType = new HashMap<>();
    private final URL url;

    private static final List<ResourceTemplate> ALL_TEMPLATES = new ArrayList<>();

    private ResourceTemplate(UrlUtil urlUtil, ManagedObjectType resourceType, Map<String, Attribute> attributeMap,
                             Map<String, Set<Class<? extends ManagedObjectType>>> children) throws IOException {
        this.urlUtil = urlUtil;
        this.resourceType = resourceType;
        this.attributeMap = Collections.unmodifiableMap(attributeMap);
        this.children = Collections.unmodifiableMap(children);
        for (Map.Entry<String, Set<Class<? extends ManagedObjectType>>> entry : children.entrySet()) {
            for (Class<? extends ManagedObjectType> type : entry.getValue()) {
                if (childrenByType.containsKey(type)) {
                    throw new IllegalStateException("Ambiguous entry " + type);
                }
                childrenByType.put(type, entry.getKey());
            }
        }
        this.url = urlUtil.createTemplateUrl(resourceType);
        resourceType.setTemplate(this);

    }

    public static ResourceTemplate createTemplate(UrlUtil urlUtil, ManagedObjectType resourceType) throws IOException {
        Builder builder =  new Builder(urlUtil, resourceType);
        resourceType.addAttributeDescriptions(builder);
        ResourceTemplate template = builder.build();
        ALL_TEMPLATES.add(template);
        return template;
    }


    public static void serializeTemplates() throws IOException, URISyntaxException {
        for (ResourceTemplate template : ALL_TEMPLATES) {
            template.serialize();
        }
    }

    Map<String, Attribute> getAttributeMap() {
        return attributeMap;
    }

    public ManagedObjectType getResourceType() {
        return resourceType;
    }

    String getResourceTypeName() {
        return resourceType.getName();
    }

    public void serialize() throws IOException, URISyntaxException {

        ModelNode model = new ModelNode();
        addLinks(model);
        model.get("object-type").set(resourceType.getName());
        model.get("description").set(resourceType.getDescription());
        addParents(model);
        addAttributes(model);
        addHttpMethods(model);

        try (final PrintWriter exampleWriter = urlUtil.getWriter(url)) {
            model.writeJSONString(exampleWriter, false);
        }
    }

    private void addLinks(ModelNode model) {
        ModelNode links = model.get("links");
        ModelNode self = new ModelNode();
        self.get("rel").set("self");
        self.get("href").set(url.toExternalForm());
        links.add(self);
    }

    private void addParents(ModelNode model) throws IOException {
        model.get("allow-null-parent").set(resourceType.getParents().contains(NullType.INSTANCE));
        ModelNode parents = model.get("parents").setEmptyObject();
        for (ManagedObjectType type : resourceType.getParents()) {
            if (type == NullType.INSTANCE) {
                //We use allow-null-parent for this
                continue;
            } else {
                Util.addTypeLinkToMap(parents, type);
            }
        }
    }

    private void addAttributes(ModelNode model) {
        ModelNode attributes = model.get("attributes");
        attributeMap.forEach((key, value) -> attributes.get(key).set(value.toModelNode()));
    }

    private void addHttpMethods(ModelNode modelNode) {
        final List<String> urlPatterns = getAllUrlPatterns();

        ModelNode httpMethods = modelNode.get("http-methods");
        ModelNode get = httpMethods.get("GET");

        get.add(createOperation(urlPatterns, "Returns a named " + resourceType.getName() + " instance", url,
                null));
        get.add(createOperation(urlPatterns, "Returns a list of the " + resourceType.getName() + "s under the parent", url,
                pattern -> pattern.substring(0, pattern.lastIndexOf("/"))));

        addPostOperations(httpMethods, urlPatterns);
    }

    private void addPostOperations(ModelNode httpMethods, List<String> urlPatterns) {
        ModelNode post = httpMethods.get("POST");
        getStateManageableOperations(urlPatterns).forEach(op -> post.add(op));
        //TODO Event Provider
        //TODO Performance Monitor
        return;
    }
    private List<ModelNode> getStateManageableOperations(List<String> urlPatterns) {
        List<ModelNode> ops = new ArrayList<>();
        ops.add(createOperation(urlPatterns, "Starts this " + resourceType.getName() + ".", null,
                pattern -> pattern + "/operations/start"));
        ops.add(createOperation(urlPatterns, "Starts this " + resourceType.getName() + " and all its children.", null,
                pattern -> pattern + "/operations/start-recursive"));
        ops.add(createOperation(urlPatterns, "Stops this " + resourceType.getName() + " and all its children.", null,
                pattern -> pattern + "/operations/stop"));
        return ops;
    }

    private ModelNode createOperation(List<String> urlPatterns, String description, URL help, URLPatternDecorator patternDecorator) {
        ModelNode op = new ModelNode();
        ModelNode patterns = op.get("url-patterns");
        urlPatterns.forEach(s -> patterns.add(patternDecorator == null ? s : patternDecorator.decorate(s)));
        op.get("description").set(description);
        if (help != null) {
            op.get("link", "rel").set("help");
            op.get("link", "href").set(url.toExternalForm());
        }
        return op;
    }

    public ResourceInstance.Builder createRootInstanceBuilder(String name) throws IOException, URISyntaxException {
        ResourceInstance.Builder builder = ResourceInstance.createRootBuilder(urlUtil, this, name);
        return builder;
    }

    URL getUrl() {
        return url;
    }

    @Override
    public int hashCode() {
        return resourceType.getName().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return resourceType == obj;
    }

    boolean isValidParent(ResourceTemplate parent) {
        return resourceType.getParents().contains(parent.getResourceType());
    }

    String getAttributeForChildType(Class<? extends ManagedObjectType> type) {
        Class<?> current = type;
        String attribute = null;
        while (true) {
            attribute = childrenByType.get(current);
            if (attribute != null) {
                return attribute;
            }
            if (current.getSuperclass() == Object.class){
                throw new IllegalStateException("Could not find attribute for " + type);
            }
            current = current.getSuperclass();
        }
    }

    private List<String> getAllUrlPatterns() {
        List<List<ResourceTemplate>> parents = new ArrayList<>();
        parents.add(new ArrayList<>());
        getAllParentTypesForUrlPattern(parents);

        List<String> result = new ArrayList<>(parents.size());
        for (List<ResourceTemplate> parentList : parents) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < parentList.size(); i++) {
                ResourceTemplate template = parentList.get(i);
                if (i == 0) {
                    String rootName = template.getResourceType().getRootName();
                    assert rootName != null;
                    sb.append("/");
                    sb.append(rootName);
                    sb.append("/{" + createId(rootName) + "-id}");
                } else {
                    ResourceTemplate parent = parentList.get(i - 1);
                    sb.append("/");
                    String attribute = parent.getAttributeForChildType(template.getResourceType().getClass());
                    sb.append(attribute);
                    sb.append("/{" + createId(attribute) + "-id}");
                }
            }
            result.add(sb.toString());
        }

        return result;
    }

    String createId(String s) {
        if (s.endsWith("s")) {
            return s.substring(0, s.length() - 1);
        }
        return s;
    }

    private void getAllParentTypesForUrlPattern(List<List<ResourceTemplate>> parents) {
        Set<ManagedObjectType> parentTypes = resourceType.getParentsForUriTemplate();
        Map<ManagedObjectType, List<List<ResourceTemplate>>> parentsMap = new LinkedHashMap<>();
        boolean first = true;
        for (ManagedObjectType parent : parentTypes) {
            if (first) {
                parentsMap.put(parent, parents);
                first = false;
            } else {
                //We need to branch
                List<List<ResourceTemplate>> branch = new ArrayList<>();
                parents.forEach(list -> branch.add(new ArrayList<>(list)));
                parentsMap.put(parent, branch);
            }
        }

        for (Map.Entry<ManagedObjectType, List<List<ResourceTemplate>>> entry : parentsMap.entrySet()) {
            List<List<ResourceTemplate>> parentList = entry.getValue();

            if (entry.getKey() != NullType.INSTANCE) {
                ResourceTemplate parent = entry.getKey().getTemplate();
                parent.getAllParentTypesForUrlPattern(parentList);
            }
            parentList.forEach(list -> list.add(this));
        }

        if (parentTypes.size() > 0) {
            //The first entry in the list is up to date, add the other ones
            first = true;
            for (List<List<ResourceTemplate>> listForParent : parentsMap.values()) {
                if (first) {
                    first = false;
                    continue;
                }
                listForParent.forEach(list -> parents.add(list));
            }
        }
    }

    @Override
    public String toString() {
        return "ResourceTemplate(" + resourceType + ")";
    }

    public static class Builder {
        private final UrlUtil urlUtil;
        private final ManagedObjectType resourceType;
        private final Map<String, Attribute> attributes = new LinkedHashMap<>();
        private final Map<String, Set<Class<? extends ManagedObjectType>>> children = new HashMap<>();

        private Builder(UrlUtil urlUtil, ManagedObjectType resourceType) {
            this.urlUtil = urlUtil;
            this.resourceType = resourceType;
        }

        public Builder addAttribute(Attribute attribute) {
            attributes.put(attribute.getName(), attribute);
            if (!attribute.getHandledChildTypes().isEmpty()) {
                createChildType(attribute.getName(), attribute.getHandledChildTypes());
            }
            return this;
        }

        private Builder createChildType(String name, Set<Class<? extends ManagedObjectType>> childTypes) {
            Set<Class<? extends ManagedObjectType>> set = new HashSet<>();
            for (Class<? extends ManagedObjectType> type : childTypes) {
                set.add(type);
            }
            children.put(name, Collections.unmodifiableSet(set));
            return this;
        }

        public ResourceTemplate build() throws IOException {
            return new ResourceTemplate(urlUtil, resourceType, attributes, children);

        }

        public String getType() {
            return resourceType.getName();
        }
    }

    @FunctionalInterface
    interface URLPatternDecorator {
        String decorate(String pattern);
    }
}
