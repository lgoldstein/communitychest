/*
 * 
 */
package net.community.apps.tools.jardiff;

import java.util.NoSuchElementException;
import java.util.zip.ZipEntry;

import net.community.chest.CoVariantReturn;
import net.community.chest.dom.DOMUtils;
import net.community.chest.ui.helpers.table.EnumColumnAbstractTableModel;

import org.w3c.dom.Element;

/**
 * <P>Copyright as per GPLv2</P>
 * @author Lyor G.
 * @since Mar 2, 2011 12:15:29 PM
 */
public class JarEntriesTableModel extends EnumColumnAbstractTableModel<JarEntriesTableColumns,JarEntriesMatchRow> {
	/**
	 * 
	 */
	private static final long serialVersionUID = -297809337495164537L;
	public JarEntriesTableModel ()
	{
		super(JarEntriesTableColumns.class, JarEntriesMatchRow.class);
		setColumnsValues(JarEntriesTableColumns.VALUES);
	}
	/*
	 * @see net.community.chest.ui.helpers.table.EnumColumnAbstractTableModel#fromColumnElement(org.w3c.dom.Element)
	 */
	@Override
	@CoVariantReturn
	public JarEntriesTableColInfo fromColumnElement (Element colElem) throws Exception
	{
		return new JarEntriesTableColInfo(colElem);
	}
	/*
	 * @see net.community.chest.ui.helpers.table.EnumColumnAbstractTableModel#fromXml(org.w3c.dom.Element)
	 */
	@Override
	@CoVariantReturn
	public JarEntriesTableModel fromXml (Element root) throws Exception
	{
		final Object	instance=super.fromXml(root);
		if (instance != this)
			throw new IllegalStateException("fromXml(" + DOMUtils.toString(root) + "] mismatched reconstructed instances");

		return this;
	}
	/*
	 * @see net.community.chest.ui.helpers.table.EnumColumnAbstractTableModel#getColumnValue(int, java.lang.Object, java.lang.Enum)
	 */
	@Override
	public Object getColumnValue (int rowIndex, JarEntriesMatchRow row, JarEntriesTableColumns colIndex)
	{
		if (null == colIndex)
			throw new IllegalStateException("getColumnValue(" + rowIndex + ") no column");

		final ZipEntry	entry=(row == null) ? null : row.getCurrentJarEntry();
		if (null == entry)
			throw new IllegalStateException("getColumnValue(" + rowIndex + "/" + colIndex + ") no row data");

		switch(colIndex)
		{
			case ENTRY_NAME	:
			case ENTRY_PATH	:
				final String	name=(null == entry) /* should not happen */ ? null : stripLastSeparator(entry.getName());
				final int		nLen=(null == name) ? 0 : name.length(),
								sPos=(nLen <= 1) ? (-1) : name.lastIndexOf('/');
				if (JarEntriesTableColumns.ENTRY_NAME.equals(colIndex))
				{
					if ((sPos > 0) && (sPos < (nLen -1)))
						return name.substring(sPos + 1);
					else
						return "";
				}
				else
				{
					if (sPos > 0)
						return name.substring(0, sPos);
					else
						return name;
				}


			case ENTRY_SIZE	:
				return Long.valueOf(entry.getSize());

			case ENTRY_TIME	:
				return Long.valueOf(entry.getTime());

			default			:
				throw new NoSuchElementException("getColumnValue(" + rowIndex + "/" + colIndex + ") unknown column requested");
		}
	}

	private static final String stripLastSeparator (final String org)
	{
		final int	len=(org == null) ? 0 : org.length();
		if ((len <= 0) || (org.charAt(len - 1) != '/'))
			return org;

		return org.substring(0, len - 1);
	}
	/*
	 * @see net.community.chest.ui.helpers.table.EnumColumnAbstractTableModel#setValueAt(int, java.lang.Object, int, java.lang.Enum, java.lang.Object)
	 */
	@Override
	public void setValueAt (int rowIndex, JarEntriesMatchRow row, int colNum, JarEntriesTableColumns colIndex, Object value)
	{
		throw new UnsupportedOperationException("setValueAt(" + rowIndex + ":" + colNum + "/" + colIndex + ")::=" + value + " - N/A");
	}
}
