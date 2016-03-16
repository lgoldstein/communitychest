/*
 *
 */
package net.community.chest.net.snmp.io;

import java.io.Closeable;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StreamCorruptedException;
import java.nio.CharBuffer;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;
import java.util.TreeMap;

import net.community.chest.ParsableString;
import net.community.chest.io.FileUtil;
import net.community.chest.io.input.TokensReader;
import net.community.chest.net.snmp.MIBAttributeEntry;
import net.community.chest.net.snmp.MIBGroup;
import net.community.chest.net.snmp.OIDAliasMap;
import net.community.chest.net.snmp.SNMPProtocol;
import net.community.chest.util.collection.CollectionsUtils;

/**
 * <P>Copyright GPLv2</P>
 *
 * @author Lyor G.
 * @since May 25, 2009 2:04:01 PM
 */
public class MIBEntriesReader implements Closeable {
    private TokensReader    _tkReader    /* =null */;
    public TokensReader getTokensReader ()
    {
        return _tkReader;
    }

    public void setTokensReader (TokensReader tkr)
    {
        _tkReader = tkr;
    }
    /*
     * @see java.io.Closeable#close()
     */
    @Override
    public void close () throws IOException
    {
        final TokensReader    tkr=getTokensReader();
        if (tkr != null)
        {
            try
            {
                tkr.close();
            }
            finally
            {
                setTokensReader(null);
            }
        }
    }

    private OIDAliasMap    _aliasesMap;
    protected OIDAliasMap getAliases (boolean createIfNotExist)
    {
        if ((null == _aliasesMap) && createIfNotExist)
            _aliasesMap = new OIDAliasMap();
        return _aliasesMap;
    }

    public OIDAliasMap getAliases ()
    {
        return getAliases(false);
    }

    public void setAliases (OIDAliasMap m)
    {
        _aliasesMap = m;
    }
    /**
     * (@link Map) of text replacements - key=alias,value=replacement text
     */
    private Map<String,String>    _rt    /* =null */;
    protected Map<String,String> getTextReplacements (boolean createIfNotExist)
    {
        if ((null == _rt) && createIfNotExist)
            _rt = new TreeMap<String,String>(String.CASE_INSENSITIVE_ORDER);
        return _rt;
    }

    public Map<String,String> getTextReplacements ()
    {
        return getTextReplacements(false);
    }

    public void setTextReplacements (Map<String,String> rt)
    {
        _rt = rt;
    }

    private MIBDefinitionResolver    _resolver;
    protected MIBDefinitionResolver getResolver (boolean createIfNotExist)
    {
        if ((_resolver == null) && createIfNotExist)
            return DefaultMIBDefinitionResolver.INSTANCE;
        return _resolver;
    }

    public MIBDefinitionResolver getResolver ()
    {
        return _resolver;
    }

    public void setResolver (MIBDefinitionResolver resolver)
    {
        _resolver = resolver;
    }

    public MIBEntriesReader (OIDAliasMap aliases, TokensReader tkr, Map<String,String> txtReps, MIBDefinitionResolver resolver)
    {
        _tkReader = tkr;
        _aliasesMap = aliases;
        _rt = txtReps;
        _resolver = resolver;
    }

    public MIBEntriesReader (OIDAliasMap aliases, TokensReader tkr, Map<String,String> txtReps)
    {
        this(aliases, tkr, txtReps, null);
    }

    public MIBEntriesReader (OIDAliasMap aliases, TokensReader tkr)
    {
        this(aliases, tkr, null);
    }

    public MIBEntriesReader (OIDAliasMap aliases, Reader r, boolean realClose)
    {
        this(aliases, (null == r) ? null : new SNMPTokensReader(r, realClose));
    }

    public MIBEntriesReader (OIDAliasMap aliases, Reader r)
    {
        this(aliases, r, true);
    }

    public MIBEntriesReader ()
    {
        this(null, null, false);
    }

    public static final int readSkipComments (final TokensReader tkr, final char[] tkData) throws IOException
    {
        if (null == tkr)
            throw new EOFException("readSkipComments() no reader to read-skip tokens from");

        // we skip at most ~32K comments to avoid an infinite loop
        for (int    readLen=tkr.readToken(tkData), rIndex=0; rIndex < Short.MAX_VALUE; readLen=tkr.readToken(tkData), rIndex++)
        {
                // check if comment and ignore if so
            if (SNMPProtocol.isCommentToken(tkData, 0, readLen))
            {
                final long    sLen=tkr.skipLine();
                if (sLen <= 0L)
                    return (-1);

                continue;
            }

            return readLen;
        }

        throw new EOFException("unexpected comments read loop exit");
    }

    private char[]    _readBuffer;
    protected void setReadBuffer (char[] tkData)
    {
        _readBuffer = tkData;
    }

