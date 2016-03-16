package net.community.chest.net.proto.text;

import java.util.Collection;
import java.util.Map;

import net.community.chest.ParsableString;
import net.community.chest.util.map.entries.StringPairEntry;

/**
 * <P>Copyright 2007 as per GPLv2</P>
 *
 * <P>Used to analyze a server's identity</P>
 * @author Lyor G.
 * @since Oct 25, 2007 8:46:21 AM
 */
public abstract class NetServerIdentityAnalyzer {
    protected NetServerIdentityAnalyzer ()
    {
        super();
    }
    /**
     * Special characters (besides "whitespace" that are considered pattern argument delimiters
     * @see #inetSkipWelcomeArgument(CharSequence cs, int startPos, int endPos)
     */
    protected static final String InetNonIgnoredWelcomePatternChars="()[]{};";
    /**
     * Skips an "argument" - defined as any non-empty (and non-igonrable) sequence of characters
     * @param cs character sequence to check
     * @param startPos start position from which to start checking argument (inclusive) - Note:
     * this position is assumed to be non-empty itself...
     * @param endPos end position at which to to stop checking (exclusive)
     * @return index of next position in sequence (<0 if error)
     * @see #InetNonIgnoredWelcomePatternChars
     */
    protected static final int inetSkipWelcomeArgument (final CharSequence cs, final int startPos, final int endPos)
    {
        for (int    index=startPos; index < endPos; index++)
        {
            final char    c=cs.charAt(index);
            if (ParsableString.isEmptyChar(c) || ((-1) != InetNonIgnoredWelcomePatternChars.indexOf(c)))
                return index;
        }

        return endPos;
    }
    /**
     * The pattern modifier indicator itself
     */
    public static final char INET_WPAT_MODIFIER='%';
    /**
     * Server type name
     */
    public static final char INET_WPAT_TYPE='T';
    /**
     * Server version string
     */
    public static final char INET_WPAT_VERSION='V';
    /**
     * Ignore string indicator - Note: if this is the last modifier, then rest
     * of the welcome line is ignored. Otherwise, this is a placeholder for a
     * single string argument.
     */
    public static final char INET_WPAT_IGNORE='I';
    /**
     * Match an RFC822 date/time
     */
    public static final char INET_WPAT_RFC822DATE='D';
    /**
     * Matches an <B>indefinite</B> number of characters. Note: must be followed
     * by a delimiter to indicate when matching is done: e.g. "%*(" -> skip till ')' found
     */
    public static final char INET_WPAT_SKIPTODELIM='*';
    /**
     * Extracts a server identity info component
     * @param cs character sequence from which to extract the component
     * @param startPos start position from which to look for component data end (inclusive) - Note:
     * assumed to be non-empty
     * @param endPos end position at which to to stop checking (exclusive)
     * @param ptChar information component type character
     * @param id The {@link StringPairEntry} information object to be updated
     * @return index of next position in sequence (<0 if error)
     * @see #inetSkipWelcomeArgument(CharSequence cs, int startPos, int endPos)
     * @see #INET_WPAT_TYPE
     * @see #INET_WPAT_VERSION
     */
    protected static final int extractServerIdentityInfo (final CharSequence cs, final int startPos, final int endPos, final char ptChar, final StringPairEntry id)
    {
        final int    valEnd=inetSkipWelcomeArgument(cs, startPos, endPos);
        if (valEnd <= startPos)    // we expect non-empty arguments
            return (-11);

        final CharSequence    ptType=cs.subSequence(startPos, valEnd);
        if (INET_WPAT_TYPE == ptChar)
            id.setKey(ptType.toString());
        else
            id.setValue(ptType.toString());

        return valEnd;
    }
    /**
     * Matches the welcome line with the given pattern and initializes the information object (if successful)
     * @param wl welcome line to be matched
     * @param startPos start position in welcome line where matching should start
     * @param wlLen number of characters available for parsing
     * @param pattern pattern to match against
     * @return If successful - server identity object as a {@link java.util.Map.Entry} whose
     * key=type and value=version (null otherwise)
     */
    public static final Map.Entry<String,String> matchServerIdentity (final CharSequence    wl,
                                                                      final int                startPos,
                                                                      final int                wlLen,
                                                                      final CharSequence    pattern)
    {
        final int    ptLen=(null == pattern) ? 0 : pattern.length(),
                    maxPos=startPos + wlLen;
        if ((null == wl) || (startPos < 0) || (wlLen <= 0) || (ptLen <= 0) || (maxPos > wl.length()))
            return null;

        StringPairEntry    id=null;
        for (int    wlPos=startPos, ptPos=0; (wlPos < maxPos) && (ptPos < ptLen); ptPos++)
        {
            /* ignore any spaces in the pattern or the welcome line */
            {
                final int    newWlPos=ParsableString.findNonEmptyDataStart(wl, wlPos, maxPos);
                if (newWlPos < wlPos)
                    wlPos = maxPos;
                else
                    wlPos = newWlPos;
            }
            {
                final int    newPtPos=ParsableString.findNonEmptyDataStart(pattern, ptPos, ptLen);
                if (newPtPos < ptPos)
                    ptPos = ptLen;
                else
                    ptPos = newPtPos;
            }

            /* if either position exhausted then no need to check any further */
            if ((wlPos >= maxPos) || (ptPos >= ptLen))
                break;

            // check if pattern modifier
            char    ptChar=pattern.charAt(ptPos);
            if (INET_WPAT_MODIFIER != ptChar)
            {
                // make sure same character in welcome line as in pattern
                if (wl.charAt(wlPos) != ptChar)
                    return null;

                wlPos++;
                continue;
            }

            ptPos++;    // skip pattern modifier

            if (ptPos >= ptLen)
                break;

            ptChar = pattern.charAt(ptPos);
            switch(ptChar)
            {
                case INET_WPAT_SKIPTODELIM:
                    ptPos++;
                    ptChar = pattern.charAt(ptPos);

                    if ((ptPos >= ptLen) || (' ' == ptChar))
                        return null;

                    if ((wlPos=ParsableString.indexOf(wl, ptChar, wlPos, maxPos)) < 0)
                        return null;

                    /* fall through to normal comparison ... */

                case INET_WPAT_MODIFIER    : /* handles '%%' as well */
                    if (ptChar != wl.charAt(wlPos))
                        return null;

                    wlPos++;
                    break;

                case INET_WPAT_RFC822DATE:
                    {
                        // find end of RFC822 date/time by searching for the GMT offset
                        int    dtEnd=ParsableString.indexOf(wl, '+', wlPos, maxPos);
                        if ((dtEnd < wlPos) || (dtEnd >= maxPos))
                        {
                            dtEnd = ParsableString.indexOf(wl, '-', wlPos, maxPos);
                            if ((dtEnd < wlPos) || (dtEnd >= maxPos))
                                return null;
                        }

                        /* NOTE !!! we should actually make sure that there is a time value encoded...
                         *         for now, we limit ourselves to checking that it is a 4-digit GMT offset
                         */
                        for (dtEnd++, wlPos=dtEnd; wlPos < maxPos; wlPos++)
                        {
                            final char    ch=wl.charAt(wlPos);
                            if ((ch < '0') || (ch > '9'))
                                break;
                        }
                        if ((wlPos - dtEnd) != 4)    // make sure EXACTLY 4 digits have been read
                            return null;

                        // skip to check if there is a timezone comment
                        dtEnd = ParsableString.findNonEmptyDataStart(wl, wlPos, maxPos);
                        if ((dtEnd >= wlPos) && (dtEnd < maxPos))
                            wlPos = dtEnd;
                        else
                            wlPos = maxPos;

                        // if there is a timezone comment then skip it
                        if ((wlPos < maxPos) && ('(' == wl.charAt(wlPos)))
                        {
                            wlPos = ParsableString.indexOf(wl, ')', wlPos, maxPos);
                            if ((wlPos <= 0) || (wlPos >= maxPos))
                                return null;

                            wlPos++;
                        }
                    }
                    break;

                case INET_WPAT_IGNORE        :
                    /* check if ignore rest of welcome line */
                    if ((ptPos + 1) >= ptLen)
                        break;

                    /* ignore current alpha string */
                    if ((wlPos=inetSkipWelcomeArgument(wl, wlPos, maxPos)) < 0)
                        return null;
                    break;

                case INET_WPAT_TYPE            :
                case INET_WPAT_VERSION        :
                    /* check if have type/version override */
                    if (((ptPos+1) < ptLen) && ('=' == pattern.charAt(ptPos+1)))
                    {
                        ptPos += 2;

                        if ((ptPos >= ptLen) || (' ' == pattern.charAt(ptPos)))
                            return null;

                        if (null == id)
                            id = new StringPairEntry();

                        if ((ptPos=extractServerIdentityInfo(pattern, ptPos, ptLen, ptChar, id)) < 0)
                            return null;

                        ptPos--;    // compensate for automatic increment by loop
                    }
                    else
                    {
                        if (null == id)
                            id = new StringPairEntry();

                        if ((wlPos=extractServerIdentityInfo(wl, wlPos, maxPos, ptChar, id)) < 0)
                            return null;
                    }
                    break;

                default                            :    // this point is reached for unknown modifier
                    return null;
            }    // end of handling modifier
        }    // end of scanning

        if ((null == id) || id.isEmpty())
            return null;

        return id;
    }
    /**
     * Analyzes the welcome line and determines the server type and version (if possible)
     * @param wl original welcome line received from server
     * @param startPos start position in welcome line to start matching
     * @param len number of characters available for analysis
     * @param patterns analysis patterns specifications
     * @return If successful - server identity object as a {@link java.util.Map.Entry} whose
     * key=type and value=version (null otherwise)
     */
    public static final Map.Entry<String,String> getServerIdentity (final CharSequence            wl,
                                                                       final int                    startPos,
                                                                       final int                    len,
                                                                       final Collection<String>    patterns)
    {
        if ((null == patterns) || (patterns.size() <= 0))
            return null;

        for (final String p : patterns)
        {
            final Map.Entry<String,String>    id=matchServerIdentity(wl, startPos, len, p);
            if (id != null)
                return id;
        }

        // this point is reached if no match found for the supplied patterns
        return null;
    }
    /**
     * @return A {@link Collection} of known patterns against which to check the welcome line (may be null/empty)
     */
    public abstract Collection<String> getKnownPatterns ();
    /**
     * Checks where the data that can be matched against a pattern begins, and returns an index AFTER this prefix.
     * @param welcomeLine line to be checked
     * @return index of next character in sequence to be matched for the pattern (<0 if error)
     */
    public abstract int getWelcomePatternMatchStart (final CharSequence welcomeLine);
    /**
     * Analyzes the welcome line and determines the server type and version (if possible)
     * @param wl original welcome line received from server
     * @param startPos start position in welcome line to start matching
     * @param len number of characters available for analysis
     * @return If successful - server identity object as a {@link java.util.Map.Entry} whose
     * key=type and value=version (null otherwise). <B>Note:</B> either type or value
     * may be null/empty but not both
     */
    public Map.Entry<String,String> getServerIdentity (final CharSequence wl, final int startPos, final int len)
    {
        return getServerIdentity(wl, startPos, len, getKnownPatterns());
    }
    /**
     * Analyzes the welcome line and determines the server type and version (if possible)
     * @param wl original welcome line received from server
     * @param patterns A {@link Collection} of patterns to check
     * @return If successful - server identity object as a {@link java.util.Map.Entry} whose
     * key=type and value=version (null otherwise). <B>Note:</B> either type or value
     * may be null/empty but not both
     */
    public Map.Entry<String,String> getServerIdentity (final CharSequence wl, final Collection<String> patterns)
    {
        final int    wLen=(null == wl) ? 0 : wl.length(),
                    startPos=(wLen <= 0) ? (-1) : getWelcomePatternMatchStart(wl);
        if (startPos < 0)
            return null;

        return getServerIdentity(wl, startPos, (wLen - startPos), patterns);
    }
    /**
     * Analyzes the welcome line and determines the server type and version (if possible)
     * @param wl original welcome line received from server
     * @return If successful - server identity object as a {@link java.util.Map.Entry} whose
     * key=type and value=version (null otherwise). <B>Note:</B> either type or value
     * may be null/empty but not both
     */
    public Map.Entry<String,String> getServerIdentity (final CharSequence wl)
    {
        return getServerIdentity(wl, getKnownPatterns());
    }
}
