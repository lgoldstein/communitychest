/*
 * 
 */
package net.community.chest.ui.helpers.table;

import java.lang.reflect.Method;
import java.util.Map;

import net.community.chest.convert.ValueStringInstantiator;
import net.community.chest.swing.component.table.TableColumnReflectiveProxy;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @param <E> The column type {@link Enum}
 * @param <C> The actual column
 * @author Lyor G.
 * @since Sep 17, 2008 2:49:43 PM
 */
public abstract class EnumTableColumnReflectiveProxy<E extends Enum<E>,C extends EnumTableColumn<E>>
			extends TableColumnReflectiveProxy<C> {
	private final Class<E>	_colClass;
	public final /* no cheating */ Class<E> getColumnTypeClass ()
	{
		return _colClass;
	}

	protected EnumTableColumnReflectiveProxy (final Class<E> colClass, final Class<C> objClass, boolean registerAsDefault)
		throws IllegalArgumentException, IllegalStateException
	{
		super(objClass, registerAsDefault);

		if (null == (_colClass=colClass))
			throw new IllegalArgumentException("No column type class specified");
	}

	protected EnumTableColumnReflectiveProxy (final Class<E> colClass, final Class<C> objClass) throws IllegalArgumentException
	{
		this(colClass, objClass, false);
	}

	public static final String	IDENTIFIER_ATTR="identifier";
	/*
	 * @see net.community.chest.awt.dom.UIReflectiveAttributesProxy#resolveAttributeInstantiator(java.lang.String, java.lang.Class)
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	protected <A> ValueStringInstantiator<A> resolveAttributeInstantiator (String name, Class<A> type) throws Exception
	{
		if (IDENTIFIER_ATTR.equalsIgnoreCase(name))
			return (ValueStringInstantiator) super.resolveAttributeInstantiator(name, getColumnTypeClass());

		return super.resolveAttributeInstantiator(name, type);
	}
	/*
	 * @see net.community.chest.dom.transform.ReflectiveAttributesProxy#handleUnknownAttribute(java.lang.Object, java.lang.String, java.lang.String, java.util.Map)
	 */
	@Override
	protected C handleUnknownAttribute (C src, String name, String value, Map<String, ? extends Method> accsMap) throws Exception
	{
		if (CLASS_ATTR.equalsIgnoreCase(name))
		{
			src.setColumnValueClass(value);
			return src;
		}

		return super.handleUnknownAttribute(src, name, value, accsMap);
	}
}
