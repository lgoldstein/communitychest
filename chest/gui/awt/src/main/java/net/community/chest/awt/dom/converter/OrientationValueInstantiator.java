package net.community.chest.awt.dom.converter;

import java.awt.ComponentOrientation;
import java.util.NoSuchElementException;

import net.community.chest.awt.Orientations;
import net.community.chest.dom.AbstractXmlValueStringInstantiator;
import net.community.chest.lang.StringUtil;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Mar 20, 2008 8:27:22 AM
 */
public class OrientationValueInstantiator extends AbstractXmlValueStringInstantiator<ComponentOrientation> {

    public OrientationValueInstantiator ()
    {
        super(ComponentOrientation.class);
    }
    /*
     * @see net.community.chest.convert.ValueStringInstantiator#convertInstance(java.lang.Object)
     */
    @Override
    public String convertInstance (final ComponentOrientation inst) throws Exception
    {
        if (null == inst)
            return null;

        final Orientations    o=Orientations.fromComponentOrientation(inst);
        if (null == o)
            throw new NoSuchElementException(getArgumentsExceptionLocation("convertInstance", inst) + " unknown " + ComponentOrientation.class.getSimpleName() + " value");

        return o.toString();
    }
    /*
     * @see net.community.chest.convert.ValueStringInstantiator#newInstance(java.lang.String)
     */
    @Override
    public ComponentOrientation newInstance (final String v) throws Exception
    {
        final String    s=StringUtil.getCleanStringValue(v);
        if ((null == s) || (s.length() <= 0))
            return null;

        final Orientations    o=Orientations.fromString(s.replace('-', '_'));
        if (null == o)
            throw new NoSuchElementException(getArgumentsExceptionLocation("newInstance", s) + " unknown " + ComponentOrientation.class.getSimpleName() + " value");

        return o.getOrientation();
    }

    public static final OrientationValueInstantiator    DEFAULT=new OrientationValueInstantiator();
}
