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

import java.util.logging.Level;

/**
 * @author Lyor G.
 */
public abstract class AbstractJULWrapper extends AbstractLog {
    protected AbstractJULWrapper() {
        super();
    }

    @Override
    public boolean isDebugEnabled() {
        return isEnabled(Level.FINE) || isEnabled(Level.FINER);
    }

    @Override
    public boolean isErrorEnabled() {
        return isFatalEnabled();
    }

    @Override
    public boolean isFatalEnabled() {
        return isEnabled(Level.SEVERE);
    }

    @Override
    public boolean isInfoEnabled() {
        return isEnabled(Level.INFO);
    }

    @Override
    public boolean isTraceEnabled() {
        return isEnabled(Level.FINEST);
    }

    @Override
    public boolean isWarnEnabled() {
        return isEnabled(Level.WARNING);
    }

    @Override
    public void trace(Object message, Throwable t) {
        log(Level.FINEST, message, t);
    }

    @Override
    public void debug(Object message, Throwable t) {
        log(Level.FINE, message, t);
    }

    @Override
    public void info(Object message, Throwable t) {
        log(Level.INFO, message, t);
    }

    @Override
    public void warn(Object message, Throwable t) {
        log(Level.WARNING, message, t);
    }

    @Override
    public void error(Object message, Throwable t) {
        fatal(message, t);
    }

    @Override
    public void fatal(Object message, Throwable t) {
        log(Level.SEVERE, message, t);
    }

    public abstract boolean isEnabled(Level level);
    
    public abstract void log(Level level, Object message, Throwable t);
}
