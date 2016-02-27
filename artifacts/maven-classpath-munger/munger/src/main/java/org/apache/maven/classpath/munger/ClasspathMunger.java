/*
 * Copyright 2013 Lyor Goldstein
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.maven.classpath.munger;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.logging.Level;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.maven.classpath.munger.logging.AbstractJULWrapper;
import org.apache.maven.classpath.munger.logging.Log;
import org.apache.maven.classpath.munger.util.HttpUtil;
import org.apache.maven.classpath.munger.util.UrlUtil;
import org.apache.maven.classpath.munger.util.properties.AggregateNamedPropertySource;
import org.apache.maven.classpath.munger.util.properties.NamedPropertySource;
import org.apache.maven.classpath.munger.util.properties.PropertiesUtil;
import org.apache.maven.classpath.munger.util.properties.PropertySource;

/**
 * @author Lyor G.
 * @since Dec 24, 2013 4:18:41 PM
 */
public class ClasspathMunger extends AbstractMunger implements ServletContextListener {
    private final Collection<ServletContextListener>  extraListeners=new LinkedList<ServletContextListener>();

    public ClasspathMunger() {
        super();
    }
    
    // unit tests
    ClasspathMunger(Log log) {
        super(log);
    }

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        Log curLogger=logger;
        try {
            ServletContext  context=sce.getServletContext();
            logger = wrapServletContext(context, Level.CONFIG); // TODO allow configurable log level
            initializeContext(sce);
            logger.info("contextInitialized(" + context.getContextPath() + ")");
        } catch(Throwable t) {
            logger.error("Failed (" + t.getClass().getSimpleName() + ") to initialize: " + t.getMessage(), t);
            if (t instanceof RuntimeException) {
                throw (RuntimeException) t;
            } else {
                throw new RuntimeException(t);
            }
        } finally {
            logger = curLogger;
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        Log curLogger=logger;
        try {
            ServletContext  context=sce.getServletContext();
            logger = wrapServletContext(context, Level.CONFIG); // TODO allow configurable log level
            destroyContext(sce);
            logger.info("contextDestroyed(" + context.getContextPath() + ")");
        } catch(Throwable t) {
            logger.error("Failed (" + t.getClass().getSimpleName() + ") to initialize: " + t.getMessage(), t);
            if (t instanceof RuntimeException) {
                throw (RuntimeException) t;
            } else {
                throw new RuntimeException(t);
            }
        } finally {
            logger = curLogger;
        }
    }

    protected void initializeContext(ServletContextEvent sce) throws Exception {
        ServletContext      context=sce.getServletContext();
        NamedPropertySource processProps=
                new AggregateNamedPropertySource(
                        PropertiesUtil.asPropertySource(context),
                        NamedPropertySource.SYSPROP,
                        NamedPropertySource.SYSENV);
        /*
         * NOTE: we use 'WEB-INF/maven' instead of 'META-INF/maven' in order to avoid
         * conflicts with Maven built WAR(s) as well as since it is much easier to
         * place files in WEB-INF during the build process
         */
        String              dependencyDataLocation=
                PropertiesUtil.format(processProps.getProperty("classpath.munger.dependency.location", "/WEB-INF/maven/pom.xml"), processProps);
        URL                 dependenciesListLocation=resolveInputDataLocation(context, dependencyDataLocation);
        // if null then assume no dependencies to manage
        if (dependenciesListLocation != null) {
            String  signaturesDataLocation=
                    PropertiesUtil.format(processProps.getProperty("classpath.munger.signatures.location", "/WEB-INF/maven/repository"), processProps);
            processDependencies(dependenciesListLocation, resolveInputDataLocation(context, signaturesDataLocation), processProps);
        }
        
        invokeExtraContextListeners(sce, processProps);
    }

    protected void destroyContext(ServletContextEvent sce) throws Exception {
        if ((extraListeners == null) || extraListeners.isEmpty()) {
            return;
        }
        
        Exception   exc=null;
        for (ServletContextListener l : extraListeners) {
            try {
                l.contextDestroyed(sce);
            } catch(Exception e) {
                logger.error(e.getClass().getSimpleName() + " while invoke " + l.getClass().getSimpleName() + "#contextDestroyed: " + e.getMessage());
                exc = e;
            }
        }
        
        if (exc != null) {
            throw exc;
        }
    }

