/*
 * 
 */
package net.community.chest.jms.framework.message;

import java.util.Enumeration;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.MessageFormatException;

/**
 * <P>Copyright 2010 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Jun 10, 2010 9:20:30 AM
 */
public class SimpleMapMessage extends AbstractMessage implements MapMessage {
	private final Map<String,Object>	_valsMap;
	protected final Map<String,Object> getValuesMap ()
	{
		return _valsMap;
	}
	/**
	 * @param <P> Type of value being retrieved
	 * @param name Name of value
	 * @param valClass Value {@link Class} to cast result into
	 * @param failIfNotExist If TRUE then throws a {@link JMSException} if
	 * specified property not found. Otherwise returns <code>null</code> 
	 * @return The value instance (<code>null</code> if not found and
	 * <I>failIfNotExist</I> is <code>false</code>)
	 * @throws JMSException If no value class provided or value not found
	 * and <I>failIfNotExist</I> is <code>true</code>
	 * @throws IllegalArgumentException If null/empty property name
	 * @throws MessageFormatException If value exists but its type is incompatible
	 * with the requested one
	 */
	protected <P> P getValue (String name, Class<P> valClass, boolean failIfNotExist)
		throws JMSException, IllegalArgumentException, MessageFormatException
	{
		if ((null == name) || (name.length() <= 0))
			throw new IllegalArgumentException("getValue()[" + ((null == valClass) ? null : valClass.getSimpleName()) + " no property name specified");
		if (null == valClass)
			throw new JMSException("getValue(" + name + ")[] no value type specified");
		
		final Map<String,?>	pm=getValuesMap();
		final Object		pv=((null == pm) || (pm.size() <= 0)) ? null : pm.get(name);
		final Class<?>		vc=(null == pv) ? null : pv.getClass();
		if (null == vc)
		{
			if (failIfNotExist)
				throw new JMSException("getValue(" + name + ")[" + valClass.getSimpleName() + "] N/A");
			return null;
		}

		if (!valClass.isAssignableFrom(vc))
			throw new MessageFormatException("getValue(" + name + ")[" + valClass.getSimpleName() + "]"
								 		   + " value (" + pv + ") type mismatch: " + vc.getSimpleName());
		
		return valClass.cast(pv);
	}
	
	protected Object setValue (String name, Object o)
		throws JMSException, IllegalArgumentException
	{
		if ((null == name) || (name.length() <= 0))
			throw new IllegalArgumentException("setValue(" + name + ")[" + o + "] no property name specified");
		if (null == o)
			throw new JMSException("setValue(" + name + ")[] no value provided");

		final Map<String,Object>	pm=getValuesMap();
		if (null == pm)
			throw new JMSException("setValue(" + name + ")[" + o + "] no internal properties map");

		final Object	prev=pm.put(name, o);
		if (prev != null)	// debug breakpoint
			return prev;
		
		return null;
	}

	public SimpleMapMessage (Map<String,?> valsMap)
	{
		_valsMap = new TreeMap<String,Object>(String.CASE_INSENSITIVE_ORDER);
		
		if ((valsMap != null) && (valsMap.size() > 0))
			_valsMap.putAll(valsMap);
	}

