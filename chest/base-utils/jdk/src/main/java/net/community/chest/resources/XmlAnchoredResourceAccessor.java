/*
 * 
 */
package net.community.chest.resources;

import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * <P>Provides further access to anchored XML resources</P>
 * @author Lyor G.
 * @since Aug 21, 2008 12:34:09 PM
 */
public interface XmlAnchoredResourceAccessor extends AnchoredResourceAccessor {
	/**
	 * Retrieves an XML configuration file from the resources
	 * @param name file name (with/out XML suffix - does not matter).
	 * @return loaded {@link Document} - null if resource does not exist
	 * @throws Exception if unable to load/parse XML file
	 */
	Document getDocument (String name) throws Exception;
	/**
	 * @return A &quot;default&quot; document that can be used for
	 * sections and such - may be null if no such default exists/available
	 * @throws Exception If cannot load/parse the XML document
	 */
	Document getDefaultDocument () throws Exception;
	/**
	 * Default element and attribute used to separate the default {@link Document}
	 * into &quot;sections&quot;
	 */
	public static final String	SECTION_ELEM_NAME="section", SECTION_NAME_ATTR="name";
	/**
	 * @return A {@link Map} of {@link Element}-s whose key=the section
	 * name and value=the matching {@link Element} - may be null/empty
	 * if no default document
	 * @throws RuntimeException If cannot load/parse the default XML document and/or
	 * divide it into sections
	 */
	Map<String,Element> getSectionsMap () throws RuntimeException;
	/**
	 * @param name Section name from default document
	 * @return Matching XML {@link Element} - null if no match found or
	 * no sections/default document
	 * @throws RuntimeException If cannot load/parse the default XML document and/or
	 * divide it into sections
	 */
	Element getSection (String name) throws RuntimeException;
}
