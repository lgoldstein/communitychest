/*
 * 
 */
package net.community.chest.jms.framework.message;

import java.io.Serializable;

import javax.jms.JMSException;
import javax.jms.ObjectMessage;

/**
 * <P>Copyright 2010 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Jun 8, 2010 4:07:40 PM
 */
public class SimpleObjectMessage extends AbstractMessage implements ObjectMessage {
	private Serializable	_object;
	public SimpleObjectMessage (Serializable object)
	{
		_object = object;
	}

	public SimpleObjectMessage ()
	{
		this(null);
	}
	/*
	 * @see javax.jms.ObjectMessage#getObject()
	 */
	@Override
	public Serializable getObject () throws JMSException
	{
		return _object;
	}
	/*
	 * @see javax.jms.ObjectMessage#setObject(java.io.Serializable)
	 */
	@Override
	public void setObject (Serializable object) throws JMSException
	{
		_object = object;
	}
	/*
	 * @see javax.jms.Message#clearBody()
	 */
	@Override
	public void clearBody () throws JMSException
	{
		setObject(null);
	}
}
