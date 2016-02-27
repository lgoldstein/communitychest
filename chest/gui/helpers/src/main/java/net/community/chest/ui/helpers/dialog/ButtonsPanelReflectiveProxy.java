/*
 * 
 */
package net.community.chest.ui.helpers.dialog;

import java.awt.Component;

import javax.swing.AbstractButton;

import net.community.chest.dom.transform.XmlValueInstantiator;
import net.community.chest.swing.component.JSeparatorReflectiveProxy;
import net.community.chest.swing.component.button.AbstractButtonReflectiveProxy;
import net.community.chest.swing.component.button.JButtonReflectiveProxy;
import net.community.chest.ui.helpers.panel.HelperPanelReflectiveProxy;

import org.w3c.dom.Element;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @param <P> The reflected {@link ButtonsPanel} instance
 * @author Lyor G.
 * @since Jan 6, 2009 3:06:44 PM
 */
public class ButtonsPanelReflectiveProxy<P extends ButtonsPanel> extends HelperPanelReflectiveProxy<P> {
	protected ButtonsPanelReflectiveProxy (Class<P> objClass, boolean registerAsDefault)
		throws IllegalArgumentException, IllegalStateException
	{
		super(objClass, registerAsDefault);
	}

	public ButtonsPanelReflectiveProxy (Class<P> objClass) throws IllegalArgumentException
	{
		this(objClass, false);
	}

	public boolean isButtonElement (final Element elem, final String tagName)
	{
		return isMatchingElement(elem, tagName, AbstractButtonReflectiveProxy.BUTTON_ELEMNAME);
	}

	public XmlValueInstantiator<? extends AbstractButton> getButtonInstantiator (final Element elem) throws Exception
	{
		return (null == elem) ? null : JButtonReflectiveProxy.BUTTON;
	}

	public AbstractButton addButton (P src, Element elem) throws Exception
	{
		final XmlValueInstantiator<? extends AbstractButton>	proxy=getButtonInstantiator(elem);
		final AbstractButton									b=(null == proxy) ? null : proxy.fromXml(elem);
		if (b != null)
			addConstrainedComponent(src, b, elem, true);

		return b;
	}

	public boolean isSeparatorElement (final Element elem, final String tagName)
	{
		return isMatchingElement(elem, tagName, JSeparatorReflectiveProxy.SEPARATOR_ELEMNAME);
	}

	public XmlValueInstantiator<? extends Component> getSeparatorInstantiator (Element elem) throws Exception
	{
		return (null == elem) ? null : JSeparatorReflectiveProxy.JSEP;
	}

	public Component addSeparator (P src, Element elem) throws Exception
	{
		final XmlValueInstantiator<? extends Component>	proxy=getSeparatorInstantiator(elem);
		final Component									s=(null == proxy) ? null : proxy.fromXml(elem);
		if (s != null)
			src.add(s);
		return s;
	}
	/*
	 * @see net.community.chest.swing.component.JComponentReflectiveProxy#fromXmlChild(javax.swing.JComponent, org.w3c.dom.Element)
	 */
	@Override
	public P fromXmlChild (P src, Element elem) throws Exception
	{
		final String	tagName=elem.getTagName();
		/*
		 *  	We handle the layout here instead of the base class since
		 *  we need it to choose the correct constraint (if any) for the
		 *  buttons that may be added
		 */
		if (isLayoutElement(elem, tagName))
		{
			setLayout(src, elem);
			return src;
		}
		else if (isButtonElement(elem, tagName))
		{
			addButton(src, elem);
			return src;
		}
		else if (isSeparatorElement(elem, tagName))
		{
			addSeparator(src, elem);
			return src;
		}

		return super.fromXmlChild(src, elem);
	}

	public static final ButtonsPanelReflectiveProxy<ButtonsPanel>	BTNSPNL=
		new ButtonsPanelReflectiveProxy<ButtonsPanel>(ButtonsPanel.class, true);
}
