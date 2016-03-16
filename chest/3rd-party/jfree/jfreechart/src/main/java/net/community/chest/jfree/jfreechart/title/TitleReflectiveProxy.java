/*
 *
 */
package net.community.chest.jfree.jfreechart.title;

import java.util.Map;

import net.community.chest.jfree.jcommon.ui.HAlignment;
import net.community.chest.jfree.jcommon.ui.VAlignment;
import net.community.chest.jfree.jfreechart.block.AbstractBlockReflectiveProxy;

import org.jfree.chart.title.Title;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @param <T> The reflected {@link Title} type
 * @author Lyor G.
 * @since Jan 27, 2009 3:27:18 PM
 */
public class TitleReflectiveProxy<T extends Title> extends AbstractBlockReflectiveProxy<T> {
    protected TitleReflectiveProxy (Class<T> objClass, boolean registerAsDefault)
            throws IllegalArgumentException, IllegalStateException
    {
        super(objClass, registerAsDefault);
    }

    public TitleReflectiveProxy (Class<T> objClass) throws IllegalArgumentException
    {
        this(objClass, false);
    }

    public static final String    POSITION_ATTR="position";
    /*
     * @see net.community.chest.dom.proxy.AbstractXmlProxyConverter#initializeAliasesMap(java.util.Map)
     */
    @Override
    protected Map<String,String> initializeAliasesMap (Map<String,String> org)
    {
        return addAttributeAliases(super.initializeAliasesMap(org),
                HAlignment.HALIGN_ALIAS, HAlignment.HALIGN_ATTR,
                VAlignment.VALIGN_ALIAS, VAlignment.VALIGN_ATTR
            );
    }
}
