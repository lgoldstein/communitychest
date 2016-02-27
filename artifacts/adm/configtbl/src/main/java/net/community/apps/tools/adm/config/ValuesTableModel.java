/*
 * 
 */
package net.community.apps.tools.adm.config;

import java.util.NoSuchElementException;

import net.community.chest.CoVariantReturn;
import net.community.chest.dom.DOMUtils;
import net.community.chest.ui.helpers.table.EnumColumnAbstractTableModel;

import org.w3c.dom.DOMException;
import org.w3c.dom.Element;

/**
 * <P>Copyright 2009 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Oct 15, 2009 10:43:40 AM
 */
public class ValuesTableModel
			extends EnumColumnAbstractTableModel<ValuesTableColumn,ValueTableEntry> {
	public ValuesTableModel (final int initialSize)
	{
		super(ValuesTableColumn.class, ValueTableEntry.class, initialSize);
		setColumnsValues(ValuesTableColumn.getValues());
	}

	public ValuesTableModel ()
	{
		this(10);
	}
	/*
	 * @see net.community.chest.ui.helpers.table.EnumColumnAbstractTableModel#fromColumnElement(org.w3c.dom.Element)
	 */
	@Override
	@CoVariantReturn
	public ValuesTableColInfo fromColumnElement (Element colElem)
			throws Exception
	{
		final ValuesTableColInfo	colInfo=new ValuesTableColInfo(colElem);
		final ValuesTableColumn		colIndex=colInfo.getIdentifier();
		if (null == colIndex)
			throw new DOMException(DOMException.SYNTAX_ERR, "fromColumnElement(" + DOMUtils.toString(colElem) + ") no identifier");

		switch(colIndex)
		{
			case NAME	:
				colInfo.setCellRenderer(new NameColumnRenderer());
				break;

			case VALUE	:
			case SOURCE	:
				break;

			default		:
				throw new DOMException(DOMException.NAMESPACE_ERR, "fromColumnElement(" + DOMUtils.toString(colElem) + ") unknown identifier: " + colIndex);
		}

		return colInfo;
	}
	/*
	 * @see net.community.chest.ui.helpers.table.EnumColumnAbstractTableModel#getColumnClass(java.lang.Enum)
	 */
	@Override
	public Class<?> getColumnClass (ValuesTableColumn colIndex)
	{
		if (null == colIndex)
			throw new IllegalStateException("getColumnClass() no column");

		return String.class;
	}
	/*
	 * @see net.community.chest.ui.helpers.table.EnumColumnAbstractTableModel#getColumnValue(int, java.lang.Object, java.lang.Enum)
	 */
	@Override
	public Object getColumnValue (int rowIndex, ValueTableEntry row, ValuesTableColumn colIndex)
	{
		if (null == colIndex)
			throw new IllegalStateException("getColumnValue(" + rowIndex + ") no column");
		if (null == row)
			throw new IllegalStateException("getColumnValue(" + rowIndex + "/" + colIndex + ") no data");

		// NOTE !!! returned type must match getColumnClass() report + expected renderer/editor type
		switch(colIndex)
		{
			case NAME	: return row.getKey();
			case VALUE	: return row.getValue();
			case SOURCE	: return row.getOriginalValue();
			default			: 
				throw new NoSuchElementException("getColumnValue(" + rowIndex + "/" + colIndex + ")[" + row + "] unknown column requested");
		}
	}
	/*
	 * @see net.community.chest.ui.helpers.table.EnumColumnAbstractTableModel#setValueAt(int, java.lang.Object, java.lang.Enum, java.lang.Object)
	 */
	@Override
	public void setValueAt (int rowIndex, ValueTableEntry row, int colNum, ValuesTableColumn colIndex, Object value)
	{
		if (null == colIndex)
			throw new IllegalStateException("getColumnValue(" + rowIndex + ":" + colNum + ") no column");
		if (null == row)
			throw new IllegalStateException("getColumnValue(" + rowIndex + ":" + colNum +  ") no data");

		// should match "isCellEditable" value
		final String	sVal=(null == value) ? null : value.toString();
		if (!isCellEditable(rowIndex, row, colNum, colIndex))
			throw new NoSuchElementException("setValueAt(" + rowIndex + ":" + colNum + "/" + colIndex + ")[" + row + "]=" + sVal + " invalid column requested");

		switch(colIndex)
		{
			case NAME	:
				if ((null == sVal) || (sVal.length() <= 0))
					return;
				if (!Main.updateParamName(row, sVal))
					return;	// debug breakpoint
				break;

			case VALUE	:
				if (!Main.updateParamValue(row, sVal))
					return;	// debug breakpoint
				break;

			case SOURCE	:
				row.setOriginalValue(sVal);
				break;

			default			: 
				throw new NoSuchElementException("setValueAt(" + rowIndex + "/" + colIndex + ")[" + row + "]=" + sVal + " unknown column requested");
		}
	}
	/*
	 * @see net.community.chest.ui.helpers.table.EnumColumnAbstractTableModel#isCellEditable(int, java.lang.Object, int, java.lang.Enum)
	 */
	@Override
	public boolean isCellEditable (int rowIndex, ValueTableEntry row, int colNum, ValuesTableColumn colIndex)
	{
		return (rowIndex >= 0)
			&& (row != null)
			&& (colNum >= 0)
			&& (colIndex != null)
			// only value column is editable
			&& ValuesTableColumn.VALUE.equals(colIndex)
			;
	}
}
