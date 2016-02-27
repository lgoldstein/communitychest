package net.community.chest.net;

import java.io.IOException;
import java.io.InvalidClassException;
import java.net.ConnectException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.naming.NamingException;

import net.community.chest.io.FileUtil;
import net.community.chest.lang.StringUtil;
import net.community.chest.net.address.IPv4Address;
import net.community.chest.net.dns.DNSAccess;
import net.community.chest.net.proto.text.ProtocolCapabilityResponse;

/**
 * Copyright 2007 as per GPLv2
 * @author Lyor G.
 * @since Jun 28, 2007 2:12:13 PM
 */
public final class NetUtil {
	private NetUtil ()
	{
		// no instance
	}
	/**
	 * Resolves ONE specific address out of an array
	 * @param ia array to scan for resolving the address
	 * @param startPos position in array to start scanning
	 * @param len number of addresses to scan
	 * @param useLowest how to resolve if more than one address - TRUE=lowest
	 * value, FALSE=highest value
	 * @return resolved address - null if could not resolve it
	 */
	public static final InetAddress resolveAddress (final InetAddress[] ia, final int startPos, final int len, final boolean useLowest)
	{
		final int	maxPos=startPos + len;
		if ((startPos < 0) || (len <= 0) || (null == ia) || (maxPos > ia.length))
			return null;

		InetAddress	ret=null;
		for (int	curPos=startPos; curPos < maxPos; curPos++)
		{
			final InetAddress	a=ia[curPos];
			if ((null == a)
			 || a.isLoopbackAddress() // don't want the loopback -> already have it
			 	// TODO: review this code if/when IPv6 addresses are in use
			 || (!(a instanceof Inet4Address)))
				continue;

			// find lowest IP address
			if (ret != null)
			{
				// TODO: review this code if/when IPv6 addresses are in use
				try
				{
					final long	rv=IPv4Address.toLong(ret.getHostAddress()) & 0x00FFFFFFFFL,
								av=IPv4Address.toLong(a.getHostAddress()) & 0x00FFFFFFFFL,
								nRes=rv - av;
					if (useLowest)
					{
						if (nRes < 0L)
							continue;
					}
					else
					{
						if (nRes > 0L)
							continue;
					}
				}
				catch(NumberFormatException e)
				{
					// should not happen
					continue;
				}
			}

			ret = a;
		}

		return ret;
	}
	/**
	 * Resolves ONE specific address out of an array
	 * @param ia array to scan for resolving the address
	 * @param useLowest how to resolve if more than one address - TRUE=lowest
	 * value, FALSE=highest value
	 * @return resolved address - null if could not resolve it
	 */
	public static final InetAddress resolveAddress (final InetAddress[] ia, final boolean useLowest)
	{
		return (null == ia) ? null : resolveAddress(ia, 0, ia.length, useLowest);
	}
	/**
	 * Default property name used in call to {@link #resolveAddressOverride(String)}
	 */
	public static final String	LOCALADDR_PROP_NAME=NetUtil.class.getName().toLowerCase() + ".localaddr";
	/**
	 * @param propName <P>property name to be used to resolve the local address
	 * override (if any) - can be used to override the {@link #getLocalAddress()}
	 * resolution mechanism. If this property is defined, then its value is
	 * interpreted as follows:</P></BR>
	 * <UL>
	 * 		<LI>
	 * 		if it points to another property (via {@link System#getProperty(String)})
	 * 		then this property value is used. <B>Note:</B> there is <U>no
	 * 		recursion</U> i.e., the value cannot be another "pointer" to a property.
	 * 		</LI>
	 * 
	 * 		<LI>
	 * 		if it is an IP address format "a.b.c.d" then it is assumed to be
	 * 		the local address. <B>Caveat emptor:</B> the address is not
	 * 		validated against the currently known IP addresses of the host.
	 * 		</LI>
	 * 
	 * 		<LI>
	 * 		otherwise, it is assumed to be a name to be resolved via DNS
	 * 		</LI>
	 * </UL>
	 * @return resolved address (null if no property value set) 
	 * @throws UnknownHostException if cannot resolve host name
	 */
	public static final InetAddress resolveAddressOverride (final String propName) throws UnknownHostException
	{
		String	propVal=((null == propName) || (propName.length() <= 0)) ? null : System.getProperty(propName),
				indProp=((null == propVal) || (propVal.length() <= 0)) ? null : System.getProperty(propVal);
		// check if indirect "pointer" 
		if ((indProp != null) && (indProp.length() > 0))
			propVal = indProp;
		if ((null == propVal) || (propVal.length() <= 0))
			return null;

		// check if direct IP address
		try
		{
		 	// TODO: review this code if/when IPv6 addresses are in use
			final byte[]	av=new byte[IPv4Address.ADDRESS_LENGTH];
			final int		aLen=IPv4Address.fromString(propVal, av);
			if (IPv4Address.ADDRESS_LENGTH == aLen)
				return InetAddress.getByAddress(av);
		}
		catch(Exception e)
		{
			// ignore - assumed a host name
		}

		// assume a host name and use DNS
		final InetAddress[]	ia=InetAddress.getAllByName(propVal);
		final InetAddress	ret=resolveAddress(ia, true);
		if (ret != null)
			return ret;

		// this point is reached if no matches found
		throw new UnknownHostException("No matches found for host=" + propVal);
	}

