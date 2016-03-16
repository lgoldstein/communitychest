package net.community.chest.net.proto.text.pop3;

import java.io.Serializable;

import net.community.chest.CoVariantReturn;
import net.community.chest.lang.PubliclyCloneable;
import net.community.chest.lang.StringUtil;

/**
 * <P>Copyright 2007 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Sep 19, 2007 2:45:51 PM
 */
public class POP3MessageInfo
            implements Serializable,
                       PubliclyCloneable<POP3MessageInfo>,
                       Comparable<POP3MessageInfo> {
    /**
     *
     */
    private static final long serialVersionUID = 8205216100461909457L;
    /**
     * Message number - non-positive if not set
     */
    private int    _msgNum    /* =0 */;
    public int getMsgNum ()
    {
        return _msgNum;
    }

    public void setMsgNum (int msgNum)
    {
        _msgNum = msgNum;
    }
    /**
     * Message size (bytes) - negative if not set
     */
    private long    _msgSize=(-1L);
    public long getMsgSize ()
    {
        return _msgSize;
    }

    public void setMsgSize (long msgSize)
    {
        _msgSize = msgSize;
    }
    /**
     * Message UIDL - null/empty if not set
     */
    private String    _msgUIDL    /* =null */;
    public String getMsgUIDL ()
    {
        return _msgUIDL;
    }

    public void setMsgUIDL (String msgUIDL)
    {
        _msgUIDL = msgUIDL;
    }

    public POP3MessageInfo (int msgNum, long msgSize, String msgUIDL)
    {
        _msgNum = msgNum;
        _msgSize = msgSize;
        _msgUIDL = msgUIDL;
    }

    public POP3MessageInfo (int msgNum, long msgSize)
    {
        this(msgNum, msgSize, null);
    }

    public POP3MessageInfo (int msgNum)
    {
        this(msgNum, (-1L));
    }

    public POP3MessageInfo ()
    {
        super();
    }
    /*
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    @Override
    public int compareTo (POP3MessageInfo o)
    {
        return POP3MessageInfoComparator.ASCENDING.compare(this, o);
    }
    /*
     * @see java.lang.Object#clone()
     */
    @Override
    @CoVariantReturn
    public POP3MessageInfo clone () throws CloneNotSupportedException
    {
        return getClass().cast(super.clone());
    }
    /*
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals (Object obj)
    {
        if ((null == obj) || (!(obj instanceof POP3MessageInfo)))
            return false;
        if (this == obj)
            return true;

        return (0 == compareTo((POP3MessageInfo) obj));
    }
    /*
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode ()
    {
        return Math.max(getMsgNum(), 0)
             + (int) (Math.max(getMsgSize(), 0L) & 0x7FFFFFFF)
             + StringUtil.getDataStringHashCode(getMsgUIDL(), true)
             ;
    }
    /*
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString ()
    {
        final String    uidl=getMsgUIDL();
        final int        uLen=(null == uidl) ? 0 : uidl.length();
        return new StringBuilder(Math.max(uLen,0) + 64)
                .append("# ").append(getMsgNum())
                .append(";SIZE=").append(getMsgSize())
                .append(";UIDL=").append(uidl)
                .toString()
                ;
    }
}
