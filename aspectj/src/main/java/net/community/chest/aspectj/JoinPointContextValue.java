/*
 * 
 */
package net.community.chest.aspectj;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;

import net.community.chest.CoVariantReturn;
import net.community.chest.util.collection.CollectionsUtils;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.reflect.SourceLocation;

/**
 * <P>Copyright as per GPLv2</P>
 * @author Lyor G.
 * @since Mar 31, 2011 10:42:06 AM
 */
public enum JoinPointContextValue {
	STRING(String.class) {
			/*
			 * @see net.community.chest.aspectj.JoinPointContextValue#getValue(org.aspectj.lang.JoinPoint)
			 */
			@Override
			@CoVariantReturn
			public String getValue (JoinPoint jp)
			{
				return (jp == null) ? null : jp.toString();
			}
		},
	SHORTSTRING(String.class) {
			/*
			 * @see net.community.chest.aspectj.JoinPointContextValue#getValue(org.aspectj.lang.JoinPoint)
			 */
			@Override
			@CoVariantReturn
			public String getValue (JoinPoint jp)
			{
				return (jp == null) ? null : jp.toShortString();
			}
		},
	LONGSTRING(String.class) {
			/*
			 * @see net.community.chest.aspectj.JoinPointContextValue#getValue(org.aspectj.lang.JoinPoint)
			 */
			@Override
			@CoVariantReturn
			public String getValue (JoinPoint jp)
			{
				return (jp == null) ? null : jp.toLongString();
			}
		},
	THIS(Object.class) {
			/*
			 * @see net.community.chest.aspectj.JoinPointContextValue#getValue(org.aspectj.lang.JoinPoint)
			 */
			@Override
			public Object getValue (JoinPoint jp)
			{
				return (jp == null) ? null : jp.getThis();
			}
		},
	TARGET(Object.class) {
			/*
			 * @see net.community.chest.aspectj.JoinPointContextValue#getValue(org.aspectj.lang.JoinPoint)
			 */
			@Override
			public Object getValue (JoinPoint jp)
			{
				return (jp == null) ? null : jp.getTarget();
			}
		},
	ARGS(Object[].class) {
			/*
			 * @see net.community.chest.aspectj.JoinPointContextValue#getValue(org.aspectj.lang.JoinPoint)
			 */
			@Override
			@CoVariantReturn
			public Object[] getValue (JoinPoint jp)
			{
				return (jp == null) ? null : jp.getArgs();
			}
		},
	SIGNATURE(Signature.class) {
			/*
			 * @see net.community.chest.aspectj.JoinPointContextValue#getValue(org.aspectj.lang.JoinPoint)
			 */
			@Override
			@CoVariantReturn
			public Signature getValue (JoinPoint jp)
			{
				return (jp == null) ? null : jp.getSignature();
			}
		},
	SOURCELOCATION(SourceLocation.class) {
			/*
			 * @see net.community.chest.aspectj.JoinPointContextValue#getValue(org.aspectj.lang.JoinPoint)
			 */
			@Override
			@CoVariantReturn
			public SourceLocation getValue (JoinPoint jp)
			{
				return (jp == null) ? null : jp.getSourceLocation();
			}
		},
	KIND(String.class) {
			/*
			 * @see net.community.chest.aspectj.JoinPointContextValue#getValue(org.aspectj.lang.JoinPoint)
			 */
			@Override
			@CoVariantReturn
			public String getValue (JoinPoint jp)
			{
				return (jp == null) ? null : jp.getKind();
			}
		},
	STATICPART(JoinPoint.StaticPart.class) {
			/*
			 * @see net.community.chest.aspectj.JoinPointContextValue#getValue(org.aspectj.lang.JoinPoint)
			 */
			@Override
			@CoVariantReturn
			public JoinPoint.StaticPart getValue (JoinPoint jp)
			{
				return (jp == null) ? null : jp.getStaticPart();
			}
		};

	public abstract Object getValue (final JoinPoint jp);

	private final Class<?>	_valType;
	public final Class<?> getValueType ()
	{
		return _valType;
	}

	JoinPointContextValue (Class<?> valType)
	{
		_valType = valType;
	}

	public static final List<JoinPointContextValue>	VALUES=Collections.unmodifiableList(Arrays.asList(values()));
	public static final JoinPointContextValue fromString (final String s)
	{
		return CollectionsUtils.fromString(VALUES, s, false);
	}
	// NOTE: skips null(s)
	public static final EnumMap<JoinPointContextValue,Object> getContext (final JoinPoint jp)
	{
		if (jp == null)
			return null;

		final EnumMap<JoinPointContextValue,Object>	ctxMap=
			new EnumMap<JoinPointContextValue,Object>(JoinPointContextValue.class);
		for (final JoinPointContextValue v : VALUES)
		{
			final Object	vv=(v == null) ? null : v.getValue(jp);
			if (vv == null)
				continue;

			ctxMap.put(v, vv);
		}

		return ctxMap;
	}
}
