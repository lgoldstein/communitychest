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

package org.apache.maven.classpath.munger.parse.pom;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.io.StreamCorruptedException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

import javax.servlet.Servlet;

import org.apache.maven.classpath.munger.AbstractTestSupport;
import org.apache.maven.classpath.munger.Dependency;
import org.apache.maven.classpath.munger.Repository;
import org.apache.maven.classpath.munger.parse.MavenDependency;
import org.apache.maven.classpath.munger.util.properties.PropertiesUtil;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.mockito.Mockito;

/**
 * @author Lyor G.
 * @since Dec 24, 2013 1:12:25 PM
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class PomDependenciesLoaderTest extends AbstractTestSupport {
    public PomDependenciesLoaderTest() {
        super();
    }

    @Test
    public void testPropertiesParsing() throws IOException {
        Map<String,String>  testProps=new TreeMap<String,String>();
        {
            Properties sysProps=System.getProperties();
            for (String name : sysProps.stringPropertyNames()) {
                if ("line.separator".equalsIgnoreCase(name)
                 || "file.separator".equalsIgnoreCase(name)) {
                    continue;   // avoid these issues
                }
                String  value=sysProps.getProperty(name);
                testProps.put(name, value);
            }
        }

        File    file=createTempFile(getCurrentTestName(), ".xml");
        PrintStream out=writePOMFilePreamble(new PrintStream(file, "UTF-8"));
        try {
            out.append('\t').println("<properties>");
            for (Map.Entry<String,String> pe : testProps.entrySet()) {
                String  name=pe.getKey(), value=pe.getValue();
                out.append("\t\t<").append(name).append('>').append(value).append("</").append(name).println('>');
            }
            out.append('\t').println("</properties>");

            out.println();
            out.println("</project>");
            if (out.checkError()) {
                throw new StreamCorruptedException("Failed to generate data");
            }
        } finally {
            out.close();
        }
        
        PomDependenciesLoader   loader=new PomDependenciesLoader(getCurrentTestLogger());
        loader.load(file);

        assertPropertySourceContents(testProps, loader.getProperties());
    }

    @Test
    public void testDependenciesParsing() throws IOException {
        List<? extends MavenDependency>    expected=
                Collections.unmodifiableList(
                        Arrays.asList(
                                new MavenDependency(getClass().getPackage().getName(), getClass().getSimpleName(), Dependency.DEFAULT_PACKAGING, "7.3.65"),
                                new MavenDependency(getClass().getPackage().getName(), getCurrentTestName(), Dependency.DEFAULT_PACKAGING, "10.2.71.813", "compile"),
                                new MavenDependency(Assert.class.getPackage().getName(), "junit", Dependency.DEFAULT_PACKAGING, "1.2.3", "runtime")
                            ));

        File    file=createTempFile(getCurrentTestName(), ".xml");
        PrintStream out=writePOMFilePreamble(new PrintStream(file, "UTF-8"));
        try {
            out.append('\t').println("<dependencies>");
            for (MavenDependency d : expected) {
                appendDependency(out, d);
            }
            for (MavenDependency d : new MavenDependency[] {
                    new MavenDependency(Servlet.class.getPackage().getName(), "servlet-api", Dependency.DEFAULT_PACKAGING, "3.0", "provided"),
                    new MavenDependency(String.class.getPackage().getName(), "tools", "1.7", Dependency.DEFAULT_PACKAGING, "system"),
                    new MavenDependency(Mockito.class.getPackage().getName(), "mockito-all", Dependency.DEFAULT_PACKAGING, "5.6.7", "test")
            }) {
                appendDependency(out, d);
            }
            out.append('\t').println("</dependencies>");

            out.println();
            out.println("</project>");
            if (out.checkError()) {
                throw new StreamCorruptedException("Failed to generate data");
            }
        } finally {
            out.close();
        }
        
        PomDependenciesLoader   loader=new PomDependenciesLoader(getCurrentTestLogger());
        loader.load(file);

        List<Dependency>    actual=loader.getDependencies();
        assertEquals("Mismatched result size", expected.size(), actual.size());
        for (int index=0; index < expected.size(); index++) {
            Dependency  eValue=expected.get(index), aValue=actual.get(index);
            assertEquals("Mismatched results at index=" + index, eValue, aValue);
        }
    }

    private <P extends PrintStream> P appendDependency(P out, MavenDependency d) {
        out.append("\t\t").println("<dependency>");
            out.append("\t\t\t").append("<groupId>").append(d.getGroupId()).println("</groupId>");
            out.append("\t\t\t").append("<artifactId>").append(d.getArtifactId()).println("</artifactId>");
            out.append("\t\t\t").append("<version>").append(d.getVersion()).println("</version>");
            out.append("\t\t\t").append("<type>").append(d.getPackaging()).println("</type>");
            
            String  scope=d.getScope();
            if (!PropertiesUtil.isEmpty(scope)) {
                out.append("\t\t\t").append("<scope>").append(scope).println("</scope>");
            }
        out.append("\t\t").println("</dependency>");
        
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
        File    file=createTempFile(getCurrentTestName(), ".xml");
        PrintStream out=writePOMFilePreamble(new PrintStream(file, "UTF-8"));
        try {
            out.append('\t').println("<repositories>");
            for (Repository r : expected) {
                out.append("\t\t").println("<repository>");
                    out.append("\t\t\t").append("<id>").append(r.getId()).println("</id>");
                    out.append("\t\t\t").append("<name>").append(r.getId()).println("</name>");
                    out.append("\t\t\t").append("<url>").append(r.getUrl()).println("</url>");
                out.append("\t\t").println("</repository>");
            }
            out.append('\t').println("</repositories>");

            out.println();
            out.println("</project>");
            if (out.checkError()) {
                throw new StreamCorruptedException("Failed to generate data");
            }
        } finally {
            out.close();
        }
        
        PomDependenciesLoader   loader=new PomDependenciesLoader(getCurrentTestLogger());
        loader.load(file);

        List<Repository>    actual=loader.getRepositories();
        assertEquals("Mismatched result size", expected.size(), actual.size());
        for (int index=0; index < expected.size(); index++) {
            Repository  eValue=expected.get(index), aValue=actual.get(index);
            assertEquals("Mismatched results at index=" + index, eValue, aValue);
        }
    }

    private <P extends PrintStream> P writePOMFilePreamble(P out) {
        out.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        out.println("<!-- " + getClass().getSimpleName() + "#" + getCurrentTestName() + " -->");
        out.println("<project xmlns=\"http://maven.apache.org/POM/4.0.0\"");
        out.append("\t\t").println("xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"");
        out.append("\t\t").println("xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/maven-v4_0_0.xsd\">");
        out.append('\t').println("<modelVersion>4.0.0</modelVersion>");
        out.append("\t\t").println("<groupId>org.apache.maven.classpath.munger</groupId>");
        out.append("\t\t").println("<artifactId>parent</artifactId>");
        out.append("\t\t").println("<packaging>pom</packaging>");
        out.append("\t\t").println("<name>" + getClass().getPackage().getName() + ":" + getCurrentTestName() + "</name>");
        out.append("\t\t").println("<version>1.0.0-CI-SNAPSHOT</version>");
        out.println();

        return out;
    }
}
