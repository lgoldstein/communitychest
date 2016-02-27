/*
 * 
 */
package net.community.apps.apache.http.jmxaccessor;

import java.io.File;
import java.util.Collection;
import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.SwingWorker;

import net.community.chest.dom.DOMUtils;
import net.community.chest.jmx.JMXErrorHandler;
import net.community.chest.jmx.dom.MBeanEntryDescriptor;
import net.community.chest.net.proto.jmx.JMXAccessor;
import net.community.chest.swing.options.BaseOptionPane;
import net.community.chest.util.logging.LoggerWrapper;
import net.community.chest.util.logging.factory.WrapperFactoryManager;

import org.w3c.dom.Document;

/**
 * <P>Copyright as per GPLv2</P>
 * @author Lyor G.
 * @since Feb 15, 2011 11:06:49 AM
 *
 */
public class MBeansPopulator extends SwingWorker<Void,MBeanEntryDescriptor> implements JMXErrorHandler {
	private final MainFrame	_frame;
	final MainFrame getMainFrame ()
	{
		return _frame;
	}

	private static final LoggerWrapper	_logger=WrapperFactoryManager.getLogger(MBeansPopulator.class);
	/*
	 * @see net.community.chest.jmx.JMXErrorHandler#mbeanError(java.lang.String, java.lang.String, java.lang.Throwable)
	 */
	@Override
	public void mbeanError (String mbName, String msg, Throwable t)
	{
		JOptionPane.showMessageDialog(getMainFrame(), msg, t.getClass().getName(), JOptionPane.ERROR_MESSAGE);
		_logger.error("[" + mbName + "] " + msg, t);
	}
	/*
	 * @see net.community.chest.jmx.JMXErrorHandler#mbeanWarning(java.lang.String, java.lang.String, java.lang.Throwable)
	 */
	@Override
	public void mbeanWarning (String mbName, String msg, Throwable t)
	{
		JOptionPane.showMessageDialog(getMainFrame(), msg, t.getClass().getName(), JOptionPane.WARNING_MESSAGE);
		_logger.warn("[" + mbName + "] " + msg, t);
	}
	/*
	 * @see net.community.chest.jmx.JMXErrorHandler#errorThrowable(java.lang.Throwable)
	 */
	@Override
	public <T extends Throwable> T errorThrowable (T t)
	{
		BaseOptionPane.showMessageDialog(getMainFrame(), t);
		return _logger.errorThrowable(t);
	}
	/*
	 * @see net.community.chest.jmx.JMXErrorHandler#warnThrowable(java.lang.Throwable)
	 */
	@Override
	public <T extends Throwable> T warnThrowable (T t)
	{
		BaseOptionPane.showMessageDialog(getMainFrame(), t);
		return _logger.warnThrowable(t);
	}

	private MBeansPopulator (MainFrame frame)
	{
		if ((_frame=frame) == null)
			throw new IllegalStateException("No frame instance provided");
	}

	private File	_descsFile;
	final File getFile ()
	{
		return _descsFile;
	}

	public MBeansPopulator (MainFrame frame, File descsFile)
	{
		this(frame);

		if ((_descsFile=descsFile) == null)
			throw new IllegalStateException("No file instance provided");
	}

	private JMXAccessor	_accessor;
	final JMXAccessor getAccessor ()
	{
		return _accessor;
	}

	public MBeansPopulator (MainFrame frame, JMXAccessor accessor)
	{
		this(frame);

		if ((_accessor=accessor) == null)
			throw new IllegalStateException("No accessor instance provided");
	}
	/*
	 * @see javax.swing.SwingWorker#doInBackground()
	 */
	@Override
	protected Void doInBackground () throws Exception
	{
		final Collection<? extends MBeanEntryDescriptor>	descs;
		final File											descsFile=getFile();
		if (descsFile != null)
		{
			final Document	doc=DOMUtils.loadDocument(descsFile);
			descs = MBeanEntryDescriptor.readMBeans(doc, this);
		}
		else
		{
			final JMXAccessor accessor=getAccessor();
			descs = accessor.listFull(null, null);
		}

		final int	numDescs=(descs == null) ? 0 : descs.size();
		if (numDescs > 0)
			publish(descs.toArray(new MBeanEntryDescriptor[numDescs]));
		return null;
	}
	/*
	 * @see javax.swing.SwingWorker#process(java.util.List)
	 */
	@Override
	protected void process (List<MBeanEntryDescriptor> chunks)
	{
		final MainFrame	frame=getMainFrame();
		frame.process(chunks);
	}
	/*
	 * @see javax.swing.SwingWorker#done()
	 */
	@Override
	protected void done ()
	{
		final MainFrame	frame=getMainFrame();
		frame.done(this);
	}
}
