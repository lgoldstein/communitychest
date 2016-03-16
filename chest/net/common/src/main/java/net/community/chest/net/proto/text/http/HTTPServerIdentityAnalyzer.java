package net.community.chest.net.proto.text.http;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import net.community.chest.net.proto.text.NetServerIdentityAnalyzer;
import net.community.chest.util.map.entries.StringPairEntry;

/**
 * <P>Copyright 2007 as per GPLv2</P>
 *
 * <P>Actually analyzes the <I>Server</I> header data</P>
 * @author Lyor G.
 * @since Oct 25, 2007 9:18:54 AM
 */
public class HTTPServerIdentityAnalyzer extends NetServerIdentityAnalyzer {
    public HTTPServerIdentityAnalyzer ()
    {
        super();
    }
    // some known servers
    public static final String    MicrosoftIIS="Microsoft-IIS",
                                Apache="Apache",
                                Coyote=Apache + "-Coyote";
    /* known templates for HTTP servers */
    // Server: Microsoft-IIS/5.0
    public static final String    MicrosoftIISServerPattern=MicrosoftIIS + "%T=" + MicrosoftIIS + " /" + "%V %I";
    // Server: Apache-Coyote/1.1
    public static final String    CoyoteServerPattern=Coyote + "%T=" + Coyote + " /" + "%V %I";
    // Server: Apache/2.2.0 (Unix) mod_jk/1.2.5
    public static final String    ApacheServerPattern=Apache + "%T=" + Apache +  " /" + "%V %I";
    /**
     * Currently known patterns
     */
    public static final List<String>    HTTPServerPatterns=Arrays.asList(
            MicrosoftIISServerPattern,
            CoyoteServerPattern,
            ApacheServerPattern
        );
    /*
     * @see net.community.chest.net.proto.text.NetServerIdentityAnalyzer#getKnownPatterns()
     */
    @Override
    public Collection<String> getKnownPatterns ()
    {
        return HTTPServerPatterns;
    }
    /* Note: handles both formats - "Server: zzz" and just "zzz"
     * @see net.community.chest.net.proto.text.NetServerIdentityAnalyzer#getWelcomePatternMatchStart(java.lang.CharSequence)
     */
    @Override
    public int getWelcomePatternMatchStart (CharSequence welcomeLine)
    {
        final int    wlLen=(null == welcomeLine) ? 0 : welcomeLine.length();
        if (wlLen <= 0)
            return (-1);

        for (int    wlPos=0; wlPos < wlLen; wlPos++)
        {
            if (welcomeLine.charAt(wlPos) != ':')
                continue;

            final String    hdrName=welcomeLine.subSequence(0, wlPos).toString();
            if ("Server".equalsIgnoreCase(hdrName))
            {
                for (wlPos++ /* skip ':' */; wlPos < wlLen; wlPos++)
                {
                    final char    wlChar=welcomeLine.charAt(wlPos);
                    if ((wlChar != ' ') && (wlChar != '\t') && (wlChar != '\r') && (wlChar != '\n'))
                        return wlPos;
                }

                // this point is reached if all characters following ':' are white-space
                return wlLen;
            }

            break;
        }

        return 0;    // OK if no ':' found
    }
    /* Use a more efficient parsing than pattern matching since format is "Type/Version"
     * @see net.community.chest.net.proto.text.NetServerIdentityAnalyzer#getServerIdentity(java.lang.CharSequence, int, int)
     */
    @Override
    public Map.Entry<String, String> getServerIdentity (CharSequence wl, int startPos, int len)
    {
        final int    maxPos=startPos + len;
        if ((null == wl) || (startPos < 0) || (len <= 0) || (maxPos > wl.length()))
            return null;

        for (int    curPos=startPos; curPos < maxPos; curPos++)
        {
            if (wl.charAt(curPos) != '/')
                continue;

            final String    type=wl.subSequence(startPos, curPos).toString();
            // make sure there is a version following
            if (curPos >= (maxPos-1))
                return null;

            int    lastPos=curPos+1; /* skip '/' */
            for ( ; lastPos < maxPos; lastPos++)
            {
                final char    wlChar=wl.charAt(lastPos);
                // use first white-space character as stopper
                if ((' ' == wlChar) || ('\t' == wlChar) || ('\r' == wlChar) || ('\n' == wlChar))
                    break;
            }

            final String    version=wl.subSequence(curPos+1, lastPos).toString();
            return new StringPairEntry(type,version);
        }

        // no '/' found
        return null;
    }
}