	private static NetworkInterface[]	_localInterfaces	/* =null */;
	/**
	 * @return all local interfaces (except for the loopback)
	 * @throws SocketException if cannot query the existing interfaces
	 */
	public static final synchronized NetworkInterface[] getAllLocalInterfaces () throws SocketException
	{
		if (null == _localInterfaces)
		{
			Collection<NetworkInterface>	ifs=null;
			for (final Enumeration<NetworkInterface>	eNics=NetworkInterface.getNetworkInterfaces();
			 	 (eNics != null) && eNics.hasMoreElements();
			 	 )
			 {
				final NetworkInterface	nic=eNics.nextElement();
				if (null == nic)	// should not happen
					continue;

				boolean haveNonLoopbackAddress=false;
				for (final Enumeration<InetAddress>	nicAddrs=nic.getInetAddresses();
					 (nicAddrs != null) && nicAddrs.hasMoreElements();
					)
				{
					final InetAddress	ia=nicAddrs.nextElement();
					if ((null == ia) // should not happen
					  || ia.isLoopbackAddress() // don't want the loopback -> already have it
	    			/* NOTE !!! this code behaves DIFFERENTLY for Windows vs. Linux - in
	    			 * 		Windows we only get Inet4Address(es), but in Linux we also get
	    			 * 		Inet6Address that represents the MAC address of the interface
	    			 * 		(which we want to ignore).
	    			 * 
	    			 * TODO: review this code if/when IPv6 addresses are in use
	    			 */
					  || (!(ia instanceof Inet4Address)))	// for the time being we want only IPv4 address(es)
						continue;

					haveNonLoopbackAddress = true;
				}

				if (haveNonLoopbackAddress)
				{
					if (null == ifs)
						ifs = new LinkedList<NetworkInterface>();
					ifs.add(nic);
				}
			 }

			final int	numNics=(null == ifs) ? 0 : ifs.size();
			_localInterfaces = (numNics <= 0) ? null : ifs.toArray(new NetworkInterface[numNics]);
		}

		return _localInterfaces;
	}

	private static InetAddress[]	_localAddresses	/* =null */;
	/**
	 * @return all IPv4 addresses assigned to this host (except for the loopback)
	 * @throws SocketException if unable to query available interfaces
	 */
	public static final synchronized InetAddress[] getAllLocalAddresses () throws SocketException
	{
		if (null == _localAddresses)
		{
			Collection<InetAddress>	addrs=null;
			
			final NetworkInterface[]	ifs=getAllLocalInterfaces();
			if ((ifs != null) && (ifs.length > 0))
			{
				for (final NetworkInterface	nic : ifs)
				{
					for (final Enumeration<InetAddress>	nicAddrs=(null == nic) /* should not happen */ ? null : nic.getInetAddresses();
						(nicAddrs != null) && nicAddrs.hasMoreElements();
						)
					{
						final InetAddress	ia=nicAddrs.nextElement();
						if ((null == ia) // should not happen
						|| ia.isLoopbackAddress() // don't want the loopback -> already have it
            			/* NOTE !!! this code behaves DIFFERENTLY for Windows vs. Linux - in
            			 * 		Windows we only get Inet4Address(es), but in Linux we also get
            			 * 		Inet6Address that represents the MAC address of the interface
            			 * 		(which we want to ignore).
            			 * 
            			 * TODO: review this code if/when IPv6 addresses are in use
            			 */
						|| (!(ia instanceof Inet4Address)))	// for the time being we want only IPv4 address(es)
							continue;

						if (null == addrs)
							addrs = new LinkedList<InetAddress>();
						addrs.add(ia);
					}
				}
			}

			final int	numAddrs=(null == addrs) ? 0 : addrs.size();
			_localAddresses =  (numAddrs <= 0) /* should not happen */ ? null : addrs.toArray(new InetAddress[numAddrs]);
		}

		return _localAddresses;
	}

