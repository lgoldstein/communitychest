/*
 *
 */
package net.community.chest.ui.helpers.combobox;

import java.util.Map;

import net.community.chest.CoVariantReturn;
import net.community.chest.dom.DOMUtils;
import net.community.chest.dom.transform.XmlValueInstantiator;
import net.community.chest.swing.component.combobox.JComboBoxReflectiveProxy;

import org.w3c.dom.Element;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @param <V> The {@link TypedComboBox} enclosed value
 * @param <CB> The reflected {@link TypedComboBox} instance
 * @author Lyor G.
 * @since Aug 27, 2008 9:51:08 AM
 */
public class TypedComboBoxReflectiveProxy<V,CB extends TypedComboBox<V>> extends JComboBoxReflectiveProxy<CB> {
    public TypedComboBoxReflectiveProxy (Class<CB> objClass) throws IllegalArgumentException
    {
        this(objClass, false);
    }

    protected TypedComboBoxReflectiveProxy (Class<CB> objClass, boolean registerAsDefault)
        throws IllegalArgumentException, IllegalStateException
    {
        super(objClass, registerAsDefault);
    }
    /**
     * Called by default implementation of {@link #addItemChoice(TypedComboBox, Element, Object)}
     * in order to resolve the text {@link String} to be displayed for the item
     * @param src The {@link TypedComboBox} instance
     * @param elem The XML {@link Element} that was used to instantiate the item
     * @param val The instantiated item
     * @return <P>The {@link String} to be used. The default implementation tries
     * the following options (in the specified order):</P></BR>
     * <UL>
     *         <LI>
     *         Check if the XML {@link Element} has a text following it - e.g.,:</BR></BR>
     *         <P>
     *         &lt;item ...&gt;This is a text&lt;/item&gt;
     *         </P></BR>
     *         </LI>
     *
     *         <LI>
     *         Use the {@link TypedComboBox#getValueDisplayText(Object)} result
     *         </LI>
     *
     *         <LI>
     *         Use the {@link Object#toString()}
     *         </LI>
     * </UL>
     * <B>Note:</B> the transition to next option is made only if the current
     * text value is null/empty
     * </P>
     * @throws Exception If no text can be resolved.
     */
    public String resolveItemChoiceText (CB src, Element elem, V val) throws Exception
    {
        String    valText=null;
        // first check if have specific text in the XML element
        if (((valText=DOMUtils.getElementStringValue(elem)) != null) && (valText.length() > 0))
            return valText;

        // if no specific text specified in element then ask the combo-box
        if (((valText=src.getValueDisplayText(val)) != null) && (valText.length() > 0))
            return valText;

        // if still failed, try toString() call
        if (((valText=(null == val) ? null : val.toString()) != null) && (valText.length() > 0))
            return valText;

        throw new IllegalStateException("resolveItemChoiceText(" + DOMUtils.toString(elem) + ")[" + val + "] no resolved item text");
    }
    /*
     * @see net.community.chest.swing.component.combobox.JComboBoxReflectiveProxy#addItemChoice(javax.swing.JComboBox, org.w3c.dom.Element, java.lang.Object)
     */
    @Override
    @CoVariantReturn
    public Map.Entry<String,V> addItemChoice (CB src, Element elem, Object item) throws Exception
    {
        if (null == item)
            return null;

        final Class<V>    valsClass=src.getValuesClass();
        final Class<?>    itemClass=item.getClass();
        if (!valsClass.isAssignableFrom(itemClass))
            throw new ClassCastException("addItemChoice(" + DOMUtils.toString(elem) + ")[" + item + "] item class (" + itemClass.getName() + ") is not compatible with " + TypedComboBox.class.getSimpleName() + " values class (" + valsClass.getName() + ")");

        final V            val=valsClass.cast(item);
        final String    valText=resolveItemChoiceText(src, elem, val);
        return src.addItem(valText, val);
    }
    /*
     * @see net.community.chest.swing.component.combobox.JComboBoxReflectiveProxy#getItemChoiceConverter(javax.swing.JComboBox, org.w3c.dom.Element, java.lang.Class)
     */
    @Override
    public XmlValueInstantiator<? extends Object> getItemChoiceConverter (CB src, Element elem, Class<?> itemClass) throws Exception
    {
        final Class<V>    valsClass=(null == src) ? null : src.getValuesClass();
        if ((null == valsClass) || (null == itemClass) || (!valsClass.isAssignableFrom(itemClass)))
            throw new ClassCastException("getItemChoiceConverter(" + ((null == itemClass) ? null : itemClass.getName()) + ")[" + DOMUtils.toString(elem) + "] mismatched values class: " + ((null == valsClass) ? null : valsClass.getName()));

        return super.getItemChoiceConverter(src, elem, itemClass);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static final TypedComboBoxReflectiveProxy    TYPCBX=
        new TypedComboBoxReflectiveProxy(TypedComboBox.class, true) {
                /*
                 * @see net.community.chest.dom.transform.AbstractReflectiveProxy#createInstance(org.w3c.dom.Element)
                 */
                @Override
                @CoVariantReturn
                public TypedComboBox<?> createInstance (Element elem) throws Exception
                {
                    final Class<?>    vc=loadElementClass(elem);
                    if (null == vc)
                        throw new ClassNotFoundException("createInstance(" + DOMUtils.toString(elem) + ") no class loaded");

                    return new TypedComboBox(vc);
                }
            };
}
