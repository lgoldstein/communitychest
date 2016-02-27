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
 * <P>Encapsulates {@link JOptionPane} confirmation options into {@link Enum}-s</P>
 * 
 * @author Lyor G.
 * @since Jul 31, 2008 11:50:16 AM
 */
public enum OptionPaneConfirmType {
	DEFAULT(JOptionPane.DEFAULT_OPTION),
	YESNO(JOptionPane.YES_NO_OPTION),
	YESNOCANCEL(JOptionPane.YES_NO_CANCEL_OPTION),
	OKCANCEL(JOptionPane.OK_CANCEL_OPTION);

	private final int	_cnfType;
	public final int getConfirmationType ()
	{
		return _cnfType;
	}

	OptionPaneConfirmType (final int cnfType)
	{
		_cnfType = cnfType;
	}

	public static final List<OptionPaneConfirmType>	VALUES=Collections.unmodifiableList(Arrays.asList(values()));
	public static final OptionPaneConfirmType fromString (final String s)
	{
		return CollectionsUtils.fromString(VALUES, s, false);
	}

	public static final OptionPaneConfirmType fromConfirmationTypeValue (final int t)
	{
		for (final OptionPaneConfirmType v : VALUES)
		{
			if ((v != null) && (v.getConfirmationType() == t))
				return v;
		}

		return null;
	}
}
