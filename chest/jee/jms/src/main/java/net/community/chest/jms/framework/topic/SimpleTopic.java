/*
 * 
 */
package net.community.chest.jms.framework.topic;

import java.io.Serializable;

import javax.jms.JMSException;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Sep 2, 2008 11:14:28 AM
 */
public class SimpleTopic implements XTopic, Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -1924111755828602296L;
	public SimpleTopic ()
	{
		super();
	}
	/**
	 * Last set topic name - default=null/empty
	 */
	private String	_tName /* =null */;
	/*
	 * @see javax.jms.Topic#getTopicName()
	 */
	@Override
	public String getTopicName () throws JMSException
	{
		return _tName;
	}
	/*
	 * @see net.community.chest.jms.topic.XTopic#setTopicName(java.lang.String)
	 */
	@Override
	public void setTopicName (String name) throws JMSException
	{
		_tName = name;
	}

	public SimpleTopic (final String name)
	{
		_tName = name;
	}
}
