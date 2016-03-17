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
import org.apache.maven.classpath.munger.logging.AbstractLoggingBean;
import org.apache.maven.classpath.munger.logging.Log;
import org.apache.maven.classpath.munger.util.properties.NamedPropertySource;
import org.apache.maven.classpath.munger.util.properties.PropertiesUtil;

/**
 * @author Lyor G.
 * @since Mar 25, 2014 9:27:32 AM
 */
public abstract class AbstractArtifactValidator extends AbstractLoggingBean implements ArtifactValidator {
    protected AbstractArtifactValidator() {
        this(null);
    }

    protected AbstractArtifactValidator(Log log) {
        super(log);
    }

    protected void validate(Dependency d, URL artifactData, URL signatureData) throws IOException, SecurityException {
        if (logger.isDebugEnabled()) {
            logger.debug("validate(" + d + ")[" + artifactData.toExternalForm() + "] - signature=" + signatureData.toExternalForm());
        }

        NamedPropertySource expected=JarValidationUtils.createJarSignature(artifactData);
        if (logger.isTraceEnabled()) {
            for (String name : expected.getAvailableNames()) {
                logger.trace("validate(" + d + ") EXP[" + name + "]: " + expected.getProperty(name));
            }
        }

        NamedPropertySource actual=PropertiesUtil.asPropertySource(signatureData);
        if (logger.isTraceEnabled()) {
            for (String name : actual.getAvailableNames()) {
                logger.trace("validate(" + d + ") ACT[" + name + "]: " + actual.getProperty(name));
            }
        }

        JarValidationUtils.validateJarSignature(expected, actual);
    }
}
