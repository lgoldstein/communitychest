/*
 *
 */
package net.community.apps.tools.commenter;

import java.io.IOException;

import net.community.chest.io.output.LinePrintStream;
import net.community.chest.io.output.NullOutputStream;

/**
 * <P>Copyright as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Jun 30, 2009 12:48:12 PM
 */
final class StatusBarPrintStream extends LinePrintStream {
    private final MainFrame    _frame;
    public final MainFrame getMainFrame ()
    {
        return _frame;
    }

    public StatusBarPrintStream (MainFrame f)
    {
        super(new NullOutputStream());

        if (null == (_frame=f))
            throw new IllegalArgumentException("No main frame instance provided");
    }
    /*
     * @see net.community.chest.io.output.LineLevelAppender#isWriteEnabled()
     */
    @Override
    public boolean isWriteEnabled ()
    {
        return getMainFrame() != null;
    }
    /*
     * @see net.community.chest.io.output.LineLevelAppender#writeLineData(java.lang.StringBuilder, int)
     */
    @Override
    public void writeLineData (StringBuilder sb, int dLen) throws IOException
    {
        final MainFrame    f=(dLen > 0) ? getMainFrame() : null;
        if (f != null)
        {
            final String    s=sb.substring(0, dLen);
            f.updateStatusBar(s);
        }

        if (sb.length() > 0)
            sb.setLength(0);
    }
}
