package net.community.chest.jmx;

import java.lang.reflect.Method;

import javax.management.IntrospectionException;
import javax.management.MBeanAttributeInfo;

import net.community.chest.reflect.AttributeAccessor;
import net.community.chest.reflect.ClassUtil;

/**
 * <P>Copyright 2007 as per GPLv2</P>
 *
 * <P>{@link MBeanAttributeInfo} extension that preserves the accesor
 * {@link Method}-s</P>
 * 
 * @author Lyor G.
 * @since Aug 14, 2007 11:45:09 AM
 */
public class ReflectiveMBeanAttributeInfo extends MBeanAttributeInfo {
	/**
	 * 
	 */
	private static final long serialVersionUID = -8720200211101968595L;
	private transient Method	_getter, _setter;
	public final /* no cheating */ Method getGetter ()
	{
		return _getter;
	}

	public final /* no cheating */ Method getSetter ()
	{
		return _setter;
	}

	public ReflectiveMBeanAttributeInfo (String aName, String aDesc, Method getter, Method setter) throws IntrospectionException
	{
		super(aName, aDesc, getter, setter);

		_getter = getter;
		_setter = setter;
	}

	public Object getValue (final Object inst) throws Exception
	{
		if (!isReadable())
			throw new UnsupportedOperationException(ClassUtil.getArgumentsExceptionLocation(getClass(), "getValue", getName()) + " not readable");

		return getGetter().invoke(inst, AttributeAccessor.EMPTY_OBJECTS_ARRAY);
	}

	public void setValue (final Object inst, final Object value) throws Exception
	{
		if (!isWritable())
			throw new UnsupportedOperationException(ClassUtil.getArgumentsExceptionLocation(getClass(), "setValue", getName(), value) + " not writeable");

		getSetter().invoke(inst, value);
	}
}
