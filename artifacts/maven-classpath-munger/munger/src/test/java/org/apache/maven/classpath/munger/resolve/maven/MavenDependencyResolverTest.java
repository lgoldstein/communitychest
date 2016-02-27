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

package org.apache.maven.classpath.munger.resolve.maven;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

import org.apache.maven.classpath.munger.AbstractTestSupport;
import org.apache.maven.classpath.munger.Dependency;
import org.apache.maven.classpath.munger.Repository;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

/**
 * @author Lyor G.
 * @since Dec 25, 2013 10:40:53 AM
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class MavenDependencyResolverTest extends AbstractTestSupport {
    public MavenDependencyResolverTest() {
        super();
    }
    
    @Test
    public void testResolveDependency() throws IOException {
        List<Repository>    repos=
                Collections.singletonList(
                        new Repository(MavenDependencyResolver.DEFAULT_REPO_URL_PROP, MavenDependencyResolver.DEFAULT_REPO_URL_VALUE));
        MavenDependencyResolver resolver=new MavenDependencyResolver(getCurrentTestLogger());
        File                    localRepoFolder=ensureFolderExists(new File(ensureTempFolderExists(), "repository"));
        String                  localRepoRoot=localRepoFolder.getAbsolutePath();
        deleteDirectory(localRepoFolder);   // make sure we are starting with a clean slate

        for (Dependency d : new Dependency[] {
                    new Dependency("junit", "junit", "4.11"),
                    new Dependency("org.mockito", "mockito-all", "1.9.5") }) {
            String  groupId=d.getGroupId(), artifactId=d.getArtifactId(), version=d.getVersion();
            File    targetPath=new File(localRepoFolder,
                                        groupId.replace('.', File.separatorChar)
                                        + File.separator + artifactId
                                        + File.separator + version
                                        + File.separator + artifactId + "-" + version
                                        + ".jar"   // TODO add support for classifier and packaging
                                     );
            resolver.cleanResidualFile(d, targetPath);
            assertFalse("Target file not removed: " + targetPath.getAbsolutePath(), targetPath.exists());

            File    resolvedPath=resolver.resolveDependency(localRepoRoot, d, repos);
            assertEquals("Mismatched resolved path", targetPath.getAbsolutePath(), resolvedPath.getAbsolutePath());
        }

    }
}
