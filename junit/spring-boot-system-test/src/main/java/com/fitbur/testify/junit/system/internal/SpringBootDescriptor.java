/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.fitbur.testify.junit.system.internal;

import com.fitbur.testify.TestContext;
import com.fitbur.testify.TestNeedContainers;
import com.fitbur.testify.TestNeeds;
import com.fitbur.testify.di.ServiceAnnotations;
import com.fitbur.testify.di.spring.SpringServiceLocator;
import javax.servlet.ServletContext;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.embedded.AnnotationConfigEmbeddedWebApplicationContext;
import org.springframework.boot.context.embedded.ConfigurableEmbeddedServletContainer;
import org.springframework.boot.context.embedded.EmbeddedServletContainer;
import org.springframework.boot.context.embedded.EmbeddedServletContainerFactory;

/**
 *
 * @author saden
 */
public class SpringBootDescriptor {

    private final AnnotationConfigEmbeddedWebApplicationContext context;

    private SpringApplication application;
    private Object[] sources;
    private String[] args;
    private EmbeddedServletContainerFactory containerFactory;
    private ConfigurableEmbeddedServletContainer configurableServletContainer;
    private EmbeddedServletContainer servletContainer;
    private TestNeeds testNeeds;
    private TestNeedContainers testContainerNeeds;
    private SpringServiceLocator serviceLocator;
    private ServiceAnnotations serviceAnnotations;
    private ServletContext servletContext;
    private TestContext testContext;

    public SpringBootDescriptor(AnnotationConfigEmbeddedWebApplicationContext context) {
        this.context = context;
    }

    public AnnotationConfigEmbeddedWebApplicationContext getApplicationContext() {
        return context;
    }

    public AnnotationConfigEmbeddedWebApplicationContext getContext() {
        return context;
    }

    public SpringApplication getApplication() {
        return application;
    }

    void setApplication(SpringApplication application) {
        this.application = application;
    }

    public Object[] getSources() {
        return sources;
    }

    void setSources(Object... sources) {
        this.sources = sources;
    }

    public String[] getArgs() {
        return args;
    }

    void setArgs(String... args) {
        this.args = args;
    }

    public EmbeddedServletContainerFactory getContainerFactory() {
        return containerFactory;
    }

    void setContainerFactory(EmbeddedServletContainerFactory containerFactory) {
        this.containerFactory = containerFactory;
    }

    public ConfigurableEmbeddedServletContainer getConfigurableServletContainer() {
        return configurableServletContainer;
    }

    void setConfigurableServletContainer(ConfigurableEmbeddedServletContainer configurableServletContainer) {
        this.configurableServletContainer = configurableServletContainer;
    }

    public EmbeddedServletContainer getServletContainer() {
        return servletContainer;
    }

    void setServletContainer(EmbeddedServletContainer servletContainer) {
        this.servletContainer = servletContainer;
    }

    public TestNeeds getTestNeeds() {
        return testNeeds;
    }

    void setTestNeeds(TestNeeds testNeeds) {
        this.testNeeds = testNeeds;
    }

    public TestNeedContainers getTestContainerNeeds() {
        return testContainerNeeds;
    }

    void setTestContainerNeeds(TestNeedContainers testContainerNeeds) {
        this.testContainerNeeds = testContainerNeeds;
    }

    public SpringServiceLocator getServiceLocator() {
        return serviceLocator;
    }

    void setServiceLocator(SpringServiceLocator serviceLocator) {
        this.serviceLocator = serviceLocator;
    }

    public ServiceAnnotations getServiceAnnotations() {
        return serviceAnnotations;
    }

    void setServiceAnnotations(ServiceAnnotations serviceAnnotations) {
        this.serviceAnnotations = serviceAnnotations;
    }

    public ServletContext getServletContext() {
        return servletContext;
    }

    void setServletContext(ServletContext servletContext) {
        this.servletContext = servletContext;
    }

    public TestContext getTestContext() {
        return testContext;
    }

    void setTestContext(TestContext testContext) {
        this.testContext = testContext;
    }

}
