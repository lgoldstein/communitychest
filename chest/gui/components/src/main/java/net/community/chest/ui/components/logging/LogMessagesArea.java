package net.community.chest.ui.components.logging;

import java.awt.Font;

import javax.swing.BorderFactory;


import org.w3c.dom.Element;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * <P>Provides the log messages scroll area</P>
 * @author Lyor G.
 * @since Jul 31, 2008 1:14:40 PM
 */
public class LogMessagesArea extends LoggingTextPane {
	/**
	 * 
	 */
	private static final long serialVersionUID = 7627485429039632193L;

	public LogMessagesArea (Font logsFont, Element elem)
	{
		setEditable(false);
		setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
		setFont(logsFont);
		layoutComponent(elem);
	}
}