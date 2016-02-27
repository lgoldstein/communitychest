/*
 * 
 */
package net.community.apps.tools.commenter;

import java.io.IOException;
import java.io.StreamCorruptedException;
import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.SwingWorker;

import net.community.chest.io.output.LinePrintStream;
import net.community.chest.io.output.NullOutputStream;
import net.community.chest.util.logging.LoggerWrapper;
import net.community.chest.util.logging.factory.WrapperFactoryManager;

/**
 * <P>Copyright as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Jun 30, 2009 1:12:58 PM
 */
final class CommentWorker extends SwingWorker<Void,String> {
	private static final LoggerWrapper	_logger=WrapperFactoryManager.getLogger(CommentWorker.class);

	private final MainFrame	_frame;
	public final MainFrame getMainFrame ()
	{
		return _frame;
	}

	public CommentWorker (final MainFrame f)
	{
		if (null == (_frame=f))
			throw new IllegalArgumentException("No main frame instance provided");
	}

	private final class WorkerPrintStream extends LinePrintStream {
		public WorkerPrintStream ()
		{
			super(new NullOutputStream());
		}
		/*
		 * @see net.community.chest.io.output.LineLevelAppender#isWriteEnabled()
		 */
		@Override
		public boolean isWriteEnabled ()
		{
			return (!isCancelled());
		}
		/*
		 * @see net.community.chest.io.output.LineLevelAppender#writeLineData(java.lang.StringBuilder, int)
		 */
		@SuppressWarnings("synthetic-access")
		@Override
		public void writeLineData (StringBuilder sb, int dLen) throws IOException
		{
			if (dLen > 0)
			{
				final String	msg=sb.substring(0, dLen);
				_logger.info(msg);
				publish(msg);
			}

			if (sb.length() > 0)
				sb.setLength(0);
		}
	}
	/*
	 * @see javax.swing.SwingWorker#doInBackground()
	 */
	@Override
	protected Void doInBackground () throws Exception
	{
        final MainFrame f=getMainFrame();
	    try {
    		if (f == null) {
    		    throw new IllegalStateException("No main frame provided");
    		}
    
    	    WorkerPrintStream ps=new WorkerPrintStream();
    	    try {
    	        f.run(ps);
    	    } finally {
    	        ps.close();
    	        
    	        if (ps.checkError()) {
    	            throw new StreamCorruptedException("Worker print stream exception");
    	        }
    	    }

            return null;
	    } catch(Exception e) {
           JOptionPane.showMessageDialog(f, e.getMessage(), e.getClass().getSimpleName(), JOptionPane.ERROR_MESSAGE);
           throw e;
	    }
	}
	/*
	 * @see javax.swing.SwingWorker#done()
	 */
	@Override
	protected void done ()
	{
		final MainFrame	f=getMainFrame();
		if (f != null)
			f.setRunningMode(false, true);
	}
	/*
	 * @see javax.swing.SwingWorker#process(java.util.List)
	 */
	@Override
	protected void process (List<String> chunks)
	{
		final MainFrame	f=((null == chunks) || (chunks.size() <= 0)) ? null :  getMainFrame();
		if (f != null)
		{
			for (final String m : chunks)
			{
				if ((null == m) || (m.length() <= 0))
					continue;
				f.updateStatusBar(m);
			}
		}
	}
}
