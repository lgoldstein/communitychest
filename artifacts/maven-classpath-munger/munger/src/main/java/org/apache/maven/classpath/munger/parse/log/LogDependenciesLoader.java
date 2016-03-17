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

package org.apache.maven.classpath.munger.parse.log;

import java.io.BufferedReader;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.maven.classpath.munger.Dependency;
import org.apache.maven.classpath.munger.Repository;
import org.apache.maven.classpath.munger.logging.Log;
import org.apache.maven.classpath.munger.parse.AbstractDependeciesLoader;
import org.apache.maven.classpath.munger.parse.MavenDependency;
import org.apache.maven.classpath.munger.util.HttpUtil;
import org.apache.maven.classpath.munger.util.properties.NamedPropertySource;
import org.apache.maven.classpath.munger.util.properties.PropertiesUtil;

/**
 * @author Lyor G.
 * @since Dec 25, 2013 3:10:56 PM
 */
public class LogDependenciesLoader extends AbstractDependeciesLoader {
    private final List<Dependency>  dependencies=new ArrayList<Dependency>();
    private final List<Repository>    repos=new ArrayList<Repository>();

    public LogDependenciesLoader() {
        super();
    }

    public LogDependenciesLoader(Log log) {
        super(log);
    }

    @Override
    public Collection<String> getDefinedPropertiesNames() {
        return Collections.emptyList();
    }

    @Override
    public NamedPropertySource getProperties() {
        return NamedPropertySource.EMPTY;
    }

    @Override
    public List<Dependency> getDependencies() {
        return dependencies;
    }

    @Override
    public List<Repository> getRepositories() {
        return repos;
    }

    @Override
    public void load(InputStream inputStream) throws IOException {
        BufferedReader  reader=new BufferedReader(new InputStreamReader(inputStream), HttpUtil.DEFAULT_BUFFER_SIZE);
        try {
            load(reader);
        } finally {
            reader.close();
        }
    }

    void load(BufferedReader reader) throws IOException {
        for (String line=reader.readLine(); line != null; line=reader.readLine()) {
            line = line.trim();
            if (PropertiesUtil.isEmpty(line)) {
                continue;
            }

            if (!line.startsWith("[INFO]")) {
                continue;
            }

            int startPos=line.indexOf(' ');
            if (startPos <= 0) {
                continue;
            }

            line = line.substring(startPos + 1).trim();
            if (PropertiesUtil.isEmpty(line)) {
                continue;
            }

            if (line.startsWith("id: ")) {
                Repository  r=updateRepository(line.substring(3).trim(), reader);
                if (r == null) {
                    continue;
                }

                if (logger.isDebugEnabled()) {
                    logger.debug("load(" + Repository.class.getSimpleName() + ") - " + r);
                }
                repos.add(r);
            } else if (line.contains(":compile") || line.contains(":runtime")) {
                Dependency  d=updateDependency(line);
                if (d == null) {
                    continue;
                }

                if (logger.isDebugEnabled()) {
                    logger.debug("load(" + Dependency.class.getSimpleName() + ") - " + d);
                }
                dependencies.add(d);
            }
        }
    }

    Dependency updateDependency(String line) {
        String[]    comps=line.split(":");
        // TODO add support for classifier, type, etc.
        return new MavenDependency(comps[0].trim(), comps[1].trim(), comps[2].trim(), comps[3].trim(), comps[4].trim());
    }

    Repository updateRepository(String id, BufferedReader reader) throws IOException {
        for (String line=reader.readLine(); line != null; line=reader.readLine()) {
            line = line.trim();
            if (PropertiesUtil.isEmpty(line)) {
                continue;
            }

            if (!line.startsWith("url:")) {
                continue;
            }

            return new Repository(id, line.substring(4).trim());
        }

        IOException e=new EOFException("updateRepository(" + id + ") Premature EOF while scan for URL");
        logger.error(e.getMessage());
        throw e;
    }
}
