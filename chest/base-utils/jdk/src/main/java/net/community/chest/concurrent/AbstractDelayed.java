package net.community.chest.concurrent;

import java.text.DateFormat;
import java.util.Date;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

import net.community.chest.lang.math.NumberTables;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * <P>Provides a simple implementation of the {@link Delayed#getDelay(java.util.concurrent.TimeUnit)}</P>
 * @author Lyor G.
 * @since Jun 17, 2008 4:17:28 PM
 */
public abstract class AbstractDelayed implements Delayed {
	protected AbstractDelayed ()
	{
		super();
	}
	/**
	 * @return Expected delay value (msec.)
	 */
	public abstract long getAbsoluteDelayValue ();
	/**
	 * First time {@link Delayed#getDelay(TimeUnit)} was called.
	 */
	private Date	_1stCallTime	/* =null */;
	public final Date getFirstCallTime ()
	{
		return _1stCallTime;
	}
	/**
	 * Restarts delay measurement - <B>Caveat emptor:</B> not <code>synchronized</code>
	 * with call to {@link #getDelay(TimeUnit)} 
	 */
	public void resetDelay ()
	{
		if (_1stCallTime != null)
			_1stCallTime = null;
	}
	/*
	 * @see java.util.concurrent.Delayed#getDelay(java.util.concurrent.TimeUnit)
	 */
	@Override
	public long getDelay (final TimeUnit unit)
	{
		final long	msDelay=getAbsoluteDelayValue();
		if (msDelay <= 0L)
			return 0L;

		final long	now=System.currentTimeMillis();
		if (null == _1stCallTime)
		{
			_1stCallTime = new Date(now);
			return unit.convert(msDelay, TimeUnit.MILLISECONDS);
		}

		final long	callDiff=now - _1stCallTime.getTime(),
					remDelay=msDelay - callDiff;
		if ((callDiff <= 0L) || (remDelay <= 0L))
			return 0L;

		return unit.convert(remDelay, TimeUnit.MILLISECONDS);
	}
	/*
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo (final Delayed o)
	{
		if (!(o instanceof AbstractDelayed))
			return (-1);

		final AbstractDelayed	other=(AbstractDelayed) o;
		final long				tv=getAbsoluteDelayValue(), ov=other.getAbsoluteDelayValue();
		if (tv < ov)
			return (-1);
		else if (tv > ov)
			return (+1);
		else
			return 0;
	}
	/*
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals (Object obj)
	{
		if (!(obj instanceof Delayed))
			return false;

		return (0 == compareTo((Delayed) obj));
	}
	/*
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode ()
	{
		return NumberTables.getLongValueHashCode(getAbsoluteDelayValue());
	}
	/*
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString ()
	{
		final Date			c1=getFirstCallTime();
		final DateFormat	dtf=(null == c1) ? null : DateFormat.getDateTimeInstance();
		final String		ct;
		if (dtf != null)
		{
			synchronized(dtf)
			{
				ct = dtf.format(c1);
			}
		}
		else
			ct = (null == c1) ? null : c1.toString();

		return "[" + ct + "](" + getAbsoluteDelayValue() + ")";
	}
}
