/*
 *
 */
package net.community.chest.web.servlet;

import java.util.Collection;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;

import net.community.chest.util.map.IteratorToEnumeration;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * <P>Allows overriding initialization parameters with those in the
 * {@link Map} it implements</P>
 *
 * @author Lyor G.
 * @since Mar 4, 2009 11:09:04 AM
 */
public class ServletConfigProxy extends TreeMap<String,String> implements ServletConfig {
    /**
     *
     */
    private static final long serialVersionUID = 5404385000040044068L;
    private ServletConfig    _sc;
    public ServletConfig getServletConfig ()
    {
        return _sc;
    }

    public void setServletConfig (ServletConfig sc)
    {
        _sc = sc;
    }

    public ServletConfigProxy (Comparator<? super String> c, ServletConfig sc)
    {
        super(c);
        _sc = sc;
    }

    public ServletConfigProxy (ServletConfig sc)
    {
        this(String.CASE_INSENSITIVE_ORDER, sc);
    }

    public ServletConfigProxy ()
    {
        this((ServletConfig) null);
    }

    public ServletConfigProxy (Map<? extends String,? extends String> m, ServletConfig sc)
    {
        this(String.CASE_INSENSITIVE_ORDER, sc);

        if ((m != null) && (m.size() > 0))
            putAll(m);
    }

    public ServletConfigProxy (Map<? extends String,? extends String> m)
    {
        this(m, null);
    }

    public ServletConfigProxy (SortedMap<String,? extends String> m, ServletConfig sc)
    {
        super(m);
        _sc = sc;
    }

    public ServletConfigProxy (SortedMap<String,? extends String> m)
    {
        this(m, null);
    }

    public ServletConfigProxy (ServletConfigProxy p)
    {
        super(p);

        _sc = p.getServletConfig();
    }
    /*
     * @see javax.servlet.ServletConfig#getInitParameter(java.lang.String)
     */
    @Override
    public String getInitParameter (String name)
    {
        if ((null == name) || (name.length() <= 0))
            return null;

        final String    val=get(name);    // check if have specific override
        if ((val != null) && (val.length() > 0))
            return val;

        final ServletConfig    sc=getServletConfig();
        if (sc != null)
            return sc.getInitParameter(name);

        return null;
    }
    /*
     * @see javax.servlet.ServletConfig#getInitParameterNames()
     */
    @Override
    public Enumeration<String> getInitParameterNames ()
    {
        final Collection<String>    ns=new TreeSet<String>(comparator()), ks=keySet();
        if ((ks != null) && (ks.size() > 0))
            ns.addAll(ks);

        // add the extra names (if any) of the embedded configuration
        final ServletConfig            sc=getServletConfig();
        for (@SuppressWarnings("unchecked")
             final Enumeration<String>    cn=(null == sc) ? null : sc.getInitParameterNames();
             (cn != null) && cn.hasMoreElements();
             )
        {
            final String    ck=cn.nextElement();
            if ((null == ck) || (ck.length() <= 0))
                continue;
            ns.add(ck);
        }

        return new IteratorToEnumeration<String>(ns);
    }
    /*
     * @see javax.servlet.ServletConfig#getServletContext()
     */
    @Override
    public ServletContext getServletContext ()
    {
        final ServletConfig    sc=getServletConfig();
        if (sc != null)
            return sc.getServletContext();

        return null;
    }
    /*
     * @see javax.servlet.ServletConfig#getServletName()
     */
    @Override
    public String getServletName ()
    {
        final ServletConfig    sc=getServletConfig();
        if (sc != null)
            return sc.getServletName();

        return null;
    }
}
