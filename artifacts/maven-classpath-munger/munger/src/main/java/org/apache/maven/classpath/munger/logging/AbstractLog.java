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

package org.apache.maven.classpath.munger.logging;

/**
 * @author Lyor G.
 * @since Jan 1, 2014 12:43:18 PM
 */
public abstract class AbstractLog implements Log {
    protected AbstractLog() {
        super();
    }

    @Override
    public void trace(Object message) {
        trace(message, null);
    }

    @Override
    public void debug(Object message) {
        debug(message, null);
    }

    @Override
    public void info(Object message) {
        info(message, null);
    }

    @Override
    public void warn(Object message) {
        warn(message, null);
    }

    @Override
    public void error(Object message) {
        error(message, null);
    }

    @Override
    public void fatal(Object message) {
        fatal(message, null);
    }
}
