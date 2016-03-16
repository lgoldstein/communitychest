/*
 *
 */
package net.community.chest.swing.component.spinner;

import java.lang.reflect.Method;

import javax.swing.SpinnerNumberModel;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @param <M> The reflected {@link SpinnerNumberModel}
 * @author Lyor G.
 * @since Oct 15, 2008 3:29:29 PM
 */
public class SpinnerNumberModelReflectiveProxy<M extends SpinnerNumberModel> extends SpinnerModelReflectiveProxy<M> {
    public SpinnerNumberModelReflectiveProxy (Class<M> objClass) throws IllegalArgumentException
    {
        this(objClass, false);
    }

    protected SpinnerNumberModelReflectiveProxy (Class<M> objClass, boolean registerAsDefault)
        throws IllegalArgumentException, IllegalStateException
    {
        super(objClass, registerAsDefault);
    }
    /**
     * Default type string used to indicate a {@link SpinnerNumberModel} instance
     */
    public static final String    NUMBER_TYPE=Number.class.getSimpleName().toLowerCase();

    protected M updateNumberValue (M src, String name, String value, Method setter) throws Exception
    {
        if ((null == name) || (name.length() <= 0))
            throw new IllegalArgumentException("updateNumberValue(" + value + ")[" + setter + "] no attribute specified");

        final Number    n=((null == value) || (value.length() <= 0)) ? null : Long.decode(value);
        if (n != null)
            setter.invoke(src, n);
        return src;
    }
    // some attributes of interest
    public static final String    MIN_ATTR="minimum",
                                MAX_ATTR="maximum",
                                STEP_ATTR="stepSize",
                                VAL_ATTR="value";
    /*
     * @see net.community.chest.dom.transform.ReflectiveAttributesProxy#updateObjectAttribute(java.lang.Object, java.lang.String, java.lang.String, java.lang.reflect.Method)
     */
    @Override
    protected M updateObjectAttribute (M src, String name, String value, Method setter) throws Exception
    {
        if (MIN_ATTR.equalsIgnoreCase(name)
         || MAX_ATTR.equalsIgnoreCase(name)
         || STEP_ATTR.equalsIgnoreCase(name)
         || VAL_ATTR.equalsIgnoreCase(name))
            return updateNumberValue(src, name, value, setter);

        return super.updateObjectAttribute(src, name, value, setter);
    }

    public static final SpinnerNumberModelReflectiveProxy<SpinnerNumberModel>    NUMMODEL=
        new SpinnerNumberModelReflectiveProxy<SpinnerNumberModel>(SpinnerNumberModel.class, true);
}
