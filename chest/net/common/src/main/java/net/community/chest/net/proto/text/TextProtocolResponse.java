package net.community.chest.net.proto.text;

import java.io.Serializable;

import net.community.chest.CoVariantReturn;
import net.community.chest.lang.PubliclyCloneable;

/**
 * <P>Copyright 2007 as per GPLv2</P>
 *
 * <P>Provides some useful base functionality for textual protocols responses
 * implementations</P>
 *
 * @author Lyor G.
 * @since Sep 19, 2007 10:26:56 AM
 */
public abstract class TextProtocolResponse implements Serializable, PubliclyCloneable<TextProtocolResponse> {
    /**
     *
     */
    private static final long serialVersionUID = 5568071825722235098L;
    protected TextProtocolResponse ()
    {
        super();
    }
    /**
     * @return TRUE if this instance represents an OK response for the
     * underlying protocol
     */
    public abstract boolean isOKResponse ();
    /**
     * Full response line (OK or otherwise) as received from the server
     */
    private String    _rspLine    /* =null */;
    public String getResponseLine ()
    {
        return _rspLine;
    }

    public void setResponseLine (String rspLine)
    {
        _rspLine = rspLine;
    }
    /**
     * Re-initializes the current contents to some default state (usually
     * not OK and empty response line)
     */
    public void reset ()
    {
        setResponseLine(null);
    }
    /*
     * @see java.lang.Object#clone()
     */
    @Override
    @CoVariantReturn
    public TextProtocolResponse clone () throws CloneNotSupportedException
    {
        return getClass().cast(super.clone());
    }
    /* Returns response line by default
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString ()
    {
        return getResponseLine();
    }

    public boolean isSameResponse (final TextProtocolResponse rsp)
    {
        if (rsp == null)
            return false;
        if (rsp == this)
            return true;

        return isOKResponse() == rsp.isOKResponse();
    }
    /* Checks only the isOKResponseState
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals (Object obj)
    {
        final Class<?>    oc=(obj == null) ? null : obj.getClass();
        if (oc != getClass())
            return false;

        return isSameResponse((TextProtocolResponse) obj);
    }
    /*
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode ()
    {
        return isOKResponse() ? 1 : 0;
    }
}
