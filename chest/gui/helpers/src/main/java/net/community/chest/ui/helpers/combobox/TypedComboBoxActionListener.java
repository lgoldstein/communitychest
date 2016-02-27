package net.community.chest.ui.helpers.combobox;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;


/**
 * Copyright 2007 as per GPLv2
 * 
 * Implements a "typed" {@link ActionListener} - i.e., one that assumes that
 * for each displayed text in the combo box there is an associated
 * {@link Object} value.
 * 
 * @param <CB> The {@link TypedComboBox} actually being listened to
 * @param <V> The type of value associated with each row of the {@link TypedComboBox} 
 * @author Lyor G.
 * @since Jun 13, 2007 2:40:24 PM
 */
public abstract class TypedComboBoxActionListener<V,CB extends TypedComboBox<V>> implements ActionListener {
	protected TypedComboBoxActionListener ()
	{
		super();
	}
	/**
	 * @param e original even supplied to {@link #actionPerformed(ActionEvent)}
	 * @param cb the underlying combo box
	 * @param text selected item text
	 * @param value selected item associated value
	 */
	public abstract void handleSelectedItem (ActionEvent e, CB cb, String text, V value);
	/*
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed (final ActionEvent e)
	{
		final Object						src=(null == e) ? null : e.getSource();
		@SuppressWarnings("unchecked")
		final CB							cb=(src instanceof TypedComboBox) ? (CB) src : null;
		final Map.Entry<String,? extends V>	ci=(null == cb) ? null : (Map.Entry<String, ? extends V>) cb.getSelectedItem();
		handleSelectedItem(e, cb, (null == ci) ? null : ci.toString(), (null == ci) ? null : ci.getValue());
	}
}
