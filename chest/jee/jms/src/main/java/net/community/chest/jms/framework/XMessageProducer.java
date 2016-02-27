/*
 * 
 */
package net.community.chest.jms.framework;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageProducer;

/**
 * <P>Copyright 2010 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Jun 8, 2010 1:55:57 PM
 */
public interface XMessageProducer extends MessageProducer {
	void setDestination (Destination dst) throws JMSException;
	boolean isDisableMessageID () throws JMSException;
	boolean isDisableMessageTimestamp () throws JMSException;
}
