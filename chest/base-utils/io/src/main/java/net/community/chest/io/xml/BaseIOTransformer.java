/*
 *
 */
package net.community.chest.io.xml;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.net.URL;

import net.community.chest.dom.transform.AbstractTransformer;
import net.community.chest.io.IOCopier;

/**
 * <P>Copyright 2010 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since May 12, 2010 12:02:27 PM
 */
public abstract class BaseIOTransformer extends AbstractTransformer {
    protected BaseIOTransformer ()
    {
        super();
    }
    /**
     * Number of spaces to be used if {@link #USE_TABS_PROPERTY} property
     * is set to <code>false</code>
     */
    public static final String    TAB_LENGTH_PROPERTY="x-tab-length";
    /**
     * Default Number of spaces to be used if {@link #TAB_LENGTH_PROPERTY} not
     * set and its value is required
     */
    public static final int    DEFAULT_TAB_LENGTH=4;

    public int getTabLength ()
    {
        return getIntProperty(getOutputProperty(TAB_LENGTH_PROPERTY), DEFAULT_TAB_LENGTH, 0, Byte.MAX_VALUE);
    }

    public void setTabLength (int l)
    {
        setOutputProperty(TAB_LENGTH_PROPERTY, String.valueOf(l));
    }
    /**
     * Property used to control how indentation is made - <code>true</code>
     * (default) use TAB (ASCII 09), <code>false</code> - use spaces (ASCII 32)
     * - in which case the {@link #TAB_LENGTH_PROPERTY} is used
     */
    public static final String    USE_TABS_PROPERTY="x-use-tabs";
    public boolean isTabsIndentation ()
    {
        return isTrueProperty(getOutputProperty(USE_TABS_PROPERTY));
    }

    public void setTabsIndentation (boolean v)
    {
        setOutputProperty(USE_TABS_PROPERTY, String.valueOf(v));
    }
    /**
     * Property used to store the URL open-timeout in case one of the
     * {@link #openURLForInput(String)} or {@link #openURLForOutput(String)}
     * is called
     */
    public static final String    URL_OPEN_TIMEOUT_VALUE="x-url-open-timeout";
    /**
     * @return Timeout (sec.) used in calls {@link #openURLForInput(String)}
     * or {@link #openURLForOutput(String)}
     */
    public int getURLOpenTimeout ()
    {
        return getIntProperty(getOutputProperty(URL_OPEN_TIMEOUT_VALUE), IOCopier.DEFAULT_URL_TIMEOUT, Integer.MIN_VALUE, Integer.MAX_VALUE);
    }

    public void setURLOpenTimeout (int maxTimeout)
    {
        setOutputProperty(URL_OPEN_TIMEOUT_VALUE, String.valueOf(maxTimeout));
    }

    protected InputStream openURLForInput (String url)
        throws IOException, URISyntaxException
    {
        return IOCopier.openURLForRead(url, getURLOpenTimeout());
    }

    protected InputStream openURLForInput (URL url) throws IOException
    {
        return IOCopier.openURLForRead(url, getURLOpenTimeout());
    }

    protected OutputStream openURLForOutput (String url)
        throws IOException, URISyntaxException
    {
        return IOCopier.openURLForWrite(url, getURLOpenTimeout());
    }

    protected OutputStream openURLForOutput (URL url)
        throws IOException
    {
        return IOCopier.openURLForWrite(url, getURLOpenTimeout());
    }
}
