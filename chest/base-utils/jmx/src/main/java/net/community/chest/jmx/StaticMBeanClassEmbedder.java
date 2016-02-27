package net.community.chest.jmx;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.TreeMap;

import javax.management.Attribute;
import javax.management.AttributeNotFoundException;
import javax.management.InvalidAttributeValueException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanOperationInfo;
import javax.management.ReflectionException;

import net.community.chest.reflect.AttributeAccessor;
import net.community.chest.reflect.AttributeMethodType;
import net.community.chest.reflect.ClassUtil;
import net.community.chest.reflect.MethodsMap;

/**
 * <P>Copyright 2007 as per GPLv2</P>
 *
 * <P>Used to embed a non-{@link javax.management.DynamicMBean} class. For this purpose,
 * it is assumed that the class has an <U>interface</U> that has the <U>exact</U>
 * same class path only with the <I>MBean</I> suffix. E.g., for class named
 * <code>foo.bar.SomeClass</code> there must be an <U>interface</U> named
 * <code>foo.bar.SomeClass<U><B>MBean</B></U></code> that specifies which
 * attributes and operations are exported.</P>
 * 
 * @param <T> Actual embedded MBean class
 * @author Lyor G.
 * @since Aug 14, 2007 11:14:47 AM
 */
public class StaticMBeanClassEmbedder<T> extends AbstractDynamicMBeanEmbedder<T> {
	public StaticMBeanClassEmbedder (Class<T> instClass, T instance, JMXErrorHandler eh)
	{
		super(instClass, instance, eh);
	}

	public StaticMBeanClassEmbedder (Class<T> instClass, JMXErrorHandler eh)
	{
		this(instClass, null, eh);
	}

	public StaticMBeanClassEmbedder (Class<T> instClass)
	{
		this(instClass, null, null);
	}

	public StaticMBeanClassEmbedder (Class<T> instClass, T instance)
	{
		this(instClass, instance, null);
	}

	@SuppressWarnings("unchecked")
	public StaticMBeanClassEmbedder (T instance, JMXErrorHandler eh)
	{
		this((null == instance) ? null : (Class<T>) instance.getClass(), eh);
	}

