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

import org.apache.maven.classpath.munger.util.properties.PropertiesUtil;

/**
 * @author Lyor G.
 * @since Dec 24, 2013 12:17:08 PM
 */
public class Repository implements Cloneable, Serializable {
    private static final long serialVersionUID = 1066745270147885645L;
    private String  id, url;

    public Repository() {
        super();
    }

    public Repository(String repoId, String repoUrl) {
        id = repoId;
        url = repoUrl;
    }

    public String getId() {
        return id;
    }

    public void setId(String value) {
        id = value;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String value) {
        url = value;
    }

    @Override
    public int hashCode() {
        return PropertiesUtil.hashCode(getId(), Boolean.FALSE)
             + PropertiesUtil.hashCode(getUrl(), Boolean.FALSE)
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

        Repository  other=(Repository) obj;
        if ((PropertiesUtil.safeCompare(getId(), other.getId(), false) == 0)
         && (PropertiesUtil.safeCompare(getUrl(), other.getUrl(), false) == 0)) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public Repository clone() {
        try {
            return getClass().cast(super.clone());
        } catch(CloneNotSupportedException e) {
            throw new RuntimeException("Failed to clone " + toString() + ": " + e.getMessage(), e);
        }
    }

    @Override
    public String toString() {
        return getId() + ":" + getUrl();
    }

}
