package net.community.chest.net.proto.text.imap4;

import java.io.IOException;

import net.community.chest.CoVariantReturn;
import net.community.chest.ParsableString;
import net.community.chest.net.TextNetConnection;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Mar 27, 2008 11:37:07 AM
 */
public class IMAP4QuotarootInfoRspHandler extends AbstractIMAP4UntaggedResponseHandlerHelper {
    private final IMAP4QuotarootInfo    _qtInfo /* =null */;
    /*
     * @see net.community.chest.net.proto.text.imap4.AbstractIMAP4UntaggedResponseHandler#getResponse()
     */
    @Override
    @CoVariantReturn
    protected IMAP4QuotarootInfo getResponse()
    {
        return _qtInfo;
    }

    public IMAP4QuotarootInfoRspHandler (final TextNetConnection conn)
    {
        super(conn);
        _qtInfo = new IMAP4QuotarootInfo();
    }
    /**
     * Extends a quota value
     * @param ps parsable string object containing the value
     * @param startPos position to start parsing (inclusive(
     * @return extract value (<0 if error)
     * @throws IOException
     */
    private int handleQuota (final ParsableString ps, final int startPos) throws IOException
    {
        // NOTE: we assume quota response is on ONE line
        if (!haveCRLF())
            return (-971);

        // we skip the folder name - whatever it is
        int    stStart=skipArgument(ps, startPos), maxIndex=ps.getMaxIndex();
        if ((stStart <= startPos) || (stStart >= maxIndex))
            return (-972);

        if (ps.getCharAt(stStart) != IMAP4Protocol.IMAP4_PARLIST_SDELIM)
            return (-973);
        stStart++;

        final long[]    vals={ 0L, 0L };    // helper array
        for (int rspIndex=0; (ps.getCharAt(stStart) != IMAP4Protocol.IMAP4_PARLIST_EDELIM) && (rspIndex < Short.MAX_VALUE); rspIndex++)
        {
            final IMAP4ParseAtomValue    aVal=extractStringHdrVal(stStart, false);
            final String      modName=(aVal.length() <= 0) ? "" : aVal.toString();
            boolean            storageVals=false;

            // check if STORAGE or MESSAGE responses
            if (IMAP4Protocol.IMAP4QuotaStorageRes.equals(modName))
                storageVals = true;
            else if (IMAP4Protocol.IMAP4QuotaMessageRes.equals(modName))
                storageVals = false;
            else // neither one...
                return (-974);

            stStart = aVal.startPos;
            // we expect 2 values
            for (int    index=0; index < vals.length; index++)
            {
                final NumInfo    numInfo=extractSimpleNumber(stStart);
                if (null == numInfo)
                    return (-975);

                vals[index] = numInfo.num;
                stStart = numInfo.startPos;
            }

            if (storageVals)
            {
                _qtInfo.setCurStorageKB(vals[0]);
                _qtInfo.setMaxStorageKB(vals[1]);
            }
            else
            {
                _qtInfo.setCurMessages((int) vals[0]);
                _qtInfo.setMaxMessages((int) vals[1]);
            }

            if (((stStart=ps.findNonEmptyDataStart(stStart)) <= startPos) || (stStart >= maxIndex))
                return (-976);
        }

        // make sure exited because of list end instead of "infinite" loop exit
        if (ps.getCharAt(stStart) != IMAP4Protocol.IMAP4_PARLIST_EDELIM)
            return (-977);

        return 0;    // OK
    }
    /*
     * @see net.community.chest.net.proto.text.imap4.AbstractIMAP4UntaggedResponseHandler#handleUntaggedResponse(net.community.chest.ParsableString, int)
     */
    @Override
    public int handleUntaggedResponse (final ParsableString ps, final int startPos) throws IOException
    {
        final int curPos=ps.findNonEmptyDataStart(startPos), maxIndex=ps.getMaxIndex();
        // should not happen, but OK if no modifiers found
        if ((curPos < startPos) || (startPos >= maxIndex))
            return 0;

        final int nextPos=ps.findNonEmptyDataEnd(curPos+1);
        // should not happen, but OK...
        if ((nextPos <= curPos) || (nextPos > maxIndex))
            return 0;

        if (ps.compareTo(curPos, nextPos, IMAP4Protocol.IMAP4QuotaRspChars, true))
            return handleQuota(ps, nextPos+1);

        // if not a known modifier, then ignore it
        return 0;
    }
}
