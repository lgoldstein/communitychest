package net.community.chest.dom.proxy;

import net.community.chest.dom.transform.XmlTranslator;

import org.w3c.dom.Element;

/**
 * <P>Copyright 2007 as per GPLv2</P>
 *
 * <P>Converts to/from XML another object rather than itself</P>
 *
 * @param <V> Type of converted object(s)
 * @author Lyor G.
 * @since Nov 21, 2007 1:45:41 PM
 */
public interface XmlProxyConvertible<V> extends XmlTranslator<V> {
    /**
     * Re-constructs the state of the supplied object instance using the
     * supplied {@link Element}-s
     * @param src Original object to be initialized
     * @param elem XML {@link Element} to be used for the initialization
     * @return initialized instance - <B>Note:</B> usually it will be same
     * as input (highly recommended), but overriding classes may decide
     * otherwise.
     * @throws Exception if failed to re-construct the object's state
     */
    V fromXml (V src, Element elem) throws Exception;
}
