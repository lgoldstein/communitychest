package net.community.chest.io.encode.base64;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.io.StreamCorruptedException;
import java.io.UnsupportedEncodingException;
import java.util.Collection;

import net.community.chest.io.FileUtil;
import net.community.chest.io.IOCopier;
import net.community.chest.util.set.SetsUtils;

/**
 * Implements Base64 encoding and decoding as defined by RFC 2045: "Multipurpose Internet
 * Mail Extensions (MIME) Part One: Format of Internet Message Bodies" page 23.
 * More information about this class is available from <a target="_top" href=
 * "http://ostermiller.org/utils/Base64.html">ostermiller.org</a>.
 *
 * <blockquote>
 * <p>The Base64 Content-Transfer-Encoding is designed to represent
 * arbitrary sequences of octets in a form that need not be humanly
 * readable.  The encoding and decoding algorithms are simple, but the
 * encoded data are consistently only about 33 percent larger than the
 * unencoded data.  This encoding is virtually identical to the one used
 * in Privacy Enhanced Mail (PEM) applications, as defined in RFC 1421.</p>
 *
 * <p>A 65-character subset of US-ASCII is used, enabling 6 bits to be
 * represented per printable character. (The extra 65th character, "=",
 * is used to signify a special processing function.)</p>
 *
 * <p>NOTE:  This subset has the important property that it is represented
 * identically in all versions of ISO 646, including US-ASCII, and all
 * characters in the subset are also represented identically in all
 * versions of EBCDIC. Other popular encodings, such as the encoding
 * used by the uuencode utility, Macintosh binhex 4.0 [RFC-1741], and
 * the base85 encoding specified as part of Level 2 PostScript, do no
 * share these properties, and thus do not fulfill the portability
 * requirements a binary transport encoding for mail must meet.</p>
 *
 * <p>The encoding process represents 24-bit groups of input bits as output
 * strings of 4 encoded characters.  Proceeding from left to right, a
 * 24-bit input group is formed by concatenating 3 8bit input groups.
 * These 24 bits are then treated as 4 concatenated 6-bit groups, each
 * of which is translated into a single digit in the base64 alphabet.
 * When encoding a bit stream via the base64 encoding, the bit stream
 * must be presumed to be ordered with the most-significant-bit first.
 * That is, the first bit in the stream will be the high-order bit in
 * the first 8bit byte, and the eighth bit will be the low-order bit in
 * the first 8bit byte, and so on.</p>
 *
 * <p>Each 6-bit group is used as an index into an array of 64 printable
 * characters.  The character referenced by the index is placed in the
 * output string.  These characters, identified in Table 1, below, are
 * selected so as to be universally representable, and the set excludes
 * characters with particular significance to SMTP (e.g., ".", CR, LF)
 * and to the multipart boundary delimiters defined in RFC 2046 (e.g.,
 * "-").</p>
 * <pre>
 *                  Table 1: The Base64 Alphabet
 *
 *   Value Encoding  Value Encoding  Value Encoding  Value Encoding
 *       0 A            17 R            34 i            51 z
 *       1 B            18 S            35 j            52 0
 *       2 C            19 T            36 k            53 1
 *       3 D            20 U            37 l            54 2
 *       4 E            21 V            38 m            55 3
 *       5 F            22 W            39 n            56 4
 *       6 G            23 X            40 o            57 5
 *       7 H            24 Y            41 p            58 6
 *       8 I            25 Z            42 q            59 7
 *       9 J            26 a            43 r            60 8
 *      10 K            27 b            44 s            61 9
 *      11 L            28 c            45 t            62 +
 *      12 M            29 d            46 u            63 /
 *      13 N            30 e            47 v
 *      14 O            31 f            48 w         (pad) =
 *      15 P            32 g            49 x
 *      16 Q            33 h            50 y
 * </pre>
 * <p>The encoded output stream must be represented in lines of no more
 * than 76 characters each.  All line breaks or other characters no
 * found in Table 1 must be ignored by decoding software.  In base64
 * data, characters other than those in Table 1, line breaks, and other
 * white space probably indicate a transmission error, about which a
 * warning message or even a message rejection might be appropriate
 * under some circumstances.</p>
 *
 * <p>Special processing is performed if fewer than 24 bits are available
 * at the end of the data being encoded.  A full encoding quantum is
 * always completed at the end of a body.  When fewer than 24 input bits
 * are available in an input group, zero bits are added (on the right)
 * to form an integral number of 6-bit groups.  Padding at the end of
 * the data is performed using the "=" character.  Since all base64
 * input is an integral number of octets, only the following cases can
 * arise: (1) the final quantum of encoding input is an integral
 * multiple of 24 bits; here, the final unit of encoded output will be
 * an integral multiple of 4 characters with no "=" padding, (2) the
 * final quantum of encoding input is exactly 8 bits; here, the final
 * unit of encoded output will be two characters followed by two "="
 * padding characters, or (3) the final quantum of encoding input is
 * exactly 16 bits; here, the final unit of encoded output will be three
 * characters followed by one "=" padding character.</p>
 *
 * <p>Because it is used only for padding at the end of the data, the
 * occurrence of any "=" characters may be taken as evidence that the
 * end of the data has been reached (without truncation in transit).  No
 * such assurance is possible, however, when the number of octets
 * transmitted was a multiple of three and no "=" characters are
 * present.</p>
 *
 * <p>Any characters outside of the base64 alphabet are to be ignored in
 * base64-encoded data.</p>
 *
 * <p>Care must be taken to use the proper octets for line breaks if base64
 * encoding is applied directly to text material that has not been
 * converted to canonical form.  In particular, text line breaks must be
 * converted into CRLF sequences prior to base64 encoding.  The
 * important thing to note is that this may be done directly by the
 * encoder rather than in a prior canonicalization step in some
 * implementations.</p>
 *
 * <p>NOTE: There is no need to worry about quoting potential boundary
 * delimiters within base64-encoded bodies within multipart entities
 * because no hyphen characters are used in the base64 encoding.</p>
 * </blockquote>
 *
 * @author Lyor G. - based on code by Stephen Ostermiller (http://ostermiller.org/contact.pl?regarding=Java+Utilities)
 * @since ostermillerutils 1.00.00
 */
