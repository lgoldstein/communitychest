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

import java.util.Properties;

import org.apache.maven.classpath.munger.AbstractTestSupport;
import org.apache.maven.classpath.munger.util.properties.PropertySource;

/**
 * @author Lyor G.
 * @since Dec 24, 2013 11:31:42 AM
 */
public abstract class AbstractPropertySourceTestSupport extends AbstractTestSupport {
    protected AbstractPropertySourceTestSupport() {
        super();
    }

    public static final void assertPropertySourceContents(Properties props, PropertySource source) {
        for (String name : props.stringPropertyNames()) {
            String  expected=props.getProperty(name), actual=source.getProperty(name);
            assertEquals(name + ": mismatched value", expected, actual);
        }
    }
}
