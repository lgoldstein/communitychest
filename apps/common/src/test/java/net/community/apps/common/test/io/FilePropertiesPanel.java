/*
 * 
 */
package net.community.apps.common.test.io;

import java.io.File;
import java.util.Collection;
import java.util.EnumMap;
import java.util.Map;

import net.community.chest.awt.TypedComponentAssignment;
import net.community.chest.io.file.FileAttributeType;
import net.community.chest.ui.helpers.panel.PresetGridLayoutPanel;

/**
 * <P>Copyright GPLv2</P>
 *
 * @author Lyor G.
 * @since Apr 26, 2009 10:06:55 AM
 */
public class FilePropertiesPanel extends PresetGridLayoutPanel implements TypedComponentAssignment<File> {
	/**
	 * 
	 */
	private static final long serialVersionUID = 8294691451638655129L;
	private Map<FileAttributeType,FilePropertyValue>	_propsMap;
	/*
	 * @see net.community.chest.awt.TypedComponentAssignment#getAssignedValue()
	 */
	@Override
	public File getAssignedValue ()
	{
		final Collection<? extends TypedComponentAssignment<? extends File>>	pl=
			((null == _propsMap) || (_propsMap.size() <= 0)) ? null : _propsMap.values();
		if ((null == pl) || (pl.size() <= 0))
			return null;

		// NOTE: we assume same file in all components
		for (final TypedComponentAssignment<? extends File> p : pl)
		{
			final File	f=(null == p) ? null : p.getAssignedValue();
			if (f != null)
				return f;
		}

		return null;
	}
	/*
	 * @see net.community.chest.awt.TypedComponentAssignment#setAssignedValue(java.lang.Object)
	 */
	@Override
	public void setAssignedValue (File value)
	{
		final Collection<? extends TypedComponentAssignment<? super File>>	pl=
			((null == _propsMap) || (_propsMap.size() <= 0)) ? null : _propsMap.values();
		if ((null == pl) || (pl.size() <= 0))
			return;

		for (final TypedComponentAssignment<? super File> p : pl)
		{
			if (null == p)
				continue;
			p.setAssignedValue(value);
		}
	}
	/*
	 * @see net.community.chest.ui.helpers.panel.HelperPanel#layoutComponent()
	 */
	@Override
	public void layoutComponent ()
	{
		super.layoutComponent();

		if (null == _propsMap)
			_propsMap = new EnumMap<FileAttributeType,FilePropertyValue>(FileAttributeType.class);

		for (final FileAttributeType a : FileAttributeType.VALUES)
		{
			final FilePropertyValue	v=new FilePropertyValue(a);
			_propsMap.put(a, v);
			add(v);
		}
	}

	public FilePropertiesPanel ()
	{
		super(0, 1, 0, 5);
	}
}
