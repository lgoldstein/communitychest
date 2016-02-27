/*
 * 
 */
package net.community.apps.apache.ant.antrunner;

import net.community.chest.apache.ant.build.EmbeddedHandlerLogger;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Jul 31, 2008 1:25:59 PM
 */
public class EventsExecutor extends EmbeddedHandlerLogger {
	public EventsExecutor ()
	{
		super(MainFrame.getBuildEventsHandler());
	}
}
