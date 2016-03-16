package net.community.chest.net.proto.text.pop3;

import net.community.chest.reflect.ClassUtil;
import net.community.chest.util.map.IntegersMap;

/**
 * <P>Copyright 2007 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Sep 19, 2007 2:59:35 PM
 */
public class POP3MessageInfoResponse extends POP3Response
            implements POP3MsgSizeHandler, POP3MsgUIDLHandler {
    /**
     *
     */
    private static final long serialVersionUID = 3772428514735899135L;
    public POP3MessageInfoResponse ()
    {
        super();
    }

    public POP3MessageInfoResponse (boolean isOK, String rspLine)
    {
        super(isOK, rspLine);
    }

    public POP3MessageInfoResponse (boolean isOK)
    {
        super(isOK);
    }

    private IntegersMap<POP3MessageInfo>    _infosMap    /* =null */;
    /**
     * @return An {@link IntegersMap} of extracted {@link POP3MessageInfo}.
     * Key=message number, value=associated {@link POP3MessageInfo} - may be
     * null/empty if no record(s) extracted or error response
     */
    public IntegersMap<POP3MessageInfo> getMessagesMap ()
    {
        return _infosMap;
    }

    public void setMessagesMap (IntegersMap<POP3MessageInfo> mm)
    {
        _infosMap = mm;
    }
    // if one does not exists, then it is allocated
    public IntegersMap<POP3MessageInfo> initMessagesMap ()
    {
        IntegersMap<POP3MessageInfo>    mm=getMessagesMap();
        if (null == mm)
        {
            setMessagesMap(new IntegersMap<POP3MessageInfo>(POP3MessageInfo.class, 4, 4));
            if (null == (mm=getMessagesMap()))    // should not happen
                throw new IllegalStateException(ClassUtil.getExceptionLocation(getClass(), "initMessagesMap") + " no " + IntegersMap.class.getName() + " retrieved though allocated");
        }

        return mm;
    }
    /*
     * @see net.community.chest.net.proto.text.pop3.POP3Response#reset()
     */
    @Override
    public void reset ()
    {
        final IntegersMap<POP3MessageInfo>    mm=getMessagesMap();
        if (mm != null)
            mm.clear();

        super.reset();
    }

    public POP3MessageInfo initMessageInfo (final int msgNum)
    {
        if (msgNum <= 0)    // should not happen
            return null;

        final IntegersMap<POP3MessageInfo>    mm=initMessagesMap();
        if (null == mm)    // should not happen
            return null;

        POP3MessageInfo    msgInfo=mm.get(msgNum);
        if (null == msgInfo)
        {
            msgInfo = new POP3MessageInfo(msgNum);
            mm.put(msgNum, msgInfo);
        }

        return msgInfo;
    }
    /*
     * @see net.community.chest.net.proto.text.pop3.POP3MsgSizeHandler#handleMsgSize(int, long)
     */
    @Override
    public int handleMsgSize (int msgNum, long msgSize)
    {
        if ((msgNum <= 0) || (msgSize < 0L))
            return (-1);    // should not happen

        final POP3MessageInfo    msgInfo=initMessageInfo(msgNum);
        if (null == msgInfo)    // should not happen
            return (-2);

        final long    prevSize=msgInfo.getMsgSize();
        if (prevSize >= 0L)    // should not happen
            return (-3);

        msgInfo.setMsgSize(msgSize);
        return 0;
    }
    /*
     * @see net.community.chest.net.proto.text.pop3.POP3MsgUIDLHandler#handleMsgUIDL(int, java.lang.String)
     */
    @Override
    public int handleMsgUIDL (int msgNum, String uidl)
    {
        if ((msgNum <= 0) || (null == uidl) || (uidl.length() <= 0))
            return (-1);    // should not happen

        final POP3MessageInfo    msgInfo=initMessageInfo(msgNum);
        if (null == msgInfo)    // should not happen
            return (-2);

        final String    prevUidl=msgInfo.getMsgUIDL();
        if ((prevUidl != null) && (prevUidl.length() > 0))    // should not happen
            return (-3);

        msgInfo.setMsgUIDL(uidl);
        return 0;
    }

}
