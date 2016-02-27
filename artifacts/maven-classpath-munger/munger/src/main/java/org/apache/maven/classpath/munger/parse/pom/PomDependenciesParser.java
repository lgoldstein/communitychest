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

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.maven.classpath.munger.Dependency;
import org.apache.maven.classpath.munger.Repository;
import org.apache.maven.classpath.munger.logging.Log;
import org.apache.maven.classpath.munger.parse.MavenDependency;
import org.apache.maven.classpath.munger.util.IgnoringSAXHandler;
import org.apache.maven.classpath.munger.util.properties.PropertiesUtil;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * @author Lyor G.
 * @since Dec 24, 2013 12:46:24 PM
 */
class PomDependenciesParser extends IgnoringSAXHandler {
    private enum HuntState {
        START, PROPERTIES, REPOS, DEPENDENCIES;
        
//        static final Set<HuntState> ALL=Collections.unmodifiableSet(EnumSet.allOf(HuntState.class));
    }

    private static final String PROP_VALUE_PLACEHOLDER="<placeholder>";

    private final Map<String,String>  properties=new TreeMap<String,String>();
    private final List<Repository>    repos=new ArrayList<Repository>();
    private final List<Dependency>    dependencies=new ArrayList<Dependency>();
    private HuntState   huntState;
    private MavenDependency  curDependency;
    private boolean exclusionsApplied;
    private Repository curRepo;
    private Set<HuntState>  processedStates=EnumSet.noneOf(HuntState.class);
    private final StringBuilder sb=new StringBuilder(Byte.MAX_VALUE);

    PomDependenciesParser(Log log) {
        super(log);
    }

    Map<String,String> getProperties() {
        return properties;
    }

    List<Dependency> getDependencies() {
        return dependencies;
    }

    List<Repository> getRepositories() {
        return repos;
    }

    @Override
    public void startDocument() throws SAXException {
        if (huntState != null) {
            throw new SAXException("Duplicate document start");
        }
        
        huntState = HuntState.START;
        processedStates.add(huntState);
    }

