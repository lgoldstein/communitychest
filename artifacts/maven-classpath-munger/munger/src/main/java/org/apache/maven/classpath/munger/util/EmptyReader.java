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
import java.io.Reader;
import java.nio.CharBuffer;

/**
 * Equivalent to <code>/dev/null</code> always returns EOF and ignores all {@link #close} calls
 * @author Lyor G.
 * @since Dec 24, 2013 12:42:47 PM
 */
public class EmptyReader extends Reader {
    public static final EmptyReader INSTANCE=new EmptyReader();

    public EmptyReader() {
        super();
    }

    @Override
    public int read(CharBuffer target) throws IOException {
        return (-1);
    }

    @Override
    public long skip(long n) throws IOException {
        if (n < 0L) {
            throw new IllegalArgumentException("skip value is negative");
        }

        return 0L;
    }

    @Override
    public boolean ready() throws IOException {
        return true;
    }

    @Override
    public int read() throws IOException {
        return (-1);
    }

    @Override
    public int read(char[] cbuf, int off, int len) throws IOException {
        return (-1);
    }

    @Override
    public void close() throws IOException {
        // ignored
    }
}
