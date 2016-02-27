/*
 * 
 */
package net.community.apps.tomcat.clhelper;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * <P>Copyright as per GPLv2</P>
 * @author Lyor G.
 * @since Dec 16, 2012 10:22:27 AM
 *
 */
public class ClassLoadHelperListener implements ServletContextListener {
	public ClassLoadHelperListener ()
	{
		super();
	}

	@Override
	public void contextInitialized (final ServletContextEvent sce)
	{
		showEvent("contextInitialized", sce);
	}

	@Override
	public void contextDestroyed (final ServletContextEvent sce)
	{
		showEvent("contextDestroyed", sce);
	}
	
	private void showEvent(final String eventType, final ServletContextEvent sce)
	{
		final ServletContext	ctx=sce.getServletContext();
		final String			eventData=eventType + "(" + ctx.getServletContextName() + "): " + ctx.getClass().getName();
		final Throwable			t=new Throwable();
		t.fillInStackTrace();

		final StackTraceElement[]	stackTrace=t.getStackTrace();
		for (final StackTraceElement ste : stackTrace) {
			ctx.log(eventData + ": " + ste);
		}
	}
}
