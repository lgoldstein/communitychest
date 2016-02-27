package net.community.chest.io.jar;

import java.util.LinkedList;
import java.util.jar.JarEntry;

import net.community.chest.reflect.ClassUtil;

/**
 * Helper class
 */
public final class ClassesEntryHandler extends LinkedList<Class<?>> implements JarEntryHandler {
	/**
	 * 
	 */
	private static final long serialVersionUID = -301052008004227214L;
	private final Class<?>	_baseClass;
	public final Class<?> getBaseClass ()
	{
		return _baseClass;
	}

	private final ClassLoader	_cl;
	public final ClassLoader getEntryClassLoader ()
	{
		return _cl;
	}

	protected ClassesEntryHandler (final Class<?> baseClass, final ClassLoader ol)
	{
		_cl =(null == ol) ? Thread.currentThread().getContextClassLoader() : ol;
		_baseClass = baseClass;
	}

	private ClassNotFoundException	_exc;
	protected ClassNotFoundException getClassNotFoundException ()
	{
		return _exc;
	}

	protected void setClassNotFoundException (ClassNotFoundException exc)
	{
		_exc = exc;
	}
	/*
	 * @see net.community.chest.io.jar.JarEntryHandler#handleJAREntry(java.util.jar.JarEntry)
	 */
	@Override
	public int handleJAREntry (JarEntry je)
	{
		final String	jn=(null == je) ? null : je.getName();
		final int		nLen=(null == jn) ? 0 : jn.length(),
						sPos=(nLen <= 1) ? (-1) : jn.lastIndexOf('.');
		final String	jp=(sPos > 0) ? jn.substring(0, sPos) : jn,
						cp=
			((null == jp) || (jp.length() <= 0)) ? null : jp.replace('/', '.');
		if ((null == cp) || (cp.length() <= 0))
			return 0;

		try
		{
			final ClassLoader	cl=getEntryClassLoader();
			final Class<?>		cc=(null == cl) ? ClassUtil.loadClassByName(cp) : cl.loadClass(cp),
								bc=getBaseClass();
			if ((bc == null) || bc.isAssignableFrom(cc))
				add(cc);
		}
		catch(ClassNotFoundException e)
		{
			setClassNotFoundException(e);
			return (-1);	// stop enumeration
		}
		catch(NoClassDefFoundError e)
		{
			setClassNotFoundException(new ClassNotFoundException(e.getMessage(), e));
			return (-2);	// stop enumeration
		}

		return 0;
	}
}