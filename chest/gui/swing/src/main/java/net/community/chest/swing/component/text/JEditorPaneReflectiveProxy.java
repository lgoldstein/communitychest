/*
 * 
 */
package net.community.chest.swing.component.text;

import javax.swing.JEditorPane;

/**
 * <P>Copyright 2009 as per GPLv2</P>
 *
 * @param <P> Type of {@link JEditorPane} being reflected
 * @author Lyor G.
 * @since Jul 29, 2009 1:59:37 PM
 */
public class JEditorPaneReflectiveProxy<P extends JEditorPane> extends
		JTextComponentReflectiveProxy<P> {
	protected JEditorPaneReflectiveProxy (Class<P> objClass, boolean registerAsDefault)
		throws IllegalArgumentException, IllegalStateException
	{
		super(objClass, registerAsDefault);
	}

	public JEditorPaneReflectiveProxy (Class<P> objClass) throws IllegalArgumentException
	{
		this(objClass, false);
	}
	
	public static final JEditorPaneReflectiveProxy<JEditorPane>	EDTPANE=
		new JEditorPaneReflectiveProxy<JEditorPane>(JEditorPane.class, true);
}