public final class Base64 {
	private Base64 ()
	{
		// no instance
	}
    /**
     * Symbol that represents the end of an input stream
     *
     * @since ostermillerutils 1.00.00
     */
    private static final int END_OF_INPUT = -1;
    /**
     * A character that is not a valid base 64 character.
     *
     * @since ostermillerutils 1.00.00
     */
    static final int NON_BASE_64 = -1;
    /**
     * A character that is not a valid base 64 character.
     *
     * @since ostermillerutils 1.00.00
     */
    static final int NON_BASE_64_WHITESPACE = -2;
    /**
     * A character that is not a valid base 64 character.
     *
     * @since ostermillerutils 1.00.00
     */
    static final int NON_BASE_64_PADDING = -3;
    /**
     * Mask value used to extract only lower 6 bits of any value
     */
    public static final int BASE64_MASK_VALUE=0x003F;
    /**
     * Table of the sixty-four characters that are used as
     * the Base64 alphabet: [A-Za-z0-9+/]
     *
     * @since ostermillerutils 1.00.00
     */
    protected static final byte[] base64Chars = {
        'A','B','C','D','E','F','G','H',
        'I','J','K','L','M','N','O','P',
        'Q','R','S','T','U','V','W','X',
        'Y','Z','a','b','c','d','e','f',
        'g','h','i','j','k','l','m','n',
        'o','p','q','r','s','t','u','v',
        'w','x','y','z','0','1','2','3',
        '4','5','6','7','8','9','+','/',
    };
    /**
     * Maximum value of an index into the BASE64 encoding characters table -
     * <B>Note:</B> this value is actually the <U>upper limit</U> - i.e., all
     * the <U>legal</U> values are <U>less</U> than this value (but non-negative)
     */
    public static final byte	MAX_BASE64_ENCODE_VALUE=(byte) base64Chars.length;
    /**
     * @param value value to be checked
     * @return TRUE if this value is an index into the BASE64 encoding characters table
     * @see #MAX_BASE64_ENCODE_VALUE
     */
    public static final boolean isBase64EncodeValue (byte value)
    {
		return (value >= 0) && (value < MAX_BASE64_ENCODE_VALUE);
    }
	/**
	 * Padding character used to complete encoding to a multiple of 4
	 */
	public static final char BASE64_PAD_CHAR='=';
	/**
	 * @param value value to extract the matching BASE64 encoding character 
	 * @return character or {@link #BASE64_PAD_CHAR} if illegal value
	 * @see #isBase64EncodeValue(byte)
	 */
	public static final char getBase64EncodeChar (byte value)
	{
		return isBase64EncodeValue(value) ? (char) base64Chars[value] : BASE64_PAD_CHAR;
	}
    /**
     * Reverse lookup table for the Base64 alphabet.
     * reversebase64Chars[byte] gives n for the nth Base64
     * character or negative if a character is not a Base64 character.
     *
     * @since ostermillerutils 1.00.00
     */
    protected static final byte[] reverseBase64Chars = new byte[0x100];
    // Fill in NON_BASE_64 for all characters to start with
    static
    {
        for (int i=0; i < reverseBase64Chars.length; i++)
            reverseBase64Chars[i] = NON_BASE_64;

        // For characters that are base64Chars, adjust the reverse lookup table.
        for (byte i=0; i < base64Chars.length; i++)
            reverseBase64Chars[base64Chars[i]] = i;

        reverseBase64Chars[' '] = NON_BASE_64_WHITESPACE;
        reverseBase64Chars['\n'] = NON_BASE_64_WHITESPACE;
        reverseBase64Chars['\r'] = NON_BASE_64_WHITESPACE;
        reverseBase64Chars['\t'] = NON_BASE_64_WHITESPACE;
        reverseBase64Chars['\f'] = NON_BASE_64_WHITESPACE;
        reverseBase64Chars[BASE64_PAD_CHAR] = NON_BASE_64_PADDING;
    }
    /**
     * @param ch character to test
     * @return TRUE if character can be a valid output of a BASE64 encoding
     * (including whitespace and padding...)
     */
    public static final boolean isBase64EncodeChar (final char ch)
    {
    	return (('A' <= ch) && (ch <= 'Z'))
    		|| (('a' <= ch) && (ch <= 'z'))
    		|| (('0' <= ch) && (ch <= '9'))
    		|| ('+' == ch)
    		|| ('/' == ch)
    		// including padding
    		|| (BASE64_PAD_CHAR == ch)
    		// including whitespace
    		|| (' ' == ch)
    		|| ('\r' == ch)
    		|| ('\n' == ch)
    		|| ('\f' == ch)
    		|| ('\t' == ch)
    		;
    }

