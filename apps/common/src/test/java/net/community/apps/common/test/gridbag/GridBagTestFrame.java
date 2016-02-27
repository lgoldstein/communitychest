package net.community.apps.common.test.gridbag;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import net.community.apps.common.test.TestMainFrame;
import net.community.chest.swing.component.panel.DefaultPanelScroll;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Mar 19, 2008 12:59:52 PM
 */
public class GridBagTestFrame extends TestMainFrame implements ActionListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = -8414159460041224679L;
	protected static class PairPanel extends JPanel implements Map.Entry<GridRowSelectionPanel,GridRowButtonsPanel> {
		/**
		 * 
		 */
		private static final long serialVersionUID = -58864248462358748L;
		private final GridRowSelectionPanel	_selPanel;
		public final GridRowSelectionPanel getSelectionPanel ()
		{
			return _selPanel;
		}

		private final GridRowButtonsPanel	_btnPanel;
		public final GridRowButtonsPanel getButtonsPanel ()
		{
			return _btnPanel;
		}

		public PairPanel (GridRowSelectionPanel selPanel, GridRowButtonsPanel btnPanel)
		{
			super(new ModifiedGridLayout(0, 2));

			if ((_selPanel=selPanel) != null)
				add(selPanel);
			if ((_btnPanel=btnPanel) != null)
				add(btnPanel);
		}
		/*
		 * @see java.util.Map.Entry#getKey()
		 */
		@Override
		public GridRowSelectionPanel getKey ()
		{
			return getSelectionPanel();
		}
		/*
		 * @see java.util.Map.Entry#getValue()
		 */
		@Override
		public GridRowButtonsPanel getValue ()
		{
			return getButtonsPanel();
		}
		/*
		 * @see java.util.Map.Entry#setValue(java.lang.Object)
		 */
		@Override
		public GridRowButtonsPanel setValue (GridRowButtonsPanel value)
		{
			throw new UnsupportedOperationException("setValue(" + value + ") N/A");
		}
	}
	private List<PairPanel>	_rl;
	protected synchronized List<PairPanel> getRowsList ()
	{
		if (null == _rl)
			_rl = new ArrayList<PairPanel>(100);
		return _rl;
	}

	protected JPanel	_rowsPanel;
	private class RowDeletionListener implements ActionListener {
		private final PairPanel	_row;
		protected RowDeletionListener (final PairPanel row)
		{
			_row = row;
		}
		/*
		 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
		 */
		@Override
		public void actionPerformed (final ActionEvent event)
		{
			final List<? extends PairPanel>	rl=getRowsList();
			if (rl.size() <= 1)
			{
				JOptionPane.showMessageDialog(null, "Not allowed to delete last row", "Last row", JOptionPane.ERROR_MESSAGE);
				return;
			}

			_rowsPanel.remove(_row);
			rl.remove(_row);

			if (rl.size() <= 1)
			{
				final Map.Entry<GridRowSelectionPanel,GridRowButtonsPanel>	lastRow=rl.get(0);
				final GridRowButtonsPanel									btnPanel=lastRow.getValue();
				final JButton												delBtn=btnPanel.getButton(false);
				delBtn.setEnabled(false);
			}
		}
	}

	private AtomicInteger	_rc;
	protected synchronized AtomicInteger getRowsCounter ()
	{
		if (null == _rc)
			_rc = new AtomicInteger(0);
		return _rc;
	}

	public PairPanel addRow ()
	{
		final AtomicInteger			rc=getRowsCounter();
		final int					rowIndex=rc.incrementAndGet();
		final GridRowSelectionPanel	rowPanel=new GridRowSelectionPanel("Row #" + rowIndex);
		final GridRowButtonsPanel	btnPanel=new GridRowButtonsPanel();
		final PairPanel				row=new PairPanel(rowPanel, btnPanel);
		btnPanel.addButtonListener(Boolean.TRUE, this);
		btnPanel.addButtonListener(Boolean.FALSE, new RowDeletionListener(row));
		_rowsPanel.add(row);
		
		final List<? super PairPanel>	rl=getRowsList();
		rl.add(row);
		return row;
	}
	/*
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed (final ActionEvent event)
	{
		final PairPanel					row=addRow();
		final List<? extends PairPanel>	rl=getRowsList();
		if ((row != null) && (rl.size() == 2))
		{
			final Map.Entry<GridRowSelectionPanel,GridRowButtonsPanel>	lastRow=rl.get(0);
			final GridRowButtonsPanel									btnPanel=lastRow.getValue();
			final JButton												delBtn=btnPanel.getButton(false);
			delBtn.setEnabled(true);
		}
	}
	/*
	 * @see net.community.apps.common.BaseMainFrame#layoutComponent()
	 */
	@Override
	public void layoutComponent () throws RuntimeException
	{
		super.layoutComponent();

		_rowsPanel = new JPanel(new GridLayout(0,1));

		final Container	ctPane=getContentPane();	
		ctPane.add(new DefaultPanelScroll(_rowsPanel), BorderLayout.CENTER);

		addRow();
	}

	public GridBagTestFrame (String... args) throws Exception
	{
		super(args);
	}
}
