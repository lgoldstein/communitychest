package net.community.chest.net.snmp;

import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.util.List;

import net.community.chest.lang.StringUtil;
import net.community.chest.net.address.IPv4Address;

/**
 * <P>Copyright 2007 as per GPLv2</P>
 *
 * <P>Useful SNMP related definitions</P>
 *
 * @author Lyor G.
 * @since Oct 18, 2007 12:44:12 PM
 */
public final class SNMPProtocol {
    private SNMPProtocol ()
    {
        // no instance
    }

    public static final String    DEFINITIONSMod="DEFINITIONS";
        public static final char[]    DEFINITIONSModChars=DEFINITIONSMod.toCharArray();
    public static final String    BEGINMod="BEGIN";
        public static final char[]    BEGINModChars=BEGINMod.toCharArray();
    public static final String    ENDMod="END";
        public static final char[]    ENDModChars=ENDMod.toCharArray();
    public static final String    MACROMod="MACRO";
        public static final char[]    MACROModChars=MACROMod.toCharArray();
    public static final String    OBJECTMod="OBJECT";
        public static final char[]    OBJECTModChars=OBJECTMod.toCharArray();
    public static final String    IDENTIFIERMod="IDENTIFIER";
        public static final char[]    IDENTIFIERModChars=IDENTIFIERMod.toCharArray();
    public static final String    TEXTCONVENTIONMod="TEXTUAL-CONVENTION";
        public static final char[]    TEXTCONVENTIONModChars=TEXTCONVENTIONMod.toCharArray();
    public static final String    MODULEIDMod="MODULE-IDENTITY";
        public static final char[]    MODULEIDModChars=MODULEIDMod.toCharArray();
    public static final String    MODULECOMPLIANCEMod="MODULE-COMPLIANCE";
        public static final char[]    MODULECOMPLIANCEModChars=MODULECOMPLIANCEMod.toCharArray();
    public static final String    OBJGROUPMod="OBJECT-GROUP";
        public static final char[]    OBJGROUPModChars=OBJGROUPMod.toCharArray();
    public static final String    DISPLAYHintMod="DISPLAY-HINT";
        public static final char[]    DISPLAYHintModChars=DISPLAYHintMod.toCharArray();
    public static final String    NOTIFTypeMod="NOTIFICATION-TYPE";
        public static final char[]    NOTIFTypeModChars=NOTIFTypeMod.toCharArray();
    public static final String    NOTIFGroupMod="NOTIFICATION-GROUP";
        public static final char[]    NOTIFGroupModChars=NOTIFGroupMod.toCharArray();

