/*
 *
 */
package net.community.apps.tools.commenter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Writer;

import net.community.chest.io.EOLStyle;

/**
 * <P>Copyright as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Jun 29, 2009 3:18:39 PM
 */
public class DosBatchFileCommenter extends BaseCommenter {
    public DosBatchFileCommenter ()
    {
        super();
    }

    @Override
    protected EOLStyle getEOLStyle ()
    {
        return EOLStyle.CRLF;
    }

    @Override
    public void addComment (BufferedReader r, Writer w, String cmnt, boolean isCommentFile) throws IOException
    {
        appendComment("REM ", w, cmnt, isCommentFile);
        w.append("\r\n");
        appendContent(r, w);
    }

}
