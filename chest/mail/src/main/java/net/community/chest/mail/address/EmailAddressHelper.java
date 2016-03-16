package net.community.chest.mail.address;

import java.io.IOException;
import java.io.StreamCorruptedException;
import java.nio.CharBuffer;

import net.community.chest.ParsableString;
import net.community.chest.mail.RFCMimeDefinitions;
import net.community.chest.mail.headers.RFCHeaderDefinitions;
import net.community.chest.reflect.ClassUtil;

/**
 * <P>Copyright 2007 as per GPLv2</P>
 *
 * <P>Helper static methods for e-mail address handling</P>
 * @author Lyor G.
 * @since Sep 12, 2007 11:20:43 AM
 */
public final class EmailAddressHelper {
    private EmailAddressHelper ()
    {
        // disable instance
    }
    /**
     * Appends display name to string buffer, while escaping any quotes found
     * in it if encountered
     * @param <A> The {@link Appendable} generic type
     * @param sb the {@link Appendable} object to be used for appending - may NOT be null
     * @param dispName display name (may be null/empty - in which case nothing is
     * appended and method returns successful code)
     * @return same as input {@link Appendable} instance
     * @throws IOException if cannot append data (which never for {@link StringBuilder}
     * and/or {@link StringBuffer})
     */
    public static final <A extends Appendable> A appendDisplayName (final A sb, final String dispName) throws IOException
    {
        final String    name=RFCHeaderDefinitions.encodeHdrValue(dispName, /* TODO allow specifying a default charset */ null);
        final int        nameLen=(null == name) ? 0 : name.length();
        if (nameLen <= 0)    // OK if nothing to append
            return sb;

        if (null == sb)
            throw new IOException(ClassUtil.getArgumentsExceptionLocation(EmailAddressHelper.class, "appendDisplayName", dispName) + " no " + Appendable.class.getName() + " instance provided");

        final int    atomName=RFCMimeDefinitions.checkRFC822AtomText(name, 0, nameLen);
        if (atomName != 0)
        {
            // append starting delimiter if cannot use name as atom
            sb.append('\"');

            for    (int    curPos=0, srchPos=0; curPos < nameLen; )
            {
                int    nextPos=name.indexOf('\"', srchPos);
                if (nextPos >= srchPos)
                {
                    // if already escaped then do nothing
                    if ((srchPos > 0) && ('\\' == name.charAt(srchPos - 1)))
                    {
                        srchPos = nextPos+1;
                        continue;
                    }
                }
                else    // if not found quote, then mark entire remaining string data as available
                    nextPos = nameLen;

                // copy whatever we have as "clear" text
                if ((curPos > 0) || (nextPos < nameLen))
                {
                    // this means that we found some un-escaped quote in the past
                    final String clrTxt=name.substring(curPos, nextPos);
                    sb.append(clrTxt);

                    // if positioned beyond end of string, then not really found a quote
                    if (nextPos >= nameLen)
                        break;

                    sb.append("\\\"");    // add escaped quote sign

                    curPos = (nextPos + 1);    // skip quote position
                    srchPos = curPos;
                }
                else    // the "clear" text is the entire name
                {
                    sb.append(name);
                    break;    // no need to keep looking if used entire string
                }
            }

            // append ending delimiter
            sb.append('\"');
        }
        else    // append entire name as unquoted atom
        {
            sb.append(name);
        }

        return sb;
    }
    /**
     * Appends an angle-bracket delimiter e-mail address to the string buffer
     * @param <A> The {@link Appendable} generic type
     * @param sb the {@link Appendable} object to be used for appending - may NOT be null
     * @param addr e-mail address (not validated, but may NOT be null/empty)
     * @return same as input {@link Appendable} instance
     * @throws IOException if cannot append data (which never for {@link StringBuilder}
     * and/or {@link StringBuffer})
     */
    public static final <A extends Appendable> A appendEmailAddress (final A sb, final String addr) throws IOException
    {
        final int    aLen=(null == addr)  ? 0 : addr.length();
        if (aLen <= 0)    // ignore if no address
            return sb;

        if (null == sb)
            throw new IOException(ClassUtil.getArgumentsExceptionLocation(EmailAddressHelper.class, "appendEmailAddress", addr) + " no " + Appendable.class.getName() + " instance provided");

        sb.append(RFCMimeDefinitions.MAIL_ADDR_START_DELIM)
          .append(addr)
          .append(RFCMimeDefinitions.MAIL_ADDR_END_DELIM)
          ;

        return sb;
    }
    /**
     * Builds and appends an address-pair string as per RFC822 specifications
     * @param <A> The {@link Appendable} generic type
     * @param sb the {@link Appendable} object to be used for appending - may NOT be null
     * @param name display name (may be null/empty)
     * @param addr address - if null/empty then error is returned
     * @return same as input {@link Appendable} instance
     * @throws IOException if cannot append data (which never for {@link StringBuilder}
     * and/or {@link StringBuffer})
     */
    public static final <A extends Appendable> A appendAddressPair (final A sb, final String name, final String addr) throws IOException
    {
        final int    aLen=(null == addr)  ? 0 : addr.length(),
                    nLen=(null == name) ? 0 : name.length();
        if (aLen <= 0)
            throw new StreamCorruptedException(ClassUtil.getArgumentsExceptionLocation(EmailAddressHelper.class, "appendAddressPair", addr, name) + " null/empty address");
        if (null == sb)
            throw new IOException(ClassUtil.getArgumentsExceptionLocation(EmailAddressHelper.class, "appendAddressPair", addr, name) + " no " + Appendable.class.getName() + " instance provided");

        appendDisplayName(sb, name);
        if (nLen > 0)
            sb.append(' ');
        appendEmailAddress(sb, addr);

        return sb;
    }
    /**
     * Builds an address-pair string as per RFC822 specifications
     * @param name display name (may be null/empty)
     * @param addr address - if null/empty then name is IGNORED (and null returned)
     * @return address pair string (or null if error or BOTH arguments null/empty)
     */
    public static final String buildAddressPair (final String name, final String addr)
    {
        final int    aLen=(null == addr)  ? 0 : addr.length();
        if (aLen <= 0)
            return null;

        final int    nLen=(null == name) ? 0 : name.length(),
                    bLen=aLen + 2 + 1 + Math.max(0, nLen) + 4;
        try
        {
            final StringBuilder    sb=appendAddressPair(new StringBuilder(bLen), name, addr);
            return ((null == sb) || (sb.length() <= 0)) ? null : sb.toString();
        }
        catch(IOException e)    // should not happen
        {
            throw new RuntimeException(e);
        }
    }
    /**
     * Forbidden separators in local part of email addresses (as per RFC821)
     */
    public static final String forbiddenMaiLocalPartSeps="<>(){}[]\\,;:\"'";
        public static final char[] forbiddenMaiLocalPartSepsChars=forbiddenMaiLocalPartSeps.toCharArray();
    /**
     * Checks if the specified sequence is a valid local mail address part
     * @param cs character sequence
     * @param startPos start position in the sequence (inclusive)
     * @param len number of characters to be checked
     * @return 0 if valid local part
     * @see #forbiddenMaiLocalPartSeps
     */
    public static final int    validateRFC822LocalMailPart (final CharSequence cs, final int startPos, final int len)
    {
        final int    maxIndex=startPos + len;
        if ((null == cs) || (startPos < 0) || (len <= 0) || (maxIndex > cs.length()))
            return (-1);

        for (int    curPos=startPos; curPos < maxIndex; curPos++)
        {
            final char    c=cs.charAt(curPos);

            // allow all digits and letters
            if (((c >= 'a') && (c <= 'z')) ||
                ((c >= 'A') && (c <= 'Z')) ||
                // allow some widely used characters as well
                ('.' == c) || ('-' == c) || ('_' == c) ||
                ((c >= '0') && (c <= '9')))
                continue;

            // do not allow consecutive '@'
            if (RFCMimeDefinitions.MAIL_DOMAIN_SEPARATOR_CHAR == c)
            {
                if (((curPos+1) < maxIndex) && (RFCMimeDefinitions.MAIL_DOMAIN_SEPARATOR_CHAR == cs.charAt(curPos+1)))
                    return (-2);

                continue;
            }

            // do not allow non-7-bit characters
            if ((c <= ' ') || (c >= 0x7F))
                return (-3);

            // do not allow some special characters
            if (forbiddenMaiLocalPartSeps.indexOf(c) != (-1))
                return (-4);
        }

        return 0;
    }
    /**
     * Checks if the specified sequence is a valid local mail address part
     * @param cs character sequence
     * @return 0 if valid local part
     * @see #forbiddenMaiLocalPartSeps
     */
    public static final int    validateRFC822LocalMailPart (final CharSequence cs)
    {
        return (null == cs) ? (-1) : validateRFC822LocalMailPart(cs, 0, cs.length());
    }
    /**
     * Checks if the specified sequence is a valid local mail address part
     * @param cs character sequence
     * @param startPos start position in the sequence (inclusive)
     * @param len number of characters to be checked
     * @return 0 if valid local part
     * @see #forbiddenMaiLocalPartSeps
     */
    public static final int    validateRFC822LocalMailPart (final char[] cs, final int startPos, final int len)
    {
        if ((null == cs) || (startPos < 0) || (len <= 0) || ((startPos+len) > cs.length))
            return (-1);
        else
            return validateRFC822LocalMailPart(CharBuffer.wrap(cs, startPos, len));
    }
    /**
     * Checks if the specified sequence is a valid local mail address part
     * @param cs character sequence
     * @return 0 if valid local part
     * @see #forbiddenMaiLocalPartSeps
     */
    public static final int    validateRFC822LocalMailPart (final char[] cs)
    {
        return (null == cs) ? (-1) : validateRFC822LocalMailPart(cs, 0, cs.length);
    }
    /**
     * Checks if the specified sequence is a valid domain mail address part
     * @param cs character sequence
     * @param startPos start poisition in the sequence (inclusive)
     * @param len number of characters to be checked
     * @return 0 if valid domain part
     */
    public static final int    validateRFC822MailDomainPart (final CharSequence cs, final int startPos, final int len)
    {
        // a domain MUST contain at least 3 character (e.g., "a.b")
        final int    maxIndex=(startPos + len);
        if ((null == cs) || (startPos < 0) || (len <= 2) || (maxIndex > cs.length()))
            return (-1);

        // make sure domain does not start with '.', '_', '-'
        {
            final char    firstChar=cs.charAt(startPos);
            if (('.' == firstChar) || ('-' == firstChar) || ('_' == firstChar))
                return (-2);
        }

        // make sure domain does not end with '.', '_', '-'
        {
            final char    lastChar=cs.charAt(maxIndex-1);
            if (('.' == lastChar) || ('-' == lastChar) || ('_' == lastChar))
                return (-3);
        }

        int        numComps=1;
        char    prevChar='\0';
        for (int    curPos=startPos; curPos < maxIndex; curPos++)
        {
            final char    c=cs.charAt(curPos);

            // allow all digits and letters
            if (((c >= 'a') && (c <= 'z')) ||
                ((c >= 'A') && (c <= 'Z')) ||
                ((c >= '0') && (c <= '9')))
            {
                prevChar = c;
                continue;
            }

            // do not allow non-7-bit-ASCII characters (or space)
            if ((c <= ' ') || (c > 0x7E))
                return (-4);

            if (('.' == c) || ('-' == c) || ('_' == c))
            {
                // make sure no 2 consecutive '.', '_', '-'
                if (c == prevChar)
                    return (-5);

                if ('.' == c)
                    numComps++;

                prevChar = c;
                continue;
            }

            // do not allow anything else except '.','-','_'
            return (-6);
        }

        // make sure at least 2 components found in domain (e.g., "a.b")
        if (numComps <= 1)
            return (-7);

        return 0;
    }
    /**
     * Checks if the specified sequence is a valid domain mail address part
     * @param cs character sequence
     * @return 0 if valid domain part
     */
    public static final int    validateRFC822MailDomainPart (final CharSequence cs)
    {
        return (null == cs) ? (-1) : validateRFC822MailDomainPart(cs, 0, cs.length());
    }
    /**
     * Checks if the specified sequence is a valid domain mail address part
     * @param cs character sequence
     * @param startPos start position in the sequence (inclusive)
     * @param len number of characters to be checked
     * @return 0 if valid domain part
     */
    public static final int    validateRFC822MailDomainPart (final char[] cs, final int startPos, final int len)
    {
        if ((null == cs) || (startPos < 0) || (len <= 0) || ((startPos+len) > cs.length))
            return (-1);
        else
            return validateRFC822MailDomainPart(CharBuffer.wrap(cs, startPos, len));
    }
    /**
     * Checks if the specified sequence is a valid domain mail address part
     * @param cs character sequence
     * @return 0 if valid domain part
     */
    public static final int    validateRFC822MailDomainPart (final char[] cs)
    {
        return (null == cs) ? (-1) : validateRFC822MailDomainPart(cs, 0, cs.length);
    }
    /**
     * Validates that the specified characters sequence represents a valid e-mail address
     * @param cs characters sequence to be checked
     * @param startPos index of 1st character to be checked
     * @param domainPos index of '@' in the characters sequence
     * @param totalLen total number of characters to be checked
     * @return 0 if successful
     */
    public static final int validateRFC822MailAddress (final CharSequence cs, final int startPos, final int domainPos, final int totalLen)
    {
        if ((null == cs) || (startPos < 0) || (domainPos <= startPos) || (domainPos >= totalLen) || (startPos >= totalLen))
            return (-1);

        int    lclLen=(domainPos - startPos), nErr=validateRFC822LocalMailPart(cs, startPos, lclLen);
        if (nErr != 0)
            return nErr;

        if ((nErr=validateRFC822MailDomainPart(cs, domainPos + 1, totalLen - lclLen - 1)) != 0)
            return nErr;

        return 0;
    }
    /**
     * Validates that the specified characters sequence represents a valid e-mail address
     * @param cs characters sequence to be checked
     * @param domainPos index of '@' in the characters sequence
     * @return 0 if successful
     */
    public static final int validateRFC822MailAddress (final CharSequence cs, final int domainPos)
    {
        return (null == cs) ? (-1) : validateRFC822MailAddress(cs, 0, domainPos, cs.length());
    }
    /**
     * Validates that the specified characters sequence represents a valid e-mail address
     * @param cs characters sequence to be checked
     * @param startPos index of 1st character to be checked
     * @param domainPos index of '@' in the characters sequence
     * @param totalLen total number of characters to be checked
     * @return 0 if successful
     */
    public static final int validateRFC822MailAddress (final char[] cs, final int startPos, final int domainPos, final int totalLen)
    {
        if ((null == cs) || (startPos < 0) || (domainPos <= startPos) || (domainPos >= totalLen) || (startPos >= totalLen))
            return (-1);

        int    lclLen=(domainPos - startPos), nErr=validateRFC822LocalMailPart(cs, startPos, lclLen);
        if (nErr != 0)
            return nErr;

        if ((nErr=validateRFC822MailDomainPart(cs, domainPos + 1, totalLen - lclLen - 1)) != 0)
            return nErr;

        return 0;
    }
    /**
     * Validates that the specified characters sequence represents a valid e-mail address
     * @param cs characters sequence to be checked
     * @param domainPos index of '@' in the characters sequence
     * @return 0 if successful
     */
    public static final int validateRFC822MailAddress (final char[] cs, final int domainPos)
    {
        return (null == cs) ? (-1) : validateRFC822MailAddress(cs, 0, domainPos, cs.length);
    }
    /**
     * Validates that the specified char sequence represents a valid e-mail address
     * @param cs character sequence to be checked
     * @param startPos position in sequence to start checking
     * @param len maximum number of characters to check
     * @return 0 if successful
     */
    public static final int validateRFC822MailAddress (final CharSequence cs, final int startPos, final int len)
    {
        final int    csLen=(null == cs) ? 0 : cs.length(), maxPos=(startPos+len);
        if ((csLen <= 0) || (startPos < 0) || (maxPos > csLen))
            return (-1);

        // we search backwards since for "a@b@c" the domain is "c"
        for (int    domainPos=maxPos-1; domainPos >= startPos; domainPos--)
        {
            if (RFCMimeDefinitions.MAIL_DOMAIN_SEPARATOR_CHAR == cs.charAt(domainPos))
            {
                int    lclLen=(domainPos - startPos), nErr=validateRFC822LocalMailPart(cs, startPos, lclLen);
                if (nErr != 0)
                    return nErr;

                if ((nErr=validateRFC822MailDomainPart(cs, domainPos + 1, len - lclLen - 1)) != 0)
                    return nErr;

                return 0;
            }
        }

        // this point is reached if not found domain separator
        return (-2);
    }
    /**
     * Validates that the specified char sequence represents a valid e-mail address
     * @param cs character sequence to be checked
     * @return 0 if successful
     */
    public static final int validateRFC822MailAddress (final CharSequence cs)
    {
        return (null == cs) ? (-1) : validateRFC822MailAddress(cs, 0, cs.length());
    }
    /**
     * Validates that the specified char sequence represents a valid e-mail address
     * @param cs array to be checked
     * @param startPos index in array from which to start checking (inclusive)
     * @param len number of characters to be checked starting at specified position
     * @return 0 if successful
     */
    public static final int validateRFC822MailAddress (final char[] cs, final int startPos, final int len)
    {
        if ((null == cs) || (startPos < 0) || ((startPos + len) >= cs.length))
            return (-1);
        else
            return validateRFC822MailAddress(CharBuffer.wrap(cs, startPos, len));
    }
    /**
     * Validates that the specified char array represents a valid e-mail address
     * @param cs array to be checked
     * @return 0 if successful
     */
    public static final int validateRFC822MailAddress (final char[] cs)
    {
        return (null == cs) ? (-1) : validateRFC822MailAddress(cs, 0, cs.length);
    }
    /**
     * Validates that the specified string represents a valid e-mail address
     * @param s array to be checked
     * @return 0 if successful
     */
    public static final int validateRFC822MailAddress (final String s)
    {
        final int    sLen=(null == s) ? 0 : s.length();
        if (sLen <= 0)
            return (-1);

        final int    domainPos=s.lastIndexOf(RFCMimeDefinitions.MAIL_DOMAIN_SEPARATOR_CHAR);
        if ((domainPos <= 0) || (domainPos >= (sLen - 1)))    // '@' MUST exists and MUST not be first/last
            return (-2);

        return validateRFC822MailAddress(s, domainPos);
    }
    /**
     * Extracts a delimited mail address from the parse object
     * @param ps parse object
     * @param startPos first character after the '<' delimiter
     * @return address string (null/empty if error/empty address)
     */
    public static final String extractDelimitedEmailAddress (final ParsableString ps, final int startPos)
    {
        final int    maxIndex=(null == ps) ? Integer.MIN_VALUE : ps.getMaxIndex();
        if (maxIndex <= startPos)
            return null;

        final int endPos=ps.indexOf(RFCMimeDefinitions.MAIL_ADDR_END_DELIM, startPos);
        if ((endPos < startPos) || (endPos >= maxIndex))
            return null;

        String    addrVal=ps.substring(startPos, endPos);
        if (null == addrVal)    // should not happen
            addrVal = "";
        else
            addrVal = addrVal.trim();

        // if null/empty address then return a dummy one
        if ((null == addrVal) || (addrVal.length() <= 0))
            return addrVal;

        if (validateRFC822MailAddress(addrVal) != 0)
            return null;

        return addrVal;
    }
    /**
     * Get the domain of the input email address.
     * For this method email address is a string that has '@' in it.
     * @param emailAddress The email address whose domain is required
     * @return For legal email address return the domain else null.
     *  If there is no "@" in the string empty string otherwise.
     */
    public static String getDomainFromEmailAddress (final String emailAddress)
    {
        final int    aLen=(null == emailAddress) ? 0 : emailAddress.length();
        if (aLen <= 0)
            return null;

        final int index=emailAddress.indexOf('@');
        if ((index < 0) || (index >= aLen))
            return null;

        return emailAddress.substring(index + 1);
    }
    /**
     * Extracts an e-mail address that may be enclosed in "<...>"
     * @param addr address string
     * @param okIfEmpty if TRUE then OK if "<>" encountered
     * @return extracted address (null if error)
     */
    public static final String extractAddress (final String addr, final boolean okIfEmpty)
    {
        final int    aLen=(null == addr) ? 0 : addr.length();
        if (aLen <= 0)    // original string may NOT be NULL/empty
            return null;

        if (RFCMimeDefinitions.MAIL_ADDR_START_DELIM == addr.charAt(0))
        {
            // mismatched '<'
            if (addr.charAt(aLen-1) != RFCMimeDefinitions.MAIL_ADDR_END_DELIM)
                return null;
            else if (aLen > 2)    // some text between the "<...>"
                return addr.substring(1, aLen-1);
            else    // "<>"
                return okIfEmpty ? "" : null;
        }
        else    // no "<>"
            return addr;
    }
    /**
     * Validates the address argument of MAIL-FROM/RCPT-TO
     * @param addr address to be validated
     * @param okIfEmpty if TRUE then "<>" is considered valid
     * @return TRUE if valid address argument
     */
    public static final boolean validateAddress (final String addr, final boolean okIfEmpty)
    {
        final int    aLen=(null == addr) ? 0 : addr.length();
        if (aLen <= 0)
            return okIfEmpty;

        if (RFCMimeDefinitions.MAIL_ADDR_START_DELIM == addr.charAt(0))
        {
            if (addr.charAt(aLen-1) != RFCMimeDefinitions.MAIL_ADDR_END_DELIM)
                return false;
            else if (aLen > 2 /* not <> */)
                return (0 == validateRFC822MailAddress(addr.substring(1, aLen-1)));
            else    // "<>"
                return okIfEmpty;
        }
        else
        {
            return (0 == validateRFC822MailAddress(addr));
        }
    }
}
