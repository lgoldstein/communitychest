/*
 *
 */
package net.community.chest.apache.ant.winver;

import net.community.chest.lang.StringUtil;

/**
 * <P>Copyright as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Jul 7, 2009 11:40:17 AM
 */
public enum VersionResourceKeyword {
    FILEVERSION("FILEVERSION") {
            /*
             * @see net.community.chest.apache.ant.winver.VersionResourceKeyword#rebuildValue(java.lang.String, java.lang.String)
             */
            @Override
            public String rebuildValue (final String lineData, final String verValue)
            {
                return rebuildSimpleValue(lineData, verValue);
            }
            /*
             * @see net.community.chest.apache.ant.winver.VersionResourceKeyword#extractVersion(java.lang.String)
             */
            @Override
            public String extractVersion (final String lineData)
            {
                return extractSimpleVersion(lineData);
            }
        },
    PRODUCTVERSION("PRODUCTVERSION") {
            /*
             * @see net.community.chest.apache.ant.winver.VersionResourceKeyword#rebuildValue(java.lang.String, java.lang.String)
             */
            @Override
            public String rebuildValue (final String lineData, final String verValue)
            {
                return rebuildSimpleValue(lineData, verValue);
            }
            /*
             * @see net.community.chest.apache.ant.winver.VersionResourceKeyword#extractVersion(java.lang.String)
             */
            @Override
            public String extractVersion (final String lineData)
            {
                return extractSimpleVersion(lineData);
            }
        },
    VALFILEVER("FileVersion") {
            /*
             * @see net.community.chest.apache.ant.winver.VersionResourceKeyword#rebuildValue(java.lang.String, java.lang.String)
             */
            @Override
            public String rebuildValue (final String lineData, final String verValue)
            {
                return rebuildBlockValue(lineData, verValue);
            }
            /*
             * @see net.community.chest.apache.ant.winver.VersionResourceKeyword#extractVersion(java.lang.String)
             */
            @Override
            public String extractVersion (final String lineData)
            {
                return extractBlockVersion(lineData);
            }
        },
    VALPRODVER("ProductVersion") {
            /*
             * @see net.community.chest.apache.ant.winver.VersionResourceKeyword#rebuildValue(java.lang.String, java.lang.String)
             */
            @Override
            public String rebuildValue (final String lineData, final String verValue)
            {
                return rebuildBlockValue(lineData, verValue);
            }
            /*
             * @see net.community.chest.apache.ant.winver.VersionResourceKeyword#extractVersion(java.lang.String)
             */
            @Override
            public String extractVersion (final String lineData)
            {
                return extractBlockVersion(lineData);
            }
        };

    private final String    _kw;
    public final String getKeyword ()
    {
        return _kw;
    }

    public String extractVersion (final String lineData)
    {
        return lineData;
    }

    protected static final String compactVersion (final String valArg)
    {
        final String    vs=StringUtil.getCleanStringValue(valArg);
        if ((null == vs) || (vs.length() <= 0))
            return vs;

        return vs.replaceAll(" ", "");
    }

    protected static final String extractSimpleVersion (final String lineData)
    {
        final int    ll=(null == lineData) ? 0 : lineData.length();
        if (ll <= 0)
            return null;

        int curPos=0;
        for ( ; curPos < ll; curPos++)
        {
            final char    c=lineData.charAt(curPos);
            if ((c != ' ') && (c != '\t'))
                break;
        }

        // skip keyword
        for (curPos++; curPos < ll; curPos++)
        {
            final char    c=lineData.charAt(curPos);
            if ((' ' == c) || ('\t' == c))
                break;
        }

        for (curPos++; curPos < ll; curPos++)
        {
            final char    c=lineData.charAt(curPos);
            if ((' ' == c) || ('\t' == c))
                continue;

            return compactVersion(lineData.substring(curPos));
        }

        return null;
    }

    protected static final String rebuildSimpleValue (final String lineData, final String verValue)
    {
        final int    ll=(null == lineData) ? 0 : lineData.length(),
                    vLen=(null == verValue) ? 0 : verValue.length();
        if ((ll <= 0) || (vLen <= 0) || (ll <= vLen))
            return lineData;

        final StringBuilder    sb=new StringBuilder(ll);
        // copy preceding white space
        int                    curPos=0;
        for (; curPos < ll; curPos++)
        {
            final char    c=lineData.charAt(curPos);
            if ((c != ' ') && (c != '\t'))
                break;
            sb.append(c);
        }

        // copy keyword
        for (; curPos < ll; curPos++)
        {
            final char    c=lineData.charAt(curPos);
            if ((' ' == c) || ('\t' == c))
                break;
            sb.append(c);
        }

        sb.append(' ')
          .append(verValue);
        return sb.toString();
    }

