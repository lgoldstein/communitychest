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

package org.apache.maven.classpath.munger.util;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

import org.apache.maven.classpath.munger.util.properties.PropertiesUtil;

/**
 * @author Lyor G.
 * @since Jan 1, 2014 11:41:15 AM
 */
public final class UrlUtil {
    private UrlUtil() {
        throw new UnsupportedOperationException("No instance");
    }
   
    /**
     * @param url The {@link URL} value - ignored if <code>null</code>
     * @return The URL(s) source path where {@link #JAR_URL_PREFIX} and
     * any sub-resource are stripped
     * @see #getURLSource(String)
     */
    public static final String getURLSource (URL url) {
        return getURLSource((url == null) ? null : url.toExternalForm());
    }

    /**
     * Separator used in URL(s) that reference a resource inside a JAR
     * to denote the sub-path inside the JAR
     */
    public static final char    RESOURCE_SUBPATH_SEPARATOR='!';

    /**
     * @param externalForm The {@link URL#toExternalForm()} string - ignored if
     * <code>null</code>/empty
     * @return The URL(s) source path where {@link #JAR_URL_PREFIX} and
     * any sub-resource are stripped
     */
    public static final String getURLSource (String externalForm) {
        String  url=externalForm;
        if (PropertiesUtil.isEmpty(url)) {
            return url;
        }

        url = stripJarURLPrefix(externalForm);
        if (PropertiesUtil.isEmpty(url)){
            return url;
        }
        
        int sepPos=url.indexOf(RESOURCE_SUBPATH_SEPARATOR);
        if (sepPos < 0) {
            return UrlUtil.adjustURLPathValue(url);
        } else {
            return UrlUtil.adjustURLPathValue(url.substring(0, sepPos));
        }
    }
    
    /**
     * URL/URI scheme that refers to a JAR
     */
    public static final String  JAR_URL_SCHEME="jar";
    /**
     * Prefix used in URL(s) that reference a resource inside a JAR
     */
    public static final String  JAR_URL_PREFIX=JAR_URL_SCHEME + ":";

    public static final String stripJarURLPrefix(String externalForm) {
        String  url=externalForm;
        if (PropertiesUtil.isEmpty(url)) {
            return url;
        }

        if (url.startsWith(JAR_URL_PREFIX)) {
            return url.substring(JAR_URL_PREFIX.length());
        }       
        
        return url;
    }

    public static final URL concat(URL base, String extension) throws MalformedURLException {
        if (PropertiesUtil.isEmpty(extension)) {
            return base;
        } else {
            return new URL(concat(base.toExternalForm(), extension));
        }
    }

    /**
     * Adds an &quot;extension&quot; to an existing URL &quot;base&quot;
     * @param base The base URL - may not be {@code null}/empty
     * @param extension The extension to be appended - ignored if {@code null}
     * or empty
     * @return The concatenation result - <B>Note:</B> takes care of any
     * trailing/preceding '/' in either the base or the extension - e.g.,</BR></BR>
     * <code>
     * concat(&quot;http://a/b/c/&quot;, &quot;d/e/f&quot;) = &quot;http://a/b/c/d/e/f&quot;</br>
     * concat(&quot;http://a/b/c&quot;, &quot;/d/e/f&quot;) = &quot;http://a/b/c/d/e/f&quot;</br>
     * concat(&quot;http://a/b/c/&quot;, &quot;/d/e/f&quot;) = &quot;http://a/b/c/d/e/f&quot;</br>
     * </code>
     */
    public static final String concat(String base, String extension) {
        if (PropertiesUtil.isEmpty(extension)) {
            return base;
        }
        
        char    endChar=base.charAt(base.length() - 1), startChar=extension.charAt(0);
        if (endChar == '/') {
            if (startChar == '/') {
                if (extension.length() == 1) {
                    return base;
                } else {
                    return base + extension.substring(1);
                }
            } else {
                return base + extension;
            }
        } else if (startChar == '/') {
            return base + extension;
        } else {
            return base + "/" + extension;
        }
    }

    public static final String toString(URL url) {
        return (url == null) ? null : url.toExternalForm();
    }

    public static final String adjustURLPathValue(URL url) {
        return (url == null) ? null : UrlUtil.adjustURLPathValue(url.toExternalForm());
    }

    /**
     * @param path A URL path value
     * @return The path after stripping any trailing '/' provided the path
     * is not '/' itself
     */
    public static final String adjustURLPathValue(final String path) {
        int   pathLen=PropertiesUtil.getSafeLength(path);
        if ((pathLen <= 1) || (path.charAt(pathLen - 1) != '/')) {
            return path;
        } else {
            return path.substring(0, pathLen - 1);
        }
    }
    
    public static final URL toURL(File file) throws MalformedURLException {
        URI uri=file.toURI();
        return uri.toURL();
    }

}
