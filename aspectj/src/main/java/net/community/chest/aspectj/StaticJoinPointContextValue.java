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
 * @since Mar 31, 2011 10:54:12 AM
 */
public enum StaticJoinPointContextValue {
	STRING(String.class) {
			/*
			 * @see net.community.chest.aspectj.JoinPointContextValue#getValue(org.aspectj.lang.JoinPoint)
			 */
			@Override
			@CoVariantReturn
			public String getValue (JoinPoint.StaticPart jp)
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
			public String getValue (JoinPoint.StaticPart jp)
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
			public String getValue (JoinPoint.StaticPart jp)
			{
				return (jp == null) ? null : jp.toLongString();
			}
		},
		SIGNATURE(Signature.class) {
			/*
			 * @see net.community.chest.aspectj.JoinPointContextValue#getValue(org.aspectj.lang.JoinPoint)
			 */
			@Override
			@CoVariantReturn
			public Signature getValue (JoinPoint.StaticPart jp)
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
			public SourceLocation getValue (JoinPoint.StaticPart jp)
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
			public String getValue (JoinPoint.StaticPart jp)
			{
				return (jp == null) ? null : jp.getKind();
			}
		},
	ID(Integer.class) {
			/*
			 * @see net.community.chest.aspectj.JoinPointContextValue#getValue(org.aspectj.lang.JoinPoint)
			 */
			@Override
			@CoVariantReturn
			public Integer getValue (JoinPoint.StaticPart jp)
			{
				return (jp == null) ? null : Integer.valueOf(jp.getId());
			}
		};

	public abstract Object getValue (final JoinPoint.StaticPart jp);

	private final Class<?>	_valType;
	public final Class<?> getValueType ()
	{
		return _valType;
	}

	StaticJoinPointContextValue (Class<?> valType)
	{
		_valType = valType;
	}

	public static final List<StaticJoinPointContextValue>	VALUES=Collections.unmodifiableList(Arrays.asList(values()));
	public static final StaticJoinPointContextValue fromString (final String s)
	{
		return CollectionsUtils.fromString(VALUES, s, false);
	}
	// NOTE: skips null(s)
	public static final EnumMap<StaticJoinPointContextValue,Object> getContext (final JoinPoint.StaticPart jp)
	{
		if (jp == null)
			return null;

		final EnumMap<StaticJoinPointContextValue,Object>	ctxMap=
			new EnumMap<StaticJoinPointContextValue,Object>(StaticJoinPointContextValue.class);
		for (final StaticJoinPointContextValue v : VALUES)
		{
			final Object	vv=(v == null) ? null : v.getValue(jp);
			if (vv == null)
				continue;

			ctxMap.put(v, vv);
		}

		return ctxMap;
	}

	public static final EnumMap<StaticJoinPointContextValue,Object> getContext (final JoinPoint jp)
	{
		return (jp == null) ? null : getContext(jp.getStaticPart());
	}
}
