/*
 *
 */
package net.community.apps.apache.maven.pomrunner;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StreamCorruptedException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.SwingWorker;

import net.community.chest.io.IOCopier;
import net.community.chest.lang.StringUtil;
import net.community.chest.util.logging.LogLevelWrapper;

/**
 * <P>Copyright as per GPLv2</P>
 * @author Lyor G.
 * @since Mar 21, 2011 2:26:20 PM
 */
public class PomRunner extends SwingWorker<Void,OutputEntry> {
    private final MainFrame    _frame;
    public final MainFrame getMainFrame ()
    {
        return _frame;
    }

    PomRunner (final MainFrame frame)
    {
        if ((_frame=frame) == null)
            throw new IllegalStateException("No frame instance provided");
    }

    private Process    _process;
    /*
     * @see javax.swing.SwingWorker#doInBackground()
     */
    @Override
    protected Void doInBackground () throws Exception
    {
        final List<String> commands=new ArrayList<String>();
        {
            final String    osType=System.getProperty("os.name", "<unknown>").toLowerCase();
            if (!osType.contains("windows"))
                commands.add("/bin/sh");    // TODO make it a program argument
        }

        final MainFrame    frame=getMainFrame();
        final String    mavenHome=frame.getMavenHome(),
                        mavenCmd=frame.getMavenCommand(),
                        cmdPath=mavenHome + File.separator + "bin" + File.separator + mavenCmd;
        commands.add(cmdPath);

        final String    cmdArgs=frame.getExtraArguments();
        if ((cmdArgs != null) && (cmdArgs.length() > 0))
            commands.addAll(StringUtil.splitString(cmdArgs, Main.CMD_ARGS_SEP));

        commands.add("-f");
        commands.add(frame.getCurrentFilePath());

        final String    targets=frame.getTargets();
        if ((targets != null) && (targets.length() > 0))
            commands.addAll(StringUtil.splitString(targets, Main.TARGETS_SEP));

        final ProcessBuilder    procBuilder=new ProcessBuilder(commands);
        procBuilder.directory(new File(frame.getWorkingDirectory()));
        procBuilder.redirectErrorStream(true);

        _process = procBuilder.start();
        final MvnStreamGobbler    gobbler=new MvnStreamGobbler(_process.getInputStream());
        try
        {
            gobbler.start();

            final int    exitValue=_process.waitFor();
            if (exitValue != 0)
                publish(new OutputEntry(LogLevelWrapper.ERROR, "Failed to complete command: error=" + exitValue));

            gobbler.join(5000L);    // wait for the gobbler to exit on its own

            if (gobbler.isAlive())
            {
                final Thread.State threadState=gobbler.getState();
                publish(new OutputEntry(LogLevelWrapper.ERROR, "Gobbler stream still alive at state=" + threadState));
            }
        }
        finally
        {
            gobbler.close();
        }

        return null;
    }

    public void stop ()
    {
        if (_process != null)
            _process.destroy();
        cancel(true);
    }

    protected void processOutputLine (final String org)
    {
        String    line=org;
        if ((line == null) || (line.length() <= 0))
            return;

        if (line.charAt(line.length() - 1) == '\n')
            line = line.substring(0, line.length() - 1);
        if (line.charAt(line.length() - 1) == '\r')
            line = line.substring(0, line.length() - 1);
        if ((line=line.trim()).length() <= 0)
            return;

        LogLevelWrapper    level=null;
        if (line.charAt(0) == '[')
        {
            final int    sepPos=line.indexOf(']');
            if (sepPos > 0)
            {
                final String    pureLevel=line.substring(1, sepPos).trim();
                if ((level=LogLevelWrapper.fromString(pureLevel)) != null)
                    line = line.substring(sepPos + 1).trim();

                if ((line == null) || (line.length() <= 0))
                    return;
            }
        }

        if (level == null)
            level = LogLevelWrapper.VERBOSE;
        publish(new OutputEntry(level, line));
    }

    protected void handleGobblerException (IOException e)
    {
        final int    nRes=JOptionPane.showConfirmDialog(getMainFrame(), e.getMessage(), e.getClass().getSimpleName(), JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (nRes != JOptionPane.YES_OPTION)
            stop();
    }
    /*
     * @see javax.swing.SwingWorker#process(java.util.List)
     */
    @Override
    protected void process (List<OutputEntry> chunks)
    {
        final MainFrame    frame=getMainFrame();
        if (frame != null)
            frame.process(chunks);
    }
    /*
     * @see javax.swing.SwingWorker#done()
     */
    @Override
    protected void done ()
    {
        final MainFrame    frame=getMainFrame();
        if (frame != null)
            frame.done(this);
    }

    class MvnStreamGobbler extends Thread implements Closeable {
        private BufferedReader    _rdr;
        MvnStreamGobbler (InputStream input) throws IOException
        {
            if (input == null)
                throw new StreamCorruptedException("No input stream provided");
            _rdr = new BufferedReader(new InputStreamReader(input), IOCopier.DEFAULT_COPY_SIZE);
        }
        /*
         * @see java.lang.Thread#run()
         */
        @Override
        public void run ()
        {
            try
            {
                for (String line=_rdr.readLine(); line != null; line=_rdr.readLine())
                {
                    if (isCancelled())
                        break;    // debug breakpoint
                    processOutputLine(line);
                }
            }
            catch(IOException e)
            {
                handleGobblerException(e);
            }
        }
        /*
         * @see java.io.Closeable#close()
         */
        @Override
        public void close () throws IOException
        {
            if (_rdr != null)
            {
                try
                {
                    _rdr.close();
                }
                finally
                {
                    _rdr = null;
                }
            }
        }
    }
}
