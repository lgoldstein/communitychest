/*
 * 
 */
package net.community.chest.dom.impl;

import org.w3c.dom.Comment;

/**
 * <P>Copyright 2009 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Aug 26, 2009 12:20:15 PM
 */
public class StandaloneCommentImpl extends BaseCharacterDataImpl<Comment> implements Comment {
	public StandaloneCommentImpl (String baseURI, String name, String value)
	{
		super(Comment.class, baseURI, name, value);
	}

	public StandaloneCommentImpl (String name, String value)
	{
		this(null, name, value);
	}

	public static final String	DEFAULT_COMMENT_NODE_NAME="#comment";
	public StandaloneCommentImpl (String value)
	{
		this(DEFAULT_COMMENT_NODE_NAME, value);
	}

	public StandaloneCommentImpl ()
	{
		this(null);
	}
	/*
	 * @see org.w3c.dom.Node#getNodeType()
	 */
	@Override
	public final short getNodeType ()
	{
		return COMMENT_NODE;
	}
}
