package net.community.chest.mail;

import java.io.IOException;

import net.community.chest.reflect.ClassUtil;

/**
 * <P>Copyright 2007 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Aug 22, 2007 9:42:22 AM
 */
public final class RFCMimeDefinitions {
    private RFCMimeDefinitions ()
    {
        // disable instantiation
    }

    /*---------------- MIME related strings -------------*/
    public static final char[] MIMEBoundaryDelimsChars={ '-', '-' };
    public static final byte[] MIMEBoundaryDelimsBytes={ (byte) '-', (byte) '-' };
    public static final String MIMEBoundaryDelims=String.valueOf(MIMEBoundaryDelimsChars);

    public static final char RFC822_MIMETAG_SEP='/';
    /**
     * Appends the MIME tag generated from specified type/sub-type
     * @param <A> The {@link Appendable} generic type
     * @param sb {@link Appendable} instance to append to
     * @param type MIME type - may NOT be null/empty
     * @param subType MIME sub-type - may NOT be null/empty
     * @return same as input instance
     * @throws IOException if cannot append data
     */
    public static final <A extends Appendable> A appendMIMETag (A sb, String type, String subType) throws IOException
    {
        if ((null == type) || (null == subType) ||
            (type.length() <= 0) || (subType.length() <= 0))
            return sb;
        if (null == sb)
            throw new IOException(ClassUtil.getArgumentsExceptionLocation(RFCMimeDefinitions.class, "appendMIMETag", type, subType) + " no " + Appendable.class.getName() + ") instance");

        // not expecting any failures so exception if they happen
        sb.append(type).append(RFC822_MIMETAG_SEP).append(subType);
        return sb;
    }
    /**
     * Builds a MIME tag from the specified type/sub-type
     * @param type MIME type - may NOT be null/empty
     * @param subType MIME sub-type - may NOT be null/empty
     * @return MIME tag (null if null/empty type/sub-type)
     */
    public static final String buildMIMETag (String type, String subType)
    {
        final int    typeLen=(null == type) ? 0 : type.length(),
                    subTypeLen=(null == subType) ? 0 : subType.length();
        if ((typeLen <= 0) || (subTypeLen <= 0))
            return null;

        try
        {
            final StringBuilder    sb=appendMIMETag(new StringBuilder(typeLen + subTypeLen + 4 /* a bit more */), type, subType);
            return sb.toString();    // just so we have a debug breakpoint
        }
        catch(IOException e)
        {
            // should not happen since StringBuilder does not throw any Appendable exceptions
            throw new UnsupportedOperationException(e);
        }
    }

        /* some standard sub-types */
    public static final String MIMEApplicationType="application";
    public static final String MIMEMultipartType="multipart";
    public static final String MIMEAudioType="audio";
    public static final String MIMEImageType="image";
    public static final String MIMETextType="text";
    public static final String MIMEMessageType="message";

    public static final String MIMEMixedSubType="mixed";
    public static final String MIMEOctetStreamSubType="octet-stream";
    public static final String MIMETiffSubType="tiff";
    public static final String MIMEPlainSubType="plain";
    public static final String MIMEHtmlSubType="html";
    public static final String MIMEXmlSubType="xml";
    public static final String MIMEAlternativeSubType="alternative";
    public static final String MIMERfc822SubType="rfc822";
    public static final String MIMEVoiceMsgSubType="voice-message";
    public static final String MIMEFaxMsgSubType="fax-message";
    public static final String MIMEDirectorySubType="directory";
    public static final String MIME32KADPCMSubType="32KADPCM";
    public static final String MIMEReportSubType="report";
    public static final String MIMEDlvryStatusSubType="delivery-status";
    public static final String MIMERelatedSubType="related";
    public static final String MIMEParallelSubType="parallel";
    public static final String MIMEDigestSubType="digest";
    public static final String MIMEPngSubType="png";
    public static final String MIMEGifSubType="gif";
    public static final String MIMEAmrSubType="amr";
    public static final String MIMEWaveSubType="wav";
    public static final String MIMEVoxSubType="vox";
    public static final String MIMEMSGSMSubType="msgsm";
    public static final String MIMEBasicSubType="basic";
    public static final String MIMEJpegSubType="jpeg";
    public static final String MIMEPJpegSubType="pjpeg";

        /* some non-standard multipart sub-types */
    public static final String MIMEAppleDoubleSubType="appledouble";
    public static final String MIMEMSTNEFSubType="ms-tnef";
    public static final String MIMESMSMsgSubType="sms-message";

