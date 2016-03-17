package org.springframework.instrument.classloading.tomcat;

import java.io.IOException;
import java.io.InputStream;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.naming.Name;
import javax.naming.NamingException;
import javax.naming.directory.DirContext;

import org.apache.naming.resources.Resource;
import org.apache.naming.resources.ResourceAttributes;
import org.springframework.util.Assert;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.StringUtils;

/**
 * This is a reverse engineering of the code in the WebappClassLoader#findResourceInternal code
 * @author lgoldstein
 */
public class TransformingDirContext implements InvocationHandler {
    protected final Logger logger=Logger.getLogger(getClass().getName());
    protected final DirContext    delegate;
    protected final TransformationContext    xformers;
    protected final Set<String>    identityResources=
            Collections.synchronizedSet(new TreeSet<String>());
    protected final Map<String,Integer>    resMap=
        Collections.synchronizedMap(new TreeMap<String,Integer>());

    public TransformingDirContext(DirContext dirContext, TransformationContext xformContext) {
        this.delegate = dirContext;
        this.xformers = xformContext;
    }

    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        String    name=method.getName();
        if ("lookup".equals(name)) {
            return handleLookup(args);
        }
        else if ("getAttributes".equals(name)) {
            return handleGetAttributes(args);
        }
        else {
            return method.invoke(delegate, args);
        }
    }

    protected Object handleGetAttributes(Object ... args)
            throws NamingException {
        Assert.notNull(args, "No arguments to get attributes");
        Assert.isTrue(args.length == 1, "Too many attributes arguments");

        Object    nameArg=args[0];
        Assert.notNull(nameArg, "No name to lookup");
        if (nameArg instanceof Name) {
            return delegate.getAttributes((Name) nameArg);
        }

        Assert.isInstanceOf(String.class, nameArg, "Attributes name is not a string");
        String    name=(String) nameArg;
        Object    result=delegate.getAttributes(name);
        if (!(result instanceof ResourceAttributes)) {
            return result;
        }

        Number    dataLength=resMap.get(name);    // check if any previous transformation occurred
        if (dataLength == null) {
            return result;
        }

        // if transformed, then adjust the reported data length
        ResourceAttributes    attrs=(ResourceAttributes) result;
        attrs.setContentLength(dataLength.longValue());
        return attrs;
    }

    protected Object handleLookup(Object ... args)
            throws NamingException, IOException, IllegalClassFormatException {
        Assert.notNull(args, "No arguments to lookup");
        Assert.isTrue(args.length == 1, "Too many lookup arguments");

        Object    nameArg=args[0];
        Assert.notNull(nameArg, "No name to lookup");
        if (nameArg instanceof Name) {
            return delegate.lookup((Name) nameArg);
        }

        Assert.isInstanceOf(String.class, nameArg, "Lookup name is not a string");
        String    name=(String) nameArg;
        Object    result=delegate.lookup(name);
        if (xformers.isEmpty() || (!name.endsWith(".class")) || (!(result instanceof Resource))) {
            return result;
        }

        // check if already handled this request in the past and did nothing
        if (identityResources.contains(name)) {
            return result;
        }

        // check if any transformation occurred
        Resource    resource=createReplacementResources(createClassName(name), (Resource) result);
        if (resource == result) {
            if (logger.isLoggable(Level.FINE)) {
                logger.fine("handleLookup(" + name + ") no transformation");
            }

            identityResources.add(name);
            return result;
        }

        byte[]    data=resource.getContent();
        resMap.put(name, Integer.valueOf(data.length));
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("handleLookup(" + name + ") cache transformed data size: " + data.length);
        }

        return resource;
    }

    protected Resource createReplacementResources (String className, Resource resource)
            throws IOException, IllegalClassFormatException {
        InputStream    in=resource.streamContent();
        byte[]        data;
        try {
            data = FileCopyUtils.copyToByteArray(in);
        } finally {
            in.close();
        }

        byte[]    xformData=xformers.applyTransformers(className, data);
        if (xformData == data) {
            return resource;    // nothing changed
        }

        return new TransformedResource(xformData);
    }

    protected String createClassName (String path) {
        Assert.hasText(path, "No path specified");
        String    classPath=cleanUpPath(path).replace('/', '.');
        return StringUtils.stripFilenameExtension(classPath);    // strip the ".class"
    }

    private static final Collection<String>    PREFIXES=Arrays.asList("/WEB-INF", "/classes");
    protected String cleanUpPath (String path) {
        String    lastPath=path;
        // strip known prefixes
        for (String    stripPath=stripPathPrefix(lastPath); stripPath != lastPath; stripPath=stripPathPrefix(lastPath)) {
            lastPath = stripPath;
        }

        // strip leading separator
        if (lastPath.charAt(0) == '/') {
            return lastPath.substring(1);
        }

        return lastPath;
    }

    protected String stripPathPrefix (String path) {
        for (String prefix : PREFIXES) {
            if (path.startsWith(prefix)) {
                return path.substring(prefix.length());
            }
        }

        return path;
    }
}
