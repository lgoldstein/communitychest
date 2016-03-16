package net.community.chest.net.proto.text.imap4;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

import net.community.chest.CoVariantReturn;
import net.community.chest.ParsableString;
import net.community.chest.net.TextNetConnection;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Mar 27, 2008 11:22:11 AM
 */
public class IMAP4SearchRspHandler extends AbstractIMAP4UntaggedResponseHandlerHelper {
    private final IMAP4SearchResponse     _srchRes /* =null */;
    private Collection<Long>            _results /* =null */;
    private final int                    _maxResults;
    /*
     * @see net.community.chest.net.proto.text.imap4.AbstractIMAP4UntaggedResponseHandler#getResponse()
     */
    @Override
    @CoVariantReturn
    protected IMAP4SearchResponse getResponse ()
    {
        return _srchRes;
    }

    public IMAP4SearchRspHandler (final TextNetConnection conn, final boolean isUID, final int maxResults)
    {
        super(conn);

        _srchRes = new IMAP4SearchResponse();
        _srchRes.setUID(isUID);
        _maxResults = maxResults;
    }
    /**
     * Updates the accumulated results into the response object
     * @param res response object to be updated (Note: msg IDs array should NOT be allocated)
     * @return 0 if successful
     */
    protected int updateResults (final IMAP4SearchResponse res)
    {
        final int    resNum=(null == _results) ? 0 : _results.size();
        if (resNum <= 0)    // OK if no results found
            return 0;

        long[]    msgIds=res.getMsgIds();
        if (msgIds != null)    // not expecting previous allocation
            return (-1011);
        msgIds = new long[resNum];

        final Iterator<Long>    iter=_results.iterator();
        for (int    resIndex=0; (iter != null) && iter.hasNext(); )
        {
            final Long    idl=iter.next();
            if ((idl != null) && (resIndex < resNum))    // should not be otherwise
            {
                msgIds[resIndex] = idl.longValue();
                resIndex++;
            }
        }

        res.setMsgIds(msgIds);
        return 0;
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

        if (!ps.compareTo(curPos, nextPos, IMAP4Protocol.IMAP4SearchCmdChars, true))
            return 0;   // OK if non-SEARCH untagged respose

        // limit to maximum ~32K results to avoid infinite loop (we do not expect these many results)
        for (int    curResults=0, numPos=nextPos; curResults < Short.MAX_VALUE; curResults++)
        {
            if (haveCRLF())
            {
                // if have entire line and no more data follows, stop (Note: this is OK even if no numbers have been returned)
                if ((numPos=_psHelper.findNonEmptyDataStart(numPos)) < _psHelper.getStartIndex())
                    return 0;
            }

            // no need to continue if achieved max results
            if ((_maxResults > 0) && (curResults >= _maxResults))
                return 0;

            NumInfo    numVal=extractSimpleNumber(numPos);
            if (null == numVal)
                return (-1012);
            if (numVal.num <= 0)    // should not happen
                return (-1013);

            if (null == _results)
                _results = new LinkedList<Long>();
            _results.add(Long.valueOf(numVal.num));

            numPos = numVal.startPos;
        }

        // this point is reached if virtual infinite loop exit
        return (-1014);
    }
}
