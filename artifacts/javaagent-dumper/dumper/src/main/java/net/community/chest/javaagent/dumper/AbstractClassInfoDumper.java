/*
 *
 */
package net.community.chest.javaagent.dumper;

import java.io.IOException;
import java.net.URL;

import net.community.chest.javaagent.dumper.data.InfoUtils;

/**
 * <P>Copyright as per GPLv2</P>
 * @author Lyor G.
 * @since Aug 3, 2011 2:10:38 PM
 */
public abstract class AbstractClassInfoDumper implements Appendable {
    private final Appendable    _out;
    protected final Appendable getOutgoingAppender ()
    {
        return _out;
    }

    protected AbstractClassInfoDumper (Appendable out)
    {
        _out = out;
    }
    /*
     * @see java.lang.Appendable#append(java.lang.CharSequence)
     */
    @Override
    public AbstractClassInfoDumper append (CharSequence csq) throws IOException
    {
        return append(csq, 0, (csq == null) ? 0 : csq.length());
    }
    /*
     * @see java.lang.Appendable#append(java.lang.CharSequence, int, int)
     */
    @Override
    public AbstractClassInfoDumper append (CharSequence csq, int start, int end) throws IOException
    {
        if (start < end)
            _out.append(csq, start, end);
        return this;
    }
    /*
     * @see java.lang.Appendable#append(char)
     */
    @Override
    public AbstractClassInfoDumper append (char c) throws IOException
    {
        _out.append(c);
        return this;
    }

    protected AbstractClassInfoDumper appendClassHeader (final String name, final int mod, final URL url) throws IOException
    {
        return InfoUtils.appendClassHeader(this, name, mod, url).println();
    }

    protected AbstractClassInfoDumper appendClassFooter () throws IOException
    {
        return InfoUtils.appendClassFooter(this).println();
    }

    protected AbstractClassInfoDumper startMethod (final String name) throws IOException
    {
        return InfoUtils.startMethod(append('\t'), name);
    }

    protected AbstractClassInfoDumper endMethod (final boolean hasParams) throws IOException
    {
        if (hasParams)
            append('\t');
        return InfoUtils.endMethod(this, hasParams).println();
    }

    protected AbstractClassInfoDumper appendMethodModifiers (final int mod) throws IOException
    {
        return InfoUtils.appendMethodModifiers(this, mod);
    }

    protected AbstractClassInfoDumper appendReturnTypeAttribute (Class<?> type) throws IOException
    {
        return appendReturnTypeAttribute(type.getName());
    }

    protected AbstractClassInfoDumper appendReturnTypeAttribute (String type) throws IOException
    {
        return InfoUtils.appendReturnTypeAttribute(this, type);
    }

    protected AbstractClassInfoDumper appendMethodArguments (final Class<?> ... args) throws IOException
    {
        if ((args == null) || (args.length <= 0))
            return this;

        for (final Class<?>    argType : args)
            appendParamTypeAttribute(argType);
        return this;
    }

    protected AbstractClassInfoDumper appendParamTypeAttribute (Class<?> type)  throws IOException
    {
        return appendParamTypeAttribute(type.getName());
    }

    protected AbstractClassInfoDumper appendParamTypeAttribute (String type) throws IOException
    {
        return InfoUtils.appendParamTypeAttribute(append("\t\t"), type).println();
    }

    protected AbstractClassInfoDumper appendAttribute (String name, String value) throws IOException
    {
        return InfoUtils.appendAttribute(this, name, value);
    }

    public AbstractClassInfoDumper println () throws IOException
    {
        return InfoUtils.println(this);
    }
}
