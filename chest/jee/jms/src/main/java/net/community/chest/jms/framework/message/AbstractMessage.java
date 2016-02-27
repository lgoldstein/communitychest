/*
 * 
 */
package net.community.chest.jms.framework.message;

import java.util.Enumeration;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageFormatException;

/**
 * <P>Copyright 2010 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Jun 8, 2010 3:24:15 PM
 */
public abstract class AbstractMessage implements XMessage {
	protected AbstractMessage ()
	{
		super();
	}

	private final Map<String,Object>	_propsMap=
		new TreeMap<String,Object>(String.CASE_INSENSITIVE_ORDER);
	protected final Map<String,Object> getPropertiesMap ()
	{
		return _propsMap;
	}
	/*
	 * @see javax.jms.Message#clearProperties()
	 */
	@Override
	public void clearProperties () throws JMSException
	{
		final Map<String,?>	pm=getPropertiesMap();
		if ((pm != null) && (pm.size() > 0))
			pm.clear();
	}
	/* NOTE: returns a COPY of the current properties in order to avoid internal changes
	 * @see net.community.chest.jms.framework.message.XMessage#getProperties()
	 */
	@Override
	public Properties getProperties () throws JMSException
	{
		final Properties	p=new Properties();
		final Map<?,?>		pm=getPropertiesMap();
		if ((null == pm) || (pm.size() <= 0))
			return p;

		p.putAll(pm);
		return p;
	}
	/**
	 * @param <P> Type of property being retrieved
	 * @param name Name of property
	 * @param propClass Property {@link Class} to cast result into
	 * @param failIfNotExist If TRUE then throws a {@link JMSException} if
	 * specified property not found. Otherwise returns <code>null</code> 
	 * @return The property value (<code>null</code> if not found and
	 * <I>failIfNotExist</I> is <code>false</code>)
	 * @throws JMSException If no property class provided or value not found
	 * and <I>failIfNotExist</I> is <code>true</code>
	 * @throws IllegalArgumentException If null/empty property name
	 * @throws MessageFormatException If value exists but its type is incompatible
	 * with the requested one
	 */
	protected <P> P getProperty (String name, Class<P> propClass, boolean failIfNotExist)
		throws JMSException, IllegalArgumentException, MessageFormatException
	{
		if ((null == name) || (name.length() <= 0))
			throw new IllegalArgumentException("getProperty()[" + ((null == propClass) ? null : propClass.getSimpleName()) + " no property name specified");
		if (null == propClass)
			throw new JMSException("getProperty(" + name + ")[] no property type specified");
		
		final Map<String,?>	pm=getPropertiesMap();
		final Object		pv=((null == pm) || (pm.size() <= 0)) ? null : pm.get(name);
		final Class<?>		vc=(null == pv) ? null : pv.getClass();
		if (null == vc)
		{
			if (failIfNotExist)
				throw new JMSException("getProperty(" + name + ")[" + propClass.getSimpleName() + "] N/A");
			return null;
		}

		if (!propClass.isAssignableFrom(vc))
			throw new MessageFormatException("getProperty(" + name + ")[" + propClass.getSimpleName() + "]"
								 		   + " value (" + pv + ") type mismatch: " + vc.getSimpleName());
		
		return propClass.cast(pv);
	}
	
	protected Object setProperty (String name, Object o)
		throws JMSException, IllegalArgumentException
	{
		if ((null == name) || (name.length() <= 0))
			throw new IllegalArgumentException("setProperty(" + name + ")[" + o + "] no property name specified");
		if (null == o)
			throw new JMSException("setProperty(" + name + ")[] no value provided");

		final Map<String,Object>	pm=getPropertiesMap();
		if (null == pm)
			throw new JMSException("setProperty(" + name + ")[" + o + "] no internal properties map");

		final Object	prev=pm.put(name, o);
		if (prev != null)	// debug breakpoint
			return prev;
		
		return null;
	}
	/*
	 * @see javax.jms.Message#getBooleanProperty(java.lang.String)
	 */
	@Override
	public boolean getBooleanProperty (String name) throws JMSException
	{
		final Boolean	v=getProperty(name, Boolean.class, true);
		return v.booleanValue();
	}
	/*
	 * @see javax.jms.Message#setBooleanProperty(java.lang.String, boolean)
	 */
	@Override
	public void setBooleanProperty (String name, boolean value)
			throws JMSException
	{
		setProperty(name, Boolean.valueOf(value));
	}
	/*
	 * @see javax.jms.Message#getByteProperty(java.lang.String)
	 */
	@Override
	public byte getByteProperty (String name) throws JMSException
	{
		final Number	v=getProperty(name, Number.class, true);
		return v.byteValue();
	}
	/*
	 * @see javax.jms.Message#setByteProperty(java.lang.String, byte)
	 */
	@Override
	public void setByteProperty (String name, byte value) throws JMSException
	{
		setProperty(name, Byte.valueOf(value));
	}
	/*
	 * @see javax.jms.Message#getDoubleProperty(java.lang.String)
	 */
	@Override
	public double getDoubleProperty (String name) throws JMSException
	{
		final Number	v=getProperty(name, Number.class, true);
		return v.doubleValue();
	}
	/*
	 * @see javax.jms.Message#setDoubleProperty(java.lang.String, double)
	 */
	@Override
	public void setDoubleProperty (String name, double value)
			throws JMSException
	{
		setProperty(name, Double.valueOf(value));
	}
	/*
	 * @see javax.jms.Message#getFloatProperty(java.lang.String)
	 */
	@Override
	public float getFloatProperty (String name) throws JMSException
	{
		final Number	v=getProperty(name, Number.class, true);
		return v.floatValue();
	}
	/*
	 * @see javax.jms.Message#setFloatProperty(java.lang.String, float)
	 */
	@Override
	public void setFloatProperty (String name, float value) throws JMSException
	{
		setProperty(name, Float.valueOf(value));
	}
	/*
	 * @see javax.jms.Message#getIntProperty(java.lang.String)
	 */
	@Override
	public int getIntProperty (String name) throws JMSException
	{
		final Number	v=getProperty(name, Number.class, true);
		return v.intValue();
	}
	/*
	 * @see javax.jms.Message#setIntProperty(java.lang.String, int)
	 */
	@Override
	public void setIntProperty (String name, int value) throws JMSException
	{
		setProperty(name, Integer.valueOf(value));
	}

