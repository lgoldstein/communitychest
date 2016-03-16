/*
 *
 */
package net.community.chest.net.proto.text.http;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import net.community.chest.io.encode.hex.Hex;
import net.community.chest.lang.StringUtil;
import net.community.chest.util.map.MapEntryImpl;
import net.community.chest.util.map.entries.StringPairEntry;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Aug 21, 2008 3:28:59 PM
 */
public final class HttpUtils {
    private HttpUtils ()
    {
        // no instance
    }

    public static final char    QUERY_PARAMS_SEP='&',
                                QUERY_VALUE_SEP='=';
    public static final List<Map.Entry<String,String>> getQueryStringParameters (final String qry) throws IllegalArgumentException
    {
        final Collection<String>    pl=StringUtil.splitString(qry, QUERY_PARAMS_SEP);
        final int                    numPairs=(null == pl) ? 0 : pl.size();
        if (numPairs <= 0)
            return null;

        final List<Map.Entry<String,String>>    npl=new ArrayList<Map.Entry<String,String>>(numPairs);
        for (final String    qpp : pl)
        {
            final int    qpLen=(null == qpp) ? 0 : qpp.length(),
                        sPos=(qpLen <= 1) ? (-1) : qpp.indexOf(QUERY_VALUE_SEP);
            if ((sPos <= 0) || (sPos >= (qpLen-1)))
                throw new IllegalArgumentException("Bad query pair (" + qpp + ") in query=" + qry);

            final String    n=qpp.substring(0, sPos), v=qpp.substring(sPos + 1);
            npl.add(new StringPairEntry(n,v));
        }

        return npl;
    }

    public static final Map<String,String> getQueryStringParametersMap (final String qryString)
    {
        final Collection<? extends Map.Entry<String,String>>    pl=getQueryStringParameters(qryString);
        if ((null == pl) || (pl.size() <= 0))
            return null;

        final Map<String,String>    pm=new TreeMap<String,String>(String.CASE_INSENSITIVE_ORDER);
        for (final Map.Entry<String,String> pe : pl)
        {
            if (null == pe)
                continue;

            final String    prev=pm.put(pe.getKey(), pe.getValue());
            if (prev != null)
                throw new IllegalStateException("getQueryStringParametersMap(" + qryString + ") duplicate parameter: " + pe.getKey());
        }

        return pm;
    }

    public static final char    USER_INFO_SEP=':';
    /**
     * Extracts the user/password information - format: <code>username:password</code>
     * @param usrInfo The input {@link String}
     * @return A &quot;pair&quot; represented as a {@link java.util.Map.Entry} whose
     * key=username, value=password - may be null if null/empty input.
     */
    public static final Map.Entry<String,String> getUserInfo (final String usrInfo)
    {
        final int    iLen=(null == usrInfo) ? 0 : usrInfo.length();
        if (iLen <= 0)
            return null;

        final int        sPos=usrInfo.lastIndexOf(USER_INFO_SEP);
        final String    u, p;
        if (sPos < 0)
        {
            u = usrInfo;
            p = null;
        }
        else
        {
            if (sPos > 0)
                u = usrInfo.substring(0, sPos);
            else
                u = null;

            if (sPos < (iLen-1))
                p = usrInfo.substring(sPos + 1);
            else
                p = null;
        }

        return new MapEntryImpl<String,String>(u, p);
    }

    public static final char    ENCODED_CHAR_PREFIX='%';
    private static final String    ENCODED_CHARS=" *?" + String.valueOf(ENCODED_CHAR_PREFIX);
    // NOTE !!! ENCODED_CHAR_PREFIX is defined as TRUE
    public static final boolean isQueryEncodingRequired (final char ch)
    {
        return (ENCODED_CHARS.indexOf(ch) >= 0);
    }

