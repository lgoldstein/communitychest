/*
 * 
 */
package net.community.apps.eclipse.cp2pom;

import net.community.chest.ui.helpers.text.InputTextField;

/**
 * <P>Copyright 2009 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Jul 30, 2009 9:03:15 AM
 */
public class PomInputTextField extends InputTextField {
	/**
	 * 
	 */
	private static final long serialVersionUID = -2323346473410020188L;

	public PomInputTextField ()
	{
		super(false);	// delay auto-layout till AFTER verifier set
		setInputVerifier(new PomFileOutputVerifier());
		layoutComponent();
	}
}
