/*
 *
 */
package net.community.chest.reflect;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.jar.Manifest;

import javax.management.ObjectName;

import net.community.chest.AbstractTestSupport;
import net.community.chest.lang.StringUtil;

import org.junit.Assert;
import org.junit.Test;

/**
 * <P>
 * Copyright as per GPLv2
 * </P>
 *
 * @author Lyor G.
 * @since Jun 14, 2012 1:21:18 PM
 */
public class ClassUtilTest extends AbstractTestSupport {
    private static final Class<?>[] WRAPPERS = { Byte.class, Short.class, Integer.class, Long.class, Float.class, Double.class,
            Boolean.class, Character.class };
    private static final Class<?>[] PRIMITIVES = { Integer.TYPE, Long.TYPE, Double.TYPE, Boolean.TYPE, Byte.TYPE, Short.TYPE,
            Float.TYPE, Character.TYPE };
    private static final Class<?>[] CORE_CLASSES = { String.class, Date.class, Map.class, Collection.class, List.class, Set.class,
            File.class, ObjectName.class, Arrays.class, Collections.class, Throwable.class, Exception.class, Error.class,
            RuntimeException.class };
    private final Class<?>[] PROJECT_CLASSES = { getClass(), ClassUtil.class, StringUtil.class };
    private static final List<String> DUMMY_CLASSES = Arrays.asList("foo.bar.Fubar", "baz", "dOublE", // case
                                                                                                        // sensitivity
            "java.lang.STRING", // ditto
            "Integer", // not fully qualified
            "java.lang.FooBraBaz", // no such class
            "javax.management.WeWish",// ditto
            "org.jboss.Main" // not in the class path
    );

    public ClassUtilTest ()
    {
        super();
    }

    @Test
    public void testGetOrIsPrimitiveClass ()
    {
        for (Class<?> clazz : PRIMITIVES) {
            String dataType = clazz.getName();
            assertSame("Mismatched class instances: " + dataType, clazz, ClassUtil.getPrimitiveClass(dataType));
            assertTrue("Not reported as primitive: " + dataType, ClassUtil.isPrimitiveClass(dataType));
        }
    }

    @Test
    public void testLoadClassByNameForPrimitives () throws ClassNotFoundException
    {
        testLoadClassByName(PRIMITIVES);
    }

    @Test
    public void testLoadClassByNameForPrimitiveWrappers () throws ClassNotFoundException
    {
        testLoadClassByName(WRAPPERS);
    }

    @Test
    public void testLoadClassByNameForCoreClasses () throws ClassNotFoundException
    {
        testLoadClassByName(CORE_CLASSES);
    }

    @Test
    public void testLoadClassByNameForProjectClasses () throws ClassNotFoundException
    {
        testLoadClassByName(PROJECT_CLASSES);
    }

    @Test
    public void testLoadNonExistingClassesByName ()
    {
        for (String fqcn : DUMMY_CLASSES) {
            try {
                Class<?> clazz = ClassUtil.loadClassByName(fqcn);
                fail("Unexpected success for " + fqcn + ": " + clazz.getName());
            }
            catch (ClassNotFoundException e) {
                // expected - ignored
            }
        }
    }

    @Test
    public void testIsPresentPrimitiveClasses ()
    {
        testIsPresent(PRIMITIVES);
    }

    @Test
    public void testIsPresentCoreClasses ()
    {
        testIsPresent(CORE_CLASSES);
    }

    @Test
    public void testIsPresentNonExistentClasses ()
    {
        testIsPresent(Boolean.FALSE, DUMMY_CLASSES);
    }

    @Test
    public void testLoadManifest () throws IOException
    {
        /*
         * Note: we cannot load our own manifest since when the test is run by
         * Maven this class is not contained in a JAR, so we would get a null
         */
        Manifest man = ClassUtil.loadContainerManifest(Assert.class);
        assertNotNull("No manifest loaded", man);

        System.out.println("Manifest attributes:");
        for (Map.Entry<?,?> attr : man.getMainAttributes().entrySet()) {
            Object name = attr.getKey(), value = attr.getValue();
            System.out.append('\t').append(String.valueOf(name)).append('=').println(value);
        }
    }

