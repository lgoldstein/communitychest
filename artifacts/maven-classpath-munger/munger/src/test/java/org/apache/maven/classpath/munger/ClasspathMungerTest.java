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

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;

import org.apache.maven.classpath.munger.util.UrlUtil;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

/**
 * @author Lyor G.
 * @since Dec 25, 2013 10:08:26 AM
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ClasspathMungerTest extends AbstractTestSupport {
    public ClasspathMungerTest() {
        super();
    }

    @Test
    public void testAddClasspathURLs() throws Exception {
        Thread          thread=Thread.currentThread();
        ClassLoader     parent=thread.getContextClassLoader();
        URLClassLoader  expectedLoader=new URLClassLoader(new URL[0], parent);
        ClasspathMunger munger=new ClasspathMunger(getCurrentTestLogger());
        URL[]           expectedURLs={ UrlUtil.toURL(new File(System.getProperty("java.io.tmpdir"))) };
        URLClassLoader  actualLoader=munger.addClasspathURLs(expectedLoader, Arrays.asList(expectedURLs));
        assertSame("Mismatched returned loader instance", expectedLoader, actualLoader);

        URL[]   actualURLs=actualLoader.getURLs();
        assertEquals("Mismatched URLs size", expectedURLs.length, actualURLs.length);
        for (int index=0; index < expectedURLs.length; index++) {
            URL eURL=expectedURLs[index], aURL=actualURLs[index];
            assertEquals("Mismatched URLs at index=" + index, UrlUtil.adjustURLPathValue(eURL), UrlUtil.adjustURLPathValue(aURL));
        }
    }
}
