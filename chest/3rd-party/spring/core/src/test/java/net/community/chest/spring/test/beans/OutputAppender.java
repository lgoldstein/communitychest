/*
 *
 */
package net.community.chest.spring.test.beans;

import java.io.IOException;
import java.io.StreamCorruptedException;

import org.springframework.beans.factory.annotation.Required;

/**
 * @author Lyor G.
 * @since Jul 20, 2010 9:10:29 AM
 */
public class OutputAppender implements Appendable {
    private Appendable    _out;
    public OutputAppender ()
    {
        super();
    }

    @Required
    public void setWriteToErr (boolean useStderr)
    {
        if (_out != null)
            throw new IllegalStateException("setWriteToErr(" + useStderr + ") already initialized");

        _out = useStderr ? System.err : System.out;
    }
    /*
     * @see java.lang.Appendable#append(java.lang.CharSequence, int, int)
     */
    @Override
    public Appendable append (CharSequence csq, int start, int end)
            throws IOException
    {
        if (null == _out)
            throw new StreamCorruptedException("append(" + csq + ") not initialized");
        _out.append(csq, start, end);
        return this;
    }
    /*
     * @see java.lang.Appendable#append(java.lang.CharSequence)
     */
    @Override
    public Appendable append (CharSequence csq) throws IOException
    {
        return append(csq, 0, (null == csq) ? 0 : csq.length());
    }
    /*
     * @see java.lang.Appendable#append(char)
     */
    @Override
    public Appendable append (char c) throws IOException
    {
        if (null == _out)
            throw new StreamCorruptedException("append(" + String.valueOf(c) + ") not initialized");
        _out.append(c);
        return this;
    }
}
