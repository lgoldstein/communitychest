/*
 * 
 */
package net.community.apps.common;

import javax.swing.JToolBar;

import org.w3c.dom.Element;

/**
 * <P>Copyright 2009 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Aug 5, 2009 11:31:34 AM
 */
public interface ToolbaredComponent {

	public static final String	MAIN_TOOLBAR_SECTION_NAME="main-toolbar";
	Element getMainToolBarElement ();
	JToolBar getMainToolBar ();
}
