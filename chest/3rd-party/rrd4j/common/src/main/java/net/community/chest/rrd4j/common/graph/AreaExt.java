package net.community.chest.rrd4j.common.graph;

import java.awt.Paint;

import net.community.chest.rrd4j.common.graph.helpers.AbstractLegendColorSource;

import org.w3c.dom.Element;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Jan 16, 2008 2:39:55 PM
 */
public class AreaExt extends AbstractLegendColorSource {
    public AreaExt (String legend, Paint color, String srcName)
    {
        super(legend, color, srcName);
    }

    public AreaExt ()
    {
        super();
    }

    public AreaExt (String legend, Paint color)
    {
        this(legend, color, null);
    }

    public AreaExt (String legend)
    {
        this(legend, null);
    }

    public AreaExt (Element elem) throws Exception
    {
        super(elem);
    }

    /*
     * @see net.community.chest.rrd4j.common.graph.helpers.AbstractLegend#getRootElementName()
     */
    @Override
    public String getRootElementName ()
    {
        return RrdGraphDefExt.AREA_ATTR;
    }
}
