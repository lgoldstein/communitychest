package net.community.chest.apache.httpclient.jmx;

import java.util.Collection;
import java.util.LinkedList;

import net.community.chest.apache.httpclient.HttpClientUtils;
import net.community.chest.jmx.JMXErrorHandler;
import net.community.chest.jmx.JMXProtocol;
import net.community.chest.jmx.dom.MBeanEntryDescriptor;

import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.NameValuePair;
import org.w3c.dom.Document;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Jan 10, 2008 9:38:49 AM
 */
public final class AccessorUtils {
	private AccessorUtils ()
	{
		// no instance
	}

	public static final NameValuePair getRequestParameter (final String op)
	{
		if ((null == op) || (op.length() <= 0))
			return null;
		else
			return new NameValuePair(JMXProtocol.REQ_PARAM, op);
	}

	public static final Collection<NameValuePair> addFlagParameter (Collection<NameValuePair> org, String flagName, boolean flagValue)
	{
		if ((null == flagName) || (flagName.length() <= 0) || (!flagValue))
			return org;
	
		Collection<NameValuePair>	npl=org;
		if (null == npl)
			npl = new LinkedList<NameValuePair>();
	
		npl.add(new NameValuePair(flagName, String.valueOf(flagValue)));
		return npl;
	}

	public static final Collection<NameValuePair> buildListRequest (
			String name, String domain, boolean withAttributes, boolean withValues, boolean includeNulls, boolean withOperations, boolean withParams)
	{
		Collection<NameValuePair>	npl=new LinkedList<NameValuePair>();
		npl.add(getRequestParameter(JMXProtocol.LIST_REQ));
		if ((name != null) && (name.length() > 0))
			npl.add(new NameValuePair(JMXProtocol.NAME_PARAM, "'" + name + "'"));
		if ((domain != null) && (domain.length() > 0))
			npl.add(new NameValuePair(JMXProtocol.DOMAIN_PARAM, "'" + domain + "'"));
	
		npl = addFlagParameter(npl, JMXProtocol.ATTRIBUTES_PARAM, withAttributes);
		npl = addFlagParameter(npl, JMXProtocol.VALUES_PARAM, withValues);
		npl = addFlagParameter(npl, JMXProtocol.NULLS_PARAM, includeNulls);
		npl = addFlagParameter(npl, JMXProtocol.OPERATIONS_PARAM, withOperations);
		npl = addFlagParameter(npl, JMXProtocol.PARAMS_PARAM, withParams);
	
		return npl;
	}

	public static final Collection<NameValuePair> buildGetRequest (String domain, boolean includeNulls)
	{
		Collection<NameValuePair>	npl=new LinkedList<NameValuePair>();
		npl.add(getRequestParameter(JMXProtocol.GET_REQ));
		if ((domain != null) && (domain.length() > 0))
			npl.add(new NameValuePair(JMXProtocol.DOMAIN_PARAM, "'" + domain + "'"));
		npl = addFlagParameter(npl, JMXProtocol.NULLS_PARAM, includeNulls);
		return npl;
	}

	public static final Collection<? extends MBeanEntryDescriptor> convertJMXServletResponse (final HttpMethod m, final JMXErrorHandler eh) throws Exception
	{
		final Document	doc=HttpClientUtils.loadDocument(m);
		if (null == doc)
			return null;
	
		return MBeanEntryDescriptor.readMBeans(doc, eh);
	}
}
