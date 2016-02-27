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

import java.util.Date;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;

import org.apache.maven.classpath.munger.util.properties.PropertiesUtil;
import org.apache.maven.classpath.munger.util.properties.PropertySource;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.mockito.stubbing.OngoingStubbing;

/**
 * @author Lyor G.
 * @since Dec 24, 2013 11:30:32 AM
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class PropertiesUtilTest extends AbstractPropertySourceTestSupport {
    public PropertiesUtilTest() {
        super();
    }

    @Test
    public void testServletContextSource() {
        ServletContext      context=Mockito.mock(ServletContext.class);
        Map<String,String>  valuesMap=addValueAnswer(Mockito.when(context.getInitParameter(Matchers.anyString())));
        assertPropertySourceContents(valuesMap, PropertiesUtil.asPropertySource(context));
    }

    @Test
    public void testServletConfigSource() {
        ServletConfig      config=Mockito.mock(ServletConfig.class);
        Map<String,String>  valuesMap=addValueAnswer(Mockito.when(config.getInitParameter(Matchers.anyString())));
        assertPropertySourceContents(valuesMap, PropertiesUtil.asPropertySource(config));
    }

    @Test
    public void testFormat() {
        Map<String,String>  valuesMap=new TreeMap<String,String>() {
                private static final long serialVersionUID = 1L;
                
                {
                    put("testName", getCurrentTestName());
                    put("timestamp", new Date().toString());
                }
            };
       PropertySource   source=PropertiesUtil.asPropertySource(valuesMap);
       assertNull("Mismatched null formatting", PropertiesUtil.format(null, source));
       // NOTE: relies on auto-interning of string literals
       assertSame("Mismatched empty formatting", "", PropertiesUtil.format("", source));

       StringBuilder    sb=new StringBuilder(Byte.MAX_VALUE).append("${unresolved}");
       StringBuilder    original=new StringBuilder(Byte.MAX_VALUE).append(sb);
       for (Map.Entry<String,String> ve : valuesMap.entrySet()) {
           String   name=ve.getKey(), value=ve.getValue();
           sb.append(name).append('=').append(value).append(';');
           original.append(name).append("=${").append(name).append("};");
       }

       String   expected=sb.toString(), pattern=original.toString();
       assertSame("Mismatched non-formatted result", expected, PropertiesUtil.format(expected, source));
       assertSame("Mismatched no-source result", pattern, PropertiesUtil.format(pattern, null));

       assertEquals("Mismatched result", expected, PropertiesUtil.format(pattern, source));
    }

    private Map<String,String> addValueAnswer(OngoingStubbing<String> stub) {
        final Map<String,String>  valuesMap=new TreeMap<String,String>() {
                private static final long serialVersionUID = 1L;
                
                {
                    put("testName", getCurrentTestName());
                    put("timestamp", new Date().toString());
                }
            };
        stub.thenAnswer(new Answer<String>() {
                @Override
                public String answer(InvocationOnMock invocation) throws Throwable {
                    Object[]    args=invocation.getArguments();
                    assertNotNull("No invocation args", args);
                    assertEquals("Mismatched invocation arguments size", 1, args.length);
    
                    Object  name=args[0];
                    assertTrue("Non string argument", name instanceof String);
                    return valuesMap.get(name.toString());
                }
            });
        return valuesMap;
    }
}