    Collection<? extends ServletContextListener> invokeExtraContextListeners(ServletContextEvent sce, PropertySource processProps) throws Exception {
        ServletContext  context=sce.getServletContext();
        String          resourceLocation=
                PropertiesUtil.format(
                        processProps.getProperty("classpath.munger.extra.listeners.definitions", "/WEB-INF/web-listeners.xml"), processProps);
        URL     listenersListLocation=resolveInputDataLocation(context, resourceLocation);
        if (listenersListLocation == null) {
            logger.info("No extra listeners to invoke");
            return Collections.emptyList();
        }
        
        Thread      thread=Thread.currentThread();
        ClassLoader cl=thread.getContextClassLoader();
        InputStream inputStream=listenersListLocation.openStream();
        try {
            BufferedReader  reader=new BufferedReader(new InputStreamReader(inputStream), HttpUtil.DEFAULT_BUFFER_SIZE);
            try {
                logger.info("invokeExtraContextListeners(" + context.getContextPath() + "): " + listenersListLocation.toExternalForm());

                for (String line=reader.readLine(); line != null; line=reader.readLine()) {
                    line = line.trim();
                    if (PropertiesUtil.isEmpty(line)) {
                        continue;
                    }
                    
                    if (!line.startsWith("<listener-class")) {
                        continue;
                    }
                    
                    int                     startPos=line.indexOf('>'), endPos=line.indexOf('<', startPos + 1);
                    String                  className=line.substring(startPos + 1, endPos).trim();
                    Class<?>                listenerClass=Class.forName(className, false, cl);
                    ServletContextListener  listener=(ServletContextListener) listenerClass.newInstance();
                    try {
                        listener.contextInitialized(sce);
                        if (logger.isDebugEnabled()) {
                            logger.debug("Invoked " + className + "#contextInitialized(" + context.getContextPath() + ")");
                        }
                    } catch(Exception e) {
                        logger.error(e.getClass().getSimpleName() + " on "
                                   + className + "#contextInitialized(" + context.getContextPath() + ")"
                                   + ": " + e.getMessage(), e);
                        throw e;
                    }
                    extraListeners.add(listener);
                }
            } finally {
                reader.close();
            }
        } finally {
            inputStream.close();
        }

        return extraListeners;
    }

    @Override
    protected void addClasspathURLs(Collection<? extends URL> urls) throws Exception {
        Thread      thread=Thread.currentThread();
        ClassLoader cl=thread.getContextClassLoader();
        if (!(cl instanceof URLClassLoader)) {
            RuntimeException    e=
                    new UnsupportedOperationException("addClasspathURLs - loader (" + cl.getClass().getSimpleName() + ")"
                                                    + " is not a " + URLClassLoader.class.getSimpleName() + ": " + cl);
            logger.error(e.getMessage());
            throw e;
        }
        
        addClasspathURLs((URLClassLoader) cl, urls);
    }
    
    URLClassLoader addClasspathURLs(URLClassLoader cl, Collection<? extends URL> urls) throws ReflectiveOperationException {
        Method  m=URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
        if (!m.isAccessible()) {
            m.setAccessible(true);
        }

        for (URL url : urls) {
            m.invoke(cl, url);
            
            if (logger.isDebugEnabled()) {
                logger.debug("addClasspathURLs - added " + url.toExternalForm());
            }
        }
        
        return cl;
    }

    URL resolveInputDataLocation(ServletContext context, String resourceLocation) throws IOException {
        if (resourceLocation.indexOf(":/") > 0) { // check if a URL
            if (resourceLocation.startsWith("file:/")) {
                int     startPos=resourceLocation.indexOf('/');
                String  filePath=resourceLocation.substring(startPos + 1).replace('/', File.separatorChar);
                return UrlUtil.toURL(new File(filePath));
            } else {    // TODO add support http(s)
                throw new MalformedURLException("Unsupported URL format: " + resourceLocation);
            }
        } else if (resourceLocation.startsWith("http://") || resourceLocation.startsWith("https://")) {
            return new URL(resourceLocation);
        } else {
            return context.getResource(resourceLocation);
        }
    }

    public static final Log wrapServletContext(final ServletContext context, final Level thresholdLevel) {
        if ((context == null) || (thresholdLevel == null)) {
            throw new IllegalArgumentException("Incomplete wrapper specification");
        }
        
        return new AbstractJULWrapper() {
            @Override
            public void log(Level level, Object message, Throwable t) {
                if (isEnabled(level)) {
                    if (t == null) {
                        context.log(level.getName() + ": " + message);
                    } else {
                        context.log(level.getName() + ": " + message, t);
                    }
                }
            }
    
            @Override
            public boolean isEnabled(Level level) {
                if (Level.OFF.equals(thresholdLevel)) {
                    return false;
                }
                
                if (Level.ALL.equals(thresholdLevel)) {
                    return true;
                }
                
                if (level.intValue() >= thresholdLevel.intValue()) {
                    return true;
                } else {
                    return false;   // debug breakpoint
                }
            }
        };
    }
}
