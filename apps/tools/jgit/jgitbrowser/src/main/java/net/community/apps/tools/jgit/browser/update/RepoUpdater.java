/*
 *
 */
package net.community.apps.tools.jgit.browser.update;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.swing.SwingWorker;

import net.community.apps.tools.jgit.browser.MainFrame;
import net.community.chest.swing.options.BaseOptionPane;

import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ProgressMonitor;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.transport.FetchResult;
import org.eclipse.jgit.transport.RefSpec;
import org.eclipse.jgit.transport.TrackingRefUpdate;
import org.eclipse.jgit.transport.Transport;

/**
 * <P>Copyright as per GPLv2</P>
 * @author Lyor G.
 * @since Mar 23, 2011 11:56:08 AM
 */
public class RepoUpdater extends SwingWorker<Void,Object> implements ProgressMonitor {

    private final MainFrame _frame;
    public final MainFrame getMainFrame ()
    {
        return _frame;
    }

    private final boolean    _mergeAfterUpdate;
    public final boolean isMergeAfterUpdate ()
    {
        return _mergeAfterUpdate;
    }

    public RepoUpdater (final MainFrame frame, final boolean mergeAfterUpdate)
    {
        if ((_frame=frame) == null)
            throw new IllegalStateException("No frame instance provided");
        _mergeAfterUpdate = mergeAfterUpdate;
    }
    /*
     * @see javax.swing.SwingWorker#doInBackground()
     */
    @Override
    protected Void doInBackground () throws Exception
    {
        final MainFrame        frame=getMainFrame();
        try
        {
            // code based on org.eclipse.jgit.pgm.Fetch command implementation
            final Repository    repo=(frame == null) ? null : frame.getRepository();
            final Transport        tn=(repo == null) ? null : Transport.open(repo, Constants.DEFAULT_REMOTE_NAME);
            if (tn != null)
                tn.setTimeout(30);

            final Collection<RefSpec>    refSpec=Collections.emptyList();
            showFetchResults((tn == null) ? null : tn.fetch(this, refSpec));
            return null;
        }
        catch(Exception e)
        {
            BaseOptionPane.showMessageDialog(frame, e);
            throw e;
        }
    }

    private void showFetchResults (final FetchResult res)
    {
        final Collection<? extends TrackingRefUpdate>    updates=
            (res == null) ? null : res.getTrackingRefUpdates();
        if ((updates == null) || updates.isEmpty())
            return;
    }
    /*
     * @see javax.swing.SwingWorker#process(java.util.List)
     */
    @Override
    protected void process (List<Object> chunks)
    {
        // TODO Auto-generated method stub
        super.process(chunks);
    }
    /*
     * @see javax.swing.SwingWorker#done()
     */
    @Override
    protected void done ()
    {
        // TODO Auto-generated method stub
        super.done();
    }

    /*
     * @see org.eclipse.jgit.lib.ProgressMonitor#start(int)
     */
    @Override
    public void start (int totalTasks)
    {
        // TODO Auto-generated method stub

    }

    /*
     * @see org.eclipse.jgit.lib.ProgressMonitor#beginTask(java.lang.String, int)
     */
    @Override
    public void beginTask (String title, int totalWork)
    {
        // TODO Auto-generated method stub

    }

    /*
     * @see org.eclipse.jgit.lib.ProgressMonitor#update(int)
     */
    @Override
    public void update (int completed)
    {
        // TODO Auto-generated method stub

    }

    /*
     * @see org.eclipse.jgit.lib.ProgressMonitor#endTask()
     */
    @Override
    public void endTask ()
    {
        // TODO Auto-generated method stub

    }
}
