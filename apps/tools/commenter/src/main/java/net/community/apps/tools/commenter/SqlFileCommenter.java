/*
 * 
 */
package net.community.apps.tools.commenter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Writer;

/**
 * <P>Copyright as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Jun 29, 2009 3:19:53 PM
 */
public class SqlFileCommenter extends BaseCommenter {
	public SqlFileCommenter ()
	{
		super();
	}

	@Override
	public void addComment (BufferedReader r, Writer w, String cmnt, boolean isCommentFile) throws IOException
	{
		appendComment("-- ", w, cmnt, isCommentFile);
		w.append("\r\n");
		appendContent(r, w);
	}

}
