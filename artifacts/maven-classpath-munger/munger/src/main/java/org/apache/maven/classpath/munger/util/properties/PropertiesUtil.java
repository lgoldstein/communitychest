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

package org.apache.maven.classpath.munger.util.properties;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;

/**
 * @author Lyor G.
 * @since Dec 24, 2013 11:17:06 AM
 */
public final class PropertiesUtil {
    private PropertiesUtil() {
        throw new UnsupportedOperationException("No instance");
    }

    /**
     * @param cs  the CharSequence to check, may be {@code null}
     * @return {@code true} if the CharSequence is empty or {@code null}
     */
    public static boolean isEmpty(CharSequence cs) {
        return (getSafeLength(cs) <= 0);
    }

    /**
     * @param seq Input {@link CharSequence}
     * @return The {@link CharSequence#length()} or zero if <code>null</code>
     */
    public static final int getSafeLength (CharSequence seq) {
        if (seq == null) {
            return 0;
        } else {
            return seq.length();
        }
    }

    /**
     * Compares 2 {@link String}-s allowing for <code>null</code>'s
     * @param s1 1st string
     * @param s2 2nd string
     * @return Same as {@link String#compareTo(String)} except that <code>null</code>
     * takes precedence over non-<code>null</code>
     * @see #safeCompare(String, String, boolean)
     */
    public static final int safeCompare (String s1, String s2) {
        return safeCompare(s1, s2, true);
    }

    /**
     * Compares 2 {@link String}-s allowing for <code>null</code>'s
     * @param s1 1st string
     * @param s2 2nd string
     * @param caseSensitive
     * @return Same as {@link String#compareTo(String)} or {@link String#compareToIgnoreCase(String)}
     * (as per the sensitivity flag) except that <code>null</code> takes
     * precedence over non-<code>null</code>
     */
    public static final int safeCompare (String s1, String s2, boolean caseSensitive) {
        if (s1 == s2) {
            return 0;
        } else if (s1 == null) {    // s2 cannot be null or s1 == s2...
            return (-1);
        } else if (s2 == null) {
            return (+1);
        } else if (caseSensitive) {
            return s1.compareTo(s2);
        } else {
            return s1.compareToIgnoreCase(s2);
        }
    }

    /**
     * @param s The {@link String} value to calculate the hash code on - may
     * be <code>null</code>/empty in which case a value of zero is returned
     * @return The calculated hash code
     * @see #hashCode(String, Boolean)
     */
    public static final int hashCode (String s) {
        return hashCode(s, null);
    }

    /**
     * @param s The {@link String} value to calculate the hash code on - may
     * be <code>null</code>/empty in which case a value of zero is returned
     * @param useUppercase Whether to convert the string to uppercase, lowercase
     * or not at all:
     * <UL>
     *      <LI><code>null</code> - no conversion</LI>
     *      <LI>{@link Boolean#TRUE} - get hash code of uppercase</LI>
     *      <LI>{@link Boolean#FALSE} - get hash code of lowercase</LI>
     * </UL>
     * @return The calculated hash code
     */
    public static final int hashCode (String s, Boolean useUppercase) {
        if (isEmpty(s)) {
            return 0;
        } else if (useUppercase == null) {
            return s.hashCode();
        } else if (useUppercase.booleanValue()) {
            return s.toUpperCase().hashCode();
        } else {
            return s.toLowerCase().hashCode();
        }
    }

    /**
     * @param sb The {@link StringBuilder} to scan
     * @param org The source character to be replaced
     * @param rep The replacement character
     * @return The updated {@code StringBuilder} where every occurrence of
     * the source character has been replaced by it replacement
     */
    public static final StringBuilder replaceChar(StringBuilder sb, char org, char rep) {
        if (org == rep) {   // Duh...
            return sb;
        }

        for (int index=0; index < sb.length(); index++) {
            char    ch=sb.charAt(index);
            if (ch == org) {
                sb.setCharAt(index, rep);
            }
        }

        return sb;
    }

    public static final NamedPropertySource asPropertySource(final ServletContext context) {
        return new AbstractNamedPropertySource() {
            @Override
            public String getProperty(String name) {
                return context.getInitParameter(name);
            }

            @Override
            public Collection<String> getAvailableNames() {
                return getPropertiesNames(context);
            }
        };
    }

    public static final Collection<String> getPropertiesNames(ServletContext context) {
        Enumeration<String> names=context.getInitParameterNames();
        if (names == null) {
            return Collections.emptyList();
        } else {
            return Collections.list(names);
        }
    }


    public static final NamedPropertySource asPropertySource(final ServletConfig config) {
        return new AbstractNamedPropertySource() {
            @Override
            public String getProperty(String name) {
                return config.getInitParameter(name);
            }

            @Override
            public Collection<String> getAvailableNames() {
                return getPropertiesNames(config);
            }
        };
    }

    public static final Collection<String> getPropertiesNames(ServletConfig config) {
        Enumeration<String> names=config.getInitParameterNames();
        if (names == null) {
            return Collections.emptyList();
        } else {
            return Collections.list(names);
        }
    }

    public static final NamedPropertySource asPropertySource(final Map<String,?> propsMap) {
        return new AbstractNamedPropertySource() {
            @Override
            public String getProperty(String name) {
                Object  value=propsMap.get(name);
                if (value == null) {
                    return null;
                } else {
                    return value.toString();
                }
            }

            @Override
            public Collection<String> getAvailableNames() {
                return new TreeSet<String>(propsMap.keySet());
            }
        };
    }