    protected char[] getReadBuffer ()
    {
        if (null == _readBuffer)
            _readBuffer = new char[128];
        return _readBuffer;
    }
    /**
     * @param expTokens expected tokens - according to expected order (!)
     * @return first non-matching token string - null if <U>all</U> tokens
     * found (match is case <U>insensitive</U>)
     * @throws IOException unable to read tokens
     */
    protected String skipTokens (final char[] ... expTokens) throws IOException
    {
        if ((null == expTokens) || (expTokens.length <= 0))
            return null;

        final char[]        tkData=getReadBuffer();
        final TokensReader    tkr=getTokensReader();
        for (final char[] expData : expTokens)
        {
            if ((null == expData) || (expData.length <= 0))
                continue;    // should not happen

            final int    readLen=readSkipComments(tkr, tkData);
            if (readLen <= 0)
                throw new EOFException("Premature EOF while expecting token=" + new String(expData));

            if (!ParsableString.compareTo(tkData, 0, readLen, expData, false))
                return new String(expData);
        }

        return null;
    }

    protected boolean skipTillToken (final char ... token) throws IOException
    {
        if ((null == token) || (token.length <= 0))
            return false;

        final char[]        tkData=getReadBuffer();
        final TokensReader    tkr=getTokensReader();
        // skip up to ~32K tokens at most to avoid infinite loop(s)
        for (int    rIndex=0, readLen=readSkipComments(tkr, tkData);
             rIndex < Short.MAX_VALUE;
             rIndex++, readLen=readSkipComments(tkr, tkData))
        {
            if (readLen <= 0)
                return false;

            if (ParsableString.compareTo(token, tkData, 0, readLen, false))
                return true;
        }

        throw new EOFException("skipTillToken(" + getMibName() + ")[" + new String(token) + "] too many tokens skipped");
    }

    protected boolean skipTillDelim (final char delim) throws IOException
    {
        final char[]        tkData=getReadBuffer();
        final TokensReader    tkr=getTokensReader();
        // skip up to ~32K tokens at most to avoid infinite loop(s)
        for (int    rIndex=0, readLen=readSkipComments(tkr, tkData);
             rIndex < Short.MAX_VALUE;
             rIndex++, readLen=readSkipComments(tkr, tkData))
        {
            if (readLen <= 0)
                return false;

            if (tkData[readLen-1] == delim)
                return true;
        }

        throw new EOFException("skipTillDelim(" + getMibName() + ")[" + String.valueOf(delim) + "] too many tokens skipped");
    }
    // return '\0' if no complement
    protected char getComplementedChar (final char startChar)
    {
        switch(startChar)
        {
            case '{'    : return '}';
            case '('    : return ')';
            case '['    : return ']';
            case '<'    : return '>';
            default        :
                return '\0';
        }
    }

    protected boolean isComplementedChar (final char startChar)
    {
        return getComplementedChar(startChar) != '\0';
    }

    protected boolean skipTillComplementChar (final char startChar) throws IOException
    {
        final char    stopChar=getComplementedChar(startChar);
        if ('\0' == stopChar)
            throw new StreamCorruptedException("skipTillComplementChar(" + getMibName() + ") unknown start char: " + String.valueOf(startChar));

        return skipTillDelim(stopChar);
    }

    private String    _mibName    /* =null */;
    public final String getMibName ()
    {
        return _mibName;
    }

    protected void startDefinitions (final String mibName) throws IOException
    {
        if ((null == mibName) || (mibName.length() <= 0))
            throw new StreamCorruptedException("no MIB name to start definitions");

        final String    prev=getMibName();
        if ((prev != null) && (!mibName.equalsIgnoreCase(prev)))
            throw new StreamCorruptedException("startDefinitions(" + mibName + ") alread set as: " + prev);

        // make sure we find the BEGIN token
        final String    modVal=skipTokens(SNMPProtocol.ASSIGNModChars, SNMPProtocol.BEGINModChars);
        if ((modVal != null) && (modVal.length() > 0))
            throw new StreamCorruptedException("startDefinitions(" + mibName + ") unexpected modifier: " + modVal);

        _mibName = mibName;
    }

    protected void readObjectIdentifier (final String rootAlias) throws IOException
    {
        final TokensReader    tkr=getTokensReader();
        if (null == tkr)
            throw new EOFException("readObjectIdentifier(" + rootAlias + ") no reader to read MIB OID(s) from");

        // skip till curly braces start
        {
            final String    modVal=skipTokens(SNMPProtocol.IDENTIFIERModChars, SNMPProtocol.ASSIGNModChars);
            if ((modVal != null) && (modVal.length() > 0))
                throw new StreamCorruptedException("readObjectIdentifier(" + rootAlias + ") unexpected modifier: " + modVal);
        }

        final OIDAliasMap    aMap=getAliases(true);
        if (null == aMap)    // should not happen
            throw new StreamCorruptedException("readObjectIdentifier(" + rootAlias + ") no OID(s) aliases map");

        aMap.readIdentifiers(rootAlias, tkr, getReadBuffer());
    }

