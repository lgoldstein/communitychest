/*
 * 
 */
package net.community.chest.jms.framework;

import javax.jms.JMSException;
import javax.jms.MessageConsumer;

/**
 * <P>Copyright 2010 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Jun 8, 2010 1:48:11 PM
 */
public interface XMessageConsumer extends MessageConsumer {
	void setMessageSelector (String msgSelector) throws JMSException;
}
