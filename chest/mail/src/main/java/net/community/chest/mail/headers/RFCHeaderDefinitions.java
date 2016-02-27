package net.community.chest.mail.headers;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.UnsupportedCharsetException;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.TreeMap;

import net.community.chest.ParsableString;
import net.community.chest.io.encode.base64.Base64;
import net.community.chest.io.encode.qp.QuotedPrintable;
import net.community.chest.lang.StringUtil;
import net.community.chest.reflect.ClassUtil;
import net.community.chest.util.datetime.DateUtil;

/**
 * <P>Copyright 2007 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Aug 22, 2007 10:13:10 AM
 */
public final class RFCHeaderDefinitions {
	private RFCHeaderDefinitions ()
	{
		// disable instantiation
	}
	
	// ############# some standard/well-known headers ##############

	public static final String stdFromHdr="From";
    public static final String stdSenderHdr="Sender";
    public static final String stdToHdr="To";
    public static final String stdCcHdr="Cc";
    public static final String stdBccHdr="Bcc";
    public static final String stdDateHdr="Date";
    public static final String stdMIMEVersionHdr="MIME-Version";
    public static final String defMIMEVersionHdrValue="1.0";
    public static final String stdReturnPathHdr="Return-Path";
    public static final String stdReplyToHdr="Reply-To";
    public static final String stdReplyCcHdr="Reply-Cc";
    public static final String stdInReplyToHdr="In-Reply-To";
    public static final String stdReceivedHdr="Received";
    public static final String stdSubjectHdr="Subject";
    public static final String stdContentTypeHdr="Content-Type";
    public static final String stdContentLengthHdr="Content-Length";
    public static final String stdContentLocationHdr="Content-Location";
    public static final String stdContentBaseHdr="Content-Base";
    public static final String stdContentIDHdr="Content-ID";
    public static final String stdContentXferEncoding="Content-Transfer-Encoding";
    public static final String stdContentDisposition="Content-Disposition";
    public static final String stdContentDescription="Content-Description";
    public static final String stdContentDuration="Content-Duration";
    public static final String stdContentLanguage="Content-Language";
    public static final String stdContentMD5Hdr="Content-MD5";
    public static final String stdMessageIDHdr="Message-ID";
    	public static final char[] stdMessageIDHdrChars=stdMessageIDHdr.toCharArray();
    public static final String stdImportanceHdr="Importance";
    public static final String stdSensitivityHdr="Sensitivity";
    public static final String stdStatusHdr="Status";
    public static final String stdReturnReceiptToHdr="Return-Receipt-To";
    public static final String stdReturnReceiptCcHdr="Return-Receipt-Cc";
    public static final String stdDispositionNotificationToHdr="Disposition-Notification-To";

    public static final String stdResentFromHdr="Resent-From";
    public static final String stdResentSenderHdr="Resent-Sender";
    public static final String stdResentToHdr="Resent-To";
    public static final String stdResentCcHdr="Resent-Cc";
    public static final String stdResentDateHdr="Resent-Date";
    public static final String stdResentMessageIDHdr="Resent-Message-ID";

    public static final String stdApparentlyToHdr="Apparently-To";
    public static final String stdApparentlyCcHdr="Apparently-Cc";
    public static final String stdApparentlyBccHdr="Apparently-Bcc";

