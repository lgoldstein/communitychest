package net.community.chest.awt.dom.converter;

import java.util.NoSuchElementException;

import javax.swing.KeyStroke;

import net.community.chest.dom.AbstractXmlValueStringInstantiator;
import net.community.chest.lang.StringUtil;

/**
 * <P>Copyright 2007 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Jul 25, 2007 3:36:33 PM
 */
public class KeyStrokeValueInstantiator extends AbstractXmlValueStringInstantiator<KeyStroke> {
    public KeyStrokeValueInstantiator ()
    {
        super(KeyStroke.class);
    }
    /*
     * @see net.community.chest.reflect.ValueStringInstantiator#convertInstance(java.lang.Object)
     */
    @Override
    public String convertInstance (KeyStroke inst) throws Exception
    {
        return (null == inst) ? null : inst.toString();
    }
    /*
     * @see net.community.chest.reflect.ValueStringInstantiator#newInstance(java.lang.String)
     */
    @Override
    public KeyStroke newInstance (String v) throws Exception
    {
        final String    s=StringUtil.getCleanStringValue(v);
        if ((null == s) || (s.length() <= 0))
            return null;

        final KeyStroke    ks=KeyStroke.getKeyStroke(s);
        if (null == ks)
            throw new NoSuchElementException("Unknown key stroke: " + s);

        return ks;
    }

    public static final KeyStrokeValueInstantiator    DEFAULT=new KeyStrokeValueInstantiator();
}
