/*
 *
 */
package net.community.chest.net.snmp;

import java.io.EOFException;
import java.io.IOException;
import java.io.StreamCorruptedException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

import net.community.chest.ParsableString;
import net.community.chest.dom.DOMUtils;
import net.community.chest.io.input.CharSequenceReader;
import net.community.chest.io.input.TokensReader;
import net.community.chest.lang.ExceptionUtil;
import net.community.chest.util.map.MapEntryImpl;

import org.w3c.dom.Element;

/**
 * <P>Copyright GPLv2</P>
 *
 * <P>A (@link Map) of OID(s) aliases - key=alias (case insensitive)
 * value=OID in dot notation</P>
 *
 * @author Lyor G.
 * @since May 25, 2009 1:53:12 PM
 */
public class OIDAliasMap extends TreeMap<String,String> {
    /**
     *
     */
    private static final long serialVersionUID = -3278208799789076425L;

    // as taken from SNMPv2-SMI mib
    public static final Collection<? extends Map.Entry<String,String>>    DEFAULT_ALIASES=
        Collections.unmodifiableList(
                Arrays.asList(new MapEntryImpl<String,String>("iso",             "{ 1 }"),
                              new MapEntryImpl<String,String>("org",             "{ iso 3 }"),
                              new MapEntryImpl<String,String>("dod",             "{ org 6 }"),
                              new MapEntryImpl<String,String>("internet",         "{ dod 1 }"),
                              new MapEntryImpl<String,String>("directory",         "{ internet 1 }"),
                              new MapEntryImpl<String,String>("mgmt",             "{ internet 2 }"),
                              new MapEntryImpl<String,String>("mib-2",             "{ mgmt 1 }"),
                              new MapEntryImpl<String,String>("transmission",     "{ mib-2 10 }"),
                              new MapEntryImpl<String,String>("experimental",     "{ internet 3 }"),
                              new MapEntryImpl<String,String>("private",         "{ internet 4 }"),
                              new MapEntryImpl<String,String>("enterprises",     "{ private 1 }"),
                              new MapEntryImpl<String,String>("security",         "{ internet 5 }"),
                              new MapEntryImpl<String,String>("snmpV2",         "{ internet 6 }"),
                              new MapEntryImpl<String,String>("snmpDomains",     "{ snmpV2 1 }"),
                              new MapEntryImpl<String,String>("snmpProxys",     "{ snmpV2 2 }"),
                              new MapEntryImpl<String,String>("snmpModules",     "{ snmpV2 3 }")
                        )
                );
    public static final <M extends OIDAliasMap> M addDefaultAliases (final M m)
    {
        if (null == m)
            return m;

        for (final Map.Entry<String,String> dp : DEFAULT_ALIASES)
        {
            if (null == dp)
                continue;

            final String    alias=dp.getKey();
            if (m.containsKey(alias))
                continue;

            try
            {
                m.readIdentifiers(dp.getKey(), dp.getValue());
            }
            catch(IOException e)    // should not happen
            {
                throw ExceptionUtil.toRuntimeException(e);
            }
        }

        return m;
    }

    private static OIDAliasMap    _defaults;
    public static final synchronized OIDAliasMap getDefaultAliases ()
    {
        if (null == _defaults)
            _defaults = addDefaultAliases(new OIDAliasMap(false));
        return _defaults;
    }

    public OIDAliasMap (boolean addDefaults)
    {
        super(String.CASE_INSENSITIVE_ORDER);

        if (addDefaults)
            addDefaultAliases(this);
    }

    public OIDAliasMap ()
    {
        this(true);
    }
    /**
     * @param alias alias name (case insensitive) - may NOT be null/empty
     * @param oid OID value - may NOT be null/empty
     * @return previous mapping (null if none)
     * @throws IllegalArgumentException if null/empty alias/OID or bad/illegal
     * OID dot-notation (<B>Note:</B> OID may NOT start/end with dot)
     * @see java.util.TreeMap#put(java.lang.Object, java.lang.Object)
     */
    @Override
    public String put (final String alias, final String oid)
        throws IllegalArgumentException
    {
        if ((null == alias) || (alias.length() <= 0))
            throw new IllegalArgumentException("put(" + alias + ")[" + oid + "] bad/illegal alias");

        final int    oidLen=(null == oid) ? 0 : oid.length();
        if (oidLen <= 0)
            throw new IllegalArgumentException("put(" + alias + ")[" + oid + "] bad/illegal OID");

        return super.put(alias, oid);
    }
    /**
     * @param alias alias to be mapped - may NOT be null/empty
     * @param oid OID to be mapped - may NOT be null/empty
     * @throws IllegalArgumentException if bad/illegal alias/OID
     * @throws IllegalStateException if previous mapping exists but with
     * other OID
     */
    public void putUnique (final String alias, final String oid)
        throws IllegalArgumentException, IllegalStateException
    {
        final String    prev=put(alias, oid);
        if ((prev != null) && (!prev.equals(oid)))
            throw new IllegalStateException("putUnique(" + alias + ") mismatched (" + oid + " <> " + prev + ") OID(s)");
    }

