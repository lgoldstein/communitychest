package net.community.chest.jmx;

import java.lang.reflect.Method;

import javax.management.MBeanOperationInfo;
import javax.management.MBeanParameterInfo;

import net.community.chest.reflect.ClassUtil;

/**
 * <P>Copyright 2007 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Aug 14, 2007 12:38:52 PM
 */
public class ReflectiveMBeanOperationInfo extends MBeanOperationInfo {
	/**
	 * 
	 */
	private static final long serialVersionUID = -6467597628406450248L;
	private final transient Method	_oper;
	public final /* no cheating */ Method getOperation ()
	{
		return _oper;
	}

	private MBeanParameterInfo[]	_parsInfo;
	/*
	 * @see javax.management.MBeanOperationInfo#getSignature()
	 */
	@Override
	public MBeanParameterInfo[] getSignature ()
	{
		if (null == _parsInfo)
			return super.getSignature();
		return _parsInfo;
	}
	// NOTE !!! no validation is done to make sure supplied parameters info matches method
	public void setSignature (MBeanParameterInfo[] parsInfo)
	{
		_parsInfo = parsInfo;
	}
	// NOTE !!! no validation is done to make sure supplied parameters info matches method
	public ReflectiveMBeanOperationInfo (String oDesc, Method method, MBeanParameterInfo[] parsInfo) throws IllegalArgumentException
	{
		super(oDesc, method);

		if (null == (_oper=method))
			throw new IllegalArgumentException(ClassUtil.getConstructorExceptionLocation(getClass()) + " no accessor method supplied");

		_parsInfo = parsInfo;
	}
	// uses the default parameters info mechanism
	public ReflectiveMBeanOperationInfo (String oDesc, Method method) throws IllegalArgumentException
	{
		this(oDesc, method, null);
	}

	public Object invoke (Object inst, Object ... params) throws Exception
	{
		return getOperation().invoke(inst, params);
	}
}