    public static String encodeToString (byte ... bytes)
    {
    	return new String(encode(bytes));
    }
    /**
     * Encode a String in Base64.
     * The String is converted to and from bytes according to the platform's
     * default character encoding.
     * No line breaks or other white space are inserted into the encoded data.
     *
     * @param string The data to encode.
     * @return An encoded String.
     *
     * @since ostermillerutils 1.00.00
     */
    public static String encode (String string)
    {
    	return encodeToString(string.getBytes());
    }
    /**
     * Encode a String in Base64.
     * No line breaks or other white space are inserted into the encoded data.
     *
     * @param string The data to encode.
     * @param enc Character encoding to use when converting to and from bytes.
     * @throws UnsupportedEncodingException if the character encoding specified is not supported.
     * @return An encoded String.
     *
     * @since ostermillerutils 1.00.00
     */
    public static String encode(String string, String enc) throws UnsupportedEncodingException {
        return new String(encode(string.getBytes(enc)), enc);
    }
	/**
	 * Number of bytes for each BASE64 input block
	 */
	public static final int BASE64_INPUT_BLOCK_LEN=3;
	/**
	 * Number of characters for each BASE64 output block
	 */
	public static final int BASE64_OUTPUT_BLOCK_LEN=4;
	/**
	 * @param nSize input size for the BASE64 encoding
	 * @return maximum number characters that will be required to encode the
	 * provide input size (<=0 if initial size is <=0)
	 */
	public static final int getMaxExpectedEncodeSize (final int nSize)
	{
		if (nSize <= 0)
			return nSize;

		final int	numInputBlocks=nSize / BASE64_INPUT_BLOCK_LEN,
					// each input block requires one output block
					outputSize=numInputBlocks * BASE64_OUTPUT_BLOCK_LEN;

		// if not an exact multiple of the input block size, then will need one more output block
		if ((nSize % BASE64_INPUT_BLOCK_LEN) != 0)
			return outputSize + BASE64_OUTPUT_BLOCK_LEN;

		return outputSize;
	}
	/**
	 * @param nSize number of input characters for BASE64 decoding
	 * @return maximum number of bytes/characters that will be required to
	 * store the decoded value (<=0 if initial size is <=0)
	 */
	public static final int getMaxExpectedDecodeSize (final int nSize)
	{
		if (nSize <= 0)
			return nSize;

		final int	numOutputBlocks=nSize / BASE64_OUTPUT_BLOCK_LEN,
					// each output block yields one input block
					outputSize=numOutputBlocks * BASE64_INPUT_BLOCK_LEN;
		// this should not really happen, but take it into account
		if ((nSize % BASE64_OUTPUT_BLOCK_LEN) != 0)
			return outputSize + BASE64_INPUT_BLOCK_LEN;

		return outputSize;
	}
    /**
     * Encode bytes in Base64.
     * No line breaks or other white space are inserted into the encoded data.
     *
     * @param bytes The data to encode.
     * @param startOffset The offset within the buffer to start with
     * @param len the number of bytes to encode
     * @return Encoded bytes.
     *
     * @since ostermillerutils 1.00.00
     */
    public static byte[] encode (byte[] bytes, int startOffset, int len)
    {
        // calculate the length of the resulting output.
        // in general it will be 4/3 the size of the input
        // but the input length must be divisible by three.
        // If it isn't the next largest size that is divisible
        // by three is used.
        int length = len;
        int mod=length % BASE64_INPUT_BLOCK_LEN;
        if (mod != 0)
            length += BASE64_INPUT_BLOCK_LEN - mod;
        length = ((length * BASE64_OUTPUT_BLOCK_LEN) / BASE64_INPUT_BLOCK_LEN);

        try(ByteArrayOutputStream out=new ByteArrayOutputStream(length)) {
            try(Base64EncodeOutputStream  bos=new Base64EncodeOutputStream(out, null, true)) {
                bos.write(bytes, startOffset, len);
            }

            return out.toByteArray();
        } catch(IOException x) {
            // This can't happen.
            // The input and output streams were constructed
            // on memory structures that don't actually use IO.
            throw new RuntimeException(x);
        }
    }
	/**
	 * Encode bytes in Base64.
	 * No line breaks or other white space are inserted into the encoded data.
	 *
	 * @param bytes The data to encode.
	 * @return Encoded bytes.
	 *
	 * @since ostermillerutils 1.00.00
	 */
	public static byte[] encode (byte ... bytes)
	{
		return encode(bytes, 0, bytes.length);
	}
	/**
	 * Standard BASE64 encoding line break len
	 */
	public static final int ENCLINE_STDLEN=76;
	/**
     * Encode data from the InputStream to the OutputStream in Base64.
     * @param in Stream from which to read data that needs to be encoded.
     * @param out Stream to which to write encoded data.
     * @param options options mask for encoding
     * @throws IOException if there is a problem reading or writing.
     * @since ostermillerutils 1.00.00
     */
    public static void encode (InputStream in, OutputStream out, Collection<Base64EncodeOptions> options) throws IOException
    {
    	final Base64EncodeOutputStream	bos=new Base64EncodeOutputStream(out, options, true);
    	for (int	v=in.read(); v != (-1); v=in.read())
    		bos.write(v);

    	FileUtil.closeAll(bos);	// finalize encoding
        out.flush();
    }

