package net.community.chest.dom;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import net.community.chest.util.collection.CollectionsUtils;

import org.w3c.dom.Attr;
import org.w3c.dom.CDATASection;
import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Element;
import org.w3c.dom.Entity;
import org.w3c.dom.EntityReference;
import org.w3c.dom.Node;
import org.w3c.dom.Notation;
import org.w3c.dom.ProcessingInstruction;
import org.w3c.dom.Text;

/**
 * <P>Copyright 2007 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Nov 6, 2007 3:01:13 PM
 */
public enum NodeTypeEnum {
	ELEMENT(Node.ELEMENT_NODE, Element.class),
	ATTRIBUTE(Node.ATTRIBUTE_NODE, Attr.class),
	TEXT(Node.TEXT_NODE, Text.class),
	CDATA(Node.CDATA_SECTION_NODE, CDATASection.class),
	ENTITYREF(Node.ENTITY_REFERENCE_NODE, EntityReference.class),
	ENTITY(Node.ENTITY_NODE,Entity.class),
	PROCINST(Node.PROCESSING_INSTRUCTION_NODE,ProcessingInstruction.class),
	COMMENT(Node.COMMENT_NODE,Comment.class),
	DOCUMENT(Node.DOCUMENT_NODE,Document.class),
	DOCTYPE(Node.DOCUMENT_TYPE_NODE,DocumentType.class),
	DOCFRAG(Node.DOCUMENT_FRAGMENT_NODE,DocumentFragment.class),
	NOTATION(Node.NOTATION_NODE,Notation.class);

	private final short	_nodeType;
	public short getNodeType ()
	{
		return _nodeType;
	}

	public boolean isSameNodeType (final Node n)
	{
		return (n != null) && (getNodeType() == n.getNodeType());
	}

	private final Class<? extends Node>	_nodeClass;
	public Class<? extends Node> getNodeClass ()
	{
		return _nodeClass;
	}

	NodeTypeEnum (final short nodeType, final Class<? extends Node> nodeClass)
	{
		_nodeType = nodeType;
		_nodeClass = nodeClass;
	}

	public static final List<NodeTypeEnum>	VALUES=Collections.unmodifiableList(Arrays.asList(values()));
	public static NodeTypeEnum fromString (final String s)
	{
		return CollectionsUtils.fromString(VALUES, s, false);
	}

	public static NodeTypeEnum fromNodeType (final short nodeType)
	{
		if (nodeType <= 0)
			return null;

		for (final NodeTypeEnum v : VALUES)
		{
			if ((v != null) && (v.getNodeType() == nodeType))
				return v;
		}

		return null;
	}

	public static NodeTypeEnum fromNode (final Node n)
	{
		if (null == n)
			return null;

		return fromNodeType(n.getNodeType());
	}

	public static NodeTypeEnum fromNodeClass (final Class<?> nodeClass)
	{
		if (null == nodeClass)
			return null;

		for (final NodeTypeEnum v : VALUES)
		{
			final Class<?>	vc=(null == v) /* should not happen */ ? null : v.getNodeClass();
			if ((vc != null) && vc.isAssignableFrom(nodeClass))
				return v;
		}

		return null;
	}
}
