package net.community.chest.ui.helpers.combobox;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.w3c.dom.Element;


/**
 * Copyright 2007 as per GPLv2
 * 
 * Enumeration objects displayed via combo box
 * 
 * @param <E> The {@link Enum} used to identify the entry
 * @author Lyor G.
 * @since Jul 5, 2007 9:22:52 AM
 */
public class EnumComboBox<E extends Enum<E>> extends TypedComboBox<E> {
	/**
	 * 
	 */
	private static final long serialVersionUID = -5964857201193538114L;
	private List<E>	_vals	/* =null */;
	public synchronized List<E> getEnumValues ()
	{
		if (null == _vals)
		{
			final TypedComboBoxModel<E>	model=getModel();
			if (model instanceof EnumComboBoxModel<?>)
				_vals = ((EnumComboBoxModel<E>) model).getEnumValues();
			else
				_vals = Collections.unmodifiableList(Arrays.asList(getValuesClass().getEnumConstants()));
		}

		return _vals;
	}

	public void setEnumValues (List<E> vals)
	{
		_vals = vals;
	}
	/**
	 * Adds all available enumeration values. <B>Caveat emptor:</B> if called
	 * more than once then the same values are re-added (<U>duplicates</U>). 
	 */
	public void populate ()
	{
		addValues(getEnumValues());
	}

	public EnumComboBox (TypedComboBoxModel<E> aModel, boolean autoPopulate, Element elem, boolean autoLayout)
	{
		super(aModel, elem, autoLayout);

		if (autoPopulate)
			populate();
	}
	/**
	 * Creates an optionally populated combo box
	 * @param aModel underlying model to use
	 * @param autoPopulate if TRUE the {@link #populate()} method is called.
	 * <B>Caveat emptor:</B> if underlying model already populated (even
	 * partially) then <U>duplicate</U> entries will occur
	 */
	public EnumComboBox (TypedComboBoxModel<E> aModel, boolean autoPopulate)
	{
		this(aModel, autoPopulate, null, true);
	}

	public EnumComboBox (Class<E> valsClass, boolean autoPopulate, Element elem, boolean autoLayout)
	{
		super(valsClass, elem, autoLayout);

		if (autoPopulate)
			populate();
	}
	/**
	 * Creates an optionally populated combo box
	 * @param valsClass enum {@link Class} instance
	 * @param autoPopulate if TRUE the {@link #populate()} method is called
	 */
	public EnumComboBox (Class<E> valsClass, boolean autoPopulate)
	{
		this(valsClass, autoPopulate, null, true);
	}
	/**
	 * Creates an un-populated combo box
	 * @param valsClass enum {@link Class} instance
	 * @see #EnumComboBox(Class, boolean)
	 */
	public EnumComboBox (Class<E> valsClass)
	{
		this(valsClass, false);
	}
	/**
	 * Creates an un-populated combo box
	 * @param aModel underlying model to use
	 * @see #EnumComboBox(TypedComboBoxModel, boolean)
	 */
	public EnumComboBox (TypedComboBoxModel<E> aModel)
	{
		this(aModel, false);
	}
}
