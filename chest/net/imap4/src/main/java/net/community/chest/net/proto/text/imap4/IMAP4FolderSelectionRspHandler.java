package net.community.chest.net.proto.text.imap4;

import java.io.IOException;
import java.io.UTFDataFormatException;

import net.community.chest.CoVariantReturn;
import net.community.chest.ParsableString;
import net.community.chest.net.TextNetConnection;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Mar 27, 2008 11:40:10 AM
 */
public class IMAP4FolderSelectionRspHandler extends AbstractIMAP4UntaggedResponseHandlerHelper {
    private final IMAP4FolderSelectionInfo _selInfo    /* =null */;
    /*
     * @see net.community.chest.net.proto.text.imap4.AbstractIMAP4UntaggedResponseHandler#getResponse()
     */
    @Override
    @CoVariantReturn
    protected IMAP4FolderSelectionInfo getResponse ()
    {
        return _selInfo;
    }
    // constructor
    public IMAP4FolderSelectionRspHandler (final TextNetConnection conn)
    {
        super(conn);
        _selInfo = new IMAP4FolderSelectionInfo();
    }
    /**
     * Handles responses containing a number followed by a modifier (e.g. "* 3 EXISTS")
     * @param ps parsable string information
     * @param fromIndex start index of number value (inclusive)
     * @param toIndex end index of number value (exclusive)
     * @return 0 if successful
     * @throws IOException if cannot extract numbers
     */
    private int handleNumberInfo (final ParsableString ps, final int fromIndex, final int toIndex) throws IOException
    {
        // try to find out which modifier it is
        int curPos=ps.findNonEmptyDataStart(toIndex), nextPos=ps.findNonEmptyDataEnd(curPos);
        if (((-1) == curPos) || ((-1) == nextPos))  // should not happen that we have only a number, but OK...
            return 0;

        /* NOTE !!! NOTE !!! NOTE !!! NOTE !!! NOTE !!! NOTE !!! NOTE !!! NOTE !!!
         *      We take a chance here if EXISTS, UNSEEN, RECENT value are above 2GB - we
         * will get an exception for these values. For all practical purpose, "int" values
         * should suffice, so no forseeable problem there. However, should this change, we
         * will need to switch to "long" values. This has not been done so far since using "int"
         * is more efficient with 32-bit architectures.
         */
        try
        {
            if (ps.compareTo(curPos, nextPos, IMAP4FolderSelectionInfo.IMAP4_EXISTSChars, true))
                _selInfo.setNumExist(ps.getUnsignedInt(fromIndex, toIndex));
            else if (ps.compareTo(curPos, nextPos, IMAP4StatusInfo.IMAP4_RECENTChars, true))
                _selInfo.setNumRecent(ps.getUnsignedInt(fromIndex, toIndex));
            else if (ps.compareTo(curPos, nextPos, IMAP4StatusInfo.IMAP4_UNSEENChars, true))
                _selInfo.setNumUnseen(ps.getUnsignedInt(fromIndex, toIndex));
        }
        catch(NumberFormatException nfe)
        {
            throw new IMAP4RspHandleException("Cannot extract " + ps.substring(curPos, nextPos) + " number: " + nfe.getMessage());
        }

        return 0;   // OK if no match - i.e. unknown, non-interesting modifier
    }
    /**
     * Handles response that contain an OK followed by bracketed information - e.g., "* OK [UIDNEXT 5]"
     * @param ps parsable string information
     * @param startIndex index in parsable string where data is to be looked for (after the OK)
     * @return 0 if successful
     * @throws IOException if cannot decipher numbers
     */
    private int handleOKInfo (final ParsableString ps, final int startIndex) throws IOException
    {
        if ((null == ps) || (startIndex < 0))
            return (-1);

        // find modifier identity location
        int maxIndex=ps.getMaxIndex(), modStart=ps.indexOf(IMAP4Protocol.IMAP4_BRCKT_SDELIM, startIndex);
        if (((-1) == modStart) || (modStart >= maxIndex)) // OK if no bracket start found
            return 0;
        modStart++;   // skip bracket delimiter

        int modEnd=ps.findNonEmptyDataEnd(modStart);
        if ((modEnd <= modStart) || (modEnd >= maxIndex))
            return 0;   // OK if not found anything beyond the modifier - all our modifiers have extra arguments

        int argStart=ps.findNonEmptyDataStart(modEnd+1);
        if ((argStart <= modEnd) || (argStart >= maxIndex))
            return 0;   // all our modifier have at least ONE extra value

        int argEnd=ps.findNonEmptyDataEnd(argStart+1);
        if ((argEnd <= argStart) || (argEnd > maxIndex))
            return 0;   // should not happen, but OK...

        try
        {
            if (ps.compareTo(modStart, modEnd, IMAP4StatusInfo.IMAP4_UIDNEXTChars, true))
            {
                // number MUST end in a ']'
                if (ps.getCharAt(argEnd-1) != IMAP4Protocol.IMAP4_BRCKT_EDELIM)
                    return (-2);

                _selInfo.setUIDNext(ps.getUnsignedLong(argStart, argEnd-1));
                return 0;
            }
            else if (ps.compareTo(modStart, modEnd, IMAP4StatusInfo.IMAP4_UIDVALIDITYChars, true))
            {
                // number MUST end in a ']'
                if (ps.getCharAt(argEnd-1) != IMAP4Protocol.IMAP4_BRCKT_EDELIM)
                    return (-2);

                _selInfo.setUIDValidity(ps.getUnsignedLong(argStart, argEnd-1));
                return 0;
            }
        }
        catch(NumberFormatException nfe)
        {
            throw new IMAP4RspHandleException("Cannot extract " + ps.substring(modStart, modEnd) + " number: " + nfe.getMessage());
        }

        // this point is reached for non-number modifiers
        try
        {
            if (ps.compareTo(modStart, modEnd, IMAP4FolderSelectionInfo.IMAP4_PERMANENTFLAGSChars, true))
                _selInfo.setPrmFlags(IMAP4MessageFlag.getMessageFlags(ps, argStart));
        }
        catch(UTFDataFormatException udfe)
        {
            throw new IMAP4RspHandleException("Cannot extract " + IMAP4FolderSelectionInfo.IMAP4_PERMANENTFLAGS + ": " + udfe.getMessage());
        }

        return 0;
    }
    /*
     * @see net.community.chest.net.proto.text.imap4.AbstractIMAP4UntaggedResponseHandler#handleUntaggedResponse(net.community.chest.ParsableString, int)
     */
    @Override
    public int handleUntaggedResponse (final ParsableString ps, final int startPos) throws IOException
    {
        if (null == ps)
            return (-1);

        final int curPos=ps.findNonEmptyDataStart(startPos), maxIndex=ps.getMaxIndex();
        // should not happen, but OK if no modifiers found
        if ((curPos < startPos) || (curPos >= maxIndex))
            return 0;

        // should not happen, but OK...
        final int nextPos=ps.findNonEmptyDataEnd(curPos+1);
        if ((nextPos <= curPos) || (nextPos > maxIndex))
            return 0;

        // if this is a number, then it might be "n EXISTS" or "n RECENT"
        if (ps.isUnsignedNumber(curPos, nextPos))
            return handleNumberInfo(ps, curPos, nextPos);

        if (ps.compareTo(curPos, nextPos, IMAP4TaggedResponse.IMAP4_OKChars, true))
            return handleOKInfo(ps, nextPos);

        if (ps.compareTo(curPos, nextPos, IMAP4FetchModifier.IMAP4_FLAGSChars, true))
        {
            try
            {
                _selInfo.setDynFlags(IMAP4MessageFlag.getMessageFlags(ps, ps.findNonEmptyDataStart(nextPos+1)));
            }
            catch(UTFDataFormatException udfe)
            {
                throw new IMAP4RspHandleException("Cannot extract selection " + IMAP4FetchModifier.IMAP4_FLAGS + " response value: " + udfe.getMessage());
            }
        }

        return 0;
    }
}