    public static final String encodeQueryParamaters (final String qry)
    {
        final int    qLen=(null == qry) ? 0 : qry.length();
        if (qLen <= 0)
            return qry;

        StringBuilder    sb=null;
        int                lastPos=0;
        for (int     pos=0; pos < qLen; pos++)
        {
            final char    ch=qry.charAt(pos);
            if (!isQueryEncodingRequired(ch))
                continue;

            // special handling for the ESCAPE char itself
            if (ENCODED_CHAR_PREFIX == ch)
            {
                final int    remLen=qLen - pos - 1;
                if ((remLen >= Hex.MAX_HEX_DIGITS_PER_BYTE)
                 && Hex.isHexDigit(qry.charAt(pos+1))
                 && Hex.isHexDigit(qry.charAt(pos+2)))
                {
                    pos += Hex.MAX_HEX_DIGITS_PER_BYTE + 1;
                    continue;
                }
            }

            // append whatever clear text there is up to here
            if (pos > lastPos)
            {
                final CharSequence    cs=qry.subSequence(lastPos, pos);
                if (null == sb)
                    sb = new StringBuilder(qLen + 64);
                sb.append(cs);
            }

            if (null == sb)
                sb = new StringBuilder(qLen + 64);
            sb.append(ENCODED_CHAR_PREFIX);

            try
            {
                sb = Hex.appendHex(sb, (byte) (ch & 0xFF), true);
            }
            catch(IOException e)
            {
                // should not happen
                throw new RuntimeException(e);
            }

            lastPos = pos + 1;
        }

        // check if have any leftovers
        if ((lastPos < qLen) && (sb != null))
        {
            final CharSequence    cs=qry.substring(lastPos);
            sb.append(cs);
        }

        if ((null == sb) || (sb.length() <= 0))
            return qry;    // means no special characters found

        return sb.toString();
    }
    /**
     * Resolve a {@link URI} value that may have been gleaned from an HTML page
     * @param rootURI The root {@link URI} from which the item path has been
     * extracted
     * @param itemPath The item path
     * @return The URI encoded in the item path - including if <U>relative</U> to
     * the root one
     * @throws URISyntaxException If result is not a valid {@link URI}
     */
    public static final URI resolveItemURI (final URI rootURI, final String itemPath) throws URISyntaxException
    {
        if ((rootURI == null) || (itemPath == null) || (itemPath.length() <= 0))
            return null;

        if (itemPath.contains("://"))
            return new URI(itemPath);

        if (itemPath.charAt(0) == '/')
        {
            if (itemPath.length() <= 1)    // must be at least /a
                throw new URISyntaxException(itemPath, "No resource following separator");

            final char    ch=itemPath.charAt(1);
            if (ch == '/')    // an implicit re-direction
            {
                if (itemPath.length() <= 2)    // must be at least //a
                    throw new URISyntaxException(itemPath, "No resource following double separator");

                final char    nch=itemPath.charAt(2);
                if ((nch == '/') || isBadURIContinuationCharacter(nch))
                    throw new URISyntaxException(itemPath, "Bad resource 3rd character");

                return new URI(rootURI.getScheme() + ":" + itemPath);
            }

            if (isBadURIContinuationCharacter(ch))
                throw new URISyntaxException(itemPath, "Bad resource 2nd character");

            return new URI(rootURI.getScheme(), null, rootURI.getHost(), rootURI.getPort(), itemPath, null, null);
        }

        if (itemPath.charAt(0) == '.')
            return resolveRelativeURI(rootURI, itemPath);
        else
            return new URI(rootURI.getScheme(), null, rootURI.getHost(), rootURI.getPort(), rootURI.getPath() + "/" + itemPath, null, null);
    }

    public static final boolean isBadURIContinuationCharacter (final char ch)
    {
        return (ch == '.') || Character.isWhitespace(ch);
    }

    public static final URI resolveRelativeURI (final URI rootURI, final String itemPath) throws URISyntaxException
    {
        if ((rootURI == null) || (itemPath == null) || (itemPath.length() <= 0))
            return null;

        final char    ch=itemPath.charAt(1);
        if (ch != '.')
        {
            if (itemPath.length() <= 2)    // must be at least ./a
                throw new URISyntaxException(itemPath, "Not enough data in CURDIR separator");
            if (ch != '/')
                throw new URISyntaxException(itemPath, "No preceding CURDIR separator");

            // this is a "./"
            return new URI(rootURI.getScheme(), null, rootURI.getHost(), rootURI.getPort(), rootURI.getPath() + itemPath.substring(1), null, null);
        }

        if (itemPath.length() <= 3)
            throw new URISyntaxException(itemPath, "Not enough data in parent separator");

        final char        sepChar=itemPath.charAt(2);
        if (sepChar != '/')
            throw new URISyntaxException(itemPath, "No parent continuation");

        final String    rootPath=rootURI.getPath();
        if ((rootPath == null) || (rootPath.length() <= 0))
            throw new URISyntaxException(itemPath, "No parent item available");

        final int        posParent=rootPath.lastIndexOf('/');
        final String    parentPath=(posParent <= 0) ? null : rootPath.substring(0, posParent);
        final URI        parentURI=new URI(rootURI.getScheme(), null, rootURI.getHost(), rootURI.getPort(), parentPath, null, null);
        final char        nextChar=itemPath.charAt(3);
        if (nextChar == '.')
            return resolveItemURI(parentURI, itemPath.substring(3));
        else
            return resolveItemURI(parentURI, itemPath.substring(2));
    }
}
