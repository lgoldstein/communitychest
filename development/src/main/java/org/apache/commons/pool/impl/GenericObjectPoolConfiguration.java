/*
 * 
 */
package org.apache.commons.pool.impl;

import java.io.Serializable;
import java.lang.reflect.Field;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * Wrapper class for {@link GenericObjectPool.Config} that has no get/set-ter(s)
 * so that we can inject it as a Spring bean 
 * @author Lyor G.
 * @since Sep 6, 2011 3:31:19 PM
 */
public class GenericObjectPoolConfiguration extends GenericObjectPool.Config implements Serializable, Cloneable {
	private static final long serialVersionUID = -1266813362155137742L;

	public GenericObjectPoolConfiguration ()
	{
		super();
	}

	private static final Field[]	FIELDS=GenericObjectPool.Config.class.getDeclaredFields();
	public GenericObjectPoolConfiguration (GenericObjectPool.Config config)
	{
		try
		{
			for (final Field f : FIELDS)
				f.set(this, f.get(config));
		}
		catch(Exception e)
		{
			if (e instanceof RuntimeException)
				throw (RuntimeException) e;
			else
				throw new RuntimeException("Failed (" + e.getClass().getName()
										 + " to copy configuration: " + e.getMessage(),
										 e);	
		}
	}

	public int getMaxIdle ()
	{
		return maxIdle;
	}

	public void setMaxIdle (int value)
	{
		this.maxIdle = value;
	}

	public int getMinIdle ()
	{
		return minIdle;
	}

	public void setMinIdle (int value)
	{
		this.minIdle = value;
	}

	public int getMaxActive ()
	{
		return maxActive;
	}

	public void setMaxActive (int value)
	{
		this.maxActive = value;
	}

	public long getMaxWait ()
	{
		return maxWait;
	}

	public void setMaxWait (long value)
	{
		this.maxWait = value;
	}

	public byte getWhenExhaustedAction ()
	{
		return whenExhaustedAction;
	}

	public void setWhenExhaustedAction (byte value)
	{
		this.whenExhaustedAction = value;
	}

	public GenericObjectPoolWhenExhaustedAction getWhenExhaustedEnum ()
	{
		return GenericObjectPoolWhenExhaustedAction.fromAction(getWhenExhaustedAction());
	}

	public void setWhenExhaustedEnum (GenericObjectPoolWhenExhaustedAction action)
	{
		setWhenExhaustedAction((null == action) ? (byte) (-1) : action.getAction());
	}

	public boolean isTestOnBorrow ()
	{
		return testOnBorrow;
	}

	public void setTestOnBorrow (boolean value)
	{
		this.testOnBorrow = value;
	}

	public boolean isTestOnReturn ()
	{
		return testOnReturn;
	}

	public void setTestOnReturn (boolean value)
	{
		this.testOnReturn = value;
	}

	public boolean isTestWhileIdle ()
	{
		return testWhileIdle;
	}

	public void setTestWhileIdle (boolean value)
	{
		this.testWhileIdle = value;
	}

	public long getTimeBetweenEvictionRunsMillis ()
	{
		return timeBetweenEvictionRunsMillis;
	}

	public void setTimeBetweenEvictionRunsMillis (long value)
	{
		this.timeBetweenEvictionRunsMillis = value;
	}

	public int getNumTestsPerEvictionRun ()
	{
		return numTestsPerEvictionRun;
	}

	public void setNumTestsPerEvictionRun (int value)
	{
		this.numTestsPerEvictionRun = value;
	}

	public long getMinEvictableIdleTimeMillis ()
	{
		return minEvictableIdleTimeMillis;
	}

	public void setMinEvictableIdleTimeMillis (long value)
	{
		this.minEvictableIdleTimeMillis = value;
	}

	public long getSoftMinEvictableIdleTimeMillis ()
	{
		return softMinEvictableIdleTimeMillis;
	}

	public void setSoftMinEvictableIdleTimeMillis (long value)
	{
		this.softMinEvictableIdleTimeMillis = value;
	}

	public boolean isLifo ()
	{
		return lifo;
	}

	public void setLifo (boolean value)
	{
		this.lifo = value;
	}
	/*
	 * @see java.lang.Object#clone()
	 */
	@Override
	public GenericObjectPoolConfiguration clone () throws CloneNotSupportedException
	{
		return getClass().cast(super.clone());
	}
	/*
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString ()
	{
		return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE, true);
	}
	/*
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode ()
	{
		return HashCodeBuilder.reflectionHashCode(this, true);
	}
	/*
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals (Object obj)
	{
		if (this == obj)
			return true;

		return EqualsBuilder.reflectionEquals(this, obj, true);
	}
}
