/*
 * 
 */
package net.community.apps.tools.commenter;

import java.io.BufferedReader;
import java.io.EOFException;
import java.io.IOException;
import java.io.Writer;
import java.util.Collection;

import net.community.chest.dom.DOMUtils;
import net.community.chest.io.EOLStyle;
import net.community.chest.io.input.TokensReader;

/**
 * <P>Copyright as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Jun 25, 2009 3:17:39 PM
 */
public class XmlFileCommenter extends BaseCommenter {
	public XmlFileCommenter ()
	{
		super();
	}

	@Override
	protected void appendCommentLines (final String linePrefix, final Collection<String> lines, final Appendable w) throws IOException {
		w.append(DOMUtils.XML_COMMENT_START).append("\r\n");
		for (String	ll : lines)
		{
			if (ll.length() > 0) 
				w.append('\t').append(ll);
			lines.add(ll);
			w.append("\r\n");
		}
		w.append("\r\n").append(DOMUtils.XML_COMMENT_END).append("\r\n");
	}

	@Override
	protected <A extends Appendable> A appendCommentLine (String linePrefix, A w, String lineData) throws IOException
	{
		if ((lineData != null) && (lineData.length() > 0))
			w.append(DOMUtils.XML_COMMENT_START)
			 .append(' ')
			 .append(lineData)
			 .append(DOMUtils.XML_COMMENT_END)
			 .append("\r\n")
			 ;
		return w;
	}

	@Override
	public void addComment (BufferedReader r, Writer w, String cmnt, boolean isCommentFile)
		throws IOException
	{
		// skip the first non-empty line
		StringBuilder	sb=new StringBuilder(80);
		for (EOLStyle	eol=TokensReader.appendLine(sb, r); ; eol=TokensReader.appendLine(sb, r))
		{
			final int	sbLen=sb.length();
			if (sbLen <= 0)
				continue;

			w.append(sb).append("\r\n");

			for (int	cIndex=0; cIndex < sbLen; cIndex++)
			{
				final char	c=sb.charAt(cIndex);
				if ((c != DOMUtils.XML_ELEM_START_DELIM) || (cIndex >= (sbLen-1)))
					continue;

				final char	nc=sb.charAt(cIndex+1);
				// add comment after DOCTYPE or processing instructions
				if ((nc != '!') && (nc != '?'))
				{
					sb = null;
					break;
				}
			}

			if (null == sb)
				break;
			if (null == eol)
				throw new EOFException("addComment() exhausted all data");

			sb.setLength(0);
		}

		appendComment(null, w, cmnt, isCommentFile);
		appendContent(r, w);
	}
}
