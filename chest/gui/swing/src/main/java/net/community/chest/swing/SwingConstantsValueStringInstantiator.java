/*
 *
 */
package net.community.chest.swing;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.TreeMap;

import javax.swing.SwingConstants;

import net.community.chest.convert.NumberValueStringInstantiator;
import net.community.chest.dom.AbstractXmlValueStringInstantiator;
import net.community.chest.lang.StringUtil;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Sep 24, 2008 11:03:53 AM
 */
public class SwingConstantsValueStringInstantiator
        extends AbstractXmlValueStringInstantiator<Integer>
        implements NumberValueStringInstantiator<Integer> {
    /*
     * @see net.community.chest.convert.NumberValueStringInstantiator#getPrimitiveValuesClass()
     */
    @Override
    public final Class<Integer> getPrimitiveValuesClass ()
    {
        return Integer.TYPE;
    }
    /*
     * @see net.community.chest.reflect.ValueNumberInstantiator#getInstanceNumber(java.lang.Object)
     */
    @Override
    public Integer getInstanceNumber (Integer inst) throws Exception
    {
        return inst;
    }
    /*
     * @see net.community.chest.reflect.ValueNumberInstantiator#newInstance(java.lang.Number)
     */
    @Override
    public Integer newInstance (Integer num) throws Exception
    {
        return num;
    }

    private static Map<String,Integer>    _constsMap    /* =null */;
    /**
     * @return A {@link Map} of all the static fields defined in
     * {@link SwingConstants} class where key=field name (case
     * <U>insensitive</U>), value=field {@link Integer} value
     * @throws Exception If failed to explore the class
     */
    public static final synchronized Map<String,Integer> getConstantsMap () throws Exception
    {
        if (null == _constsMap)
        {
            try
            {
                final Field[]    fa=SwingConstants.class.getDeclaredFields();
                for (final Field f : fa)
                {
                    final String    n=(null == f) ? null : f.getName();
                    if ((null == n) || (n.length() <= 0))
                        continue;    // should not happen

                    final int    mod=f.getModifiers();
                    if ((!Modifier.isPublic(mod)) || (!Modifier.isStatic(mod)))
                        continue;

                    if (!f.isAccessible())    // should not happen, but do it anyway...
                        f.setAccessible(true);

                    final Object    v=f.get(null);
                    if (!(v instanceof Integer))
                        continue;

                    if (null == _constsMap)
                        _constsMap = new TreeMap<String,Integer>(String.CASE_INSENSITIVE_ORDER);

                    final Object prev=_constsMap.put(n, (Integer) v);
                    if (prev != null)
                        throw new IllegalStateException("Multiple values for field=" + n + ": new=" + v + "/old=" + prev);
                }
            }
            catch(Exception e)
            {
                if (_constsMap != null)
                    _constsMap = null;
                throw e;
            }
        }

        return _constsMap;
    }
    /*
     * @see net.community.chest.convert.ValueStringInstantiator#convertInstance(java.lang.Object)
     */
    @Override
    public String convertInstance (final Integer inst) throws Exception
    {
        if (null == inst)
            return null;

        // NOTE !!! only UNIQUE values will succeed
        final Map<String,Integer>                                cm=getConstantsMap();
        final Collection<? extends Map.Entry<String,Integer>>    cml=
            ((null == cm) || (cm.size() <= 0)) ? null : cm.entrySet();
        String                                                    ret=null;
        if ((cml != null) && (cml.size() > 0))
        {
            for (final Map.Entry<String,Integer>    ce : cml)
            {
                final Integer    v=(null == ce) ? null : ce.getValue();
                if (!inst.equals(v))
                    continue;

                if (ret != null)
                    throw new IllegalStateException("convertInstance(" + inst + ") multiple matches: " + ret + "/" + ce.getKey());

                if ((null == (ret=ce.getKey())) || (ret.length() <= 0))    // should not happen
                    throw new IllegalStateException("convertInstance(" + inst + ") no constant name");
            }
        }

        if ((null == ret) || (ret.length() <= 0))
            throw new NoSuchElementException("convertInstance(" + inst + ") no match found");

        return ret;
    }
    /*
     * @see net.community.chest.convert.ValueStringInstantiator#newInstance(java.lang.String)
     */
    @Override
    public Integer newInstance (final String v) throws Exception
    {
        final String    s=StringUtil.getCleanStringValue(v);
        if ((null == s) || (s.length() <= 0))
            return null;

        final Map<String,Integer>    cm=getConstantsMap();
        final Integer                i=((null == cm) || (cm.size() <= 0)) ? null : cm.get(s);
        if (null == v)
            throw new NoSuchElementException("newInstance(" + s + ") unknown constant");

        return i;
    }

    public SwingConstantsValueStringInstantiator ()
    {
        super(Integer.class);
    }

    public static final SwingConstantsValueStringInstantiator    DEFAULT=new SwingConstantsValueStringInstantiator();
}