        /* some "well-known" X-hdrs */
    public static final String XPriorityHdr="X-Priority";
    public static final String XMailerHdr="X-Mailer";
    /**
     * Parses and adjusts the timezone (if any)
     * @param ps parsable string object to be used
     * @param startPos start position in parse buffer to look for timezone (inclusive)
     * @param cdt calendar object whose timezone is to be set (if successful)
     * @return next position in parse buffer (or <0 if error)
     */
    public static final int adjustTimezone (final ParsableString ps, final int startPos, final Calendar cdt)
    {
    	// NOTE: we look for the "+/-" of the GMT offset, but if not found, then be lenient (and assume "+/-0000")
    	if ((null == ps) || (null == cdt))
    		return (-1);

    	final int startIndex=ps.getStartIndex(), maxIndex=ps.getMaxIndex();
    	int	curPos=ps.findNonEmptyDataStart(startPos);
    	if ((curPos <= startIndex) || (curPos >= maxIndex))
    		return (-2);

    	int		tzSign=0;
    	char	chSize=ps.getCharAt(curPos);
    	if ('+' == chSize)
    	{	
    		tzSign = 1;
    		curPos++;
    	}
    	else if ('-' == chSize)
    	{	
    		tzSign = (-1);
    		curPos++;
    	}
    	// NOTE: we allow omitting the '+/-' if digit straight away
    	else if ((chSize >= '0') && (chSize <= '9'))
    	{	
    		tzSign = 1;
    		chSize = '+';
    	}
    	else	// error - neither a sign nor a digit
    		return (-3);

    	int	tzHours=0, tzMinutes=0;
    	try
    	{
    		tzHours = ps.getUnsignedInt(curPos, curPos+2);

    		// some timezones contain a ':' between the hours and the minutes
    		if (DateUtil.DEFAULT_TMSEP == ps.getCharAt(curPos+2))
    			curPos++;

    		tzMinutes = ps.getUnsignedInt(curPos+2, curPos+4);
    	}
    	catch(NumberFormatException nfe)
    	{
    		return (-4);	// should not happen - we expect EXACTLY 4 digits
    	}

    	// TODO check decoding when server GMT zone different than current host
    	final int		absOffset=(tzHours * 3600) + (tzMinutes * 60), tzOffset=absOffset * tzSign;
    	final TimeZone	tz=TimeZone.getDefault();
    	tz.setRawOffset(tzOffset * 1000);
    	tz.setID("GMT" + ((chSize != '+') ? /* '-' is added automatically */ "" : "+") + tzHours);
    	cdt.setTimeZone(tz);

    	return (curPos + 4);
    }
    /**
     * Parses and adjusts the timezone (if any)
     * @param cs char sequence to be decoded
     * @param startPos start position in sequence to start parsing (inclusive)
     * @param len maximum number of characters to be parsed
     * @param cdt calendar object to be set (if successful)
     * @return position in char sequence of next parsable char (or <0 if error)
     */
    public static final int adjustTimezone (final CharSequence cs, final int startPos, final int len, final Calendar cdt)
    {
    	if ((null == cs) || (startPos < 0) || (len <= 0) || ((startPos + len) > cs.length()))
    		return (-1);
    	else
    		return adjustTimezone(new ParsableString(cs, startPos, len), startPos, cdt);
    }
    /**
     * Parses and adjusts the timezone (if any)
     * @param cs char sequence to be decoded
     * @param cdt calendar object to be set (if successful)
     * @return position in char sequence of next parsable char (or <0 if error)
     */
    public static final int adjustTimezone (final CharSequence cs, final Calendar cdt)
    {
    	return adjustTimezone(cs, 0, (null == cs) ? 0 : cs.length(), cdt);
    }
    /**
     * Decodes one of the (many) possible formats of a "Date:" header value
     * @param dtValue string value to decode (without the "Date:" header name)
     * @param startPos position in sequence to start parsing (inclusive)
     * @param len maximum number of characters available for parsing
     * @return calendar object representing the value (or null if unable to decode)
	 * @throws IllegalStateException if ERA not "AD" (which should be NEVER...)
     */
    public static final Calendar xlateDateValue (CharSequence dtValue, int startPos, int len) throws IllegalStateException
    {
    	final int	dtLen=(null == dtValue) ? 0 : dtValue.length();
    	if ((dtLen <= 0) || (startPos < 0) || (len <= 0) || ((startPos+len) > dtLen))
    		return null;

    	return (new DateValueDecoder(dtValue, startPos, len)).decodeValue();
    }
    /**
     * Decodes one of the (many) possible formats of a "Date:" header value
     * @param dtValue string value to decode (without the "Date:" header name)
     * @return calendar object representing the value (or null if unable to decode)
	 * @throws IllegalStateException if ERA not "AD" (which should be NEVER...)
     */
    public static final Calendar xlateDateValue (CharSequence dtValue) throws IllegalStateException
    {
    	return xlateDateValue(dtValue, 0, (null == dtValue) ? 0 : dtValue.length());
    }
    /**
     * Builds a MIME compliant date/time value given the calendar object
     * @param <A> The {@link Appendable} generic type
     * @param sb - {@link Appendable} instance to append generated date/time string
     * @param dtv date/time object - assumed to contain valid data (including day of week)
     * @param daysOfWeekNames (abbreviated) names of day-of-week (0=Sunday)
     * @param monthsNames (abbreviated) names of months (0=January)
     * @return same as input {@link Appendable} instance
     * @throws IOException if failed to append or bad parameters
     */
    public static final <A extends Appendable> A appendStdDateValue (
    					final A				sb,
    					final Calendar 		dtv,
    					final List<String>	daysOfWeekNames,
    					final List<String>	monthsNames) throws IOException
    {
    	final int	numDays=(null == daysOfWeekNames) ? 0 : daysOfWeekNames.size(),
    				numMonths=(null == monthsNames) ? 0 : monthsNames.size();
    	if ((null == dtv) || (null == sb))
    		throw new IOException(ClassUtil.getExceptionLocation(RFCHeaderDefinitions.class, "appendStdDateValue") + " incomplete parameters");

    	// start with day of week
    	{
    		int	dowIndex=dtv.get(Calendar.DAY_OF_WEEK) - Calendar.SUNDAY;
    		if ((dowIndex < 0) || (dowIndex >= numDays))
    		{
    			// try Doomsday algorithm to determine day of week - NOTE: good only for Gregorian
    			// TODO check if need non-Gregorian calendar support as well
    			dowIndex = (DateUtil.getDayOfWeekForDate(dtv) - Calendar.SUNDAY);
    			if ((dowIndex < 0) || (dowIndex >= numDays))
    				throw new IOException(ClassUtil.getExceptionLocation(RFCHeaderDefinitions.class, "appendStdDateValue") + " unresolved day-of-week value: " + dowIndex);
    		}

    		sb.append(daysOfWeekNames.get(dowIndex))
    		  .append(',')
    		  .append(' ')
    		  ;
    	}

    	// add the current date
    	{
    		StringUtil.appendPaddedNum(sb, dtv.get(Calendar.DAY_OF_MONTH), 0)
    				  .append(' ')
    				  ;
    		
    		final int	moyIndex=dtv.get(Calendar.MONTH) - Calendar.JANUARY;
    		if ((moyIndex < 0) || (moyIndex >= numMonths))
				throw new IOException(ClassUtil.getExceptionLocation(RFCHeaderDefinitions.class, "appendStdDateValue") + " unresolved month value: " + moyIndex);
    		sb.append(monthsNames.get(moyIndex))
    		  .append(' ')
    		  ;

    		StringUtil.appendPaddedNum(sb, dtv.get(Calendar.YEAR), 4)
    				  .append(' ')
    				  ;
    	}

    	// add the current time
    	{
    		StringUtil.appendPaddedNum(sb, dtv.get(Calendar.HOUR_OF_DAY), 2)
    				  .append(DateUtil.DEFAULT_TMSEP)
    				  ;

    		StringUtil.appendPaddedNum(sb, dtv.get(Calendar.MINUTE), 2)
    				  .append(DateUtil.DEFAULT_TMSEP)
    				  ;

    		StringUtil.appendPaddedNum(sb, dtv.get(Calendar.SECOND), 2)
    				  .append(' ')
    				  ;
    	}
    	
    	// add the timezone offset
    	{
    		final TimeZone	dtz=dtv.getTimeZone();
    		if (null == dtz)	// OK if no timezone - assume GMT
    		{	
    			sb.append("-0000");	// signal the error with a '-'
    		}
    		else
    		{	
    			int	tzOffset=dtz.getRawOffset();	// msec.
    			if (dtz.useDaylightTime() && dtz.inDaylightTime(dtv.getTime()))
    				tzOffset += dtz.getDSTSavings();

    			final int	tzHours=tzOffset / 3600000,
    						absHours=Math.abs(tzHours),
    						absMinutes=Math.abs((tzOffset % 3600000) / 60000);
    			sb.append((tzHours > 0) ? '+' : '-');

    			StringUtil.appendPaddedNum(sb, absHours, 2);
    			if (absMinutes != 0)
    				StringUtil.appendPaddedNum(sb, absMinutes, 2);
    			else
    				sb.append("00");
    		}
    	}
    	
    	return sb;
    }
    /**
     * Builds a MIME compliant date/time value given the calendar object
     * @param <A> The {@link Appendable} generic type
     * @param sb - {@link Appendable} instance to append generated date/time string
     * @param dtv date/time object - assumed to contain valid data (including day of week)
     * @return same as input {@link Appendable} instance
     * @throws IOException if failed to append or bad parameters
     */
    public static final <A extends Appendable> A appendStdDateValue (A sb, Calendar dtv) throws IOException
    {
    	return appendStdDateValue(sb, dtv, DateUtil.abbrevDaysNames, DateUtil.abbrevMonthsNames);
    }
    /**
     * Minimum length of a MIME compliant date/time string
     */
    public static final int STD_DATETIME_MIN_STRLEN=DateUtil.ABBREV_DOW_LEN + 1 + 1	// day of week
    		+ 2 + 1 + DateUtil.ABBREV_MOY_LEN + 1 + 4	// date: day month-name year
    		+ 2 + 1 + 2 + 1 + 2 + 1	// time: hh:mm:ss
    		+ 1 + 4		// zone offset
    	;
    /**
     * Builds a MIME compliant date/time value given the calendar object
     * @param dtv date/time object - (non-null) assumed to contain valid
     * data (including day of week)
     * @return string representing the value
     * @throws IOException if bad/illegal {@link Calendar} value
     */
    public static final String buildStdDateValue (final Calendar dtv) throws IOException
    {
		final StringBuilder	sb=appendStdDateValue(new StringBuilder(STD_DATETIME_MIN_STRLEN), dtv);
		return sb.toString();	// just so we have a debug breakpoint
    }
    /**
     * Hunts for escaped (via backslash) characters and replaces them with the original character they escape
     * @param hdrValue header value to be scanned - if null/empty then nothing is done
     * @return adjusted value (could be the original value if no translation required)
     * @throws IllegalStateException if unexpected appending error occurs (which should be never)
     */
    public static final String replaceEscapedChars (final String hdrValue)
    {
    	final int nLen=(null == hdrValue) ? 0 : hdrValue.length();
    	if (nLen <= 0)
    		return hdrValue;

    	StringBuilder    sb=null;    // will allocate if necessary
    	for (int    i=0; i < nLen; i++)
    	{
    		char    c=hdrValue.charAt(i);
    		// make sure something follows the escape character - otherwise ignore it and append it transparently
    		if (('\\' == c) && ((i+1) < nLen))
    		{
    			// if this is the first time, then allocate a string buffer and copy everything up to first occurence
    			if (null == sb)
    			{
    				final String	subHdrValue=hdrValue.substring(0, i);
    				sb = new StringBuilder(nLen + RFCMessageHeaders.MIN_HDRENC_LEN /* we don't really expect any growth */);
    				sb.append(subHdrValue);
    			}

    			i++;

    			// append the escaped character whatever it is
    			c = hdrValue.charAt(i);
    		}

    		// if have an active string buffer, then append to it
    		if (sb != null)
    			sb.append(c);
    	}

    	// if we did not have to use the string buffer, then no replacement took place
    	if (null == sb)
    		return hdrValue;
    	else
    		return sb.toString();
    }
    /**
     * Some known charset aliases - key=alias (case insensitive), value=canonical name
     */
    private static final Map<String,String> _charsetAliases=new TreeMap<String,String>(String.CASE_INSENSITIVE_ORDER);
    /**
     * @return Map of known charset aliases - key=alias (case insensitive),
     * value=canonical name. <B>Note:</B> this is the <U>internal</U> map so
     * <U><B>don't</B></U> change it unless you know what you are doing
     */
    public static final Map<String,String> getCharsetAliases ()
    {
    	return _charsetAliases;
    }
    /**
     * Adds the mapping of aliases to canonical name for the built-in charset of the current JVM
     * @param cnMap map to which to add the mapping (key=alias (case
     * insensitive), value=canonical name) - should NOT be null
     * @return updated map (same as input)
     */
    private static final Map<String,String> addStdAliases (final Map<String,String> cnMap)
    {
    	if (null == cnMap)
    		return cnMap;

    	final Map<String,Charset>	chMap=Charset.availableCharsets();
    	final Collection<Charset>	chEntries=((null == chMap) || (chMap.size() <= 0)) ? null : chMap.values();
    	if (chEntries != null)
    	{
    		for (final Charset	c : chEntries)
    		{
    			final String				cnName=
    					(null == c) /* should not happen */ ? null : c.name();
    			final Collection<String>	aSet=
    				(null == c) /* should not happen */ ? null : c.aliases();
    			if (aSet != null)
    			{
    				for (final String	alName : aSet)
    				{
    					if ((null == alName) || (alName.length() <= 0))
    						continue;	// should not happen
    			
    					cnMap.put(alName, cnName);
    				}
    			}
    		}
    	}
    	
    	return cnMap;
    }
    /* 		static initializer - populates the charset aliases map with the
     * internal JVM charset(s) as well as some "known" aliases
     */
    static
    {
    	/**
    	 * Some known charset aliases - even index=alias, odd index=canonical name
    	 */
    	final String[] knownAliases={
    				"ISO-LATIN-1",          "ISO-8859-1",
					"ISO-8859-6-I",         "ISO-8859-6",
					"ISO-8859-6-E",         "ISO-8859-6",
					"ISO-8859-8-I",         "ISO-8859-8",
					"HEBREW(WINDOWS)",      "WINDOWS-1255",
					"HEBREW(ISO-LOGICAL)",  "ISO-8859-8",   // actually it is "iso-8859-8-i" , but we avoid recursions
					"HEBREW(ISO-VISUAL)",   "ISO-8859-8",
					"ARABIC(ISO)",          "ISO-8859-6",
					"ARABIC(WINDOWS)",      "WINDOWS-1256",
					"UNICODE-1-1-UTF-8",    "UTF-8",
					"UNICODE-1-1-UTF-7",    "UTF-7",
					"EUC-JP",               "ISO-2022-JP",
					"SHIFT_JIS",            "ISO-2022-JP",
					"ISO-2022-JP-1",        "ISO-2022-JP",
					"KOI-8",                "ISO-8859-5"
    	};
    	// we first add our mappings, that may be overwritten by the current JVM
    	for (int    i=0; i < knownAliases.length; i+= 2)
    		_charsetAliases.put(knownAliases[i], knownAliases[i+1]);
    	// add the built-in mappings
    	addStdAliases(_charsetAliases);
    }
    /**
     * @param cn charset name to be checked if this is an alias
     * @return the canonical name (if found) - could be the same as the
     * input charset or uppercase version of the input
     */
    public static final String getCanonicalCharsetName (final String cn)
    {
    	if ((null == cn) || (cn.length() <= 0))
    		return cn;

    	final String	cnAlias=_charsetAliases.get(cn);
    	return (null == cnAlias) ? cn.toUpperCase() : cnAlias.toUpperCase();
    }
    /**
     * Resolves the effective charset name
     * @param cn original charset value - if null/empty then "US-ASCII" is
     * returned (as per RFC2045 section 5.2 specification)
     * @return effective (<U>canonical</U>) charset name.
     */
    public static final String getEffectiveCharsetName (final String cn)
    {
    	return ((null == cn) || (cn.length() <= 0)) ? "US-ASCII" : getCanonicalCharsetName(cn);
    }
    /**
     * Encodes the header value according to RFC2045-9 rules
     * @param hdrValue the header value to be encoded
     * @param charset requested charset for the header value - may be
     * null/empty, in which case {@link RFCMessageHeaders#HDRENCDEFCHARSET}
     * will be used.
     * @return encoded value (same "hdrValue" if no encoding required)
     */
    public static final String encodeHdrValue (final String hdrValue, final String charset)
    {
    	final int	hvLen=(null == hdrValue) ? 0 : hdrValue.length();
    	if (hvLen <= 0)	// OK if nothing to do
    		return hdrValue;

    	// convert header value to required charset
    	final int		csnLen=(null == charset) ? 0 : charset.length();
    	final String    encCharset=(csnLen <= 0) ? RFCMessageHeaders.HDRENCDEFCHARSET : getCanonicalCharsetName(charset);
    	Charset			cs=null;
    	try
    	{
    		cs = Charset.forName(encCharset);
    	}
    	// if unknown charset, then return raw use the default
    	catch(IllegalCharsetNameException icne)
    	{
    		cs = null;
    	}
    	catch(UnsupportedCharsetException uce)
    	{
    		cs = null;
    	}
    	finally
    	{
    		// NOTE !!! we assume no exception is thrown for the default charset
    		if (null == cs)	// TODO allow configuring the default value
    			cs = Charset.forName(RFCMessageHeaders.HDRENCDEFCHARSET);
    	}

    	// for UTF-8 we can test the need to encode the header value without converting it to bytes
    	if ("UTF-8".equalsIgnoreCase(encCharset))
    	{
    		boolean	encRequired=false;
    		for (int	cPos=0; (cPos < hvLen) && (!encRequired); cPos++)
    		{
    			final char	hc=hdrValue.charAt(cPos);
    			// UTF-8 is "insensitive" to US-ASCII range
    			if (hc < ' ')
    			{
    				// skip whitespace
    				if (('\r' == hc) || ('\t' == hc) || ('\n' == hc))
    					continue;
    				
    				encRequired = true;
    			}
    			else if (hc > (char) 0x007f)
    				encRequired = true;
    		}

    		if (!encRequired)
    			return hdrValue;
    	}

    	// TODO handle case of unknown/alias charset
    	final ByteBuffer  bb=cs.encode(hdrValue);
    	final byte[]      hb=bb.array();

    	// For some reason, the returned byte array contains zero padding at its end, and we cannot use its length, but rather the "valid" data length
    	int	nLen=bb.remaining(), nXChars=0, nQPEncLen=0;
    	for (int    i=0; i < nLen; i++)
    	{
    		final byte    b=hb[i];
    		final boolean fIsSeparator=(('\r' == b) || ('\t' == b) || ('\n' == b));

    		if (fIsSeparator)
    			continue;

    		// UTF-8 is transparent to characters up to 0x9f, but in order not to take chances, we stop at 0x7F
    		if ((b < 0x20) || (b > 0x7e))
    			nXChars++;
    		// check if QP encoding	might be required (if we decide on it)
    		else if (QuotedPrintable.QPDELIM == (char) (b & 0x00FF))	
    			nQPEncLen += QuotedPrintable.QPENCLEN;
    	}

    	// if no "translatable" characters then do nothing
    	if (0 == nXChars)
    		// TODO - scan the string and escape any double-quotes or backslash
    		return hdrValue;

    	/* Calculate the required encoding using QP:
    	 *
    	 *      non-encoded characters = total - encoded
    	 *      encoded characters size = encoded characters * encoding size
    	 */
    	nQPEncLen += (nLen - nXChars) + (nXChars * QuotedPrintable.QPENCLEN);

    	// check how much would it take to use a BASE64 encoding
    	final int 			nB64EncLen=Base64.calculateEncodedSize(nLen, null);
    	// use the encoding that yields the least encoded length
    	final int          	nSBLen=RFCMessageHeaders.MIN_HDRENC_LEN + encCharset.length() + Math.min(nQPEncLen,nB64EncLen);
    	// allocate a little more, just in case
    	final StringBuilder	sb=new StringBuilder(nSBLen + RFCMessageHeaders.MIN_HDRENC_LEN);
    	sb.append(RFCMessageHeaders.HDRENCPREFIXCHARS);
    	sb.append(encCharset);
    	sb.append(RFCMessageHeaders.HDRENCDATADELIMITER);

    	final char cEnc=((nB64EncLen > nQPEncLen) ? RFCMessageHeaders.QP_HDRENC_CHAR : RFCMessageHeaders.BASE64_HDRENC_CHAR);
    	sb.append(cEnc);
    	sb.append(RFCMessageHeaders.HDRENCDATADELIMITER);

    	if (nB64EncLen > nQPEncLen)
    	{
    		final char[]	qpc=new char[QuotedPrintable.QPENCLEN];
    		for (int    i=0; i < nLen; i++)
    		{
    			final byte	b=hb[i];
    			final char	c=(char) (b & 0x00FF);
    			if (' ' == c)
    				sb.append('_');
    			else if (QuotedPrintable.requiresEncoding(c, QuotedPrintable.ENCOPT_NOSPACE | QuotedPrintable.ENCOPT_SPACE_AS_UNDERLINE | QuotedPrintable.ENCOPT_SEPARATORS))
    			{
    				final int	aLen=QuotedPrintable.getQPChars(hb[i], qpc, 0, qpc.length);
    				if (aLen <= 0)	// should not happen
    					return hdrValue;
    				sb.append(qpc, 0, aLen);
    			}
    			else
    				sb.append(c);
    		}

    		// TODO - scan the QP string and escape any double-quotes or backslash
    	}
    	else    // use BASE64
    	{	
    		try
    		{
    			final String	b64Value=new String(Base64.encode(hb, 0, nLen), "UTF-8");
    			sb.append(b64Value);
    		}
    		catch (UnsupportedEncodingException e)
    		{	// this should not happen
    			return hdrValue;
    		}
    	}

    	sb.append(RFCMessageHeaders.HDRENCSUFFIXCHARS);

    	return sb.toString();
    }
    /**
     * Extract the characters as ASCII bytes - this is needed because the
     * "getBytes" call of the string object returns corrupted results for
     * unknown character sets
     * @param sectData section data to be converted to bytes
     * @return ASCII bytes - null/empty if problem encountered
     */
    public static final byte[] getEncodedBytes (final CharSequence sectData)
    {
    	final int	encLength=(null == sectData) ? 0 : sectData.length();
    	if (encLength <= 0)
    		return null;	// should not happen

    	// since we expect ASCII bytes, the array size is same as number of characters in string
    	final byte[]	encBytes=new byte[encLength];
    	for (int	encIndex=0; encIndex < encLength; encIndex++)
    	{
    		final char	ech=sectData.charAt(encIndex);
    		// we expect only ASCII characters, but need to check anyway
    		if (ech > (char) 0x00FF)
    			return null;
    		
    		encBytes[encIndex] = (byte) (ech & 0x00FF);
    	}

    	return encBytes;
    }

