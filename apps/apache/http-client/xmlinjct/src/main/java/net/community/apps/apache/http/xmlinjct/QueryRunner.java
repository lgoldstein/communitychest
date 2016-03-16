/*
 *
 */
package net.community.apps.apache.http.xmlinjct;

import java.util.List;

import javax.swing.SwingWorker;

import net.community.chest.Triplet;

import org.apache.commons.httpclient.Header;
import org.w3c.dom.Document;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Feb 16, 2009 12:09:06 PM
 */
public class QueryRunner extends SwingWorker<Triplet<String,Document,Header[]>,Triplet<String,Document,Header[]>> {
    private MainFrame    _frame;
    public final MainFrame getMainFrame ()
    {
        return _frame;
    }

    QueryRunner (MainFrame f) throws IllegalArgumentException
    {
        if (null == (_frame=f))
            throw new IllegalArgumentException("No frame supplied");
    }

    private long    _qDuration;
    /*
     * @see javax.swing.SwingWorker#doInBackground()
     */
    @SuppressWarnings("unchecked")
    @Override
    protected Triplet<String,Document,Header[]> doInBackground () throws Exception
    {
        final MainFrame                            f=getMainFrame();
        final long                                qStart=System.currentTimeMillis();
        final Triplet<String,Document,Header[]>    qRes=f.runQuery(this);
        final long                                qEnd=System.currentTimeMillis();
        _qDuration = qEnd - qStart;
        if (qRes != null)
            publish(qRes);
        return qRes;
    }
    /*
     * @see javax.swing.SwingWorker#done()
     */
    @Override
    protected void done ()
    {
        if (_frame != null)
        {
            _frame.signalQueryDone(this);
            _frame = null;
        }

        super.done();
    }
    /*
     * @see javax.swing.SwingWorker#process(java.util.List)
     */
    @Override
    protected void process (List<Triplet<String,Document,Header[]>> chunks)
    {
        final int                                numRes=(null == chunks) ? 0 : chunks.size();
        final Triplet<String,Document,Header[]>    qRes=(numRes != 1) ? null : chunks.get(0);
        final MainFrame                            f=getMainFrame();
        f.setQueryResult(qRes, _qDuration);
    }
}
