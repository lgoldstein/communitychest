/*
 * 
 */
package net.community.chest.jms.framework.message;

import java.util.Properties;

import javax.jms.JMSException;
import javax.jms.Message;

/**
 * <P>Copyright 2010 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Jun 8, 2010 3:27:07 PM
 */
public interface XMessage extends Message {
	Properties getProperties () throws JMSException;
	boolean isJMSRedelivered () throws JMSException;
	boolean isAcknowledged ();
	void setAcknowledged (boolean ack);
}
