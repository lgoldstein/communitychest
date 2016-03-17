package org.springframework.instrument.classloading.tomcat;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarFile;

import javax.naming.directory.DirContext;

/**
 * @author lgoldstein
 */
public final class WebappClassLoaderInstrumentor {
    public static final String    WEBAPP_CLASS_LOADER_CLASS_PATH="org.apache.catalina.loader.WebappClassLoader";
    private static final Map<ClassLoader,TransformationContext>    contextsMap=
            Collections.synchronizedMap(new HashMap<ClassLoader,TransformationContext>());

    private WebappClassLoaderInstrumentor () {
        throw new UnsupportedOperationException("Construction N/A");
    }

    private static final Class<?>    loaderClass;
    private static final Field    jarsField;
    private static final Method    getResourcesMethod, setResourcesMethod, getContextNameMethod;
    static {
        try {
            loaderClass = Class.forName(WEBAPP_CLASS_LOADER_CLASS_PATH);
            jarsField = loaderClass.getDeclaredField("jarFiles");
            if (!jarsField.isAccessible()) {
                jarsField.setAccessible(true);
            }

            getResourcesMethod = loaderClass.getDeclaredMethod("getResources");
            setResourcesMethod = loaderClass.getDeclaredMethod("setResources", DirContext.class);
            getContextNameMethod  = loaderClass.getDeclaredMethod("getContextName");
        } catch(Exception e) {
            if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            } else {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * @param loader The root {@link ClassLoader}
     * @return The associated {@link TransformationContext} for the loader or
     * any of its parents
     */
    public static TransformationContext getTransformationContext (ClassLoader loader) {
        for (ClassLoader curLoader=loader; curLoader != null; curLoader = curLoader.getParent()) {
            TransformationContext    context=contextsMap.get(loader);
            if (context != null) {
                return context;
            }
        }

        return null;
    }

    public static TransformationContext adjustWebappClassLoader(ClassLoader loader) {
        try {
            if (!isWebappClassLoader(loader)) {
                throw new UnsupportedOperationException("Class loader (" + loader + ") incompatible with " + loaderClass.getName());
            }

            if (contextsMap.containsKey(loader)) {
                throw new IllegalStateException("Class loader already instrumented: " + loader);
            }

            TransformationContext    xform=new TransformationContext(loader);
            DirContext                dirContext=(DirContext) getResourcesMethod.invoke(loader);
            TransformingDirContext    proxyHandler=new TransformingDirContext(dirContext, xform);
            Class<?>[]                intfcs={ DirContext.class };
            setResourcesMethod.invoke(loader, Proxy.newProxyInstance(loader, intfcs, proxyHandler));

            JarFile[]    orgFiles=(JarFile[]) jarsField.get(loader),
                        xfrFiles=TransformingJarFile.createReplacementFiles(xform, orgFiles);
            jarsField.set(loader, xfrFiles);

            // close the replaced files
            for (JarFile jarFile : orgFiles) {
                jarFile.close();
            }

            if (contextsMap.put(loader, xform) != null) {
                throw new ConcurrentModificationException("Multiple instrumentations for " + loader);
            }

            return xform;
        } catch(Exception e) {
            if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            } else {
                throw new RuntimeException(e);
            }
        }
    }

    public static final String getContextName (ClassLoader loader) {
        try {
            if (!isWebappClassLoader(loader)) {
                return String.valueOf(loader);
            }

            return (String) getContextNameMethod.invoke(loader);
        } catch(Exception e) {
            return loader.getClass().getName();
        }
    }

    public static boolean isWebappClassLoader (ClassLoader loader) {
        if (loader == null) {
            return false;
        }

        Class<?>    loaderType=loader.getClass();
        return loaderClass.isAssignableFrom(loaderType);
    }
}
