package net.community.chest.net.proto.text.smtp;

/**
 * <P>Copyright 2007 as per GPLv2</P>
 *
 * <P>Used to handle ESMTP capabilities report on EHLO response</P>
 * 
 * @author Lyor G.
 * @since Sep 20, 2007 7:26:42 AM
 */
public interface ESMTPCapabilityHandler {
	/**
	 * "Callback" used to inform about an extended capability
	 * @param cap capability string
	 * @return 0 if OK to continue (NOTE: if error code returned, connection may be aborted)
	 */
	int handleCapability (String cap);
}
