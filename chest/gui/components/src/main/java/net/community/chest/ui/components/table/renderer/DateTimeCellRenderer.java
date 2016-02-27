/*
 * 
 */
package net.community.chest.ui.components.table.renderer;

import java.awt.Component;
import java.text.DateFormat;
import java.util.Date;

import javax.swing.JTable;

import net.community.chest.awt.attributes.AttrUtils;
import net.community.chest.swing.component.table.BaseTableCellRenderer;

/**
 * <P>Copyright 2009 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Aug 4, 2009 4:40:17 PM
 */
public class DateTimeCellRenderer extends BaseTableCellRenderer {
	/**
	 * 
	 */
	private static final long serialVersionUID = 7817659966318002797L;
	private DateFormat	_fmt;
	public DateFormat getDateFormat ()
	{
		return _fmt;
	}

	public void setDateFormat (DateFormat f)
	{
		_fmt = f;
	}

	public DateTimeCellRenderer (DateFormat f)
	{
		_fmt = f;
	}
	
	public DateTimeCellRenderer ()
	{
		this(DateFormat.getDateTimeInstance());
	}

	protected String setText (Component c, Date d)
	{
		if ((!AttrUtils.isTextableComponent(c)) || (null == d))
			return null;

		final DateFormat	org=getDateFormat(),
							fmt=
			(null == org) ? DateFormat.getDateTimeInstance() : org;
		final String		t;
		synchronized(fmt)
		{
			t = fmt.format(d);
		}

		AttrUtils.setComponentText(c, t);
		return t;
	}

	protected String setText (Component c, long timestamp)
	{
		return setText(c, new Date(timestamp));
	}
	/*
	 * @see javax.swing.table.DefaultTableCellRenderer#getTableCellRendererComponent(javax.swing.JTable, java.lang.Object, boolean, boolean, int, int)
	 */
	@Override
	public Component getTableCellRendererComponent (JTable table, Object value,
			boolean isSelected, boolean hasFocus, int row, int column)
	{
		final Component	c=super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
		if (null == value)
			setText(c, null);
		else if (value instanceof Number)
			setText(c, ((Number) value).longValue());
		else if (value instanceof Date)
			setText(c, (Date) value);
		else if (value instanceof String)
			AttrUtils.setComponentText(c, value.toString());
		else
			throw new IllegalArgumentException("getTableCellRendererComponent(" + row + "," + column + ")[" + value + "] unknown value type: " + ((null == value) ? null : value.getClass().getName()));

		return c;
	}

}
