/*
 *
 */
package net.community.chest.javaagent.dumper;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URL;

import org.apache.bcel.classfile.ClassParser;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.Type;

/**
 * <P>Copyright as per GPLv2</P>
 * @author Lyor G.
 * @since Aug 2, 2011 3:57:41 PM
 */
final class BcelClassInfoDumper extends AbstractClassInfoDumper {
    BcelClassInfoDumper (final Appendable out)
    {
        super(out);
    }

    Appendable dump (final String className, final URL location, final byte[] classfileBuffer) throws IOException
    {
        final ClassParser    parser=new ClassParser(new ByteArrayInputStream(classfileBuffer), className);
        final JavaClass        jc=parser.parse();
        appendClassHeader(className, jc.getModifiers(), location);
        dumpMethods(jc.getMethods());
        appendClassFooter();

        return getOutgoingAppender();
    }

    protected BcelClassInfoDumper dumpMethods (final Method ... methods) throws IOException
    {
        if ((methods == null) || (methods.length <= 0))
            return this;

        for (final Method method : methods)
            dumpMethod(method);
        return this;
    }

    protected BcelClassInfoDumper dumpMethod (final Method method) throws IOException
    {
        if (method == null)
            return this;

        startMethod(method.getName());
        appendMethodModifiers(method.getModifiers());
        appendReturnTypeAttribute(method.getReturnType());

        final Type[]    args=method.getArgumentTypes();
        if ((args != null) && (args.length > 0))
        {
            append(" >").println();
            appendMethodArguments(args).endMethod(true);
        }
        else
            endMethod(false);
        return this;
    }

    protected BcelClassInfoDumper appendMethodArguments (final Type ... args) throws IOException
    {
        if ((args == null) || (args.length <= 0))
            return this;

        for (final Type argType : args)
            appendParamTypeAttribute(argType);

        return this;
    }

    protected BcelClassInfoDumper appendReturnTypeAttribute (final Type type) throws IOException
    {
        if (type == null)
            return this;

        appendReturnTypeAttribute(type.toString());
        return this;
    }

    protected BcelClassInfoDumper appendParamTypeAttribute (Type type) throws IOException
    {
        if (type == null)
            return this;

        appendParamTypeAttribute(type.toString());
        return this;
    }
}
