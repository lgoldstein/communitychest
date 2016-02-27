package net.community.chest.ui.helpers.table;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import javax.swing.table.TableColumnModel;

import net.community.chest.CoVariantReturn;
import net.community.chest.lang.TypedValuesContainer;
import net.community.chest.swing.component.table.TableUtil;

/**
 * <P>Copyright 2007 as per GPLv2</P>
 *
 * <P>Represents a table whose rows are objects of the same type</P>
 * 
 * @param <V> The type of value associated with each row
 * @author Lyor G.
 * @since Aug 6, 2007 8:15:33 AM
 */
public class TypedTable<V> extends HelperTable implements TypedValuesContainer<V> {
	/**
	 * 
	 */
	private static final long serialVersionUID = 4617415410289226756L;

	public TypedTable (TypedTableModel<V> tbModel, TableColumnModel tcModel)
	{
		super(tbModel, tcModel);
	}

	public TypedTable (TypedTableModel<V> model)
	{
		this(model, null);
	}

	@SuppressWarnings("unchecked")
	@CoVariantReturn
	public TypedTableModel<V> getTypedModel ()
	{
		return (TypedTableModel<V>) getModel();
	}
	/*
	 * @see net.community.chest.lang.TypedValuesContainer#getValuesClass()
	 */
	@Override
	public Class<V> getValuesClass ()
	{
		final TypedTableModel<V>	m=getTypedModel();
		return (null == m) ? null : m.getValuesClass();
	}
	// returns number of added values (ignores null(s))
	public int addValues (final Collection<? extends V> values)
	{
		if ((null == values) || (values.size() <= 0))
			return 0;

		final TypedTableModel<V>	m=getTypedModel();
		if (null == m)
			return 0;

		int	numAdded=0;
		for (final V val : values)
		{
			if (null == val)
				continue;
			m.add(val);
		}

		return numAdded;
	}

	public int addValues (V ... values)
	{
		if ((null == values) || (values.length <= 0))
			return 0;

		return addValues(Arrays.asList(values));
	}

	public static final <V> List<V> getSelectedValues (final TypedTable<V> tbl)
	{
		return TableUtil.getSelectedValues(tbl, (null == tbl) ? null : tbl.getTypedModel());
	}

	public List<V> getSelectedValues ()
	{
		return getSelectedValues(this);
	}
}
