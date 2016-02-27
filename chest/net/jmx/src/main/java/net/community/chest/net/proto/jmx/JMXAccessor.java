package net.community.chest.net.proto.jmx;

import java.io.Closeable;
import java.io.IOException;
import java.net.URI;
import java.nio.channels.Channel;
import java.util.Collection;

import net.community.chest.jmx.dom.MBeanEntryDescriptor;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * <P>A baseline JMX access interface over HTTP</P>
 * @author Lyor G.
 * @since Jan 7, 2008 12:28:09 PM
 */
public interface JMXAccessor extends Closeable, Channel {
	/**
	 * @param u The {@link URI} to use as base for contacting and generating
	 * the requests - including the host, port, path part (but <U>no query
	 * parameters</U> !!!)
	 * @throws IOException if cannot contact/access the JMX server.
	 * <B>Note:</B> the fact that the call succeeded does not necessarily
	 * mean that the JMX server is alive - especially for HTTP access, where
	 * only when one of the other methods is invoked the server is actually
	 * contacted
	 */
	void connect (URI u) throws IOException;
	/**
	 * @return Original {@link URI} supplied in {@link #connect(URI)} call.
	 * Null if {@link #connect(URI)} not called.
	 */
	URI getAccessURL ();
	/**
	 * @param name Specific MBean name - if null/empty then <U>all</U>
	 * available MBean(s) are listed
	 * @param domain Specific JMX domain to query - if null/empty the
	 * default domain is used
	 * @param withAttributes TRUE=fetch also all attributes specifications
	 * @param withValues TRUE=fetch also all values
	 * @param includeNulls TRUE=fetch <code>null</code> values (valid if also
	 * <I>withValues</I>=TRUE)
	 * @param withOperations TRUE=fetch operations
	 * @param withParameters TRUE=fetch operation parameters (valid if also
	 * <I>withOperations</I>=TRUE)
	 * @return A {@link Collection} of {@link MBeanEntryDescriptor}-s whose
	 * attributes/values are set according to the selected parameters. May
	 * be null/empty if no MBean(s) available
	 * @throws IOException if cannot access/parse the JMX server response
	 */
	Collection<? extends MBeanEntryDescriptor> list (String name, String domain,
			boolean withAttributes, boolean withValues, boolean includeNulls,
			boolean withOperations, boolean withParameters)
		throws IOException;
	/**
	 * @param name Specific MBean name - if null/empty then <U>all</U>
	 * available MBean(s) are listed
	 * @param domain Specific JMX domain to query - if null/empty the
	 * default domain is used
	 * @return A {@link Collection} of {@link MBeanEntryDescriptor}-s whose
	 * attributes/values are set according to the selected parameters. May
	 * be null/empty if no MBean(s) available
	 * @throws IOException if cannot access/parse the JMX server response
	 * @see #list(String, String, boolean, boolean, boolean, boolean, boolean)
	 */
	Collection<? extends MBeanEntryDescriptor> listFull (String name, String domain) throws IOException;
	/**
	 * @param domain Specific JMX domain to query - if null/empty the
	 * default domain is used
	 * @return A {@link Collection} of all currently registered MBean(s) names
	 * @throws IOException if cannot access/parse the JMX server response
	 */
	Collection<String> listNames (String domain) throws IOException;
	/**
	 * @param domain Specific JMX domain to query - if null/empty the
	 * default domain is used
	 * @param mbl A {@link Collection} of {@link MBeanEntryDescriptor}-s whose
	 * attributes <U>values</U> we want to retrieve
	 * @param includeNulls TRUE=include attributes with <code>null</code>
	 * values in the response
	 * @return A {@link Collection} of {@link MBeanEntryDescriptor}-s that
	 * have some attributes and values (e.g., if <I>includeNulls</I> is
	 * FALSE and none of the specified attributes has a non-<code>null</code>
	 * value then it is dropped from the response). <B>Note:</B> the returned
	 * {@link MBeanEntryDescriptor}-s may not be the same as the original ones.
	 * @throws IOException if cannot access/parse the JMX server response
	 */
	Collection<? extends MBeanEntryDescriptor> getValues (
			String domain, Collection<? extends MBeanEntryDescriptor> mbl, boolean includeNulls) throws IOException;
}