    public static void encode (InputStream in, OutputStream out, Base64EncodeOptions ... options) throws IOException
    {
    	encode(in, out, SetsUtils.setOf(options));
    }
    /**
     * Encode data from the InputStream to the OutputStream in Base64.
     * Line breaks are inserted every 76 characters in the output.
     *
     * @param in Stream from which to read data that needs to be encoded.
     * @param out Stream to which to write encoded data.
     * @throws IOException if there is a problem reading or writing.
     * @since ostermillerutils 1.00.00
     */
    public static void encode (InputStream in, OutputStream out) throws IOException
    {
    	encode(in, out, Base64EncodeOptions.BREAK);
    }
    /**
     * Decode a Base64 encoded String.
     * Characters that are not part of the Base64 alphabet are ignored
     * in the input.
     * The String is converted to and from bytes according to the platform's
     * default character encoding.
     *
     * @param string The data to decode.
     * @return A decoded String.
     * @throws IOException
     *
     * @since ostermillerutils 1.00.00
     */
    public static String decode (String string) throws IOException
    {
        return new String(decode(string.getBytes()));
    }
    /**
     * Decode a Base64 encoded String.
     * Characters that are not part of the Base64 alphabet are ignored
     * in the input.
     *
     * @param string The data to decode.
     * @param enc Character encoding to use when converting to and from bytes.
     * @throws UnsupportedEncodingException if the character encoding specified is not supported.
     * @throws IOException
     * @return A decoded String.
     *
     * @since ostermillerutils 1.00.00
     */
    public static String decode (String string, String enc) throws IOException, UnsupportedEncodingException
    {
        return new String(decode(string.getBytes(enc)), enc);
    }
    /**
     * Decode a Base64 encoded String.
     * Characters that are not part of the Base64 alphabet are ignored
     * in the input.
     *
     * @param string The data to decode.
     * @param encIn Character encoding to use when converting input to bytes (should not matter because Base64 data is designed to survive most character encodings)
     * @param encOut Character encoding to use when converting decoded bytes to output.
     * @throws UnsupportedEncodingException if the character encoding specified is not supported.
     * @throws IOException
     * @return A decoded String.
     *
     * @since ostermillerutils 1.00.00
     */
    public static String decode (String string, String encIn, String encOut) throws IOException, UnsupportedEncodingException
    {
        return new String(decode(string.getBytes(encIn)), encOut);
    }
    /**
     * Decode a Base64 encoded String.
     * Characters that are not part of the Base64 alphabet are ignored
     * in the input.
     * The String is converted to and from bytes according to the platform's
     * default character encoding.
     *
     * @param string The data to decode.
     * @return A decoded String.
     * @throws IOException
     * @since ostermillerutils 1.02.16
     */
    public static String decodeToString (String string) throws IOException
    {
        return new String(decode(string.getBytes()));
    }
    /**
     * Decode a Base64 encoded String.
     * Characters that are not part of the Base64 alphabet are ignored
     * in the input.
     *
     * @param string The data to decode.
     * @param enc Character encoding to use when converting to and from bytes.
     * @throws UnsupportedEncodingException if the character encoding specified is not supported.
     * @throws IOException
     * @return A decoded String.
     *
     * @since ostermillerutils 1.02.16
     */
    public static String decodeToString (String string, String enc) throws IOException, UnsupportedEncodingException
    {
        return new String(decode(string.getBytes(enc)), enc);
    }
    /**
     * Decode a Base64 encoded String.
     * Characters that are not part of the Base64 alphabet are ignored
     * in the input.
     *
     * @param string The data to decode.
     * @param encIn Character encoding to use when converting input to bytes (should not matter because Base64 data is designed to survive most character encodings)
     * @param encOut Character encoding to use when converting decoded bytes to output.
     * @throws UnsupportedEncodingException if the character encoding specified is not supported.
     * @throws IOException
     * @return A decoded String.
     *
     * @since ostermillerutils 1.02.16
     */
    public static String decodeToString (String string, String encIn, String encOut) throws IOException, UnsupportedEncodingException
    {
        return new String(decode(string.getBytes(encIn)), encOut);
    }
    /**
     * Decode a Base64 encoded String to an OutputStream.
     * Characters that are not part of the Base64 alphabet are ignored
     * in the input.
     * The String is converted from bytes according to the platform's
     * default character encoding.
     *
     * @param string The data to decode.
     * @param out Stream to which to write decoded data.
     * @throws IOException if an IO error occurs.
     *
     * @since ostermillerutils 1.02.16
     */
    public static void decodeToStream (String string, OutputStream out) throws IOException
    {
        decode(new ByteArrayInputStream(string.getBytes()), out);
    }
    /**
     * Decode a Base64 encoded String to an OutputStream.
     * Characters that are not part of the Base64 alphabet are ignored
     * in the input.
     *
     * @param string The data to decode.
     * @param enc Character encoding to use when converting to and from bytes.
     * @param out Stream to which to write decoded data.
     * @throws UnsupportedEncodingException if the character encoding specified is not supported.
     * @throws IOException if an IO error occurs.
     *
     * @since ostermillerutils 1.02.16
     */
    public static void decodeToStream (String string, String enc, OutputStream out) throws UnsupportedEncodingException, IOException
    {
        decode(new ByteArrayInputStream(string.getBytes(enc)), out);
    }
    /**
     * Decode a Base64 encoded String.
     * @param string The data to decode.
     * @param throwException - if FALSE then characters that are not part of the Base64 alphabet are ignored
     * in the input.
     * @return decoded data.
     * @throws IOException
     */
    public static byte[] decodeToBytes (String string, boolean throwException) throws IOException
    {
    	return decode(string.getBytes(), throwException);
    }
    /**
     * Decode a Base64 encoded String.
     * Characters that are not part of the Base64 alphabet cause exception to be thrown
     * The String is converted from bytes according to the platform's
     * default character encoding.
     *
     * @param string The data to decode.
     * @return decoded data.
     * @throws IOException
     * @since ostermillerutils 1.02.16
     * @see Base64#decodeToBytes(String string, boolean throwException)
     */
    public static byte[] decodeToBytes (String string) throws IOException
    {
        return decodeToBytes(string, true);
    }
    /**
     * Decode a Base64 encoded String.
     * Characters that are not part of the Base64 alphabet are ignored
     * in the input.
     *
     * @param string The data to decode.
     * @param enc Character encoding to use when converting from bytes.
     * @throws UnsupportedEncodingException if the character encoding specified is not supported.
     * @throws IOException
     * @return decoded data.
     *
     * @since ostermillerutils 1.02.16
     */
    public static byte[] decodeToBytes (String string, String enc) throws IOException, UnsupportedEncodingException
    {
        return decode(string.getBytes(enc));
    }
    /**
     * Decode Base64 encoded bytes.
     * Characters that are not part of the Base64 alphabet are ignored
     * in the input.
     * The String is converted to bytes according to the platform's
     * default character encoding.
     *
     * @param bytes The data to decode.
     * @return A decoded String.
     * @throws IOException
     * @since ostermillerutils 1.02.16
     */
    public static String decodeToString (byte ... bytes)throws IOException
    {
        return new String(decode(bytes));
    }
    /**
     * Decode Base64 encoded bytes.
     * Characters that are not part of the Base64 alphabet are ignored
     * in the input.
     *
     * @param bytes The data to decode.
     * @param enc Character encoding to use when converting to and from bytes.
     * @throws UnsupportedEncodingException if the character encoding specified is not supported.
     * @throws IOException
     * @return A decoded String.
     *
     * @since ostermillerutils 1.02.16
     */
    public static String decodeToString (byte[] bytes, String enc) throws IOException, UnsupportedEncodingException
    {
        return new String(decode(bytes), enc);
    }
    /**
     * @param bytes
     * @param throwException if FALSE then characters that are not part of the
     * Base64 alphabet are ignored in the input.
     * @return decoded bytes
     * @throws IOException
     */
    public static byte[] decodeToBytes (byte[] bytes, boolean throwException) throws IOException
    {
    	return decode(bytes, throwException);
    }
    /**
     * Decode Base64 encoded bytes.
     * @param bytes The data to decode.
     * @throws IOException
     * @return Decoded bytes.
     * @see Base64#decodeToBytes(byte[] bytes, boolean throwException)
     * @since ostermillerutils 1.02.16
     */
    public static byte[] decodeToBytes (byte ... bytes) throws IOException
    {
        return decodeToBytes(bytes, true);
    }
    /**
     * Decode Base64 encoded bytes.
     * Characters that are not part of the Base64 alphabet are ignored
     * in the input.
     *
     * @param bytes The data to decode.
     * @param throwExceptions if TRUE and errors found, then exception is thrown
     * @return Decoded bytes.
     * @throws IOException if errors (besides if "throwExceptions"
     *
     * @since ostermillerutils 1.00.00
     */
    public static byte[] decode (byte[] bytes, boolean throwExceptions) throws IOException
    {
    	InputStream				in=null;
    	ByteArrayOutputStream	out=null;
    	try
    	{
    		in=new ByteArrayInputStream(bytes);
	        // calculate the length of the resulting output.
	        // in general it will be at most 3/4 the size of the input
	        // but the input length must be divisible by four.
	        // If it isn't the next largest size that is divisible
	        // by four is used.
	        int mod;
	        int length = bytes.length;
	        if ((mod = length % BASE64_OUTPUT_BLOCK_LEN) != 0){
	            length += BASE64_OUTPUT_BLOCK_LEN - mod;
	        }
	        length = ((length * BASE64_INPUT_BLOCK_LEN) / BASE64_OUTPUT_BLOCK_LEN);
	        out = new ByteArrayOutputStream(length);
	        decode(in, out, throwExceptions);

	        final byte[]	decBytes=out.toByteArray();
	        return decBytes;
    	}
    	finally
    	{
    		FileUtil.closeAll(in, out);
    	}
    }