    @Override
    public void endDocument() throws SAXException {
        if (huntState == null) {
            throw new SAXException("No document start signalled on document end");
        }
        
        huntState = null;
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        if (huntState == null) {
            throw new SAXException("startElement(" + uri + ")[" + localName + "][" + qName + "] no document start signalled");
        }

        if (sb.length() > 0) {
            sb.setLength(0);
        }

        switch(huntState) {
            case START      :
                if ("properties".equalsIgnoreCase(localName)) {
                    huntState = HuntState.PROPERTIES;
                } else if ("dependencies".equalsIgnoreCase(localName)) {
                    huntState = HuntState.DEPENDENCIES;
                } else if ("repositories".equalsIgnoreCase(localName)) {
                    huntState = HuntState.REPOS;
                }
                break;

            case PROPERTIES     : {
                    String  prev=properties.put(localName, PROP_VALUE_PLACEHOLDER);
                    if (prev != null) {
                        throw new SAXException("startElement(" + uri + ")[" + localName + "][" + qName + "] multiple values for property");
                    }
                }
                break;

            case DEPENDENCIES   :
                if ("dependency".equalsIgnoreCase(localName)) {
                    curDependency = new MavenDependency();
                } else if ("exclusions".equalsIgnoreCase(localName)) {
                    exclusionsApplied = true;
                }
                break;

            case REPOS          :
                if ("repository".equalsIgnoreCase(localName)) {
                    curRepo = new Repository();
                }
                break;

            default             :
                throw new SAXException("startElement(" + uri + ")[" + localName + "][" + qName + "] unknown state: " + huntState);
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (huntState == null) {
            throw new SAXException("endElement(" + uri + ")[" + localName + "][" + qName + "] no document start signalled");
        }

        switch(huntState) {
            case START  :   // do nothing
                break;

            case PROPERTIES     :
                updateProperty(uri, localName, qName);
                break;

            case DEPENDENCIES   :
                updateDependency(uri, localName, qName);
                break;

            case REPOS          :
                updateRepository(uri, localName, qName);
                break;

            default             :
                throw new SAXException("endElement(" + uri + ")[" + localName + "][" + qName + "] unknown state: " + huntState);
        }
        
        // clear any accumulated data at end of element
        if (sb.length() > 0) {
            sb.setLength(0);
        }
    }

    void updateProperty(String uri, String localName, String qName) throws SAXException {
        if ("properties".equalsIgnoreCase(localName)) { // check if end of properties signalled
            processedStates.add(huntState);
            huntState = HuntState.START;
        } else {
            String  value=sb.toString().trim();
            String  prev=properties.put(localName, value);
            if (prev != PROP_VALUE_PLACEHOLDER) {
                throw new SAXException("updateProperty(" + uri + ")[" + localName + "][" + qName + "] multiple values for property");
            }
            
            if (logger.isDebugEnabled()) {
                logger.debug(huntState + "[" + localName + "]: " + value);
            }
        }
    }

    void updateRepository(String uri, String localName, String qName) throws SAXException {
        if ("repositories".equalsIgnoreCase(localName)) {
            processedStates.add(huntState);
            huntState = HuntState.START;
            return;
        }
        
        if (curRepo == null) {
            throw new SAXException("updateRepository(" + uri + ")[" + localName + "][" + qName + "] no current repo");
        }

        if ("repository".equalsIgnoreCase(localName)) {
            if (PropertiesUtil.isEmpty(curRepo.getUrl())) {
                throw new SAXException("updateRepository(" + uri + ")[" + localName + "][" + qName + "] incomplete current repository: " + curRepo);

            }
            
            if (PropertiesUtil.isEmpty(curRepo.getId())) {
                curRepo.setId(curRepo.getUrl());
            }
            
            repos.add(curRepo);
            if (logger.isDebugEnabled()) {
                logger.debug(huntState +  ": " + curRepo);
            }

            curRepo = null;
        } else if ("id".equalsIgnoreCase(localName)) {
            curRepo.setId(sb.toString().trim());
        } else if ("url".equalsIgnoreCase(localName)) {
            curRepo.setUrl(sb.toString().trim());
        }
    }

    void updateDependency(String uri, String localName, String qName) throws SAXException {
        if ("dependencies".equalsIgnoreCase(localName)) {
            processedStates.add(huntState);
            huntState = HuntState.START;
            return;
        }

        if (curDependency == null) {
            throw new SAXException("updateDependency(" + uri + ")[" + localName + "][" + qName + "] no current dependency");
        }

        if ("exclusions".equalsIgnoreCase(localName)) {
            exclusionsApplied = false;
            return;
        }

        if (exclusionsApplied) {
            return;
        }

        if ("dependency".equalsIgnoreCase(localName)) {
            if (PropertiesUtil.isEmpty(curDependency.getGroupId())
             || PropertiesUtil.isEmpty(curDependency.getArtifactId())
             || PropertiesUtil.isEmpty(curDependency.getVersion())) {
                throw new SAXException("updateDependency(" + uri + ")[" + localName + "][" + qName + "] incomplete current dependency: " + curDependency);
            }
            
            String  scope=curDependency.getScope();
            if ("system".equalsIgnoreCase(scope)
             || "test".equalsIgnoreCase(scope)
             || "provided".equalsIgnoreCase(scope)) {
                if (logger.isDebugEnabled()) {
                    logger.debug(huntState +  " skip (" + scope + "): " + curDependency);
                }
            } else {
                dependencies.add(curDependency);
                if (logger.isDebugEnabled()) {
                    logger.debug(huntState +  ": " + curDependency);
                }
            }

            curDependency = null;
        } else if ("groupId".equalsIgnoreCase(localName)) {
            curDependency.setGroupId(sb.toString().trim());
        } else if ("artifactId".equalsIgnoreCase(localName)) {
            curDependency.setArtifactId(sb.toString().trim());
        } else if ("version".equalsIgnoreCase(localName)) {
            curDependency.setVersion(sb.toString().trim());
        } else if ("type".equalsIgnoreCase(localName)) {
            curDependency.setPackaging(sb.toString().trim());
        } else if ("scope".equalsIgnoreCase(localName)) {
            curDependency.setScope(sb.toString().trim());
        }
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        if (huntState == null) {
            throw new SAXException("characters(" + new String(ch, start, length) + ") no document start signalled");
        }

        if (HuntState.START.equals(huntState)) {
            if (sb.length() > 0) {
                sb.setLength(0);
            }
        } else {
            if (length > 0) {
                sb.append(ch, start, length);
            }
        }
    }
}
