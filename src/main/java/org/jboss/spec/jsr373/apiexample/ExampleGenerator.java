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

import java.io.IOException;
import java.net.URISyntaxException;

import org.jboss.spec.jsr373.apiexample.resource.ResourceInstance;
import org.jboss.spec.jsr373.apiexample.resource.ResourceTemplate;
import org.jboss.spec.jsr373.apiexample.resource.objects.AppClientModuleType;
import org.jboss.spec.jsr373.apiexample.resource.objects.ApplicationType;
import org.jboss.spec.jsr373.apiexample.resource.objects.DomainType;
import org.jboss.spec.jsr373.apiexample.resource.objects.EJBModuleType;
import org.jboss.spec.jsr373.apiexample.resource.objects.EntityBeanType;
import org.jboss.spec.jsr373.apiexample.resource.objects.JvmType;
import org.jboss.spec.jsr373.apiexample.resource.objects.MessageDrivenBeanType;
import org.jboss.spec.jsr373.apiexample.resource.objects.ServerType;
import org.jboss.spec.jsr373.apiexample.resource.objects.ServletType;
import org.jboss.spec.jsr373.apiexample.resource.objects.StatefulSessionBeanType;
import org.jboss.spec.jsr373.apiexample.resource.objects.StatelessSessionBeanType;
import org.jboss.spec.jsr373.apiexample.resource.objects.WebModuleType;

/**
 * @author <a href="mailto:kabir.khan@jboss.com">Kabir Khan</a>
 */
public class ExampleGenerator {
    private final UrlUtil urlUtil;

    private final ResourceTemplate domain;
    private final ResourceTemplate server;
    private final ResourceTemplate jvm;
    private final ResourceTemplate application;
    private final ResourceTemplate appClient;
    private final ResourceTemplate webModule;
    private final ResourceTemplate servlet;
    private final ResourceTemplate ejbModule;
    private final ResourceTemplate entityBean;
    private final ResourceTemplate messageDrivenBean;
    private final ResourceTemplate statefulSessionBean;
    private final ResourceTemplate statelessSessionBean;



    public ExampleGenerator(UrlUtil urlUtil) throws IOException {
        this.urlUtil = urlUtil;
        //Set up all the templates
        domain = ResourceTemplate.createTemplate(urlUtil, DomainType.INSTANCE);
        server = ResourceTemplate.createTemplate(urlUtil, ServerType.INSTANCE);
        jvm =  ResourceTemplate.createTemplate(urlUtil, JvmType.INSTANCE);
        application = ResourceTemplate.createTemplate(urlUtil, ApplicationType.INSTANCE);
        appClient = ResourceTemplate.createTemplate(urlUtil, AppClientModuleType.INSTANCE);
        webModule = ResourceTemplate.createTemplate(urlUtil, WebModuleType.INSTANCE);
        servlet = ResourceTemplate.createTemplate(urlUtil, ServletType.INSTANCE);
        ejbModule = ResourceTemplate.createTemplate(urlUtil, EJBModuleType.INSTANCE);
        entityBean = ResourceTemplate.createTemplate(urlUtil, EntityBeanType.INSTANCE);
        messageDrivenBean = ResourceTemplate.createTemplate(urlUtil, MessageDrivenBeanType.INSTANCE);
        statefulSessionBean = ResourceTemplate.createTemplate(urlUtil, StatefulSessionBeanType.INSTANCE);
        statelessSessionBean = ResourceTemplate.createTemplate(urlUtil, StatelessSessionBeanType.INSTANCE);
    }

    public void generate() throws Exception {

        //Serialize all the templates
        ResourceTemplate.serializeTemplates();

        //Now create the instances
        ResourceInstance.Builder domainMainBuilder = domain.createRootInstanceBuilder("main");
        ResourceInstance.Builder serverOneBuilder = domainMainBuilder.createChildBuilder(server, "one");
        ResourceInstance.Builder jvmOneBuilder = serverOneBuilder.createChildBuilder(jvm, "one");
        ResourceInstance.Builder jvmTwoBuilder = serverOneBuilder.createChildBuilder(jvm, "two");

        //Add some top-level deployments
        addDeployedObjects(serverOneBuilder, jvmOneBuilder);
        //Add an ear deployment containing some sub-deployments
        ResourceInstance.Builder applicationOneBuilder =
                serverOneBuilder.createChildBuilder(application, "application-one.ear");
        addDeployedObjects(applicationOneBuilder, jvmOneBuilder);

        //Build and serialize the root instance which will also do the same for the children
        ResourceInstance domainMain = domainMainBuilder.build();
        domainMain.serialize();
    }

    private void addDeployedObjects(ResourceInstance.Builder parentBuilder, ResourceInstance.Builder jvmBuilder) throws IOException, URISyntaxException {
        parentBuilder.createManagedObjectChildBuilder(appClient, "app-client.jar", jvmBuilder);

        ResourceInstance.Builder webModuleOneBuilder =
                parentBuilder.createManagedObjectChildBuilder(webModule, "web-one.war", jvmBuilder);
        webModuleOneBuilder.createChildBuilder(servlet, "MyServlet");
        webModuleOneBuilder.createChildBuilder(servlet, "AnotherServlet");

        ResourceInstance.Builder ejbModuleBuilder =
                parentBuilder.createManagedObjectChildBuilder(ejbModule, "ejb-one.jar", jvmBuilder);
        ejbModuleBuilder.createChildBuilder(entityBean, "MyEntityBean");
        ejbModuleBuilder.createChildBuilder(entityBean, "AnotherEntityBean");
        ejbModuleBuilder.createChildBuilder(messageDrivenBean, "MyMessageDrivenBean");
        ejbModuleBuilder.createChildBuilder(messageDrivenBean, "AnotherMessageDrivenBean");
        ejbModuleBuilder.createChildBuilder(statefulSessionBean, "MyStatefulSessionBean");
        ejbModuleBuilder.createChildBuilder(statefulSessionBean, "AnotherStatefulSessionBean");
        ejbModuleBuilder.createChildBuilder(statelessSessionBean, "MyStatelessSessionBean");
        ejbModuleBuilder.createChildBuilder(statelessSessionBean, "AnotherStatelessSessionBean");
    }
}
