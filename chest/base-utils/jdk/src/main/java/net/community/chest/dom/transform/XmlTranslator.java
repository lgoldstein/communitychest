package net.community.chest.dom.transform;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * <P>Copyright 2007 as per GPLv2</P>
 *
 * @param <V> Type of converted object(s)
 * @author Lyor G.
 * @since Dec 24, 2007 9:42:01 AM
 */
public interface XmlTranslator<V> extends XmlValueInstantiator<V> {
    /**
     * Updates the supplied {@link Element} with the attributes and/or
     * sub-elements required for the value.
     * @param src The value to use for updating - <B>Note:</B> if null then
     * recommended behavior is simply to do nothing
     * @param doc Document instance to use to create more XML elements (if
     * not using only attributes
     * @param elem Element to update or use as root - as created by the
     * {@link #toXml(Object, Document)} call
     * @return Updated element - <B>Note:</B> recommended behavior is to
     * return same as input parameter - unless very good reason not to
     * @throws Exception if cannot update the element
     */
    Element toXml (V src, Document doc, Element elem) throws Exception;
    Element toXml (V src, Document doc) throws Exception;
}