    public abstract String rebuildValue (final String lineData, final String verValue);

    VersionResourceKeyword (String kw)
    {
        _kw = kw;
    }

    private static VersionResourceKeyword[]    _values;
    public static final synchronized VersionResourceKeyword[] getValues ()
    {
        if (null == _values)
            _values = values();
        return _values;
    }

    public static final String    VALUE_KWD="VALUE";
    protected static final String extractValueType (final String valArg)
    {
        final int    vLen=
            (null == valArg) ? 0 : valArg.length(),
                    sPos=
            (vLen <= 2) ? (-1) : valArg.indexOf('"'),
                    ePos=
            ((sPos < 0) || (sPos >= (vLen-1))) ? (-1) : valArg.indexOf('"', sPos+1);
        if ((sPos < 0) || (ePos < 0) || (sPos >= ePos) || (ePos >= (vLen-1)))
            return null;

        return valArg.substring(sPos + 1, ePos);
    }

    protected static final String extractBlockVersion (final String lineData)
    {
        final int    ll=(null == lineData) ? 0 : lineData.length(),
                    ePos=(ll <= 2) ? (-1) : lineData.lastIndexOf('"'),
                    sPos=((ll <= 2) || (ePos <= 1)) ? (-1) : lineData.lastIndexOf('"', ePos-1);
        if ((sPos < 0) || (ePos < 0) || (sPos >= ePos))
            return null;

        final String    valArg=lineData.substring(sPos+1, ePos);
        final int        vLen=(null == valArg) ? 0 : valArg.length();
        if ((vLen > 2)
        &&  ('0' == valArg.charAt(vLen-1))
        &&  ('\\' == valArg.charAt(vLen-2)))
            return compactVersion(valArg.substring(0, vLen-2));
        else
            return compactVersion(valArg);
    }

    protected static final String rebuildBlockValue (final String lineData, final String verValue)
    {
        final int    ll=(null == lineData) ? 0 : lineData.length(),
                    vLen=(null == verValue) ? 0 : verValue.length();
        if ((ll <= 0) || (vLen <= 0) || (ll <= vLen))
            return lineData;

        final StringBuilder    sb=new StringBuilder(ll);
        // copy preceding white space
        int                    curPos=0;
        for (; curPos < ll; curPos++)
        {
            final char    c=lineData.charAt(curPos);
            if ((c != ' ') && (c != '\t'))
                break;
            sb.append(c);
        }

        // copy VALUE keyword
        for (; curPos < ll; curPos++)
        {
            final char    c=lineData.charAt(curPos);
            if ((' ' == c) || ('\t' == c))
                break;
            sb.append(c);
        }

        final String    vt=extractValueType(lineData.substring(curPos));
        sb.append(" \"")
          .append(vt)
          .append("\", \"")
          .append(verValue)
          .append("\\0\"")
          ;

        return sb.toString();
    }

    public static final VersionResourceKeyword fromLineData (final CharSequence s)
    {
        final int    sLen=(null == s) ? 0 : s.length();
        String        lineData=null;
        // strip preceding white space
        for (int    curPos=0; curPos < sLen; curPos++)
        {
            final char    c=s.charAt(curPos);
            if ((' ' == c) || ('\t' == c))
                continue;

            lineData = s.subSequence(curPos, sLen).toString();
            break;
        }
        if ((null == lineData) || (lineData.length() <= 0))
            return null;

        final VersionResourceKeyword[]    vals=getValues();
        if ((null == vals) || (vals.length <= 0))
            return null;    // should not happen

        final String    vv;
        if (StringUtil.startsWith(lineData, VALUE_KWD, true, true))
            vv = extractValueType(lineData.substring(VALUE_KWD.length()));
        else
            vv = null;

        for (final VersionResourceKeyword v : vals)
        {
            final String    kw=(null == v) ? null : v.getKeyword();
            if ((vv != null) && (vv.length() > 0))
            {
                if (StringUtil.compareDataStrings(vv, kw, true) != 0)
                    continue;
            }
            else if (!StringUtil.startsWith(lineData, kw, true, true))
                continue;

            return v;
        }

        return null;
    }
}
