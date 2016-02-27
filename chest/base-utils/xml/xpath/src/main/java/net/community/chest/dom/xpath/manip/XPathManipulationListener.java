/*
 * 
 */
package net.community.chest.dom.xpath.manip;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * <P>Copyright GPLv2</P>
 *
 * @author Lyor G.
 * @since May 7, 2009 10:54:59 AM
 */
public interface XPathManipulationListener {
	void handleManipulationExecutionResult (XPathManipulationData manip, Document doc, Element elem, Object result);
}
