/**
 * 
 */
package net.community.chest.swing.options;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.swing.JOptionPane;

import net.community.chest.util.collection.CollectionsUtils;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * <P>Encapsulates the {@link JOptionPane} message types into {@link Enum}-s</P>
 * @author Lyor G.
 * @since Jul 31, 2008 12:12:07 PM
 */
public enum OptionPaneMessageType {
	PLAIN(JOptionPane.PLAIN_MESSAGE),
	INFORMATION(JOptionPane.INFORMATION_MESSAGE),
	WARNING(JOptionPane.WARNING_MESSAGE),
	QUESTION(JOptionPane.QUESTION_MESSAGE),
	ERROR(JOptionPane.ERROR_MESSAGE);

	private final int	_typeValue;
	public final int getTypeValue ()
	{
		return _typeValue;
	}

	OptionPaneMessageType (final int v)
	{
		_typeValue = v;
	}

	public static final List<OptionPaneMessageType>	VALUES=Collections.unmodifiableList(Arrays.asList(values()))	/* =null */;
	public static final OptionPaneMessageType fromString (final String s)
	{
		return CollectionsUtils.fromString(VALUES, s, false);
	}

	public static final OptionPaneMessageType fromTypeValue (final int t)
	{
		for (final OptionPaneMessageType v : VALUES)
		{
			if ((v != null) && (v.getTypeValue() == t))
				return v;
		}

		return null;
	}
}
