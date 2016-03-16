/*
 *
 */
package net.community.apps.tools.commenter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StreamCorruptedException;
import java.io.Writer;

/**
 * <P>Copyright as per GPLv2</P>
 * @author Lyor G.
 * @since May 7, 2012 1:40:36 PM
 *
 */
public class JavaFileCommenter extends BaseCommenter {
    public JavaFileCommenter ()
    {
        super();
    }

    @Override
    public void addComment (BufferedReader r, Writer w, String cmnt, boolean isCommentFile) throws IOException
    {
        for (String    packageLine=r.readLine(); packageLine != null; packageLine=r.readLine())
        {
            packageLine    = packageLine.trim();
            if (!packageLine.startsWith("package "))
                continue;

            appendEOL(w.append("/*"));
            appendComment(" * ", w, cmnt, isCommentFile);
            appendEOL(w.append(" */"));
            appendEOL(w);

            appendEOL(w.append(packageLine));
            appendContent(r, w);
            return;
        }

        throw new StreamCorruptedException("No package line found");
    }

}