    protected void readImports () throws IOException
    {
        final char[]        tkData=getReadBuffer();
        final TokensReader    tkr=getTokensReader();
        boolean                endFound=false;
        for (int    readLen=readSkipComments(tkr, tkData);
             readLen > 0;
             readLen=readSkipComments(tkr, tkData))
        {
            if ((1 == readLen) && (';' == tkData[0]))
                return;    // check if read only the end delimiter

            if (ParsableString.compareTo(tkData, 0, readLen, SNMPProtocol.FROMModChars, false))
            {
                if ((readLen=readSkipComments(tkr, tkData)) <= 0)
                    throw new StreamCorruptedException("readImports(" + getMibName() + ") missing " + SNMPProtocol.FROMMod + " import specification");

                if (tkData[readLen - 1] == ';')
                {
                    readLen--;
                    if (readLen <= 0)
                        throw new StreamCorruptedException("readImports(" + getMibName() + ") missing IMPORT FROM specification argument");
                    endFound = true;
                }

                final String    mibName=new String(tkData, 0, readLen);
                readImports(mibName);
                if (endFound)
                    return;
            }
        }

        throw new EOFException("readImports(" + getMibName() + ") no end of imports found");
    }

    private static final Collection<MIBAttributeEntry>    DUMMY_ENTRIES=CollectionsUtils.ignoringCollection();
    protected void readImports (final String mibName) throws IOException
    {
        if ("SNMPv2-SMI".equalsIgnoreCase(mibName))
            return;    // TODO fix the problem with parsing this MIB

        final MIBDefinitionResolver    res=getResolver(true);
        try
        {
            Reader                impStream=new InputStreamReader(res.openMIB(mibName));
            MIBEntriesReader    impReader=new MIBEntriesReader(getAliases(true),
                                                               new SNMPTokensReader(impStream, true),
                                                               getTextReplacements(true),
                                                               res);
            impReader.setReadBuffer(getReadBuffer());
            try
            {
                impReader.readMIBEntries(DUMMY_ENTRIES);
            }
            finally
            {
                FileUtil.closeAll(impStream, impReader);
            }
        }
        catch(IOException e)
        {
            throw new IOException("readImports(" + getMibName() + ") failed (" + e.getClass().getName() + ") to import mib=" + mibName + ": " + e.getMessage(), e);
        }
    }

    protected MIBAttributeEntry skipParantheses (final MIBAttributeEntry e, final int sIndex, final int sLen) throws IOException
    {
        final String    entryName=(null == e) ? null : e.getAttrName();
        if ((null == entryName) || (entryName.length() <= 0))
            throw new StreamCorruptedException("skipParantheses(" + getMibName() + ") no entry instance");

        // limit to ~127 arguments being parsed to avoid infinite loops
        final char[]        tkData=getReadBuffer();
        final TokensReader    tkr=getTokensReader();
        for (int    readLen=sLen, nDepth=1, rIndex=0;
             (readLen > 0) && (rIndex < Byte.MAX_VALUE);
             readLen=readSkipComments(tkr, tkData), rIndex++)
        {
            for (int    dIndex=(rIndex > 0) ? 0 : sIndex; dIndex < readLen; dIndex++)
            {
                final char    tch=tkData[dIndex];
                if ('(' == tch)
                    nDepth++;
                else if (')' == tch)
                    nDepth--;
            }

            if (0 == nDepth)
                return e;
        }

        throw new EOFException("skipParantheses(" + getMibName() + "[" + entryName + "]) incomplete definition");
    }

    protected MIBAttributeEntry readOctetStringSyntax (final MIBAttributeEntry e) throws IOException
    {
        final String    entryName=(null == e) ? null : e.getAttrName();
        if ((null == entryName) || (entryName.length() <= 0))
            throw new StreamCorruptedException("readOctetStringSyntax(" + getMibName() + ") no entry instance");
        e.setSyntax(SNMPProtocol.OCTETSTRINGMod);    // obviously, since reached this method

        // limit to ~127 arguments being parsed to avoid infinite loops
        final char[]        tkData=getReadBuffer();
        final TokensReader    tkr=getTokensReader();
        final int            readLen=readSkipComments(tkr, tkData);
        if (readLen > 0)
        {
            // TODO parse it more strictly rather than count parenthesis
            for (int    dIndex=0; dIndex < readLen; dIndex++)
            {
                final char    tch=tkData[dIndex];
                if ('(' == tch)
                    return skipParantheses(e, dIndex+1, readLen);
            }

            return e;
        }

        throw new EOFException("readOctetStringSyntax(" + getMibName() + "[" + entryName + "]) incomplete definition");
    }

    protected MIBAttributeEntry readOctetSyntax (final MIBAttributeEntry e) throws IOException
    {
        final String    entryName=(null == e) ? null : e.getAttrName();
        if ((null == entryName) || (entryName.length() <= 0))
            throw new StreamCorruptedException("readOctetSyntax(" + getMibName() + ") no entry instance");

        // we read at most ~127 OCTET syntax type(s)
        final char[]        tkData=getReadBuffer();
        final TokensReader    tkr=getTokensReader();
        final int            readLen=readSkipComments(tkr, tkData);
        if (readLen > 0)
        {
            if (ParsableString.compareTo(tkData, 0, readLen, SNMPProtocol.STRINGModChars, false))
                return readOctetStringSyntax(e);

            throw new StreamCorruptedException("readOctetSyntax(" + getMibName() + ") unknown sub-syntax: " + new String(tkData, 0, readLen));
        }

        throw new EOFException("readOctetSyntax(" + getMibName() + "[" + entryName + "]) incomplete definition");
    }

