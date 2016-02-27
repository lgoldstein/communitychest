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

import java.util.Collections;
import java.util.Map;

import org.apache.maven.classpath.munger.util.properties.AggregatePropertySource;
import org.apache.maven.classpath.munger.util.properties.PropertiesUtil;
import org.apache.maven.classpath.munger.util.properties.PropertySource;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

/**
 * @author Lyor G.
 * @since Dec 24, 2013 11:26:41 AM
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class AggregatePropertySourceTest extends AbstractPropertySourceTestSupport {
    public AggregatePropertySourceTest() {
        super();
    }

    @Test
    public void testAggregation() {
        Map<String,String>  m1=Collections.singletonMap("foo", "bar");
        PropertySource      s1=PropertiesUtil.asPropertySource(m1);
        Map<String,String>  m2=Collections.singletonMap("foo", "rab");
        PropertySource      s2=PropertiesUtil.asPropertySource(m2);

        // ensure correct initial state
        for (Map.Entry<String,String> m1p : m1.entrySet()) {
            String  name=m1p.getKey(), v1=m1p.getValue(), v2=m2.get(name);
            assertNotNull("Missing " + name + " from 2nd source", v2);
            assertFalse("Identical values for " + name, v1.equals(v2));
        }

        runAggregationTest(m1, s1, s2);
        runAggregationTest(m2, s2, s1);
    }
    
    private void runAggregationTest (Map<String,String> valsMap, PropertySource ... srcList) {
        assertPropertySourceContents(valsMap, new AggregatePropertySource(srcList));
    }
}
