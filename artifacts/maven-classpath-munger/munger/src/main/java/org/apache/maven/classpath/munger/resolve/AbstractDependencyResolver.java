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

package org.apache.maven.classpath.munger.resolve;

import org.apache.maven.classpath.munger.logging.AbstractLoggingBean;
import org.apache.maven.classpath.munger.logging.Log;

/**
 * @author Lyor G.
 * @since Dec 25, 2013 9:18:12 AM
 */
public abstract class AbstractDependencyResolver extends AbstractLoggingBean implements DependencyResolver {
    protected AbstractDependencyResolver() {
        super();
    }

    protected AbstractDependencyResolver(Log log) {
        super(log);
    }
}