        /* some widely used MIME related keywords */
    public static final String MIMEBoundaryKeyword="boundary";
    public static final String MIMEFilenameKeyword="filename";
    public static final String MIMECharsetKeyword="charset";
    public static final String MIMENameKeyword="name";
    public static final String MIMEProfileKeyword="profile";
    public static final String MIMEVoiceKeyword="voice";
    public static final String MIMEVversionKeyword="version";
    public static final String MIMETypeKeyword="type";
    public static final String MIMEStartKeyword="start";
    public static final String MIMECodecKeyword="codec";
    public static final String MIMEAttachmentDisp="attachment";
    public static final String MIMEInlineDisp="inline";
    /**
     * Character used to delimit lists of attribute/value pairs
     */
    public static final char RFC822_ATTRS_LIST_DELIM=';';
    /**
     * Character used to delimit the value of an attribute/value pair
     */
    public static final char RFC822_KEYWORD_VALUE_DELIM='=';

    public static final char     MAIL_DOMAIN_SEPARATOR_CHAR='@';
    public static final String    MAIL_DOMAIN_SEPARATOR=String.valueOf(MAIL_DOMAIN_SEPARATOR_CHAR);
    public static final char    MAIL_ADDR_START_DELIM='<', MAIL_ADDR_END_DELIM='>';

    public static final String NonMailUserLocalPart="non-mail-user";
    public static final String NonMailUserMailDomainPart="missing.domain.net";
    public static final String NonMailMailAddress=NonMailUserLocalPart + MAIL_DOMAIN_SEPARATOR + NonMailUserMailDomainPart;
    /**
     *  List of characters not allowed in RFC822 atoms
     *
     * Note: includes only characters above (and including) space (ASCII 32).
     * All characters below ASCII 32 or above ASCII 126 are automatically
     * considered illegal in an atom. Use {@link #isRFC822AtomChar(char)}
     * wherever possible instead of doing your own checks.
     */
    public static final String    nonRFC822AtomChars=" <>[]():;,.@\\'\"";
    /**
     * As per RFC2822 an atom text is defined as:
     *
     * atext = ALPHA / DIGIT / ; Any character except controls (ASCII < 32), SP, and specials (ASCII >= 127).
     *         "!" / "#" /
     *         "$" / "%" /
     *         "&" / "'" /
     *         "*" / "+" /
     *         "-" / "/" /
     *         "=" / "?" /
     *         "^" / "_" /
     *         "`" / "{" /
     *        "|" / "}" /
     *         "~"
     * @param ch character to be checked
     * @return TRUE if character allowed to appear in an RFC822 atom
     * @see #nonRFC822AtomChars for excluded characters
     */
    public static final boolean isRFC822AtomChar (final char ch)
    {
        if ((ch <= ' ') || (ch >= (char) 0x7F))
            return false;

        final int    chPos=nonRFC822AtomChars.indexOf(ch);
        if ((chPos < 0) || (chPos >= nonRFC822AtomChars.length()))
            return true;    // OK if not found in the excluded characters

        return false;
    }
    /**
     * @param cs sequence to be checked
     * @param startPos position in sequence to start checking
     * @param len number of characters to be checked
     * @return 0 if all characters in sequence are OK as atom characters,
     * >0 if some illegal parameter found, <0 encoded index of non-atom
     * character - index=(-1)-return value
     * <B>Note:</B> an empty sequence is NOT considered a valid atom
     * @see #isRFC822AtomChar(char) for allowed characters in an atom
     */
    public static final int checkRFC822AtomText (final CharSequence cs, final int startPos, final int len)
    {
        if ((startPos < 0) /* bad/illegal */ || (len <= 0) /* empty data is not an atom */)
            return (+1);

        final int    maxPos=startPos + len;
        if ((null == cs) || (maxPos > cs.length()))
            return (+2);

        for (int    curPos=startPos; curPos < maxPos; curPos++)
        {
            final char    ch=cs.charAt(curPos);
            if (!isRFC822AtomChar(ch))
                return ((-1) - curPos);
        }

        // this point is reached if ALL characters are allowed in the atom
        return 0;
    }
    /**
     * @param cs sequence to be checked
     * @return 0 if all characters in sequence are OK as atom characters,
     * >0 if some illegal parameter found, <0 encoded index of non-atom
     * character - index=(-1)-return value
     * <B>Note:</B> an empty sequence is NOT considered a valid atom
     * @see #isRFC822AtomChar(char) for allowed characters in an atom
     * @see #checkRFC822AtomText(CharSequence, int, int) for partial sequence
     * checking
     */
    public static final int checkRFC822AtomText (final CharSequence cs)
    {
        return (null == cs) ? (+3) : checkRFC822AtomText(cs, 0, cs.length());
    }
}