    @Test
    public void testNewProxyInstance () throws Exception
    {
        final String TEST_DATA = "testNewProxyInstance";
        StringBuilder sb = new StringBuilder();
        Appendable result = ClassUtil.newProxyInstance(Appendable.class, createDelegatingInvocationHandler(sb), Appendable.class);
        // this must be false since we only specified Appendable as the proxy
        // interface
        assertFalse("Result is a " + CharSequence.class.getSimpleName(), result instanceof CharSequence);

        Appendable actual = result.append(TEST_DATA);
        assertSame("Mismatched appended instance result", sb, actual);
        assertEquals("Mismatched builder contents", TEST_DATA, sb.toString());
    }

    @Test
    public void testNewProxyInstanceFailures ()
    {
        final Object dummy = new Object();
        final InvocationHandler h = createDelegatingInvocationHandler(dummy);
        for (Class<?>[] interfaces : new Class<?>[][] {
                null, // null array
                new Class[] {}, // empty array
                new Class[] { CharSequence.class, StringBuilder.class /*
                                                                     * not an
                                                                     * interface
                                                                     */, Appendable.class },
                new Class[] { CharSequence.class, null /* blank spot */, Appendable.class } }) {
            try {
                Object result = ClassUtil.newProxyInstance(Object.class, h, interfaces);
                fail("Unexpected proxy success for " + Arrays.toString(interfaces) + ": " + result);
            }
            catch (IllegalArgumentException e) {
                // expected - ignored
            }
        }
    }

    @Test
    public void testGetDefaultClassLoaderForThreadContext () {
        Thread        thread=Thread.currentThread();
        ClassLoader    clThread=thread.getContextClassLoader();
        if (clThread != null) {
            ClassLoader    cl=ClassUtil.getDefaultClassLoader(getClass());
            assertSame("Mismatched loaders", clThread, cl);
        }
    }

    @Test
    public void testGetDefaultClassLoaderForCoreClass () {
        Class<?>    TEST_CLASS=String.class;
        ClassLoader    clCore=TEST_CLASS.getClassLoader();
        if (clCore == null) {
            clCore = ClassLoader.getSystemClassLoader();
        }

        Thread        thread=Thread.currentThread();
        ClassLoader    clThread=thread.getContextClassLoader();
        try {
            thread.setContextClassLoader(null);
            ClassLoader    cl=ClassUtil.getDefaultClassLoader(TEST_CLASS);
            assertSame("Mismatched loaders", clCore, cl);
        } finally {
            thread.setContextClassLoader(clThread);
        }
    }

    private InvocationHandler createDelegatingInvocationHandler (final Object target)
    {
        return new InvocationHandler() {
            @Override
            public Object invoke (Object proxy, Method method, Object[] args) throws Throwable
            {
                return method.invoke(target, args);
            }
        };
    }

    private void testIsPresent (Class<?>... classList)
    {
        Collection<String> namesList = new ArrayList<String>(classList.length);
        for (Class<?> clazz : classList) {
            namesList.add(clazz.getName());
        }

        testIsPresent(Boolean.TRUE, namesList);
    }

    private void testIsPresent (Boolean expected, Collection<String> namesList)
    {
        ClassLoader cl = ClassUtil.getDefaultClassLoader(getClass());
        for (String fqcn : namesList) {
            assertEquals("Mismatched presence result: " + fqcn, expected, Boolean.valueOf(ClassUtil.isPresent(fqcn, cl)));
        }
    }

    private void testLoadClassByName (Class<?>... classList) throws ClassNotFoundException
    {
        ClassLoader cl = ClassUtil.getDefaultClassLoader(getClass());
        for (Class<?> clazz : classList) {
            assertSame("Mismatched class instances: " + clazz.getName(), clazz, ClassUtil.loadClassByName(cl, clazz.getName()));
        }
    }

}
