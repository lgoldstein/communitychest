/*
 * 
 */
package net.community.chest.ui.helpers.panel.input;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.swing.JTextField;

import net.community.chest.awt.attributes.Textable;
import net.community.chest.dom.DOMUtils;
import net.community.chest.dom.proxy.XmlProxyConvertible;
import net.community.chest.swing.component.text.JTextFieldReflectiveProxy;
import net.community.chest.ui.helpers.panel.HelperPanelReflectiveProxy;
import net.community.chest.util.collection.CollectionsUtils;

import org.w3c.dom.Element;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @param <P> Type of reflected {@link AbstractInputTextPanel}
 * @author Lyor G.
 * @since Aug 20, 2008 2:37:28 PM
 */
public class InputTextPanelReflectiveProxy<P extends AbstractInputTextPanel> extends HelperPanelReflectiveProxy<P> {
	public InputTextPanelReflectiveProxy (Class<P> objClass) throws IllegalArgumentException
	{
		this(objClass, false);
	}

	protected InputTextPanelReflectiveProxy (Class<P> objClass, boolean registerAsDefault)
		throws IllegalArgumentException, IllegalStateException
	{
		super(objClass, registerAsDefault);
	}

	private static final List<String>	DEFERRED_ATTRS=Arrays.asList(
			Textable.ATTR_NAME
		);
	/*
	 * @see net.community.chest.dom.proxy.AbstractReflectiveProxy#handleDeferredAttribute(java.lang.Object, java.lang.String, java.lang.String, java.lang.reflect.AccessibleObject)
	 */
	@Override
	protected P handleDeferredAttribute (P src, String aName, String aValue, Method setter) throws Exception
	{
		if (CollectionsUtils.containsElement(DEFERRED_ATTRS, aName, String.CASE_INSENSITIVE_ORDER))
			return super.updateObjectAttribute(src, aName, aValue, setter);
		else
			return super.handleDeferredAttribute(src, aName, aValue, setter);
	}
	/*
	 * @see net.community.chest.dom.proxy.ReflectiveAttributesProxy#updateObjectAttribute(java.lang.Object, java.lang.String, java.lang.String, java.lang.reflect.Method)
	 */
	@Override
	protected P updateObjectAttribute (final P src, final String aName, final String aValue, final Method setter) throws Exception
	{
		if (CollectionsUtils.containsElement(DEFERRED_ATTRS, aName, String.CASE_INSENSITIVE_ORDER))
			return src;	// defer these attributes till after the children set

		return super.updateObjectAttribute(src, aName, aValue, setter);
	}

	public boolean isTextFieldElement (final Element elem, final String tagName)
	{
		return isMatchingElement(elem, tagName, JTextFieldReflectiveProxy.TEXTFIELD_ELEMNAME);
	}

	public XmlProxyConvertible<? extends JTextField> getTextFieldConverter (final Element elem) throws Exception
	{
		return (null == elem) ? null : JTextFieldReflectiveProxy.TXTFIELD;
	}

	public JTextField setTextField (final P src, final Element elem) throws Exception
	{
		final XmlProxyConvertible<? extends JTextField>	proxy=getTextFieldConverter(elem);
		final JTextField								orgField=src.getTextField(), f;
		if (null == orgField)
		{
			if ((f=proxy.fromXml(elem)) != null)
				src.setTextField(f);
		}
		else
		{
			@SuppressWarnings("unchecked")
			final Object	o=((XmlProxyConvertible<JTextField>) proxy).fromXml(orgField, elem);
			if (o != orgField)
				throw new IllegalStateException("setTextField(" + DOMUtils.toString(elem) + ") mismatched reconstructed instances");

			f = orgField;
		}

		return f;
	}
	/*
	 * @see net.community.chest.swing.component.panel.JPanelReflectiveProxy#fromXmlChild(javax.swing.JPanel, org.w3c.dom.Element)
	 */
	@Override
	public P fromXmlChild (final P src, final Element elem) throws Exception
	{
		final String	tagName=elem.getTagName();
		if (isTextFieldElement(elem, tagName))
		{
			setTextField(src, elem);
			return src;
		}

		return super.fromXmlChild(src, elem);
	}
	/*
	 * @see net.community.chest.dom.proxy.AbstractReflectiveProxy#fromXmlChildren(java.lang.Object, java.util.Collection, java.util.Map)
	 */
	@Override
	public P fromXmlChildren (P src, Collection<? extends Element> cl, Map<String,AttributeHandlingResult<Method>> resMap) throws Exception
	{
		// handle any deferred attributes
		final P	sVal=super.fromXmlChildren(src, cl, resMap);
		return fromXmlAttributes(sVal, resMap, true, DEFERRED_ATTRS);
	}

	public static final InputTextPanelReflectiveProxy<AbstractInputTextPanel> ITEXTPNL=
		new InputTextPanelReflectiveProxy<AbstractInputTextPanel>(AbstractInputTextPanel.class, true);
}