    public static final NamedPropertySource asPropertySource(final URL props) throws IOException {
        try(InputStream input=props.openStream()) {
            return asPropertySource(input);
        }
    }

    public static final NamedPropertySource asPropertySource(final InputStream props) throws IOException {
        Properties  p=new Properties();
        try {
            p.load(props);
        } catch(IllegalArgumentException e) {
            throw new IOException("Malformed properties file contents: " + e.getMessage(), e);
        }

        return asPropertySource(p);
    }

    public static final NamedPropertySource asPropertySource(final Properties props) {
        return new NamedPropertySource() {
            @Override
            public String getProperty(String name, String defaultValue) {
                return props.getProperty(name, defaultValue);
            }

            @Override
            public String getProperty(String name) {
                return props.getProperty(name);
            }

            @Override
            public Collection<String> getAvailableNames() {
                return new TreeSet<String>(props.stringPropertyNames());
            }

            @Override
            public String toString() {
                return Objects.toString(props);
            }
        };
    }

    public static final String toString(NamedPropertySource props) {
        if (props == null) {
            return null;
        }

        Collection<String>  names=props.getAvailableNames();
        if ((names == null) || names.isEmpty()) {
            return "{}";
        }

        StringBuilder   sb=new StringBuilder(names.size() * Byte.MAX_VALUE).append('{');
        for (String k : names) {
            String  v=props.getProperty(k);
            sb.append(" [").append(k).append(":").append(v).append(']');
        }
        sb.append(']');
        return sb.toString();
    }

    public static final Map<String,String> resolvePropertiesValues(NamedPropertySource source) {
        return resolvePropertiesValues(source, (source == null) ? Collections.<String>emptyList() : source.getAvailableNames());
    }

    /**
     * @param source The {@link PropertySource} to use - ignored if {@link null}
     * @param names The {@link Collection} of properties names to be processed
     *  - ignored if {@link null}/empty
     * @return A {@link Map} of all the properties whose values have <U>changed</U>
     * due to invocation of the {@link #format(String, PropertySource)} on their
     * original value: key=property name, value=formatting result
     */
    public static final Map<String,String> resolvePropertiesValues(PropertySource source, Collection<String> names) {
        if ((names == null) || (names.size() <= 0) || (source == null)) {
            return Collections.emptyMap();
        }

        Map<String,String>  modsMap=null;
        for (String name : names) {
            String  orgValue=source.getProperty(name), fmtValue=format(orgValue, source);
            if (orgValue == fmtValue) {
                continue;   // nothing to replace
            }

            if (modsMap == null) {
                modsMap = new TreeMap<String,String>();
            }

            modsMap.put(name, fmtValue);
        }

        if (modsMap == null) {
            return Collections.emptyMap();
        } else {
            return modsMap;
        }
    }

    /**
     * Traverses the input {@link String} and looks for property patterns
     * encoded as <code>${propname}</code>. Once such a pattern is encountered
     * it is replaced with its value from the associated {@link PropertySource}
     * instance. If no value is found then the pattern is echoed to the output
     * as-is.
     * @param s Input string - may be {@code null}/empty (in which case nothing
     * is translated)
     * @param source The {@link PropertySource} to use to resolve referenced
     * properties - may be {@code null} (in which case nothing is translated)
     * @return Translation result - same as input if no translation occurred
     */
    public static final String format (final String s, final PropertySource source) {
        int   sLen=getSafeLength(s);
        if ((sLen <= 0) || (source == null)) {
            return s;
        }

        StringBuilder   sb=null;
        int             curPos=0;
        for (int    nextPos=s.indexOf('$'); (nextPos >= curPos) && (nextPos < sLen); )
        {
            if (nextPos >= (sLen-1)) {
                break;  // if '$' at end then nothing can follow it anyway
            }

            if (s.charAt(nextPos+1) != '{') {
                nextPos = s.indexOf('$', nextPos + 1);
                continue;   // if not followed by '{' then assume not start of a property
            }

            final int   endPos=s.indexOf('}', nextPos + 2);
            if (endPos <= nextPos) {
                break;  // if no ending '}' then no more properties can exist
            }

            if (endPos <= (nextPos+2)) {
                if (endPos >= (sLen-1))
                    break;

                nextPos = s.indexOf('$', endPos + 1);
                continue;   // if empty property name assume clear text
            }

            final String    propName=s.substring(nextPos+2, endPos), propVal=source.getProperty(propName);
            if (propVal == null) {
                nextPos = s.indexOf('$', endPos + 1);
                continue;   // if empty property value assume clear text
            }

            final String    repVal=format(propVal, source);    // do recursive resolution
            if (sb == null) {
                sb = new StringBuilder(sLen + repVal.length());
            }

            // append clear text
            if (nextPos > curPos) {
                final String    t=s.substring(curPos, nextPos);
                sb.append(t);
            }
            sb.append(repVal);

            if ((curPos=(endPos+1)) >= sLen) {
                break;  // stop if gone beyond string length
            }

            nextPos = s.indexOf('$', curPos);   // keep looking
        }

        // check if any leftovers
        if ((curPos > 0) && (curPos < sLen)) {
            final String    t=s.substring(curPos);
            sb.append(t);   // NOTE: sb cannot be null since we appended something to it
        }

        if (sb == null) {   // means no replacement took place
            return s;
        } else {
            return sb.toString();
        }
    }
}
