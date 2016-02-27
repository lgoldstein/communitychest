/*
 * 
 */
package net.community.chest.jms.framework;

import javax.jms.ConnectionConsumer;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.ServerSessionPool;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * <P>Useful for implementors</P>
 * 
 * @author Lyor G.
 * @since Sep 2, 2008 8:46:55 AM
 */
public interface XConnectionConsumer extends ConnectionConsumer {
	/**
	 * @return The {@link Destination} that was used to create this consumer (may be null)
	 * @throws JMSException if internal error
	 */
	Destination getDestination () throws JMSException;
	void setDestination (Destination dst) throws JMSException;
	/**
	 * @return message selector that was used to create this consumer (may be null/empty)
	 * @throws JMSException if internal error
	 */
	String getMessageSelector () throws JMSException;
	void setMessageSelector (String msgSelector) throws JMSException;
	/**
	 * @return max. reported messages used to create this consumer (<=0 if not set)
	 * @throws JMSException if internal error
	 */
	int getMaxMessages () throws JMSException;
	void setMaxMessages (int maxMsgs) throws JMSException;
	/**
	 * @param sessionPool session pool that was used when consumer was created
	 * @throws JMSException if internal error
	 * @see ConnectionConsumer#getServerSessionPool()
	 */ 
	void setServerSessionPool (ServerSessionPool sessionPool) throws JMSException;
	/**
	 * Starts the receiver on the set queue - if already running then
	 * nothing is done and method succeeds
	 * @throws JMSException
	 * @see #isOpen()
	 * @see #close()
	 */
	void open () throws JMSException;
	/**
	 * @return TRUE if receiver is currently started
	 * @throws JMSException if unable to return correct internal state
	 */
	boolean isOpen () throws JMSException;
}
