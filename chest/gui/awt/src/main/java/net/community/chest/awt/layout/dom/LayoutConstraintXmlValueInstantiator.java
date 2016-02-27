/*
 * 
 */
package net.community.chest.awt.layout.dom;

import java.awt.LayoutManager;

import net.community.chest.dom.transform.XmlValueInstantiator;
import net.community.chest.lang.TypedValuesContainer;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * <P>Special interface used for layouts that expect/accept a constraint</P>
 * 
 * @param <L> The type of {@link LayoutManager} for which this instantiator
 * generates constraints 
 * @param <V> The type of constraint being generated
 * @author Lyor G.
 * @since Jan 8, 2009 8:04:16 AM
 */
public interface LayoutConstraintXmlValueInstantiator<L extends LayoutManager,V>
		extends XmlValueInstantiator<V>, TypedValuesContainer<V> {
	/**
	 * @return The {@link Class} of the {@link LayoutManager} for which the
	 * constraint applies
	 */
	Class<L> getLayoutClass ();
	/**
	 * @param n The {@link Node} representing the constraint data
	 * @return Constraint value - may be null if null input parameter
	 * @throws Exception If failed to build the constraint from the node
	 */
	V fromConstraintNode (Node n) throws Exception;
	/**
	 * @param n The {@link Node} to be considered
	 * @return TRUE if the {@link Node} is a valid constraint
	 */
	boolean isConstraintNode (Node n);
	/**
	 * @param elem The container {@link Node} under which there is a {@link Node}
	 * representing a constraint suitable for the layout (the argument is usually
	 *  {@link Element} used in call to {@link #fromXmlContainer(Element)})
	 * @return The {@link Node} to be used in call to {@link #fromConstraintNode(Node)}
	 */
	Node getContainerConstraintNode (Node elem);
	/**
	 * @param elem An XML {@link Element} representing some <B><U>other</U></B>
	 * component for which one of its "special" sub-nodes represents the
	 * constraint data.
	 * @return The constraint value
	 * @throws Exception If failed to recover the constraint data
	 */
	V fromXmlContainer (Element elem) throws Exception;
}