    protected MIBAttributeEntry readSequenceSyntax (final MIBAttributeEntry e) throws IOException
    {
        final String    entryName=(null == e) ? null : e.getAttrName();
        if ((null == entryName) || (entryName.length() <= 0))
            throw new StreamCorruptedException("readSequenceSyntax(" + getMibName() + ") no entry instance");

        final char[]        tkData=getReadBuffer();
        final TokensReader    tkr=getTokensReader();
        int                    readLen=readSkipComments(tkr, tkData);
        if (readLen <= 0)
            throw new EOFException("readSequenceSyntax(" + getMibName() + "[" + entryName + "]) missing OF keyword");
        if (!ParsableString.compareTo(tkData, 0, readLen, SNMPProtocol.OFModChars, false))
            throw new StreamCorruptedException("readSequenceSyntax(" + getMibName() + "[" + entryName + "]) unexpected keyword: " + new String(tkData, 0, readLen));

        if ((readLen=readSkipComments(tkr, tkData)) <= 0)
            throw new EOFException("readSequenceSyntax(" + getMibName() + "[" + entryName + "]) missing OF what value");

        e.setSyntax(SNMPProtocol.SEQUENCEMod);
        e.addIndex(new String(tkData, 0, readLen));
        return e;
    }

    protected MIBAttributeEntry readObjectSyntax (final MIBAttributeEntry e) throws IOException
    {
        final String    entryName=(null == e) ? null : e.getAttrName();
        if ((null == entryName) || (entryName.length() <= 0))
            throw new StreamCorruptedException("readObjectSyntax(" + getMibName() + ") no entry instance");

        // we read at most ~127 OBJECT syntax sub-type(s)
        final char[]        tkData=getReadBuffer();
        final TokensReader    tkr=getTokensReader();
        final int            readLen=readSkipComments(tkr, tkData);
        if (readLen > 0)
        {
            if (ParsableString.compareTo(tkData, 0, readLen, SNMPProtocol.IDENTIFIERModChars, false))
            {
                e.setSyntax(SNMPProtocol.OBJECTIDENTIFIDERMod);
                return e;
            }

            throw new StreamCorruptedException("readObjectSyntax(" + getMibName() + ") unknown sub-syntax: " + new String(tkData, 0, readLen));
        }

        throw new EOFException("readObjectSyntax(" + getMibName() + "[" + entryName + "]) incomplete definition");
    }

    protected String resolveEntrySyntax (@SuppressWarnings("unused") final String entryName, final String stx)
    {
        final Map<String,String>    repMap=
            ((null == stx) || (stx.length() <= 0)) ? null : getTextReplacements();
        final String                repText=
            ((null == repMap) || (repMap.size() <= 0)) ? null : repMap.get(stx);
        if ((null == repText) || (repText.length() <= 0))
            return stx;
        // TODO use recursive resolution in case replace text also refers to an alias
        return repText;
    }

    protected MIBAttributeEntry readEntrySyntax (final MIBAttributeEntry e) throws IOException
    {
        final String    entryName=(null == e) ? null : e.getAttrName();
        if ((null == entryName) || (entryName.length() <= 0))
            throw new StreamCorruptedException("readEntrySyntax(" + getMibName() + ") no entry instance");

        final char[]        tkData=getReadBuffer();
        final TokensReader    tkr=getTokensReader();
        final int            readLen=readSkipComments(tkr, tkData);
        if (readLen > 0)
        {
            if (ParsableString.compareTo(tkData, 0, readLen, SNMPProtocol.OCTETModChars, false))
                return readOctetSyntax(e);
            else if (ParsableString.compareTo(tkData, 0, readLen, SNMPProtocol.SEQUENCEModChars, false))
                return readSequenceSyntax(e);
            else if (ParsableString.compareTo(tkData, 0, readLen, SNMPProtocol.OBJECTModChars, false))
                return readObjectSyntax(e);

            final String    stx=resolveEntrySyntax(entryName, new String(tkData, 0, readLen));
            if ((stx != null) && (stx.length() > 0))
                e.setSyntax(stx);

            return e;
        }

        throw new EOFException("readEntrySyntax(" + getMibName() + "[" + entryName + "]) incomplete definition");
    }

    protected MIBAttributeEntry readEntryAccess (final MIBAttributeEntry e) throws IOException
    {
        final String    entryName=(null == e) ? null : e.getAttrName();
        if ((null == entryName) || (entryName.length() <= 0))
            throw new StreamCorruptedException("readEntryAccess(" + getMibName() + ") no entry instance");

        final char[]        tkData=getReadBuffer();
        final TokensReader    tkr=getTokensReader();
        final int            readLen=readSkipComments(tkr, tkData);
        if (readLen <= 0)
            throw new EOFException("readEntryAccess(" + getMibName() + "[" + entryName + "]) no access");

        e.setAccess(new String(tkData, 0, readLen));
        return e;
    }

