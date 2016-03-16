/*
 *
 */
package net.community.chest.xml.transform;

import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

import javax.xml.transform.ErrorListener;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.URIResolver;

/**
 * <P>Copyright 2010 as per GPLv2</P>
 *
 * <P>Provides some default implementations for some of the {@link Transformer}
 * abstract methods</P>
 *
 * @author Lyor G.
 * @since May 12, 2010 9:19:18 AM
 */
public abstract class BaseTransformer extends Transformer {
    protected BaseTransformer ()
    {
        super();
    }
    /**
     * @param v A {@link Transformer} property value
     * @param defVal Value to return if null/empty {@link String} value
     * @return <code>true</U> if value is &quot;yes&quot; or &quot;true&quot;
     * (<U>case insensitive</U>).
     */
    protected boolean isTrueProperty (final String v, final boolean defVal)
    {
        if ((null == v) || (v.length() <= 0))
            return defVal;

        return "yes".equalsIgnoreCase(v) || "true".equalsIgnoreCase(v);
    }
    // NOTE !!! returns TRUE for null/empty string
    protected boolean isTrueProperty (final String v)
    {
        return isTrueProperty(v, true);
    }
    /**
     * Used to retrieve an <code>int</code> property
     * @param v Value {@link String}
     * @param defVal Default value if null/empty value
     * @param minVal Min. allowed value (inclusive)
     * @param maxVal Max. allowed value (inclusive)
     * @return Parsed/default value
     * @throws NumberFormatException if bad string or parsed value not in
     * min./max. range (including the default value)
     */
    protected int getIntProperty (final String v, final int defVal, final int minVal, final int maxVal) throws NumberFormatException
    {
        final int    iVal=((null == v) || (v.length() <= 0)) ? defVal : Integer.parseInt(v);
        if ((iVal < minVal) || (iVal > maxVal))
            throw new NumberFormatException("getIntProperty(" + v + ") value not in [" + minVal + " - " + maxVal + "] range");

        return iVal;
    }

    public boolean isOmitXmlDeclaration ()
    {
        return isTrueProperty(getOutputProperty(OutputKeys.OMIT_XML_DECLARATION), false);
    }

    public void setOmitXmlDeclaration (boolean val)
    {
        setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, String.valueOf(val));
    }

    private Map<String,Object>    _paramsMap    /* =null */;
    public Map<String,Object> getParameters ()
    {
        return _paramsMap;
    }

    public void setParameters (Map<String,Object> paramsMap)
    {
        _paramsMap = paramsMap;
    }
    /*
     * @see javax.xml.transform.Transformer#getParameter(java.lang.String)
     */
    @Override
    public Object getParameter (String name)
    {
        if ((null == name) || (name.length() <= 0))
            return null;

        final Map<String,?>    m=getParameters();
        if ((null == m) || (m.size() <= 0))
            return null;

        return m.get(name);
    }
    /*
     * @see javax.xml.transform.Transformer#setParameter(java.lang.String, java.lang.Object)
     */
    @Override
    public void setParameter (String name, Object value)
    {
        if ((null == name) || (name.length() <= 0) || (null == value))
            throw new IllegalArgumentException("setParameter(" + name + "/" + value + ") not allowed null/empty name/value");

        Map<String,Object>    m=getParameters();
        if (null == m)
        {
            setParameters(new TreeMap<String,Object>());

            if (null == (m=getParameters()))    // should not happen since we just set it
                throw new IllegalStateException("setParameter(" + name + "/" + value + ") no " + Map.class.getSimpleName() + " available though set");
        }

        m.put(name, value);
    }
    /*
     * @see javax.xml.transform.Transformer#clearParameters()
     */
    @Override
    public void clearParameters ()
    {
        final Map<String,?>    m=getParameters();
        if ((m != null) && (m.size() > 0))
            m.clear();
    }

    private ErrorListener    _errListener    /* =null */;
    /*
     * @see javax.xml.transform.Transformer#getErrorListener()
     */
    @Override
    public ErrorListener getErrorListener ()
    {
        return _errListener;
    }
    /*
     * @see javax.xml.transform.Transformer#setErrorListener(javax.xml.transform.ErrorListener)
     */
    @Override
    public void setErrorListener (ErrorListener listener) throws IllegalArgumentException
    {
        if (null == (_errListener=listener))    // comply with javadoc for this method
            throw new IllegalArgumentException("Null " + ErrorListener.class.getSimpleName() + " N/A");
    }

    private Properties    _outProps    /* =null */;
    /*
     * @see javax.xml.transform.Transformer#getOutputProperties()
     */
    @Override
    public Properties getOutputProperties ()
    {
        return _outProps;
    }
    /*
     * @see javax.xml.transform.Transformer#setOutputProperties(java.util.Properties)
     */
    @Override
    public void setOutputProperties (Properties oformat)
    {
        _outProps = oformat;
    }
    /*
     * @see javax.xml.transform.Transformer#getOutputProperty(java.lang.String)
     */
    @Override
    public String getOutputProperty (String name) throws IllegalArgumentException
    {
        if ((null == name) || (name.length() <= 0))
            throw new IllegalArgumentException("getOutputProperty(" + name + ") null/empty name N/A");

        final Properties    props=getOutputProperties();
        if ((null == props) || (props.size() <= 0))
            return null;

        return props.getProperty(name);
    }
    /*
     * @see javax.xml.transform.Transformer#setOutputProperty(java.lang.String, java.lang.String)
     */
    @Override
    public void setOutputProperty (String name, String value) throws IllegalArgumentException
    {
        if ((null == name) || (name.length() <= 0)
         || (null == value) || (value.length() <= 0))
            throw new IllegalArgumentException("setOutputProperty(" + name + "/" + value + ") not allowed null/empty name/value");

        Properties props=getOutputProperties();
        if (null == props)
        {
            setOutputProperties(new Properties());

            if (null == (props=getOutputProperties()))    // should not happen since we just set it
                throw new IllegalStateException("setOutputProperty(" + name + "/" + value + ") no " + Properties.class.getSimpleName() + " instance though set");
        }

        props.setProperty(name, value);
    }

    private URIResolver    _resolver    /* =null */;
    /*
     * @see javax.xml.transform.Transformer#getURIResolver()
     */
    @Override
    public URIResolver getURIResolver ()
    {
        return _resolver;
    }
    /*
     * @see javax.xml.transform.Transformer#setURIResolver(javax.xml.transform.URIResolver)
     */
    @Override
    public void setURIResolver (URIResolver resolver)
    {
        _resolver = resolver;
    }
    /*
     * @see javax.xml.transform.Transformer#reset()
     */
    @Override
    public void reset ()
    {
        // not calling "super" since it would throw an UnsupportedOperationException
    }
}