	public SimpleMapMessage ()
	{
		this(null);
	}
	/*
	 * @see javax.jms.MapMessage#getBoolean(java.lang.String)
	 */
	@Override
	public boolean getBoolean (String name) throws JMSException
	{
		final Boolean	v=getValue(name, Boolean.class, true);
		return v.booleanValue();
	}
	/*
	 * @see javax.jms.MapMessage#setBoolean(java.lang.String, boolean)
	 */
	@Override
	public void setBoolean (String name, boolean value) throws JMSException
	{
		setValue(name, Boolean.valueOf(value));
	}
	/*
	 * @see javax.jms.MapMessage#getByte(java.lang.String)
	 */
	@Override
	public byte getByte (String name) throws JMSException
	{
		final Number	v=getValue(name, Number.class, true);
		return v.byteValue();
	}
	/*
	 * @see javax.jms.MapMessage#setByte(java.lang.String, byte)
	 */
	@Override
	public void setByte (String name, byte value) throws JMSException
	{
		setValue(name, Byte.valueOf(value));
	}
	/*
	 * @see javax.jms.MapMessage#getBytes(java.lang.String)
	 */
	@Override
	public byte[] getBytes (String name) throws JMSException
	{
		return getValue(name, byte[].class, false);
	}
	/*
	 * @see javax.jms.MapMessage#setBytes(java.lang.String, byte[])
	 */
	@Override
	public void setBytes (String name, byte[] value) throws JMSException
	{
		setValue(name, value);
	}
	/*
	 * @see javax.jms.MapMessage#setBytes(java.lang.String, byte[], int, int)
	 */
	@Override
	public void setBytes (String name, byte[] value, int offset, int length)
			throws JMSException
	{
		if ((offset > 0) || (length < value.length))
		{
			final byte[]	vc=new byte[length];
			if (length > 0)
				System.arraycopy(value, offset, vc, 0, length);
			setBytes(name, vc);
		}
		else
			setBytes(name, value);
	}
	/*
	 * @see javax.jms.MapMessage#getChar(java.lang.String)
	 */
	@Override
	public char getChar (String name) throws JMSException
	{
		final Character	v=getValue(name, Character.class, true);
		return v.charValue();
	}
	/*
	 * @see javax.jms.MapMessage#setChar(java.lang.String, char)
	 */
	@Override
	public void setChar (String name, char value) throws JMSException
	{
		setValue(name, Character.valueOf(value));
	}
	/*
	 * @see javax.jms.MapMessage#getDouble(java.lang.String)
	 */
	@Override
	public double getDouble (String name) throws JMSException
	{
		final Number	v=getValue(name, Number.class, true);
		return v.doubleValue();
	}
	/*
	 * @see javax.jms.MapMessage#setDouble(java.lang.String, double)
	 */
	@Override
	public void setDouble (String name, double value) throws JMSException
	{
		setValue(name, Double.valueOf(value));
	}
	/*
	 * @see javax.jms.MapMessage#getFloat(java.lang.String)
	 */
	@Override
	public float getFloat (String name) throws JMSException
	{
		final Number	v=getValue(name, Number.class, true);
		return v.floatValue();
	}
	/*
	 * @see javax.jms.MapMessage#setFloat(java.lang.String, float)
	 */
	@Override
	public void setFloat (String name, float value) throws JMSException
	{
		setValue(name, Float.valueOf(value));
	}
	/*
	 * @see javax.jms.MapMessage#getInt(java.lang.String)
	 */
	@Override
	public int getInt (String name) throws JMSException
	{
		final Number	v=getValue(name, Number.class, true);
		return v.intValue();
	}
	/*
	 * @see javax.jms.MapMessage#setInt(java.lang.String, int)
	 */
	@Override
	public void setInt (String name, int value) throws JMSException
	{
		setValue(name, Integer.valueOf(value));
	}
	/*
	 * @see javax.jms.MapMessage#getLong(java.lang.String)
	 */
	@Override
	public long getLong (String name) throws JMSException
	{
		final Number	v=getValue(name, Number.class, true);
		return v.longValue();
	}
	/*
	 * @see javax.jms.MapMessage#setLong(java.lang.String, long)
	 */
	@Override
	public void setLong (String name, long value) throws JMSException
	{
		setValue(name, Long.valueOf(value));
	}
	/*
	 * @see javax.jms.MapMessage#getObject(java.lang.String)
	 */
	@Override
	public Object getObject (String name) throws JMSException
	{
		return getValue(name, Object.class, false);
	}
	/*
	 * @see javax.jms.MapMessage#setObject(java.lang.String, java.lang.Object)
	 */
	@Override
	public void setObject (String name, Object value) throws JMSException
	{
		setValue(name, value);
	}
	/*
	 * @see javax.jms.MapMessage#getShort(java.lang.String)
	 */
	@Override
	public short getShort (String name) throws JMSException
	{
		final Number	v=getValue(name, Number.class, true);
		return v.shortValue();
	}
	/*
	 * @see javax.jms.MapMessage#setShort(java.lang.String, short)
	 */
	@Override
	public void setShort (String name, short value) throws JMSException
	{
		setValue(name, Short.valueOf(value));
	}
	/*
	 * @see javax.jms.MapMessage#getString(java.lang.String)
	 */
	@Override
	public String getString (String name) throws JMSException
	{
		return getValue(name, String.class, false);
	}
	/*
	 * @see javax.jms.MapMessage#setString(java.lang.String, java.lang.String)
	 */
	@Override
	public void setString (String name, String value) throws JMSException
	{
		setValue(name, value);
	}
	/*
	 * @see javax.jms.MapMessage#getMapNames()
	 */
	@Override
	public Enumeration<?> getMapNames () throws JMSException
	{
		final Properties	p=new Properties();
		final Map<String,?>	vm=getValuesMap();
		if ((vm != null) && (vm.size() > 0))
			p.putAll(vm);
		return p.keys();
	}
	/*
	 * @see javax.jms.MapMessage#itemExists(java.lang.String)
	 */
	@Override
	public boolean itemExists (String name) throws JMSException
	{
		if ((null == name) || (name.length() <= 0))
			throw new IllegalArgumentException("itemExists() no name specified");

		final Map<String,?>	vm=getValuesMap();
		return vm.containsKey(name);
	}
	/*
	 * @see javax.jms.Message#clearBody()
	 */
	@Override
	public void clearBody () throws JMSException
	{
		final Map<String,?>	vm=getValuesMap();
		if ((vm != null) && (vm.size() > 0))
			vm.clear();
	}
}
