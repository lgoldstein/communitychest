package net.community.chest.rrd4j.common.core;

import net.community.chest.rrd4j.common.ConsolFunExt;
import net.community.chest.rrd4j.common.RrdUtils;

import org.rrd4j.ConsolFun;
import org.rrd4j.core.ArcDef;
import org.w3c.dom.Element;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Jan 8, 2008 2:08:43 PM
 */
public class ArcDefExt extends ArcDef {
    public ArcDefExt (ConsolFun consolFun, double xff, int steps, int rows)
    {
        super(consolFun, xff, steps, rows);
    }

    public static final String    ARCDEF_ELEM_NAME=ArcDef.class.getSimpleName(),
                                    XFF_ATTR="xff",
                                    STEPS_ATTR="steps",
                                    ROWS_ATTR="rows";
    public static final double getXff (Element elem) throws Exception
    {
        return RrdUtils.getDouble(elem, XFF_ATTR);
    }

    public static final int getSteps (Element elem) throws Exception
    {
        return RrdUtils.getInteger(elem, STEPS_ATTR);
    }

    public static final int getRows (Element elem) throws Exception
    {
        return RrdUtils.getInteger(elem, ROWS_ATTR);
    }

    public ArcDefExt (Element elem) throws Exception
    {
        this(ConsolFunExt.DEFAULT.fromXml(elem), getXff(elem), getSteps(elem), getRows(elem));
    }
    /*
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString ()
    {
        return dump();
    }
}
