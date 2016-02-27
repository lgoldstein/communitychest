/*
 * 
 */
package net.community.chest.ui.helpers.dialog;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.GraphicsConfiguration;
import java.awt.GridLayout;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.JPanel;

import net.community.chest.ui.helpers.panel.input.LRFieldWithLabelInput;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * <P>Copyright 2009 as per GPLv2</P>
 *
 * <P>Assumes each added section is an {@link LRFieldWithLabelInput}</P>
 * 
 * @param <V> Type of content 
 * @author Lyor G.
 * @since Oct 20, 2009 2:59:02 PM
 */
public class LRInputTextFieldsDialog<V> extends SettableDialog<V> {
	/**
	 * 
	 */
	private static final long serialVersionUID = 5364152628856679663L;
	public LRInputTextFieldsDialog (boolean autoInit)
	{
		super(autoInit);
	}

	public LRInputTextFieldsDialog (Frame owner, boolean modal, boolean autoInit)
	{
		super(owner, modal, autoInit);
	}

	public LRInputTextFieldsDialog (Dialog owner, boolean modal, boolean autoInit)
	{
		super(owner, modal, autoInit);
	}

	public LRInputTextFieldsDialog (Frame owner, Element elem, boolean autoInit)
	{
		super(owner, elem, autoInit);
	}

	public LRInputTextFieldsDialog (Element elem, boolean autoInit)
	{
		super(elem, autoInit);
	}

	public LRInputTextFieldsDialog (Dialog owner, Element elem, boolean autoInit)
	{
		super(owner, elem, autoInit);
	}

	public LRInputTextFieldsDialog (Document doc, boolean autoInit)
	{
		super(doc, autoInit);
	}

	public LRInputTextFieldsDialog (Frame owner, Document doc, boolean autoInit)
	{
		super(owner, doc, autoInit);
	}

	public LRInputTextFieldsDialog (Dialog owner, Document doc, boolean autoInit)
	{
		super(owner, doc, autoInit);
	}

	public LRInputTextFieldsDialog (Frame owner, String title, boolean modal, boolean autoInit)
	{
		super(owner, title, modal, autoInit);
	}

	public LRInputTextFieldsDialog (Dialog owner, String title, boolean modal, boolean autoInit)
	{
		super(owner, title, modal, autoInit);
	}

	public LRInputTextFieldsDialog (Frame owner, String title, boolean modal, GraphicsConfiguration gc, boolean autoInit)
	{
		super(owner, title, modal, gc, autoInit);
	}

	public LRInputTextFieldsDialog (Dialog owner, String title, boolean modal, GraphicsConfiguration gc, boolean autoInit)
	{
		super(owner, title, modal, gc, autoInit);
	}

	public LRInputTextFieldsDialog ()
	{
		this(true);
	}

	public LRInputTextFieldsDialog (Frame owner)
	{
		super(owner);
	}

	public LRInputTextFieldsDialog (Dialog owner)
	{
		super(owner);
	}

	public LRInputTextFieldsDialog (Frame owner, boolean modal)
	{
		super(owner, modal);
	}

	public LRInputTextFieldsDialog (Frame owner, String title)
	{
		super(owner, title);
	}

	public LRInputTextFieldsDialog (Dialog owner, boolean modal)
	{
		super(owner, modal);
	}

	public LRInputTextFieldsDialog (Dialog owner, String title)
	{
		super(owner, title);
	}

	public LRInputTextFieldsDialog (Frame owner, Element elem)
	{
		this(owner, elem, true);
	}

	public LRInputTextFieldsDialog (Element elem)
	{
		this(elem, true);
	}

	public LRInputTextFieldsDialog (Dialog owner, Element elem)
	{
		this(owner, elem, true);
	}

	public LRInputTextFieldsDialog (Document doc)
	{
		this(doc, true);
	}

	public LRInputTextFieldsDialog (Frame owner, Document doc)
	{
		this(owner, doc, true);
	}

	public LRInputTextFieldsDialog (Dialog owner, Document doc)
	{
		this(owner, doc, true);
	}

	public LRInputTextFieldsDialog (Frame owner, String title, boolean modal)
	{
		super(owner, title, modal);
	}

	public LRInputTextFieldsDialog (Dialog owner, String title, boolean modal)
	{
		super(owner, title, modal);
	}

	public LRInputTextFieldsDialog (Frame owner, String title, boolean modal, GraphicsConfiguration gc)
	{
		super(owner, title, modal, gc);
	}

	public LRInputTextFieldsDialog (Dialog owner, String title, boolean modal, GraphicsConfiguration gc)
	{
		super(owner, title, modal, gc);
	}

	private Container	_fieldsPanel;
	protected Container getFieldsPanel (boolean createIfNotExist)
	{
		if ((null == _fieldsPanel) && createIfNotExist)
			_fieldsPanel = new JPanel(new GridLayout(0, 1, 0, 5));
		return _fieldsPanel;
	}

	protected Container addField (LRFieldWithLabelInput fld)
	{
		if (null == fld)
			return getFieldsPanel(false);

		final Container	p=getFieldsPanel(true);
		p.add(fld);
		return p;
	}

	private Map<String,LRFieldWithLabelInput>	_fieldsMap	/* =null */;
	protected Map<String,LRFieldWithLabelInput> getFieldsMap (boolean createIfNotExist)
	{
		if ((null == _fieldsMap) && createIfNotExist)
			_fieldsMap = new TreeMap<String,LRFieldWithLabelInput>(String.CASE_INSENSITIVE_ORDER);
		return _fieldsMap;
	}

	protected LRFieldWithLabelInput getTextField (final String name)
	{
		if ((null == name) || (name.length() <= 0))
			return null;

		final Map<String,? extends LRFieldWithLabelInput>	fm=getFieldsMap(false);
		if ((null == fm) || (fm.size() <= 0))
			return null;
		
		return fm.get(name);
	}
	// returns previous - null if none
	protected LRFieldWithLabelInput addTextField (final String name, final LRFieldWithLabelInput fld)
	{
		final Map<String,LRFieldWithLabelInput>	fm=getFieldsMap(true);
		return fm.put(name, fld);
	}

	protected LRFieldWithLabelInput createTextField (String name, Element elem)
	{
		final Map<String,LRFieldWithLabelInput>	fm=getFieldsMap(true);
		if (fm.containsKey(name))
			throw new IllegalStateException("createTextField(" + name + ") re-specified");

		final LRFieldWithLabelInput	fld=new LRFieldWithLabelInput(elem),
									prev=addTextField(name, fld);
		if (prev != null)
			throw new IllegalStateException("createTextField(" + name + ") replaced some previous field");

		return fld;
	}
	/*
	 * @see net.community.chest.ui.helpers.dialog.HelperDialog#layoutSection(java.lang.String, org.w3c.dom.Element)
	 */
	@Override
	public void layoutSection (String name, Element elem) throws RuntimeException
	{
		final String	tagName=(null == elem) ? null : elem.getTagName();
		if ("field".equalsIgnoreCase(tagName))
		{
			final LRFieldWithLabelInput	fld=createTextField(name, elem);
			addField(fld);
		}
		else
			super.layoutSection(name, elem);
	}

	protected void layoutFieldsPanel (Container p)
	{
		if (p != null)
			add(p, BorderLayout.CENTER);
	}
	/*
	 * @see net.community.chest.ui.helpers.dialog.FormDialog#layoutComponent()
	 */
	@Override
	public void layoutComponent () throws RuntimeException
	{
		super.layoutComponent();

		layoutFieldsPanel(getFieldsPanel(false));
	}
}
