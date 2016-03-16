package net.community.chest.dom.transform;

import org.w3c.dom.Element;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @param <V> Type of constructed object(s)
 * @author Lyor G.
 * @since Jan 10, 2008 10:10:13 AM
 */
public interface XmlValueInstantiator<V> {
    /**
     * Creates an instance of the value from the supplied XML {@link Element}
     * @param elem The element to use for the value instantiation
     * @return Instantiated value
     * @throws Exception If cannot instantiate from XML element
     */
    V fromXml (Element elem) throws Exception;
}
