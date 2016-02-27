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

package org.apache.maven.classpath.munger.parse;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collection;
import java.util.List;

import org.apache.maven.classpath.munger.Dependency;
import org.apache.maven.classpath.munger.Repository;
import org.apache.maven.classpath.munger.util.properties.NamedPropertySource;

/**
 * @author Lyor G.
 * @since Dec 24, 2013 12:33:59 PM
 */
public interface DependeciesLoader {
    Collection<String> getDefinedPropertiesNames();
    NamedPropertySource getProperties();
    List<Dependency> getDependencies();
    List<Repository> getRepositories();

    void load(File file) throws IOException;
    void load(URL url) throws IOException;
    void load(InputStream inputStream) throws IOException;
}
