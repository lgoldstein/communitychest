/*
 * 
 */
package net.community.apps.tools.jardiff;

import java.util.Collection;
import java.util.Collections;

import javax.swing.SwingWorker;

import net.community.chest.swing.options.BaseOptionPane;

/**
 * <P>Copyright as per GPLv2</P>
 * @author Lyor G.
 * @since Mar 8, 2011 10:47:21 AM
 */
abstract class AbstractJarPaneProcessor extends SwingWorker<Void,JarEntriesMatchRow> {
	private final MainFrame	_frame;
	public final MainFrame getMainFrame ()
	{
		return _frame;
	}

	private final Collection<? extends JarComparisonPane>	_panes;
	protected AbstractJarPaneProcessor (MainFrame frame, Collection<? extends JarComparisonPane> panes)
	{
		if ((_frame=frame) == null)
			throw new IllegalStateException("No frame instance provided");

		if (panes == null)
			_panes = Collections.emptyList();
		else
			_panes = panes;
	}

	protected abstract void processPane (JarComparisonPane pane) throws Exception;
	/*
	 * @see javax.swing.SwingWorker#doInBackground()
	 */
	@Override
	protected final Void doInBackground () throws Exception
	{
		for (final JarComparisonPane pane : _panes)
		{
			if (isCancelled())
				break;
			try
			{
				processPane(pane);
			}
			catch(Exception e)
			{
				BaseOptionPane.showMessageDialog(getMainFrame(), e);
			}
		}

		return null;
	}

}