    protected MIBAttributeEntry readEntryStatus (final MIBAttributeEntry e) throws IOException
    {
        final String    entryName=(null == e) ? null : e.getAttrName();
        if ((null == entryName) || (entryName.length() <= 0))
            throw new StreamCorruptedException("readEntryStatus(" + getMibName() + ") no entry instance");

        final char[]        tkData=getReadBuffer();
        final TokensReader    tkr=getTokensReader();
        final int            readLen=readSkipComments(tkr, tkData);
        if (readLen <= 0)
            throw new EOFException("readEntryStatus(" + getMibName() + "[" + entryName + "]) no status");

        e.setStatus(new String(tkData, 0, readLen));
        return e;
    }
    // OK if null
    protected <A extends Appendable> A appendDescription (final String identifier /* only for exceptions */, final A sb) throws IOException
    {
        // limit to at most ~32K words of description to avoid infinite loops
        final char[]        tkData=getReadBuffer();
        final TokensReader    tkr=getTokensReader();
        for (int    readLen=readSkipComments(tkr, tkData), nDepth=0, rIndex=0;
             (readLen > 0) && (rIndex < Short.MAX_VALUE);
             readLen=readSkipComments(tkr, tkData), rIndex++)
        {
            // TODO parse it more correctly
            if ('"' == tkData[0])
                nDepth = 1 - nDepth;
            if ((readLen > 1) && ('"' == tkData[readLen-1]))
                nDepth = 1 - nDepth;

            if ((nDepth > 0) && (sb != null))
                sb.append(CharBuffer.wrap(tkData, 0, readLen));

            if (0 == nDepth)
                return sb;
        }

        throw new EOFException("appendDescription(" + getMibName() + "[" + identifier + "]) incomplete value");
    }

    protected void skipDescription (final String identifier) throws IOException
    {
        appendDescription(identifier, null);
    }

    protected MIBAttributeEntry readEntryDescription (final MIBAttributeEntry e) throws IOException
    {
        final String    entryName=(null == e) ? null : e.getAttrName();
        if ((null == entryName) || (entryName.length() <= 0))
            throw new StreamCorruptedException("readEntryDescription(" + getMibName() + ") no entry instance");

        skipDescription(entryName);    // TODO accumulate description
        return e;
    }

    protected MIBAttributeEntry readEntryOID (final MIBAttributeEntry e) throws IOException
    {
        final String    entryName=(null == e) ? null : e.getAttrName();
        if ((null == entryName) || (entryName.length() <= 0))
            throw new StreamCorruptedException("readEntryOID(" + getMibName() + ") no entry/name instance");

        final OIDAliasMap    aMap=getAliases(true);
        if (null == aMap)    // should not happen
            throw new StreamCorruptedException("readEntryOID(" + getMibName() + "[" + entryName + "]) no OID(s) aliases map");

        aMap.readIdentifiers(entryName, getTokensReader(), getReadBuffer());

        // retrieve entry OID
        final String    oid=aMap.get(entryName);
        if ((null == oid) || (oid.length() <= 0))
            throw new StreamCorruptedException("readEntryOID(" + getMibName() + "[" + entryName + "]) no OID in aliases map");

        e.setOid(oid);
        return e;
    }

    public static final Collection<String> addEnumValues (final Collection<String> eColl, final char[] tkData, final int startPos, final int readLen)
    {
        Collection<String>    vals=eColl;
        for (int curPos=startPos; curPos < readLen; curPos++)
        {
            final int    nStart=curPos;
            for ( ; curPos < readLen; curPos++)
            {
                final char    ch=tkData[curPos];
                if ((',' == ch) || ('}' == ch))
                    break;
            }

            final int        nLen=(curPos - nStart);
            if (nLen > 0)
            {
                final String    n=new String(tkData, nStart, nLen);
                if (null == vals)
                    vals = new LinkedList<String>();
                vals.add(n);
            }
        }

        return vals;
    }

    public Collection<String> readEnumeration (final String entryName, final Collection<String> eColl, final boolean retValues) throws IOException
    {
        if ((null == entryName) || (entryName.length() <= 0))
            throw new StreamCorruptedException("readEnumeration(" + getMibName() + ") no entry/name instance");

        Collection<String>    vals=eColl;
        // we limit the loop to ~32K values to avoid infinite loops
        final char[]        tkData=getReadBuffer();
        final TokensReader    tkr=getTokensReader();
        for (int    readLen=readSkipComments(tkr, tkData), rIndex=0;
             (readLen > 0) && (rIndex < Short.MAX_VALUE);
             readLen=readSkipComments(tkr, tkData), rIndex++)
        {
            if (retValues)
                vals = addEnumValues(vals, tkData, 0, readLen);

            // check if end of enumeration found
            if ('}' == tkData[readLen-1])
                return vals;
        }

        throw new EOFException("readEnumeration(" + getMibName() + "[" + entryName + "]) incomplete definition");
    }

    protected MIBAttributeEntry readEnumeration (final MIBAttributeEntry e) throws IOException
    {
        readEnumeration((null == e) ? null : e.getAttrName(), null, false);
        return e;
    }

