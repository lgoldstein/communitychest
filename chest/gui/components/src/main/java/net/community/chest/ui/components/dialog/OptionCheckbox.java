package net.community.chest.ui.components.dialog;

import java.lang.reflect.Method;

import net.community.chest.reflect.AttributeAccessor;
import net.community.chest.reflect.AttributeMethodType;
import net.community.chest.ui.helpers.button.TypedCheckBox;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * Checkbox used to hold a dialog option state
 * @author Lyor G.
 * @since Jan 6, 2009 8:39:55 AM
 */
public class OptionCheckbox extends TypedCheckBox<AttributeAccessor> {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1262283812993896553L;

	public OptionCheckbox (final AttributeAccessor aa)
	{
		super(AttributeAccessor.class, aa);
		setText(AttributeMethodType.getSpacedAttributeName(aa.getName()));
	}

	public Object getOptionValue (Object o) throws Exception
	{
		final AttributeAccessor	a=getAssignedValue();
		final Method			gm=(null == a) ? null : a.getGetter();
		return (gm == null) ? null : gm.invoke(o, AttributeAccessor.EMPTY_OBJECTS_ARRAY);
	}

	public void setOptionValue (Object o) throws Exception
	{
		final AttributeAccessor	a=getAssignedValue();
		final Method			sm=(null == a) ? null : a.getSetter();
		final Boolean			v=Boolean.valueOf(isSelected());
		if (sm == null)
			return;	// debug breakpoint
		sm.invoke(o, v);
	}
}