/*
 *
 */
package net.community.chest.jfree.jfreechart.axis.value;

import net.community.chest.convert.DoubleValueStringConstructor;

import org.jfree.chart.axis.CyclicNumberAxis;
import org.w3c.dom.Element;

/**
 * <P>Copyright GPLv2</P>
 *
 * @param <A> The type of {@link CyclicNumberAxis} being reflected
 * @author Lyor G.
 * @since May 25, 2009 10:10:33 AM
 */
public class CyclicNumberAxisReflectiveProxy<A extends CyclicNumberAxis> extends NumberAxisReflectiveProxy<A> {
    protected CyclicNumberAxisReflectiveProxy (Class<A> objClass, boolean registerAsDefault)
        throws IllegalArgumentException, IllegalStateException
    {
        super(objClass, registerAsDefault);
    }

    public CyclicNumberAxisReflectiveProxy (Class<A> objClass) throws IllegalArgumentException
    {
        this(objClass, false);
    }

    public static final String    PERIOD_ATTR="period";
    public static final CyclicNumberAxisReflectiveProxy<CyclicNumberAxis>    CYCLIC=
        new CyclicNumberAxisReflectiveProxy<CyclicNumberAxis>(CyclicNumberAxis.class, true) {
            /*
             * @see net.community.chest.dom.proxy.AbstractReflectiveProxy#createInstance(org.w3c.dom.Element)
             */
            @Override
            public CyclicNumberAxis createInstance (Element elem) throws Exception
            {
                final String    n=elem.getAttribute(NAME_ATTR),
                                v=elem.getAttribute(PERIOD_ATTR);
                final Double    p=DoubleValueStringConstructor.DEFAULT.newInstance(v);
                final double    pv=(null == p) ? 0.0d : p.doubleValue();
                if ((null == p) || Double.isInfinite(pv) || Double.isNaN(pv))
                    throw new IllegalStateException("Bad/Missing '" + PERIOD_ATTR + "' value: " + v);

                return new CyclicNumberAxis(pv, n);
            }
        };
}
