/*
 *
 */
package net.community.chest.swing;

import javax.swing.SwingWorker;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * <P>Simply embeds a {@link Runnable} and runs it on {@link #doInBackground()} call</P>
 *
 * @author Lyor G.
 * @since Feb 19, 2009 1:35:13 PM
 */
public class SwingWorkerRunner extends SwingWorker<Void,Void> {
    private Runnable    _r;
    public Runnable getRunnable ()
    {
        return _r;
    }
    // CAVEAT EMPTOR - if called after "execute()" may have no effect
    public void setRunnable (Runnable r)
    {
        _r = r;
    }

    public SwingWorkerRunner (Runnable r)
    {
        _r = r;
    }

    public SwingWorkerRunner ()
    {
        this(null);
    }
    /*
     * @see javax.swing.SwingWorker#doInBackground()
     */
    @Override
    protected Void doInBackground () throws Exception
    {
        if (!isCancelled())    // maybe was able to call "setCancelled"
        {
            final Runnable    r=getRunnable();
            if (null == r)
                throw new IllegalStateException("No " + Runnable.class.getSimpleName() + " instance provided");
            r.run();
        }

        return null;
    }

    public static final SwingWorker<Void,Void> executeInSwingWorker (Runnable r) throws IllegalArgumentException
    {
        if (null == r)
            throw new IllegalArgumentException("No " + Runnable.class.getSimpleName() + " instance provided");


        final SwingWorker<Void,Void>    worker=new SwingWorkerRunner(r);
        worker.execute();
        return worker;
    }
}
