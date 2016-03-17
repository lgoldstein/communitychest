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

package org.apache.maven.classpath.munger.util;

import java.io.IOException;

import org.apache.maven.classpath.munger.logging.Log;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * A {@link DefaultHandler} extension that logs warnings and errors and
 * returns empty {@link InputSource}-s from its {@link #resolveEntity(String, String)}
 * calls
 * @author Lyor G.
 * @since Dec 24, 2013 12:40:52 PM
 */
public abstract class IgnoringSAXHandler extends DefaultHandler {
    protected final transient Log logger;

    protected IgnoringSAXHandler(Log log) {
        logger = log;
    }

    @Override
    public void warning(SAXParseException e) throws SAXException {
        logger.warn(e.getMessage() + " at line " + e.getLineNumber() + ", col " + e.getColumnNumber());
        throw e;
    }

    @Override
    public void error(SAXParseException e) throws SAXException {
        logger.error(e.getMessage() + " at line " + e.getLineNumber() + ", col " + e.getColumnNumber());
        throw e;
    }

    @Override
    public InputSource resolveEntity(String publicId, String systemId)
            throws IOException, SAXException {
        logger.info("resolveEntity(" + publicId + ")[" + systemId + "] ignored");

        InputSource source=new InputSource(EmptyReader.INSTANCE);
        // not really necessary, but recommended
        source.setPublicId(publicId);
        source.setSystemId(systemId);
        source.setEncoding("UTF-8");
        return source;
    }
}
