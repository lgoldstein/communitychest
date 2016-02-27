package net.community.chest.net.proto.jmx;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import net.community.chest.jmx.dom.MBeanEntryDescriptor;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Jan 7, 2008 12:36:57 PM
 */
public abstract class AbstractJMXAccessor implements JMXAccessor {
	protected AbstractJMXAccessor ()
	{
		super();
	}
	/*
	 * @see net.community.chest.net.proto.jmx.JMXAccessor#listNames(java.lang.String)
	 */
	@Override
	public Collection<String> listNames (String domain) throws IOException
	{
		final Collection<? extends MBeanEntryDescriptor>	mbl=list(null, domain, false, false, false, false, false);
		final int											numMBeans=(null == mbl) ? 0 : mbl.size();
		if (numMBeans <= 0)
			return null;

		final Collection<String>	nl=new ArrayList<String>(numMBeans);
		for (final MBeanEntryDescriptor mbe : mbl)
		{
			final String	mbName=(null == mbe) /* should not happen */ ? null : mbe.getObjectName();
			if ((null == mbName) || (mbName.length() <= 0))
				continue;	// should not happen

			nl.add(mbName);
		}

		return nl;
	}
	/*
	 * @see net.community.chest.net.proto.jmx.JMXAccessor#listFull(java.lang.String, java.lang.String)
	 */
	@Override
	public Collection<? extends MBeanEntryDescriptor> listFull (String name, String domain) throws IOException
	{
		return list(name, domain, true, true, true, true, true);
	}
	/*
	 * @see java.nio.channels.Channel#isOpen()
	 */
	@Override
	public boolean isOpen ()
	{
		return getAccessURL() != null;
	}
}