    protected MIBAttributeEntry readEntryIndex (final MIBAttributeEntry e) throws IOException
    {
        final String    entryName=(null == e) ? null : e.getAttrName();
        if ((null == entryName) || (entryName.length() <= 0))
            throw new StreamCorruptedException("readEntryIndex(" + getMibName() + ") no entry/name instance");

        final char[]        tkData=getReadBuffer();
        final TokensReader    tkr=getTokensReader();
        int                    readLen=readSkipComments(tkr, tkData);
        if ((readLen < 1) || (tkData[0] != '{'))
            throw new EOFException("readEntryIndex(" + getMibName() + "[" + entryName + "]) no index start");

        Collection<String>    idx=addEnumValues(null, tkData, 1, readLen), odx=e.getIndices();
        if ((null == (idx=readEnumeration(entryName, idx, true))) || (idx.size() <= 0))    // MUST have an INDEX specification
            throw new StreamCorruptedException("readEntryIndex(" + getMibName() + "[" + entryName + "]) no index set");

        // if have previously set indices make sure same one re-set
        if ((odx != null) && (odx.size() > 0))
        {
            if (!MIBAttributeEntry.compareIndices(idx, odx))
                throw new StreamCorruptedException("readEntryIndex(" + getMibName() + "[" + entryName + "]) mismatched indices");
        }

        e.setIndices(idx);
        return e;
    }

    protected void handleUnknownEntryModifier (final MIBAttributeEntry    e, final char[] tkData, final int off, final int len) throws IOException
    {
        if (null == e)
            throw new StreamCorruptedException("handleUnknownEntryModifier(" + getMibName() + "[" + new String(tkData, off, len) + "]) no entry");

        skipDescription(e.getAttrName());
    }

    protected MIBAttributeEntry readAttributeEntry (final String entryName) throws IOException
    {
        if ((null == entryName) || (entryName.length() <= 0))
            throw new StreamCorruptedException("readAttributeEntry(" + getMibName() + "[" + entryName + "]) no entry name");

        final MIBAttributeEntry    e=new MIBAttributeEntry(entryName);
        final char[]            tkData=getReadBuffer();
        final TokensReader        tkr=getTokensReader();
        // allow at most ~127 entry specifiers to avoid infinite loops
        for (int    readLen=readSkipComments(tkr, tkData), rIndex=0;
             (readLen > 0) && (rIndex < Byte.MAX_VALUE);
             readLen=readSkipComments(tkr, tkData), rIndex++)
        {
            if (ParsableString.compareTo(tkData, 0, readLen, SNMPProtocol.SYNTAXModChars, false))
                readEntrySyntax(e);
            else if (ParsableString.compareTo(tkData, 0, readLen, SNMPProtocol.ACCESSModChars, false)
                  || ParsableString.compareTo(tkData, 0, readLen, SNMPProtocol.MAXACCESSModChars, false))
                readEntryAccess(e);
            else if (ParsableString.compareTo(tkData, 0, readLen, SNMPProtocol.STATUSModChars, false))
                readEntryStatus(e);
            else if (ParsableString.compareTo(tkData, 0, readLen, SNMPProtocol.DESCRIPTIONModChars, false))
                readEntryDescription(e);
            else if (ParsableString.compareTo(tkData, 0, readLen, SNMPProtocol.INDEXModChars, false))
                readEntryIndex(e);
            else if ((1 == readLen) && ('{' == tkData[0]))
                readEnumeration(e);
            else if (ParsableString.compareTo(tkData, 0, readLen, SNMPProtocol.ASSIGNModChars, false))
            {
                // check if have some syntax replacements
                final Map<String,String>    repText=getTextReplacements(false);
                final String                eStx=e.getSyntax(),
                                            mStx=
                    ((null == eStx) || (eStx.length() <= 0) || (null == repText) || (repText.size() <= 0)) ? null : repText.get(eStx),
                                            fStx=
                    ((null == mStx) || (mStx.length() <= 0)) ? eStx : mStx;
                if (fStx != eStx)    // override syntax if found a replacement
                    e.setSyntax(fStx);

                return readEntryOID(e);    // nothing expected after OID
            }
            // if found parenthesis assume some "un-consumed" range (TODO review this decision)
            else if ('(' == tkData[0])
                skipParantheses(e, 1, readLen);
            else
                handleUnknownEntryModifier(e, tkData, 0, readLen);
        }

        throw new EOFException("readAttributeEntry(" + getMibName() + "[" + entryName + "]) data exhausted before whole entry read");
    }

    public static final int readToken (final TokensReader tkr, final char[] tkData) throws IOException
    {
        if (null == tkr)
            throw new EOFException("No token(s) reader to read from");

        return tkr.readToken(tkData);
    }

    protected void readSequence (final String entryName) throws IOException
    {
        if ((null == entryName) || (entryName.length() <= 0))
            throw new StreamCorruptedException("readSequence(" + getMibName() + "[" + entryName + "]) no name");

        // limit the loop to ~32K members to avoid infinite loops
        final char[]        tkData=getReadBuffer();
        final TokensReader    tkr=getTokensReader();
        for (int    readLen=readSkipComments(tkr, tkData), rIndex=0;
             (readLen > 0) && (rIndex < Short.MAX_VALUE);
             readLen=readSkipComments(tkr, tkData), rIndex++)
        {
            if ((1 == readLen) && ('{' == tkData[0]))
            {
                readEnumeration(entryName, null, false);
                return;
            }
        }

        throw new EOFException("readSequence(" + getMibName() + "[" + entryName + "]) data exhausted before whole entry read");
    }

