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

import java.io.Serializable;
import java.util.Comparator;

import org.apache.maven.classpath.munger.util.properties.PropertiesUtil;

/**
 * @author Lyor G.
 * @since Dec 24, 2013 12:25:06 PM
 * TODO add support for classifier
 */
public class Dependency implements Cloneable, Serializable, Comparable<Dependency> {
    private static final long serialVersionUID = 8895161455819250877L;

    public static final Comparator<Dependency>  BY_FULL_INFO_COMPARATOR=
            new Comparator<Dependency>() {
                @Override
                public int compare(Dependency d1, Dependency d2) {
                    int nRes=0;
                    
                    if (d1 == d2) {
                        return 0;
                    }

                    {
                        String  g1=(d1 == null) ? null : d1.getGroupId();
                        String  g2=(d2 == null) ? null : d2.getGroupId();
                        if ((nRes=PropertiesUtil.safeCompare(g1, g2, false)) != 0) {
                            return nRes;
                        }
                    }

                    {
                        String  a1=(d1 == null) ? null : d1.getArtifactId();
                        String  a2=(d2 == null) ? null : d2.getArtifactId();
                        if ((nRes=PropertiesUtil.safeCompare(a1, a2, false)) != 0) {
                            return nRes;
                        }
                    }

                    {
                        String  v1=(d1 == null) ? null : d1.getVersion();
                        String  v2=(d2 == null) ? null : d2.getVersion();
                        if ((nRes=PropertiesUtil.safeCompare(v1, v2, false)) != 0) {
                            return nRes;
                        }
                    }

                    {
                        String  p1=(d1 == null) ? null : d1.getPackaging();
                        String  p2=(d2 == null) ? null : d2.getPackaging();
                        if ((nRes=PropertiesUtil.safeCompare(p1, p2, false)) != 0) {
                            return nRes;
                        }
                    }

                    return 0;
                }
            };
    public static final String DEFAULT_PACKAGING="jar";
    private String  groupId, artifactId, version, packaging;

    public Dependency() {
        super();
    }

    public Dependency(String group, String name, String ver) {
        this(group, name, DEFAULT_PACKAGING, ver);
    }

    public Dependency(String group, String name, String pkg, String ver) {
        groupId = group;
        artifactId = name;
        packaging = pkg;
        version = ver;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String value) {
        groupId = value;
    }

    public String getArtifactId() {
        return artifactId;
    }

    public void setArtifactId(String value) {
        artifactId = value;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String value) {
        version = value;
    }

    public String getPackaging() {
        return packaging;
    }

    public void setPackaging(String value) {
        packaging = value;
    }

    @Override
    public int compareTo(Dependency o) {
        return BY_FULL_INFO_COMPARATOR.compare(this, o);
    }

    @Override
    public int hashCode() {
        return PropertiesUtil.hashCode(getGroupId(), Boolean.FALSE)
             + PropertiesUtil.hashCode(getArtifactId(), Boolean.FALSE)
             + PropertiesUtil.hashCode(getVersion(), Boolean.FALSE)
             + PropertiesUtil.hashCode(getPackaging(), Boolean.FALSE)
             ;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (this == obj)
            return true;
        if (getClass() != obj.getClass())
            return false;
        
        if (compareTo((Dependency) obj) == 0) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public Dependency clone() throws CloneNotSupportedException {
        try {
            return getClass().cast(super.clone());
        } catch(CloneNotSupportedException e) {
            throw new RuntimeException("Failed to clone " + toString() + ": " + e.getMessage(), e);
        }
    }

    @Override
    public String toString() {
        return getGroupId()
             + ":" + getArtifactId()
             + ":" + getPackaging()
             + ":" + getVersion()
             ;
    }
    
    public String buildArtifactPath(char separatorChar) {
        return buildArtifactPath(separatorChar, "");
    }

    public String buildArtifactPath(char separatorChar, String suffix) {
        String  gid=getGroupId(), aid=getArtifactId(), p=getPackaging(), v=getVersion();
        if (PropertiesUtil.isEmpty(gid)
         || PropertiesUtil.isEmpty(aid)
         || PropertiesUtil.isEmpty(p)
         || PropertiesUtil.isEmpty(v)) {
            throw new IllegalStateException("Incomplete specification: " + toString());
        }
        
        StringBuilder   sb=new StringBuilder(gid.length() + 1   /* separator */
                                           + aid.length() + 1   /* separator */
                                           + v.length() + 1     /* separator */
                                           + aid.length() + 1 + v.length()
                                           + 1 + p.length()
                                           + PropertiesUtil.getSafeLength(suffix));
        PropertiesUtil.replaceChar(sb.append(gid), '.', separatorChar);
        sb.append(separatorChar).append(aid);
        sb.append(separatorChar).append(v);
        sb.append(separatorChar).append(aid).append('-').append(v);
        sb.append('.').append(p);
        
        if (!PropertiesUtil.isEmpty(suffix)) {
            sb.append(suffix);
        }
        
        return sb.toString();
    }
}
