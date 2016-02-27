/*
 * 
 */
package net.community.chest.jms.framework.queue;

import javax.jms.JMSException;
import javax.jms.Queue;
import javax.jms.QueueSender;

/**
 * <P>Copyright 2010 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Jun 8, 2010 2:05:57 PM
 */
public interface XQueueSender extends QueueSender {
	void setQueue (Queue q) throws JMSException;
}
