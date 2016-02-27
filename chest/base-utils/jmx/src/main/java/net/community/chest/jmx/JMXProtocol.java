package net.community.chest.jmx;

import java.io.IOException;
import java.util.Collection;

import net.community.chest.dom.DOMUtils;
import net.community.chest.io.EOLStyle;
import net.community.chest.jmx.dom.MBeanEntryDescriptor;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * <P>Definitions of the proprietary JMX protocol over HTTP</P>
 * @author Lyor G.
 * @since Jan 7, 2008 12:23:28 PM
 */
public final class JMXProtocol {
	private JMXProtocol ()
	{
		// no instance
	}

	public static final String	DEFAULT_MBEANS_LIST_ROOT_ELEM_NAME="mbeans";
	// various request parameters
    public static final String	ATTRIBUTES_PARAM="attributes",	// true=list MBean attributes (default=false)
								VALUES_PARAM="values",			// true=fetch MBean attributes values (default=false)
								OPERATIONS_PARAM="operations",	// true=fetch MBean operations (default=false)
								PARAMS_PARAM="params",			// true=fetch MBean operation parameters (default=false)
								NULLS_PARAM="null",				// true=include null values (default=false)
								NAME_PARAM="name",
								DOMAIN_PARAM="domain",
								OPNAME_PARAM="opname",			// if non-nulll then overrides the operation name in the XML descriptor
								UNIQUE_PARAM="unique",			// true=invoked operation name is unique (default=true)
								PARAMLESS_PARAM="paramless";		// true=no need to read operation XML descriptor since operation has no parameters
    // format: http://somehost:port/servlet?req=list[&attributes=true/false][&values=true/false][&name='...'][&null=true/false][operations=true/false][params=true/false]
    // format: http://somehost:port/servlet?req=get[&null=true/false]
    public static final String	REQ_PARAM="req",
    								ALL_REQ="all",	// equivalent to req=list&attributes=true&values=true&null=true&operations=true&params=true
									LIST_REQ="list",
									AGENTS_REQ="agents",
									GET_REQ="get",	// only the 'null' option is valid
									INVOKE_REQ="invoke",
									WHEREAMI_REQ="whereami",
									SYSPROPS_REQ="sysprops",
									ENV_REQ="env",
									CONTEXT_REQ="context",	// servlet context
									CONFIG_REQ="config",	// servlet configuration
									VERINFO_REQ="verinfo";	// community chest version information
									

    public static final <A extends Appendable> A appendDescriptors (A sb, Collection<? extends MBeanEntryDescriptor> mbl) throws IOException
    {
    	final int	numMBeans=(null == mbl) ? 0 : mbl.size();
    	if (null == sb)
    		throw new IOException("appendDescriptors(" + numMBeans + ") no " + Appendable.class.getSimpleName() + " instance");

    	if (numMBeans > 0)
    	{
    		for (final MBeanEntryDescriptor mbe : mbl)
    		{
    			final String	mbString=(null == mbe) ? null : mbe.toString();
    			if ((null == mbString) || (mbString.length() <= 0))
    				continue;
    			sb.append(mbString);
    			EOLStyle.CRLF.appendEOL(sb);
    		}
    	}

    	return sb;
    }

    public static final String buildDescriptorsDocument (Collection<? extends MBeanEntryDescriptor> mbl) throws IOException
    {
    	final int		numMBeans=(null == mbl) ? 0 : mbl.size();
    	StringBuilder	sb=new StringBuilder(Math.max(numMBeans, 1) * 128 + 64)
    						.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>")
   						.append(EOLStyle.CRLF.getStyleChars())
    						.append(DOMUtils.XML_ELEM_START_DELIM)
    						.append(DEFAULT_MBEANS_LIST_ROOT_ELEM_NAME)
    						.append(DOMUtils.XML_ELEM_END_DELIM)
   						.append(EOLStyle.CRLF.getStyleChars())
   						;

    	sb = appendDescriptors(sb, mbl);

    	sb.append(DOMUtils.XML_ELEM_START_DELIM)
			.append(DOMUtils.XML_ELEM_CLOSURE_DELIM)
			.append(DEFAULT_MBEANS_LIST_ROOT_ELEM_NAME)
			.append(DOMUtils.XML_ELEM_END_DELIM)
			;

		return sb.toString();
    }
}
