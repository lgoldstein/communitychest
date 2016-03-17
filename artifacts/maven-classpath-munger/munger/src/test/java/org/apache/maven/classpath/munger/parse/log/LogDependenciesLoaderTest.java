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

package org.apache.maven.classpath.munger.parse.log;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.io.StreamCorruptedException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.apache.maven.classpath.munger.AbstractTestSupport;
import org.apache.maven.classpath.munger.Dependency;
import org.apache.maven.classpath.munger.Repository;
import org.apache.maven.classpath.munger.parse.MavenDependency;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.mockito.Mockito;

/**
 * @author Lyor G.
 * @since Dec 25, 2013 3:27:18 PM
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class LogDependenciesLoaderTest extends AbstractTestSupport {
    public LogDependenciesLoaderTest() {
        super();
    }

    @Test
    public void testParseDependencies() throws IOException {
        List<? extends MavenDependency>    expected=
                Collections.unmodifiableList(
                        Arrays.asList(
                                new MavenDependency(getClass().getPackage().getName(), getCurrentTestName(), Dependency.DEFAULT_PACKAGING, "10.2.71.813", "compile"),
                                new MavenDependency(Assert.class.getPackage().getName(), "junit", Dependency.DEFAULT_PACKAGING, "1.2.3", "runtime")
                            ));

        File        file=createTempFile(getCurrentTestName(), ".log");
        final long  startTime=System.currentTimeMillis();
        PrintStream out=writeLogFilePreamble(new PrintStream(file, "UTF-8"));
        try {
            out.append("[INFO] --- maven-dependency-plugin:2.8:list ").append(getClass().getSimpleName()).append(" @ ").append(getCurrentTestName()).println(" ---");
            out.println("[INFO]");
            out.println("[INFO] The following files have been resolved:");
            out.println("[INFO]");
            for (MavenDependency d : expected) {
                appendDependency(out, d);
            }
            for (MavenDependency d : new MavenDependency[] {
                    new MavenDependency(String.class.getPackage().getName(), "tools", Dependency.DEFAULT_PACKAGING, "1.7", "system"),
                    new MavenDependency(Mockito.class.getPackage().getName(), "mockito-all", Dependency.DEFAULT_PACKAGING, "5.6.7", "test")
            }) {
                appendDependency(out, d);
            }
            writeLogFileEpilogue(out, startTime);
            if (out.checkError()) {
                throw new StreamCorruptedException("Failed to generate data");
            }
        } finally {
            out.close();
        }

        LogDependenciesLoader   loader=new LogDependenciesLoader(getCurrentTestLogger());
        loader.load(file);

        List<Dependency>    actual=loader.getDependencies();
        assertEquals("Mismatched result size", expected.size(), actual.size());
        for (int index=0; index < expected.size(); index++) {
            Dependency  eValue=expected.get(index), aValue=actual.get(index);
            assertEquals("Mismatched results at index=" + index, eValue, aValue);
        }
    }

    private <P extends PrintStream> P appendDependency(P out, MavenDependency d) {
        out.append("[INFO]    ").append(d.getGroupId())
            .append(':').append(d.getArtifactId())
            .append(":").append(d.getPackaging())
            .append(':').append(d.getVersion())
            .append(':').println(d.getScope())
            ;
        return out;
    }

    @Test
    public void testParseRepositories() throws IOException {
        List<Repository>    expected=
                Collections.unmodifiableList(
                        Arrays.asList(
                                new Repository(getClass().getSimpleName(), "http://7.3.6.5:8080/maven/nexus"),
                                new Repository(getCurrentTestName(), "https://10.2.71.813/repo/maven2")
                            ));
        File    file=createTempFile(getCurrentTestName(), ".log");
        final long  startTime=System.currentTimeMillis();
        PrintStream out=writeLogFilePreamble(new PrintStream(file, "UTF-8"));
        try {
            out.println("[INFO]");
            out.append("[INFO] --- maven-dependency-plugin:2.8:list-repositories ").append(getClass().getSimpleName()).append(" @ ").append(getCurrentTestName()).println(" ---");
            out.println("[INFO] Repositories Used by this build:");

            for (Repository r : expected) {
                appendRepository(out, r);
            }

            writeLogFileEpilogue(out, startTime);
            if (out.checkError()) {
                throw new StreamCorruptedException("Failed to generate data");
            }
        } finally {
            out.close();
        }

        LogDependenciesLoader   loader=new LogDependenciesLoader(getCurrentTestLogger());
        loader.load(file);

        List<Repository>    actual=loader.getRepositories();
        assertEquals("Mismatched result size", expected.size(), actual.size());
        for (int index=0; index < expected.size(); index++) {
            Repository  eValue=expected.get(index), aValue=actual.get(index);
            assertEquals("Mismatched results at index=" + index, eValue, aValue);
        }
    }

    private <P extends PrintStream> P appendRepository(P out, Repository r) {
        out.append("[INFO]        id: ").println(r.getId());
        out.append("      url: ").println(r.getUrl());
        out.println("      layout: default");
        out.println("      snapshots: [enabled => true, update => daily]");
        out.println("      releases: [enabled => true, update => daily]");
        out.println();
        return out;
    }

    private <P extends PrintStream> P writeLogFilePreamble(P out) {
        out.println("Some gibberish");
        out.println("[ERROR] Some non info data");
        out.println("[INFO] Scanning for projects...");
        out.println("[INFO]");
        out.println("[INFO] ------------------------------------------------------------------------");
        out.println("[INFO] Test test test test test  test test test test test test test test test");
        out.println("[INFO] ------------------------------------------------------------------------");
        out.println("[INFO]");
        return out;
    }

    private <P extends PrintStream> P writeLogFileEpilogue(P out, long startTime) {
        out.println("[INFO]");
        out.println("[INFO] ------------------------------------------------------------------------");
        out.println("[INFO] BUILD SUCCESS");
        out.println("[INFO] ------------------------------------------------------------------------");
        out.append("[INFO] Total time: ").append(String.valueOf(System.currentTimeMillis() - startTime)).println(" msec");
        out.append("[INFO] [INFO] Finished at: ").println(new Date());
        out.println("[INFO] ------------------------------------------------------------------------");
        out.println("[INFO]");
        return out;
    }
}
