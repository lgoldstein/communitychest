package net.community.chest.net.proto.text.imap4;

import java.util.Collection;
import java.util.LinkedList;

/**
 * <P>Copyright 2007 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Sep 20, 2007 10:36:02 AM
 */
public class IMAP4FastResponse extends IMAP4TaggedResponse {
    /**
     *
     */
    private static final long serialVersionUID = -5727616698154219050L;
    private Collection<IMAP4FastMsgInfo>    _msgs    /* =null */;
    public Collection<IMAP4FastMsgInfo> getMessages ()
    {
        return _msgs;
    }

    public void setMessages (Collection<IMAP4FastMsgInfo> msgs)
    {
        _msgs = msgs;
    }
    /**
     * Adds the message to the collection
     * @param msgInfo instance to be added - ignored if null
     * @return updated messages collection - may be null/empty
     * if no previous messages and nothing added
     */
    public Collection<IMAP4FastMsgInfo> addMsgInfo (final IMAP4FastMsgInfo msgInfo)
    {
        Collection<IMAP4FastMsgInfo>    msgs=getMessages();

        if (msgInfo != null)
        {
            if (null == msgs)
            {
                setMessages(new LinkedList<IMAP4FastMsgInfo>());
                if (null == (msgs=getMessages()))    // should not happen
                    throw new IllegalStateException("No messages " + Collection.class.getName() + " instance though created");
            }
            msgs.add(msgInfo);
        }

        return msgs;
    }
    /**
     * Empty (default) constructor
     */
    public IMAP4FastResponse ()
    {
        super();
    }
    /**
     * Fully initialized constructor
     * @param rsp response info to be used for further initialization - ignored if null
     * @param msgs messages to be set - may be null/empty
     */
    public IMAP4FastResponse (IMAP4TaggedResponse rsp, Collection<IMAP4FastMsgInfo> msgs)
    {
        super(rsp);
        _msgs = msgs;
    }
    /**
     * Pre-initialized (partial) constructor
     * @param rsp response info to be used for further initialization - ignored if null
     */
    public IMAP4FastResponse (IMAP4TaggedResponse rsp)
    {
        this(rsp, null);
    }
    /**
     * Copy constructor
     * @param rsp response info to be used for further initialization - ignored if null
     */
    public IMAP4FastResponse (IMAP4FastResponse rsp)
    {
        this(rsp, (null == rsp) ? null : rsp.getMessages());
    }
}