    protected MIBAttributeEntry readTextConvention (final String entryName) throws IOException
    {
        if ((null == entryName) || (entryName.length() <= 0))
            throw new StreamCorruptedException("readTextConvention(" + getMibName() + "[" + entryName + "]) no entry name");

        final MIBAttributeEntry    e=new MIBAttributeEntry(entryName);
        final char[]            tkData=getReadBuffer();
        final TokensReader        tkr=getTokensReader();
        // allow at most ~127 entry specifiers to avoid infinite loops
        for (int    readLen=readSkipComments(tkr, tkData), rIndex=0;
             (readLen > 0) && (rIndex < Byte.MAX_VALUE);
             readLen=readSkipComments(tkr, tkData), rIndex++)
        {
            if (ParsableString.compareTo(tkData, 0, readLen, SNMPProtocol.SYNTAXModChars, false))
            {
                readEntrySyntax(e);
                return e;    // TODO find some better stop criterion
            }
            else if (ParsableString.compareTo(tkData, 0, readLen, SNMPProtocol.STATUSModChars, false))
                readEntryStatus(e);
            else if (ParsableString.compareTo(tkData, 0, readLen, SNMPProtocol.DESCRIPTIONModChars, false))
                readEntryDescription(e);
        }

        throw new EOFException("readTextConvention(" + getMibName() + "[" + entryName + "]) data exhausted before whole entry read");
    }

    // returns read replacement
    protected String readTextReplacement (final String aliasName) throws IOException
    {
        if ((null == aliasName) || (aliasName.length() <= 0))
            throw new StreamCorruptedException("readTextReplacement(" + getMibName() + "[" + aliasName + "]) no alias name");

        final char[]        tkData=getReadBuffer();
        final TokensReader    tkr=getTokensReader();
        int                    readLen=readToken(tkr, tkData);
        if (readLen <= 0)
            throw new EOFException("readTextReplacement(" + getMibName() + "[" + aliasName + "]) premature EOF: " + readLen);

        if (ParsableString.compareTo(tkData, 0, readLen, SNMPProtocol.SEQUENCEModChars, false))
        {
            readSequence(aliasName);
            return null;
        }

        final String    aValue;
        if (ParsableString.compareTo(tkData, 0, readLen, SNMPProtocol.TEXTCONVENTIONModChars, false))
        {
            final MIBAttributeEntry e=readTextConvention(aliasName);
            aValue = e.getSyntax();
        }
        else
        {
            final StringBuilder    sb=new StringBuilder(readLen + 8);
            sb.append(tkData, 0, readLen);
            // we allow at most ~32K tokens to avoid infinite loop
            for (int    rIndex=0; rIndex < Short.MAX_VALUE; rIndex++)
            {
                if ((readLen=readToken(tkr, tkData)) <= 0)
                    break;    // OK if exhausted data

                if (SNMPProtocol.isCommentToken(tkData, 0, readLen))
                {
                    tkr.skipLine();
                    break;
                }

                sb.append(' ');    // separate from previous data
                sb.append(tkData, 0, readLen);
            }

            aValue = sb.toString();
        }

        final Map<String,String>    repText=getTextReplacements(true);
        final String                prev=repText.put(aliasName, aValue);
        if (prev != null)
        {
            // if previous value found then make sure same (case insensitive)
            if (!prev.equalsIgnoreCase(aValue))
                throw new StreamCorruptedException("readTextReplacement(" + getMibName() + "[" + aliasName + "]) mismatched replacements: old=" + prev + ";new=" + aValue);
        }

        return aValue;
    }

    protected void readModuleIdentity (final String modId) throws IOException
    {
        if ((null == modId) || (modId.length() <= 0))
            throw new StreamCorruptedException("readModuleIdentity(" + getMibName() + ") no module ID");

        if (!skipTillToken(SNMPProtocol.ASSIGNModChars))
            throw new StreamCorruptedException("readModuleIdentity(" + getMibName() + ")[" + modId + "] missing '" + SNMPProtocol.ASSIGNMod + "' separator");

        final OIDAliasMap    aMap=getAliases(true);
        if (null == aMap)    // should not happen
            throw new StreamCorruptedException("readModuleIdentity(" + getMibName() + ")[" + modId + "] no OID(s) aliases map");

        aMap.readIdentifiers(modId, getTokensReader(), getReadBuffer());
    }

    protected void readNotification (final String entryName, final String notifType) throws IOException
    {
        if ((null == notifType) || (notifType.length() <= 0))
            throw new StreamCorruptedException("readModuleIdentity(" + getMibName() + ")[" + entryName + "] no notification type");

        readModuleIdentity(entryName);
    }