	private String	_corrId;
	/*
	 * @see javax.jms.Message#getJMSCorrelationID()
	 */
	@Override
	public String getJMSCorrelationID () throws JMSException
	{
		return _corrId;
	}
	/*
	 * @see javax.jms.Message#setJMSCorrelationID(java.lang.String)
	 */
	@Override
	public void setJMSCorrelationID (String correlationID) throws JMSException
	{
		_corrId = correlationID;
	}
	/*
	 * @see javax.jms.Message#getJMSCorrelationIDAsBytes()
	 */
	@Override
	public byte[] getJMSCorrelationIDAsBytes () throws JMSException
	{
		final String	corrID=getJMSCorrelationID();
		if ((null == corrID) || (corrID.length() <= 0))
			return null;

		return corrID.getBytes();
	}
	/*
	 * @see javax.jms.Message#setJMSCorrelationIDAsBytes(byte[])
	 */
	@Override
	public void setJMSCorrelationIDAsBytes (byte[] correlationID)
			throws JMSException
	{
		if ((null == correlationID) || (correlationID.length <= 0))
			setJMSCorrelationID(null);
		else
			setJMSCorrelationID(new String(correlationID));
	}
	
	private int	_deliveryMode=DEFAULT_DELIVERY_MODE;
	/*
	 * @see javax.jms.Message#getJMSDeliveryMode()
	 */
	@Override
	public int getJMSDeliveryMode () throws JMSException
	{
		return _deliveryMode;
	}
	/*
	 * @see javax.jms.Message#setJMSDeliveryMode(int)
	 */
	@Override
	public void setJMSDeliveryMode (int deliveryMode) throws JMSException
	{
		_deliveryMode = deliveryMode;
	}

	private Destination	_dst;
	/*
	 * @see javax.jms.Message#getJMSDestination()
	 */
	@Override
	public Destination getJMSDestination () throws JMSException
	{
		return _dst;
	}
	/*
	 * @see javax.jms.Message#setJMSDestination(javax.jms.Destination)
	 */
	@Override
	public void setJMSDestination (Destination destination) throws JMSException
	{
		_dst = destination;
	}

	private long	_expiration;
	/*
	 * @see javax.jms.Message#getJMSExpiration()
	 */
	@Override
	public long getJMSExpiration () throws JMSException
	{
		return _expiration;
	}
	/*
	 * @see javax.jms.Message#setJMSExpiration(long)
	 */
	@Override
	public void setJMSExpiration (long expiration) throws JMSException
	{
		_expiration = expiration;
	}

	private String	_msgID;
	/*
	 * @see javax.jms.Message#getJMSMessageID()
	 */
	@Override
	public String getJMSMessageID () throws JMSException
	{
		return _msgID;
	}
	/*
	 * @see javax.jms.Message#setJMSMessageID(java.lang.String)
	 */
	@Override
	public void setJMSMessageID (String id) throws JMSException
	{
		_msgID = id;
	}

	private int	_priority=DEFAULT_PRIORITY;
	/*
	 * @see javax.jms.Message#getJMSPriority()
	 */
	@Override
	public int getJMSPriority () throws JMSException
	{
		return _priority;
	}
	/*
	 * @see javax.jms.Message#setJMSPriority(int)
	 */
	@Override
	public void setJMSPriority (int priority) throws JMSException
	{
		_priority = priority;
	}

