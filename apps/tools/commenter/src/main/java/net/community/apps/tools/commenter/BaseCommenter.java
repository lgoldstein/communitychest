/*
 * 
 */
package net.community.apps.tools.commenter;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

import net.community.chest.io.EOLStyle;
import net.community.chest.io.FileUtil;
import net.community.chest.io.IOCopier;

/**
 * <P>Copyright as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Jun 25, 2009 2:27:47 PM
 */
public abstract class BaseCommenter implements Commenter {
    private final char[]    workBuf=new char[4 * IOCopier.DEFAULT_COPY_SIZE];
    private Collection<String>  commentLines;

	protected BaseCommenter ()
	{
		super();
	}

	protected EOLStyle getEOLStyle ()
	{
		return EOLStyle.LOCAL;
	}

	protected void appendContent (final Reader r, final Writer w) throws IOException
	{
		final long	cpyLen=IOCopier.copyReaderToWriter(r, w, workBuf);
		if (cpyLen < 0L)
			throw new IOException("Failed (err=" + cpyLen + ") to copy content");
	}

	protected <A extends Appendable> A appendCommentLine (
			final String linePrefix, final A w, final String lineData) throws IOException
	{
		if ((linePrefix != null) && (linePrefix.length() > 0))
			w.append(linePrefix);
		if ((lineData != null) && (lineData.length() > 0))
			w.append(lineData);
		return appendEOL(w);
	}

	protected <A extends Appendable> A appendEOL (final A w) throws IOException {
		EOLStyle	style=getEOLStyle();
		return style.appendEOL(w);
	}

    protected void appendCommentLines (final String linePrefix, final Collection<String> lines, final Appendable w) throws IOException {
        if (lines == null) {
            return;
        }

        for (String ll : lines) {
            appendCommentLine(linePrefix, w, ll);
        }
    }

	protected Collection<String> appendCommentFile (final String linePrefix, final BufferedReader r, final Appendable w) throws IOException	{
	    Collection<String> lines=new LinkedList<String>();
		for (String	ll=r.readLine(); ll != null; ll = r.readLine()) {
			lines.add(ll);
		}
		
        appendCommentLines(linePrefix, lines, w);
		return lines;
	}

	protected Collection<String> appendComment (
			final String linePrefix, final Appendable w, final String cmnt, final boolean isCommentFile)
		throws IOException
	{
	    if (commentLines == null) {
    		if (isCommentFile)
    		{
    			BufferedReader	r=new BufferedReader(new FileReader(cmnt), IOCopier.DEFAULT_COPY_SIZE);
    			try
    			{
    			    commentLines = appendCommentFile(linePrefix, r, w);
    			}
    			finally
    			{
    				FileUtil.closeAll(r);
    			}
    		} else {
    			appendCommentLine(linePrefix, w, cmnt);
    			commentLines = Collections.singletonList(cmnt);
    		}
	    } else {
	        appendCommentLines(linePrefix, commentLines, w);
	    }
	    
	    return commentLines;
	}
}
