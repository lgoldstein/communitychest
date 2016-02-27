/*
 * 
 */
package net.community.chest.ui.components.table.file;

import java.awt.Component;
import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;

import javax.swing.Icon;
import javax.swing.JTable;

import net.community.chest.awt.attributes.AttrUtils;
import net.community.chest.awt.image.AbstractImageReader;
import net.community.chest.io.file.FileAttributeType;
import net.community.chest.swing.component.table.BaseTableCellRenderer;

/**
 * <P>Copyright 2009 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Aug 4, 2009 4:54:02 PM
 */
public class FileAttrsCellRenderer extends BaseTableCellRenderer {
	/**
	 * 
	 */
	private static final long serialVersionUID = -3074082573052213297L;
	public static final Collection<FileAttributeType>	CHECKED_ATTRIBUTES=
		Arrays.asList(	// NOTE !!! order is important
				FileAttributeType.ISDIR,
				FileAttributeType.READABLE,
				FileAttributeType.WRITEABLE,
				FileAttributeType.EXECUTABLE
			);
	public static final Collection<FileAttributeType> getFileAttributes (final File f)
	{
		if (null == f)
			return null;

   		Collection<FileAttributeType>	ret=null;
   		for (final FileAttributeType a : CHECKED_ATTRIBUTES)
   		{
   			final Object	o=(null == a) ? null : a.getValue(f);
   			if (!(o instanceof Boolean))
   				continue;
   			if (!((Boolean) o).booleanValue())
   				continue;
   			if (null == ret)
   				ret = EnumSet.noneOf(FileAttributeType.class);
   			ret.add(a);
   		}

   		return ret;
	}

	private Map<FileAttributeType,?>	_attrsMap;
	public Map<FileAttributeType,?> getAttributesMap ()
	{
		return _attrsMap;
	}

	public void setAttributesMap (Map<FileAttributeType,?> m)
	{
		_attrsMap = m;
	}

	public FileAttrsCellRenderer (Map<FileAttributeType,?> m)
	{
		_attrsMap = m;
	}

	private static final EnumMap<FileAttributeType,String>	_defaultAttrsMap=
		new EnumMap<FileAttributeType,String>(FileAttributeType.class);
	public static final Map<FileAttributeType,String> getDefaultAttributesMap ()
	{
		synchronized(_defaultAttrsMap)
		{
			if (_defaultAttrsMap.size() <= 0)
			{
				final Object[]	vals={
						FileAttributeType.ISDIR, 		"d",
						FileAttributeType.READABLE, 	"r",
						FileAttributeType.WRITEABLE,	"w",
						FileAttributeType.EXECUTABLE,	"x"
					};
				for (int	vIndex=0; vIndex < vals.length; vIndex += 2)
					_defaultAttrsMap.put((FileAttributeType) vals[vIndex], (String) vals[vIndex+1]);
			}
		}

		return _defaultAttrsMap.clone();
	}
	
	public FileAttrsCellRenderer ()
	{
		this(getDefaultAttributesMap());
	}

	protected String setText (final Component c, final Collection<FileAttributeType> attrs)
	{
		if (!AttrUtils.isTextableComponent(c))
			return null;

		final StringBuilder				sb=new StringBuilder(16);
		final Map<FileAttributeType,?>	m=getAttributesMap();
		Map<FileAttributeType,?>		d=null;
   		for (final FileAttributeType a : CHECKED_ATTRIBUTES)
   		{
   			final boolean	isSetAttr=
   				(a != null) && (attrs != null) && (attrs.size() > 0) && attrs.contains(a);
   			if (isSetAttr)
   			{
   				Object	o=(null == m) ? null : m.get(a);
   				String	s=(null == o) ? null : o.toString();
   				if ((null == s) || (s.length() <= 0))
   				{
   					if (null == d)
   						d = getDefaultAttributesMap();
   					s = d.get(a).toString();
   				}
   				sb.append(s);
   			}
   			else
   				sb.append('-');	// TODO allow customization for this as well
   		}

   		final String	t=sb.toString();
   		AttrUtils.setComponentText(c, t);
   		return t;
	}

	protected String setText (final Component c, final File f)
	{
		return setText(c, (this == c) ? getFileAttributes(f) : null);  
	}

	protected Icon getIcon (final File f)
	{
		if (AbstractImageReader.isImageFile(f.getAbsolutePath()))
			return null;	// TODO load the file itself
		return null;
	}

	protected Icon setIcon (final Component c, final File f)
	{
		if (!AttrUtils.isIconableComponent(c))
			return null;

		final Icon	icon=getIcon(f);
		AttrUtils.setComponentIcon(c, icon);
		return icon;
	}
	/*
	 * @see javax.swing.table.DefaultTableCellRenderer#getTableCellRendererComponent(javax.swing.JTable, java.lang.Object, boolean, boolean, int, int)
	 */
	@Override
	public Component getTableCellRendererComponent (JTable table, Object value,
			boolean isSelected, boolean hasFocus, int row, int column)
	{
		final Component	c=super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
		final File		f;
		if (null == value)
			f = null;
		else if (value instanceof File)
			f = (File) value;
		else if (value instanceof String)
			f = new File(value.toString());
		else if (value instanceof Collection<?>)
		{
			@SuppressWarnings("unchecked")
			final Collection<FileAttributeType>	attrs=
				(Collection<FileAttributeType>) value;
			setText(c, attrs);
			return c;
		}
		else
			throw new IllegalArgumentException("getTableCellRendererComponent(" + row + "," + column + ")[" + value + "] unknown value type: " + ((null == value) ? null : value.getClass().getName()));

		setText(c, f);
		setIcon(c, f);
		return c;
	}
}
