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

package org.apache.maven.classpath.munger.validation;

import java.io.IOException;
import java.net.URL;

import org.apache.maven.classpath.munger.Dependency;

/**
 * @author Lyor G.
 * @since Mar 25, 2014 9:16:41 AM
 */
public interface ArtifactValidator {
    /**
     * Validates the dependency signature
     * @param d The {@link Dependency} being validate
     * @param artifactData The {@link URL} to the resolved artifact content
     * @throws IOException If failed to access the contents
     * @throws SecurityException If failed to validate the contents
     */
    void validate(Dependency d, URL artifactData) throws IOException, SecurityException;
}
