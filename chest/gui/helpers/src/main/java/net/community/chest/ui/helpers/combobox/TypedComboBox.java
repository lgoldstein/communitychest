package net.community.chest.ui.helpers.combobox;

import java.util.Arrays;
import java.util.Collection;
import java.util.Locale;
import java.util.Map;

import javax.swing.JComboBox;

import net.community.chest.CoVariantReturn;
import net.community.chest.awt.LocalizedComponent;
import net.community.chest.dom.DOMUtils;
import net.community.chest.dom.proxy.XmlProxyConvertible;
import net.community.chest.dom.transform.XmlConvertible;
import net.community.chest.lang.ExceptionUtil;
import net.community.chest.lang.TypedValuesContainer;
import net.community.chest.swing.models.SingleSelectionModeler;
import net.community.chest.swing.models.TypedDisplayModeler;
import net.community.chest.ui.helpers.XmlElementComponentInitializer;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Copyright 2007 as per GPLv2
 * 
 * Provides a "typed" combo box view where each element is actually a
 * {@link java.util.Map.Entry} whose key is the text to be displayed, and its value is
 * the associated value
 * 
 * @param <V> The type of value associated with each row
 * @author Lyor G.
 * @since Jul 5, 2007 8:52:51 AM
 */
public class TypedComboBox<V> extends JComboBox
			implements 	TypedValuesContainer<V>,
						TypedDisplayModeler<V>,
						SingleSelectionModeler<V>,
						XmlConvertible<TypedComboBox<V>>,
						XmlElementComponentInitializer,
						LocalizedComponent {
	/**
	 * 
	 */
	private static final long serialVersionUID = 6091727690003091015L;
	/*
	 * @see javax.swing.JComboBox#getModel()
	 */
	@SuppressWarnings("unchecked")
	@Override
	@CoVariantReturn
	public TypedComboBoxModel<V> getModel ()
	{
		return (TypedComboBoxModel<V>) super.getModel();
	}
	/*
	 * @see net.community.chest.TypedValuesContainer#getValuesClass()
	 */
	@Override
	public final /* no cheating */ Class<V> getValuesClass ()
	{
		return getModel().getValuesClass();
	}
	/*
	 * @see net.community.chest.swing.models.TypedDisplayModeler#addItem(java.lang.String, java.lang.Object)
	 */
	@Override
	public Map.Entry<String,V> addItem (final String name, final V value) throws IllegalArgumentException
	{
		return getModel().addItem(name, value);
	}
	/*
	 * @see net.community.chest.swing.models.TypedDisplayModeler#getValueDisplayText(java.lang.Object)
	 */
	@Override
	public String getValueDisplayText (final V value)
	{
		return getModel().getValueDisplayText(value); 
	}
	/*
	 * @see net.community.chest.swing.models.TypedDisplayModeler#addValue(java.lang.Object)
	 */
	@Override
	public Map.Entry<String,V> addValue (final V value)
	{
		return addItem(getValueDisplayText(value), value);
	}
	/*
	 * @see net.community.chest.swing.models.TypedDisplayModeler#addValues(java.util.Collection)
	 */
	@Override
	public void addValues (final Collection<? extends V> vals)
	{
		if ((vals != null) && (vals.size() > 0))
		{
			for (final V value : vals)
				addValue(value);
		}
	}
	/*
	 * @see net.community.chest.swing.models.TypedDisplayModeler#addValues(V[])
	 */
	@Override
	public void addValues (final V ... vals)
	{
		if ((vals != null) && (vals.length > 0))
			addValues(Arrays.asList(vals));
	}
	/*
	 * @see net.community.chest.swing.models.TypedDisplayModeler#addItem(java.util.Map.Entry)
	 */
	@Override
	public void addItem (Map.Entry<String, ? extends V> item) throws IllegalArgumentException
	{
		getModel().addItem(item);
	}
	/*
	 * @see net.community.chest.swing.models.TypedDisplayModeler#addItems(java.util.Collection)
	 */
	@Override
	public void addItems (final Collection<? extends Map.Entry<String,? extends V>> items)
	{
		getModel().addItems(items);
	}
	/*
	 * @see net.community.chest.swing.models.TypedDisplayModeler#addItems(java.util.Map.Entry<java.lang.String,V>[])
	 */
	@Override
	public void addItems (final Map.Entry<String,V> ... items)
	{
		getModel().addItems(items);
	}
	/*
	 * @see net.community.chest.swing.models.TypedDisplayModeler#addItems(java.util.Map)
	 */
	@Override
	public void addItems (final Map<String,? extends V> items)
	{
		getModel().addItems(items);
	}
	/**
	 * @return selected "pair" (name + value)
	 */
	@Override
	@SuppressWarnings("unchecked")
	public Map.Entry<String,V> getSelectedItem ()
	{
		return (Map.Entry<String,V>) super.getSelectedItem();
	}
	/**
	 * @return selected value
	 */
	@Override
	public V getSelectedValue ()
	{
		final Map.Entry<String,V>	item=getSelectedItem();
		return (null == item) /* may happen if no selection made */ ? null : item.getValue();
	}
	/**
	 * @return text of the selected item
	 */
	@Override
	public String getSelectedText ()
	{
		final Map.Entry<String, V>	item=getSelectedItem();
		return (null == item) /* can happen if no selection */ ? null : item.getKey();
	}
	/**
	 * Remove all elements in the underlying model
	 */
	public void removeAllElements ()
	{
		getModel().removeAllElements();
	}
	/**
	 * @return number of current elements in the underlying model
	 */
	public int getNumElements ()
	{
		return getModel().getSize();
	}
	/*
	 * @see net.community.chest.swing.models.SingleSelectionModeler#setSelectedValue(java.lang.Object)
	 */
	@Override
	public int setSelectedValue (V value)
	{
		return getModel().setSelectedValue(value);
	}
	/*
	 * @see net.community.chest.swing.models.TypedDisplayModeler#getItemText(int)
	 */
	@Override
	public String getItemText (int index)
	{
		return getModel().getItemText(index);
	}
	/*
	 * @see net.community.chest.swing.models.TypedDisplayModeler#getItemValue(int)
	 */
	@Override
	public V getItemValue (int index)
	{
		return getModel().getItemValue(index);
	}
	/*
	 * @see javax.swing.JComboBox#getItemAt(int)
	 */
	@SuppressWarnings("unchecked")
	@Override
	@CoVariantReturn
	public Map.Entry<String,V> getItemAt (int index)
	{
		return (Map.Entry<String,V>) super.getItemAt(index);
	}
	/*
	 * @see net.community.chest.awt.ModeledXmlConvertibleComponent#getConvertedModel()
	 */
	public TypedComboBoxModel<V> getConvertedModel ()
	{
		return getModel();
	}

	public XmlProxyConvertible<?> getInstanceConverter (final Element elem) throws Exception
	{
		return (null == elem) ? null : TypedComboBoxReflectiveProxy.TYPCBX;
	}
	/*
	 * @see net.community.chest.dom.XmlConvertible#fromXml(org.w3c.dom.Element)
	 */
	@Override
	public TypedComboBox<V> fromXml (final Element elem) throws Exception
	{
		final XmlProxyConvertible<?>	conv=getInstanceConverter(elem);
		@SuppressWarnings("unchecked")
		final Object					inst=(null == conv) ? null : ((XmlProxyConvertible<Object>) conv).fromXml(this, elem);
		if (inst != this)
			throw new IllegalStateException("fromXml(" + DOMUtils.toString(elem) + ") mismatched re-constructed instances");

		return this;
	}
 	/*
	 * @see net.community.chest.dom.XmlConvertible#toXml(org.w3c.dom.Document)
	 */
	@Override
	public Element toXml (final Document doc) throws Exception
	{
		throw new UnsupportedOperationException("toXml() N/A");
	}

	private Locale	_lcl	/* =null */;
	/*
	 * @see net.community.chest.ui.helpers.LocalizedComponent#getDisplayLocale()
	 */
	@Override
	public synchronized Locale getDisplayLocale ()
	{
		if (null == _lcl)
			_lcl = Locale.getDefault();
		return _lcl;
	}
	/*
	 * @see net.community.chest.ui.helpers.LocalizedComponent#setDisplayLocale(java.util.Locale)
	 */
	@Override
	public synchronized void setDisplayLocale (Locale l)
	{
		if (_lcl != l)	// debug breakpoint
			_lcl = l;
	}

	private Element	_elem;
	/*
	 * @see net.community.chest.ui.helpers.XmlElementComponentInitializer#getComponentElement()
	 */
	@Override
	public Element getComponentElement () throws RuntimeException
	{
		return _elem;
	}
	/*
	 * @see net.community.chest.ui.helpers.XmlElementComponentInitializer#setComponentElement(org.w3c.dom.Element)
	 */
	@Override
	public void setComponentElement (Element elem)
	{
		if (_elem != elem)
			_elem = elem;
	}
	/*
	 * @see net.community.chest.ui.helpers.XmlElementComponentInitializer#layoutComponent(org.w3c.dom.Element)
	 */
	@Override
	public void layoutComponent (Element elem) throws RuntimeException
	{
		if (elem != null)
		{
			try
			{
				final Object	o=fromXml(elem);
				if (o != this)
					throw new IllegalStateException("Mismatched re-constructed instances");
				setComponentElement(elem);	// remember last used
			}
			catch(Exception e)
			{
				throw ExceptionUtil.toRuntimeException(e);
			}
		}
	}
	/*
	 * @see net.community.chest.ui.helpers.ComponentInitializer#layoutComponent()
	 */
	@Override
	public void layoutComponent () throws RuntimeException
	{
		layoutComponent(getComponentElement());
	}

	public TypedComboBox (TypedComboBoxModel<V> aModel, Element elem, boolean autoLayout)
	{
		super(aModel);

		setComponentElement(elem);
		if (autoLayout)
			layoutComponent();
	}

	public TypedComboBox (TypedComboBoxModel<V> aModel, boolean autoLayout)
	{
		this(aModel, null, autoLayout);
	}
	/**
	 * @param aModel underlying model to be used
	 */
	public TypedComboBox (TypedComboBoxModel<V> aModel)
	{
		this(aModel, true);
	}

	public TypedComboBox (Class<V> valsClass, Element elem, boolean autoLayout)
	{
		this(new TypedComboBoxModel<V>(valsClass), elem, autoLayout);	
	}

	public TypedComboBox (Class<V> valsClass, boolean autoLayout)
	{
		this(valsClass, null, autoLayout);
	}
	/**
	 * Generates an automatic {@link TypedComboBoxModel} for the combo box
	 * @param valsClass class of values for the generated {@link TypedComboBoxModel} 
	 */
	public TypedComboBox (Class<V> valsClass)
	{
		this(valsClass, true);
	}
}
