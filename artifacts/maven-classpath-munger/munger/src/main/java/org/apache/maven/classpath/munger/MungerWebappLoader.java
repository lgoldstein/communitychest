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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.catalina.Container;
import org.apache.catalina.Context;
import org.apache.catalina.loader.VirtualWebappLoader;
import org.apache.maven.classpath.munger.logging.AbstractLog;
import org.apache.maven.classpath.munger.logging.Log;
import org.apache.maven.classpath.munger.util.HttpUtil;
import org.apache.maven.classpath.munger.util.UrlUtil;
import org.apache.maven.classpath.munger.util.properties.AggregateNamedPropertySource;
import org.apache.maven.classpath.munger.util.properties.NamedPropertySource;
import org.apache.maven.classpath.munger.util.properties.PropertiesUtil;

/**
 * @author Lyor G.
 * @since Jan 1, 2014 10:58:03 AM
 */
public class MungerWebappLoader extends VirtualWebappLoader {
    private static final org.apache.juli.logging.Log log=
            org.apache.juli.logging.LogFactory.getLog(MungerWebappLoader.class);
    public static final List<String>    DEFAULT_CONFIG_FILE_NAMES=
            Collections.unmodifiableList(
                    Arrays.asList("pom.xml", "dependencies-list.log"));

    public MungerWebappLoader() {
        super();
    }

    public MungerWebappLoader(ClassLoader parent) {
        super(parent);
    }

    @Override
    public void setContainer(Container container) {
        super.setContainer(container);
        
        if (container == null) {
            return;
        }

        if (container instanceof Context) {
            setContext((Context) container);
        } else {
            log.warn("setContainer(" + container.getName() + "): " + container.getInfo() + " no context");
        }
    }

    // TODO override setVirtualClasspath method and interpret it as the location of the dependencies file
    protected void setContext(Context context) {
        URL     configFile=context.getConfigFile();
        String  urlValue=UrlUtil.toString(configFile);
        if (PropertiesUtil.isEmpty(urlValue)) {
            log.warn("setContext(" + context.getBaseName() + ")[" + context.getDocBase() + "]: no configuration location");
            return;
        }

        log.info("setContext(" + context.getBaseName() + ")[" + context.getDocBase() + "]: " + urlValue);
        
        int     lastPos=urlValue.lastIndexOf('/');
        // TODO allow for non-Maven locations
        String  baseUrl=UrlUtil.adjustURLPathValue(urlValue.substring(0, lastPos)) + "/maven";
        if ((configFile=resolveDependenciesFileLocation(baseUrl)) == null) {
            log.warn("setContext(" + context.getBaseName() + ")[" + context.getDocBase() + "]: no dependencies specification");
            return;
        }
        
        try {
            // TODO allow for non-Maven locations
            addContextDependencies(context, configFile, new URL(UrlUtil.concat(baseUrl, "/repository")));
        } catch(Exception e) {
            log.error("setContext(" + context.getBaseName() + ")[" + context.getDocBase() + "]"
                    + " failed (" + e.getClass().getSimpleName() + ")"
                    + " to add dependecies: " + e.getMessage(),
                     e);
            throw (e instanceof RuntimeException) ? (RuntimeException) e : new RuntimeException(e);
        }
    }
    