	private static InetAddress	_localAddr	/* =null */;
    /**
     * @return local address information. <B>Note:</B> if more than one
     * address available then the <U>lowest</U> IP address is chosen
     * @throws UnknownHostException unable to extract it
     * @see #getAllLocalAddresses()
     */
    public static synchronized InetAddress getLocalAddress () throws UnknownHostException
    {
    	// check if have an invocation command specific override
    	if (null == _localAddr)
    		_localAddr = resolveAddressOverride(LOCALADDR_PROP_NAME);

    	if (null == _localAddr)
    	{
            try
			{
				if (null == (_localAddr=resolveAddress(getAllLocalAddresses(), true)))
					throw new UnknownHostException("No local address could be resolved");
			}
			catch(SocketException e)
			{
				throw new UnknownHostException("Failed to retrieve all local address: " + e.getMessage());
			}
    	}

    	return _localAddr;
    }

	public static final String resolveComputerName () throws UnknownHostException
	{
    	InetAddress	lclAddr=getLocalAddress();
        String		lclName=(null == lclAddr) ? null : lclAddr.getHostName();
        boolean		isLclIPAddress=IPv4Address.isIPv4Address(lclName);
		if ((null == lclName) || (lclName.length() <= 0) || isLclIPAddress)
		{
        	lclAddr = InetAddress.getLocalHost();

        	final String	n2=(null == lclAddr) ? null : lclAddr.getHostName();
        	if ((n2 != null) && (n2.length() > 0)
        	 && (!"localhost".equalsIgnoreCase(n2))
        	 && (!"127.0.0.1".equalsIgnoreCase(n2)))
        		lclName = n2;
		}

		return lclName;
	}

	private static String _computerName /* =null */;
	private static String _fullComputerName /* =null */;
    private static String _computerDomain /* =null */;
	private static String _hostIp /* =null */;
    /**
     * @return local host name
     * @throws UnknownHostException if unable to retrieve it
     */
    public static synchronized String getComputerName () throws UnknownHostException
    {
        if (null == _computerName)
        {
        	if ((null == (_computerName=resolveComputerName())) || (_computerName.length() <= 0))
        		throw new UnknownHostException("getComputerName() cannot resolve local host name");

        	if (IPv4Address.isIPv4Address(_computerName))
        	{
            	_fullComputerName = _computerName;
            	_computerDomain = "";
        	}
        	else
        	{
	            final int	cnLen=(null == _computerName) ? 0 : _computerName.length();
	            if (cnLen <= 0)	// should not happen
	            	throw new UnknownHostException("Null/empty host name returned for local addr=" + getLocalAddress());

	            // check if FQDN returned
	            final int	domPos=_computerName.indexOf('.');
	            if (domPos > 0)
	            {
	            	_fullComputerName = _computerName;
	            	_computerDomain = _fullComputerName.substring(domPos + 1);
	            	_computerName = _fullComputerName.substring(0, domPos);
	            }
        	}
        }

        return _computerName;
    }
	/**
	 * @return IP address of local host
	 * @throws UnknownHostException if unable to retrieve it
	 */
	public static synchronized String getHostIp () throws UnknownHostException
	{
		 // First get our local hostname
         if (null == _hostIp)
		 {
         	final InetAddress	lclAddr=getLocalAddress();
         	if (null == lclAddr)
         		throw new UnknownHostException("No local address for host IP");

		 	_hostIp = lclAddr.getHostAddress();
		 }

         return _hostIp;
	}
	/**
	 * get full name of local host
	 * @return the full computer name
	 * @throws UnknownHostException if fail to get local host name
	 */
	public static synchronized String getFullComputerName() throws UnknownHostException
	{
		 // First get our local hostname
        if (null == _fullComputerName)
		{
        	InetAddress	fqdnName=getLocalAddress();
        	_fullComputerName = (null == fqdnName) ? null : fqdnName.getCanonicalHostName();

        	if ((null == _fullComputerName) || (_fullComputerName.length() <= 0))
        	{
        		fqdnName = InetAddress.getLocalHost();
            	_fullComputerName = (null == fqdnName) ? null : fqdnName.getCanonicalHostName();
        	}

        	if ((null == _fullComputerName) || (_fullComputerName.length() <= 0))
        		throw new UnknownHostException("No full computer name for FQDN addr=" + ((null == fqdnName) ? null : fqdnName.getHostAddress()));

        	int	index=_fullComputerName.indexOf('.');
        	if (index <= 0)
        	{
        		final String	ipAddr = getHostIp();
        		// Now go get the fully qualified DNS name
        		fqdnName = InetAddress.getByName(ipAddr);

        		_fullComputerName = fqdnName.getHostName();
        		index = _fullComputerName.indexOf('.');
        	}

        	// check if we can fill in more missing data
			if (null == _computerDomain)
			{
               if (index < 0)
                   _computerDomain = "";
               else
                   _computerDomain = _fullComputerName.substring(index + 1);
            }

            if (null == _computerName)
            {
                if (index > 0)
                   _computerName = _fullComputerName.substring(0, index);
                else  // if no domain, then assume computer name same as full
                   _computerName = _fullComputerName;
            }
		}

        return _fullComputerName;
	}
    /**
     * @return the DNS domain part of the local host - may be empty (!)
     * @throws UnknownHostException if unable to retrieve it
     */
    public static synchronized String getComputerDomain() throws UnknownHostException
	{
    	// First get our local hostname
        if (null == _computerDomain)
            getFullComputerName();

		return _computerDomain;
	}
    /**
     * Enumeration that can be used to extract specific network related
     * information about the current host
     * @author lyorg
     * @since Jan 23, 2006 11:08:59 AM
     */
    public static enum ComputerNetInfoEnum {
    	SHORT_NAME, FULL_NAME, IP_ADDR, DOMAIN_NAME;

