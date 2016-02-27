/*
 * 
 */
package net.community.chest.ui.components.dialog.manifest;

import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import net.community.chest.CoVariantReturn;
import net.community.chest.ui.helpers.table.EnumTableColumn;

import org.w3c.dom.Element;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Sep 23, 2008 10:48:01 AM
 */
public class ManifestTableCol extends EnumTableColumn<ManifestTableColumns> {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1579586600078609215L;
	public ManifestTableCol (ManifestTableColumns colIndex, int colWidth,
							 TableCellRenderer colRenderer, TableCellEditor colEditor)
							throws IllegalArgumentException
	{
		super(ManifestTableColumns.class, colIndex, colWidth, colRenderer, colEditor);
	}

	public ManifestTableCol (ManifestTableColumns colIndex, int colWidth)
	{
		this(colIndex, colWidth, null, null);
	}

	public ManifestTableCol (ManifestTableColumns colIndex)
	{
		this(colIndex, 75);
	}

	public ManifestTableCol (Element elem) throws Exception
	{
		super(ManifestTableColumns.class, elem);
	}
	/*
	 * @see net.community.chest.swing.component.table.BaseTableColumn#getColumnConverter(org.w3c.dom.Element)
	 */
	@Override
	@CoVariantReturn
	protected ManifestTableColReflectiveProxy getColumnConverter (final Element elem) throws Exception
	{
		return (null == elem) ? null : ManifestTableColReflectiveProxy.DEFAULT;
	}
}
