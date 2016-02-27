package net.community.chest.jmx;

import javax.management.MBeanRegistration;
import javax.management.MBeanServer;
import javax.management.ObjectName;

import net.community.chest.lang.TypedValuesContainer;
import net.community.chest.reflect.ClassUtil;

/**
 * <P>Copyright 2007 as per GPLv2</P>
 *
 * <P>Useful embedder of an MBean information class</P>
 * 
 * @param <T> The actual embedded MBean class
 * @author Lyor G.
 * @since Aug 14, 2007 3:52:20 PM
 */
public abstract class AbstractDynamicMBeanEmbedder<T>
			extends AbstractDynamicMBean
			implements MBeanRegistration, TypedValuesContainer<T> {
	private final Class<T>	_valsClass;
	/*
	 * @see net.community.chest.lang.TypedValuesContainer#getValuesClass()
	 */
	@Override
	public final /* no cheating */ Class<T> getValuesClass ()
	{
		return _valsClass;
	}
	/**
	 * Cached instance on which reflection API is executed - lazy allocated by
	 * first call to {@link #getEmbeddedInstance()}
	 */
	private T	_instance	/* =null */;
	public synchronized T getEmbeddedInstance () throws Exception
	{
		if (null == _instance)
			_instance = getValuesClass().newInstance();
		return _instance;
	}

	public synchronized void setEmbeddedInstance (T instance)
	{
		_instance = instance;
	}

	protected AbstractDynamicMBeanEmbedder (Class<T> instClass, T instance, JMXErrorHandler eh)
	{
		super(eh);

		if (null == (_valsClass=instClass))
			throw new IllegalArgumentException(ClassUtil.getConstructorExceptionLocation(getClass()) + " no values class specified");
		_instance = instance;
	}

	protected AbstractDynamicMBeanEmbedder (Class<T> instClass, JMXErrorHandler eh)
	{
		this(instClass, null, eh);
	}

	protected AbstractDynamicMBeanEmbedder (Class<T> instClass, T instance)
	{
		this(instClass, instance, null);
	}

	protected AbstractDynamicMBeanEmbedder (Class<T> instClass)
	{
		this(instClass, null, null);
	}

	@SuppressWarnings("unchecked")
	protected AbstractDynamicMBeanEmbedder (T instance, JMXErrorHandler eh)
	{
		this((null == instance) ? null : (Class<T>) instance.getClass(), eh);
	}

	protected AbstractDynamicMBeanEmbedder (T instance)
	{
		this(instance, null);
	}
	/*
	 * @see javax.management.MBeanRegistration#preRegister(javax.management.MBeanServer, javax.management.ObjectName)
	 */
	@Override
	public ObjectName preRegister (MBeanServer server, ObjectName name) throws Exception
	{
		final T	inst=getEmbeddedInstance();
		if (inst instanceof MBeanRegistration)
			return ((MBeanRegistration) inst).preRegister(server, name);
		else
			return name;
	}
	/*
	 * @see javax.management.MBeanRegistration#postRegister(java.lang.Boolean)
	 */
	@Override
	public void postRegister (Boolean registrationDone)
	{
		try
		{
			final T	inst=getEmbeddedInstance();
			if (inst instanceof MBeanRegistration)
				((MBeanRegistration) inst).postRegister(registrationDone);
		}
		catch(Exception e)
		{
			if (e instanceof RuntimeException)
				throw (RuntimeException) e;

			throw new IllegalStateException(ClassUtil.getArgumentsExceptionLocation(getClass(), "postRegister", registrationDone) + " " + e.getClass().getName() + ": " + e.getMessage());
		}
	}
	/*
	 * @see javax.management.MBeanRegistration#preDeregister()
	 */
	@Override
	public void preDeregister () throws Exception
	{
		final T	inst=getEmbeddedInstance();
		if (inst instanceof MBeanRegistration)
			((MBeanRegistration) inst).preDeregister();
	}
	/*
	 * @see javax.management.MBeanRegistration#postDeregister()
	 */
	@Override
	public void postDeregister ()
	{
		try
		{
			final T	inst=getEmbeddedInstance();
			if (inst instanceof MBeanRegistration)
				((MBeanRegistration) inst).postDeregister();
		}
		catch(Exception e)
		{
			if (e instanceof RuntimeException)
				throw (RuntimeException) e;

			throw new IllegalStateException(ClassUtil.getExceptionLocation(getClass(), "postDeregister") + " " + e.getClass().getName() + ": " + e.getMessage());
		}
	}
}