    public static final char    OBJIDPATH_START='{', OBJIDPATH_END='}';
    /**
     * @param rootAlias alias whose OID we want to read and resolve - may
     * NOT be null/empty
     * @param tkr (@link TokenReader) to use for reading identifiers. It
     * is assumed to be positioned <U>before</U> the curly brackets start
     * (see example below) - may NOT be null
     * @param tkData work buffer to be used - may NOT be null
     * @return number of mapped aliases (may be >0 if can infer several
     * aliases from a single list - e.g., if "internet OBJECT IDENTIFIER ::= { iso org(3) dod(6) 1 }"
     * then we can also infer that "org" is "iso.3" and that "dod" is "org.6")
     * @throws IOException if cannot read or a referred alias is not already
     * mapped (e.g., in the above example is is assumed that the "iso"
     * alias has been mapped)
     */
    public int readIdentifiers (final String rootAlias, final TokensReader tkr, final char[] tkData) throws IOException
    {
        if ((null == tkr) || (null == rootAlias) || (rootAlias.length() <= 0)
         || (null == tkData) || (tkData.length <= 1))
            throw new StreamCorruptedException("readIdentifiers(" + rootAlias + ") no alias/tokens reader/data buffer instance");

        // read first token and make sure it is curly brackets start
        {
            int    readLen=tkr.readToken(tkData);
            if ((readLen != 1) || (tkData[0] != OBJIDPATH_START))
                throw new StreamCorruptedException("readIdentifiers(" + rootAlias + ") unexpected token (" + ((readLen > 0) ? new String(tkData, 0, readLen) : String.valueOf(readLen)) + ") while reading list start");
        }

        // used to accumulate the OID components
        final StringBuilder    sb=new StringBuilder(MAX_OID_ALIAS_NAME);
        for (int readLen=tkr.readToken(tkData), aCount=0; readLen > 0; readLen=tkr.readToken(tkData))
        {
            // check if reached end of list
            if ((1 == readLen) && (OBJIDPATH_END == tkData[0]))
            {
                // make sure some identifier(s) read
                if (sb.length() <= 0)
                    throw new EOFException("readIdentifiers(" + rootAlias + ") no identifiers read in between");

                final String    oidPart=sb.toString();
                try
                {
                    putUnique(rootAlias, oidPart);
                }
                catch(RuntimeException e)
                {
                    throw new StreamCorruptedException("readIdentifiers(" + rootAlias + ") " + e.getClass().getName() + " while map OID=" + oidPart + ": " + e.getMessage());
                }

                return (aCount + 1);
            }


            final String    tkValue=new String(tkData, 0, readLen);
            String            numPart=null, aliasPart=null, subNumber=null;
            if (!ParsableString.isUnsignedNumber(tkData, 0, readLen))
            // check if this is a pure alias or one with a number spec.
            {
                for (int    dIndex=0; dIndex < readLen; dIndex++)
                {
                    // alias with a sub-number
                    if ('(' == tkData[dIndex])
                    {
                        aliasPart = new String(tkData, 0, dIndex);
                        if (aliasPart.length() <= 0)    // not allowed
                            throw new StreamCorruptedException("readIdentifiers(" + rootAlias + ") bad/illegal (sub-)alias name: " + aliasPart);

                        // extract number sub-part (and make sure it is a number and correctly delimited)
                        if ((tkData[readLen-1] != ')')
                         || (!ParsableString.isUnsignedNumber(tkData, dIndex+1, readLen-1)))
                            throw new StreamCorruptedException("readIdentifiers(" + rootAlias + ") bad/illegal (sub-)number value: " + tkValue);

                        // MUST be set since this is an alias with a sub-number
                        numPart = new String(tkData, dIndex+1, readLen - dIndex - 2);
                        if (numPart.length() <= 0)
                            throw new StreamCorruptedException("readIdentifiers(" + rootAlias + ") bad/illegal sub-number for ID=" + tkValue);

                        subNumber = numPart;
                        break;
                    }
                }

                if (null == aliasPart)    // if not set then this is a "pure" alias
                {
                    if (sb.length() > 0)    // "pure" aliases allowed only as FIRST component
                        throw new StreamCorruptedException("readIdentifiers(" + rootAlias + ") sub-numbered alias (" + tkValue + ") not allowed in mid-list");
                    aliasPart = tkValue;
                }

                // if not set then this is a "pure" alias
                if (null == numPart)
                {
                    // make sure pure alias OID known
                    if ((null == (numPart=get(aliasPart))) || (numPart.length() <= 0))
                        throw new StreamCorruptedException("readIdentifiers(" + rootAlias + ") no OID for (sub-)alias=" + aliasPart);
                }
            }
            else
                numPart = tkValue;

            if (sb.length() > 0)
                sb.append('.');
            sb.append(numPart);

            if ((aliasPart != null) && (subNumber != null))
            {
                final String    oidPart=sb.toString();
                try
                {
                    putUnique(aliasPart, oidPart);
                    aCount++;
                }
                catch(RuntimeException e)
                {
                    throw new StreamCorruptedException("readIdentifiers(" + rootAlias + ") " + e.getClass().getName() + " while map sub-alias (" + aliasPart + "[" + oidPart + "]): " + e.getMessage());
                }
            }
        }

        // this point is reached if no curly brackets end found
        throw new EOFException("readIdentifiers(" + rootAlias + ") EOF reached before end of identifiers found");
    }
    /**
     * Max. expected OID alias name in (@link #readIdentifiers(TokensReader))
     */
    public static final int    MAX_OID_ALIAS_NAME=32;