    // "dummy" modifier
    public static final String    OBJECTIDENTIFIDERMod=OBJECTMod + " " + IDENTIFIERMod;
    public static final String    IMPORTSMod="IMPORTS";
        public static final char[]    IMPORTSModChars=IMPORTSMod.toCharArray();
    public static final String    FROMMod="FROM";
        public static final char[]    FROMModChars=FROMMod.toCharArray();
    public static final String    OBJECTTYPEMod="OBJECT-TYPE";
        public static final char[]    OBJECTTYPEModChars=OBJECTTYPEMod.toCharArray();
    public static final String    SYNTAXMod="SYNTAX";
        public static final char[]    SYNTAXModChars=SYNTAXMod.toCharArray();
    public static final String    ACCESSMod="ACCESS";
        public static final char[]    ACCESSModChars=ACCESSMod.toCharArray();
    public static final String    MAXACCESSMod="MAX-ACCESS";
        public static final char[]    MAXACCESSModChars=MAXACCESSMod.toCharArray();
    public static final String    STATUSMod="STATUS";
        public static final char[]    STATUSModChars=STATUSMod.toCharArray();
    public static final String    INDEXMod="INDEX";
        public static final char[]    INDEXModChars=INDEXMod.toCharArray();
    public static final String    DESCRIPTIONMod="DESCRIPTION";
        public static final char[]    DESCRIPTIONModChars=DESCRIPTIONMod.toCharArray();
    public static final String    UNITSMod="UNITS";
        public static final char[]    UNITSModChars=UNITSMod.toCharArray();
    public static final String OCTETMod="OCTET";
        public static final char[] OCTETModChars=OCTETMod.toCharArray();
    public static final String STRINGMod="STRING";
        public static final char[] STRINGModChars=STRINGMod.toCharArray();
    // "dummy" modifier
    public static final String OCTETSTRINGMod=OCTETMod + " " + STRINGMod;
    public static final String SIZEMod="SIZE";
        public static final char[] SIZEModChars=SIZEMod.toCharArray();
    public static final String INTEGERMod="INTEGER";
        public static final char[] INTEGERModChars=INTEGERMod.toCharArray();
    public static final String INTEGER32Mod="INTEGER32";
        public static final char[] INTEGER32ModChars=INTEGER32Mod.toCharArray();
    public static final String INTEGER64Mod="INTEGER64";
        public static final char[] INTEGER64ModChars=INTEGER64Mod.toCharArray();
    public static final String COUNTERMod="COUNTER";
        public static final char[] COUNTERModChars=COUNTERMod.toCharArray();
    public static final String COUNTER32Mod="COUNTER32";
        public static final char[] COUNTER32ModChars=COUNTER32Mod.toCharArray();
    public static final String COUNTER64Mod="COUNTER64";
        public static final char[] COUNTER64ModChars=COUNTER64Mod.toCharArray();
    public static final String GAUGEMod="GAUGE";
        public static final char[] GAUGEModChars=GAUGEMod.toCharArray();
    public static final String GAUGE32Mod="GAUGE32";
        public static final char[] GAUGE32ModChars=GAUGE32Mod.toCharArray();
    public static final String GAUGE64Mod="GAUGE64";
        public static final char[] GAUGE64ModChars=GAUGE64Mod.toCharArray();
    public static final String    SEQUENCEMod="SEQUENCE";
        public static final char[]    SEQUENCEModChars=SEQUENCEMod.toCharArray();
    public static final String    OFMod="OF";
        public static final char[]    OFModChars=OFMod.toCharArray();
    public static final String    ASSIGNMod="::=";
        public static final char[]    ASSIGNModChars=ASSIGNMod.toCharArray();

    public static final boolean isCommentToken (final char[] tkData, final int startPos, final int len)
    {
        if ((len < 2) || (null == tkData) || (startPos < 0) || ((startPos + len) > tkData.length))
            return false;

        return ('-' == tkData[startPos]) && ('-' == tkData[startPos+1]);
    }

    public static final boolean isCommentToken (final char ... tkData)
    {
        return (null == tkData) ? false : isCommentToken(tkData, 0, tkData.length);
    }

    public static final boolean isCommentToken (final CharSequence tkData, final int startPos, final int len)
    {
        if ((len < 2) || (null == tkData) || (startPos < 0) || ((startPos + len) > tkData.length()))
            return false;

        return ('-' == tkData.charAt(startPos)) && ('-' == tkData.charAt(startPos+1));
    }

