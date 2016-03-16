package net.community.chest.net.proto.text;

import java.io.Serializable;

import net.community.chest.CoVariantReturn;
import net.community.chest.lang.PubliclyCloneable;

/**
 * <P>Copyright 2007 as per GPLv2</P>
 *
 * <P>Represents a textual protocol server welcome line (e.g., SMTP, POP3, IMAP4)</P>
 *
 * @author Lyor G.
 * @since Jun 28, 2007 2:00:37 PM
 */
public class NetServerWelcomeLine implements Serializable, PubliclyCloneable<NetServerWelcomeLine> {
    /**
     *
     */
    private static final long serialVersionUID = -4640246389915705637L;
    /**
     * Actual welcome line (may be null)
     */
    private String _line /* =null */;
    public String getLine ()
    {
        return _line;
    }

    public void setLine (String line)
    {
        _line = line;
    }
    /**
     * Pre-initialized constructor
     * @param line initial line value - may be null/empty
     */
    public NetServerWelcomeLine (String line)
    {
        setLine(line);
    }
    /**
     * Default (empty) constructor
     */
    public NetServerWelcomeLine ()
    {
        this(null);
    }
    /*
     * returns welcome line data ("" if null)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString ()
    {
        return getLine();
    }
    /*
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object o)
    {
        if ((null == o) || (!(o instanceof NetServerWelcomeLine)))
            return false;

        final NetServerWelcomeLine    wl=(NetServerWelcomeLine) o;
        final String                l=getLine(), ol=wl.getLine();
        if ((null == l) || (null == ol))
            return (l == ol);

        return l.equals(ol);
    }
    /*
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode()
    {
        final String    line=getLine();
        return ((null == line) || (line.length() <= 0)) ? 0 : line.hashCode();
    }
    /*
     * @see java.lang.Object#clone()
     */
    @Override
    @CoVariantReturn
    public NetServerWelcomeLine clone () throws CloneNotSupportedException
    {
        return getClass().cast(super.clone());
    }
}
