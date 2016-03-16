package net.community.chest.apache.ant;

import java.io.File;
import java.util.Map;
import java.util.TreeMap;

import net.community.chest.apache.ant.helpers.AttributeValuePair;
import net.community.chest.apache.ant.helpers.InMemoryFileContentsReplacer;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;

/**
 * Copyright 2007 as per GPLv2
 *
 * Searches each file and replaces the occurrences of the "${xxx}" with the
 * value of the "xxx" property taken from the current project.
 *
 * @author Lyor G.
 * @since Jun 20, 2007 4:08:16 PM
 */
public class ReplaceProps extends InMemoryFileContentsReplacer {
    /**
     * Default constructor
     */
    public ReplaceProps ()
    {
        super();
    }
    /**
     * TRUE if fail the task if a property value not found
     */
    private boolean    _failOnMissingProp=true;
    public boolean isFailOnMissingProp ()
    {
        return _failOnMissingProp;
    }

    public void setFailOnMissingProp (boolean failOnMissingProp)
    {
        _failOnMissingProp = failOnMissingProp;
    }
    /**
     * If TRUE then CRLF is replaced by LF
     */
    private boolean    _LFonly    /* =false */;
    public boolean isLFOnly ()
    {
        return _LFonly;
    }

    public void setLFOnly (boolean lfOnly)
    {
        _LFonly = lfOnly;
    }
    /**
     * {@link Map} of extra "local" properties to be used only for the duration
     * of this task - key=name, value=non-null value
     */
    private Map<String,String>    _extraProps    /* =null */;
    public void addExtraProp (final String name, final String value) throws BuildException
    {
        if ((null == name) || (name.length() <= 0))
            throw new BuildException("Missing extra property name", getLocation());

        final Project    p=getProject();
        final String    pVal=(null == p) ? null : p.getProperty(name);
        if (pVal != null)
        {
            if (!pVal.equals(value))
                throw new BuildException("Extra property " + name + " already defined with different value=" + pVal);
        }
        else
        {
            if (_extraProps != null)
            {
                final String    epVal=_extraProps.get(name);
                if (epVal != null)
                    throw new BuildException("Extra property " + name + " re-specified", getLocation());
            }
            else
                _extraProps = new TreeMap<String,String>();

            _extraProps.put(name, (null == value) ? "" : value);
        }
    }
    /**
     * Class used to represent &lt;extraprop&gt; extra "local" properties
     * @author lyorg
     * @since Apr 6, 2006 2:55:04 PM
     */
    public static final class ExtraProp extends AttributeValuePair {
        /**
         *
         */
        private static final long serialVersionUID = -6237681892379825686L;

        public ExtraProp ()
        {
            super();
        }

        public ExtraProp (String name, String value) throws BuildException
        {
            super(name, value);

            if ((null == name) || (name.length() <= 0))
                throw new BuildException("Missing extra property name");
        }
    }
    /* NOTE !!! the name of this method MUST be "addConfiguredExtraProp",
     *         otherwise the <extraprop> sub-element(s) are NOT handled
     *         correctly by the ANT parser
     */
    public void addConfiguredExtraProp (ExtraProp ep) throws BuildException
    {
        addExtraProp((null == ep) ? null : ep.getName(), (null == ep) ? null : ep.getValue());
    }
    /*
     * @see net.community.chest.apache.ant.helpers.InMemoryFileContentsReplacer#replaceProperties(java.io.File, java.lang.String)
     */
    @Override
    protected String replaceProperties (final File inFile, final String inData) throws BuildException
    {
        final int    iLen=(null == inData) ? 0 : inData.length();
        if (iLen <= 0)
            return inData;

        StringBuilder    sb=null;    // allocated if necessary
        final Project    proj=getProject();    // needed for properties
        int    curPos=0;
        for (int nextPos=inData.indexOf('$', curPos); (nextPos > 0) && (nextPos < iLen); )
        {
            // ignore '$' at end of string
            if (nextPos >= (iLen-1))
                break;

            // ignore '$' if not followed by '{'
            if (inData.charAt(nextPos+1) != '{')
            {
                nextPos = inData.indexOf('$', nextPos + 1);
                continue;
            }

            String    propName=null;
            int        lastPos=nextPos+2;
            boolean    isValidName=true;
            for ( ; lastPos < iLen; lastPos++)
            {
                final char    posChar=inData.charAt(lastPos);
                if ('}' == posChar)
                {
                    propName = inData.substring(nextPos+2, lastPos);
                    break;
                }

                // only lowercase and numbers are allowed
                if (((posChar >= 'a') && (posChar <= 'z'))
                 || ((posChar >= '0') && (posChar <= '9'))
                 || ('.' == posChar)
                 || ('-' == posChar))
                 continue;

                isValidName = false;
                break;
            }

            // assume that if not one of these characters, then not
            // an ANT property - e.g., Unix Shell: JAVAPTH=${JAVAPTH:-"$JDK_HOME/bin"}
            if (!isValidName)
            {
                if (lastPos >= iLen)
                    break;

                nextPos = inData.indexOf('$', lastPos);
                continue;
            }

            if ((null == propName) || (propName.length() <= 0))
                throw new BuildException("Improper property name format at offset=" + nextPos + " of file " + inFile.getAbsolutePath(), getLocation());

            String propValue=(null == proj) ? null : proj.getProperty(propName);
            // if not in project properties, check if have it in extra properties
            if ((null == propValue) && (_extraProps != null))
                propValue = _extraProps.get(propName);

            if (propValue != null)
            {
                final String    clrText=inData.substring(curPos, nextPos);
                if (null == sb)
                    sb = new StringBuilder(iLen);

                sb.append(clrText).append(propValue);

                if ((curPos=lastPos + 1 /* skip ending '}' */) >= iLen)
                    break;

                nextPos = inData.indexOf('$', curPos);
            }
            else
            {
                if (isFailOnMissingProp())
                    throw new BuildException("No value found for property=" + propName + " in file " + inFile.getAbsolutePath(), getLocation());

                // ignore and continue
                log("Skipping unknown property=" + propName, Project.MSG_WARN);
                nextPos = inData.indexOf('$', lastPos);
            }
        }

        // check if any leftovers
        if ((curPos < iLen) && (sb != null))
        {
            final String    endData=inData.substring(curPos);
            sb.append(endData);
        }

        if (isLFOnly())
        {
            curPos = 0;

            // if no change, then check if initial string has CR
            if (null == sb)
            {
                if ((curPos=inData.indexOf('\r')) >= 0)
                    sb = new StringBuilder(inData);
            }

            for (int curLen=(null == sb) ? 0 : sb.length(); (curPos >= 0) && (curPos < curLen); )
            {
                if ('\r' == sb.charAt(curPos))
                {
                    sb = sb.deleteCharAt(curPos);
                    curLen = sb.length();

                    // do NOT increment 'curPos' since we deleted the character at its position
                }
                else
                    curPos++;
            }
        }

        return ((null == sb) || (sb.length() <= 0)) ? inData : sb.toString();
    }
}