    	/**
    	 * Cached instance of all enumeration values - initialized by first
    	 * call to {@link #getValues()}
    	 */
    	private static ComputerNetInfoEnum[]	_vals;
    	public static final synchronized ComputerNetInfoEnum[] getValues ()
    	{
    		if (null == _vals)
    			_vals = values();
    		return _vals;
    	}
    }
    /**
     * @param eInfo type of network information requested
     * @return requested information string (may be null/empty)
     * @throws UnknownHostException cannot access requested network information
     */
    public static final String getNetInfo (final ComputerNetInfoEnum eInfo) throws UnknownHostException
    {
    	switch(eInfo)
    	{
    		case SHORT_NAME	: return getComputerName();
    		case FULL_NAME	: return getFullComputerName();
    		case IP_ADDR	: return getHostIp();
    		case DOMAIN_NAME: return getComputerDomain();
    		default			:
    			throw new UnknownHostException("getNetInfo(" + eInfo + ") unknown info enum");
    	}
    }
    /**
     * @param host host name/address
     * @param addrs array of {@link InetAddress}-es to check if the specified
     * host name/address is one of them
     * @return matching instance - null if no match (or null/empty host/array)
     */
    public static InetAddress findHostAddress (final String host, final InetAddress[] addrs)
    {
    	if ((null == host) || (host.length() <= 0)
    	 || (null == addrs) || (addrs.length <= 0))
    		return null;
   
    	for (final InetAddress ia : addrs)
    	{
    		if (null == ia)
    			continue;

    		// check from most likely string to less likely one
    		if (host.equalsIgnoreCase(ia.getHostAddress())
    		 || host.equalsIgnoreCase(ia.getHostName())
    		 || host.equalsIgnoreCase(ia.getCanonicalHostName()))
    			return ia;
    	}

    	// this point is reached if no match found
    	return null;
    }
    /**
     * @param host host name/address
     * @return TRUE if non/null empty and it is either localhost,
     * 127.0.0.1, the local host name (full/partial) or IP
     * @throws UnknownHostException unable to resolve the network identity 
     */
    public static boolean isLocalHostIdentity (final String host) throws UnknownHostException
    {
    	if ((null == host) || (host.length() <= 0))
    		return false;

    	// check the obvious
		if ("localhost".equalsIgnoreCase(host) || "127.0.0.1".equalsIgnoreCase(host))
			return true;

		// check the most likely
		if (host.equalsIgnoreCase(getHostIp())
		 || host.equalsIgnoreCase(getComputerName())
		 || host.equalsIgnoreCase(getFullComputerName()))
			return true;

		/* TODO check how this code behaves if DNS not configured
		try
		{
			return (findHostAddress(host, getAllLocalAddresses()) != null);
		}
		catch(Exception e)	// should not happen
		*/
		{
			return false;
		}
    }
    /**
	 * Attempts to resolve the host's address given its name
	 * @param nsHost name server host - if null/empty then some JVM default
	 * is used
	 * @param host <U>FQDN</U> host name
	 * @return resolved addresses {@link List} - may be null/empty
	 * @throws IOException if unable to resolve using {@link InetAddress#getAllByName(java.lang.String)}
	 * @throws NamingException if unable to resolve using the JVM's internal resolver
     */
    public static List<String> resolveHostAddresses (final String nsHost, final String host) throws IOException, NamingException
    {
		if ((null == nsHost) || (nsHost.length() <= 0))
		{
			final InetAddress[]	ia=InetAddress.getAllByName(host);
			final int			numAddrs=(null == ia) ? 0 : ia.length;
			final List<String>	aList=(numAddrs <= 0) ? null : new ArrayList<String>(numAddrs);
			if (numAddrs > 0)
			{
				for (final InetAddress a : ia)
				{
					 // don't want the loopback -> already have it
					if ((null == a) || a.isLoopbackAddress()
					// TODO review this code if/when IPv6 addresses are allowed
					 || (!(a instanceof Inet4Address)))
						continue;
					aList.add(a.getHostAddress());
				}
			}

			return aList;
		}
		else
		{
			final DNSAccess	nsa=new DNSAccess();
			nsa.setServer(nsHost);

			return nsa.aLookup(host);
		}
    }
	/**
	 * @param addr remote address value
	 * @return remote host address - null/empty if null or not instance
	 * of {@link InetSocketAddress}
	 */
	public static final String getRemoteHostAddress (final SocketAddress addr)
	{
		if ((null == addr) || (!(addr instanceof InetSocketAddress)))
			return null;

		final InetSocketAddress	isa=(InetSocketAddress) addr;
		final InetAddress		ia=isa.getAddress();

		return (null == ia) ? null : ia.getHostAddress();
	}
	/**
	 * @param s {@link Socket} instance whose remote host address we want
	 * @return remote host address (null/empty if cannot resolve it)
	 */
	public static final String getRemoteHostAddress (final Socket s)
	{
		return (null == s) ? null : getRemoteHostAddress(s.getRemoteSocketAddress());
	}
	/**
	 * Waits for a connection on the server socket channel and returns it (if
	 * established) embedded in an INetConnection interface - according to the
	 * supplied class object
	 * @param <N> The {@link NetConnection} instance used
	 * @param aSock socket for awaiting incoming connections
	 * @param connClass INetConnection derived connection class
	 * @return INetConnection object according to specified class
	 * @throws IOException if unable to await or bad/illegal class
	 */
	public static <N extends NetConnection> N waitForConnection (final ServerSocketChannel aSock, final Class<N> connClass) throws IOException
	{
		if ((null == aSock) || (null == connClass))
			throw new IOException("No server/connection class to wait on");
		if (!connClass.isAssignableFrom(NetConnection.class))
			throw new InvalidClassException("Connection class (" + connClass.getName() + ") incompatible with " + NetConnection.class.getName());
	
		SocketChannel	cSock=aSock.accept();
		if (null == cSock)
			throw new IOException("No socket object returned on accept");

		try
		{
			final N	conn=connClass.newInstance();
			conn.attach(cSock);
			cSock = null;	// disable auto-close on finally
			return conn;
		}
		catch (InstantiationException e)
		{
			throw new InvalidClassException(e.getClass().getName(), e.getMessage());
		}
		catch (IllegalAccessException e)
		{
			throw new InvalidClassException(e.getClass().getName(), e.getMessage());
		}
		finally
		{
			FileUtil.closeAll(cSock);
		}
	}
	/**
	 * Updates the supplied socket read timeout
	 * @param sock to be updated
	 * @param timeMillis timeout for waiting on new input - 0 == INFINITE, (<0) illegal
	 * @throws IOException if error setting the read timeout
	 */
	public static final void setReadTimeout (final Socket sock, final int timeMillis) throws IOException
	{
		if (null == sock)
			throw new SocketException(NetUtil.class.getName() + "#setReadTimeout(" + timeMillis + ") Bad/Illegal " + Socket.class.getName() + " instance");
		if (timeMillis < 0)
			throw new SocketException(NetUtil.class.getName() + "#setReadTimeout(" + timeMillis + ") bad value");

		sock.setSoTimeout(timeMillis);
		sock.setKeepAlive(true);
	}
	/**
	 * Updates the supplied socket channel read timeout
	 * @param channel socket channel to be updated
	 * @param timeMillis timeout for waiting on new input - 0 == INFINITE, (<0) illegal
	 * @throws IOException If failed to set the timeout (e.g., negative value or no channel)
	 */
	public static final void setReadTimeout (final SocketChannel channel, final int timeMillis) throws IOException
	{
		setReadTimeout((null == channel) ? null : channel.socket(), timeMillis);
	}

