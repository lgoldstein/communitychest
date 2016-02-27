package net.community.chest.dom.proxy;

import org.w3c.dom.Element;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * <P>Adds the capability to specify a resources loading context</P>
 * 
 * @param <V> Type of converted object(s)
 * @author Lyor G.
 * @since Jun 19, 2008 3:16:33 PM
 */
public interface ResourcedXmlProxyConvertible<V> extends XmlProxyConvertible<V> {
	/**
	 * Re-constructs the state of the supplied object instance using the
	 * supplied {@link Element}-s
	 * @param src Original object to be initialized
	 * @param elem XML {@link Element} to be used for the initialization
	 * @param resContext The resources loading context - can be null
	 * @return initialized instance - <B>Note:</B> usually it will be same
	 * as input (highly recommended), but overriding classes may decide
	 * otherwise.
	 * @throws Exception if failed to re-construct the object's state
	 */
	V fromXml (V src, Element elem, ReflectiveResourceLoaderContext resContext) throws Exception;
}
