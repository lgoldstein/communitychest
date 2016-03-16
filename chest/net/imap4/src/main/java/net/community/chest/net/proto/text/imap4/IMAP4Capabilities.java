package net.community.chest.net.proto.text.imap4;

import java.io.IOException;
import java.util.Collection;
import java.util.Set;
import net.community.chest.lang.StringUtil;
import net.community.chest.net.NetUtil;
import net.community.chest.net.TextNetConnection;
import net.community.chest.net.proto.text.ProtocolCapabilityResponse;

/**
 * <P>Copyright 2007 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Sep 20, 2007 10:26:33 AM
 */
public class IMAP4Capabilities extends IMAP4TaggedResponse implements ProtocolCapabilityResponse {
    /**
     *
     */
    private static final long serialVersionUID = 1726906647701580844L;
    public static final String IMAP4_REV1="IMAP4rev1";  // RFC2060
    public static final String IMAP4_QUOTA="QUOTA"; // RFC2087
    public static final String IMAP4_NAMESPACE="NAMESPACE"; // RFC2342
    public static final String IMAP4_IDLE="IDLE";   // RFC2177
    public static final String IMAP4_UIDPLUS="UIDPLUS"; // RFC2359
    public static final String IMAP4_LITERALPLUS="LITERAL+";    // RFC2088
    public static final String IMAP4_MBOXREFERRAL="MAILBOX-REFERRALS";  // RFC2193
    public static final String IMAP4_LOGINREFERRAL="LOGIN-REFERRALS";   // RFC2221
    public static final String IMAP4_CHILDREN="CHILDREN";   // RFC3348
    public static final String IMAP4_LOGINDISABLED="LOGINDISABLED";    // RFC2595

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
    /**
     * Checks if current capabilities include specified one
     * @param cap capability to be checked
     * @return TRUE if specified capability found (case-insensitive search)
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
    /**
     * @return TRUE if reported capabilities contain the QUOTA
     */
    public boolean hasQuota ()
    {
        return hasCapability(IMAP4_QUOTA);
    }
    /**
     * @return TRUE if reported capabilities contain the NAMESPACE
     */
    public boolean hasNamespace ()
    {
        return hasCapability(IMAP4_NAMESPACE);
    }
    /**
     * @return TRUE if reported capabilities contain the LOGINDISABLED
     */
    public boolean isPlaintextLoginDisabled ()
    {
        return hasCapability(IMAP4_LOGINDISABLED);
    }
    /*
     * @see net.community.chest.net.proto.text.imap4.IMAP4TaggedResponse#equals(java.lang.Object)
     */
    @Override
    public boolean equals (Object obj)
    {
        final Class<?>    oc=(obj == null) ? null : obj.getClass();
        if (oc != getClass())
            return false;
        if (this == obj)
            return true;

        final IMAP4Capabilities    caps=(IMAP4Capabilities) obj;
        if (!isSameResponse(caps))
            return false;

        final Collection<String>    cMap=getCapabilities(),
                                    oMap=caps.getCapabilities();
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
     * @see net.community.chest.net.proto.text.imap4.IMAP4TaggedResponse#hashCode()
     */
    @Override
    public int hashCode ()
    {
        final Collection<String>    caps=getCapabilities();
        int                            nRes=super.hashCode();
        if ((caps != null) && (caps.size() > 0))
        {
            for (final String c : caps)
                nRes += StringUtil.getDataStringHashCode(c, false);
        }

        return nRes;
    }
    /*
     * @see net.community.chest.net.proto.text.imap4.IMAP4TaggedResponse#reset()
     */
    @Override
    public void reset ()
    {
        super.reset();

        final Collection<String>    cMap=getCapabilities();
        if ((cMap != null) && (cMap.size() > 0))
            cMap.clear();
    }

    public static final IMAP4Capabilities getFinalResponse (final TextNetConnection conn, final int tagValue) throws IOException
    {
        final IMAP4Capabilities   rsp=(IMAP4Capabilities) (new IMAP4CapabilitiesRspHandler(conn)).handleResponse(tagValue);
        if (!rsp.hasCapabilities())
            throw new IMAP4RspHandleException("No capabilities resported in retrieved response");

        return rsp;
    }
}
