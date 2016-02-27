/*
 * 
 */
package net.community.apps.tools.adm.config;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyListener;
import java.util.Collection;
import java.util.Map;

import javax.swing.AbstractButton;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import net.community.chest.awt.window.EscapeKeyWindowCloser;
import net.community.chest.ui.helpers.dialog.ButtonsPanel;
import net.community.chest.ui.helpers.dialog.LRInputTextFieldsDialog;
import net.community.chest.ui.helpers.panel.input.LRFieldWithLabelInput;
import net.community.chest.ui.helpers.text.InputTextField;

import org.w3c.dom.Element;

/**
 * <P>Copyright 2009 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Oct 20, 2009 2:55:00 PM
 */
public class ValueEditDialog extends LRInputTextFieldsDialog<ValueTableEntry>
			implements ActionListener, ChangeListener {
	private ValueTableEntry	_vte;
	public final ValueTableEntry getContent ()
	{
		return _vte;
	}

	private static boolean updateOkButtonState (final AbstractButton 		btn,
										 		final LRFieldWithLabelInput	fldName,
										 		final LRFieldWithLabelInput	fldValue)
	{
		if (null == btn)
			return false;

		final String[]	texts={
				(null == fldName) ? null : fldName.getText(),
				(null == fldValue) ? null : fldValue.getText()
			};
		for (final String t : texts)
		{
			if ((null == t) || (t.length() <= 0))
			{
				btn.setEnabled(false);
				return false;
			}
		}

		btn.setEnabled(true);	// this point is reached if all texts non-null/empty
		return true;
	}

	private boolean updateOkButtonState (final AbstractButton btn)
	{
		return updateOkButtonState(btn, getTextField("name"), getTextField("value"));
	}

	private AbstractButton	_okBtn;
	/*
	 * @see net.community.chest.ui.helpers.dialog.SettableDialog#setContent(java.lang.Object)
	 */
	@Override
	public void setContent (final ValueTableEntry vte)
	{
		final LRFieldWithLabelInput	fldName=getTextField("name"),
									fldValue=getTextField("value");
		if (fldName != null)
		{
			final String	name=(null == vte) ? null : vte.getKey();
			fldName.setText(name);

			// for edit mode do not allow renaming the parameter
			if ((name != null) && (name.length() > 0))
			{
				final InputTextField	tf=fldName.getInputTextField();
				tf.setEditable(false);
			}
		}
		
		if (fldValue != null)
		{
			final String	value=(null == vte) ? null : vte.getValue();
			fldValue.setText(value);
		}

		if (vte != null)
			vte.setOriginalValue(vte.getValue());

		updateOkButtonState(_okBtn, fldName, fldValue);
		_vte = vte;
	}

	public ValueEditDialog (Frame owner, ValueTableEntry vte, Element elem, boolean autoInit)
	{
		super(owner, elem, autoInit);
		setContent(vte);
	}
	/*
	 * @see net.community.chest.ui.helpers.dialog.LRInputTextFieldsDialog#createTextField(java.lang.String, org.w3c.dom.Element)
	 */
	@Override
	protected LRFieldWithLabelInput createTextField (String name, Element elem)
	{
		final LRFieldWithLabelInput	fld=super.createTextField(name, elem);
		fld.addDataChangeListener(this);
		return fld;
	}

	public static final String	OK_CMD="ok";
	/*
	 * @see net.community.chest.ui.helpers.dialog.LRInputTextFieldsDialog#layoutComponent()
	 */
	@Override
	public void layoutComponent () throws RuntimeException
	{
		super.layoutComponent();

		{
			final ButtonsPanel	bp=getButtonsPanel();
			if (null == (_okBtn=(null == bp) ? null : bp.getButton(OK_CMD)))
				throw new IllegalStateException("Missing OK button/panel");
			_okBtn.addActionListener(this);
			updateOkButtonState(_okBtn);
		}

		{
			final Map<String,LRFieldWithLabelInput>	fm=getFieldsMap(false);
			final Collection<? extends LRFieldWithLabelInput>	il=
				((null == fm) || (fm.size() <= 0)) ? null : fm.values();

			KeyListener	kl=null;
			if ((il != null) && (il.size() > 0))
			{
				for (final LRFieldWithLabelInput f : il)
				{
					final InputTextField	i=(null == f) ? null : f.getInputTextField();
					if (null == i)
						continue;
					
					if (null == kl)
						kl = new EscapeKeyWindowCloser(this);
					i.addKeyListener(kl);
				}
				
				if (kl != null)
					addKeyListener(kl);
			}
		}
	}

	private boolean	_okExit	/* =false */;
	public boolean isOkExit ()
	{
		return _okExit;
	}
	/*
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed (ActionEvent e)
	{
		final String	cmd=(null == e) ? null : e.getActionCommand();
		if (OK_CMD.equalsIgnoreCase(cmd))
		{
			final ValueTableEntry vte=getContent();
			if (null == vte)
				return;

			final LRFieldWithLabelInput	fldName=getTextField("name"),
										fldValue=getTextField("value");
			if ((null == fldName) || (null == fldValue))
				return;

			final InputTextField	iName=fldName.getInputTextField();
			if (iName.isEditable())
				vte.setContent(fldName.getText(), fldValue.getText());
			else
				vte.setValue(fldValue.getText());

			_okExit = true;
			dispose();
		}
	}
	/*
	 * @see javax.swing.event.ChangeListener#stateChanged(javax.swing.event.ChangeEvent)
	 */
	@Override
	public void stateChanged (ChangeEvent e)
	{
		final Object	src=(null == e) ? null : e.getSource();
		if ((_okBtn != null) && (src instanceof InputTextField))
			updateOkButtonState(_okBtn);
	}
}
