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

package org.apache.maven.classpath.munger.validation.maven;

import java.io.IOException;
import java.net.URL;

import org.apache.maven.classpath.munger.Dependency;
import org.apache.maven.classpath.munger.logging.Log;
import org.apache.maven.classpath.munger.util.UrlUtil;
import org.apache.maven.classpath.munger.validation.AbstractArtifactValidator;

/**
 * @author Lyor G.
 * @since Mar 25, 2014 9:31:53 AM
 */
public class MavenArtifactValidator extends AbstractArtifactValidator {
    private final URL baseLocation;

    public MavenArtifactValidator(URL signaturesBaseLocation) {
        this(null, signaturesBaseLocation);
    }

    public MavenArtifactValidator(Log log, URL signaturesBaseLocation) {
        super(log);

        if (signaturesBaseLocation == null) {
            throw new IllegalArgumentException("No signatures base location provided");
        }
        baseLocation = signaturesBaseLocation;
    }

    @Override
    public void validate(Dependency d, URL artifactData) throws IOException, SecurityException {
        String  signaturePath=d.buildArtifactPath('/', ".signature");
        validate(d, artifactData, UrlUtil.concat(baseLocation, signaturePath));
    }
}
