package net.community.chest.reflect;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import net.community.chest.lang.TypedValuesContainer;
import net.community.chest.resources.PropertyAccessor;

/**
 * <P>Copyright 2007 as per GPLv2</P>
 *
 * <P>Provides an efficient mapping of methods to objects by using
 * their signatures - key=method key (String), value=object</P>
 * 
 * @param <V> The mapped value generic type
 * @author Lyor G.
 * @since Aug 14, 2007 12:02:43 PM
 */
public class MethodsMap<V> extends TreeMap<String,V>
		implements TypedValuesContainer<V>, PropertyAccessor<AccessibleObject,V> {
	/**
	 * 
	 */
	private static final long serialVersionUID = 9055718899403440637L;
	private final Class<V>	_valsClass;
	/*
	 * @see net.community.chest.lang.TypedValuesContainer#getValuesClass()
	 */
	@Override
	public final /* no cheating */ Class<V> getValuesClass ()
	{
		return _valsClass;
	}

	public MethodsMap (Class<V> valsClass)
	{
		super();

		if (null == (_valsClass=valsClass))
			throw new IllegalArgumentException(ClassUtil.getConstructorExceptionLocation(getClass()) + " no values class instance supplied");
	}
	/*
	 * @see java.util.TreeMap#put(java.lang.Object, java.lang.Object)
	 */
	@Override
	public V put (final String mKey, final V o) throws IllegalArgumentException
	{
		if ((null == mKey) || (mKey.length() <= 0) || (null == o))
			throw new IllegalArgumentException("put(" + mKey + ") bad/illegal key/object to map");

		return super.put(mKey, o);
	}
	/**
	 * @param clsPath top-level class path to which the method should be
	 * attributed - may NOT be null/empty. This is required since the method
	 * might be an <U>inherited</U> one - i.e., mapping it with its own name
	 * will yield the super-class in the signature instead of the actual
	 * class on which we might want to invoke the method
	 * @param m method to be mapped - may NOT be null
	 * @param o object to be mapped - may NOT be null
	 * @return previous mapping - null if none
	 * @throws IllegalArgumentException if null class/method/object
	 */
	public V put (final String clsPath, final Method m, final V o) throws IllegalArgumentException
	{
		if (null == o)
			throw new IllegalArgumentException("bad/illegal object to map");

		return put(MethodUtil.getMethodKey(clsPath, m), o);
	}
	/**
	 * @param c top-level class to which the method should be attributed - may
	 * NOT be null. This is required since the method might be an
	 * <U>inherited</U> one - i.e., mapping it with its own name will yield the
	 * super-class in the signature instead of the actual class on which we might
	 * want to invoke the method (this one in this case
	 * @param m method to be mapped - may NOT be null
	 * @param o object to be mapped - may NOT be null
	 * @return previous mapping - null if none
	 * @throws IllegalArgumentException if null class/method/object
	 */
	public V put (final Class<?> c, final Method m, final V o) throws IllegalArgumentException
	{
		return put(MethodUtil.getMethodKey(c, m), o);
	}
	/**
	 * @param c top-level class to which the constructor should be attributed - may
	 * NOT be null. This is required since the constructor might be an
	 * <U>inherited</U> one - i.e., mapping it with its own name will yield the
	 * super-class in the signature instead of the actual class on which we might
	 * want to invoke the method (this one in this case
	 * @param m constructor to be mapped - may NOT be null
	 * @param o object to be mapped - may NOT be null
	 * @return previous mapping - null if none
	 * @throws IllegalArgumentException if null class/method/object
	 */
	public V put (final Class<?> c, final Constructor<?> m, final V o) throws IllegalArgumentException
	{
		return put(MethodUtil.getConstructorKey(c, m), o);
	}
	/**
	 * @param m method to be mapped - may NOT be null
	 * @param o object to be mapped - may NOT be null
	 * @return previous mapping - null if none
	 * @throws IllegalArgumentException if null method/object
	 */
	public V put (final Method m, final V o) throws IllegalArgumentException
	{
		return put(MethodUtil.getMethodKey(m), o);
	}
	/**
	 * @param c constructor to be mapped - may NOT be null
	 * @param o object to be mapped - may NOT be null
	 * @return previous mapping - null if none
	 * @throws IllegalArgumentException if null method/object
	 */
	public V put (final Constructor<?> c, final V o) throws IllegalArgumentException
	{
		return put(MethodUtil.getConstructorKey(c), o);
	}
	/**
	 * @param f Field to be mapped - may NOT be null
	 * @param o object to be mapped - may NOT be null
	 * @return previous mapping - null if none
	 * @throws IllegalArgumentException if null method/object
	 */
	public V put (final Field f, final V o) throws IllegalArgumentException
	{
		return put(MethodUtil.getFieldKey(f), o);
	}
	/**
	 * @param cName declaring method class name - may NOT be null/empty
	 * @param mName method name - may NOT be null/empty
	 * @param params parameters classes - may be null/empty, and even have
	 * "empty" elements (which are ignored when the key is generated)
	 * @param o object to be mapped - may NOT be null
	 * @return previous mapping - null if none
	 * @throws IllegalArgumentException if null method/object specification
	 */
	public V put (final String cName, final String mName, final Class<?>[] params, final V o) throws IllegalArgumentException
	{
		return put(MethodUtil.getMethodKey(cName, mName, params), o);
	}
	
	public V putByConstructor (final String cName, final Class<?>[] params, final V o)
	{
		return put(cName, MethodUtil.CONSTRUCTOR_METHOD_NAME, params, o);
	}

	public V putByConstructor (final Class<?> c, final Class<?>[] params, final V o)
	{
		return putByConstructor((null == c) ? null : c.getName(), params, o);
	}
	/**
	 * @param clsPath top-level class path to which the constructor should be
	 * attributed - may NOT be null/empty. This is required since the constructor
	 * might be an <U>inherited</U> one - i.e., mapping it with its own name
	 * will yield the super-class in the signature instead of the actual
	 * class on which we might want to invoke the constructor
	 * @param c constructor to be mapped - may NOT be null
	 * @param o object to be mapped - may NOT be null
	 * @return previous mapping - null if none
	 * @throws IllegalArgumentException if null class/constructor/object
	 */
	public V put (final String clsPath, final Constructor<?> c, final V o) throws IllegalArgumentException
	{
		return put(MethodUtil.getConstructorKey(clsPath, c),  o);
	}
	/**
	 * @param cName declaring method class name - may NOT be null/empty
	 * @param mName method name - may NOT be null/empty
	 * @param params parameters classes - may be null/empty, and even have
	 * "empty" elements (which are ignored when the key is generated)
	 * @param o object to be mapped - may NOT be null
	 * @return previous mapping - null if none
	 * @throws IllegalArgumentException if null method/object specification
	 */
	public V put (final String cName, final String mName, final String[] params, final V o) throws IllegalArgumentException
	{
		return put(MethodUtil.getMethodKey(cName, mName, params), o);
	}
	/**
	 * @param mKey method key generated by one of the <I>getMethodKey</I>
	 * overloads (<B>Caveat emptor:</B> not validated) - if null, then
	 * same as if lookup failed
	 * @return mapped object - null if none found
	 */
	public V get (final String mKey)
	{
		if ((null == mKey) || (mKey.length() <= 0))
			return null;
		else
			return super.get(mKey);
	}
	/**
	 * @param cName declaring method class name - if null/empty, then nothing is looked up
	 * @param mName method name - if null/empty, then nothing is looked up
	 * @param params parameters classes - may be null/empty, and even have
	 * "empty" elements (which are ignored when the key is generated)
	 * @return previous mapping - null if none
	 */
	public V get (final String cName, final String mName, final Class<?>... params)
	{
		return get(MethodUtil.getMethodKey(cName, mName, params));
	}

	public V getByConstructor (final String cName, final Class<?> ... params)
	{
		return get(cName, MethodUtil.CONSTRUCTOR_METHOD_NAME, params);
	}

	public V getByConstructor (final Class<?> c, final Class<?> ... params)
	{
		return getByConstructor((null == c) ? null : c.getName(), params);
	}
	/**
	 * @param cName declaring method class name - if null/empty, then nothing is looked up
	 * @param mName method name - if null/empty, then nothing is looked up
	 * @param params parameters classes - may be null/empty, and even have
	 * "empty" elements (which are ignored when the key is generated)
	 * @return previous mapping - null if none
	 */
	public V get (final String cName, final String mName, final String... params)
	{
		return get(MethodUtil.getMethodKey(cName, mName, params));
	}
	/**
	 * @param c {@link Class} that declared the method - if null, nothing is looked up
	 * @param mName method name - if null/empty, then nothing is looked up
	 * @param params parameters classes - may be null/empty, and even have
	 * "empty" elements (which are ignored when the key is generated)
	 * @return previous mapping - null if none
	 */
	public V get (final Class<?> c, final String mName, final String... params)
	{
		return get((null == c) ? null : c.getName(), mName, params);
	}
	/**
	 * @param clsPath top-level class path to which the method should be
	 * attributed - if null/empty then nothing is looked up
	 * @param m method to be looked up - if NULL then nothing is looked up
	 * @return previous mapping - null if none
	 */
	public V get (final String clsPath, final Method m)
	{
		return get(MethodUtil.getMethodKey(clsPath, m));
	}
	/**
	 * @param clsPath top-level class path to which the method should be
	 * attributed - if null/empty then nothing is looked up
	 * @param c constructor to be looked up - if NULL then nothing is looked up
	 * @return previous mapping - null if none
	 */
	public V get (final String clsPath, final Constructor<?> c)
	{
		return get(MethodUtil.getConstructorKey(clsPath, c));
	}
	/**
	 * @param c top-level class to which the method should be attributed - if
	 * NULL then nothing is looked up
	 * @param m method to be looked up - if NULL then nothing is looked up
	 * @return previous mapping - null if none
	 */
	public V get (final Class<?> c, final Method m)
	{
		return get(MethodUtil.getMethodKey(c, m));
	}
	/**
	 * @param c top-level class to which the method should be attributed - if
	 * NULL then nothing is looked up
	 * @param m constructor to be looked up - if NULL then nothing is looked up
	 * @return previous mapping - null if none
	 */
	public V get (final Class<?> c, final Constructor<?> m)
	{
		return get(MethodUtil.getConstructorKey(c, m));
	}
	/**
	 * @param m {@link Method} to be mapped - if null, then nothing is looked up
	 * @return previous mapping - null if none
	 */
	public V get (final Method m)
	{
		return get(MethodUtil.getMethodKey(m));
	}
	/**
	 * @param c {@link Constructor} to be mapped - if null, then nothing is looked up
	 * @return previous mapping - null if none
	 */
	public V get (final Constructor<?> c)
	{
		return get(MethodUtil.getConstructorKey(c));
	}
	/**
	 * @param f {@link Field} to be mapped - if null, then nothing is looked up
	 * @return previous mapping - null if none
	 */
	public V get (final Field f)
	{
		return get(MethodUtil.getFieldKey(f));
	}
	/*
	 * @see net.community.chest.resources.PropertyAccessor#getProperty(java.lang.Object)
	 */
	@Override
	public V getProperty (AccessibleObject key)
	{
		if (key instanceof Method)
			return get((Method) key);
		else if (key instanceof Constructor<?>)
			return get((Constructor<?>) key);
		else if (key instanceof Field)
			return get((Field) key);
		else
			return null;
	}
	/*
	 * @see java.util.Map#containsKey(java.lang.Object)
	 * 
	 */
	@Override
	public boolean containsKey (Object key)
	{
		return (get(key) != null);
	}
	/*
	 * @see java.util.TreeMap#putAll(java.util.Map)
	 */
	@Override
	public void putAll (Map<? extends String, ? extends V> map)
	{
		throw new UnsupportedOperationException(ClassUtil.getExceptionLocation(getClass(), "putAll") + " N/A");
	}
	/**
	 * @param mKey method key generated by one of the <I>getMethodKey</I>
	 * overloads (<B>Caveat emptor:</B> not validated) - if null, then
	 * same as if lookup failed
	 * @return removed associated value
	 */
	public V remove (final String mKey)
	{
		if ((null == mKey) || (mKey.length() <= 0))
			return null;
		else
			return super.remove(mKey);
	}
	/**
	 * @param cName declaring method class name - if null/empty, then nothing is looked up
	 * @param mName method name - if null/empty, then nothing is looked up
	 * @param params parameters classes - may be null/empty, and even have
	 * "empty" elements (which are ignored when the key is generated)
	 * @return previous mapping - null if none
	 */
	public V remove (final String cName, final String mName, final Class<?>... params)
	{
		return remove(MethodUtil.getMethodKey(cName, mName, params));
	}
	/**
	 * @param cName declaring method class name - if null/empty, then nothing is looked up
	 * @param mName method name - if null/empty, then nothing is looked up
	 * @param params parameters classes - may be null/empty, and even have
	 * "empty" elements (which are ignored when the key is generated)
	 * @return previous mapping - null if none
	 */
	public V remove (final String cName, final String mName, final String... params)
	{
		return remove(MethodUtil.getMethodKey(cName, mName, params));
	}
	/**
	 * @param clsPath top-level class path to which the method should be
	 * attributed - if null/empty then nothing is removed
	 * @param m method to be removed - if NULL then nothing is removed
	 * @return previous mapping - null if none
	 */
	public V remove (final String clsPath, final Method m)
	{
		return remove(MethodUtil.getMethodKey(clsPath, m));
	}
	/**
	 * @param c top-level class to which the method should be attributed - if
	 * NULL then nothing is removed
	 * @param m method to be removed - if NULL then nothing is removed
	 * @return previous mapping - null if none
	 */
	public V remove (final Class<?> c, final Method m)
	{
		return remove(MethodUtil.getMethodKey(c,m));
	}
	/**
	 * @param m method to be mapped - if null, then nothing is looked up
	 * @return previous mapping - null if none
	 */
	public V remove (final Method m)
	{
		return remove(MethodUtil.getMethodKey(m));
	}
	/**
	 * @param clsPath top-level class path to which the constructor should be
	 * attributed - if null/empty then nothing is removed
	 * @param c constructor to be removed - if NULL then nothing is removed
	 * @return previous mapping - null if none
	 */
	public V remove (final String clsPath, final Constructor<?> c)
	{
		return remove(MethodUtil.getConstructorKey(clsPath, c));
	}
	/**
	 * @param c top-level class to which the constructor should be attributed - if
	 * NULL then nothing is removed
	 * @param m constructor to be removed - if NULL then nothing is removed
	 * @return previous mapping - null if none
	 */
	public V remove (final Class<?> c, final Constructor<?> m)
	{
		return remove(MethodUtil.getConstructorKey(c,m));
	}
	/**
	 * @param c constructor to be mapped - if null, then nothing is looked up
	 * @return previous mapping - null if none
	 */
	public V remove (final Constructor<?> c)
	{
		return remove(MethodUtil.getConstructorKey(c));
	}

	public static final synchronized Map<Integer,Constructor<?>> getConstructorsMapByNumArgs (final Class<?> c)
	{
		final Constructor<?>[]	ca=c.getDeclaredConstructors();
		if ((null == ca) || (ca.length <= 0))
			return null;

		Map<Integer,Constructor<?>>	cm=new HashMap<Integer,Constructor<?>>(ca.length, 1.0f);
		for (final Constructor<?> cc : ca)
		{
			final Class<?>[]		pa=(null == cc) ? null : cc.getParameterTypes();
			final int				numParams=(null == pa) ? 0 : pa.length;
			final Constructor<?>	pc=cm.put(Integer.valueOf(numParams), cc);
			if (pc != null)
				throw new IllegalStateException("Multiple constructors with " + numParams + " arguments");
		}

		return cm;
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static final MethodsMap<Constructor<?>> createConstructorsMap ()
	{
		return new MethodsMap(Constructor.class);
	}

}
