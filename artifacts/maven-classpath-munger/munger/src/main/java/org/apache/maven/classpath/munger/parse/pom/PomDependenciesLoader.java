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

package org.apache.maven.classpath.munger.parse.pom;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.maven.classpath.munger.Dependency;
import org.apache.maven.classpath.munger.Repository;
import org.apache.maven.classpath.munger.logging.Log;
import org.apache.maven.classpath.munger.parse.AbstractDependeciesLoader;
import org.apache.maven.classpath.munger.util.properties.NamedPropertySource;
import org.apache.maven.classpath.munger.util.properties.PropertiesUtil;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * @author Lyor G.
 * @since Dec 24, 2013 12:44:08 PM
 */
public class PomDependenciesLoader extends AbstractDependeciesLoader {
    private NamedPropertySource properties;
    private List<Dependency>    dependencies;
    private List<Repository>    repos;
    private Set<String>         propsNames;

    public PomDependenciesLoader() {
        super();
    }

    public PomDependenciesLoader(Log log) {
        super(log);
    }

    public PomDependenciesLoader(InputStream inputStream) throws IOException {
        load(inputStream);
    }

    @Override
    public NamedPropertySource getProperties() {
        return properties;
    }

    @Override
    public Set<String> getDefinedPropertiesNames() {
        return propsNames;
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
        InputSource             source=new InputSource(inputStream);
        PomDependenciesParser   parser=new PomDependenciesParser(logger);   
        SAXParserFactory        factory=SAXParserFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setValidating(false);

        try {
            SAXParser   saxParser=factory.newSAXParser();
            saxParser.parse(source, parser);
        } catch(SAXException e) {
            logger.error("Failed (" + e.getClass().getSimpleName() + ") to load: " + e.getMessage(), e);
            throw new IOException(e);
        } catch(ParserConfigurationException e) {
            logger.error("Failed (" + e.getClass().getSimpleName() + ") to instantiate parser: " + e.getMessage(), e);
            throw new IOException(e);
        }
        
        Map<String,String>  propsMap=parser.getProperties();
        propsNames = Collections.unmodifiableSet(new TreeSet<String>(propsMap.keySet()));
        properties = PropertiesUtil.asPropertySource(propsMap);
        dependencies = Collections.unmodifiableList(parser.getDependencies());
        repos = Collections.unmodifiableList(parser.getRepositories());
    }

}
