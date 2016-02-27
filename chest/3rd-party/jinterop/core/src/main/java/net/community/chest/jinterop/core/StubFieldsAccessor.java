/*
 * 
 */
package net.community.chest.jinterop.core;

import net.community.chest.reflect.FieldsAccessor;
import rpc.Stub;

/**
 * <P>Copyright GPLv2</P>
 *
 * @param <S> Type of {@link Stub} being accessed
 * @author Lyor G.
 * @since May 19, 2009 11:08:58 AM
 */
public class StubFieldsAccessor<S extends Stub> extends FieldsAccessor<S> {
	protected StubFieldsAccessor (Class<S> valsClass)
			throws IllegalArgumentException
	{
		super(valsClass, Stub.class);
	}
}
