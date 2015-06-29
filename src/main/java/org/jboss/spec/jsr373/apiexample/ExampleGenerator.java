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

package org.jboss.spec.jsr373.apiexample;

import org.jboss.spec.jsr373.apiexample.resource.ResourceInstance;
import org.jboss.spec.jsr373.apiexample.resource.ResourceTemplate;
import org.jboss.spec.jsr373.apiexample.resource.objects.AppClientModuleType;
import org.jboss.spec.jsr373.apiexample.resource.objects.DomainType;
import org.jboss.spec.jsr373.apiexample.resource.objects.JvmType;
import org.jboss.spec.jsr373.apiexample.resource.objects.ServerType;

/**
 * @author <a href="mailto:kabir.khan@jboss.com">Kabir Khan</a>
 */
public class ExampleGenerator {
    private final UrlUtil urlUtil;

    public ExampleGenerator(UrlUtil urlUtil) {
        this.urlUtil = urlUtil;
    }

    public void generate() throws Exception {
        //Set up all the templates
        ResourceTemplate domain = ResourceTemplate.createTemplate(urlUtil, DomainType.INSTANCE);
        ResourceTemplate server = ResourceTemplate.createTemplate(urlUtil, ServerType.INSTANCE);
        ResourceTemplate jvm =  ResourceTemplate.createTemplate(urlUtil, JvmType.INSTANCE);
        ResourceTemplate appClient = ResourceTemplate.createTemplate(urlUtil, AppClientModuleType.INSTANCE);

        //Serialize all the templates
        ResourceTemplate.serializeTemplates();

        //Now create the instances
        ResourceInstance.Builder domainMainBuilder = domain.createRootInstanceBuilder("main");
        ResourceInstance.Builder serverOneBuilder = domainMainBuilder.createChildBuilder(server, "one");
        ResourceInstance.Builder jvmOneBuilder = serverOneBuilder.createChildBuilder(jvm, "one");

        ResourceInstance.Builder appClientOneTopBuilder =
                serverOneBuilder.createManagedObjectChildBuilder(appClient, "app-client-top.jar", jvmOneBuilder);


        //Build and serialize the root instance which will also do the same for the children
        ResourceInstance domainMain = domainMainBuilder.build();
        domainMain.serialize();
    }
}
