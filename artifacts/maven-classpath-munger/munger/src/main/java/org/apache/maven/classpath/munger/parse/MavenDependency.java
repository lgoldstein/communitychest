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
package org.apache.maven.classpath.munger.parse;

import org.apache.maven.classpath.munger.Dependency;
import org.apache.maven.classpath.munger.util.properties.PropertiesUtil;

/**
 * @author Lyor G.
 * @since Dec 25, 2013 3:28:15 PM
 */
public class MavenDependency extends Dependency {   //for unit tests
    private static final long serialVersionUID = 5325229275697082461L;
    private String  scope;
    
    public MavenDependency() {
        super();
    }

    public MavenDependency(String groupId, String artifactId, String version) {
        this(groupId, artifactId, DEFAULT_PACKAGING, version);
    }

    public MavenDependency(String groupId, String artifactId, String pkg, String version) {
        this(groupId, artifactId, pkg, version, null);
    }

    public MavenDependency(String groupId, String artifactId, String pkg, String version, String scopeValue) {
        super(groupId, artifactId, pkg, version);
        scope = scopeValue;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String value) {
        scope = value;
    }

    @Override
    public int hashCode() {
        return super.hashCode()
             + PropertiesUtil.hashCode(getScope(), Boolean.FALSE)
             ;
    }

    @Override
    public boolean equals(Object obj) {
        if (!super.equals(obj)) {
            return false;
        }
        
        MavenDependency other=(MavenDependency) obj;
        if (PropertiesUtil.safeCompare(getScope(), other.getScope(), false) == 0) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public String toString() {
        String  base=super.toString(), s=getScope();
        if (PropertiesUtil.isEmpty(scope)) {
            return base;
        } else {
            return base + ":" + s;
        }
    }
}