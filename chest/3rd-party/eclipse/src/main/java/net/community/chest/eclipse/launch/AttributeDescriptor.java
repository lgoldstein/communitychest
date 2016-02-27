/*
 * 
 */
package net.community.chest.eclipse.launch;

import org.w3c.dom.Element;

/**
 * <P>Copyright 2009 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Oct 13, 2009 9:50:26 AM
 */
public interface AttributeDescriptor {
	AttributeType getAttributeType ();
	/**
	 * @return The key value used in the XML element
	 */
	String getAttributeKey ();
	/**
	 * @return The type/class of data being used by this attribute
	 */
	Class<?> getAttributeClass ();
	/**
	 * @param elem XML {@link Element} containing the attribute data
	 * @return The encoded object - converted to the {@link #getAttributeClass()}
	 * type
	 * @throws Exception If failed to instantiate the object
	 */
	Object newInstance (Element elem) throws Exception;
}
