/*
 * 
 */
package net.community.chest.swing.component.scroll;

import javax.swing.JScrollPane;

import net.community.chest.convert.ValueStringInstantiator;
import net.community.chest.swing.component.JComponentReflectiveProxy;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @param <P> The reflected {@link JScrollPane} instance
 * @author Lyor G.
 * @since Dec 3, 2008 3:48:11 PM
 */
public class JScrollPaneReflectiveProxy<P extends JScrollPane> extends JComponentReflectiveProxy<P> {
	public JScrollPaneReflectiveProxy (Class<P> objClass) throws IllegalArgumentException
	{
		this(objClass, false);
	}

	protected JScrollPaneReflectiveProxy (Class<P> objClass, boolean registerAsDefault)
		throws IllegalArgumentException, IllegalStateException
	{
		super(objClass, registerAsDefault);
	}

	// some attributes of interest
	public static final String	VERTICAL_POLICY_ATTR="VerticalScrollBarPolicy",
								HORIZONTAL_POLICY_ATTR="HorizontalScrollBarPolicy";
	/*
	 * @see net.community.chest.awt.dom.UIReflectiveAttributesProxy#resolveAttributeInstantiator(java.lang.String, java.lang.Class)
	 */
	@SuppressWarnings("unchecked")
	@Override
	protected <C> ValueStringInstantiator<C> resolveAttributeInstantiator (String name, Class<C> type) throws Exception
	{
		if (VERTICAL_POLICY_ATTR.equalsIgnoreCase(name))
			return (ValueStringInstantiator<C>) VerticalPolicyValueStringInstantiator.DEFAULT;
		else if (HORIZONTAL_POLICY_ATTR.equalsIgnoreCase(name))
			return (ValueStringInstantiator<C>) HorizontalPolicyValueStringInstantiator.DEFAULT;

		return super.resolveAttributeInstantiator(name, type);
	}

	public static final JScrollPaneReflectiveProxy<JScrollPane>	SCRLPNE=
		new JScrollPaneReflectiveProxy<JScrollPane>(JScrollPane.class, true);
}
