/*
 * 
 */
package net.community.chest.swing.component;

import javax.swing.JSeparator;

import net.community.chest.convert.ValueStringInstantiator;
import net.community.chest.swing.SwingConstantsValueStringInstantiator;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @param <S> The reflected {@link JSeparator}
 * @author Lyor G.
 * @since Sep 24, 2008 11:00:14 AM
 */
public class JSeparatorReflectiveProxy<S extends JSeparator> extends JComponentReflectiveProxy<S> {
	public static final String SEPARATOR_ELEMNAME="separator";

	public JSeparatorReflectiveProxy (Class<S> objClass) throws IllegalArgumentException
	{
		this(objClass, false);
	}

	protected JSeparatorReflectiveProxy (Class<S> objClass, boolean registerAsDefault)
		throws IllegalArgumentException, IllegalStateException
	{
		super(objClass, registerAsDefault);
	}

	public static final String	ORIENTATION_ATTR="orientation";
	/*
	 * @see net.community.chest.awt.dom.UIReflectiveAttributesProxy#resolveAttributeInstantiator(java.lang.String, java.lang.Class)
	 */
	@SuppressWarnings("unchecked")
	@Override
	protected <C> ValueStringInstantiator<C> resolveAttributeInstantiator (String name, Class<C> type) throws Exception
	{
		if (ORIENTATION_ATTR.equalsIgnoreCase(name))
			return (ValueStringInstantiator<C>) SwingConstantsValueStringInstantiator.DEFAULT;

		return super.resolveAttributeInstantiator(name, type);
	}

	public static final JSeparatorReflectiveProxy<JSeparator>	JSEP=
			new JSeparatorReflectiveProxy<JSeparator>(JSeparator.class, true);
}