    private char[]    _readBuffer;
    protected char[] getReadBuffer ()
    {
        if (null == _readBuffer)
            _readBuffer = new char[MAX_OID_ALIAS_NAME];
        return _readBuffer;
    }
    /**
     * @param rootAlias alias whose OID we want to read and resolve - may
     * NOT be null/empty
     * @param tkr (@link TokenReader) to use for reading identifiers. It
     * is assumed to be positioned <U>before</U> the curly brackets start
     * (see example below) - may NOT be null
     * @return number of mapped aliases (may be >0 if can infer several
     * aliases from a single list - e.g., if "internet OBJECT IDENTIFIER ::= { iso org(3) dod(6) 1 }"
     * then we can also infer that "org" is "iso.3" and that "dod" is "org.6")
     * @throws IOException if cannot read or a referred alias is not already
     * mapped (e.g., in the above example is is assumed that the "iso"
     * alias has been mapped)
     */
    public int readIdentifiers (final String rootAlias, final TokensReader tkr) throws IOException
    {
        return readIdentifiers(rootAlias, tkr, getReadBuffer());
    }
    /**
     * @param rootAlias alias whose OID we want to read and resolve - may
     * NOT be null/empty
     * @param idents The (@link CharSequence) to use for reading identifiers.
     * It is assumed to contain the curly brackets start as 1st non-whitespace
     * character (see example below) - may NOT be null/empty
     * @return number of mapped aliases (may be >0 if can infer several
     * aliases from a single list - e.g., if "internet OBJECT IDENTIFIER ::= { iso org(3) dod(6) 1 }"
     * then we can also infer that "org" is "iso.3" and that "dod" is "org.6")
     * @throws IOException if cannot read or a referred alias is not already
     * mapped (e.g., in the above example is is assumed that the "iso"
     * alias has been mapped)
     */
    public int readIdentifiers (final String rootAlias, final CharSequence idents) throws IOException
    {
        if ((null == idents) || (idents.length() <= 0))
            throw new EOFException("readIdentifiers(" + rootAlias + ") no identifiers");

        try(TokensReader    tkr=new TokensReader(new CharSequenceReader(idents), true)) {
            return readIdentifiers(rootAlias, tkr);
        }
    }
    /**
     * XML (@link Element) attribute to be used to specify alias name
     */
    public static final String OIDALIAS_XML_ATTR_NAME="alias";
    /**
     * @param elem <P>XML (@link Element) containing an identifier definition
     * (may NOT be null). Format:</P></BR>
     *         <identifier alias="...">{ ...OID... }</identifier>
     * <P>Where <I>OID</I> format is same as in SMI "OBJECT IDENTIFIER" value</P?
     * @return number of mapped aliases (may be >0 if can infer several
     * aliases from a single list - e.g., if "internet OBJECT IDENTIFIER ::= { iso org(3) dod(6) 1 }"
     * then we can also infer that "org" is "iso.3" and that "dod" is "org.6")
     * @throws IOException if bad/illegal format/value(s)
     */
    public int readIdentifiers (final Element elem) throws IOException
    {
        try
        {
            final String    rootAlias=elem.getAttribute(OIDALIAS_XML_ATTR_NAME),
                            eValue=DOMUtils.getElementStringValue(elem);

            return readIdentifiers(rootAlias, eValue);
        }
        catch(Exception e)
        {
            throw new IOException("readIdentifiers(" + DOMUtils.toString(elem) + ") " + e.getClass().getName() + ": " + e.getMessage(), e);
        }
    }
    /**
     * Translates a {@link Map} where key=OID,value=alias to one where key=alias
     * and value=OID
     * @param oidMap Original {@link Map} - key=OID,value=alias
     * @return Translated {@link Map} key=alias, value=OID
     * @throws IllegalStateException if same OID has more than one alias
     */
    public static final OIDAliasMap toAliasMap (final Map<String,String> oidMap) throws IllegalStateException
    {
        final Collection<? extends Map.Entry<String,String>>    ol=
            ((null == oidMap) || (oidMap.size() <= 0)) ? null : oidMap.entrySet();
        if ((null == ol) || (ol.size() <= 0))
            return null;

        final OIDAliasMap    ret=new OIDAliasMap(false);
        for (final Map.Entry<String,String> oe : ol)
        {
            final String    oid=(null == oe) ? null : oe.getKey(),
                            alias=(null == oe) ? null : oe.getValue();
            if ((null == oid) || (oid.length() <= 0)
             || (null == alias) || (alias.length() <= 0))
                continue;

            ret.putUnique(alias, oid);
        }

        return ret;
    }
    /**
     * Attempts to build an object identifier format for the provided OID
     * using the aliases {@link Map} - e.g., <code>{ iso(1) org(3) }</code>.
     * If it fails to find an alias then it uses the raw number - e.g.
     * <code>{ iso(1) org(3) 4 private(6) }</code>
     * @param <A> The {@link Appendable} type
     * @param sb The {@link Appendable} instance to use - may NOT be null
     * @param oid The OID value - if null/empty then nothing is appended
     * @param oidMap The {@link Map} to use to translate OID(s) into aliases
     * (i.e., key=OID, value=alias). If null/empty the only the raw OID will
     * be generated - e.g. <code>{ 1 3 4 6 1 }</code>
     * @return The same as input {@link Appendable} instance
     * @throws IOException If failed to append the data or bad OID format
     */
    public static final <A extends Appendable> A appendOIDPath (final A sb, final String oid, final Map<String,String> oidMap) throws IOException
    {
        if (null == sb)
            throw new EOFException("appendOIDPath(" + oid + ") no " + Appendable.class.getSimpleName() + " instance");

        final int    oidLen=(null == oid) ? 0 : oid.length();
        if (oidLen <= 0)
            return sb;

        sb.append(OBJIDPATH_START);
        for (int curPos=0; curPos < oidLen; )
        {
            final int    nextPos=oid.indexOf('.', curPos);
            if (nextPos == curPos)
                throw new StreamCorruptedException("appendOIDPath(" + oid + ") bad format");

            final String    oidPart=
                (nextPos > curPos) ? oid.substring(0, nextPos) : oid,
                            prevComp=
                (nextPos > curPos) ? oid.substring(curPos, nextPos) : oid.substring(curPos),
                            aliasPart=
                ((null == oidMap) || (oidMap.size() <= 0)) ? null : oidMap.get(oidPart);

            sb.append(' ');    // separate from previous
            if ((aliasPart != null) && (aliasPart.length() > 0))
                sb.append(aliasPart)
                  .append('(')
                  .append(prevComp)
                  .append(')')
                  ;
            else
                sb.append(prevComp);

            if (nextPos < curPos)
                break;

            curPos = nextPos + 1;
        }

        sb.append(' ')
          .append(OBJIDPATH_END)
          ;
        return sb;
    }
    /**
     * Attempts to build an object identifier format for the provided OID
     * using the aliases {@link Map} - e.g., <code>{ iso(1) org(3) }</code>.
     * If it fails to find an alias then it uses the raw number - e.g.
     * <code>{ iso(1) org(3) 4 private(6) }</code>
     * @param oid The OID value - if null/empty then nothing is appended
     * @param oidMap The {@link Map} to use to translate OID(s) into aliases
     * (i.e., key=OID, value=alias)
     * @return The same as input {@link Appendable} instance
     * @throws RuntimeException If failed to append the data or bad OID format
     */
    public static final String getOIDPath (final String oid, final Map<String,String> oidMap)
    {
        final int    oidLen=(null == oid) ? 0 : oid.length();
        if (oidLen <= 0)
            return null;

        try
        {
            return appendOIDPath(new StringBuilder(oidLen * 3), oid, oidMap).toString();
        }
        catch(IOException e)    // should not happen
        {
            throw ExceptionUtil.toRuntimeException(e);
        }
    }
}
