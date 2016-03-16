package net.community.chest.net.proto.text.imap4;

import java.io.IOException;
import java.util.Collection;

import net.community.chest.CoVariantReturn;
import net.community.chest.ParsableString;
import net.community.chest.net.TextNetConnection;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Mar 27, 2008 11:27:40 AM
 */
public class IMAP4CapabilitiesRspHandler extends AbstractIMAP4UntaggedResponseHandlerHelper {
    private final IMAP4Capabilities _capInfo /* =null */;
    /*
     * @see net.community.chest.net.proto.text.imap4.AbstractIMAP4UntaggedResponseHandler#getResponse()
     */
    @Override
    @CoVariantReturn
    protected IMAP4Capabilities getResponse ()
    {
        return _capInfo;
    }

    public IMAP4CapabilitiesRspHandler (TextNetConnection conn)
    {
        super(conn);
        _capInfo = new IMAP4Capabilities();
    }
    /* Format: * CAPABILITY IMAP4rev1 UIDPLUS IDLE LOGIN-REFERRALS NAMESPACE QUOTA CHILDREN X-CP-MOVE AUTH=CRAM-MD5 AUTH=DIGEST-MD5 AUTH=PLAIN
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

        if (!ps.compareTo(curPos, nextPos, IMAP4Protocol.IMAP4CapabilityCmdChars, true))
            return 0;   // OK if non-capability untagged response

        // NOTE !!! we assume all capabilities are contained in one line
        if (!haveCRLF())
            throw new IMAP4RspHandleException("Capabilities extend beyond one line");

        final Collection<String>    caps=ps.tokenize(nextPos+1);
        if (_capInfo.getCapabilities() != null)
            return (-751);    // cannot have over initialization

        if ((caps != null) && (caps.size() > 0))
        {
            for (final String c : caps)
                _capInfo.addCapability(c);
        }

        if (!_capInfo.hasCapabilities())
            throw new IMAP4RspHandleException("No capabilities reported");

        return 0;
    }
}
