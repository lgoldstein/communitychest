/*
 *
 */
package net.community.chest.ui.helpers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.TreeMap;
import net.community.chest.CoVariantReturn;
import net.community.chest.dom.DOMUtils;
import net.community.chest.lang.ExceptionUtil;
import net.community.chest.util.collection.CollectionsUtils;
import net.community.chest.util.map.MapEntryImpl;

import org.w3c.dom.Element;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Jan 19, 2009 1:59:53 PM
 */
public class SectionsMapImpl extends TreeMap<String,Element> implements SectionsMap {
    /**
     *
     */
    private static final long serialVersionUID = 9078844975160699427L;
    public SectionsMapImpl (Comparator<? super String> c)
    {
        super(c);
    }

    public SectionsMapImpl ()
    {
        this(String.CASE_INSENSITIVE_ORDER);
    }

    private Collection<String>    _names;
    /*
     * @see net.community.chest.ui.helpers.SectionsMap#getSectionsNames()
     */
    @Override
    public Collection<String> getSectionsNames ()
    {
        return _names;
    }
    /*
     * @see net.community.chest.ui.helpers.SectionsMap#sectionsSet()
     */
    @Override
    public Collection<Entry<String,Element>> sectionsSet ()
    {
        final Collection<String>    nl=getSectionsNames();
        final int                    numSections=(null == nl) ? 0 : nl.size();
        if (numSections <= 1)    // if have one or less order has no meaning
            return entrySet();

        // order the sections according to the names
        final Collection<Map.Entry<String,Element>>    sl=
            new ArrayList<Map.Entry<String,Element>>(numSections);
        for (final String n : nl)
        {
            final Element    elem=((null == n) || (n.length() <= 0)) ? null : get(n);
            if (null == elem)
                throw new NoSuchElementException("sectionsSet(" + n + ") no element");

            sl.add(new MapEntryImpl<String,Element>(n, elem));
        }

        return sl;
    }
    /*
     * @see java.util.TreeMap#clear()
     */
    @Override
    public void clear ()
    {
        super.clear();

        final Collection<String>    nl=getSectionsNames();
        if ((nl != null) && (nl.size() > 0))
            nl.clear();
    }
    /*
     * @see java.util.TreeMap#clone()
     */
    @Override
    @CoVariantReturn
    public SectionsMapImpl clone ()
    {
        final SectionsMapImpl        m=getClass().cast(super.clone());
        final Collection<String>    nl=getSectionsNames();
        m._names = ((null == nl) || (nl.size() <= 0)) ? null : new LinkedList<String>(nl);
        return m;
    }
    /*
     * @see java.util.TreeMap#put(java.lang.Object, java.lang.Object)
     */
    @Override
    public Element put (String key, Element value)
    {
        final Element    elem=super.put(key, value);
        if (null == elem)    // add name if 1st mapping
        {
            if (null == _names)
                _names = new LinkedList<String>();
            _names.add(key);
        }

        return elem;
    }
    /*
     * @see java.util.TreeMap#remove(java.lang.Object)
     */
    @Override
    public Element remove (final Object key)
    {
        final Element    elem=super.remove(key);
        if (elem != null)
        {
            final Collection<String>            nl=getSectionsNames();
            final String                        n=key.toString();
            final Comparator<? super String>    c=comparator();
            final Collection<String>            dl=CollectionsUtils.removeMatchingElements(nl, n, c);
            // we must have exactly one match since the map successfully removed the key
            if ((null == dl) || (dl.size() != 1))
                throw new IllegalStateException("remove(" + n + ") no section name removed for value=" + DOMUtils.toString(elem));
        }

        return elem;
    }

    ///////////////// Useful utilities for building various sections names

    public static final <E extends Enum<E>> String getSectionName (E v)
    {
        return (null == v) ? null : v.toString();
    }
    /**
     * Separator character used to build multiple keys name
     * @see #appendKeys(Appendable, boolean, Collection)
     */
    public static final char    SECTION_NAME_COMP_SEP='-';
    /**
     * @param <A> The {@link Appendable} type being used
     * @param sb The {@link Appendable} instance
     * @param addFirst <code>true</code> if append a {@link #SECTION_NAME_COMP_SEP}
     * delimiter <U>before</U> appending the 1st key.
     * @param keys A {@link Collection} of {@link Object}-s whose
     * {@link Object#toString()} values are to be appended to generate the
     * final key. <B>Note:</B> <code>null</code>/empty key strings are
     * silently <U>ignored</U>
     * @return Same {@link Appendable} instance as input
     * @throws IOException If failed to append
     */
    public static final <A extends Appendable> A appendKeys (final A sb, final boolean addFirst, final Collection<?> keys) throws IOException
    {
        final int    numKeys=(null == keys) ? 0 : keys.size();
        if (numKeys <= 0)
            return sb;

        if (null == sb)
            throw new IOException("appendKeys(" + keys + ") no " + Appendable.class.getSimpleName() + " instance");

        boolean    addSep=addFirst;
        for (final Object k : keys)
        {
            final String    s=(null == k) ? null : k.toString();
            if ((null == s) || (s.length() <= 0))
                continue;

            if (addSep)
                sb.append(SECTION_NAME_COMP_SEP);
            sb.append(s);
            addSep = true;
        }
        return sb;
    }

    public static final <A extends Appendable> A appendKeys (final A sb, final boolean addFirst, final Object ... keys) throws IOException
    {
        return appendKeys(sb, addFirst, ((null == keys) || (keys.length <= 0)) ? null : Arrays.asList(keys));
    }

    public static final String getSectionName (final Collection<?> keys)
    {
        final int    numKeys=(null == keys) ? 0 : keys.size();
        if (numKeys <= 0)
            return null;

        try
        {
            final StringBuilder    sb=appendKeys(new StringBuilder(numKeys * 16), false, keys);
            return ((null == sb) || (sb.length() <= 0)) ? null : sb.toString();
        }
        catch(IOException e)    // should not happen
        {
            throw ExceptionUtil.toRuntimeException(e);
        }
    }

    public static final String getSectionName (final Object ... keys)
    {
        return getSectionName(((null == keys) || (keys.length <= 0)) ? null : Arrays.asList(keys));
    }
}
