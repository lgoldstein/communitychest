package net.community.chest.net.dns;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;

import net.community.chest.ParsableString;
import net.community.chest.lang.math.NumberTables;
import net.community.chest.net.address.IPv4Address;

/**
 * Copyright 2007 as per GPLv2
 *
 * Class used to retrieve all sort of DNS related information raw records
 *
 * @author Lyor G.
 * @since Jun 28, 2007 2:21:57 PM
 */
public class DNSAccess {
    /**
     * Naming service environmental parameters
     * see <href a="http://java.sun.com/j2se/1.5.0/docs/guide/jndi/jndi-dns.html">this</href>
     * location for more in-depth explanations
     */
    private final Hashtable<String,String> _dnsEnv=new Hashtable<String,String>();
    /**
     * Updates the DNS class factory to be used - takes effect from this call on
     * @param nsFactory class to used as DNS factory
     * @throws IllegalArgumentException if null/empty factory
     */
    public void setDNSNamingFactory (final String nsFactory) throws IllegalArgumentException
    {
        if ((null == nsFactory) || (nsFactory.length() <= 0))
            throw new IllegalArgumentException("setDNSNamingFactory(" + nsFactory + ") bad/illegal value");

        _dnsEnv.put(Context.INITIAL_CONTEXT_FACTORY, nsFactory);
    }
    /**
     * Default constructor - initializes the environment to <I>com.sun.jndi.dns.DnsContextFactory</I>
     * and uses the default DNS server (whatever it is resolved to by the JVM)
     */
    public DNSAccess ()
    {
        setDNSNamingFactory("com.sun.jndi.dns.DnsContextFactory");
    }
    /**
     * @param url <P>DNS server url - format:</P></BR>
     *
     *    dns:[//host[:port]][/domain]
     *
     *        <P>The host and port indicate the DNS server to use. If only a host
     * is given, the port defaults to 53. If only a port is given, the host
     * defaults to "localhost". If neither is given, the provider will attempt
     * to determine and use the server(s) configured for the underlying
     * platform, and if successful, will set the java.naming.provider.url
     * property to a space-separated list of URLs constructed using the server(s).
     * For example, on Solaris or Linux, the provider will read the <I>/etc/resolv.conf</I>
     * file. If DNS has not been configured on the underlying platform, the
     * host and port default to "localhost" and 53.</P>
     *
     * <B>Note:</B> URL validity is not checked. If null/empty then <U>all</I>
     * previous URL(s) are removed
     * @return previous set URL(s) - null/empty if none
     */
    public String setServerURL (final String url)
    {
        if ((null == url) || (url.length() <= 0))
            return _dnsEnv.remove(Context.PROVIDER_URL);
        else
            return _dnsEnv.put(Context.PROVIDER_URL, url);
    }
    /**
     * Set default DNS server to specified one
     * @param server server to be used - if null/empty the uses some JVM default
     * @return previous DNS URL(s) list - null/empty if none
     * @see #setServerURL(String) for full URL format
     */
    public String setServer (final String server)
    {
        if ((null == server) || (server.length() <= 0))
            return setServerURL(null);
        else
            return setServerURL("dns://" + server);
    }
    /**
     * @param url URL to be added (if null/empty nothing is done). Format
     * should be same as {@link #setServerURL(String)} - not validated (!)
     * @return previous value(s) - null/empty if none
     */
    public String addServerURL (final String url)
    {
        final String    curVal=_dnsEnv.get(Context.PROVIDER_URL);
        if ((url != null) && (url.length() > 0))
        {
            if ((null == curVal) || (curVal.length() <= 0))
                _dnsEnv.put(Context.PROVIDER_URL, url);
            else
                _dnsEnv.put(Context.PROVIDER_URL, curVal + " " + url);
        }

        return curVal;
    }
    /**
     * @param server server to be added - if null/empty then nothing is added
     * @return previous value(s) - null/empty if none
     * @see #addServerURL(String) for full URL(s) and their behavior
     */
    public String addServer (final String server)
    {
        if ((null == server) || (server.length() <= 0))
            return addServerURL(null);
        else
            return addServerURL("dns://" + server);
    }
    /**
     * @param server DNS server name/address to be used
     * @throws IllegalArgumentException if null/empty server name/address
     */
    public DNSAccess (final String server) throws IllegalArgumentException
    {
        this();

        if ((null == server) || (server.length() <= 0))
            throw new IllegalArgumentException("DNSAccess(" + server + ") bad/illegal value");

        setServer(server);
    }
    /**
     * Looks up specified type of records (MX, PTR, A, CNAME, etc.)
     * @param recValue value for which records are required
     * @param recType record type (case-sensitive)
     * @return Attributes records (may be null/empty)
     * @throws NamingException if problems encountered
     * @see javax.naming.directory.Attribute
     */
    public Attribute dnsLookup (final String recValue, final String recType) throws NamingException
    {
        if ((null == recValue) || (recValue.length() <= 0) ||
            (null == recType) || (recType.length() <= 0))
            throw new NamingException("No record name/attribute specified");

        final DirContext    ictx=new InitialDirContext(_dnsEnv);
        try
        {
            final String[]        attrIds={ recType };
            final Attributes    atts=ictx.getAttributes(recValue, attrIds);
            if ((null == atts) || (atts.size() <= 0))
                return null;

            return atts.get(recType);
        }
        finally
        {
            try
            {
                ictx.close();
            }
            catch(NamingException ne)
            {
                /* ignored */
            }
        }
    }
    /**
     * Retrieves a DNS strings response
     * @param recs returned records from the DNS query
     * @return A {@link List} of {@link String}-s of of the attributes
     * (may be null/empty)
     * @throws NamingException if problems encountered
     */
    public List<String> getStringAttributes (final Attribute recs)  throws NamingException
    {
        final int    numRecs=(null == recs) ? 0 : recs.size();
        if (numRecs <= 0)
            return null;

        List<String>    names=null;    // lazy allocation
        for (int    nameIndex=0; nameIndex < numRecs; nameIndex++)
        {
            final Object    oName=recs.get(nameIndex);
            final String    sName=(null == oName) ? null : oName.toString();
            final int        snLen=(null == sName) ? 0 :sName.length();
            if (snLen <= 0)
                continue;    // should not happen

            // sometimes a '.' ends the PTR response name - we usually do not use it
            final String    aName=('.' == sName.charAt(snLen - 1)) ? sName.substring(0, snLen-1) : sName;
            if (null == names)
                names = new ArrayList<String>(numRecs);
            names.add(aName);
        }

        return names;
    }
    /**
     * Retrieves a DNS strings response for a given record type
     * @param recValue value for which records are required
     * @param recType record type (case-sensitive)
     * @return A {@link List} of {@link String}-s of of the attributes
     * (may be null/empty)
     * @throws NamingException if problems encountered
     */
    public List<String> getStringAttributes (final String recValue, final String recType)  throws NamingException
    {
        return getStringAttributes(dnsLookup(recValue, recType));
    }
    /**
     * Attribute name to be used for naming service MX information query
     */
    public static final String    MXAttribute="MX";
    /**
     * Queries given domain MX records information
     * @param dmName domain name
     * @return MX records attributes {@link List} (null/empty if none found).
     * Format is "N H" - where N is the preference value, and H is the
     * fully-qualified domain-name (with a possible extra '.' at its end)
     * @throws NamingException if problems encountered
     * @see javax.naming.directory.Attribute
     * @see #MXAttribute
     */
    public List<String> mxLookup (final String dmName) throws NamingException
    {
        return getStringAttributes(dmName, MXAttribute);
    }
    /**
     * Attribute name to be used for naming service PTR (address->name) information query
     */
    public static final String    PTRAttribute="PTR";
    /**
     * Virtual DNS domain attached to PTR queries
     */
    public static final String    ptrVirtualDomain="in-addr.arpa";
        public static final char[]    ptrVirtualDomainChars=ptrVirtualDomain.toCharArray();
    /**
     * Converts an IPv4 address to a PTR lookup value
     * @param addr original (IPv4) address
     * @param startPos position in sequence to start parsing
     * @param len number of characters to parse
     * @return PTR lookup value (or null/empty if error)
     */
    public String getPtrAddressString (final CharSequence addr, final int startPos, final int len)
    {
        final int    aLen=(null == addr) ? 0 : addr.length(), maxPos=startPos + len;
        if ((startPos < 0) || (maxPos > aLen) || (len <= 0))
            return null;

        final ParsableString    ps=new ParsableString(addr, startPos, len);
        final int                psStart=ps.getStartIndex(), psMaxIndex=ps.getMaxIndex();
        // TODO update the code for IPv6
        final int[]                ipv4Address=new int[IPv4Address.ADDRESS_LENGTH];
        int                        addrLen=0;

        for (int    curPos=ps.findNonEmptyDataStart(); (curPos >= psStart) && (curPos < psMaxIndex);  )
        {
            final int    endPos=ps.findNumberEnd(curPos);
            if ((endPos <= curPos) || (endPos > psMaxIndex))
                return null;

            if (addrLen >= ipv4Address.length)
                return null;

            try
            {
                ipv4Address[addrLen] = ps.getUnsignedInt(curPos, endPos);
                addrLen++;
            }
            catch(NumberFormatException nfe)
            {
                return null;    // should not happen
            }

            final int    dotPos=ps.findNonEmptyDataStart(endPos);
            if ((dotPos < psStart) || (dotPos >= psMaxIndex))
                break;    // OK if no more data

            // make sure separator is dot
            if (ps.getCharAt(dotPos) != '.')
                return null;

            // IP address cannot just end with '.'
            if (((curPos=ps.findNonEmptyDataStart(dotPos+1)) <= dotPos) || (curPos >= psMaxIndex))
                return null;
        }

        if (addrLen != ipv4Address.length)
            return null;    // make sure EXACT IPv4 address length decoded

        final StringBuilder    sb=new StringBuilder(addrLen * (NumberTables.MAX_UNSIGNED_INT_DIGITS_NUM + 1) + ptrVirtualDomainChars.length + 4);
        for ( ; addrLen > 0; addrLen--)    // place components in reverse
        {
            if (addrLen < ipv4Address.length)
                sb.append('.');

            sb.append(ipv4Address[addrLen - 1]);
        }

        if (sb.length() <= 0)
            return null;    // error if no current address extracted

        sb.append('.')
          .append(ptrVirtualDomainChars)
          ;

        return sb.toString();
    }
    /**
     * Converts an IPv4 address to a PTR lookup value
     * @param addr original (IPv4) address
     * @return PTR lookup value (or null/empty if error)
     */
    public String getPtrAddressString (final CharSequence addr)
    {
        return getPtrAddressString(addr, 0, (null == addr) ? 0 : addr.length());
    }
    /**
     * Attempts to find all names of specified (IPv4) address
     * @param addr (IPv4) address
     * @param startPos position in sequence to start parsing
     * @param len number of characters to parse
     * @return A {@link List} of all available aliases (may be null/empty)
     * @throws NamingException if problems encountered
     * @see #PTRAttribute
     */
    public List<String> ptrLookup (final CharSequence addr, final int startPos, final int len) throws NamingException
    {
        return getStringAttributes(getPtrAddressString(addr, startPos, len), PTRAttribute);
    }
    /**
     * Attempts to find all names of specified (IPv4) address
     * @param addr (IPv4) address
     * @return A {@link List} of all available aliases (may be null/empty)
     * @throws NamingException if problems encountered
     * @see #PTRAttribute
     */
    public List<String> ptrLookup (CharSequence addr) throws NamingException
    {
        return ptrLookup(addr, 0, (null == addr) ? 0 : addr.length());
    }
    /**
     * Attribute name to be used for naming service A (name->address) information query
     */
    public static final String    AAttribute="A";
    /**
     * @param name Fully qualified DNS name (including domain)
     * @return A {@link List} of all available aliases (may be null/empty)
     * @throws NamingException if problems encountered
     * @see #AAttribute
     */
    public List<String> aLookup (final String name) throws NamingException
    {
        return getStringAttributes(dnsLookup(name, AAttribute));
    }
    /**
     * Attribute name to be used for naming service AAAA (name->address) information query
     */
    public static final String    AAAAttribute="AAAA";
    /**
     * @param name Fully qualified DNS name (including domain)
     * @return A {@link List} of all available aliases (may be null/empty)
     * @throws NamingException if problems encountered
     * @see #AAAAttribute
     */
    public List<String> aaaaLookup (final String name) throws NamingException
    {
        return getStringAttributes(dnsLookup(name, AAAAttribute));
    }

    /**
     * Attribute name to be used for canonical name information query
     */
    public static final String    CNAMEAttribute="CNAME";

    /**
     * @param value
     * @return A {@link List} of all available canonical names (may be null/empty)
     * @throws NamingException if problems encountered
     * @see #CNAMEAttribute
     */
    public List<String> cnameLookup(String value) throws NamingException {
        return getStringAttributes(dnsLookup(value, CNAMEAttribute));

    }
}
