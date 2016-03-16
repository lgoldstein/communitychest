package net.community.chest.ui.helpers.combobox;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

import javax.swing.DefaultComboBoxModel;

import net.community.chest.CoVariantReturn;
import net.community.chest.dom.DOMUtils;
import net.community.chest.dom.transform.XmlConvertible;
import net.community.chest.lang.TypedValuesContainer;
import net.community.chest.swing.models.SingleSelectionModeler;
import net.community.chest.swing.models.TypedDisplayModeler;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * <P>Copyright 2007 as per GPLv2</P>
 *
 * <P>Provides a "typed" combo box model whereas each "element" is actually a
 * pair - text + value. The text is the displayed name in the combo box entry
 * and the value is the associated item. In other words, the elements stored
 * in the {@link DefaultComboBoxModel} from which this object is derived are
 * actually such pairs. For implementation convenience reasons, the typed pair
 * is represented as a {@link java.util.Map.Entry} where the "key" is the string text
 * and the value is the associated item.</P>
 *
 * @param <V> The generic combo-box entry type
 * @author Lyor G.
 * @since Jun 13, 2007 2:09:58 PM
 */
public class TypedComboBoxModel<V> extends DefaultComboBoxModel
            implements  TypedValuesContainer<V>,
                        TypedDisplayModeler<V>,
                        SingleSelectionModeler<V>,
                        XmlConvertible<TypedComboBoxModel<V>> {
    /**
     *
     */
    private static final long serialVersionUID = -5861341719740199771L;
    private final Class<V>    _valsClass;
    /*
     * @see net.community.chest.TypedValuesContainer#getValuesClass()
     */
    @Override
    public final /* no cheating */ Class<V> getValuesClass ()
    {
        return _valsClass;
    }
    /**
     * @param valsClass {@link Class} representing the expected items type
     * associated with each entry in the combo box
     * @throws IllegalArgumentException if null values {@link Class} instance
     * supplied as parameter
     */
    public TypedComboBoxModel (final Class<V> valsClass) throws IllegalArgumentException
    {
        if (null == (_valsClass=valsClass))
            throw new IllegalArgumentException("no values class supplied");
    }
    /*
     * @see net.community.chest.swing.models.TypedDisplayModeler#addItem(java.util.Map.Entry)
     */
    @Override
    public void addItem (final Map.Entry<String,? extends V> item)
    {
        if (item != null)
            super.addElement(item);
    }
    /*
     * @see net.community.chest.swing.models.TypedDisplayModeler#addItem(java.lang.String, java.lang.Object)
     */
    @Override
    public Map.Entry<String,V> addItem (final String text, final V value) throws IllegalArgumentException
    {
        if ((null == text) || (text.length() <= 0))
            throw new IllegalArgumentException("null/empty combo box item name");

        final Map.Entry<String,V>    item=new TypedComboBoxEntry<V>(text, value);
        addItem(item);
        return item;
    }
    /*
     * @see net.community.chest.swing.models.TypedDisplayModeler#addItems(java.util.Collection)
     */
    @Override
    public void addItems (final Collection<? extends Map.Entry<String,? extends V>> items)
    {
        if ((items != null) && (items.size() > 0))
        {
            for (final Map.Entry<String,? extends V> e : items)
            {
                if (e != null)
                    addItem(e);
            }
        }
    }
    /*
     * @see net.community.chest.swing.models.TypedDisplayModeler#addItems(java.util.Map)
     */
    @Override
    public void addItems (final Map<String,? extends V> items)
    {
        if ((items != null) && (items.size() > 0))
            addItems(items.entrySet());
    }
    /*
     * @see net.community.chest.swing.models.TypedDisplayModeler#addItems(java.util.Map.Entry<java.lang.String,V>[])
     */
    @Override
    public void addItems (final Map.Entry<String, V> ... items)
    {
        if ((items != null) && (items.length > 0))
            addItems(Arrays.asList(items));
    }
    /*
     * @see net.community.chest.swing.models.TypedDisplayModeler#getValueDisplayText(java.lang.Object)
     */
    @Override
    public String getValueDisplayText (final V value)
    {
        return (null == value) ? null : value.toString();
    }
    /*
     * @see net.community.chest.swing.models.TypedDisplayModeler#addValue(java.lang.Object)
     */
    @Override
    public Map.Entry<String,V> addValue (V value)
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
     * @see javax.swing.DefaultComboBoxModel#addElement(java.lang.Object)
     */
    @Override
    public void addElement (Object anObject)
    {
        throw new UnsupportedOperationException("addItem for typed combo box instead of addElement");
    }
    /* NOTE: returns null if invalid index
     * @see javax.swing.DefaultComboBoxModel#getElementAt(int)
     */
    @SuppressWarnings("unchecked")
    @Override
    @CoVariantReturn
    public Map.Entry<String,V> getElementAt (final int index)
    {
        if ((index < 0) || (index >= getSize()))
            return null;
        return (Map.Entry<String, V>) super.getElementAt(index);
    }
    /*
     * @see net.community.chest.swing.models.TypedDisplayModeler#getItemAt(int)
     */
    @Override
    public Map.Entry<String, V> getItemAt (final int index)
    {
        return getElementAt(index);
    }
    /*
     * @see net.community.chest.swing.models.TypedDisplayModeler#getItemValue(int)
     */
    @Override
    public V getItemValue (final int index)
    {
        final Map.Entry<String, V>    item=getElementAt(index);
        return (null == item) /* can happen if invalid index */ ? null : item.getValue();
    }
    /*
     * @see net.community.chest.swing.models.TypedDisplayModeler#getItemText(int)
     */
    @Override
    public String getItemText (final int index)
    {
        final Map.Entry<String, V>    item=getElementAt(index);
        return (null == item) /* can happen if invalid index */ ? null : item.getKey();
    }
    /*
     * @see javax.swing.DefaultComboBoxModel#getIndexOf(java.lang.Object)
     */
    @Override
    public int getIndexOf (Object anObject)
    {
        throw new UnsupportedOperationException("getIndexOf(Object) N/A for typed combo box");
    }
    /*
     * @see javax.swing.DefaultComboBoxModel#getSelectedItem()
     */
    @SuppressWarnings("unchecked")
    @Override
    @CoVariantReturn
    public Map.Entry<String,V> getSelectedItem ()
    {
        return (Map.Entry<String, V>) super.getSelectedItem();
    }
    /*
     * @see net.community.chest.swing.models.SingleSelectionModeler#getSelectedIndex()
     */
    @Override
    public int getSelectedIndex ()
    {
        return super.getIndexOf(getSelectedItem());
    }
    /*
     * @see net.community.chest.swing.models.SingleSelectionModeler#setSelectedIndex(int)
     */
    @Override
    public void setSelectedIndex (int index)
    {
        if ((index >= 0) && (index < getSize()))
            super.setSelectedItem(getElementAt(index));
    }
    /*
     * @see net.community.chest.swing.models.SingleSelectionModeler#getSelectedValue()
     */
    @Override
    public V getSelectedValue ()
    {
        final Map.Entry<String, V>    item=getSelectedItem();
        return (null == item) /* can happen if no selection */ ? null : item.getValue();
    }
    /*
     * @see net.community.chest.swing.models.SingleSelectionModeler#setSelectedValue(java.lang.Object)
     */
    @Override
    public int setSelectedValue (V value)
    {
        final int    numElems=getSize();
        for (int eIndex=0; eIndex < numElems; eIndex++)
        {
            final V    vElem=getItemValue(eIndex);
            if ((value == vElem)
             ||    ((vElem != null) && vElem.equals(value)))
            {
                setSelectedIndex(eIndex);
                return eIndex;
            }
        }

        return (-1);
    }
    /*
     * @see net.community.chest.swing.models.SingleSelectionModeler#getSelectedText()
     */
    @Override
    public String getSelectedText ()
    {
        final Map.Entry<String, V>    item=getSelectedItem();
        return (null == item) /* can happen if no selection */ ? null : item.getKey();
    }
    /*
     * @see javax.swing.DefaultComboBoxModel#insertElementAt(java.lang.Object, int)
     */
    @Override
    public void insertElementAt (Object anObject, int index)
    {
        throw new UnsupportedOperationException("insertElementAt(" + index + ")[" + anObject + "] N/A for typed combo box");
    }
    /*
     * @see javax.swing.DefaultComboBoxModel#setSelectedItem(java.lang.Object)
     */
    @Override
    public void setSelectedItem (Object anObject)
    {
        if ((null == anObject) || (!(anObject instanceof Map.Entry<?,?>)))
            throw new UnsupportedOperationException("setSelectedItem(" + anObject + ") bad element");

        final Object    aValue=((Map.Entry<?,?>) anObject).getValue();
        final Class<?>    aClass=(null == aValue) /* OK */ ? null : aValue.getClass();
        if (aClass != null)
        {
            final Class<?>    vClass=getValuesClass();
            if (!vClass.isAssignableFrom(aClass))
                throw new UnsupportedOperationException("setSelectedItem(" + aClass.getName() + ") bad value class - expected " + vClass.getName());
        }

        setSelectedValue((null == aValue) ? null : getValuesClass().cast(aValue));
    }
    /*
     * @see net.community.chest.swing.models.SingleSelectionModeler#getItemCount()
     */
    @Override
    public int getItemCount ()
    {
        return getSize();
    }
    /**
     * Default name of XML root {@link Element} generated by {@link #fromXml(Element)} method
     */
    public static final String    DEFAULT_ROOT_ELEMNAME="cbmodel",
    /**
     * Default name of XML {@link Element} representing an item
     */
                                DEFAULT_ITEM_ELEMNAME="item",
                                    ITEM_NAME_ATTR="name",
                                    ITEM_VALUE_ATTR="value";
    /**
     * @return name of XML {@link Element} representing an item to be used
     * by default implementation of {@link #toXml(Document, Element, java.util.Map.Entry)}
     */
    public String getItemElementName ()
    {
        return DEFAULT_ITEM_ELEMNAME;
    }
    /**
     * Called by default {@link #toXml(Document)} implementation in order to
     * create the specific XML element for the item. The default generate
     * element uses the {@link #ITEM_NAME_ATTR} as the item name and the
     * {@link #ITEM_VALUE_ATTR} as its associated (string) value. <B>Note:</B>
     * if null string value, then no {@link #ITEM_VALUE_ATTR} attribute is
     * added to the XML element (P.S. <U>empty</U> strings <U>are</U> added
     * as empty {@link #ITEM_VALUE_ATTR} attribute).
     * @param doc {@link Document} object to be used to create the element
     * @param root root element obtained via {@link #createRootElement(Document)}
     * call. <B>Note:</B> do <U>not</U> append the generated XML element to
     * the root unless you return null as result - otherwise returned element
     * will be <U>duplicated</U> due to automatic call to <I>appendChild</I>
     * by {@link #fromXml(Element)} if non-null value returned from this call.
     * @param item item value
     * @return {@link Element} to be appended as child of the root element
     * obtained via {@link #createRootElement(Document)} call - if null then
     * nothing append to root element
     * @throws Exception if cannot generate requested XML element
     */
    public Element toXml (Document doc, Element root, Map.Entry<String, V> item) throws Exception
    {
        final Element    itemElem=doc.createElement(getItemElementName());
        final String    itemName=item.getKey();
        itemElem.setAttribute(ITEM_NAME_ATTR, itemName);

        final V            itemValue=item.getValue();
        final String    itemValStr=(null == itemValue) ? null : itemValue.toString();
        if (itemValStr != null)
            itemElem.setAttribute(ITEM_VALUE_ATTR, itemValStr);

        if (null == root)    // just so compiler does not complain about unused root parameter
            throw new IllegalStateException("toXml(" + itemName + ") no root element");

        return itemElem;
    }
    /**
     * @return name of root {@link Element} generated by {@link #fromXml(Element)} method
     * default={@link #DEFAULT_ROOT_ELEMNAME}
     */
    public String getRootElementName ()
    {
        return DEFAULT_ROOT_ELEMNAME;
    }
    /**
     * Called by default {@link #toXml(Document)} implementation in order to
     * create the root XML {@link Element} under which the current items
     * sub-elements will be appened
     * @param doc {@link Document} object to be used to create the root element
     * @return root element (may NOT be null)
     * @throws Exception if unable to create element
     * @see #getRootElementName()
     */
    public Element createRootElement (Document doc) throws Exception
    {
        final String    rootName=getRootElementName();
        final Element    root=doc.createElement(rootName);
        return root;
    }
    /*
     * @see net.community.chest.dom.XmlConvertible#toXml(org.w3c.dom.Document)
     */
    @Override
    public Element toXml (Document doc) throws Exception
    {
        final Element    root=createRootElement(doc);
        final int        numItems=getSize();
        for (int    i=0; i < numItems; i++)
        {
            final Map.Entry<String, V>     item=getItemAt(i);
            final Element                elem=toXml(doc, root, item);
            if (elem != null)
                root.appendChild(elem);
        }

        return root;
    }

    private Constructor<V>    _ctor    /* =null */;
    /**
     * @return {@link Constructor} that accepts a single {@link String}
     * value as its argument - used by default {@link #fromXmlItem(Element)}
     * implementation to "reconstruct" the value from it string.
     * @throws Exception if unable to get such a constructor
     */
    protected Constructor<V> getStringValueConstructor () throws Exception
    {
        if (null == _ctor)
            _ctor = getValuesClass().getConstructor(String.class);
        return _ctor;
    }
    /**
     * Recovers the value from its XML element - default implementation simply
     * recovers the string value and then employs the {@link Constructor}
     * received from the {@link #getStringValueConstructor()} call
     * @param itemElem original XML element from which to extract the value
     * @param itemValStr the value string - null if missing {@link #ITEM_VALUE_ATTR}
     * attribute (which is the convention for null values - as opposed to
     * <U>empty</U> ones)
     * @return extracted value - may be null...
     * @throws Exception if unable to extract the value
     */
    public V fromXmlItemValueString (final Element itemElem, final String itemValStr) throws Exception
    {
        if (null == itemElem)    // mostly so that compiler does not complain about un-used parameter
            throw new IllegalArgumentException("fromXmlItemValueString(" + itemValStr + ") no " + Element.class.getName() + " instance");
        return getStringValueConstructor().newInstance(itemValStr);
    }
    /**
     * @param itemElem XML element representing one combo box ite,
     * @return reconstructed item - if null then nothing is added to the
     * combo box
     * @throws Exception if unable to reconstruct the item
     */
    public Map.Entry<String, V> fromXmlItem (final Element itemElem) throws Exception
    {
        final String    itemName=itemElem.getAttribute(ITEM_NAME_ATTR);
        if ((null == itemName) || (itemName.length() <= 0))
            throw new IllegalArgumentException("fromXmlItem(" + itemElem.getTagName() + ") no '" + ITEM_NAME_ATTR + "' attribute value");

        final V    itemValue=fromXmlItemValueString(itemElem, itemElem.getAttribute(ITEM_VALUE_ATTR));
        return new TypedComboBoxEntry<V>(itemName, itemValue);
    }

    public Collection<Map.Entry<String, V>> fromXml (final Collection<? extends Element> el) throws Exception
    {
        final int    numNodes=(null == el) ? 0 : el.size();
        if (numNodes <= 0)
            return null;

        final Collection<Map.Entry<String, V>>    res=new ArrayList<Map.Entry<String,V>>(numNodes);
        for (final Element elem : el)
        {
            final Map.Entry<String, V>    item=fromXmlItem(elem);
            if (item != null)
            {
                addItem(item);
                res.add(item);
            }
        }

        return res;
    }

    public Collection<Map.Entry<String, V>> fromXml (final NodeList nodes) throws Exception
    {
        return fromXml(DOMUtils.extractAllNodes(Element.class, nodes, Node.ELEMENT_NODE));
    }
    /*
     * @see net.community.chest.dom.XmlConvertible#fromXml(org.w3c.dom.Element)
     */
    @Override
    public TypedComboBoxModel<V> fromXml (Element root) throws Exception
    {
        if (root != null)
            fromXml(root.getChildNodes());

        return this;
    }
}
