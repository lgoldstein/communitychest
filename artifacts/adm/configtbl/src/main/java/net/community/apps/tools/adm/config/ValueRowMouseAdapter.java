/*
 * 
 */
package net.community.apps.tools.adm.config;

import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;

import org.w3c.dom.Element;

/**
 * <P>Copyright 2009 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Oct 20, 2009 2:43:38 PM
 */
public class ValueRowMouseAdapter extends MouseAdapter {
	private final ValuesTable	_tbl;
	public final ValuesTable getValuesTable ()
	{
		return _tbl;
	}

	private final MainFrame	_owner;
	public final MainFrame getOwnerFrame ()
	{
		return _owner;
	}

	private final Element	_dlgEditElement;
	public final Element getDlgEditElement ()
	{
		return _dlgEditElement;
	}

	public ValueRowMouseAdapter (MainFrame owner, ValuesTable tbl, Element dlgEditElement)
	{
		if (null == (_owner=owner))
			throw new IllegalArgumentException("No owner frame supplied");
		if (null == (_tbl=tbl))
			throw new IllegalArgumentException("No table supplied");
		if (null == (_dlgEditElement=dlgEditElement))
			throw new IllegalArgumentException("No dialog edit element supplied");
	}
	/*
	 * @see java.awt.event.MouseAdapter#mouseClicked(java.awt.event.MouseEvent)
	 */
	@Override
	public void mouseClicked (final MouseEvent e)
	{
		final Point	clickPt=(null == e) ? null : e.getPoint();
		if (null == clickPt)			// should not happen
			return;

		final int	rowIndex=_tbl.rowAtPoint(clickPt);
		if (SwingUtilities.isLeftMouseButton(e))	// left click
		{
			if (e.getClickCount() < 2)	// only double-click(s)
				return;

			if (rowIndex < 0) // interpret as "new row"
			{

				final ValueTableEntry	vte=new ValueTableEntry();
				final MainFrame			owner=getOwnerFrame();
				final ValueEditDialog	dlg=
					new ValueEditDialog(owner, vte, getDlgEditElement(), true);
				dlg.setVisible(true);

				if (!dlg.isOkExit())
					return;

				if (!Main.createNewEntry(vte))
					return;

				if (!owner.addConfigValue(vte))
					return;
			}
			else // interpret as "edit row"
			{
				final ValuesTable						tbl=getValuesTable();
				final List<? extends ValueTableEntry>	sl=
					(null == tbl) ? null : tbl.getSelectedValues();
				final int								numSelected=
					(null == sl) ? 0 : sl.size();
				if (numSelected != 1)
				{
					JOptionPane.showMessageDialog(null, "Bad # of selected rows: " + numSelected, "Edit row", JOptionPane.ERROR_MESSAGE);
					return;
				}

				final ValueTableEntry	vte=sl.get(0);
				final ValueEditDialog	dlg=
					new ValueEditDialog(getOwnerFrame(), vte, getDlgEditElement(), true);
				dlg.setVisible(true);
				
				if ((!dlg.isOkExit()) || (!vte.isChangedEntry()))
					return;

				if (!Main.updateParamValue(vte, vte.getValue()))
					return;	// debug breakpoint
				
				final TableModel	m=tbl.getModel();
				if (m instanceof AbstractTableModel)
					((AbstractTableModel) m).fireTableRowsUpdated(rowIndex, rowIndex);
			}
		}
		else if (SwingUtilities.isRightMouseButton(e))	// show context menu
		{
			if (e.getClickCount() > 1)	// only single click
				return;

			if (rowIndex < 0)
			{
				// show only "New row" enabled
				JOptionPane.showMessageDialog(null, "New row context TBD", "New row context", JOptionPane.ERROR_MESSAGE);
			}
			else
			{
				// show only "Delete/Edit" enabled
				JOptionPane.showMessageDialog(null, "Delete/Edit row context TBD", "Delete/Edit row context", JOptionPane.ERROR_MESSAGE);
			}
		}
	}
}
