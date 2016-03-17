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

import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.TreeSet;

/**
 * @author Lyor G.
 * @since Mar 25, 2014 9:41:34 AM
 */
public interface NamedPropertySource extends PropertySource {
    /**
     * @return The {@link Collection} of available properties names
     */
    Collection<String> getAvailableNames();

    NamedPropertySource EMPTY=new AbstractNamedPropertySource() {
            @Override
            public String getProperty(String name) {
                return null;
            }

            @Override
            public Collection<String> getAvailableNames() {
                return Collections.emptyList();
            }

            @Override
            public String toString() {
                return "{}";
            }
        };

    NamedPropertySource SYSENV=new AbstractNamedPropertySource() {
            @Override
            public String getProperty(String name) {
                return System.getenv(name);
            }

            @Override
            public Collection<String> getAvailableNames() {
                return new TreeSet<String>(System.getenv().keySet());
            }

            @Override
            public String toString() {
                return Objects.toString(System.getenv());
            }
        };

    NamedPropertySource SYSPROP=new NamedPropertySource() {
            @Override
            public String getProperty(String name, String defaultValue) {
                return System.getProperty(name, defaultValue);
            }

            @Override
            public String getProperty(String name) {
                return System.getProperty(name);
            }

            @Override
            public Collection<String> getAvailableNames() {
                return new TreeSet<String>(System.getProperties().stringPropertyNames());
            }

            @Override
            public String toString() {
                return Objects.toString(System.getProperties());
            }
        };
}