    protected void addContextDependencies(final Context context, final URL configFile, final URL signaturesBase) throws Exception {
        NamedPropertySource  processProps=
                new AggregateNamedPropertySource(
// TODO                 PropertiesUtil.asPropertySource(context),
                        NamedPropertySource.SYSPROP,
                        NamedPropertySource.SYSENV);
        final StringBuilder  sb=new StringBuilder(HttpUtil.DEFAULT_BUFFER_SIZE);
        AbstractMunger  munger=new AbstractMunger(wrapJuliLogger(log)) {
                @Override
                protected void addClasspathURLs(Collection<? extends URL> urls) throws Exception {
                    if ((urls == null) || urls.isEmpty()) {
                        if (logger.isDebugEnabled()) {
                            logger.debug("addContextDependencies(" + context.getBaseName() + ")[" + context.getDocBase() + "]"
                                       + " no URL(s) from " + configFile.toExternalForm());
                        }
                        return;
                    }
                    
                    for (URL url : urls) {
                        URI uri=url.toURI();
                        if (!"file".equalsIgnoreCase(uri.getScheme())) {
                            throw new UnsupportedOperationException("Non file URL: " + uri.toString());
                        }
    
                        File    file=new File(uri);
                        String  location=file.getAbsolutePath();
                        if (sb.length() > 0) {
                            sb.append(';');
                        }
                        
                        sb.append(location);
    
                        if (logger.isTraceEnabled()) {
                            logger.trace("addContextDependencies(" + context.getBaseName() + ")[" + context.getDocBase() + "] appended " + location);
                        }
                    }
                }
            };
        munger.processDependencies(configFile, signaturesBase, processProps);
        
        if (sb.length() > 0) {
            super.setVirtualClasspath(sb.toString());
        }
    }

    protected URL resolveDependenciesFileLocation(String baseLocation) {
        StringBuilder   sb=new StringBuilder(Byte.MAX_VALUE).append(baseLocation).append('/');
        final int       sbLen=sb.length();
        for (String configFileName : DEFAULT_CONFIG_FILE_NAMES) {
            String  urlCandidate=sb.append(configFileName).toString();
            try {
               URL          url=new URL(urlCandidate); 
               InputStream  input=url.openStream();
               if (input == null) {
                   throw new FileNotFoundException("No stream opened");
               }
               
               // TODO close it inside a special try-catch-loop to avoid confusion
               input.close();
               return url;
            } catch(IOException e) {
                if (log.isDebugEnabled()) {
                    log.debug("resolveDependenciesFileLocation(" + urlCandidate + ")"
                            + " failed (" + e.getClass().getSimpleName() + ")"
                            + " to locate: " + e.getMessage(),
                             e);
                }
            } finally {
                sb.setLength(sbLen);    // prepare for next iteration
            }
        }
        
        return null;
    }
    
    public static final Log wrapJuliLogger(final org.apache.juli.logging.Log logger) {
        return new AbstractLog() {
            @Override
            public boolean isDebugEnabled() {
                return logger.isDebugEnabled();
            }

            @Override
            public boolean isErrorEnabled() {
                return logger.isErrorEnabled();
            }

            @Override
            public boolean isFatalEnabled() {
                return logger.isFatalEnabled();
            }

            @Override
            public boolean isInfoEnabled() {
                return logger.isInfoEnabled();
            }

            @Override
            public boolean isTraceEnabled() {
                return logger.isTraceEnabled();
            }

            @Override
            public boolean isWarnEnabled() {
                return logger.isWarnEnabled();
            }

            @Override
            public void trace(Object message, Throwable t) {
                if (t == null) {
                    logger.trace(message);
                } else {
                    logger.trace(message, t);
                }
            }

            @Override
            public void debug(Object message, Throwable t) {
                if (t == null) {
                    logger.debug(message);
                } else {
                    logger.debug(message, t);
                }
            }

            @Override
            public void info(Object message, Throwable t) {
                if (t == null) {
                    logger.info(message);
                } else {
                    logger.info(message, t);
                }
            }

            @Override
            public void warn(Object message, Throwable t) {
                if (t == null) {
                    logger.warn(message);
                } else {
                    logger.warn(message, t);
                }
            }

            @Override
            public void error(Object message, Throwable t) {
                if (t == null) {
                    logger.error(message);
                } else {
                    logger.error(message, t);
                }
            }

            @Override
            public void fatal(Object message, Throwable t) {
                if (t == null) {
                    logger.fatal(message);
                } else {
                    logger.fatal(message, t);
                }
            }
        };
    }
}
