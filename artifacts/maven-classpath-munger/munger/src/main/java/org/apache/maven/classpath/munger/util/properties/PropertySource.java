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

/**
 * @author Lyor G.
 * @since Dec 24, 2013 10:50:09 AM
 */
public interface PropertySource {
    /**
     * @param name Property name (may not be {@code null}/empty)
     * @return Property value - <code>null</code> if no such property defined
     * (equivalent to {@link #getProperty(String, String)} with <code>null</code>
     * as the default value
     */
    String getProperty (String name);
    /**
     * @param name Property name (may not be {@code null}/empty)
     * @param defaultValue Value to return if property not defined
     * @return The property value or the default if no such property defined
     */
    String getProperty (String name, String defaultValue);
}
