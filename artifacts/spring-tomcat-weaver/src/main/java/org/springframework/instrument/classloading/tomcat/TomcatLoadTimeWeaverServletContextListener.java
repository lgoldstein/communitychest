package org.springframework.instrument.classloading.tomcat;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * @author lgoldstein
 */
public class TomcatLoadTimeWeaverServletContextListener implements ServletContextListener {
    public TomcatLoadTimeWeaverServletContextListener() {
        super();
    }

    public void contextInitialized(ServletContextEvent sce) {
        Thread                    thread=Thread.currentThread();
        ClassLoader                classLoader=thread.getContextClassLoader();
        WebappClassLoaderInstrumentor.adjustWebappClassLoader(classLoader);
    }

    public void contextDestroyed(ServletContextEvent sce) {
        // do nothing
    }

}
