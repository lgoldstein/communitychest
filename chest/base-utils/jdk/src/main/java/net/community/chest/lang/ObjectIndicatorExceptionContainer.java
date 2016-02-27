package net.community.chest.lang;

import net.community.chest.BaseTypedValuesContainer;

/**
 * <P>Copyright 2007 as per GPLv2</P>
 *
 * <P>Used to carry information about a specific object that caused an
 * exception</P>
 * 
 * @param <V> Type of contained object(s)
 * @author Lyor G.
 * @since Jul 30, 2007 7:43:20 AM
 */
public class ObjectIndicatorExceptionContainer<V> extends BaseTypedValuesContainer<V> {
	private V _objValue	/* =null */;
	public V getObjectValue ()
	{
		return _objValue;
	}

	public void setObjectValue (V value)
	{
		_objValue = value;
	}

	private Throwable	_cause	/* =null */;
	/**
	 * @return the cause/exception that this object caused
	 */
	public Throwable getCause ()
	{
		return _cause;
	}

	public void setCause (Throwable cause)
	{
		_cause = cause;
	}

	public ObjectIndicatorExceptionContainer (Class<V> valClass, V value, Throwable t)
	{
		super(valClass);

		_objValue = value;
		_cause = t;
	}

	public ObjectIndicatorExceptionContainer (Class<V> valClass, V value)
	{
		this(valClass, value, null);
	}

	public ObjectIndicatorExceptionContainer (Class<V> valClass, Throwable t)
	{
		this(valClass, null, t);
	}

	public ObjectIndicatorExceptionContainer (Class<V> valClass)
	{
		this(valClass, null, null);
	}
}
