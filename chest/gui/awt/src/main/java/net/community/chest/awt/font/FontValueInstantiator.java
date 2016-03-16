package net.community.chest.awt.font;

import java.awt.Font;

import net.community.chest.dom.AbstractXmlValueStringInstantiator;
import net.community.chest.lang.StringUtil;

import org.w3c.dom.Element;

/**
 * <P>Copyright 2007 as per GPLv2</P>
 *
 * <P>Provides various conversions between {@link String}-s, XML
 * {@link org.w3c.dom.Element}-s and {@link Font}-s</P>
 *
 * @author Lyor G.
 * @since Jul 29, 2007 3:56:30 PM
 */
public class FontValueInstantiator extends AbstractXmlValueStringInstantiator<Font> {
    public FontValueInstantiator ()
    {
        super(Font.class);
    }

    public static final String toString (final Font inst)
    {
        if (null == inst)
            return null;
        final String    name=inst.getName(),
                        style=FontUtils.convertFontStyle(inst),
                        size=String.valueOf(inst.getSize());

        return name + "-" + style + "-" + size;
    }
    /*
     * @see net.community.chest.reflect.ValueStringInstantiator#convertInstance(java.lang.Object)
     */
    @Override
    public String convertInstance (final Font inst) throws Exception
    {
        return toString(inst);
    }

    public static final Font fromString (final String v)
    {
        final String    s=StringUtil.getCleanStringValue(v);
        if ((null == s) || (s.length() <= 0))
            return null;

        return Font.decode(s);
    }
    /*
     * @see net.community.chest.reflect.ValueStringInstantiator#newInstance(java.lang.String)
     */
    @Override
    public Font newInstance (final String s) throws Exception
    {
        return fromString(s);
    }
    /*
     * @see net.community.chest.dom.AbstractXmlValueStringInstantiator#fromXml(org.w3c.dom.Element)
     */
    @Override
    public Font fromXml (Element elem) throws Exception
    {
        return FontUtils.fromXml(elem);
    }

    public static final FontValueInstantiator DEFAULT=new FontValueInstantiator();
}
