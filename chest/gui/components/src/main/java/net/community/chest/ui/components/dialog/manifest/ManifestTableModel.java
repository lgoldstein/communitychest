package net.community.chest.ui.components.dialog.manifest;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import net.community.chest.CoVariantReturn;
import net.community.chest.io.jar.ManifestInfoExtractor;
import net.community.chest.reflect.ClassUtil;
import net.community.chest.ui.helpers.table.EnumColumnAbstractTableModel;
import net.community.chest.util.map.entries.StringPairEntry;

import org.w3c.dom.Element;

/**
 * <P>Copyright 2007 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Aug 6, 2007 2:30:20 PM
 */
public class ManifestTableModel extends EnumColumnAbstractTableModel<ManifestTableColumns,StringPairEntry> {
	/**
	 * 
	 */
	private static final long serialVersionUID = 801932699268248028L;
	// NOTE: must call "fireTableDataChanged" event afterwards
	public void setManifestData (final Manifest m)
	{
		final int	curAttrs=size();
		if (curAttrs > 0)
			clear();

		final Attributes							mainAttrs=(null == m) ? null : m.getMainAttributes();
		final Collection<? extends Map.Entry<?,?>>	attrs=((null == mainAttrs) || (mainAttrs.size() <= 0)) ? null : mainAttrs.entrySet();
		if ((attrs != null) && (attrs.size() > 0))
		{
			for (final Map.Entry<?,?> a : attrs)
			{
				final Attributes.Name	k=(Attributes.Name) ((null == a) ? null : a.getKey());
				final Object			v=(null == a) ? null : a.getValue();
				if ((k != null) || (v != null))
					add(new StringPairEntry((null == k) ? null : k.toString(), (null == v) ? null : v.toString()));
			}
		}
	}

	public void setManifestData (final Class<?> anchor) throws IOException
	{
		setManifestData(ManifestInfoExtractor.getAnchorClassManifest(anchor));
	}

	public ManifestTableModel (Manifest m)
	{
		super(ManifestTableColumns.class, StringPairEntry.class);
		setColumnsValues(ManifestTableColumns.VALUES);	// make sure using the cached instance

		if (m != null)
			setManifestData(m);
	}

	public ManifestTableModel (Class<?> anchor) throws IOException
	{
		this(ManifestInfoExtractor.getAnchorClassManifest(anchor));
	}

	public ManifestTableModel ()
	{
		this((Manifest) null);
	}
	/*
	 * @see net.community.chest.swing.component.table.EnumColumnAbstractTableModel#getColumnClass(int)
	 */
	@Override
	public Class<?> getColumnClass (int columnIndex)
	{
		return String.class;	// all the values are String(s)
	}
	/*
	 * @see net.community.chest.swing.component.table.EnumColumnAbstractTableModel#getColumnValue(int, java.lang.Object, java.lang.Enum)
	 */
	@Override
	@CoVariantReturn
	public String getColumnValue (int rowIndex, StringPairEntry row, ManifestTableColumns colIndex)
	{
		if (null == colIndex)
			throw new NoSuchElementException(ClassUtil.getArgumentsExceptionLocation(getClass(), "getColumnValue", Integer.valueOf(rowIndex), colIndex) + " unresolved column");

		switch(colIndex)
		{
			case ATTR_NAME	:
				return (null == row) ? null : row.getKey();
			case ATTR_VALUE	:
				return (null == row) ? null : row.getValue();
			default			:
				throw new IllegalStateException(ClassUtil.getArgumentsExceptionLocation(getClass(), "getColumnValue", Integer.valueOf(rowIndex), colIndex) + " unexpected column");
		}
	}
	/*
	 * @see net.community.chest.ui.helpers.table.EnumColumnAbstractTableModel#setValueAt(int, java.lang.Object, int, java.lang.Enum, java.lang.Object)
	 */
	@Override
	public void setValueAt (int rowIndex, StringPairEntry row, int colNum, ManifestTableColumns colIndex, Object value)
	{
		if (null == colIndex)
			throw new NoSuchElementException(ClassUtil.getArgumentsExceptionLocation(getClass(), "setValueAt", Integer.valueOf(rowIndex), Integer.valueOf(colNum), colIndex, value) + " unresolved column");

		switch(colIndex)
		{
			case ATTR_NAME	: row.setKey((null == value) ? null : value.toString()); break;
			case ATTR_VALUE	: row.setValue((null == value) ? null : value.toString()); break;
			default			:
				throw new IllegalStateException(ClassUtil.getArgumentsExceptionLocation(getClass(), "setValueAt", Integer.valueOf(rowIndex), Integer.valueOf(colNum), colIndex, value) + " unexpected column");
		}
	}
	/**
	 * Special XML {@link Element} name that can be used to add pre-defined
	 * (ready-made) manifest entries
	 */
	public static final String	PREDEFINED_MANIFEST_ENTRY_ELEM_NAME="attribute";
	public Map.Entry<String,String> addAttribute (final Element elem) throws Exception
	{
		final String	aName=elem.getAttribute("name"),
						aValue=elem.getAttribute("value");
		if ((null == aName) || (aName.length() <= 0))
			throw new IllegalStateException("addAttribute() no attribute name specified");

		final StringPairEntry	ae=new StringPairEntry(aName, (null == aValue) ? "" : aValue);
		add(ae);
		return ae;
	}
	/*
	 * @see net.community.chest.swing.component.table.EnumColumnAbstractTableModel#handleUnknownModelElement(org.w3c.dom.Element)
	 */
	@Override
	public void handleUnknownModelElement (Element colElem) throws Exception
	{
		final String	tagName=colElem.getTagName();
		if (PREDEFINED_MANIFEST_ENTRY_ELEM_NAME.equalsIgnoreCase(tagName))
		{
			addAttribute(colElem);
			return;
		}

		super.handleUnknownModelElement(colElem);
	}
	/*
	 * @see net.community.chest.swing.component.table.EnumColumnAbstractTableModel#fromColumnElement(org.w3c.dom.Element)
	 */
	@Override
	@CoVariantReturn
	public ManifestTableCol fromColumnElement (Element colElem) throws Exception
	{
		return new ManifestTableCol(colElem);
	}

	public ManifestTableModel (Element elem, Manifest m) throws Exception
	{
		this();

		final EnumColumnAbstractTableModel<ManifestTableColumns,StringPairEntry>	inst=fromXml(elem);
		if (inst != this)
			throw new IllegalStateException(ClassUtil.getConstructorExceptionLocation(getClass()) + " mismatched instances");

		if (m != null)
			setManifestData(m);
	}
	
	public ManifestTableModel (Element elem, Class<?> anchor) throws Exception
	{
		this(elem, ManifestInfoExtractor.getAnchorClassManifest(anchor));
	}

	public ManifestTableModel (Element elem) throws Exception
	{
		this(elem, (Manifest) null);
	}
}
