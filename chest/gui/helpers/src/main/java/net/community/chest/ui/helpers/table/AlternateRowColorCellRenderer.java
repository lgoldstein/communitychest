package net.community.chest.ui.helpers.table;

import java.awt.Color;
import java.awt.Component;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * <P>Paints each row with a different {@link Color} - based on the modulu of
 * the rendered <U>row</U> index.</P></BR>
 * 
 * <P><B>Note:</B> this {@link TableCellRenderer} has no special handling
 * for the <U>selection</U> state - i.e., foreground/background color(s)
 * for the row.
 * 
 * @author Lyor G.
 * @since Mar 25, 2008 12:19:37 PM
 */
public class AlternateRowColorCellRenderer extends JLabel implements TableCellRenderer {
	/**
	 * 
	 */
	private static final long serialVersionUID = -1229763694853566872L;
	private final List<Color>	_rowColors=new ArrayList<Color>();
	/**
	 * @return <P>The {@link List} of the current row colors where the
	 * {@link Color} at index <I>N</I> shows the each <I>N</I><B>+1</B>th
	 * row color (null means no special coloring).</P></BR>
	 * <B>Note:</B> for efficiency reasons, the <U>internal</U> (shared)
	 * instance is returned - please don't change it unless you really
	 * know what you are doing
	 */
	public final List<Color> getCurrentColors ()
	{
		return _rowColors;
	}
	/**
	 * @param rowIndex Zero based row index
	 * @return The {@link Color} for rendering this row index - null if
	 * no special color set
	 */
	public Color getRowColor (final int rowIndex)
	{
		if (rowIndex < 0)
			return null;

		final List<? extends Color>	cl=getCurrentColors();
		final int					numColors=(null == cl) ? 0 : cl.size();
		if (numColors <= 0)
			return null;

		final int	lIndex=(numColors > 1) ? (rowIndex % numColors) : 0;
		return cl.get(lIndex);
	}
	/**
	 * @param colors The {@link Color}-s in order of <U>ascending</U>
	 * modulu value (e.g. index=2 holds the color of each 3rd row). If
	 * <code>null</code> is specified then this specific row modulu will
	 * not be assigned any color. If no colors specified, then same as if
	 * no colors for any row
	 */
	public void setColors (final Iterable<? extends Color> colors)
	{
		_rowColors.clear();

		for (final Iterator<? extends Color> ic=(null == colors) ? null : colors.iterator();
			 (ic != null) && ic.hasNext();
			 )
			_rowColors.add(ic.next());
	}

	public void setColors (final Color ... colors)
	{
		setColors(((null == colors) || (colors.length <= 0)) ? null : Arrays.asList(colors));
	}

	public AlternateRowColorCellRenderer (final Iterable<? extends Color> colors)
	{
		setColors(colors);
	}

	public AlternateRowColorCellRenderer (final Color ... colors)
	{
		setColors(colors);
	}

	public AlternateRowColorCellRenderer ()
	{
		super();
	}
	/*
	 * @see javax.swing.table.TableCellRenderer#getTableCellRendererComponent(javax.swing.JTable, java.lang.Object, boolean, boolean, int, int)
	 */
	@Override
	public Component getTableCellRendererComponent (JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
	{
		Color	fg=isSelected ? table.getSelectionForeground() : table.getForeground(),
				bg=isSelected ? table.getSelectionBackground() : table.getBackground();
		if (!isSelected)
		{
			final Color	rowColor=getRowColor(row);
			if (rowColor != null)
				bg = rowColor;
		}

		setText((null == value) ? "" : value.toString());
		setForeground(fg);
		setBackground(bg);
		return this;
	}
}
