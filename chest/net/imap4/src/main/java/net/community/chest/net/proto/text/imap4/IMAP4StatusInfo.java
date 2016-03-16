package net.community.chest.net.proto.text.imap4;

import java.io.IOException;

import net.community.chest.net.TextNetConnection;

/**
 * <P>Copyright 2007 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Sep 20, 2007 12:42:07 PM
 */
public class IMAP4StatusInfo extends IMAP4TaggedResponse {
    /**
     *
     */
    private static final long serialVersionUID = 5377238905047811194L;
    /* some known status modifiers */
    public static final String IMAP4_MESSAGES="MESSAGES";
        public static final char[] IMAP4_MESSAGESChars=IMAP4_MESSAGES.toCharArray();
    public static final String IMAP4_UIDNEXT="UIDNEXT";
        public static final char[] IMAP4_UIDNEXTChars=IMAP4_UIDNEXT.toCharArray();
    public static final String IMAP4_UIDVALIDITY="UIDVALIDITY";
        public static final char[] IMAP4_UIDVALIDITYChars=IMAP4_UIDVALIDITY.toCharArray();
    public static final String IMAP4_RECENT="RECENT";
        public static final char[] IMAP4_RECENTChars=IMAP4_RECENT.toCharArray();
    public static final String IMAP4_UNSEEN="UNSEEN";
        public static final char[] IMAP4_UNSEENChars=IMAP4_UNSEEN.toCharArray();

    /**
     * number of messages in folder
     */
    private int _numOfMsgs /* =0 */;
    /**
     * number of messages with "\Recent" flag set
     */
    private int _numRecent /* =0 */;
    /**
     * number of messages that do NOT have "\Seen" flag set
     */
    private int _numUnseen /* =0 */;
    /**
     * next UID to be assigned to mew message in mailbox (0 if unknown)
     */
    private long _UIDNext /* =0L */;
    /**
     * UID validity value assigned to mailbox (0 if unknown)
     */
    private long _UIDValidity /* =0L */;
    // default constructor
    public IMAP4StatusInfo ()
    {
        super();
    }
    // called by SelectionInfo response
    public IMAP4StatusInfo (IMAP4TaggedResponse rsp)
    {
        super(rsp);
    }

    public int getNumOfMsgs ()
    {
        return _numOfMsgs;
    }

    public void setNumOfMsgs (int numOfMsgs)
    {
        _numOfMsgs = numOfMsgs;
    }

    public int getNumRecent ()
    {
        return _numRecent;
    }

    public void setNumRecent (int numRecent)
    {
        _numRecent = numRecent;
    }

    public int getNumUnseen ()
    {
        return _numUnseen;
    }

    public void setNumUnseen (int numUnseen)
    {
        _numUnseen = numUnseen;
    }

    public long getUIDNext ()
    {
        return _UIDNext;
    }

    public void setUIDNext (long next)
    {
        _UIDNext = next;
    }

    public long getUIDValidity ()
    {
        return _UIDValidity;
    }

    public void setUIDValidity (long validity)
    {
        _UIDValidity = validity;
    }
    /*
     * @see net.community.chest.net.proto.text.imap4.IMAP4TaggedResponse#equals(java.lang.Object)
     */
    @Override
    public boolean equals (Object obj)
    {
        final Class<?>    oc=(obj == null) ? null : obj.getClass();
        if (oc != getClass())
            return false;
        if (this == obj)
            return true;

        final IMAP4StatusInfo    stInfo=(IMAP4StatusInfo) obj;
        if (!isSameResponse(stInfo))
            return false;

        return (stInfo.getNumOfMsgs() == getNumOfMsgs())
            && (stInfo.getNumRecent() == getNumRecent())
            && (stInfo.getNumUnseen() == getNumUnseen())
            && (stInfo.getUIDNext() == getUIDNext())
            && (stInfo.getUIDValidity() == getUIDValidity())
            ;
    }
    /*
     * @see net.community.chest.net.proto.text.imap4.IMAP4TaggedResponse#hashCode()
     */
    @Override
    public int hashCode ()
    {
        return super.hashCode()
            + adjustHashCode(getNumOfMsgs())
            + adjustHashCode(getNumRecent())
            + adjustHashCode(getNumUnseen())
            + adjustHashCode(getUIDNext())
            + adjustHashCode(getUIDValidity())
            ;
    }
    /*
     * @see net.community.chest.net.proto.text.imap4.IMAP4TaggedResponse#reset()
     */
    @Override
    public void reset ()
    {
        super.reset();

        setNumOfMsgs(0);
        setNumRecent(0);
        setNumUnseen(0);
        setUIDNext(0L);
        setUIDValidity(0L);
    }
    /**
     * @return a similar string to the STATUS response
     */
    public String getStatusString ()
    {
        return "(" + IMAP4_MESSAGES + " " + _numOfMsgs
                + " " + IMAP4_RECENT + " " + _numRecent
                + " " + IMAP4_UNSEEN + " " + _numUnseen
                + " " + IMAP4_UIDNEXT + " " + _UIDNext
                + " " + IMAP4_UIDVALIDITY + " " + _UIDValidity
            + ")";
    }

    public static final IMAP4StatusInfo getFinalResponse (final TextNetConnection conn, final int tagValue) throws IOException
    {
        return (IMAP4StatusInfo) (new IMAP4FolderStatusRspHandler(conn)).handleResponse(tagValue);
    }
}
