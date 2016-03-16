/*
 *
 */
package net.community.chest.swing.component.combobox;

import javax.swing.ComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.MutableComboBoxModel;

import net.community.chest.convert.ValueStringInstantiator;
import net.community.chest.dom.DOMUtils;
import net.community.chest.dom.transform.XmlValueInstantiator;
import net.community.chest.reflect.ClassUtil;
import net.community.chest.swing.component.JComponentReflectiveProxy;

import org.w3c.dom.Element;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @param <CB> The reflected {@link JComboBox} instance
 * @author Lyor G.
 * @since Aug 27, 2008 9:40:01 AM
 */
public class JComboBoxReflectiveProxy<CB extends JComboBox> extends JComponentReflectiveProxy<CB> {
    public JComboBoxReflectiveProxy (Class<CB> objClass) throws IllegalArgumentException
    {
        this(objClass, false);
    }

    protected JComboBoxReflectiveProxy (Class<CB> objClass, boolean registerAsDefault)
        throws IllegalArgumentException, IllegalStateException
    {
        super(objClass, registerAsDefault);
    }
    /**
     * Default XMl {@link Element} name used to denote an item to be added
     */
    public static final String    ITEM_CHOICE_ELEM_NAME="item";
    public boolean isItemChoiceElement (final Element elem, final String tagName)
    {
        return isMatchingElement(elem, tagName, ITEM_CHOICE_ELEM_NAME);
    }
    /**
     * Called by default implementation of {@link #getItemChoiceConverter(JComboBox, Element)}
     * @param src The {@link JComboBox} instance
     * @param elem The XML {@link Element} to be used for instantiation
     * @param itemClass The added item {@link Class}
     * @return The {@link XmlValueInstantiator} instance for the value
     * @throws Exception unless overridden throws {@link UnsupportedOperationException}
     * for any of the {@link Class}-es that do not have a &quot;built-in&quot;
     * {@link net.community.chest.convert.ValueStringInstantiator}
     */
    @SuppressWarnings("unchecked")
    public XmlValueInstantiator<? extends Object> getItemChoiceConverter (
            final CB src, final Element elem, final Class<?> itemClass) throws Exception
    {
        final ValueStringInstantiator<?>    vsi=resolveAttributeInstantiator(CLASS_ATTR, itemClass);
        if (vsi instanceof XmlValueInstantiator)
            return ((XmlValueInstantiator<? extends Object>) vsi);

        throw new UnsupportedOperationException("getItemChoiceConverter(" + ((null == itemClass) ? null : itemClass.getName()) + ")[" + DOMUtils.toString(elem) + "] N/A");
    }
    /**
     * Called by default implementation of {@link #addItemChoice(JComboBox, Element)}
     * in order to retrieve an {@link XmlValueInstantiator} instantiator
     * @param src The {@link JComboBox} instance
     * @param elem The XML {@link Element} to be used for instantiation
     * @return The {@link XmlValueInstantiator} instantiator. The default
     * implementation is looking for a {@link #CLASS_ATTR} attribute and
     * attempts to use it by instantiating a {@link Class} object and calling
     * the
     * @throws Exception if unable to generate an instantiator
     */
    public XmlValueInstantiator<? extends Object> getItemChoiceConverter (final CB src, final Element elem) throws Exception
    {
        final String    clsPath=elem.getAttribute(CLASS_ATTR);
        final Class<?>    c=
            ((null == clsPath) || (clsPath.length() <= 0)) ? null : ClassUtil.loadClassByName(clsPath);
        if (c != null)
            return getItemChoiceConverter(src, elem, c);

        throw new UnsupportedOperationException("getItemChoiceConverter(" + DOMUtils.toString(elem) + ") N/A");
    }
    /**
     * Called by default implementation of {@link #addItemChoice(JComboBox, Element)}
     * in order to add the choice to the {@link JComboBox}
     * @param src The {@link JComboBox} instance
     * @param elem The XML {@link Element} used for instantiation
     * @param item The instantiated item - may be null if no instantiation
     * took place - in which case nothing is done
     * @return Same as input item (unless extremely good reason otherwise)
     * @throws Exception If failed to add the item
     */
    public Object addItemChoice (final CB src, final Element elem, final Object item) throws Exception
    {
        if (item != null)
        {
            final ComboBoxModel<?>    m=src.getModel();
            if (!(m instanceof MutableComboBoxModel))
                throw new ClassCastException("addItemChoice(" + DOMUtils.toString(elem) + ")[" + item + "] combo-box model not " + MutableComboBoxModel.class.getSimpleName());

            @SuppressWarnings("unchecked")
            MutableComboBoxModel<Object> mcb=(MutableComboBoxModel<Object>) m;
            mcb.addElement(item);
        }

        return item;
    }
    /**
     * Called by default implementation of {@link #fromXmlChild(JComboBox, Element)}
     * when an {@link #isItemChoiceElement(Element, String)} call returns
     * <code>true</code> in order to enable populating the choices
     * @param src The {@link JComboBox} instance
     * @param elem The XML {@link Element} to be used to instantiate the
     * added choice
     * @return The added choice {@link Object} - null if none generated
     * @throws Exception If failed to instantiate/add the choice
     */
    public Object addItemChoice (final CB src, final Element elem) throws Exception
    {
        final XmlValueInstantiator<? extends Object>    objConverter=getItemChoiceConverter(src, elem);
        return addItemChoice(src, elem, (null == objConverter) ? null : objConverter.fromXml(elem));
    }
    /*
     * @see net.community.chest.swing.component.JComponentReflectiveProxy#fromXmlChild(javax.swing.JComponent, org.w3c.dom.Element)
     */
    @Override
    public CB fromXmlChild (CB src, Element elem) throws Exception
    {
        final String    tagName=elem.getTagName();
        if (isItemChoiceElement(elem, tagName))
        {
            addItemChoice(src, elem);
            return src;
        }

        return super.fromXmlChild(src, elem);
    }

    public static final JComboBoxReflectiveProxy<JComboBox>    COMBOBOX=
                    new JComboBoxReflectiveProxy<JComboBox>(JComboBox.class, true);
}
