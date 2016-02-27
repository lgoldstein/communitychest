package net.community.chest.apache.log4j.appender.jmx;

import java.util.Collection;

import net.community.chest.apache.log4j.appender.RollingFileAppenderController;

/**
 * <P>Copyright 2007 as per GPLv2</P>
 *
 * <P>Reflects all calls to the {@link RollingFileAppenderController} to a
 * "real" instance provided by an <code>abstract</code> method. Can be used as
 * a base class for an MBean implementation</P>
 * 
 * @author Lyor G.
 * @since Oct 2, 2007 11:18:17 AM
 */
public abstract class AbstractRollingFileAppenderControllerContainer
	extends AbstractFileAppenderControllerContainer
	implements RollingFileAppenderController {

	protected AbstractRollingFileAppenderControllerContainer ()
	{
		super();
	}

	protected abstract RollingFileAppenderController getRollingFileAppenderController ();
	/*
	 * @see net.community.chest.apache.log4j.appender.jmx.AbstractFileAppenderControllerContainer#getFileAppenderControllerInstance()
	 */
	@Override
	protected AbstractFileAppenderController getFileAppenderControllerInstance ()
	{
		return getRollingFileAppenderController();
	}
	/*
	 * @see net.community.chest.apache.log4j.appender.RollingFileAppenderController#getAppendTimeDiff()
	 */
	@Override
	public long getAppendTimeDiff ()
	{
		final RollingFileAppenderController	inst=getRollingFileAppenderController();
		return (null == inst) ? 0L : inst.getAppendTimeDiff();
	}
	/*
	 * @see net.community.chest.apache.log4j.appender.RollingFileAppenderController#setAppendTimeDiff(long)
	 */
	@Override
	public void setAppendTimeDiff (long appendTimeDiff)
	{
		final RollingFileAppenderController	inst=getRollingFileAppenderController();
		inst.setAppendTimeDiff(appendTimeDiff);	// cause intentional NullPointerException
	}
	/*
	 * @see net.community.chest.apache.log4j.appender.RollingFileAppenderController#getCurrentFileIndex()
	 */
	@Override
	public String getCurrentFileIndex ()
	{
		final RollingFileAppenderController	inst=getRollingFileAppenderController();
		return (null == inst) ? null : inst.getCurrentFileIndex();
	}
	/*
	 * @see net.community.chest.apache.log4j.appender.RollingFileAppenderController#getFileNamePrefix()
	 */
	@Override
	public String getFileNamePrefix ()
	{
		final RollingFileAppenderController	inst=getRollingFileAppenderController();
		return (null == inst) ? null : inst.getFileNamePrefix();
	}
	/*
	 * @see net.community.chest.apache.log4j.appender.RollingFileAppenderController#setFileNamePrefix(java.lang.String)
	 */
	@Override
	public void setFileNamePrefix (String prfx)
	{
		final RollingFileAppenderController	inst=getRollingFileAppenderController();
		inst.setFileNamePrefix(prfx);	// cause intentional NullPointerException
	}
	/*
	 * @see net.community.chest.apache.log4j.appender.RollingFileAppenderController#getMaxAgeDays()
	 */
	@Override
	public int getMaxAgeDays ()
	{
		final RollingFileAppenderController	inst=getRollingFileAppenderController();
		return (null == inst) ? 0 : inst.getMaxAgeDays();
	}
	/*
	 * @see net.community.chest.apache.log4j.appender.RollingFileAppenderController#setMaxAgeDays(int)
	 */
	@Override
	public void setMaxAgeDays (int d)
	{
		final RollingFileAppenderController	inst=getRollingFileAppenderController();
		inst.setMaxAgeDays(d);	// cause intentional NullPointerException
	}
	/*
	 * @see net.community.chest.apache.log4j.appender.RollingFileAppenderController#getMaxSizeKB()
	 */
	@Override
	public int getMaxSizeKB ()
	{
		final RollingFileAppenderController	inst=getRollingFileAppenderController();
		return (null == inst) ? 0 : inst.getMaxSizeKB();
	}
	/*
	 * @see net.community.chest.apache.log4j.appender.RollingFileAppenderController#setMaxSizeKB(int)
	 */
	@Override
	public void setMaxSizeKB (int maxSizeKB)
	{
		final RollingFileAppenderController	inst=getRollingFileAppenderController();
		inst.setMaxSizeKB(maxSizeKB);	// cause intentional NullPointerException
	}
	/*
	 * @see net.community.chest.apache.log4j.appender.RollingFileAppenderController#isRollAtMidnight()
	 */
	@Override
	public boolean isRollAtMidnight ()
	{
		final RollingFileAppenderController	inst=getRollingFileAppenderController();
		return (null == inst) ? false : inst.isRollAtMidnight();
	}
	/*
	 * @see net.community.chest.apache.log4j.appender.RollingFileAppenderController#setRollAtMidnight(boolean)
	 */
	@Override
	public void setRollAtMidnight (boolean rollAtMidnight)
	{
		final RollingFileAppenderController	inst=getRollingFileAppenderController();
		inst.setRollAtMidnight(rollAtMidnight);	// cause intentional NullPointerException
	}
	/*
	 * @see net.community.chest.apache.log4j.appender.RollingFileAppenderController#removeOldFiles()
	 */
	@Override
	public Collection<String> removeOldFiles ()
	{
		final RollingFileAppenderController	inst=getRollingFileAppenderController();
		return (null == inst) ? null : inst.removeOldFiles();
	}
	/*
	 * @see net.community.chest.apache.log4j.appender.RollingFileAppenderController#rollOver()
	 */
	@Override
	public boolean rollOver ()
	{
		final RollingFileAppenderController	inst=getRollingFileAppenderController();
		return (null == inst) ? false : inst.rollOver();
	}
}
