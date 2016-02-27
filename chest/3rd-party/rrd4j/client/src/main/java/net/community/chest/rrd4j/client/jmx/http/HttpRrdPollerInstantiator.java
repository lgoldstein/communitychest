package net.community.chest.rrd4j.client.jmx.http;

import java.net.URI;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import net.community.chest.CoVariantReturn;
import net.community.chest.rrd4j.client.jmx.AbstractRrdPollerInstantiator;
import net.community.chest.rrd4j.common.jmx.MBeanRrdDef;
import net.community.chest.util.map.MapEntryImpl;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Jan 10, 2008 2:35:48 PM
 */
public class HttpRrdPollerInstantiator extends AbstractRrdPollerInstantiator {
	private URI	_url	/* =null */;
	public URI getAccessURL ()
	{
		return _url;
	}
	// can be set while thread is not running
	public void setAccessURL (URI u)
	{
		_url = u;
	}

	public HttpRrdPollerInstantiator (URI u)
	{
		_url = u;
	}

	public HttpRrdPollerInstantiator ()
	{
		this(null);
	}

	private static final AtomicInteger	_counter=new AtomicInteger(0);
	/*
	 * @see net.community.chest.rrd4j.client.jmx.AbstractRrdPollerInstantiator#createPollerInstance(java.util.Collection)
	 */
	@Override
	@CoVariantReturn
	protected Map.Entry<String,? extends HttpMBeanRrdPoller> createPollerInstance (Collection<? extends MBeanRrdDef> defs) throws Exception
	{
		final URI	u=getAccessURL();
		if (null == u)
			throw new IllegalStateException("createPollerInstance() no URL value set");

		final HttpMBeanRrdPoller	p=new HttpMBeanRrdPoller(u, defs);
		final int					v=_counter.incrementAndGet();
		final String				name="tHttpRrdPoller-" + v;
		return new MapEntryImpl<String,HttpMBeanRrdPoller>(name, p);
	}
}