    public static byte[] decode (byte ... bytes) throws IOException
    {
    	return decode(bytes, true);
    }

    public static void decode (byte[] bytes, int offset, int len, OutputStream out) throws IOException
    {
    	ByteArrayInputStream in=new ByteArrayInputStream(bytes, offset, len);
    	try
    	{
    		decode(in, out, false);
    	}
    	finally
    	{
    		FileUtil.closeAll(in);
    	}
    }
    /**
     * Decode Base64 encoded bytes to the an OutputStream.
     * Characters that are not part of the Base64 alphabet are ignored
     * in the input.
     *
     * @param bytes The data to decode.
     * @param out Stream to which to write decoded data.
     * @throws IOException if an IO error occurs.
     *
     * @since ostermillerutils 1.00.00
     */
    public static void decode (byte[] bytes, OutputStream out) throws IOException
    {
    	if ((bytes != null) && (bytes.length > 0))
    		decode(bytes, 0, bytes.length, out);
    }
    /**
     * Reads the next (decoded) Base64 character from the input stream.
     * Non Base64 characters are skipped.
     *
     * @param in Stream from which bytes are read.
     * @param throwExceptions Throw an exception if an unexpected character is encountered.
     * @return the next Base64 character from the stream or -1 if there are no more Base64 characters on the stream.
     * @throws IOException if an IO Error occurs (also Base64DecodingException if unexpected data
     * is encountered when throwExceptions is specified).
     *
     * @since ostermillerutils 1.00.00
     */
    private static final int readBase64 (InputStream in, boolean throwExceptions) throws IOException {
        int read=0, numPadding = 0;
        do {
            read = in.read();
            if (read == END_OF_INPUT)
	            return END_OF_INPUT;

            if ((read < 0) || (read > reverseBase64Chars.length))
            	throw new IOException("Read BASE64 character out of range: " + read);

            read = reverseBase64Chars[read];
            if (throwExceptions && ((read == NON_BASE_64) || ((numPadding > 0) && (read > NON_BASE_64))))
                throw new Base64DecodingException("unexpectedchar", (char)read);

            if (read == NON_BASE_64_PADDING)
            {
                numPadding++;

                // check if too much padding supplied
                if ((numPadding >= BASE64_INPUT_BLOCK_LEN) && throwExceptions)
                	throw new Base64DecodingException("too much padding at end of block", BASE64_PAD_CHAR);
            }
        } while (read <= NON_BASE_64);

        return read;
    }
    /**
     * Decode Base64 encoded data from the InputStream to a byte array.
     * Characters that are not part of the Base64 alphabet are ignored
     * in the input.
     *
     * @param in Stream from which to read data that needs to be decoded.
     * @return decoded data.
     * @throws IOException if an IO error occurs.
     *
     * @since ostermillerutils 1.00.00
     */
    public static byte[] decodeToBytes (InputStream in) throws IOException
    {
        ByteArrayOutputStream out=null;
        try
        {
        	out = new ByteArrayOutputStream();
        	decode(in, out, false);
        	
        	final byte[]	resBytes=out.toByteArray();
        	return resBytes;
        }
        finally
        {
        	FileUtil.closeAll(out);
    	}
    }
    /**
     * Decode Base64 encoded data from the InputStream to a String.
     * Characters that are not part of the Base64 alphabet are ignored
     * in the input.
     * Bytes are converted to characters in the output String according to the platform's
     * default character encoding.
     *
     * @param in Stream from which to read data that needs to be decoded.
     * @return decoded data.
     * @throws IOException if an IO error occurs.
     *
     * @since ostermillerutils 1.02.16
     */
    public static String decodeToString (InputStream in) throws IOException
    {
        return new String(decodeToBytes(in));
    }
    /**
     * Decode Base64 encoded data from the InputStream to a String.
     * Characters that are not part of the Base64 alphabet are ignored
     * in the input.
     *
     * @param in Stream from which to read data that needs to be decoded.
     * @param enc Character encoding to use when converting bytes to characters.
     * @return decoded data.
     * @throws IOException if an IO error occurs (also UnsupportedEncodingException
     * if the character encoding specified is not supported.
     *
     * @since ostermillerutils 1.02.16
     */
    public static String decodeToString (InputStream in, String enc) throws IOException
    {
        return new String(decodeToBytes(in), enc);
    }
    /**
     * Decode Base64 encoded data from the InputStream to the OutputStream.
     * Characters in the Base64 alphabet, white space and equals sign are
     * expected to be in urlencoded data.  The presence of other characters
     * could be a sign that the data is corrupted.
     *
     * @param in Stream from which to read data that needs to be decoded.
     * @param out Stream to which to write decoded data.
     * @throws IOException if an IO error occurs (also Base64DecodingException
     * if unexpected data is encountered and throwing is enabled).
     *
     * @since ostermillerutils 1.00.00
     */
    public static void decode (InputStream in, OutputStream out) throws IOException
    {
        decode(in, out, true);
    }
	/**
	 * Decodes a BASE64 output block and writes the result to the output stream.
	 * @param inBuffer buffer containing the data to be decoded - Note: it must be AT LEAST (!) of length
	 * BASE64_OUTPUT_BLOCK_LEN (any data beyond that is ignored), and must contain only VALID BASE64 characters.
	 * While the length is not checked, any buffer size smaller than BASE64_OUTPUT_BLOCK_LEN will cause an
	 * "array-out-of-bounds" exception
	 * @param out output stream to write to
	 * @return TRUE if end of encoding found (i.e. END-OO-INPUT
	 * @throws IOException
	 */
    protected static boolean decodeBase64Block (final int[] inBuffer, OutputStream out)  throws IOException
    {
		// Calculate the output
		// The first two bytes of our in buffer should always be valid (if not, then this is end of input)
		// but we must check to make sure the other two bytes
		// are not END_OF_INPUT before using them.
		// The basic idea is that the four bytes will get reconstituted
		// into three bytes along these lines:
		// [xxAAAAAA] [xxBBBBBB] [xxCCCCCC] [xxDDDDDD]
		//      [AAAAAABB] [BBBBCCCC] [CCDDDDDD]
		// bytes are considered to be zero when absent.

		final int[]   outBuffer={
			// six A and two B
			((END_OF_INPUT == inBuffer[0]) || (END_OF_INPUT == inBuffer[1])) ? END_OF_INPUT : (((inBuffer[0] << 2) | (inBuffer[1] >> 4)) & 0x00FF),
			// four B and four C
			(END_OF_INPUT == inBuffer[2]) ? END_OF_INPUT : (((inBuffer[1] << 4) | (inBuffer[2] >> 2)) & 0x00FF),
			// two C and six D
			(END_OF_INPUT == inBuffer[3]) ? END_OF_INPUT : (((inBuffer[2] << 6) | inBuffer[3]) & 0x00FF)
		};

		for (int    i=0; i < outBuffer.length; i++)
			if (END_OF_INPUT == outBuffer[i])
                return true; // end of encoding found
			else
				out.write(outBuffer[i]);

		return false;   // not end of encoding
    }
    /**
     * Reads a BASE64 block from the input stream
     * @param in input stream to read from
     * @param inBuffer buffer to store data into it - NOTE: buffer must be of at least (!) BASE64_OUTPUT_BLOCK_LEN size
     * (otherwise an "array-out-of-bounds" exception will be caused)
     * @param throwExceptions if TRUE then exception is thrown if non-BASE64 character encountered
     * @return TRUE if the first 2 characters are <B><U>NOT</U></B> valid BASE64 characters
     * @throws IOException if read error
     */
	protected static boolean readBase64Block (final InputStream in, final int[] inBuffer, final boolean throwExceptions) throws IOException
	{
	    int nPos=0;

	    // read up to BASE64_OUTPUT_BLOCK_LEN or END_OF_INPUT - whichever comes first
	    for ( ; nPos < BASE64_OUTPUT_BLOCK_LEN; nPos++)
		    if (END_OF_INPUT == (inBuffer[nPos]=readBase64(in, throwExceptions)))
			    break;

	    // fill-in all other data with "END_OF_INPUT" just be sure
	    for (int    aPos=nPos; aPos < BASE64_OUTPUT_BLOCK_LEN; aPos++)
		    inBuffer[aPos] =  END_OF_INPUT;

	    // TRUE if the first 2 characters are NOT valid BASE64 characters
	    return (nPos < 2);
	}
    /**
     * Decode Base64 encoded data from the InputStream to the OutputStream.
     * Characters in the Base64 alphabet, white space and equals sign are
     * expected to be in urlencoded data.  The presence of other characters
     * could be a sign that the data is corrupted.
     *
     * @param in Stream from which to read data that needs to be decoded.
     * @param out Stream to which to write decoded data.
     * @param throwExceptions Whether to throw exceptions when unexpected data is encountered.
     * @throws IOException if an IO error occurs (also Base64DecodingException if unexpected
     * data is encountered when throwExceptions is specified).
     *
     * @since ostermillerutils 1.00.00
     */
    public static void decode (InputStream in, OutputStream out, boolean throwExceptions) throws IOException
    {
        // Base64 decoding converts four bytes of input to three bytes of output
        final int[] inBuffer = new int[BASE64_OUTPUT_BLOCK_LEN];

        // read bytes unmapping them from their ASCII encoding in the process
        // we must read at least two bytes to be able to output anything
        for (boolean done=readBase64Block(in, inBuffer, throwExceptions); !done; done=readBase64Block(in, inBuffer, throwExceptions))
	        if (decodeBase64Block(inBuffer, out))
		        break;

        out.flush();
    }
    /**
     * Determines if the byte array is in base64 format.
     * <p>
     * Data will be considered to be in base64 format if it contains
     * only base64 characters and whitespace with equals sign padding
     * on the end so that the number of base64 characters is divisible
     * by four.
     * <p>
     * It is possible for data to be in base64 format but for it to not
     * meet these stringent requirements.  It is also possible for data
     * to meet these requirements even though decoding it would not make
     * any sense.  This method should be used as a guide but it is not
     * authoritative because of the possibility of these false positives
     * and false negatives.
     * <p>
     * Additionally, extra data such as headers or footers may throw
     * this method off the scent and cause it to return false.
     *
     * @param bytes data that could be in base64 format.
     * @return TRUE if bytes contain a valid BASE64 encoding
     * @since ostermillerutils 1.00.00
     */
    public static boolean isBase64 (final byte ... bytes)
    {
        try
        {
            return isBase64(new ByteArrayInputStream(bytes));
        }
        catch (IOException x)
        {
            // This can't happen.
            // The input and output streams were constructed
            // on memory structures that don't actually use IO.
            return false;
        }
    }
    /**
     * Determines if the String is in base64 format.
     * <p>
     * Data will be considered to be in base64 format if it contains
     * only base64 characters and whitespace with equals sign padding
     * on the end so that the number of base64 characters is divisible
     * by four.
     * <p>
     * It is possible for data to be in base64 format but for it to not
     * meet these stringent requirements.  It is also possible for data
     * to meet these requirements even though decoding it would not make
     * any sense.  This method should be used as a guide but it is not
     * authoritative because of the possibility of these false positives
     * and false negatives.
     * <p>
     * Additionally, extra data such as headers or footers may throw
     * this method off the scent and cause it to return false.
     *
     * @param string String that may be in base64 format.
     * @param enc Character encoding to use when converting to bytes.
     * @return Best guess as to whether the data is in base64 format.
     * @throws UnsupportedEncodingException if the character encoding specified is not supported.
     */
    public static boolean isBase64 (String string, String enc) throws UnsupportedEncodingException
    {
        return isBase64(string.getBytes(enc));
    }
    /**
     * Determines if the String is in base64 format.
     * The String is converted to and from bytes according to the platform's
     * default character encoding.
     * <p>
     * Data will be considered to be in base64 format if it contains
     * only base64 characters and whitespace with equals sign padding
     * on the end so that the number of base64 characters is divisible
     * by four.
     * <p>
     * It is possible for data to be in base64 format but for it to not
     * meet these stringent requirements.  It is also possible for data
     * to meet these requirements even though decoding it would not make
     * any sense.  This method should be used as a guide but it is not
     * authoritative because of the possibility of these false positives
     * and false negatives.
     * <p>
     * Additionally, extra data such as headers or footers may throw
     * this method off the scent and cause it to return false.
     *
     * @param string String that may be in base64 format.
     * @return Best guess as to whether the data is in base64 format.
     * @throws UnsupportedEncodingException (never since UTF-8 is supported)
     * @since ostermillerutils 1.00.00
     */
    public static boolean isBase64 (String string) throws UnsupportedEncodingException
    {
        return isBase64(string, "US-ASCII");
    }
    /**
     * Reads data from the stream and determines if it is
     * in base64 format.
     * <p>
     * Data will be considered to be in base64 format if it contains
     * only base64 characters and whitespace with equals sign padding
     * on the end so that the number of base64 characters is divisible
     * by four.
     * <p>
     * It is possible for data to be in base64 format but for it to not
     * meet these stringent requirements.  It is also possible for data
     * to meet these requirements even though decoding it would not make
     * any sense.  This method should be used as a guide but it is not
     * authoritative because of the possibility of these false positives
     * and false negatives.
     * <p>
     * Additionally, extra data such as headers or footers may throw
     * this method off the scent and cause it to return false.
     *
     * @param in Stream from which to read data to be tested.
     * @return Best guess as to whether the data is in base64 format.
     * @throws IOException if an I/O error occurs.
     *
     * @since ostermillerutils 1.00.00
     */
    public static boolean isBase64 (final InputStream in) throws IOException
    {
        long numBase64Chars=0L;
        for (int	read=in.read(), numPadding=0; read != -1; read=in.read())
        {
        	if ((read < 0) || (read >= reverseBase64Chars.length))
        		return false;

        	read = reverseBase64Chars[read];
            if (NON_BASE_64 == read)
                return false;
            if (NON_BASE_64_WHITESPACE == read)
            	continue;	// skip whitespace

            if (NON_BASE_64_PADDING == read)
            {
                numPadding++;
                numBase64Chars++;
            }
            else if (numPadding > 0)	// valid BASE64 character after padding is illegal
                return false;
            else
                numBase64Chars++;
        }

        if (numBase64Chars <= 0L)
	        return false;
        // number of characters MUST be a full multiple of a BASE64 block
        if ((numBase64Chars % BASE64_OUTPUT_BLOCK_LEN) != 0)
	        return false;
        return true;
    }
    /**
     * @param nRawSize raw data size to be encoded
     * @param options take into account line breaks, and if so are they CRLF
     * @return required buffer size to hold BASE64 encoding of specified raw size
     */
	public static int calculateEncodedSize (int nRawSize, Collection<Base64EncodeOptions> options)
	{
		int	ulB64Size=0, ulB64Blocks=0;

		// calculate expected size if we encode "pure" MIME (w/o CRLF)
		ulB64Blocks = (nRawSize / BASE64_INPUT_BLOCK_LEN);
		if ((nRawSize % BASE64_INPUT_BLOCK_LEN) != 0)
			ulB64Blocks++;
		ulB64Size = (ulB64Blocks * BASE64_OUTPUT_BLOCK_LEN);

		// adjust expected output size if required CRLF
		if ((options != null) && options.contains(Base64EncodeOptions.BREAK))
		{
			int ulB64Lines=(ulB64Size / ENCLINE_STDLEN);
			int ulB64LastLine=(ulB64Size % ENCLINE_STDLEN);

			/* check if last line complete */
			if (ulB64LastLine != 0)
				ulB64Lines++;

			/* take into account CR/LF at end of each line */
			if (options.contains(Base64EncodeOptions.CRLF))
				ulB64Size += (ulB64Lines * 2);
			else
				ulB64Size += ulB64Lines;
		}

		return ulB64Size;
	}
	/**
	 * Calculates the expected decoded size of a BASE64 encoded data
	 * @param ulEncSize encoded size
	 * @param ulLineLen line length (used only if ENCOPT_BREAK_LINES option specified)
	 * @param options original options used to encode
	 * @return expected decoded size (within 2 bytes due to last block ambiguity!!!)
	 */
	public static int calculateDecodedSize (final int ulEncSize, final int ulLineLen, final Collection<Base64EncodeOptions> options)
	{
		final boolean	breakLines=(options != null) && options.contains(Base64EncodeOptions.BREAK);
		if ((ulLineLen <= 0) || (!breakLines))
			return (ulEncSize * BASE64_INPUT_BLOCK_LEN) / BASE64_OUTPUT_BLOCK_LEN;

		// extra number of characters added for CRLF
		final boolean	useCRLF=(options != null) && options.contains(Base64EncodeOptions.CRLF);
		final int		b64XtraLen=useCRLF ? 2 : 1;
		final int		ulB64FLL= ulLineLen + b64XtraLen;
		// take into account CRLF when calculating number of full lines
		int		ulB64Lines=(ulEncSize / ulB64FLL);
		// number of bytes taken by full lines
		int 	ulB64LSize = (ulB64Lines * ulB64FLL);
		// remainder - non-full line
		int		ulB64RemLen = ulEncSize - ulB64LSize;

		// calculate "pure" B64 size - w/o the CRLF(s)
		ulB64LSize -= (ulB64Lines * b64XtraLen);
		ulB64LSize += ulB64RemLen;

		// number of decode blocks * each encode block yields one decoded block
		return (ulB64LSize * BASE64_INPUT_BLOCK_LEN) / BASE64_OUTPUT_BLOCK_LEN;
	}
	/**
	 * Calculates the expected decoded size of a BASE64 encoded data
	 * @param ulEncSize encoded size - assume standard line length and CRLF break between lines
	 * @return expected decoded size (within 2 bytes due to last block ambiguity!!!)
	 */
	public static int calculateDecodedSize (final int ulEncSize)
	{
		return calculateDecodedSize(ulEncSize, ENCLINE_STDLEN, Base64EncodeOutputStream.DEFAULT_OPTIONS);
	}

