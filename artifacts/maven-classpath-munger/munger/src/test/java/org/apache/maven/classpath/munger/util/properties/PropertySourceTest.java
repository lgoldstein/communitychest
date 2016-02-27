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

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

/**
 * @author Lyor G.
 * @since Dec 24, 2013 11:26:22 AM
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class PropertySourceTest extends AbstractPropertySourceTestSupport {
    public PropertySourceTest() {
        super();
    }

    @Test
    public void testSysprops() {
        assertPropertySourceContents(System.getProperties(), NamedPropertySource.SYSPROP);
    }

    @Test
    public void testSysenv() {
        assertPropertySourceContents(System.getenv(), NamedPropertySource.SYSENV);
    }
}
