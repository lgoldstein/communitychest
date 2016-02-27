/*
 * 
 */
package net.community.chest.swing.component.button;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import javax.swing.AbstractButton;
import javax.swing.Icon;

import net.community.chest.util.collection.CollectionsUtils;

/**
 * <P>Copyright GPLv2</P>
 *
 * <P>Encapsulate the various types of {@link Icon}-s that can be configured
 * for an {@link AbstractButton}</P>
 * 
 * @author Lyor G.
 * @since Jun 22, 2009 8:48:53 AM
 */
public enum ButtonIconType {
	DEFAULT {
			/*
			 * @see net.community.chest.swing.component.button.ButtonIconType#getIcon(javax.swing.AbstractButton)
			 */
			@Override
			public Icon getIcon (AbstractButton b)
			{
				return (null == b) ? null : b.getIcon();
			}
			/*
			 * @see net.community.chest.swing.component.button.ButtonIconType#setIcon(javax.swing.AbstractButton, javax.swing.Icon)
			 */
			@Override
			public void setIcon (AbstractButton b, Icon i)
			{
				if (b != null)
					b.setIcon(i);
			}
		},
	PRESSED {
			/*
			 * @see net.community.chest.swing.component.button.ButtonIconType#getIcon(javax.swing.AbstractButton)
			 */
			@Override
			public Icon getIcon (AbstractButton b)
			{
				return (null == b) ? null : b.getPressedIcon();
			}
			/*
			 * @see net.community.chest.swing.component.button.ButtonIconType#setIcon(javax.swing.AbstractButton, javax.swing.Icon)
			 */
			@Override
			public void setIcon (AbstractButton b, Icon i)
			{
				if (b != null)
					b.setPressedIcon(i);
			}
		},
	SELECTED {
			/*
			 * @see net.community.chest.swing.component.button.ButtonIconType#getIcon(javax.swing.AbstractButton)
			 */
			@Override
			public Icon getIcon (AbstractButton b)
			{
				return (null == b) ? null : b.getSelectedIcon();
			}
			/*
			 * @see net.community.chest.swing.component.button.ButtonIconType#setIcon(javax.swing.AbstractButton, javax.swing.Icon)
			 */
			@Override
			public void setIcon (AbstractButton b, Icon i)
			{
				if (b != null)
					b.setSelectedIcon(i);
			}
		},
	DISABLED {
			/*
			 * @see net.community.chest.swing.component.button.ButtonIconType#getIcon(javax.swing.AbstractButton)
			 */
			@Override
			public Icon getIcon (AbstractButton b)
			{
				return (null == b) ? null : b.getDisabledIcon();
			}
			/*
			 * @see net.community.chest.swing.component.button.ButtonIconType#setIcon(javax.swing.AbstractButton, javax.swing.Icon)
			 */
			@Override
			public void setIcon (AbstractButton b, Icon i)
			{
				if (b != null)
					b.setDisabledIcon(i);
			}
		},
	ROLLOVER {
			/*
			 * @see net.community.chest.swing.component.button.ButtonIconType#getIcon(javax.swing.AbstractButton)
			 */
			@Override
			public Icon getIcon (AbstractButton b)
			{
				return (null == b) ? null : b.getRolloverIcon();
			}
			/*
			 * @see net.community.chest.swing.component.button.ButtonIconType#setIcon(javax.swing.AbstractButton, javax.swing.Icon)
			 */
			@Override
			public void setIcon (AbstractButton b, Icon i)
			{
				if (b != null)
					b.setRolloverIcon(i);
			}
		},
	SELDISABLED {
			/*
			 * @see net.community.chest.swing.component.button.ButtonIconType#getIcon(javax.swing.AbstractButton)
			 */
			@Override
			public Icon getIcon (AbstractButton b)
			{
				return (null == b) ? null : b.getDisabledSelectedIcon();
			}
			/*
			 * @see net.community.chest.swing.component.button.ButtonIconType#setIcon(javax.swing.AbstractButton, javax.swing.Icon)
			 */
			@Override
			public void setIcon (AbstractButton b, Icon i)
			{
				if (b != null)
					b.setDisabledSelectedIcon(i);
			}
		},
	SELROLLOVER {
			/*
			 * @see net.community.chest.swing.component.button.ButtonIconType#getIcon(javax.swing.AbstractButton)
			 */
			@Override
			public Icon getIcon (AbstractButton b)
			{
				return (null == b) ? null : b.getRolloverSelectedIcon();
			}
			/*
			 * @see net.community.chest.swing.component.button.ButtonIconType#setIcon(javax.swing.AbstractButton, javax.swing.Icon)
			 */
			@Override
			public void setIcon (AbstractButton b, Icon i)
			{
				if (b != null)
					b.setRolloverSelectedIcon(i);
			}
		};

	public abstract Icon getIcon (AbstractButton b);
	public abstract void setIcon (AbstractButton b, Icon i);

	public static final List<ButtonIconType>	VALUES=Collections.unmodifiableList(Arrays.asList(values()));
	public static final ButtonIconType fromString (final String name)
	{
		return CollectionsUtils.fromString(VALUES, name, false);
	}
	// NOTE: ignores null icons
	public static final Map<ButtonIconType,Icon> getIcons (final AbstractButton b)
	{
		if (null == b)
			return null;

		Map<ButtonIconType,Icon>	ret=null;
		for (final ButtonIconType t : VALUES)
		{
			final Icon	i=(null == t) ? null : t.getIcon(b);
			if (null == i)
				continue;

			if (null == ret)
				ret = new EnumMap<ButtonIconType,Icon>(ButtonIconType.class);
			ret.put(t, i);
		}
	
		return ret;
	}
}
