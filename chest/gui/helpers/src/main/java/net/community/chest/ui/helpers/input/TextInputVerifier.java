/*
 * 
 */
package net.community.chest.ui.helpers.input;

import javax.swing.InputVerifier;
import javax.swing.JComponent;

import net.community.chest.awt.attributes.AttrUtils;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Jan 12, 2009 2:17:06 PM
 */
public class TextInputVerifier extends InputVerifier {
	public TextInputVerifier ()
	{
		super();
	}

	public boolean verifyText (final String text)
	{
		return (text != null) && (text.length() > 0);
	}
 	/*
	 * @see javax.swing.InputVerifier#verify(javax.swing.JComponent)
	 */
	@Override
	public boolean verify (JComponent input)
	{
		// if cannot retrieve the text then cannot parse it
		if (!AttrUtils.isTextableComponent(input))
			return false;

		try
		{
			return verifyText(AttrUtils.getComponentText(input));
		}
		catch(RuntimeException e)
		{
			return false;
		}
	}

	public static final TextInputVerifier	TEXT=new TextInputVerifier();
}
