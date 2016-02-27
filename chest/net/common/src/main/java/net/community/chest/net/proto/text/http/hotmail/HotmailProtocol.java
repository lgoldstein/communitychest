/**
 * 
 */
package net.community.chest.net.proto.text.http.hotmail;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Aug 3, 2008 9:06:12 AM
 */
public final class HotmailProtocol {
	private HotmailProtocol ()
	{
		// no instance
	}
	/**
	 * Default port for accessing the Hotmail account
	 */
	public static final int	DEFAULT_ACCESS_PORT=80;
	/**
	 * location of MS Hotmail schema
	 */
	public static final String HOTMAIL_SCHEMA_URI="http://schemas.microsoft.com/hotmail/";
	/**
	 * fully qualified XML namespace of Hotmail properties
	 */
	public static final String HOTMAIL_XML_NAMESPACE="urn:schemas:httpmail:";

	public static final String DEFAULT_ACCESS_HOST="services.msn.com",
							   DEFAULT_ACCESS_PATH="/svcs/hotmail/httpmail.asp",
							   DEFAULT_USER_AGENT="Outlook Express/5.0 (MSIE 5.0; Windows 98; DigExt; MSNIA)";
	/**
	 * Locates the <U>first</U> sub-node with a non-empty value
	 * @param rootNode root node to start looking (inclusive)
	 * @return string value (or null/empty if none found)
	 */
	public static final String getNodeValue (final Node rootNode)
	{
		if (null == rootNode)
			return null;

		final String rootVal=rootNode.getNodeValue();
		if ((rootVal != null) && (rootVal.length() > 0))
			return rootVal;

		final NodeList	nl=rootNode.getChildNodes();
		final int		numChildren=(null == nl) ? 0 : nl.getLength();
		for (int	nIndex=0; nIndex < numChildren; nIndex++)
		{
			final String	nodeVal=getNodeValue(nl.item(nIndex));
			if ((nodeVal != null) && (nodeVal.length() > 0))
				return nodeVal;
		}

		// exhausted all children
		return null;
	}
}
