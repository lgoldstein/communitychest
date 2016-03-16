/*
 *
 */
package net.community.chest.awt.border;

import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;

import net.community.chest.reflect.FieldsAccessor;

/**
 * <P>Copyright as per GPLv2</P>
 *
 * @param <B> Type of {@link CompoundBorder} being reflected
 * @author Lyor G.
 * @since Jul 1, 2009 9:08:26 AM
 */
public class CompoundBorderFieldsAccessor<B extends CompoundBorder> extends FieldsAccessor<B> {
    /**
     *
     */
    private static final long serialVersionUID = -1147515701646631893L;
    public CompoundBorderFieldsAccessor (Class<B> valsClass)
    {
        super(valsClass, CompoundBorder.class);
    }

    protected void setBorder (B cb, Border b, String name) throws Exception
    {
        setFieldValue(cb, name, b);
    }

    public static final String    INSIDE_BORDER_FLDNAME="insideBorder";
    public void setInsideBorder (B cb, Border b) throws Exception
    {
        setBorder(cb, b, INSIDE_BORDER_FLDNAME);
    }

    public static final String    OUTSIDE_BORDER_FLDNAME="outsideBorder";
    public void setOutsideBorder (B cb, Border b) throws Exception
    {
        setBorder(cb, b, OUTSIDE_BORDER_FLDNAME);
    }

    public static final CompoundBorderFieldsAccessor<CompoundBorder>    DEFAULT=
        new CompoundBorderFieldsAccessor<CompoundBorder>(CompoundBorder.class);
}