	public static SocketChannel openSocketChannel (final String host, final int port) throws IOException
	{
		SocketChannel	connChannel=null;
		try
		{
			final InetSocketAddress socketAddress=new InetSocketAddress(host, port);

			connChannel = SocketChannel.open();
			if (!connChannel.isBlocking())
				connChannel.configureBlocking(true);

			final Socket	s=connChannel.socket();
			if (!s.getKeepAlive())
				s.setKeepAlive(true);
			if (!s.isBound())
				s.setReuseAddress(true);
			if (!connChannel.connect(socketAddress))
				throw new ConnectException("connect(" + host + ":" + port + ") failed to connect");

			final SocketChannel	ret=connChannel;
			connChannel = null;	// disable "finally" cleanup
			return ret;
		}
		finally
		{
			FileUtil.closeAll(connChannel);
		}
	}
	/**
	 * Compares 2 ports - for this purposes, non-positive ports are considered
	 * equal to each other
	 * @param p1 first port
	 * @param p2 second port
	 * @return 0=equal, positive=2nd is less than 1st, negative=viceversa
	 */
	public static final int comparePorts (final int p1, final int p2)
	{
		if (p1 <= 0)
			return (p2 <= 0) ? 0 : (+1);	// push "empty" ports back
		else if (p2 <= 0)
			return (-1);	// push "empty" ports back

		return (p1 - p2);
	}
	/**
	 * @param p port number
	 * @return hash code for the port - zero for all non-positive ports
	 */
	public static final int getPortHashCode (final int p)
	{
		return Math.max(p, 0);
	}
	/**
	 * Checks if ALL specified capabilities are supported by this response
	 * @param cMap capabilities {@link java.util.Map} (key-capability name)
	 * @param caps capabilities {@link Collection} - if null/empty/no elements then false
	 * is returned (Note: the array may have null/empty elements, but MUST
	 * have some non-null/empty ones as well)
	 * @return TRUE if this response contains <U>all</U> of the
	 * capabilities in the specified array
	 */
	public static final boolean containsCapabilities (final Collection<String> cMap, final Collection<String> caps)
	{
		final int	numCaps=(null == caps) ? 0 : caps.size();
		if ((numCaps <= 0) || (null == cMap) || (cMap.size() <= 0))
			return false;

		for (final String c : caps)
		{
			if ((null == c) || (c.length() <= 0))
				return false;

			if (!cMap.contains(c))
				return false;
		}

		return true;
	}

	public static final Set<String> addCapability (final ProtocolCapabilityResponse rsp, final String c)
	{
		Set<String>	cMap=(null == rsp) ? null : rsp.getCapabilities();
		if ((null == c) || (c.length() <= 0))
			return cMap;

		if (null == cMap)
		{
			if (null == rsp)
				throw new IllegalArgumentException("addCapability(" + c + ") no " + ProtocolCapabilityResponse.class.getSimpleName() + " instance");

			rsp.setCapabilities(new TreeSet<String>(String.CASE_INSENSITIVE_ORDER));
		}

		if (null == (cMap=rsp.getCapabilities()))	// should not happen
			throw new IllegalStateException("addCapability(" + rsp.getClass().getName() + ")[" + c + "] no " + Set.class.getName() + " available though created");

		if (!cMap.add(c))	// just so we have a debug breakpoint
			return rsp.getCapabilities();

		return cMap;
	}

	public static final int getCapabilitiesHashCode (final Collection<String> caps)
	{
		if ((null == caps) || (caps.size() <= 0))
			return 0;

		int	nRes=0;
		for (final String c : caps)
			nRes += StringUtil.getDataStringHashCode(c, false);
		return nRes;
	}
}
