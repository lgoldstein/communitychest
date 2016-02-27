/*
 * 
 */
package net.community.chest.ui.helpers.input;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import net.community.chest.awt.TypedComponentAssignment;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * <P>Helper {@link KeyAdapter} that traps the {@link KeyAdapter#keyReleased(java.awt.event.KeyEvent)}
 * and invokes the {@link InputFieldValidator#signalDataChanged(boolean)} method</P>
 * 
 * @param <F> The type of {@link InputFieldValidator} being used 
 * @author Lyor G.
 * @since Jan 12, 2009 12:30:36 PM
 */
public class InputFieldValidatorKeyListener<F extends InputFieldValidator>
	extends KeyAdapter implements TypedComponentAssignment<F> {

	private F	_validator	/* =null */;
	/*
	 * @see net.community.chest.ui.helpers.TypedComponentAssignment#getAssignedValue()
	 */
	@Override
	public F getAssignedValue ()
	{
		return _validator;
	}

	public F getValidator ()
	{
		return getAssignedValue();
	}
	/*
	 * @see net.community.chest.ui.helpers.TypedComponentAssignment#setAssignedValue(java.lang.Object)
	 */
	@Override
	public void setAssignedValue (F value)
	{
		if (_validator != value)
			_validator = value;
	}

	public void setValidator (F v)
	{
		setAssignedValue(v);
	}

	public InputFieldValidatorKeyListener (F v)
	{
		_validator = v;
	}

	public InputFieldValidatorKeyListener ()
	{
		this(null);
	}
	/*
	 * @see java.awt.event.KeyAdapter#keyReleased(java.awt.event.KeyEvent)
	 */
	@Override
	public void keyReleased (KeyEvent e)
	{
		final Object	src=(null == e) ? null : e.getSource();
		final F			v=getValidator();
		if ((src == v) && (v != null))
			v.signalDataChanged(true);
	}
}
