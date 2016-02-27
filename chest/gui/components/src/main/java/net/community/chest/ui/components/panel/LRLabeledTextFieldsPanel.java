/*
 * 
 */
package net.community.chest.ui.components.panel;

import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

import net.community.chest.awt.attributes.Editable;
import net.community.chest.dom.impl.StandaloneDocumentImpl;
import net.community.chest.ui.helpers.panel.PresetGridLayoutPanel;
import net.community.chest.ui.helpers.panel.input.LRFieldWithLabelInput;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * <P>Copyright as per GPLv2</P>
 * Contains a series of label+text-field pairs inside a grid layout
 * @author Lyor G.
 * @since Mar 21, 2011 1:19:00 PM
 */
public class LRLabeledTextFieldsPanel extends PresetGridLayoutPanel implements Editable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 4153284801074177984L;
	public LRLabeledTextFieldsPanel (int vgap, Document doc, boolean autoLayout)
	{
		super(0, 1, 0, vgap, doc, autoLayout);
	}

	public LRLabeledTextFieldsPanel (Document doc, boolean autoLayout)
	{
		this(0, doc, autoLayout);
	}

	public LRLabeledTextFieldsPanel (Document doc)
	{
		this(doc, true);
	}

	public LRLabeledTextFieldsPanel (boolean autoLayout)
	{
		this((Document) null, autoLayout);
	}

	public LRLabeledTextFieldsPanel ()
	{
		this(true);
	}

	public LRLabeledTextFieldsPanel (int vgap, Element elem, boolean autoLayout)
	{
		this(vgap, (null == elem) ? null : new StandaloneDocumentImpl(elem), autoLayout);
	}

	public LRLabeledTextFieldsPanel (Element elem, boolean autoLayout)
	{
		this(0, elem, autoLayout);
	}
	
	public LRLabeledTextFieldsPanel (Element elem)
	{
		this(elem, true);
	}
	/* Returns TRUE if ANY of the fields is editable
	 * @see net.community.chest.awt.attributes.Editable#isEditable()
	 */
	@Override
	public boolean isEditable ()
	{
		final Map<?,? extends Editable>			fm=getFieldsMap(false);
		final Collection<? extends Editable>	fl=((fm == null) || fm.isEmpty()) ? null : fm.values();
		if ((fl == null) || fl.isEmpty())
			return false;

		for (final Editable e : fl)
		{
			if ((e != null) && e.isEditable())
				return true;
		}

		return false;
	}
	/*
	 * @see net.community.chest.awt.attributes.Editable#setEditable(boolean)
	 */
	@Override
	public void setEditable (boolean b)
	{
		final Map<?,? extends Editable>			fm=getFieldsMap(false);
		final Collection<? extends Editable>	fl=((fm == null) || fm.isEmpty()) ? null : fm.values();
		if ((fl == null) || fl.isEmpty())
			return;

		for (final Editable e : fl)
		{
			if (e == null)
				continue;
			e.setEditable(b);
		}
	}

	private Map<String,LRFieldWithLabelInput>	_fieldsMap	/* =null */;
	protected Map<String,LRFieldWithLabelInput> getFieldsMap (boolean createIfNotExist)
	{
		if ((null == _fieldsMap) && createIfNotExist)
			_fieldsMap = new TreeMap<String,LRFieldWithLabelInput>(String.CASE_INSENSITIVE_ORDER);
		return _fieldsMap;
	}

	public Map<String,LRFieldWithLabelInput> getFieldsMap ()
	{
		return getFieldsMap(false);
	}

	public LRFieldWithLabelInput getTextField (final String name)
	{
		if ((null == name) || (name.length() <= 0))
			return null;

		final Map<String,? extends LRFieldWithLabelInput>	fm=getFieldsMap();
		if ((null == fm) || (fm.size() <= 0))
			return null;
		
		return fm.get(name);
	}
	// returns previous - null if none
	public LRFieldWithLabelInput addTextField (final String name, final LRFieldWithLabelInput fld)
	{
		if ((name == null) || (name.length() <= 0) || (fld == null))
			return null;

		final Map<String,LRFieldWithLabelInput>	fm=getFieldsMap(true);
		final LRFieldWithLabelInput				prev=fm.put(name, fld);
		if (prev != null)
			remove(prev);
		add(fld);
		return prev;
	}

	// returns previous - null if none
	public LRFieldWithLabelInput addTextField (final LRFieldWithLabelInput fld)
	{
		return addTextField((fld == null) ? null : fld.getName(), fld);
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

	@Override
	public void layoutSection (String name, Element elem) throws RuntimeException
	{
		final String	tagName=(null == elem) ? null : elem.getTagName();
		if ("field".equalsIgnoreCase(tagName))
			createTextField(name, elem);
		else
			super.layoutSection(name, elem);
	}
}
