package net.community.chest.net.proto.text.pop3;

/**
 * <P>Copyright 2007 as per GPLv2</P>
 *
 * <P>Interface used to report message information resulting from UIDL command</P>
 *
 * @author Lyor G.
 * @since Sep 19, 2007 11:01:12 AM
 */
public interface POP3MsgUIDLHandler {
    /**
     * Called for each message to handle its UIDL value
     * @param msgNum message sequence number (starts at 1)
     * @param uidl UIDL value
     * @return 0 if successful - Note: error values cause the UIDL call to be aborted
     */
    int handleMsgUIDL (int msgNum, String uidl);
}
