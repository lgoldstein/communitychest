/*
 * 
 */
package net.community.chest.junit;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import junit.framework.TestCase;
import net.community.chest.reflect.AttributeAccessor;

/**
 * <P>Copyright as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Jul 5, 2009 10:15:41 AM
 */
public class EmbeddedMethodTest extends TestCase {
	private Method	_testMethod;
	public Method getTestMethod ()
	{
		return _testMethod;
	}

	public void setTestMethod (Method testMethod)
	{
		_testMethod = testMethod;
	}

	private Object	_testObject;
	public Object getTestObject ()
	{
		return _testObject;
	}

	public void setTestObject (Object testObject)
	{
		_testObject = testObject;
	}

	protected synchronized Object getTestObjectInstance () throws Exception
	{
		if (null == _testObject)
		{
			final Method	m=getTestMethod();
			final Class<?>	c=m.getDeclaringClass();
			_testObject = c.newInstance();
		}

		return _testObject;
	}

	public EmbeddedMethodTest (Method m, Object o)
	{
		_testMethod = m;
		_testObject = o;
	}

	public EmbeddedMethodTest (Method m)
	{
		this(m, null);
	}

	public EmbeddedMethodTest ()
	{
		this(null);
	}

	public EmbeddedMethodTest (Class<?> c, String methodName, Object o)
		throws SecurityException, NoSuchMethodException
	{
		this(c.getDeclaredMethod(methodName), o);
	}

	public EmbeddedMethodTest (Class<?> c, String methodName)
		throws SecurityException, NoSuchMethodException
	{
		this(c, methodName, null);
	}

	protected void invokeTestPhaseMethod (final String name) throws Exception
	{
		final Method	m=getTestMethod();
		final Class<?>	c=m.getDeclaringClass();
		try
		{
			final Method	pm=c.getDeclaredMethod(name);
			if (pm != null)
				pm.invoke(getTestObjectInstance(), AttributeAccessor.EMPTY_OBJECTS_ARRAY);
		}
		catch(NoSuchMethodException e)
		{
			// ignored
		}
	}
	/*
	 * @see junit.framework.TestCase#setUp()
	 */
	@Override
	protected void setUp () throws Exception
	{
		invokeTestPhaseMethod("setUp");
	}
	/*
	 * @see junit.framework.TestCase#tearDown()
	 */
	@Override
	protected void tearDown () throws Exception
	{
		invokeTestPhaseMethod("setUp");
	}
	/*
	 * @see junit.framework.TestCase#runTest()
	 */
	@Override
	protected void runTest () throws Throwable
	{
		final Method 	runMethod=getTestMethod();
		final Object	testObj=getTestObjectInstance();
		try
		{
			runMethod.invoke(testObj, AttributeAccessor.EMPTY_OBJECTS_ARRAY);
		}
		catch(InvocationTargetException e)
		{
			e.fillInStackTrace();
			throw e.getTargetException();
		}
		catch(IllegalAccessException e)
		{
			e.fillInStackTrace();
			throw e;
		}
	}
}
