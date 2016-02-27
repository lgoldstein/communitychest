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
import java.util.logging.Logger;

/**
 * @author Lyor G.
 * @since Dec 24, 2013 10:53:33 AM
 */
public interface LogCreator {
    Log createLogger(String name);

    LogCreator DEFAULT=new LogCreator() {
            @Override
            public Log createLogger(final String name) {
                final Logger  logger=Logger.getLogger(name);
                return new AbstractJULWrapper() {
                    @Override
                    public void log(Level level, Object message, Throwable t) {
                        if (!isEnabled(level)) {
                            return;
                        }
                        
                        if (t != null) {
                            logger.log(level, message.toString(), t);
                        } else {
                            logger.log(level, message.toString());
                        }
                    }
                    @Override
                    public boolean isEnabled(Level level) {
                        return logger.isLoggable(level);
                    }
                };
            }
        };

}
