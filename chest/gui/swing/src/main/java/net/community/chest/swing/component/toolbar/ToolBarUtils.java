/*
 * 
 */
package net.community.chest.swing.component.toolbar;

import java.awt.event.ActionListener;
import java.util.Map;

import javax.swing.AbstractButton;
import javax.swing.JToolBar;

import net.community.chest.swing.component.button.ButtonUtils;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Sep 28, 2008 10:16:47 AM
 */
public final class ToolBarUtils {
	private ToolBarUtils ()
	{
		super();
	}
	/**
	 * @param b The {@link JToolBar} to be explored - may be null
	 * @param errIfDuplicate <code>true</code> throw an exception if same
	 * command already mapped to other button
	 * @return A {@link Map} whose key=the action command, value=the {@link AbstractButton}
	 * mapped to that command. May be null/empty if no initial map supplied
	 * and no commands found
	 * @throws IllegalStateException if null/empty action command found or
	 * duplicate mapping and duplicates not allowed.
	 */
	public static final Map<String,AbstractButton> getButtonsMap (final JToolBar b, final boolean errIfDuplicate) throws IllegalStateException
	{
		return ButtonUtils.updateButtonsMap(null, b, errIfDuplicate);
	}
	/**
	 * @param b The {@link JToolBar} to be explored - may be null
	 * @return A {@link Map} whose key=the action command, value=the {@link AbstractButton}
	 * mapped to that command. May be null/empty if no initial map supplied
	 * and no commands found
	 * @throws IllegalStateException if null/empty action command found or
	 * duplicate mapping.
	 */
	public static final Map<String,AbstractButton> getButtonsMap (final JToolBar b) throws IllegalStateException
	{
		return getButtonsMap(b, true); 
	}

	// returns ONLY set buttons
	public static final Map<String,AbstractButton> setToolBarHandlers (final JToolBar b, final boolean errIfDuplicate, final Map<String,? extends ActionListener> lm)
	{
		if ((null == lm) || (lm.size() <= 0))
			return null;

		final Map<String,? extends AbstractButton>	bm=getButtonsMap(b, errIfDuplicate);
		if ((null == bm) || (bm.size() <= 0))
			return null;

		return ButtonUtils.setButtonActionHandlers(bm, lm);
 	}

	public static final Map<String,AbstractButton> setToolBarHandlers (final JToolBar b, final Map<String,? extends ActionListener> lm)
	{
		return setToolBarHandlers(b, true, lm);
	}
}
