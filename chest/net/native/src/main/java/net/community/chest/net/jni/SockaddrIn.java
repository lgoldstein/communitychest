/*
 *
 */
package net.community.chest.net.jni;

import net.community.chest.net.address.IPv4Address;

/**
 * <P>Copyright GPLv2</P>
 *
 * @author Lyor G.
 * @since Jun 23, 2009 9:37:28 AM
 */
public class SockaddrIn implements Cloneable {
    public SockaddrIn ()
    {
        super();
    }

    private int    _portNumber;
    public int getPortNumber ()
    {
        return _portNumber;
    }

    public void setPortNumber (int portNumber)
    {
        _portNumber = portNumber;
    }

    private long    _ipAddress;
    public long getIpAddress ()
    {
        return _ipAddress;
    }

    public void setIpAddress (long ipAddress)
    {
        _ipAddress = ipAddress;
    }

    public void setIpAddress (String a) throws NumberFormatException
    {
        setIpAddress(IPv4Address.toLong(a));
    }

    public SockaddrIn (long a, int p)
    {
        _ipAddress = a;
        _portNumber = p;
    }

    public SockaddrIn (String a, int p) throws NumberFormatException
    {
        this(IPv4Address.toLong(a), p);
    }
    /**
     * The Internet socket family
     */
    public static final short    AF_INET=2;

    public Sockaddr toSockaddr ()
    {
        final byte[]    data=new byte[Sockaddr.SA_DATA_LEN];
        int                len=NativeSocket.htons(getPortNumber(), data);
        len += NativeSocket.htonl((int) getIpAddress(), data, len);

        return new Sockaddr(AF_INET, data, len);
    }
    /*
     * @see java.lang.Object#clone()
     */
    @Override
    public SockaddrIn clone () throws CloneNotSupportedException
    {
        return getClass().cast(super.clone());
    }
    /*
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals (Object obj)
    {
        if (!(obj instanceof SockaddrIn))
            return false;
        if (this == obj)
            return true;

        final SockaddrIn    a=(SockaddrIn) obj;
        return (a.getIpAddress() == getIpAddress())
            && (a.getPortNumber() == getPortNumber())
            ;
    }
    /*
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode ()
    {
        return (int) (getIpAddress() & getPortNumber());
    }
    /*
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString ()
    {
        return IPv4Address.toString(getIpAddress()) + "@" + getPortNumber();
    }
}
