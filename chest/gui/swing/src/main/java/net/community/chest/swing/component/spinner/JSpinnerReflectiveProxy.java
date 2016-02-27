/*
 * 
 */
package net.community.chest.swing.component.spinner;

import java.lang.reflect.Method;

import javax.swing.JComponent;
import javax.swing.JSpinner;
import javax.swing.SpinnerModel;

import net.community.chest.CoVariantReturn;
import net.community.chest.dom.DOMUtils;
import net.community.chest.dom.transform.XmlValueInstantiator;
import net.community.chest.swing.component.JComponentReflectiveProxy;

import org.w3c.dom.Element;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @param <S> The reflected {@link JSpinner} type
 * @author Lyor G.
 * @since Oct 15, 2008 3:19:05 PM
 */
public class JSpinnerReflectiveProxy<S extends JSpinner> extends JComponentReflectiveProxy<S> {
	public JSpinnerReflectiveProxy (Class<S> objClass) throws IllegalArgumentException
	{
		this(objClass, false);
	}

	protected JSpinnerReflectiveProxy (Class<S> objClass, boolean registerAsDefault)
		throws IllegalArgumentException, IllegalStateException
	{
		super(objClass, registerAsDefault);
	}

	public static final String	EDITOR_ATTR="editor";
	protected JComponent setEditor (S src, String aValue, Method setter) throws Exception
	{
		final JSpinner.DefaultEditor	e=DefaultEditorType.fromString(src, aValue);
		if (null == e)
			return null;
		
		setter.invoke(src, e);
		return e;
	}
	/*
	 * @see net.community.chest.awt.dom.UIReflectiveAttributesProxy#updateObjectAttribute(java.lang.Object, java.lang.String, java.lang.String, java.lang.Class, java.lang.reflect.Method)
	 */
	@Override
	protected S updateObjectAttribute (S src, String aName, String aValue, Class<?> aType, Method setter)
		throws Exception
	{
		if (EDITOR_ATTR.equalsIgnoreCase(aName))
		{
			setEditor(src, aValue, setter);
			return src;
		}

		return super.updateObjectAttribute(src, aName, aValue, aType, setter);
	}

	public static final String	MODEL_ATTR="model";
	public boolean isModelElement (final Element elem, final String tagName)
	{
		return isMatchingElement(elem, tagName, MODEL_ATTR);
	}
	// uses the 'class' attribute
	public XmlValueInstantiator<? extends SpinnerModel> getModelInstantiator (final Element elem) throws Exception
	{
		final String	type=elem.getAttribute(CLASS_ATTR);
		if (SpinnerNumberModelReflectiveProxy.NUMBER_TYPE.equalsIgnoreCase(type))
			return SpinnerNumberModelReflectiveProxy.NUMMODEL;

		throw new UnsupportedOperationException("getModelInstantiator(" + DOMUtils.toString(elem) + ") unknown type: " + type);
	}

	public SpinnerModel setModel (final S src, final Element elem) throws Exception
	{
		final XmlValueInstantiator<? extends SpinnerModel>	inst=getModelInstantiator(elem);
		final SpinnerModel									m=inst.fromXml(elem);
		if (m != null)
			src.setModel(m);

		return m;
	}
	/*
	 * @see net.community.chest.swing.component.JComponentReflectiveProxy#fromXmlChild(javax.swing.JComponent, org.w3c.dom.Element)
	 */
	@Override
	public S fromXmlChild (S src, Element elem) throws Exception
	{
		final String	tagName=elem.getTagName();
		if (isModelElement(elem, tagName))
		{
			setModel(src, elem);
			return src;
		}

		return super.fromXmlChild(src, elem);
	}

	public static final JSpinnerReflectiveProxy<JSpinner>	SPINNER=
				new JSpinnerReflectiveProxy<JSpinner>(JSpinner.class, true) {
					/*
					 * @see net.community.chest.dom.transform.AbstractReflectiveProxy#createInstance(org.w3c.dom.Element)
					 */
					@Override
					@CoVariantReturn
					public BaseSpinner createInstance (Element elem) throws Exception
					{
						return (null == elem) ? null : new BaseSpinner();
					}
			};
}
