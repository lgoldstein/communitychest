package net.community.chest.swing.component.label;

import java.lang.reflect.Method;

import javax.swing.JLabel;

import net.community.chest.awt.dom.converter.KeyCodeValueInstantiator;
import net.community.chest.convert.ValueStringInstantiator;
import net.community.chest.swing.HAlignmentValueStringInstantiator;
import net.community.chest.swing.VAlignmentValueStringInstantiator;
import net.community.chest.swing.component.JComponentReflectiveProxy;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @param <L> The reflected {@link JLabel} type
 * @author Lyor G.
 * @since Mar 24, 2008 11:17:19 AM
 */
public class JLabelReflectiveProxy<L extends JLabel> extends JComponentReflectiveProxy<L> {
	public JLabelReflectiveProxy (Class<L> objClass) throws IllegalArgumentException
	{
		this(objClass, false);
	}

	protected JLabelReflectiveProxy (Class<L> objClass, boolean registerAsDefault)
		throws IllegalArgumentException, IllegalStateException
	{
		super(objClass, registerAsDefault);
	}
	/**
	 * Default element name used for labels(s)
	 */
	public static final String LABEL_ELEMNAME="label";

	// some specialized values handling
	public static final String	HALIGN_ATTR="horizontalAlignment",
								HTEXTPOS_ATTR="horizontalTextPosition",
								VALIGN_ATTR="verticalAlignment",
								VTEXTPOS_ATTR="verticalTextPosition",
								DISPMNEMONIC_ATTR="displayedMnemonic",
								ICON_ATTR="icon",
								DISABLED_ICON_ATTR="disabledIcon";
	/*
	 * @see net.community.chest.awt.dom.UIReflectiveAttributesProxy#resolveAttributeInstantiator(java.lang.String, java.lang.Class)
	 */
	@SuppressWarnings("unchecked")
	@Override
	protected <C> ValueStringInstantiator<C> resolveAttributeInstantiator (String name, Class<C> type) throws Exception
	{
		if (HALIGN_ATTR.equalsIgnoreCase(name)
		 || HTEXTPOS_ATTR.equalsIgnoreCase(name))
			return (ValueStringInstantiator<C>) HAlignmentValueStringInstantiator.DEFAULT;
		else if (VALIGN_ATTR.equalsIgnoreCase(name)
			  || VTEXTPOS_ATTR.equalsIgnoreCase(name))
			return (ValueStringInstantiator<C>) VAlignmentValueStringInstantiator.DEFAULT;
		else if (DISPMNEMONIC_ATTR.equalsIgnoreCase(name))
			return (ValueStringInstantiator<C>) KeyCodeValueInstantiator.DEFAULT;

		return super.resolveAttributeInstantiator(name, type);
	}
	/*
	 * @see net.community.chest.dom.transform.ReflectiveAttributesProxy#updateObjectAttribute(java.lang.Object, java.lang.String, java.lang.String, java.lang.reflect.Method)
	 */
	@Override
	protected L updateObjectAttribute (L src, String name, String value, Method setter) throws Exception
	{
		if (ICON_ATTR.equalsIgnoreCase(name)
		 || DISABLED_ICON_ATTR.equalsIgnoreCase(name))
			return updateObjectResourceAttribute(src, name, value, setter);

		return super.updateObjectAttribute(src, name, value, setter);
	}

	public static final JLabelReflectiveProxy<JLabel>	LABEL=
					new JLabelReflectiveProxy<JLabel>(JLabel.class, true);
}