    private static final StringBuilder getHeaderDecoderWorkBuf (final int nLen)
    {
    	return new StringBuilder(Math.max(nLen,0)+8);
    }

    public static final String decodeHdrValue (final String hdrValue, final Collection<? extends EncodedHeaderSection> secs, final Map<String, String> csMap, final boolean replaceEscChars)
    {
    	// if no encoded sub-sections then do nothing
    	final int	numSections=(null == secs) ? 0 : secs.size();
    	if (numSections <= 0)
    		return (replaceEscChars ? RFCHeaderDefinitions.replaceEscapedChars(hdrValue) : hdrValue);

    	final int 			nLen=(null == hdrValue) ? 0 : hdrValue.length();
    	final StringBuilder	sb=getHeaderDecoderWorkBuf(nLen);
    	int					nLastPos=0;
    	for (final EncodedHeaderSection hs : secs)
    	{
    		if (null == hs)	// should not happen
    			continue;

    		// decode the section payload according to the encoding
    		byte[]  decBytes=null;

    		/* 		Extract ASCII bytes - we do not use "getBytes" as it
    		 *  corrupts the resulting bytes if some unknown charset used.
    		 */
    		{
    			final String	subHdrValue=hs.getSectionData();
    			final byte[]	encBytes=getEncodedBytes(subHdrValue);
    			if ((null == encBytes) || (encBytes.length <= 0))
    				continue;

    			final char	chEnc=Character.toUpperCase(hs.getEncodeChar());
    			try
    			{
    				switch(chEnc)
    				{
    					case RFCMessageHeaders.BASE64_HDRENC_CHAR	:
    						decBytes = Base64.decodeToBytes(encBytes, false);
    						break;
    				
    					case RFCMessageHeaders.QP_HDRENC_CHAR		:
    						decBytes = QuotedPrintable.decodeToBytes(encBytes, QuotedPrintable.DECOPT_UNDERLINE_AS_SPACE);
    						break;
    				
    					default										:
    						throw new UnsupportedEncodingException("Unknown encoding (" + String.valueOf(chEnc) + " for section=" + subHdrValue);
    				}
    				
    				if ((null == decBytes) || (decBytes.length <= 0))
    					continue;	// should not happen
    			}
    			catch(IOException ioe)
    			{
    				continue;	// unexpected decoding error
    			}

    		}

    		Charset cs=null;
			{
				final String	cn=hs.getCharsetName(),
								mapName=(null == csMap) ? null : csMap.get(cn),
								charsetName=getCanonicalCharsetName(((null == mapName) || (mapName.length() <= 0)) ? cn : mapName);
				try
				{
					cs = Charset.forName(charsetName);
	    		}
	    		// if unknown charset, then skip it and add the data as-is
	    		catch(IllegalCharsetNameException icne)
	    		{
	    			continue;
	    		}
	    		catch(UnsupportedCharsetException uce)
	    		{
	    			continue;
	    		}
			}

    		final ByteBuffer  bb=ByteBuffer.allocate(decBytes.length+1);
    		bb.put(decBytes);
    		bb.flip();

    		final CharBuffer  decBuf=cs.decode(bb);
    		final String      decData=decBuf.toString();

    		// append non-encoded part (if any)
    		final int	nPos=hs.getStartIndex();
    		if (nLastPos < nPos)
    		{
    			final String  clrData=hdrValue.substring(nLastPos, nPos);
    			sb.append(clrData);
    		}

    		if ((decData != null) && (decData.length() > 0))
    			sb.append(decData);	// should not be otherwise

    		// mark clear text start at end of current section
    		nLastPos = hs.getEndIndex();
    	}

    	// check if had to do any decoding
    	if ((null == sb) || (sb.length() <= 0))
    		return (replaceEscChars ? RFCHeaderDefinitions.replaceEscapedChars(hdrValue) : hdrValue);

    	if (nLastPos < nLen)
    	{
    		final String  hdrRemainder=hdrValue.substring(nLastPos);
    		sb.append(hdrRemainder);
    	}

    	final String	decVal=sb.toString();
    	return (replaceEscChars ? RFCHeaderDefinitions.replaceEscapedChars(decVal) : decVal);
    }
    /**
     * Decodes a MIME encoded header value, and translates the header charset (if any) to Java representation
     * @param hdrValue original header
     * @param csMap (if non-null) map of charset aliases that can be used to
     * resolve any charset that is not a canonical one. Key=charset alias,
     * value=canonical name (strings).
     * @param replaceEscChars if TRUE then any '\C' value is replaced by 'C'
     * @return translated value (could be the original, if no translation required)
     * @see RFCHeaderDefinitions#getCanonicalCharsetName(String)
     */
    public static final String decodeHdrValue (final String hdrValue, final Map<String, String> csMap, final boolean replaceEscChars)
    {
    	return decodeHdrValue(hdrValue, EncodedHeaderSection.getEncodedSections(hdrValue), csMap, replaceEscChars);
    }
    /**
     * Decodes a MIME encoded header value, and translates the header charset (if any) to Java representation
     * @param hdrValue original header
     * @param replaceEscChars if TRUE then any '\C' value is replaced by 'C'
     * @return translated value (could be the original, if no translation required)
     */
    public static final String decodeHdrValue (String hdrValue, boolean replaceEscChars)
    {
    	return decodeHdrValue(hdrValue, null, replaceEscChars);
    }
}
