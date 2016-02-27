package net.community.chest.jmx.test;

import javax.management.Attribute;
import javax.management.AttributeNotFoundException;
import javax.management.InvalidAttributeValueException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanRegistration;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.ReflectionException;

import net.community.chest.jmx.AbstractDynamicMBean;
import net.community.chest.reflect.ClassUtil;

/**
 * <P>Copyright 2007 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Aug 14, 2007 1:42:46 PM
 */
public class DynamicJMXTester extends AbstractDynamicMBean implements MBeanRegistration {
	public DynamicJMXTester ()
	{
		super();
	}
	/*
	 * @see net.community.chest.jmx.AbstractDynamicMBean#handleGetAttributeException(java.lang.String, java.lang.Throwable)
	 */
	@Override
	protected void handleGetAttributeException (String attrName, Throwable t)
	{
		System.err.println(ClassUtil.getArgumentsExceptionLocation(getClass(), "handleGetAttributeException", attrName) + " " + t.getClass().getName() + ": " + t.getMessage());
	}
	/*
	 * @see net.community.chest.jmx.AbstractDynamicMBean#handleSetAttributeException(javax.management.Attribute, java.lang.Throwable)
	 */
	@Override
	protected void handleSetAttributeException (Attribute a, Throwable t)
	{
		final String	attrName=(null == a) ? null : a.getName();
		final Object	attrValue=(null == a) ? null : a.getValue();
		System.err.println(ClassUtil.getArgumentsExceptionLocation(getClass(), "handleSetAttributeException", attrName, attrValue) + " " + t.getClass().getName() + ": " + t.getMessage());
	}
	/*
	 * @see javax.management.DynamicMBean#getAttribute(java.lang.String)
	 */
	@Override
	public Object getAttribute (String attribute)
			throws AttributeNotFoundException, MBeanException, ReflectionException
	{
		if ("StringAttr".equalsIgnoreCase(attribute))
			return getClass().getName();
		else if ("LongAttr".equalsIgnoreCase(attribute))
			return Long.valueOf(System.currentTimeMillis());
		else if ("BooleanAttr".equalsIgnoreCase(attribute))
			return Boolean.valueOf(0L == (System.currentTimeMillis() & 1L));
		else
			throw new AttributeNotFoundException(ClassUtil.getArgumentsExceptionLocation(getClass(), "getAttribute", attribute));
	}
	/*
	 * @see javax.management.DynamicMBean#setAttribute(javax.management.Attribute)
	 */
	@Override
	public void setAttribute (Attribute attribute)
			throws AttributeNotFoundException, InvalidAttributeValueException, MBeanException, ReflectionException
	{
		final String	attrName=(null == attribute) /* should not happen */ ? null : attribute.getName();
		final Object	attrValue=(null == attribute) /* should not happen */ ? null : attribute.getValue();
		final String	locValue=ClassUtil.getArgumentsExceptionLocation(getClass(), "setAttribute", attrName, attrValue);
		if ("BooleanAttr".equalsIgnoreCase(attrName))
		{
			if (!(attrValue instanceof Boolean))
				System.err.println(locValue + " unexpected object class: " + ((null == attrValue) ? null : attrValue.getClass().getName()));
			else
				System.out.println(locValue + " done");
		}
		else
			throw new AttributeNotFoundException(locValue);
	}
	/*
	 * @see javax.management.DynamicMBean#invoke(java.lang.String, java.lang.Object[], java.lang.String[])
	 */
	@Override
	public Object invoke (String actionName, Object[] params, String[] signature)
			throws MBeanException, ReflectionException
	{
		throw new ReflectionException(new UnsupportedOperationException(ClassUtil.getArgumentsExceptionLocation(getClass(), "invoke", actionName) + " unexpected call"));
	}

	private MBeanInfo	_mbInfo	/* =null */;
	/*
	 * @see javax.management.DynamicMBean#getMBeanInfo()
	 */
	@Override
	public synchronized MBeanInfo getMBeanInfo ()
	{
		if (null == _mbInfo)
		{
			final MBeanAttributeInfo[]	attrs={
					new MBeanAttributeInfo("StringAttr", String.class.getName(), "String attribute", true, false, false),
					new MBeanAttributeInfo("LongAttr", Long.TYPE.getName(), "Atomic integer attribute", true, false, false),
					new MBeanAttributeInfo("BooleanAttr", Boolean.TYPE.getName(), "Boolean integer attribute", true, true, true)
				};

			_mbInfo = new MBeanInfo(getClass().getName(), "Dynamic JMX tester MBean", attrs, null, null /* operations */, null);
			System.out.println(ClassUtil.getExceptionLocation(getClass(), "getMBeanInfo") + " 1st call");
		}
		else
			System.out.println(ClassUtil.getExceptionLocation(getClass(), "getMBeanInfo") + " re-call");

		return _mbInfo;
	}
	/*
	 * @see javax.management.MBeanRegistration#postDeregister()
	 */
	@Override
	public void postDeregister ()
	{
		System.out.println(ClassUtil.getExceptionLocation(getClass(), "postDeregister")); 
	}
	/*
	 * @see javax.management.MBeanRegistration#postRegister(java.lang.Boolean)
	 */
	@Override
	public void postRegister (Boolean registrationDone)
	{
		System.out.println(ClassUtil.getArgumentsExceptionLocation(getClass(), "postRegister", registrationDone));
	}
	/*
	 * @see javax.management.MBeanRegistration#preDeregister()
	 */
	@Override
	public void preDeregister () throws Exception
	{
		System.out.println(ClassUtil.getExceptionLocation(getClass(), "preDeregister")); 
	}
	/*
	 * @see javax.management.MBeanRegistration#preRegister(javax.management.MBeanServer, javax.management.ObjectName)
	 */
	@Override
	public ObjectName preRegister (MBeanServer server, ObjectName name) throws Exception
	{
		System.out.println(ClassUtil.getArgumentsExceptionLocation(getClass(), "preRegister", name));
		return name;
	}
}
