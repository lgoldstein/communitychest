/*
 *
 */
package net.community.chest.swing.component.spinner;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.swing.JSpinner;

import net.community.chest.CoVariantReturn;
import net.community.chest.util.collection.CollectionsUtils;

/**
 * <P>Copyright 2010 as per GPLv2</P>
 *
 * <P>Encapsulates the various available {@link javax.swing.JSpinner.DefaultEditor} types
 * as an {@link Enum}</P>
 *
 * @author Lyor G.
 * @since May 12, 2010 8:49:32 AM
 */
public enum DefaultEditorType {
    NUMBER(JSpinner.NumberEditor.class, Number.class) {
            /*
             * @see net.community.chest.swing.component.spinner.DefaultEditorType#createInstance(javax.swing.JSpinner, java.lang.String)
             */
            @Override
            @CoVariantReturn
            public JSpinner.NumberEditor createInstance (JSpinner s, String argVal)
            {
                if ((null == argVal) || (argVal.length() <= 0))
                    return new JSpinner.NumberEditor(s);
                else
                    return new JSpinner.NumberEditor(s, argVal);
            }
        },
    DATE(JSpinner.DateEditor.class, Date.class) {
            /*
             * @see net.community.chest.swing.component.spinner.DefaultEditorType#createInstance(javax.swing.JSpinner, java.lang.String)
             */
            @Override
            @CoVariantReturn
            public JSpinner.DateEditor createInstance (JSpinner s, String argVal)
            {
                if ((null == argVal) || (argVal.length() <= 0))
                    return new JSpinner.DateEditor(s);
                else
                    return new JSpinner.DateEditor(s, argVal);
            }
        },
    LIST(JSpinner.ListEditor.class, Object.class) {
            /*
             * @see net.community.chest.swing.component.spinner.DefaultEditorType#createInstance(javax.swing.JSpinner, java.lang.String)
             */
            @Override
            @CoVariantReturn
            public JSpinner.ListEditor createInstance (JSpinner s, String argVal)
            {
                if ((null == argVal) || (argVal.length() <= 0))
                    return new JSpinner.ListEditor(s);

                throw new IllegalArgumentException("No argument expected for " + JSpinner.ListEditor.class.getSimpleName());
            }
        };

    private final Class<? extends JSpinner.DefaultEditor>    _editorClass;
    /**
     * @return Type of {@link javax.swing.JSpinner.DefaultEditor} being generated by
     * the {@link #createInstance(JSpinner, String)} call
     */
    public final Class<? extends JSpinner.DefaultEditor> getEditorClass ()
    {
        return _editorClass;
    }

    private final Class<?>    _valueClass;
    /**
     * @return Type of value being edited
     */
    public final Class<?> getValuesClass ()
    {
        return _valueClass;
    }

    public abstract JSpinner.DefaultEditor createInstance (JSpinner s, String argVal);

    DefaultEditorType (Class<? extends JSpinner.DefaultEditor> eClass, Class<?> vClass)
    {
        _editorClass = eClass;
        _valueClass = vClass;
    }

    public static final List<DefaultEditorType>    VALUES=Collections.unmodifiableList(Arrays.asList(values()));
    public static final DefaultEditorType fromString (final String v)
    {
        return CollectionsUtils.fromString(VALUES, v, false);
    }

    public static final DefaultEditorType fromEditorClass (final Class<?> c)
    {
        if ((null == c) || (!JSpinner.DefaultEditor.class.isAssignableFrom(c)))
            return null;

        for (final DefaultEditorType v : VALUES)
        {
            final Class<?>    ec=(null == v) ? null : v.getEditorClass();
            if ((null == ec) || (!ec.isAssignableFrom(c)))
                continue;

            return v;
        }

        return null;
    }

    public static final DefaultEditorType fromValueClass (final Class<?> c)
    {
        if (null == c)
            return null;

        for (final DefaultEditorType v : VALUES)
        {
            final Class<?>    vc=(null == v) ? null : v.getValuesClass();
            if ((null == vc) || (!vc.isAssignableFrom(c)))
                continue;

            return v;
        }

        return null;
    }

    public static final JSpinner.DefaultEditor fromString (JSpinner s, String v)
        throws IllegalArgumentException
    {
        final int    vLen=(null == v) ? 0 : v.length();
        if (vLen <= 0)
            return null;

        final int    sPos=v.indexOf('('), ePos=v.lastIndexOf(')');
        if ((sPos <= 0) || (ePos <= sPos) || (ePos > vLen))
            throw new IllegalArgumentException("fromString(" + v + ") missing parantheses");

        final String    typeVal=v.substring(0, sPos).trim(),
                        argVal=v.substring(sPos+1, ePos);
        if ((null == typeVal) || (typeVal.length() <= 0))
            throw new IllegalArgumentException("fromString(" + v + ") no type specified");

        final DefaultEditorType    t=fromString(typeVal);
        if (null == t)
            throw new IllegalArgumentException("fromString(" + v + ") no unknown type: " + typeVal);

        if (null == s)
            throw new IllegalArgumentException("fromString(" + v + ") no spinner instance provided");

        return t.createInstance(s, argVal);
    }
}
