package net.community.chest.net.proto.text.imap4;

import java.io.IOException;
import java.util.List;

import net.community.chest.CoVariantReturn;
import net.community.chest.ParsableString;
import net.community.chest.net.TextNetConnection;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Mar 27, 2008 11:45:13 AM
 */
public class IMAP4FolderStatusRspHandler extends AbstractIMAP4UntaggedResponseHandlerHelper {
    private final IMAP4StatusInfo _stInfo /* =null */;
    /*
     * @see net.community.chest.net.proto.text.imap4.AbstractIMAP4UntaggedResponseHandler#getResponse()
     */
    @Override
    @CoVariantReturn
    protected IMAP4StatusInfo getResponse ()
    {
        return _stInfo;
    }

    public IMAP4FolderStatusRspHandler (final TextNetConnection conn)
    {
        super(conn);
        _stInfo = new IMAP4StatusInfo();
    }
    /*
     * @see net.community.chest.net.proto.text.imap4.AbstractIMAP4UntaggedResponseHandler#handleUntaggedResponse(net.community.chest.ParsableString, int)
     */
    @Override
    public int handleUntaggedResponse (final ParsableString ps, final int startPos) throws IOException
    {
        final int curPos=ps.findNonEmptyDataStart(startPos), maxIndex=ps.getMaxIndex();
        // should not happen, but OK if no modifiers found
        if ((curPos < startPos) || (curPos >= maxIndex))
            return 0;

        // should not happen, but OK...
        final int nextPos=ps.findNonEmptyDataEnd(curPos+1);
        if ((nextPos <= curPos) || (nextPos >= maxIndex))
            return 0;

        if (!ps.compareTo(curPos, nextPos, IMAP4Protocol.IMAP4StatusCmdChars, true))
            return 0;   // ignore any non-STATUS untagged response

        // some servers erroneously ommit repeating the folder name in the response, so we check which is it...
        int stStart=ps.findNonEmptyDataStart(nextPos+1);
        if (ps.getCharAt(stStart) != IMAP4Protocol.IMAP4_PARLIST_SDELIM)
            stStart = skipArgument(ps, stStart); // we skip the folder name - whatever it is

        // we need to get the new limits in case a literal has been encountered
        int psStart=ps.getStartIndex(), psMax=ps.getMaxIndex();
        if ((stStart <= psStart) || (stStart >= psMax))
            return (-2);    // we expect to have some data after the folder name

        // we expect a non-empty list of responses
        if (ps.getCharAt(stStart) != IMAP4Protocol.IMAP4_PARLIST_SDELIM)
            return (-3);
        stStart++;

        // make sure we have the ending list delimiter
        int stEnd=ps.indexOf(IMAP4Protocol.IMAP4_PARLIST_EDELIM, stStart);
        if (stEnd <= stStart)
            return (-4);

        // we expect a non-empty list of tokens with EVEN number of tokens (data pairs)
        final List<? extends ParsableString.SplitInfo>  tokens=ps.split(stStart, stEnd);
        final int                                        numTokens=(null == tokens) ? 0 : tokens.size();
        if ((numTokens <= 0) || ((numTokens & 0x01) != 0))
            return (-5);

        try
        {
            for (int    tkIndex=0; tkIndex < numTokens; tkIndex += 2)
            {
                final ParsableString.SplitInfo    tkName=tokens.get(tkIndex), tkVal=tokens.get(tkIndex+1);
                if (ps.compareTo(tkName, IMAP4StatusInfo.IMAP4_MESSAGESChars, true))
                    _stInfo.setNumOfMsgs(ps.getUnsignedInt(tkVal));
                else if (ps.compareTo(tkName, IMAP4StatusInfo.IMAP4_RECENTChars, true))
                    _stInfo.setNumRecent(ps.getUnsignedInt(tkVal));
                else if (ps.compareTo(tkName, IMAP4StatusInfo.IMAP4_UNSEENChars, true))
                    _stInfo.setNumUnseen(ps.getUnsignedInt(tkVal));
                else if (ps.compareTo(tkName, IMAP4StatusInfo.IMAP4_UIDNEXTChars, true))
                    _stInfo.setUIDNext(ps.getUnsignedLong(tkVal));
                else if (ps.compareTo(tkName, IMAP4StatusInfo.IMAP4_UIDVALIDITYChars, true))
                    _stInfo.setUIDValidity(ps.getUnsignedLong(tkVal));
                else    // just so we have a debug breakpoint
                    continue;    // Note: we ignore any unknown tokens...
            }
        }
        catch(NumberFormatException nfe)
        {
            return (-100);  // should not happen
        }

        return 0;
    }
}
