package net.community.chest.net.proto.text.smtp;

import java.util.Collection;
import java.util.Set;
import net.community.chest.net.NetUtil;
import net.community.chest.net.proto.text.ProtocolCapabilityResponse;

/**
 * <P>Copyright 2007 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Sep 20, 2007 7:29:12 AM
 */
public class SMTPExtendedHeloResponse extends SMTPResponse
        implements ESMTPCapabilityHandler, ProtocolCapabilityResponse {
    /**
     *
     */
    private static final long serialVersionUID = 1755062211615428473L;
    public SMTPExtendedHeloResponse ()
    {
        super();
    }

    private Set<String>    _capsMap    /* =null */;
    /*
     * @see net.community.chest.net.proto.text.ProtocolCapabilityResponse#getCapabilities()
     */
    @Override
    public Set<String> getCapabilities ()
    {
        return _capsMap;
    }
    /*
     * @see net.community.chest.net.proto.text.ProtocolCapabilityResponse#setCapabilities(java.util.Set)
     */
    @Override
    public void setCapabilities (Set<String> caps)
    {
        _capsMap = caps;
    }
    /*
     * @see net.community.chest.net.proto.text.ProtocolCapabilityResponse#addCapability(java.lang.String)
     */
    @Override
    public Set<String> addCapability (final String c)
    {
        return NetUtil.addCapability(this, c);
    }

    @Override
    public boolean hasCapabilities ()
    {
        final Collection<String>    cMap=getCapabilities();
        return (cMap != null) && (cMap.size() > 0);
    }
    /*
     * @see net.community.chest.net.proto.text.smtp.ESMTPCapabilityHandler#handleCapability(java.lang.String)
     */
    @Override
    public int handleCapability (String cap)
    {
        final Collection<String>    cMap=addCapability(cap);
        return (cMap != null) ? 0 : (-1);
    }
    /*
     * @see net.community.chest.net.proto.text.ProtocolCapabilityResponse#hasCapability(java.lang.String)
     */
    @Override
    public boolean hasCapability (final String cap)
    {
        if ((null == cap) || (cap.length() <= 0))
            return false;

        final Collection<String>    cMap=getCapabilities();
        if ((null == cMap) || (cMap.size() <= 0))
            return false;

        if (!cMap.contains(cap))
            return false;    // just so we have a debug breakpoint

        return true;
    }
    /*
     * @see net.community.chest.net.proto.text.smtp.SMTPResponse#equals(java.lang.Object)
     */
    @Override
    public boolean equals (Object obj)
    {
        final Class<?>    oc=(obj == null) ? null : obj.getClass();
        if (oc != getClass())
            return false;
        if (this == obj)
            return true;

        final SMTPExtendedHeloResponse    rsp=(SMTPExtendedHeloResponse) obj;
        if (!isSameResponse(rsp))
            return false;

        final Collection<String>    cMap=getCapabilities(), oMap=rsp.getCapabilities();
        if ((null == cMap) || (cMap.size() <= 0))
            return ((null == oMap) || (oMap.size() <= 0));
        else if ((null == oMap) || (oMap.size() <= 0))
            return false;
        else if ((oMap.size() != cMap.size()))
            return false;

        return NetUtil.containsCapabilities(cMap, oMap)
            && NetUtil.containsCapabilities(oMap, cMap)
            ;
    }
    /*
     * @see net.community.chest.net.proto.text.smtp.SMTPResponse#hashCode()
     */
    @Override
    public int hashCode ()
    {
        return super.hashCode()
            + NetUtil.getCapabilitiesHashCode(getCapabilities())
            ;
    }
    /*
     * @see net.community.chest.net.proto.text.smtp.SMTPResponse#reset()
     */
    @Override
    public void reset ()
    {
        super.reset();

        final Collection<String>    cMap=getCapabilities();
        if ((cMap != null) && (cMap.size() > 0))
            cMap.clear();
    }
}
