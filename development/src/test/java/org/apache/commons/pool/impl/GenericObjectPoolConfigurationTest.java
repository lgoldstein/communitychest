/*
 * 
 */
package org.apache.commons.pool.impl;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Random;

import org.apache.commons.lang3.ArrayUtils;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Lyor G.
 * @since Sep 7, 2011 8:37:00 AM
 */
public class GenericObjectPoolConfigurationTest extends Assert {
	public GenericObjectPoolConfigurationTest ()
	{
		super();
	}
	/**
	 * Ensures that all the public non-static {@link Field}-s of {@link GenericObjectPool.Config}
	 * have get/set-ter(s) in {@link GenericObjectPoolConfiguration}
	 * @throws NoSuchMethodException if cannot find a matching get/set-ter
	 */
	@Test
	public void testFieldsCoverage () throws NoSuchMethodException
	{
		for (final Field field : GenericObjectPool.Config.class.getDeclaredFields())
		{
			final int	mods=field.getModifiers();
			if (Modifier.isFinal(mods) || Modifier.isStatic(mods) || (!Modifier.isPublic(mods)))
				continue;

			final String	name=field.getName(), aName=adjustFieldName(name);
			final Class<?>	fieldType=field.getType();
			final Method	gMethod;
			try
			{
				gMethod = GenericObjectPoolConfiguration.class.getDeclaredMethod(
						((Boolean.TYPE == fieldType) ? "is" : "get") + aName, ArrayUtils.EMPTY_CLASS_ARRAY);
			}
			catch(NoSuchMethodException e)
			{
				fail("No matching getter found for " + name);
				throw e;
			}

			assertSame("Mismatched field and getter type for " + name, fieldType, gMethod.getReturnType());

			final Method	sMethod;
			try
			{
				sMethod = GenericObjectPoolConfiguration.class.getDeclaredMethod("set" + aName, fieldType);
			}
			catch(NoSuchMethodException e)
			{
				fail("No matching setter found for " + name);
				throw e;
			}

			final Class<?>	sType=sMethod.getReturnType();
			assertSame("Non void return type for setter of " + name, void.class, sType);
		}
	}
	/**
	 * Makes sure that {@link GenericObjectPoolConfiguration#GenericObjectPoolConfiguration(org.apache.commons.pool.impl.GenericObjectPool.Config)}
	 * constructor does what is intended
	 */
	@Test
	public void testGenericObjectPoolConfigurationCopyConstructor ()
	{
		final GenericObjectPool.Config	src=new GenericObjectPool.Config();
		final Random					rnd=new Random(System.nanoTime());
		src.maxActive = rnd.nextInt();
		src.maxIdle = rnd.nextInt();
		src.maxWait = rnd.nextLong();
		src.minEvictableIdleTimeMillis = rnd.nextLong();
		src.minIdle = rnd.nextInt();
		src.numTestsPerEvictionRun = rnd.nextInt();
		src.softMinEvictableIdleTimeMillis = rnd.nextInt();
		src.testOnBorrow = rnd.nextBoolean();
		src.testOnReturn = rnd.nextBoolean();
		src.testWhileIdle = rnd.nextBoolean();
		src.timeBetweenEvictionRunsMillis = rnd.nextLong();
		src.whenExhaustedAction = (byte) rnd.nextInt();

		final GenericObjectPool.Config	dst=new GenericObjectPoolConfiguration(src);
		assertEquals("Mismatched copied contents", dst, src);
	}

	private static final String adjustFieldName (final String name)
	{
		final char	ch1=name.charAt(0);
		if ((ch1 >= 'A') && (ch1 <= 'Z'))
			return name;
		else
			return Character.toUpperCase(ch1) + name.substring(1);
	}
}
