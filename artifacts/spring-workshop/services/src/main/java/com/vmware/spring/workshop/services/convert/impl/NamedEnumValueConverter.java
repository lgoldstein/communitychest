package com.vmware.spring.workshop.services.convert.impl;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.NoSuchElementException;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * Assumes both model and DTO are {@link Enum}-s having the same name(s)
 * @author lgoldstein
 */
public class NamedEnumValueConverter<SRC extends Enum<SRC>,DST extends Enum<DST>> implements ValueConverter<SRC,DST> {
    private final Map<SRC,DST>    _src2dstValuesMap;
    public NamedEnumValueConverter(Class<SRC> srcClass, Class<DST> dstClass) {

        _src2dstValuesMap = Collections.unmodifiableMap(toValuesMap(srcClass, dstClass));
    }

    @Override
    public DST convertValue(final SRC srcValue) {
        if (srcValue == null)
            return null;
        else
            return _src2dstValuesMap.get(srcValue);
    }

    public static final <SRC extends Enum<SRC>,DST extends Enum<DST>> Map<SRC,DST> toValuesMap (
            final Class<SRC> srcClass, final Class<DST> dstClass)
    {
        final SRC[]            srcValues=srcClass.getEnumConstants();
        final DST[]            dstValues=dstClass.getEnumConstants();
        final Map<SRC,DST>    valsMap=new EnumMap<SRC,DST>(srcClass);
        for (final SRC src : srcValues)
        {
            final String    name=src.name();
            final DST        dst=findByName(name, dstValues);
            if (dst == null)
                throw new NoSuchElementException("No " + dstClass.getSimpleName() + " matching value found"
                                               + " for " + srcClass.getSimpleName() + "[" + name + "]");

            final DST    prev=valsMap.put(src, dst);
            if (prev != null)
                throw new IllegalStateException("Multiple mappings for " + src + ": " + dst + "/" + prev);
        }

        return valsMap;
    }

    public static final <E extends Enum<E>> E findByName (final String name, final E ... values)
    {
        if (StringUtils.isBlank(name))
            return null;
        if (ArrayUtils.isEmpty(values))
            return null;

        for (final E v : values)
        {
            if (name.equals(v.name()))
                return v;
        }

        return  null;
    }

}
