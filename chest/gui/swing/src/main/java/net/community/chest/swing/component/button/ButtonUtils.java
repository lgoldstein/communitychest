/*
 * 
 */
package net.community.chest.swing.component.button;

import java.awt.Component;
import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.AbstractButton;
import javax.swing.JToolBar;

/**
 * <P>Copyright GPLv2</P>
 *
 * @author Lyor G.
 * @since May 12, 2009 2:03:04 PM
 */
public final class ButtonUtils {
	private ButtonUtils ()
	{
		// no instance
	}
	/**
	 * @param org Original {@link Map} if <code>null</code> one will be
	 * allocated if necessary
	 * @param b The {@link JToolBar} to be explored - may be null
	 * @param errIfDuplicate <code>true</code> throw an exception if same
	 * command already mapped to other button
	 * @return A {@link Map} whose key=the action command, value=the {@link AbstractButton}
	 * mapped to that command. May be null/empty if no initial map supplied
	 * and no commands found
	 * @throws IllegalStateException if null/empty action command found or
	 * duplicate mapping and duplicates not allowed.
	 */
	public static final Map<String,AbstractButton> updateButtonsMap (
			final Map<String,AbstractButton> 	org,
			final JToolBar						b,
			final boolean						errIfDuplicate)
		throws IllegalStateException
	{
		final Component[]	ca=(null == b) ? null : b.getComponents();
		if ((null == ca) || (ca.length <= 0))
			return org;
	
		Map<String,AbstractButton>	ret=org;
		for (final Component c : ca)
		{
			if (!(c instanceof AbstractButton))
				continue;
	
			final AbstractButton	btn=(AbstractButton) c;
			final String			cmd=btn.getActionCommand();
			if ((null == cmd) || (cmd.length() <= 0))
				throw new IllegalStateException("updateButtonsMap(" + btn + ") no action command");
	
			if (null == ret)
				ret = new TreeMap<String,AbstractButton>(String.CASE_INSENSITIVE_ORDER);
	
			final AbstractButton	prev=ret.put(cmd, btn);
			if ((prev != null) && errIfDuplicate && (prev != btn))
				throw new IllegalStateException("updateButtonsMap(" + btn + ") duplicate command: " + cmd);
		}
	
		return ret;
	}

	public static final Map<String,AbstractButton> setButtonActionHandlers (
			final Map<String,? extends AbstractButton> 								bm,
			final Collection<? extends Map.Entry<String,? extends ActionListener>>	ll)
	{
		if ((null == ll) || (ll.size() <= 0))
			return null;
	
		Map<String,AbstractButton>	ret=null;
		for (final Map.Entry<String,? extends ActionListener> le : ll)
		{
			final String			cmd=(null == le) ? null : le.getKey();
			final ActionListener	l=(null == le) ? null : le.getValue();
			if ((null == cmd) || (cmd.length() <= 0) || (null == l))
				continue;	// should not happen
	
			final AbstractButton	btn=bm.get(cmd);
			if (null == btn)
				continue;
	
			btn.addActionListener(l);
	
			if (null == ret)
				ret = new TreeMap<String,AbstractButton>(String.CASE_INSENSITIVE_ORDER);
			ret.put(cmd, btn);
		}
	
		return ret;
	}

	public static final Map<String,AbstractButton> setButtonActionHandlers (
			final Map<String,? extends AbstractButton> bm,
			final Map<String,? extends ActionListener> lm)
	{
		if ((null == bm) || (bm.size() <= 0))
			return null;
	
		final Collection<? extends Map.Entry<String,? extends ActionListener>>	ll=
			((null == lm) || (lm.size() <= 0)) ? null : lm.entrySet();
		return setButtonActionHandlers(bm, ll);
	}
	/**
	 * @param <B> Type of {@link AbstractButton} being managed
	 * @param bm A {@link Map} where key=action command, value=associated
	 * {@link AbstractButton} for the command
	 * @param sl A {@link Collection} of pairs as {@link java.util.Map.Entry}-ies
	 * where key=action command, value=TRUE/FALSE parameter to
	 * {@link AbstractButton#setEnabled(boolean)} method
	 * @return A {@link Collection} of all buttons whose {@link AbstractButton#setEnabled(boolean)}
	 * method has been successfully invoked - may be null/empty if no methods
	 * invoked
	 */
	public static final <B extends AbstractButton> Collection<B> updateButtonsStates (
			final Map<String,? extends B>							bm,
			final Collection<? extends Map.Entry<String,Boolean>>	sl)
	{
		if ((null == sl) || (sl.size() <= 0)
		 || (null == bm) || (bm.size() <= 0))
			return null;

		Collection<B>	ret=null;
		for (final Map.Entry<String,Boolean> be : sl)
		{
			final String	cmd=(null == be) ? null : be.getKey();
			final Boolean	val=(null == be) ? null : be.getValue();
			final B			btn=
				((null == cmd) || (cmd.length() <= 0) || (null == val)) ? null : bm.get(cmd);
			if (null == btn)
				continue;

			btn.setEnabled(val.booleanValue());

			if (null == ret)
				ret = new LinkedList<B>();
			ret.add(btn);
		}

		return ret;
	}
	/**
	 * @param <B> Type of {@link AbstractButton} being managed
	 * @param bm A {@link Map} where key=action command, value=associated
	 * {@link AbstractButton} for the command
	 * @param sm A {@link Map} where key=action command, value=TRUE/FALSE
	 * parameter to {@link AbstractButton#setEnabled(boolean)} method
	 * @return A {@link Collection} of all buttons whose {@link AbstractButton#setEnabled(boolean)}
	 * method has been successfully invoked - may be null/empty if no methods
	 * invoked
	 */
	public static final <B extends AbstractButton> Collection<B> updateButtonsStates (
			final Map<String,? extends B> bm, final Map<String,Boolean> sm)
	{
		return updateButtonsStates(bm, ((null == sm) || (sm.size() <= 0)) ? null : sm.entrySet());
	}
}
