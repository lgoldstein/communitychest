/*
 *
 */
package net.community.apps.apache.ant.antrunner;

import java.io.File;
import java.util.Collection;
import java.util.List;

import javax.swing.SwingWorker;
import javax.swing.text.BadLocationException;

import net.community.chest.apache.ant.build.BuildEventInfo;
import net.community.chest.apache.ant.build.BuildEventsHandler;
import net.community.chest.apache.ant.build.BuildFileExecutor;
import net.community.chest.swing.options.BaseOptionPane;
import net.community.chest.util.logging.LoggerWrapper;
import net.community.chest.util.logging.factory.WrapperFactoryManager;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Feb 16, 2009 9:35:19 AM
 */
class BuildRunner extends SwingWorker<Void,BuildEventInfo> implements BuildEventsHandler {
    private static final LoggerWrapper    _logger=WrapperFactoryManager.getLogger(BuildRunner.class);

    private MainFrame    _frame;
    public final MainFrame getMainFrame ()
    {
        return _frame;
    }

    public final File getFilePath ()
    {
        final MainFrame f=getMainFrame();
           return (null == f) ? null : f.getFilePath();
    }

    private final String    _tgtName;
    public final String getTargetName ()
    {
        return _tgtName;
    }

    public final Collection<String> getArguments ()
    {
        final MainFrame f=getMainFrame();
           return (null == f) ? null : f.getArguments();
    }

    BuildRunner (MainFrame f, String tgtName) throws IllegalArgumentException
    {
        if (null == (_frame=f))
            throw new IllegalArgumentException("No frame supplied");
        if ((null == (_tgtName=tgtName)) || (tgtName.length() <= 0))
            throw new IllegalArgumentException("No target specified");
    }
    /*
     * @see net.community.chest.apache.ant.build.BuildEventsHandler#handleEvent(net.community.chest.apache.ant.build.BuildEventInfo)
     */
    @Override
    public EventHandleResult handleEvent (final BuildEventInfo eventInfo)
    {
        if (_logger.isVerboseEnabled())
            _logger.verbose("handleEvent(" + eventInfo + ")");

        if (isCancelled())
            return EventHandleResult.ABORT;

        if (eventInfo != null)
            publish(eventInfo);

        if (isDone())
            return EventHandleResult.FINISHED;
        return EventHandleResult.CONTINUE;
    }
    /*
     * @see javax.swing.SwingWorker#doInBackground()
     */
    @Override
    protected Void doInBackground () throws Exception
    {
        final File        f=getFilePath();
        final String    filePath=(null == f) ? null : f.getAbsolutePath(),
                        tgtName=getTargetName();
           try
           {
               _logger.info("doInBackground(" + filePath + ")[" + tgtName + "] starting...");

               // NOTE: we use reflection API since we need the ANT classes provided by the class loader
               final Thread                t=Thread.currentThread();
               final ClassLoader            cl=t.getContextClassLoader();
               final Class<?>                rc=getClass();
               final Package                rp=rc.getPackage();
               final String                rpName=rp.getName();
               final Class<?>                eeClass=cl.loadClass(rpName + ".EventsExecutor"),
                                           diClass=cl.loadClass(rpName + ".DataInputHandler");
               final Collection<String>    args=getArguments();
               final EventHandleResult        res=
                BuildFileExecutor.executeScript(filePath, tgtName, eeClass, diClass, cl, args);
               if (!EventHandleResult.FINISHED.equals(res))
                   throw new IllegalStateException("Execution result incomplete: " + res);

               _logger.info("doInBackground(" + filePath + ")[" + tgtName + "] finished");
           }
           catch(Throwable e)
           {
               _logger.error("doInBackground(" + filePath + ")[" + tgtName + "] " + e.getClass().getName() + ": " + e.getMessage(), e);
            BaseOptionPane.showMessageDialog(getMainFrame(), e);
           }

        return null;
    }
    /*
     * @see javax.swing.SwingWorker#done()
     */
    @Override
    protected void done ()
    {
        if (_frame != null)
        {
            _frame.signalBuildDone(this);
            _frame = null;
        }

        super.done();
    }
    /*
     * @see javax.swing.SwingWorker#process(java.util.List)
     */
    @Override
    protected void process (final List<BuildEventInfo> chunks)
    {
        if ((null == chunks) || (chunks.size() <= 0))
            return;

        final MainFrame    f=getMainFrame();
        if (null == f)
            return;

        for (final BuildEventInfo event : chunks)
        {
            if (isCancelled())
                return;

            try
            {
                f.logEvent(event);
            }
            catch(BadLocationException e)
            {
                _logger.warn(e.getClass().getName() + " while logging event=" + event + ": " + e.getMessage(), e);
            }
        }
    }
}