	public StaticMBeanClassEmbedder (T instance)
	{
		this(instance, null);
	}
	/**
	 * Cached instance - lazy allocated by first call to {@link #getMBeanInfo()} 
	 */
	private MBeanInfo	_mbInfo	/* =null */;
	// lazy initialized by first call to {@link #getMBeanInfo()}
	private Map<String,ReflectiveMBeanAttributeInfo>	_attrsMap	/* =null */;
	private MethodsMap<ReflectiveMBeanOperationInfo>	_opersMap	/* =null */;
	/*
	 * @see javax.management.DynamicMBean#getMBeanInfo()
	 */
	@Override
	public synchronized MBeanInfo getMBeanInfo ()
	{
		if (_mbInfo != null)
			return _mbInfo;

		try
		{
			final Class<?>	instClass=getValuesClass(), intfcClass;
			final String	instName=instClass.getName();
			// check if embedding an interface already - if so, assume this is the MBean interface class
			if (!instClass.isInterface())
			{
				final String	intfcName=instName + "MBean";
				intfcClass = ClassUtil.loadClassByName(intfcName);
				if (!intfcClass.isInterface())	// make sure this is an interface
					throw new IllegalStateException(ClassUtil.getArgumentsExceptionLocation(getClass(), "getMBeanInfo", intfcName) + " not an interface");
			}
			else
				intfcClass = instClass;

			final Method[]						methods=intfcClass.getMethods();
			final MethodsMap<Method>			opers=new MethodsMap<Method>(Method.class);
			final Map<String,AttributeAccessor>	attrs=new TreeMap<String, AttributeAccessor>(String.CASE_INSENSITIVE_ORDER);
			if ((methods != null) && (methods.length > 0))
			{
				for (final Method m : methods)
				{
					final AttributeMethodType	aType=AttributeMethodType.classifyAttributeMethod(m);
					if (null == aType)	// should not happen
						continue;

					switch(aType)
					{
						case GETTER		:
						case PREDICATE	:
						case SETTER		:
							{
								final String		attrName=aType.getPureAttributeName(m);
								AttributeAccessor	acc=attrs.get(attrName);
								if (null == acc)	// OK if first time - allocate an entry
								{
									acc = new AttributeAccessor(attrName);
									attrs.put(attrName, acc);
								}

								// TODO log or throw an exception if getter/setter already set
								if (AttributeMethodType.SETTER.equals(aType))
									acc.setSetter(m);
								else
									acc.setGetter(m);
							}
							break;

						case OPERATION	:
							opers.put(m, m);
							// TODO log or throw an exception if operation already mapped
							break;

						default			:
							throw new NoSuchElementException("Unknown method=" + m + " type=" + aType);
					}
				}
			}

			_attrsMap = JMXUtils.buildMBeanAttributes(attrs);
			final MBeanAttributeInfo[]	aInfo;
			{
				final Collection<? extends ReflectiveMBeanAttributeInfo>	cAttrs=
					((null == _attrsMap) || (_attrsMap.size() <= 0)) ? null : _attrsMap.values();

				final int	numAttrs=(null == cAttrs) ? 0 : cAttrs.size();
				aInfo = (numAttrs <= 0) ? null : cAttrs.toArray(new ReflectiveMBeanAttributeInfo[numAttrs]);
			}

			_opersMap = JMXUtils.buildMBeanOperations(instClass, opers);
			final MBeanOperationInfo[]	oInfo;
			{
				final Collection<? extends ReflectiveMBeanOperationInfo>	cOpers=
					((null == _opersMap) || (_opersMap.size() <= 0)) ? null : _opersMap.values();

				final int	numOpers=(null == cOpers) ? 0 : cOpers.size();
				oInfo = (numOpers <= 0) ? null : cOpers.toArray(new ReflectiveMBeanOperationInfo[numOpers]);
			}

			_mbInfo = new MBeanInfo(instName, "Generic MBean", aInfo, null /* TODO add MBean constructors */, oInfo, null /* TODO add MBean notitifcations */);
		}
		catch(Exception e)
		{
			if (e instanceof RuntimeException)
				throw (RuntimeException) e;

			throw new IllegalStateException(ClassUtil.getExceptionLocation(getClass(), "getMBeanInfo") + " " + e.getClass().getName() + ": " + e.getMessage());
		}

		return _mbInfo;
	}
	/*
	 * @see javax.management.DynamicMBean#getAttribute(java.lang.String)
	 */
	@Override
	public Object getAttribute (final String attribute)
			throws AttributeNotFoundException, MBeanException, ReflectionException
	{
		// NOTE !!! assumes "getMBeanInfo" has been called !!!
		final ReflectiveMBeanAttributeInfo	aInfo=
				((null == attribute) || (attribute.length() <= 0) || (null == _attrsMap) || (_attrsMap.size() <= 0)) ? null : _attrsMap.get(attribute);
		if (null == aInfo)
			throw new AttributeNotFoundException(ClassUtil.getArgumentsExceptionLocation(getClass(), "getAttribute", attribute) + " no " + ReflectiveMBeanAttributeInfo.class.getName() + " mapping");

		try
		{
			final T	inst=getEmbeddedInstance();
			return aInfo.getValue(inst);
		}
		catch(Exception e)
		{
			throw new ReflectionException(e, ClassUtil.getArgumentsExceptionLocation(getClass(), "getAttribute", attribute));
		}
	}
	/*
	 * @see javax.management.DynamicMBean#setAttribute(javax.management.Attribute)
	 */
	@Override
	public void setAttribute (final Attribute attribute)
			throws AttributeNotFoundException, InvalidAttributeValueException, MBeanException, ReflectionException
	{
		final String	attrName=(null == attribute) ? null : attribute.getName();
		// NOTE !!! assumes "getMBeanInfo" has been called !!!
		final ReflectiveMBeanAttributeInfo	aInfo=
				((null == attrName) || (attrName.length() <= 0) || (null == _attrsMap) || (_attrsMap.size() <= 0)) ? null : _attrsMap.get(attrName);
		if (null == aInfo)
			throw new AttributeNotFoundException(ClassUtil.getArgumentsExceptionLocation(getClass(), "setAttribute", attrName) + " no " + ReflectiveMBeanAttributeInfo.class.getName() + " mapping");

		final Object	attrValue=attribute.getValue();
		try
		{
			final T	inst=getEmbeddedInstance();
			aInfo.setValue(inst, attrValue);
		}
		catch(Exception e)
		{
			throw new ReflectionException(e, ClassUtil.getArgumentsExceptionLocation(getClass(), "setAttribute", attrName, attrValue));
		}
	}
	/*
	 * @see javax.management.DynamicMBean#invoke(java.lang.String, java.lang.Object[], java.lang.String[])
	 */
	@Override
	public Object invoke (final String actionName, final Object[] params, final String[] signature)
			throws MBeanException, ReflectionException
	{
		final ReflectiveMBeanOperationInfo	oInfo=
				((null == actionName) || (actionName.length() <= 0) || (null == _opersMap) || (_opersMap.size() <= 0)) ? null : _opersMap.get(getValuesClass(), actionName, signature);
		if (null == oInfo)
			throw new MBeanException(new NoSuchElementException(), ClassUtil.getArgumentsExceptionLocation(getClass(), "invoke", (Object[]) signature) + " no such operation");

		try
		{
			final T	inst=getEmbeddedInstance();
			return oInfo.invoke(inst, params);
		}
		catch(Exception e)
		{
			throw new ReflectionException(e, ClassUtil.getArgumentsExceptionLocation(getClass(), "invoke", (Object[]) signature));
		}
	}
}
