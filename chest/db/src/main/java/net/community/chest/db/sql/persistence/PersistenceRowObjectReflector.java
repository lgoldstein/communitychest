package net.community.chest.db.sql.persistence;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

import javax.persistence.Column;
import javax.persistence.Transient;

import net.community.chest.db.sql.RowObjectReflector;
import net.community.chest.lang.StringUtil;
import net.community.chest.reflect.AttributeAccessor;
import net.community.chest.reflect.AttributeMethodType;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * <P>Attempts to use the {@link Column} annotation to locate columns
 * for which the getter/setter does not have the same name as the column</P>
 * 
 * @param <V> The expected row object class
 * @author Lyor G.
 * @since Apr 30, 2008 3:30:17 PM
 */
public class PersistenceRowObjectReflector<V> extends RowObjectReflector<V> {
	public PersistenceRowObjectReflector (Class<V> objClass) throws IllegalArgumentException
	{
		super(objClass);
	}
	/*
	 * @see net.community.chest.db.sql.RowObjectReflector#getColumnSetter(java.lang.String)
	 */
	@Override
	public Method getColumnSetter (final String colName) throws Exception
	{
		final Method	m=super.getColumnSetter(colName);
		if (m != null)	// if found using column name, then OK
			return m;

		final Map<String,? extends AttributeAccessor>	attrsMap=
			((null == colName) || (colName.length() <= 0)) ? null : getObjectAttributes();
		final Collection<? extends AttributeAccessor>	accList=
			((null == attrsMap) || (attrsMap.size() <= 0)) ? null : attrsMap.values();
		if ((null == accList) || (accList.size() <= 0))
			return null;

		for (final AttributeAccessor aa : accList)
		{
			if (null == aa)	// should not happen
				continue;

			final Method	gm=aa.getGetter(), sm=aa.getSetter();
			Column			ac=(null == gm) ? null : gm.getAnnotation(Column.class);
			if (null == ac)	// getter is usually annotated - but if not, then try the setter
				ac = (null == sm) ? null : sm.getAnnotation(Column.class);
			if (null == ac)	// OK if no such annotation
				continue;

			final String	acName=ac.name();
			if (0 == StringUtil.compareDataStrings(acName, colName, false))
				return sm;
		}

		return null;	// no match found
	}
	// removes @Transient attributes and renames @Column ones that have a 'name' value
	public static final Map<String,AttributeAccessor> adjustAttributeAccessorsMap (
			final Collection<? extends Map.Entry<String,? extends AttributeAccessor>>	al,
			final boolean																errIfDuplicate)
		throws IllegalStateException
	{
		if ((null == al) || (al.size() <= 0))
			return null;

		Map<String,AttributeAccessor>	ret=null;
		for (final Map.Entry<String,? extends AttributeAccessor> ae : al)
		{
			final AttributeAccessor	aa=(null == ae) ? null : ae.getValue();
			if (null == aa)
				continue;
			if (aa.findClosestAnnotation(Transient.class) != null)
				continue;

			final Map.Entry<? extends Column,? extends Method>	ca=
				aa.findClosestAnnotation(Column.class);
			final Column										cv=
				(null == ca) ? null : ca.getKey();
			final String										kv,
																cn=
				(null == cv) ? null : cv.name();
			if ((null == cn) || (cn.length() <= 0))
				kv =  ae.getKey();
			else	// debug breakpoint
				kv = cn;

			if (null == ret)
				ret = new TreeMap<String,AttributeAccessor>(String.CASE_INSENSITIVE_ORDER);

			final AttributeAccessor	pa=ret.put(kv, aa);
			if ((pa != null) && errIfDuplicate)
				throw new IllegalStateException("adjustAttributeAccessorsMap(" + kv + ") multiple accessors: prev=" + pa + "/new=" + aa);
		}

		return ret;
	}

	public static final Map<String,AttributeAccessor> adjustAttributeAccessorsMap (
			final Map<String,? extends AttributeAccessor>	aMap,
			final boolean									errIfDuplicate)
		throws IllegalStateException
	{
		return ((null == aMap) || (aMap.size() <= 0)) ? null : adjustAttributeAccessorsMap(aMap.entrySet(), errIfDuplicate);
	}

	public static final Map<String,AttributeAccessor> getAdjustedAttributeAccessorsMap (
			final Class<?> c, final boolean errIfDuplicate)
		throws IllegalStateException
	{
		return adjustAttributeAccessorsMap(AttributeMethodType.getAllAttributes(c), errIfDuplicate);
	}

	public static final Map<String,AttributeAccessor> getAdjustedAttributeAccessorsMap (
			final Object o, final boolean errIfDuplicate)
		throws IllegalStateException
	{
		return (null == o) ? null : getAdjustedAttributeAccessorsMap(o.getClass(), errIfDuplicate);
	}
}
