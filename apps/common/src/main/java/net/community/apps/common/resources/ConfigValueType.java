/*
 *
 */
package net.community.apps.common.resources;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.sql.Blob;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

import javax.naming.Referenceable;

import net.community.chest.convert.ClassValueStringInstantiator;
import net.community.chest.convert.DoubleValueStringConstructor;
import net.community.chest.convert.FloatValueStringConstructor;
import net.community.chest.convert.ValueStringInstantiator;
import net.community.chest.dom.ElementStringInstantiator;
import net.community.chest.io.FileStringInstantiator;
import net.community.chest.reflect.NumberValueStringConstructor;
import net.community.chest.reflect.ValueStringConstructor;
import net.community.chest.text.DecimalFormatValueStringInstantiator;
import net.community.chest.text.SimpleDateFormatValueStringInstantiator;
import net.community.chest.util.collection.CollectionsUtils;
import net.community.chest.util.datetime.Duration;
import net.community.chest.util.datetime.DurationValueStringInstantiator;
import net.community.chest.util.locale.LocaleValueInstantiator;

import org.w3c.dom.Document;
import org.w3c.dom.Element;


/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * <P>Useful types of values that can be stored in an application's
 * configuration table(s)</P>
 *
 * @author Lyor G.
 * @since Feb 18, 2009 12:30:15 PM
 */
public enum ConfigValueType {
    // these types are assumed to contain a reference to their actual data
    DOCUMENT(Document.class, null),
    BLOB(Blob.class, null),
    PROPERTIES(Properties.class, null),
    BYTEBUFFER(ByteBuffer.class, null),
    CHARBUFFER(CharBuffer.class, null),

    LOCALE(Locale.class, LocaleValueInstantiator.DEFAULT),
    /**
     * Special type that points to another location
     */
    REFERENCEABLE(Referenceable.class, null),
    BOOLEAN(Boolean.class, ValueStringConstructor.BOOLEAN),
    INTEGER(Integer.class, NumberValueStringConstructor.INTEGER),
    LONG(Long.class, NumberValueStringConstructor.LONG),
    FLOAT(Float.class, FloatValueStringConstructor.DEFAULT),
    DOUBLE(Double.class, DoubleValueStringConstructor.DEFAULT),
    STRING(String.class, ValueStringConstructor.STRING),
    /**
     * Default assumed to be a "simple" {@link Element} - i.e., no children
     */
    ELEMENT(Element.class, ElementStringInstantiator.DEFAULT),
    FILE(File.class, FileStringInstantiator.DEFAULT),
    CLASS(Class.class, ClassValueStringInstantiator.DEFAULT),
    TIMESTAMP(Timestamp.class, null),    // a formatted date/time
    DATEFORMAT(DateFormat.class, SimpleDateFormatValueStringInstantiator.DEFAULT),
    NUMBERFORMAT(NumberFormat.class, DecimalFormatValueStringInstantiator.DEFAULT),
    DURATION(Duration.class, DurationValueStringInstantiator.DEFAULT);

    private final Class<?>    _valuesClass;
    /**
     * @return The {@link Class} of object the value will be cast to once read
     * from the configuration database
     */
    public final Class<?> getValuesClass ()
    {
        return _valuesClass;
    }

    private final ValueStringInstantiator<?>    _vsi;
    public final ValueStringInstantiator<?> getDefaultInstantiator ()
    {
        return _vsi;
    }

    ConfigValueType (final Class<?> valuesClass, final ValueStringInstantiator<?> vsi)
    {
        _valuesClass = valuesClass;
        _vsi = vsi;
    }

    public static final List<ConfigValueType>    VALUES=Collections.unmodifiableList(Arrays.asList(values()));
    public static final ConfigValueType fromString (final String s)
    {
        return CollectionsUtils.fromString(VALUES, s, false);
    }

    public static final ConfigValueType fromValueClass (final Class<?> c)
    {
        if (null == c)
            return null;

        for (final ConfigValueType v : VALUES)
        {
            final Class<?>    vc=(null == v) ? null : v.getValuesClass();
            if ((vc != null) && vc.isAssignableFrom(c))
                return v;
        }

        return null;
    }

    public static final ConfigValueType fromValueObject (final Object o)
    {
        return (null == o) ? null : fromValueClass(o.getClass());
    }
}