	private boolean	_redelivered;
	/*
	 * @see javax.jms.Message#getJMSRedelivered()
	 */
	@Override
	public boolean getJMSRedelivered () throws JMSException
	{
		return _redelivered;
	}
	/*
	 * @see net.community.chest.jms.framework.message.XMessage#isJMSRedelivered()
	 */
	@Override
	public boolean isJMSRedelivered () throws JMSException
	{
		return getJMSRedelivered();
	}
	/*
	 * @see javax.jms.Message#setJMSRedelivered(boolean)
	 */
	@Override
	public void setJMSRedelivered (boolean redelivered) throws JMSException
	{
		_redelivered = redelivered;
	}

	private Destination	_replyTo;
	/*
	 * @see javax.jms.Message#getJMSReplyTo()
	 */
	@Override
	public Destination getJMSReplyTo () throws JMSException
	{
		return _replyTo;
	}
	/*
	 * @see javax.jms.Message#setJMSReplyTo(javax.jms.Destination)
	 */
	@Override
	public void setJMSReplyTo (Destination replyTo) throws JMSException
	{
		_replyTo = replyTo;
	}

	private long	_timestamp;
	/*
	 * @see javax.jms.Message#getJMSTimestamp()
	 */
	@Override
	public long getJMSTimestamp () throws JMSException
	{
		return _timestamp;
	}
	/*
	 * @see javax.jms.Message#setJMSTimestamp(long)
	 */
	@Override
	public void setJMSTimestamp (long timestamp) throws JMSException
	{
		_timestamp = timestamp;
	}

	private String	_type;
	/*
	 * @see javax.jms.Message#getJMSType()
	 */
	@Override
	public String getJMSType () throws JMSException
	{
		return _type;
	}
	/*
	 * @see javax.jms.Message#setJMSType(java.lang.String)
	 */
	@Override
	public void setJMSType (String type) throws JMSException
	{
		_type = type;
	}
	/*
	 * @see javax.jms.Message#getLongProperty(java.lang.String)
	 */
	@Override
	public long getLongProperty (String name) throws JMSException
	{
		final Number	v=getProperty(name, Number.class, true);
		return v.longValue();
	}
	/*
	 * @see javax.jms.Message#setLongProperty(java.lang.String, long)
	 */
	@Override
	public void setLongProperty (String name, long value) throws JMSException
	{
		setProperty(name, Long.valueOf(value));
	}
	/*
	 * @see javax.jms.Message#getObjectProperty(java.lang.String)
	 */
	@Override
	public Object getObjectProperty (String name) throws JMSException
	{
		return getProperty(name, Object.class, false);
	}
	/*
	 * @see javax.jms.Message#setObjectProperty(java.lang.String, java.lang.Object)
	 */
	@Override
	public void setObjectProperty (String name, Object value) throws JMSException
	{
		setProperty(name, value);
	}
	/*
	 * @see javax.jms.Message#getPropertyNames()
	 */
	@Override
	public Enumeration<Object> getPropertyNames () throws JMSException
	{
		final Properties	p=getProperties();
		return p.keys();
	}
	/*
	 * @see javax.jms.Message#getShortProperty(java.lang.String)
	 */
	@Override
	public short getShortProperty (String name) throws JMSException
	{
		final Number	v=getProperty(name, Number.class, true);
		return v.shortValue();
	}
	/*
	 * @see javax.jms.Message#setShortProperty(java.lang.String, short)
	 */
	@Override
	public void setShortProperty (String name, short value) throws JMSException
	{
		setProperty(name, Short.valueOf(value));
	}
	/*
	 * @see javax.jms.Message#getStringProperty(java.lang.String)
	 */
	@Override
	public String getStringProperty (String name) throws JMSException
	{
		return getProperty(name, String.class, false);
	}
	/*
	 * @see javax.jms.Message#setStringProperty(java.lang.String, java.lang.String)
	 */
	@Override
	public void setStringProperty (String name, String value)
			throws JMSException
	{
		if (null == value)
			throw new IllegalArgumentException("setStringProperty(" + name + ") no value");

		setProperty(name, value);
	}
	/*
	 * @see javax.jms.Message#propertyExists(java.lang.String)
	 */
	@Override
	public boolean propertyExists (String name) throws JMSException
	{
		if ((null == name) || (name.length() <= 0))
			throw new IllegalArgumentException("propertyExists() no name specified");

		final Map<String,?>	pm=getPropertiesMap();
		if ((null == pm) || (pm.size() <= 0))
			return false;

		return pm.containsKey(name);
	}

	private boolean	_acked;
	/*
	 * @see net.community.chest.jms.framework.message.XMessage#isAcknowledged()
	 */
	@Override
	public boolean isAcknowledged ()
	{
		return _acked;
	}
	/*
	 * @see net.community.chest.jms.framework.message.XMessage#setAcknowledged(boolean)
	 */
	@Override
	public void setAcknowledged (boolean ack)
	{
		_acked = ack;
	}
	/*
	 * @see javax.jms.Message#acknowledge()
	 */
	@Override
	public void acknowledge () throws JMSException
	{
		if (!isAcknowledged())
			setAcknowledged(true);
		
	}
}
