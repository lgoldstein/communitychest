/*
 * 
 */
package net.community.chest.swing.component.table;

import java.util.Vector;

import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

import net.community.chest.awt.attributes.Backgrounded;
import net.community.chest.awt.attributes.Enabled;
import net.community.chest.awt.attributes.Foregrounded;
import net.community.chest.awt.attributes.Tooltiped;
import net.community.chest.dom.proxy.XmlProxyConvertible;
import net.community.chest.dom.transform.XmlConvertible;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Jan 20, 2009 9:21:59 AM
 */
public class BaseTable extends JTable
		implements XmlConvertible<BaseTable>, Tooltiped, Foregrounded, Backgrounded, Enabled {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2278737220208859380L;

	public BaseTable (TableModel dm, TableColumnModel cm, ListSelectionModel sm)
	{
		super(dm, cm, sm);
	}

	public BaseTable (TableModel dm, TableColumnModel cm)
	{
		this(dm, cm, null);
	}

	public BaseTable (TableModel dm)
	{
		this(dm, (TableColumnModel) null);
	}

	public BaseTable ()
	{
		this((TableModel) null);
	}

	public BaseTable (int numRows, int numColumns)
	{
		super(numRows, numColumns);
	}

	public BaseTable (Vector<?> rowData, Vector<?> columnNames)
	{
		super(rowData, columnNames);
	}

	public BaseTable (Object[][] rowData, Object[] columnNames)
	{
		super(rowData, columnNames);
	}

	public XmlProxyConvertible<? extends JTable> getTableConverter (Element elem) throws Exception
	{
		return (null == elem) ? null : JTableReflectiveProxy.TBL;
	}
	/*
	 * @see net.community.chest.dom.transform.XmlConvertible#fromXml(org.w3c.dom.Element)
	 */
	@Override
	public BaseTable fromXml (Element elem) throws Exception
	{
		final XmlProxyConvertible<? extends JTable>	proxy=getTableConverter(elem);
		@SuppressWarnings({ "unchecked", "rawtypes" })
		final Object								o=((XmlProxyConvertible) proxy).fromXml(this, elem);
		if (o != this)
			throw new IllegalStateException("fromXml(" + elem + ") mismatched reconstructed instances");

		return this;
	}
	/*
	 * @see net.community.chest.dom.transform.XmlConvertible#toXml(org.w3c.dom.Document)
	 */
	@Override
	public Element toXml (Document doc) throws Exception
	{
		// TODO implement "toXml"
		throw new UnsupportedOperationException("toXml() N/A");
	}
	
	public void addTableModelListener (TableModelListener l)
	{
		final TableModel	m=(null == l) ? null : getModel();
		if (null == m)
			return;

		m.addTableModelListener(l);
	}
	
	public void removeTableModelListener (TableModelListener l)
	{
		final TableModel	m=(null == l) ? null : getModel();
		if (null == m)
			return;

		m.removeTableModelListener(l);
	}

	public int getSelectionMode ()
	{
		final ListSelectionModel	m=getSelectionModel();
		return (null == m) ? (-1) : m.getSelectionMode();
	}
}
