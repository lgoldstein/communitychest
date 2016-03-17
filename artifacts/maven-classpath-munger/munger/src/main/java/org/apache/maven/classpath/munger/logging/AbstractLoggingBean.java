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

import org.apache.maven.classpath.munger.util.properties.PropertiesUtil;

/**
 * @author Lyor G.
 * @since Dec 24, 2013 12:30:37 PM
 */
public abstract class AbstractLoggingBean {
    protected transient Log logger;

    protected AbstractLoggingBean() {
        this("");
    }

    protected AbstractLoggingBean(Log log) {
        logger = (log == null) ? LogFactory.getLog(getClass()) : log;
    }

    protected AbstractLoggingBean(Class<?> index) {
        this(null, index);
    }

    protected AbstractLoggingBean(String index) {
        this(null, index);
    }

    protected AbstractLoggingBean(LogFactory factory) {
        this(factory, "");
    }

    protected AbstractLoggingBean(LogFactory factory, Class<?> index) {
        this(factory, (index == null) ? "" : index.getSimpleName());
    }

    protected AbstractLoggingBean(LogFactory factory, String index) {
        if (PropertiesUtil.isEmpty(index)) {
            logger = (factory == null)
                   ? LogFactory.getLog(getClass())
                   : factory.getInstance(getClass())
                   ;
        } else {
            logger = (factory == null)
                   ? LogFactory.getLog(getClass().getName() + "[" + index + "]")
                   : factory.getInstance(getClass().getName() + "[" + index + "]")
                   ;
        }
    }

    public final Log getLogger() {
        return logger;
    }
}