	public static final <A extends Appendable> A append (byte[] hb, int startIndex, int len, A sb) throws UnsupportedEncodingException, IOException
	{
		if (null == sb)
			throw new IOException("No " + Appendable.class.getSimpleName() + " instance");

		final byte[]  b64Enc=encode(hb, startIndex, len);
		final String  b64Val=new String(b64Enc, "US-ASCII");
		sb.append(b64Val);
		return sb;
	}

	public static final <A extends Appendable> A append (A sb, byte ... hb) throws UnsupportedEncodingException, IOException
	{
		return append(hb, 0, ((null == hb) ? 0 : hb.length), sb);
	}

	public static final void serialize (final OutputStream out, final Serializable obj, final Collection<Base64EncodeOptions> options) throws IOException
	{
		if (null == out)
			throw new IOException("No " + OutputStream.class.getSimpleName() + " provided");
		if (null == obj)
			throw new StreamCorruptedException("No " + Serializable.class.getSimpleName() + " instance");

		ObjectOutputStream	so=null;
		try
		{
			so = new ObjectOutputStream(new Base64EncodeOutputStream(out, IOCopier.DEFAULT_COPY_SIZE, options, true, false));
			so.writeObject(obj);
		}
		finally
		{
			FileUtil.closeAll(so);
		}
	}

	public static final void serialize (final OutputStream out, final Serializable obj, final Base64EncodeOptions ... options) throws IOException
	{
		serialize(out, obj, SetsUtils.setOf(options));
	}
}
