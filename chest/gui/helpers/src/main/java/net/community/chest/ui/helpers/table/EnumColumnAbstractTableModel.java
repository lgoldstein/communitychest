package net.community.chest.ui.helpers.table;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import net.community.chest.dom.DOMUtils;
import net.community.chest.dom.transform.XmlConvertible;
import net.community.chest.reflect.ClassUtil;
import net.community.chest.swing.component.table.TableUtil;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * <P>Copyright 2007 as per GPLv2</P>
 *
 * <P>Uses an {@link Enum} value as the column identifier</P>
 * 
 * @param <E> The {@link Enum} used to identify the column
 * @param <V> The associated column value
 * @author Lyor G.
 * @since Aug 6, 2007 8:40:06 AM
 */
public abstract class EnumColumnAbstractTableModel<E extends Enum<E>,V>
		extends AbstractTypedTableModel<V>
		implements XmlConvertible<EnumColumnAbstractTableModel<E,V>> {
	/**
	 * 
	 */
	private static final long serialVersionUID = -7973719118906108888L;
	private final Class<E>	_colClass;
	public final /* no cheating */ Class<E> getColumnClass ()
	{
		return _colClass;
	}

	private List<E>	_colValues	/* =null */;
	// NOTE !!! by default calls getColumnClass().getEnumConstants()
	public synchronized List<E> getColumnsValues ()
	{
		if (null == _colValues)
			_colValues = Collections.unmodifiableList(Arrays.asList(getColumnClass().getEnumConstants()));
		return _colValues;
	}
	// NOTE !!! not necessarily all the enum values are contained
	public synchronized void setColumnsValues (List<E> colVals)
	{
		_colValues = colVals;
	}

	protected EnumColumnAbstractTableModel (Class<E> colClass, Class<V> valsClass, int initialSize, List<E> cols) throws IllegalArgumentException
	{
		super(valsClass, initialSize);

		if (null == (_colClass=colClass))
			throw new IllegalArgumentException("Missing column class specification");

		_colValues = cols;
	}

	protected EnumColumnAbstractTableModel (Class<E> colClass, Class<V> valsClass, List<E> cols) throws IllegalArgumentException
	{
		this(colClass, valsClass, 0, cols);
	}

	protected EnumColumnAbstractTableModel (Class<E> colClass, Class<V> valsClass, int initialSize) throws IllegalArgumentException
	{
		this(colClass, valsClass, initialSize, (List<E>) null);
	}

	protected EnumColumnAbstractTableModel (Class<E> colClass, Class<V> valsClass) throws IllegalArgumentException
	{
		this(colClass, valsClass, 0);
	}
	/**
	 * {@link Map} of {@link EnumTableColumn}-s - key={@link Enum} value
	 * representing the column
	 */
	private Map<E,EnumTableColumn<E>>	_colsMap	/* =null */;
	protected Map<E,EnumTableColumn<E>> getColumnsMap (final boolean createIfNotExist)
	{
		if ((null == _colsMap) && createIfNotExist)
			_colsMap = new EnumMap<E,EnumTableColumn<E>>(getColumnClass());
		return _colsMap;
	}

	protected Map<E,EnumTableColumn<E>> getColumnsMap ()
	{
		return getColumnsMap(false);
	}
	// CAVEAT EMPTOR if set after adding columns
	protected void setColumnsMap (Map<E,EnumTableColumn<E>> cm)
	{
		_colsMap = cm;
	}

	public EnumTableColumn<E> getColumnInfo (final E colIndex)
	{
		final Map<E,? extends EnumTableColumn<E>>	cm=getColumnsMap();
		return ((null == colIndex) || (null == cm) || (cm.size() <= 0)) ? null : cm.get(colIndex);
	}
	// returns column model index (negative if error)
	public int getColumnModelIndex (final E colIndex)
	{
		final TableColumn	tc=getColumnInfo(colIndex);
		return (null == tc) ? (-1) : tc.getModelIndex();
	}

	public EnumTableColumn<E> setColCellRenderer (
			final E colIndex, final TableCellRenderer r)
	{
		final EnumTableColumn<E>	col=getColumnInfo(colIndex);
		if (col != null)
			col.setCellRenderer(r);
		return col;
	}

	public EnumTableColumn<E> setColCellEditor (
			final E colIndex, final TableCellEditor e)
	{
		final EnumTableColumn<E>	col=getColumnInfo(colIndex);
		if (col != null)
			col.setCellEditor(e);
		return col;
	}

	public EnumTableColumn<E> setColHeaderRenderer (
			final E colIndex, final TableCellRenderer r)
	{
		final EnumTableColumn<E>	col=getColumnInfo(colIndex);
		if (col != null)
			col.setHeaderRenderer(r);
		return col;
	}
	// CAVEAT EMPTOR
	public Collection<? extends EnumTableColumn<E>> getColumnsInfoData ()
	{
		final Map<?,? extends EnumTableColumn<E>>	cm=getColumnsMap();
		return ((null == cm) || (cm.size() <= 0)) ? null : cm.values();
	}

	// returns a copy (!) to avoid concurrent modifications
	public List<EnumTableColumn<E>> getColumnsInfo ()
	{
		final Collection<? extends EnumTableColumn<E>>	cl=getColumnsInfoData();
		if ((null == cl) || (cl.size() <= 0))
			return null;

		return new ArrayList<EnumTableColumn<E>>(cl);
	}

	public <M extends TableColumnModel> M populateTableColumnModel (M m)
	{
		final Collection<? extends TableColumn>	tl=
			(null == m) ? null : getColumnsInfoData();
		if ((null == tl) || (tl.size() <= 0))
			return m;

		for (final TableColumn tc : tl)
		{
			if (null == tc)
				continue;
			m.addColumn(tc);
		}

		return m;
	}

	public EnumTableColumnModel<E> getTableColumnModel ()
	{
		final Map<?,?>	m=getColumnsMap(false);
		if ((null == m) || (m.size() <= 0))
			return null;

		return populateTableColumnModel(new EnumTableColumnModel<E>(getColumnClass()));	
	}

	public EnumTableColumn<E> fromColIndex (final int colIndex)
	{
		final Collection<? extends EnumTableColumn<E>>	cl=getColumnsInfoData();
		if ((null == cl) || (cl.size() <= 0))
			return null;

		return TableUtil.findTableColumn(colIndex, cl);
	}

	public E getColumnValue (int colIndex)
	{
		final EnumTableColumn<E>	colInfo=fromColIndex(colIndex);
		return (null == colInfo) ? null : colInfo.getColumnValue();
	}
	/**
	 * <P>Called by default {@link #getColumnValue(int, Object, int)}
	 * implementation to retrieve column display object</P>
	 * @param rowIndex row index
	 * @param row row associated value
	 * @param colIndex column index as an {@link Enum} - <B>Note:</B>
	 * may be null if by some internal error the ordinal could not be
	 * resolved.
	 * @return column display object
	 */
	public abstract Object getColumnValue (final int rowIndex, final V row, final E colIndex);
	/*
	 * @see net.community.chest.swing.component.table.AbstractTypedTableModel#getColumnValue(int, java.lang.Object, int)
	 */
	@Override
	public Object getColumnValue (int rowIndex, V row, int colIndex)
	{
		final EnumTableColumn<E>	colInfo=fromColIndex(colIndex);
		return getColumnValue(rowIndex, row, (null == colInfo) ? null : colInfo.getColumnValue());
	}
	/**
	 * <P>Called by default {@link #setValueAt(int, Object, int, Object)}
	 * implementation in order to set an associated row value data (if
	 * editing allowed of course)</P>
	 * @param rowIndex row index
	 * @param row row associated value
	 * @param colNum column index
	 * @param colIndex column index as an {@link Enum} - <B>Note:</B>
	 * may be null if by some internal error the ordinal could not be
	 * resolved.
	 * @param value value to be set for the specified column
	 */
	public abstract void setValueAt (int rowIndex, V row, int colNum, E colIndex, Object value);
	/*
	 * @see net.community.chest.swing.component.table.AbstractTypedTableModel#setValueAt(int, java.lang.Object, int, java.lang.Object)
	 */
	@Override
	public void setValueAt (int rowIndex, V row, int colIndex, Object value)
	{
		final EnumTableColumn<E>	colInfo=fromColIndex(colIndex);
		setValueAt(rowIndex, row, colIndex, (null == colInfo) ? null : colInfo.getColumnValue(), value);
	}

	public boolean isCellEditable (int rowIndex, V row, int colNum, E colIndex)
	{
		return (colIndex != null) && super.isCellEditable(rowIndex, row, colNum);
	}
	/*
	 * @see net.community.chest.ui.helpers.table.AbstractTypedTableModel#isCellEditable(int, java.lang.Object, int)
	 */
	@Override
	public boolean isCellEditable (int rowIndex, V row, int columnIndex)
	{
		final EnumTableColumn<E>	colInfo=fromColIndex(columnIndex);
		return isCellEditable(rowIndex, row, columnIndex, (null == colInfo) ? null : colInfo.getColumnValue());
	}
	/*
	 * @see javax.swing.table.TableModel#getColumnCount()
	 */
	@Override
	public int getColumnCount ()
	{
		return (null == _colsMap) ? 0 : _colsMap.size();
	}

	public Class<?> getColumnClass (E colIndex)
	{
		return (null == colIndex) ? null : Object.class;
	}
	/*
	 * @see javax.swing.table.AbstractTableModel#getColumnClass(int)
	 */
	@Override
	public Class<?> getColumnClass (final int columnIndex)
	{
		return getColumnClass(getColumnValue(columnIndex));
	}
	// NOTE: columns must be added BEFORE model is attached to a table
	public Map<E,EnumTableColumn<E>> addColumn (final EnumTableColumn<E> col)
	{
		final E	colIndex=(null == col) /* OK - ignored */ ? null : col.getColumnValue();
		if (colIndex != null)
		{
			final Map<E,EnumTableColumn<E>>	cm=getColumnsMap(true);
			final EnumTableColumn<E>		prev=cm.put(colIndex, col);
			if (prev != null)
				return cm;	// debug breakpoint
		}

		return getColumnsMap();
	}
	// NOTE: columns must be added BEFORE model is attached to a table
	public Map<E,EnumTableColumn<E>> addColumns (final EnumTableColumn<E> ... cols)
	{
		Map<E,EnumTableColumn<E>>	m=getColumnsMap(false);
		if ((null == cols) || (cols.length <= 0))
			return m;

		for (final EnumTableColumn<E> c : cols)
		{
			if (null == c)	// should not happen
				continue;

			m = addColumn(c);
		}

		return m;
	}
	/*
	 * @see javax.swing.table.AbstractTableModel#getColumnName(int)
	 */
	@Override
	public String getColumnName (final int column)
	{
		final EnumTableColumn<?>	colInfo=fromColIndex(column);
		final String				colName=(null == colInfo) ? null : colInfo.getColumnName();
		if ((colName != null) && (colName.length() > 0))
			return colName;

		final Object	colIndex=(null == colInfo) ? null : colInfo.getColumnValue();
		if (colIndex != null)
			return colIndex.toString();

		return super.getColumnName(column);
	}
	/**
	 * Default XML {@link Element} name used to detect a column definition
	 * by the default {@link #isColumnElement(Element, String)} implementation
	 */
	public static final String	COLUMN_ELEM_NAME="column";
	/**
	 * <P>Called by default {@link #fromXml(Element)} implementation in order
	 * to decide if an XML {@link Element} represents a column definition. If
	 * so then {@link #fromColumnElement(Element)} is called. Otherwise, the
	 * {@link #handleUnknownModelElement(Element)} is called (whose default
	 * implementation throws an exception)</P>
	 * @param elem XML element to be checked if represents a column definition
	 * @param tagName XML element tag name
	 * @return TRUE if OK to call {@link #fromColumnElement(Element)}
	 */
	public boolean isColumnElement (final Element elem, final String tagName)
	{
		return (elem != null) && COLUMN_ELEM_NAME.equalsIgnoreCase(tagName);
	}
	/**
	 * <P>Called by default {@link #fromXml(Element)} implementation in order
	 * to create a {@link EnumTableColumn} from the XML representation (which
	 * is assumed to have passed the {@link #isColumnElement(Element, String)}
	 * so the element tag name is not re-checked)</P>
	 * @param colElem XML element to be used as the column definition
	 * @return re-constructed column data - ignored if null
	 * @throws Exception if cannot re-construct the column data
	 */
	public EnumTableColumn<E> fromColumnElement (final Element colElem) throws Exception
	{
		return new EnumTableColumn<E>(getColumnClass(), colElem);
	}

	public void handleUnknownModelElement (final Element colElem) throws Exception
	{
		if (null == colElem)
			throw new DOMException(DOMException.HIERARCHY_REQUEST_ERR, ClassUtil.getExceptionLocation(getClass(), "handleUnknownModelElement") + " no " + Element.class.getName() + " instance");

		throw new DOMException(DOMException.NAMESPACE_ERR, ClassUtil.getArgumentsExceptionLocation(getClass(), "handleUnknownModelElement", colElem.getTagName()) + " unknown tag");
	}
	/*
	 * @see net.community.chest.dom.XmlConvertible#fromXml(org.w3c.dom.Element)
	 */
	@Override
	public EnumColumnAbstractTableModel<E,V> fromXml (final Element root) throws Exception
	{
		final Collection<? extends Element>	el=
			DOMUtils.extractAllNodes(Element.class, root, Node.ELEMENT_NODE);
		if ((el == null) || (el.size() <= 0))
			return this;

		for (final Element	colElem : el)
		{
			if (null == colElem)	// should not happen
				continue;

			final String	tagName=colElem.getTagName();
			if (isColumnElement(colElem, tagName))
			{
				final EnumTableColumn<E>	colInfo=fromColumnElement(colElem);
				if (colInfo != null)
					addColumn(colInfo);
			}
			else
				handleUnknownModelElement(colElem);
		}

		return this;
	}

	protected EnumColumnAbstractTableModel (final Class<E> colClass, final Class<V> valsClass, final int initialSize, final Element elem) throws Exception
	{
		this(colClass, valsClass, initialSize);

		final Object	inst=fromXml(elem);
		if (inst != this)
			throw new IllegalStateException(ClassUtil.getConstructorExceptionLocation(getClass()) + " mismatched instances");
	}

	protected EnumColumnAbstractTableModel (final Class<E> colClass, final Class<V> valsClass, final Element elem) throws Exception
	{
		this(colClass, valsClass, 0, elem);
	}
	/*
	 * @see net.community.chest.dom.XmlConvertible#toXml(org.w3c.dom.Document)
	 */
	@Override
	public Element toXml (Document doc) throws Exception
	{
		// TODO implement toXml
		throw new UnsupportedOperationException(ClassUtil.getExceptionLocation(getClass(), "toXml") + " N/A");
	}
	/**
	 * Assumes the current column widths are actually percentages of the total
	 * width and adjusts their widths accordingly
	 * @param totalWidth Total available width from which to calculated each
	 * column's percentage width
	 * @return A {@link Collection} of the columns whose widths have been changed
	 */
	public Collection<EnumTableColumn<E>> adjustRelativeColWidths (final int totalWidth)
	{
		final Collection<? extends E>	ce=getColumnsValues();
		if ((null == ce) || (ce.size() <= 0))
			return null;

		final Collection<EnumTableColumn<E>>	ret=new ArrayList<EnumTableColumn<E>>(ce.size());
		for (final E colIndex : ce)
		{
			final EnumTableColumn<E>	colInfo=getColumnInfo(colIndex);
			if (null == colInfo)	// should not happen
				continue;

			final int	colWidth=colInfo.getWidth(),
						newWidth=(colWidth * totalWidth) / 100;
			if (newWidth > colWidth)
				colInfo.setWidth(newWidth);
			ret.add(colInfo);
		}

		return ret;
	}
}
