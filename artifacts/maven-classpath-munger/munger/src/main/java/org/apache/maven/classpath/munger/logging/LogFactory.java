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

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Fashioned like <A HREF="http://commons.apache.org/proper/commons-logging/">Apache Commons Logging</A>
 * @author Lyor G.
 * @since Dec 24, 2013 10:40:02 AM
 */
public class LogFactory {
    private final Map<String,LoggerWeakRef>  loggersMap=new TreeMap<String,LoggerWeakRef>();
    private final AtomicReference<LogCreator>  creatorHolder=new AtomicReference<LogCreator>(null);
    private static final LogFactory    factoryHolder=new LogFactory(LogCreator.DEFAULT);
    
    public static final LogCreator setupLogFactory(LogCreator creator) {
        synchronized(factoryHolder) {
            LogCreator  prev=factoryHolder.getLogCreator();
            factoryHolder.setLogCreator(creator);
            return prev;
        }
    }

    public static final Log getLog(Class<?> c) {
        return getLog(c.getName());
    }
    
    public static final Log getLog(String name) {
        return factoryHolder.getInstance(name);
    }

    LogFactory(LogCreator creator) {
        creatorHolder.set(creator);
    }

    LogCreator getLogCreator() {
        return creatorHolder.get();
    }

    void setLogCreator(LogCreator creator) {
        creatorHolder.set(creator);
    }

    public Log getInstance(Class<?> c) {
        return getInstance(c.getName());
    }
    
    public Log getInstance(String name) {
        return demandLogger(name);
    }

    Log demandLogger(String name) {
        String  loggerName=(name == null) ? "" : name;
        synchronized(loggersMap) {
            Log   logger=getCurrentLogger(loggerName);
            if (logger == null) {
                logger = createLogger(loggerName);
                loggersMap.put(loggerName, new LoggerWeakRef(logger, loggerName));
            }

            return logger;
        }
    }

    Log getCurrentLogger(String loggerName) {
        LoggerWeakRef    loggerRef=loggersMap.get(loggerName);
        if (loggerRef == null) {
            return null;
        }
        
        Log   logger=loggerRef.get();
        if (logger == null) {   // check if logger GC'ed
            loggersMap.remove(loggerName);
        }
        
        return logger;
    }

    Log createLogger(String name) {
        LogCreator  creator=getLogCreator();
        return creator.createLogger(name);
    }
    
    private static final ReferenceQueue<Log> loggerRefQueue= new ReferenceQueue<Log>();
    private final class LoggerWeakRef extends WeakReference<Log> {
        private final String name;
        @SuppressWarnings("synthetic-access")
        LoggerWeakRef(Log logger, String loggerName) {
            super(logger, loggerRefQueue);
            name = loggerName;
        }

        @Override
        public String toString() {
            return name;
        }
    }
}
