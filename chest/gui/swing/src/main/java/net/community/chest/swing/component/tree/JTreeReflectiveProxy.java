/*
 * 
 */
package net.community.chest.swing.component.tree;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.NoSuchElementException;

import javax.swing.DropMode;
import javax.swing.JTree;
import javax.swing.tree.TreeSelectionModel;

import net.community.chest.CoVariantReturn;
import net.community.chest.swing.DropModeValue;
import net.community.chest.swing.component.JComponentReflectiveProxy;

import org.w3c.dom.Element;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @param <T> The {@link JTree} class being reflected 
 * @author Lyor G.
 * @since Aug 20, 2008 9:34:35 AM
 */
public class JTreeReflectiveProxy<T extends JTree> extends JComponentReflectiveProxy<T> {
	public JTreeReflectiveProxy (Class<T> objClass) throws IllegalArgumentException
	{
		this(objClass, false);
	}

	protected JTreeReflectiveProxy (Class<T> objClass, boolean registerAsDefault)
			throws IllegalArgumentException, IllegalStateException
	{
		super(objClass, registerAsDefault);
	}
	// special handling
	public static final String	DROP_MODE_ATTR="dropMode", SELMODE_ATTTR="selectionMode";
	/*
	 * @see net.community.chest.dom.transform.ReflectiveAttributesProxy#updateObjectAttribute(java.lang.Object, java.lang.String, java.lang.String, java.lang.reflect.Method)
	 */
	@Override
	protected T updateObjectAttribute (T src, String name, String value, Method setter) throws Exception
	{
		if (DROP_MODE_ATTR.equalsIgnoreCase(name))
		{
			final DropMode	m=DropModeValue.fromString(value);
			if (null == m)
				throw new NoSuchElementException(getArgumentsExceptionLocation("updateObjectAttribute", value) + " unknown '" + name + "' value");

			setter.invoke(src, m);
			return src;
		}

		return super.updateObjectAttribute(src, name, value, setter);
	}
	/*
	 * @see net.community.chest.awt.dom.proxy.ComponentReflectiveProxy#handleUnknownAttribute(java.awt.Component, java.lang.String, java.lang.String, java.util.Map)
	 */
	@Override
	protected T handleUnknownAttribute (T src, String name, String value, Map<String,? extends Method> accsMap) throws Exception
	{
		if (SELMODE_ATTTR.equalsIgnoreCase(name))
		{
			final SelectionModelType	m=SelectionModelType.fromString(value);
			if (null == m)
				throw new NoSuchElementException(getArgumentsExceptionLocation("handleUnknownAttribute", value) + " unknown '" + name + "' value");

			final TreeSelectionModel	selModel=src.getSelectionModel();
			selModel.setSelectionMode(m.getSelectionMode());
			return src;
		}

		return super.handleUnknownAttribute(src, name, value, accsMap);
	}

	public static final JTreeReflectiveProxy<JTree>	TREE=
				new JTreeReflectiveProxy<JTree>(JTree.class, true) {
			/* Extension implements some useful functionality
			 * @see net.community.chest.dom.transform.AbstractReflectiveProxy#createInstance(org.w3c.dom.Element)
			 */
			@Override
			@CoVariantReturn
			public BaseTree createInstance (Element elem) throws Exception
			{
				return (null == elem) ? null : new BaseTree();
			}
		};
}
