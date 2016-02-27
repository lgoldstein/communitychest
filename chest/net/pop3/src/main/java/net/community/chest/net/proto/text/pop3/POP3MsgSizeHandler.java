package net.community.chest.net.proto.text.pop3;

/**
 * <P>Copyright 2007 as per GPLv2</P>
 *
 * <P>Interface used to report message size information resulting from the LIST command</P>
 * 
 * @author Lyor G.
 * @since Sep 19, 2007 11:06:08 AM
 */
public interface POP3MsgSizeHandler {
	/**
	 * Called for each message to handle its size
	 * @param msgNum message sequence number (starts at 1)
	 * @param msgSize message size (in octets)
	 * @return 0 if successful - Note: error values cause the LIST call to be aborted
	 */
	int handleMsgSize (int msgNum, long msgSize);
}
