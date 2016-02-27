/*
 * 
 */
package net.community.chest.jms.framework.queue;

import javax.jms.JMSException;
import javax.jms.Queue;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 * <P>Useful for implementors - the "get"-ers are inherited</P>
 * 
 * @author Lyor G.
 * @since Sep 2, 2008 11:16:42 AM
 */
public interface XQueue extends Queue {
	/**
	 * <code>getQueueName()</code> is supplied by the {@link Queue} interface
	 * @param qName current queue name
	 * @throws JMSException if internal server error
	 */
	void setQueueName (String qName) throws JMSException;
}
