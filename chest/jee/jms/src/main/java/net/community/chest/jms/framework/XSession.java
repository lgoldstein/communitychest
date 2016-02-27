/*
 * 
 */
package net.community.chest.jms.framework;

import javax.jms.JMSException;
import javax.jms.Session;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * <P>Useful for implementors - the "get"-ers are inherited</P>
 * @author Lyor G.
 * @since Sep 2, 2008 8:49:09 AM
 */
public interface XSession extends Session {
	void setAcknowledgeMode (int mode) throws JMSException;
	boolean isTransacted () throws JMSException;
	void setTransacted (boolean isTransacted) throws JMSException;
}