    public static final boolean isCommentToken (final CharSequence tkData)
    {
        return (null == tkData) ? false : isCommentToken(tkData, 0, tkData.length());
    }
    /**
     * Converts a dot-notation OID string into its actual numerical values.
     * @param oid OID dot-notation string - if null/empty then null/empty
     * result returned. <B>Note:</B> OID may <U>not</U> start/end with dot (as
     * some applications use...).
     * @return matching numerical array - null/empty if null/empty string
     * to begin with
     * @throws NumberFormatException if illegal format or negative values found
     */
    public static final int[] getOIDValue (final String oid) throws NumberFormatException
    {
        final int    oidLen=(null == oid) ? 0 : oid.length();
        if (oidLen <= 0)
            return null;

        final List<String>    cl=StringUtil.splitString(oid, '.');
        final int            numDots=(null == cl) ? 0 : cl.size();
        if (numDots <= 0)    // should not happen (NOTE: zero is OK - e.g. OID="3")
            throw new NumberFormatException("getOIDValue(" + oid + ") invalid dots count: " + numDots);

        final int[]    vals=new int[numDots];
        for (int vIndex=0; vIndex < numDots; vIndex++)
        {
            final String    val=cl.get(vIndex);
            if ((vals[vIndex]=Integer.parseInt(val)) < 0)
                throw new NumberFormatException("getOIDValue(" + oid + ") invalid OID component value (" + val + ") at index=" + vIndex);
        }

        return vals;
    }
    /**
     * Converts an array of numerical values into an OID dot-notation string
     * @param vals numerical values  - may be null/empty if zero length specified
     * (and OK start position)
     * @param startPos position in array to start - may NOT be negative
     * @param len number of values to use in OID - may NOT be negative (OK if zero)
     * @return OID string - null/empty if zero length
     * @throws NumberFormatException if bad/illegal values encountered
     */
    public static final String toString (final int[] vals, final int startPos, final int len) throws NumberFormatException
    {
        if ((startPos < 0) || (len < 0))
            throw new NumberFormatException("Bad/illegal OID values range: start=" + startPos + "/len=" + len);
        if (0 == len)    // if zero length don't check if null/empty values array
            return null;

        final int    maxPos=startPos+len;
        if ((null == vals) || (maxPos > vals.length))
            throw new NumberFormatException("Bad/Illegal OID values array");

        final StringBuilder    sb=new StringBuilder(vals.length * 4);
        for (int    vPos=startPos; vPos < maxPos; vPos++)
        {
            final int    v=vals[vPos];
            if (v < 0)
                throw new NumberFormatException("Bad/Illegal OID component value (" + v + ") at position=" + vPos);
            if (vPos > startPos)    // if not first component then add delimiter
                sb.append('.');
            sb.append(String.valueOf(v));
        }

        return sb.toString();
    }
    /**
     * Converts an array of numerical values into an OID dot-notation string
     * @param vals numerical values - may be null/empty
     * @return OID string - null/empty if zero length
     * @throws NumberFormatException if bad/illegal values encountered
     */
    public static final String toString (final int ... vals) throws NumberFormatException
    {
        return toString(vals, 0, (null == vals) ? 0 : vals.length);
    }
    /**
     * @param v1 1st OID values array - may be null/empty if zero length reported
     * @param s1 1st OID comparison start index - may NOT be negative
     * @param l1 1st OID length - may NOT be negative
     * @param v2 2nd OID values array - may be null/empty if zero length reported
     * @param s2 2nd OID comparison start index - may NOT be negative
     * @param l2 2nd OID length - may NOT be negative
     * @return 0=equal, negative=1st less than 2nd, positive=1st greater than 2nd
     */
    public static final int compareValues (final int[] v1, final int s1, final int l1,
                                           final int[] v2, final int s2, final int l2)
    {
        final int    prfLen=Math.min(l1,l2);
        for (int    pIndex=0, p1=s1, p2=s2; pIndex < prfLen; pIndex++, p1++, p2++)
        {
            final int    dVal=v1[p1] - v2[p2];
            if (dVal != 0)
                return dVal;
        }

        /*
         *      This point is reached if both same length or one is prefix of
         * the other. In which case, the shorter one comes first - so the
         * comparison result is simply the length difference
         */
        return (l1 - l2);
    }
    /**
     * @param v1 1st OID values array - may be null/empty
     * @param v2 2nd OID values array - may be null/empty
     * @return 0=equal, negative=1st less than 2nd, positive=1st greater than 2nd
     */
    public static final int compareValues (final int[] v1, final int[] v2)
    {
        return compareValues(v1, 0, (null == v1) ? 0 : v1.length, v2, 0, (null == v2) ? 0 : v2.length);
    }
    /**
     * @param o1 1st OID dot-notation - may be null/empty
     * @param o2 2nd OID dot-notation - may be null/empty
     * @return longest common prefix of OID(s) - null/empty if either one is
     * null/empty or no common prefix
     */
    public static final String getCommonOidPrefix (final String o1, final String o2)
    {
        final int    l1=(null == o1) ? 0 : o1.length(),
                    l2=(null == o2) ? 0 : o2.length(),
                    len=Math.min(l1, l2);
        if (len <= 0)
            return null;

        for (int    oIndex=0; oIndex < len; oIndex++)
        {
            if (o1.charAt(oIndex) == o2.charAt(oIndex))
                continue;

            // if stumbled at first character then no common prefix possible
            if (oIndex <= 0)
                return null;

            // the common prefix is everything up to the current index (not including)
            final String    prefix=o1.substring(0, oIndex);
            final int        dotPos=prefix.lastIndexOf('.');
            if (dotPos <= 0)    // if no dot in prefix then no common prefix
                return null;

            // the common prefix is everything up to (but not including) the last dot
            return prefix.substring(0, dotPos);
        }

        if (l1 == l2)
            return o1;

        // the prefix (if any) is according to shortest OID
        final String    prefix=(l1 < l2) ? o1 : o2, other=(l1 < l2) ? o2 : o1;
        // check if shorter OID is exact prefix of longer one
        if ('.' == other.charAt(prefix.length()))
            return prefix;

        final int    dotPos=prefix.lastIndexOf('.');
        if (dotPos <= 0)    // if no dot in prefix then no common prefix
            return null;

        // the common prefix is everything up to (but not including) the last dot
        return prefix.substring(0, dotPos);
    }
    /**
     * @param o1 1st OID dot-notation - may be null/empty
     * @param o2 2nd OID dot-notation - may be null/empty
     * @return 0=equal, negative=1st less than 2nd, positive=1st greater than 2nd
     */
    public static final int compareOIDs (final String o1, final String o2)
    {
        final int    l1=(null == o1) ? 0 : o1.length(),
                    l2=(null == o2) ? 0 : o2.length(),
                    len=Math.min(l1, l2);

        for (int    oIndex=0; oIndex < len; oIndex++)
        {
            final char    c1=o1.charAt(oIndex), c2=o2.charAt(oIndex);
            if (c1 == c2)
                continue;

            // the one containing the '.' at this position comes first
            if ('.' == c1)
                return (-1);
            else if ('.' == c2)
                return (+1);

            // compare values till first dot (if any)
            final int        d1=o1.indexOf('.', oIndex), d2=o2.indexOf('.', oIndex);
            final String    v1=((d1 <= oIndex) || (d1 >= l1)) ? o1.substring(oIndex) : o1.substring(oIndex, d1),
                            v2=((d2 <= oIndex) || (d2 >= l2)) ? o2.substring(oIndex) : o2.substring(oIndex, d2);
            final int        n1=Integer.parseInt(v1), n2=Integer.parseInt(v2);
            return (n1 - n2);
        }

        // at this point we know that the shorter OID is prefix of longer one (or both equal)
        return (l1 - l2);
    }
    /**
     * OID suffix used to denote a scalar entry
     */
    public static final String    SCALAR_OID_SUFFIX=".0";
    /**
     * @param oid OID to be checked
     * @return TRUE if this is a scalar OID
     * @see #SCALAR_OID_SUFFIX
     */
    public static final boolean isScalarOID (final String oid)
    {
        if ((null == oid) || (oid.length() <= SCALAR_OID_SUFFIX.length()))
            return false;
        else
            return oid.endsWith(SCALAR_OID_SUFFIX);
    }
    /**
     * @param oid original OID to adjust
     * @return scalar OID if not already such - may be same as input if
     * null/empty or already scalar OID value
     */
    public static final String adjustScalarOID (final String oid)
    {
        if ((null == oid) || (oid.length() <= 0) || isScalarOID(oid))
            return oid;
        else
            return oid + SCALAR_OID_SUFFIX;
    }
    /**
     * @param s string value used as index in an SNMP table - may NOT be null/empty
     * @return fixed length string OID encoding (null/empty if null/empty input)
     * @throws UnsupportedEncodingException if unable to convert to UTF-8 (which should be NEVER)
     */
    public static final String createFixedLengthStringIndexOID (final String s) throws UnsupportedEncodingException
    {
        final int        sLen=(null == s) ? 0 : s.length();
        final byte[]    sBytes=(sLen <= 0) ? null : s.getBytes("UTF-8");
        final int        bLen=(null == sBytes) ? 0 : sBytes.length;
        if (bLen <= 0)
            return null;

        final StringBuilder    sb=new StringBuilder(bLen * 4 /* 3 ASCII digits + dot */ + 2);
        for (int    bIndex=0; bIndex < bLen; bIndex++)
        {
            if (bIndex > 0)
                sb.append('.');
            sb.append(sBytes[bIndex] & 0xFF);
        }

        return sb.toString();
    }
    /**
     * @param s string value used as index in an SNMP table - OK if null/empty
     * @return variable string OID encoding
     * @throws UnsupportedEncodingException if unable to convert to UTF-8 (which should be NEVER)
     */
    public static final String createVarLengthStringIndexOID (final String s) throws UnsupportedEncodingException
    {
        final int        sLen=(null == s) ? 0 : s.length();
        final String    oLen=(sLen <= 0) ? "0" : String.valueOf(sLen),
                        oVal=(sLen <= 0) ? null : createFixedLengthStringIndexOID(s);
        return (sLen <= 0) ? oLen : oLen + "." + oVal;
    }
    /**
     * @param ia {@link InetAddress} instance to be encoded (as per SNMPv1 rules)
     * @return encoded string - null/empty if null/empty address to begin with
     * @throws UnsupportedEncodingException if non-IPv4 address
     */
    public static final String createIPAddressIndexOID (final InetAddress ia) throws UnsupportedEncodingException
    {
        final String    ha=(null == ia) ? null : ia.getHostAddress();
        final int        haLen=(null == ha) ? 0 : ha.length();
        if (haLen < 7)    // min.="a.b.c.d"
            throw new UnsupportedEncodingException("createIPAddressIndexOID(" + ha + ") illegal initial host address");

        try
        {
            IPv4Address.toLong(ha);
        }
        catch(NumberFormatException e)
        {
            throw new UnsupportedEncodingException("createIPAddressIndexOID(" + ha + ") non-IPv4 address");
        }

        return ha;
    }
    /**
     * @param oid The OID dot-notation to validate
     * @param throwExc If TRUE then throws an {@link IllegalArgumentException}
     * if any problem found
     * @return TRUE if non-null/empty valid OID
     * @throws IllegalArgumentException if invalid OID and <code>throwExc</code>
     * is <code>true</code>
     */
    public static final boolean validateOID (final String oid, final boolean throwExc) throws IllegalArgumentException
    {
        final int    oidLen=(null == oid) ? 0 : oid.length();
        if (oidLen <= 0)
        {
            if (throwExc)
                throw new IllegalArgumentException("validateOID(" + oid + ") null/empty");
            return false;
        }
        // validate OID components
        for (int curPos=0; curPos < oidLen; )
        {
            final int        nextPos=oid.indexOf('.', curPos);
            final String    vPart;
            if (nextPos > curPos)
                vPart = oid.substring(curPos, nextPos);
            else if (nextPos < 0)    // this means no more dots
                vPart = oid.substring(curPos);
            else    // this means dot after dot without number in between
            {
                if (throwExc)
                    throw new IllegalArgumentException("validateOID(" + oid + ") bad/illegal dot at position=" + curPos);
                return false;
            }

            try
            {
                final int    oidVal=Integer.parseInt(vPart);
                if (oidVal <= 0)
                {
                    if (throwExc)
                        throw new IllegalArgumentException("validateOID(" + oid + ") bad/illegal OID part: " + vPart);
                    return false;
                }
            }
            catch(NumberFormatException e)
            {
                if (throwExc)
                    throw new IllegalArgumentException("validateOID(" + oid + ") non-numerical OID part: " + vPart);
                return false;
            }

            if (nextPos < 0)    // if no more dots, then stop
                break;

            curPos = nextPos + 1;    // skip dot
        }

        return true;
    }
}
