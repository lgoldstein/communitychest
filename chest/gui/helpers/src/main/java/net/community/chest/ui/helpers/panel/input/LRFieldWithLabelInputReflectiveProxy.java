/*
 * 
 */
package net.community.chest.ui.helpers.panel.input;

import javax.swing.JTextField;

import net.community.chest.dom.proxy.XmlProxyConvertible;
import net.community.chest.ui.helpers.text.InputTextFieldReflectiveProxy;

import org.w3c.dom.Element;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @param <P> The reflected {@link LRFieldWithLabelInput} type
 * @author Lyor G.
 * @since Jan 13, 2009 11:19:54 AM
 */
public class LRFieldWithLabelInputReflectiveProxy<P extends LRFieldWithLabelInput> extends LRFieldWithLabelReflectiveProxy<P> {
	protected LRFieldWithLabelInputReflectiveProxy (Class<P> objClass, boolean registerAsDefault)
		throws IllegalArgumentException, IllegalStateException
	{
		super(objClass, registerAsDefault);
	}

	public LRFieldWithLabelInputReflectiveProxy (Class<P> objClass)
			throws IllegalArgumentException
	{
		this(objClass, false);
	}
	/*
	 * @see net.community.chest.ui.helpers.panel.input.InputTextPanelReflectiveProxy#getTextFieldConverter(org.w3c.dom.Element)
	 */
	@Override
	public XmlProxyConvertible<? extends JTextField> getTextFieldConverter (final Element elem) throws Exception
	{
		return (null == elem) ? null : InputTextFieldReflectiveProxy.INPTXT;
	}

	public static final LRFieldWithLabelInputReflectiveProxy<LRFieldWithLabelInput>	LRFLBLINP=
		new LRFieldWithLabelInputReflectiveProxy<LRFieldWithLabelInput>(LRFieldWithLabelInput.class, true);
}
