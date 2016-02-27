/*
 * 
 */
package net.community.chest.awt.border;

import java.awt.Color;
import java.awt.Font;

import javax.swing.border.Border;
import javax.swing.border.TitledBorder;

import net.community.chest.awt.attributes.FontControl;
import net.community.chest.awt.attributes.Foregrounded;
import net.community.chest.awt.attributes.Titled;

/**
 * <P>Copyright as per GPLv2</P>
 *
 * <P>Adds some attributes implementations</P>
 * 
 * @author Lyor G.
 * @since Jul 1, 2009 10:22:55 AM
 */
public class ExtendedTitledBorder extends TitledBorder
			implements Titled, FontControl, Foregrounded {
	/**
	 * 
	 */
	private static final long serialVersionUID = 5885013506733963257L;
	public ExtendedTitledBorder (String titleValue)
	{
		super(titleValue);
	}

	public ExtendedTitledBorder (Border borderValue)
	{
		super(borderValue);
	}

	public ExtendedTitledBorder (Border borderValue, String titleValue)
	{
		super(borderValue, titleValue);
	}

	public ExtendedTitledBorder (Border borderValue, String titleValue, int titleJustificationValue, int titlePositionValue)
	{
		super(borderValue, titleValue, titleJustificationValue, titlePositionValue);
	}

	public ExtendedTitledBorder (Border borderValue, String titleValue, int titleJustificationValue, int titlePositionValue, Font titleFontValue)
	{
		super(borderValue, titleValue, titleJustificationValue, titlePositionValue, titleFontValue);
	}

	public ExtendedTitledBorder (Border borderValue, String titleValue,
								 int titleJustificationValue, int titlePositionValue,
								 Font titleFontValue, Color titleColorValue)
	{
		super(borderValue, titleValue, titleJustificationValue, titlePositionValue, titleFontValue, titleColorValue);
	}
	/*
	 * @see net.community.chest.awt.attributes.FontControl#getFont()
	 */
	@Override
	public Font getFont ()
	{
		return getTitleFont();
	}
	/*
	 * @see net.community.chest.awt.attributes.FontControl#setFont(java.awt.Font)
	 */
	@Override
	public void setFont (Font f)
	{
		setTitleFont(f);
	}
	/*
	 * @see net.community.chest.awt.attributes.Foregrounded#getForeground()
	 */
	@Override
	public Color getForeground ()
	{
		return getTitleColor();
	}
	/*
	 * @see net.community.chest.awt.attributes.Foregrounded#setForeground(java.awt.Color)
	 */
	@Override
	public void setForeground (Color c)
	{
		setTitleColor(c);
	}
}