    protected void readMacroDefinition (final String macroName) throws IOException
    {
        if ((null == macroName) || (macroName.length() <= 0))
            throw new StreamCorruptedException("readMacroDefinition(" + getMibName() + ") no macro name");

        if (!skipTillToken(SNMPProtocol.ENDModChars))
            throw new StreamCorruptedException("readMacroDefinition(" + getMibName() + ") no macro end: " + macroName);
    }
    /**
     * @param org Original (@link Collection) of (@link MIBAttributeEntry).
     * If <code>null</code> then new one is allocated
     * @return Updated (@link Collection) of (@link MIBAttributeEntry)
     * @throws IOException if problems encountered reading/parsing the MIB
     */
    public Collection<MIBAttributeEntry> readMIBEntries (final Collection<MIBAttributeEntry> org) throws IOException
    {
        final TokensReader    tkr=getTokensReader();
        if (null == tkr)
            throw new EOFException("readMIBEntries() no reader to read MIB entries from");

        Collection<MIBAttributeEntry>    entries=org;
        final char[]                    tkData=getReadBuffer();
        String                            curVal=null;
        for (int    readLen=readSkipComments(tkr, tkData); readLen > 0; readLen=readSkipComments(tkr, tkData))
        {
            // special handling for "leftovers"
            if (isComplementedChar(tkData[0]))
            {
                final char    firstChar=tkData[0],
                            lastChar=tkData[readLen-1],
                            cchar=getComplementedChar(firstChar);
                if ((readLen <= 1) || (cchar != lastChar))
                {
                    if (!skipTillComplementChar(firstChar))
                        throw new StreamCorruptedException("readMIBEntries(" + getMibName() + ") missing end bracket for token=" + curVal);
                }

                curVal = null;
                continue;
            }

            if (ParsableString.compareTo(tkData, 0, readLen, SNMPProtocol.DEFINITIONSModChars, false))
            {
                startDefinitions(curVal);
                curVal = null;
            }
            else if (ParsableString.compareTo(tkData, 0, readLen, SNMPProtocol.OBJECTModChars, false))
            {
                readObjectIdentifier(curVal);
                curVal = null;
            }
            else if (ParsableString.compareTo(tkData, 0, readLen, SNMPProtocol.OBJECTTYPEModChars, false))
            {
                final MIBAttributeEntry    e=readAttributeEntry(curVal);
                final String            oid=(null == e) ? /* should not happen */ null : e.getOid();
                // make sure added entry has an OID
                if ((null == oid) || (oid.length() <= 0))
                    throw new StreamCorruptedException("readMIBEntries(" + getMibName() + ") no OID for entry=" + e);

                if (null == entries)
                    entries = new LinkedList<MIBAttributeEntry>();
                entries.add(e);

                curVal = null;
            }
            else if (ParsableString.compareTo(tkData, 0, readLen, SNMPProtocol.IMPORTSModChars, false))
            {
                readImports();
            }
            else if (ParsableString.compareTo(tkData, 0, readLen, SNMPProtocol.NOTIFTypeModChars, false)
                  || ParsableString.compareTo(tkData, 0, readLen, SNMPProtocol.NOTIFGroupModChars, false))
            {
                readNotification(curVal, new String(tkData, 0, readLen));
                curVal = null;
            }
            else if (ParsableString.compareTo(tkData, 0, readLen, SNMPProtocol.ASSIGNModChars, false))
            {
                readTextReplacement(curVal);
                curVal = null;
            }
            else if (ParsableString.compareTo(tkData, 0, readLen, SNMPProtocol.ENDModChars, false))
            {
                if (curVal != null)
                    throw new StreamCorruptedException("readMIBEntries(" + getMibName() + ") unclosed token on end: " + curVal);

                final String    mibName=getMibName();
                if ((null == mibName) || (mibName.length() <= 0))
                    throw new StreamCorruptedException("no MIB name set at end");

                break;
            }
            else if (ParsableString.compareTo(tkData, 0, readLen, SNMPProtocol.MODULEIDModChars, false)
                  || ParsableString.compareTo(tkData, 0, readLen, SNMPProtocol.MODULECOMPLIANCEModChars, false)
                  || ParsableString.compareTo(tkData, 0, readLen, SNMPProtocol.OBJGROUPModChars, false))
            {
                readModuleIdentity(curVal);
                curVal = null;
            }
            else if (ParsableString.compareTo(tkData, 0, readLen, SNMPProtocol.MACROModChars, false))
            {
                readMacroDefinition(curVal);
                curVal = null;
            }
            else    // TODO more compareTo(s)
            {
                if (curVal != null)
                    throw new StreamCorruptedException("readMIBEntries(" + getMibName() + ") unclosed previous token: " + curVal);
                curVal = new String(tkData, 0, readLen);
            }
        }

        return entries;
    }

    public MIBGroup readMIBGroup () throws IOException
    {
        // NOTE !!! do NOT skip "readMIBEntries" call since it sets value returned by "getMibName()" as side-effect
        try
        {
            final Collection<? extends MIBAttributeEntry>    entries=readMIBEntries(null);
            return MIBGroup.buildMIBGroup(getMibName(), entries);
        }
        catch(RuntimeException re)
        {
            throw new StreamCorruptedException("readMIBGroup(" + getMibName() + ") " + re.getClass().getName() + ": " + re.getMessage());
        }
    }
}
