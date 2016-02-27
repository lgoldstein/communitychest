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

import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.apache.maven.classpath.munger.logging.AbstractLoggingBean;
import org.apache.maven.classpath.munger.logging.Log;
import org.apache.maven.classpath.munger.parse.DependeciesLoader;
import org.apache.maven.classpath.munger.parse.log.LogDependenciesLoader;
import org.apache.maven.classpath.munger.parse.pom.PomDependenciesLoader;
import org.apache.maven.classpath.munger.resolve.DependencyResolver;
import org.apache.maven.classpath.munger.resolve.maven.MavenDependencyResolver;
import org.apache.maven.classpath.munger.util.properties.AggregateNamedPropertySource;
import org.apache.maven.classpath.munger.util.properties.NamedPropertySource;
import org.apache.maven.classpath.munger.util.properties.PropertiesUtil;
import org.apache.maven.classpath.munger.util.properties.PropertySource;
import org.apache.maven.classpath.munger.validation.ArtifactValidator;
import org.apache.maven.classpath.munger.validation.maven.MavenArtifactValidator;

/**
 * @author Lyor G.
 * @since Jan 1, 2014 12:27:05 PM
 */
public abstract class AbstractMunger extends AbstractLoggingBean {
    protected AbstractMunger() {
        this(null);
    }
    
    protected AbstractMunger(Log log) {
        super(log);
    }

    public void processDependencies(URL dependenciesDefinitionsLocation,
                                    URL signaturesDataLocation,
                                    NamedPropertySource propsSource) throws Exception {
        NamedPropertySource processProps=propsSource;
        DependeciesLoader   loader=resolveDependenciesLoader(dependenciesDefinitionsLocation, processProps);
        loader.load(dependenciesDefinitionsLocation);
        
        Collection<String>  extraNames=loader.getDefinedPropertiesNames();
        if ((extraNames != null) && (extraNames.size() > 0)) {
            processProps = new AggregateNamedPropertySource(loader.getProperties(), processProps);
        }

        processProps = resolveLoadingProperties(processProps);

        List<? extends Dependency>  dependencies=resolveDependencies(processProps, loader.getDependencies());
        if (dependencies.isEmpty()) {
            logger.info("No dependencies to resolve");
            return;
        }

        List<? extends Repository>  repos=resolveRepositories(processProps, loader.getRepositories());
        // TODO use another property to choose resolver - e.g., Gradle, Ivy
        DependencyResolver          resolver=new MavenDependencyResolver(logger);
        Map<Dependency,URL>         urls=resolver.resolveDependencies(processProps, repos, dependencies);
        validateDependencies(urls, signaturesDataLocation);
        // TODO add (configurable) support for signed JAR(s) validation
        addClasspathURLs(urls.values());
    }

    protected void validateDependencies(Map<Dependency,URL> urlsMap, URL signaturesDataLocation) throws IOException, SecurityException {
        // TODO use another property to choose validator - e.g., Gradle, Ivy
        ArtifactValidator   validator=new MavenArtifactValidator(logger, signaturesDataLocation);
        SecurityException   se=null;
        for (Map.Entry<Dependency,URL> dp : urlsMap.entrySet()) {
            Dependency  d=dp.getKey();
            URL         artifactData=dp.getValue();
            try {
                validator.validate(d, artifactData);
            } catch(Exception e) {
                logger.error("Failed (" + e.getClass().getSimpleName() + ")"
                           + " to validate dependency="
                           + " at location=" + artifactData.toExternalForm()
                           + ": " + e.getMessage());
                /*
                 * NOTE: we throw runtime and I/O exception(s) immediately but
                 * delay security ones in order to list all the invalid issues
                 * so they can be addressed at once instead of piecemeal
                 */
                if (e instanceof RuntimeException) {
                    throw (RuntimeException) e;
                } else if (e instanceof IOException) {
                    throw (IOException) e;
                } else if (e instanceof SecurityException) {
                    se = (SecurityException) e;
                } else {
                    throw new RuntimeException(e);
                }
            }
        }
        
        if (se != null) {
            throw se;
        }
    }

    protected abstract void addClasspathURLs(Collection<? extends URL> urls) throws Exception;

    protected DependeciesLoader resolveDependenciesLoader(URL dependenciesDefinitionsLocation, PropertySource processProps) {
        String dependencyDataLocation=dependenciesDefinitionsLocation.toExternalForm();
        // TODO use another property to decide format
        int     sepPos=dependencyDataLocation.lastIndexOf('.');
        if ((sepPos <= 0) || (sepPos >= (dependencyDataLocation.length() - 1))) {
            throw new IllegalArgumentException("No file location: " + dependencyDataLocation);
        }

        String  suffix=dependencyDataLocation.substring(sepPos + 1);
        if ("xml".equalsIgnoreCase(suffix)) {
            return new PomDependenciesLoader(logger);
        } else if ("log".equalsIgnoreCase(suffix)) {
            return new LogDependenciesLoader(logger);
        } else {
            throw new NoSuchElementException("No loader found for " + dependencyDataLocation);
        }
    }

    protected NamedPropertySource resolveLoadingProperties(NamedPropertySource processProps) {
        Map<String,String>  fmtValues=PropertiesUtil.resolvePropertiesValues(processProps);
        if (fmtValues.size() <= 0) {
            return processProps;
        }

        if (logger.isDebugEnabled()) {
            for (Map.Entry<String,String> fe : fmtValues.entrySet()) {
                String  name=fe.getKey();
                logger.debug("contextInitialized(" + name + ")"
                        + " formatted property value: old=" + processProps.getProperty(name)
                        + ",new=" + fe.getValue());
            }
        }
            
        // if have any formatted properties put them first so we won't have to re-format them
        return new AggregateNamedPropertySource(PropertiesUtil.asPropertySource(fmtValues), processProps);
    }

    protected List<? extends Dependency> resolveDependencies(PropertySource processProps, List<? extends Dependency> deps) {
        if ((deps == null) || deps.isEmpty()) {
            return Collections.emptyList();
        }
        
        for (Dependency d : deps) {
            String  orgVersion=d.getVersion(), fmtVersion=PropertiesUtil.format(orgVersion, processProps);
            if (orgVersion == fmtVersion) {
                continue;
            }
            
            if (logger.isDebugEnabled()) {
                logger.debug("resolveDependencies(" + d.getGroupId() + ":" + d.getArtifactId() + ")"
                           + " " + orgVersion + " => " + fmtVersion);
            }
            d.setVersion(fmtVersion);
        }
        
        return deps;
    }

    protected List<? extends Repository>  resolveRepositories(PropertySource processProps, List<? extends Repository> repos) {
        if ((repos == null) || repos.isEmpty()) {
            return Collections.emptyList();
        }
        
        for (Repository r : repos) {
            String  orgUrl=r.getUrl(), fmtUrl=PropertiesUtil.format(orgUrl, processProps);
            if (orgUrl == fmtUrl) {
                continue;
            }
            
            if (logger.isDebugEnabled()) {
                logger.debug("resolveRepositories(" + r.getId() + ") " + orgUrl + " => " + fmtUrl);
            }
            r.setUrl(fmtUrl);
        }
        
        return repos;
    }

}
