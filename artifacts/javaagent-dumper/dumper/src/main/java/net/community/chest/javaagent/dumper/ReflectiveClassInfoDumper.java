/*
 *
 */
package net.community.chest.javaagent.dumper;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;

/**
 * <P>Copyright as per GPLv2</P>
 * @author Lyor G.
 * @since Aug 3, 2011 2:27:38 PM
 */
final class ReflectiveClassInfoDumper extends AbstractClassInfoDumper {
    public ReflectiveClassInfoDumper (Appendable out)
    {
        super(out);
    }

    Appendable dump (final String className, final URL location, final Class<?> clazz) throws IOException
    {
        appendClassHeader(className, clazz.getModifiers(), location);

        // the class-init method is not visible via reflection so we simulate it
        startMethod("<clinit>")
            .appendMethodModifiers(Modifier.STATIC)
            .appendReturnTypeAttribute(void.class)
            .endMethod(false)
            ;
        dumpConstructors(clazz.getDeclaredConstructors());
        dumpMethods(clazz.getDeclaredMethods());
        appendClassFooter();

        return getOutgoingAppender();
    }

    protected ReflectiveClassInfoDumper dumpConstructors (final Constructor<?> ... ctors) throws IOException
    {
        if ((ctors == null) || (ctors.length <= 0))
            return this;

        for (final Constructor<?> ctor : ctors)
            dumpConstructor(ctor);
        return this;
    }

    protected ReflectiveClassInfoDumper dumpConstructor (final Constructor<?> ctor) throws IOException
    {
        if (ctor == null)
            return this;

        startMethod("<init>");
        appendMethodModifiers(ctor.getModifiers());
        appendReturnTypeAttribute(void.class);
        appendMethodArguments(ctor.getParameterTypes());
        return this;
    }

    protected ReflectiveClassInfoDumper dumpMethods (final Method ...methods ) throws IOException
    {
        if ((methods == null) || (methods.length <= 0))
            return this;

        for (final Method method : methods)
            dumpMethod(method);
        return this;
    }

    protected ReflectiveClassInfoDumper dumpMethod (final Method method) throws IOException
    {
        if (method == null)
            return this;

        startMethod(method.getName());
        appendMethodModifiers(method.getModifiers());
        appendReturnTypeAttribute(method.getReturnType());
        appendMethodArguments(method.getParameterTypes());
        return this;
    }
    /*
     * @see net.community.chest.javaagent.dumper.AbstractClassInfoDumper#appendMethodArguments(java.lang.Class<?>[])
     */
    @Override
    protected ReflectiveClassInfoDumper appendMethodArguments (final Class<?> ... args) throws IOException
    {
        if ((args != null) && (args.length > 0))
        {
            append(" >").println();
            super.appendMethodArguments(args);
            endMethod(true);
        }
        else
            endMethod(false);
        return this;
    }
}
