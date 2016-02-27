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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.apache.maven.classpath.munger.logging.AbstractLoggingBean;
import org.apache.maven.classpath.munger.logging.Log;
import org.apache.maven.classpath.munger.util.HttpUtil;

/**
 * @author Lyor G.
 * @since Dec 24, 2013 12:35:48 PM
 */
public abstract class AbstractDependeciesLoader
            extends AbstractLoggingBean
            implements DependeciesLoader {

    protected AbstractDependeciesLoader() {
        super();
    }
    
    protected AbstractDependeciesLoader(Log log) {
        super(log);
    }

    @Override
    public void load(File file) throws IOException {
        InputStream input=new BufferedInputStream(new FileInputStream(file), HttpUtil.DEFAULT_BUFFER_SIZE);
        try {
            load(input);
        } finally {
            input.close();
        }
    }

    @Override
    public void load(URL url) throws IOException {
        InputStream input=url.openStream();
        try {
            load(input);
        } finally {
            input.close();
        }
    }
    
}
