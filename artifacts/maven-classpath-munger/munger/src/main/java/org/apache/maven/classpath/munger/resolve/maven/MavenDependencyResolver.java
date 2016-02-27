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

package org.apache.maven.classpath.munger.resolve.maven;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StreamCorruptedException;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.maven.classpath.munger.Dependency;
import org.apache.maven.classpath.munger.Repository;
import org.apache.maven.classpath.munger.logging.Log;
import org.apache.maven.classpath.munger.resolve.AbstractDependencyResolver;
import org.apache.maven.classpath.munger.util.HttpUtil;
import org.apache.maven.classpath.munger.util.UrlUtil;
import org.apache.maven.classpath.munger.util.properties.PropertiesUtil;
import org.apache.maven.classpath.munger.util.properties.PropertySource;

/**
 * @author Lyor G.
 * @since Dec 25, 2013 9:19:06 AM
 */
public class MavenDependencyResolver extends AbstractDependencyResolver {
    public static final String  DEFAULT_REPO_URL_PROP="classpath.munger.default.maven.repo";
        public static final String DEFAULT_REPO_URL_VALUE="http://repo1.maven.org/maven2";
    public static final String  LOCAL_REPO_OVERRIDE_PROP="classpath.munger.local.maven.repo";

    public MavenDependencyResolver() {
        super();
    }

    public MavenDependencyResolver(Log log) {
        super(log);
    }

    @Override
    public Map<Dependency,URL> resolveDependencies(PropertySource                   processProps,
                                                   Collection<? extends Repository> repositories,
                                                   Collection<? extends Dependency> dependencies)
               throws IOException {
        Collection<? extends Repository>    repos=repositories;
        // TODO use a default repository only if allowed to ...
        if (repos.isEmpty()) {
            String  defaultRepo=
                    PropertiesUtil.format(processProps.getProperty(DEFAULT_REPO_URL_PROP, DEFAULT_REPO_URL_VALUE), processProps);
            repos = Collections.singletonList(new Repository(DEFAULT_REPO_URL_PROP, defaultRepo));
            logger.warn("No repositories specified - using default=" + defaultRepo);
        }

        // make sure the local file separator is used regardless
        String      localRepoRoot=resolveMavenLocalRepository(processProps).replace('/', File.separatorChar);
        // if path ends in separator char then remove it
        if (localRepoRoot.charAt(localRepoRoot.length() - 1) == File.separatorChar) {
            localRepoRoot = localRepoRoot.substring(0, localRepoRoot.length() - 1);
        }

        Map<Dependency,URL>  urls=new LinkedHashMap<>(dependencies.size());
        for (Dependency d : dependencies) {
            File        artifact=resolveDependency(localRepoRoot, d, repos);
            URL         url=UrlUtil.toURL(artifact), prev=urls.put(d, url);
            if (prev != null) {
                throw new StreamCorruptedException("resolveDependencies(" + d + ")"
                                                 + " multiple URL(s): prev=" + prev.toExternalForm()
                                                 + " ,current=" + url.toExternalForm());
            }
        }

        return urls;
    }
    
    protected File resolveDependency(String localRepoRoot, Dependency d, Collection<? extends Repository> repos)
            throws IOException {
        File    targetPath=new File(localRepoRoot + File.separator + d.buildArtifactPath(File.separatorChar));
        if (targetPath.exists()) {
            if (logger.isDebugEnabled()) {
                logger.debug("resolveDependency(" + d + ") skip existing: " + targetPath.getAbsolutePath());
            }
            return targetPath;
        }
        
        String  downloadPath=d.buildArtifactPath('/');
        for (Repository r : repos) {
            cleanResidualFile(d, targetPath);
            try {
                long    startTime=System.currentTimeMillis();
                long    dataSize=HttpUtil.downloadDataStream(UrlUtil.concat(r.getUrl(), downloadPath), targetPath);
                long    endTime=System.currentTimeMillis();
                if (dataSize <= 0L) {
                    throw new StreamCorruptedException("No data downloaded");
                }
                
                logger.info("resolveDependency(" + d + ")[" + r + "]"
                          + " downloaded " + dataSize + " bytes"
                          + " in " + (endTime - startTime) + " msec.: " + targetPath);
                return targetPath;
            } catch(IOException e) {
                logger.warn("resolveDependency(" + d + ")[" + r + "]"
                        + " failed (" + e.getClass().getSimpleName() + ")"
                        + " to download: " + e.getMessage());
                cleanResidualFile(d, targetPath);
            }
        }
        
        IOException e=new FileNotFoundException("resolveDependency(" + d + ") no instance found");
        logger.error(e.getMessage());
        throw e;
    }

    protected boolean cleanResidualFile(Dependency d, File targetPath) throws IOException {
        if (!targetPath.exists()) {
            return false;
        }

        if (targetPath.delete()) {
            if (logger.isDebugEnabled()) {
                logger.debug("cleanResidualFile(" + d + ") clean up residual file: " + targetPath.getAbsolutePath());
            }
            
            return true;
        } else {
            IOException e=
                    new StreamCorruptedException("cleanResidualFile(" + d + ") failed to delete residual file: " + targetPath.getAbsolutePath());
            logger.error(e.getMessage());
            throw e;
        }
    }

    protected String resolveMavenLocalRepository(PropertySource processProps) {
        String  repoRoot=
                PropertiesUtil.format(processProps.getProperty(LOCAL_REPO_OVERRIDE_PROP), processProps);
        if (repoRoot != null) {
            logger.info("Using specific local repository: " + repoRoot);
            return repoRoot;
        }

        if ((repoRoot=processProps.getProperty("M2_REPO")) != null) {
            logger.info("Using environment variable location: " + repoRoot);
            return repoRoot;
        }

        repoRoot = processProps.getProperty("user.home") + File.separator + ".m2" + File.separator + "repository";
        logger.warn("Using local user location: " + repoRoot);
        return repoRoot;
    }
}
