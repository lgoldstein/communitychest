/*
 *
 */
package net.community.chest.net.ldap;

import java.io.IOException;
import java.io.PrintStream;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;

import net.community.chest.io.encode.base64.Base64;

/**
 * <P>Copyright as per GPLv2</P>
 *
 * Similar to the <A HREF="http://linux.die.net/man/1/ldapcompare">{@code ldapcompare}</A> command.
 * Inspired by <A HREF="http://docs.oracle.com/javase/jndi/tutorial/ldap/search/compare.html">The LDAP &quot;Compare&quot; Operation</A>
 * @author Lyor G.
 * @since Oct 29, 2014 10:06:04 AM
 */
public class LDAPCompare extends SearchControls {
    // we're not serializing it
    private static final long serialVersionUID = 1;
    /**
     * Available options - each element is a line to be displayed
     */
    private static final String[]   opts={
        "-h host\t\tLDAP server name/address",
        "-p port\t\tAccess port (default=" + LDAPProtocol.IPPORT_LDAP + ")",
        "-D bindDN\tif not specified then non-authenticated access is assumed",
        "-w password\t(for simple authentication only)",
        "-P version\t\tLDAP version (1, 2 or 3)",
        "-R referrals are not to be followed automatically",
        "-connect limit\t\ttimeout (msec.) for connecting to the LDAP server",
        "-l limit\t\ttime limit value (msec.)",
        "-v\tshow more info to STDOUT (recommend to define it FIRST)"
    };
    private static final int adjustErrCode (final int nErr)
    {
        return (nErr > 0) ? (0 - nErr) : nErr;
    }

    private static final int showUsage (final PrintStream out, final int retCode)
    {
        out.println("Usage: ldapcompare [options] DN {attr:value|attr::value}");
        for (final String o : opts)
            out.append('\t').println(o);

        return retCode;
    }

    private static final int showUsageError (final String msg, final PrintStream out, final PrintStream err, final int retCode)
    {
        err.println(msg);

        return showUsage(out, retCode);
    }

    private final transient PrintStream _out,_err;
    public PrintStream getStdout ()
    {
        return _out;
    }

    public PrintStream getStderr ()
    {
        return _err;
    }

    private int showUsageError (final String msg, final int retCode)
    {
        return showUsageError(msg, getStdout(), getStderr(), retCode);
    }
    /**
     * LDAP access initialization properties
     */
    private final Properties _env=new Properties();
    public Properties getContextEnvironment ()
    {
        return _env;
    }
    /**
     * @param propName property name to be added to the environment used
     * to generated the {@link DirContext} instance
     * @param propVal property value - MUST be a valid integer
     * @return integer value (negative if error)
     */
    private int updateEnvironmentIntParam (final String propName, final String propVal)
    {
        try
        {
            final int           retVal=Integer.parseInt(propVal);
            final Properties    env=getContextEnvironment();
            env.put(propName, propVal);
            if (isVerbose())
                getStdout().println("env(" + propName + "=" + propVal + ")");
            return retVal;
        }
        catch(NumberFormatException e)
        {
            getStderr().println("updateEnvironmentIntParam(" + propName + "=" + propVal + ") bad/illegal value: " + e.getMessage());
            return Integer.MIN_VALUE;
        }
    }
    /**
     * Default constructor
     * @param out STDOUT stream
     * @param err STDERR stream
     * @throws IllegalArgumentException if null stream(s)
     */
    private LDAPCompare (final PrintStream out, final PrintStream err) throws IllegalArgumentException
    {
        if ((null == (_out=out)) || (null == (_err=err)))
            throw new IllegalArgumentException("Missing STDOUT/ERR stream(s)");

        setSearchScope(SearchControls.OBJECT_SCOPE);
        setReturningAttributes(new String[0]);       // return no attributes

        {
            final String    facName=System.getProperty(LDAPProtocol.DEFAULT_LDAP_FACTORY_PROPNAME,LDAPProtocol.DEFAULT_LDAP_FACTORY_PROPVAL);
            if (isVerbose())
                getStdout().println("\t" + Context.INITIAL_CONTEXT_FACTORY + "=" + facName);
            _env.put(Context.INITIAL_CONTEXT_FACTORY, facName);
        }
    }

    private boolean _verbose    /* =false */;
    public boolean isVerbose ()
    {
        return _verbose;
    }

    public void setVerbose (boolean verbose)
    {
        _verbose = verbose;
    }
    /**
     * Host name/address to which search query should be addressed
     */
    private String  _host   /* =null */;
    public String getHost ()
    {
        return _host;
    }

    public void setHost (String host)
    {
        if (isVerbose())
            getStdout().println("\tsetHost(" + _host + " => " + host + ")");
        _host = host;
    }
    /**
     * Port number to use for the query - default={@link LDAPProtocol#IPPORT_LDAP}
     */
    private int _port=LDAPProtocol.IPPORT_LDAP;
    public int getPort ()
    {
        return _port;
    }

    public void setPort (int port)
    {
        if (isVerbose())
            getStdout().println("\tsetPort(" + _port + " => " + port + ")");
        _port = port;
    }
    /**
     * Bind DN value - if null/empty then no authentication required
     */
    private String  _bindDN /* =null */;
    public String getBindDN ()
    {
        return _bindDN;
    }

    public void setBindDN (String bindDN)
    {
        if (isVerbose())
            getStdout().println("\tsetBindDN(" + _bindDN + " => " + bindDN + ")");
        _bindDN = bindDN;
    }
    /**
     * Password to be used if simple authentication required
     */
    private String  _password   /* =null */;
    public String getPassword ()
    {
        return _password;
    }

    public void setPassword (String password)
    {
        if (isVerbose())
            getStdout().println("\tsetPassword(" + _password + " => " + password + ")");
        _password = password;
    }

    @Override
    public void setDerefLinkFlag (final boolean on)
    {
        if (isVerbose())
            getStdout().println("\tsetDerefLinkFlag(" + getDerefLinkFlag() + " => " + on + ")");
        super.setDerefLinkFlag(on);
    }

    @Override
    public void setTimeLimit (final int ms)
    {
        if (isVerbose())
            getStdout().println("\tsetTimeLimit(" + getTimeLimit() + " => " + ms + ")");
        super.setTimeLimit(ms);
    }

    private String _referralMode="follow";
    public String getReferralMode()
    {
        return _referralMode;
    }

    public void setReferralMode (String mode)
    {
        _referralMode = mode;
    }

    private String  _dn;
    public String getDN() {
        return _dn;
    }

    public void setDN(String dn) {
        _dn = dn;
    }

    private String  _attrName;
    public String getAttributeName() {
        return _attrName;
    }

    public void setAttributeName(String n) {
        _attrName = n;
    }

    private String  _attrValue;
    public String getAttributeValue() {
        return _attrValue;
    }

    public void setAttributeValue(String v) {
        _attrValue = v;
    }

    /**
     * @param args same as the old trusted LDAPCOMPARE
     * @return 0 if successful
     */
    private int processArgs(final String ... args)
    {
        int aIndex=0;
        for ( ; aIndex < args.length; aIndex++)
        {
            final String    optName=args[aIndex];
            if ((null == optName) || (optName.length() <= 0))
                return showUsageError("Null/empty option #" + (aIndex + 1) + " name", (-1));

            // stop at first non-option
            if (optName.charAt(0) != '-')
                break;

            final String    optVal=optName.substring(1);
            if ((null == optVal) || (optVal.length() <= 0))
                return showUsageError("Null/empty option #" + (aIndex + 1) + " value", (-2));

            if ("h".equals(optVal))
            {
                aIndex++;

                final String    hostName=(aIndex < args.length) ? args[aIndex] : null;
                if ((null == hostName) || (hostName.length() <= 0))
                    return showUsageError("Missing host name after " + optName + " option", (-3));
                setHost(hostName);
            }
            else if ("p".equals(optVal))
            {
                aIndex++;

                final String    portName=(aIndex < args.length) ? args[aIndex] : null;
                if ((null == portName) || (portName.length() <= 0))
                    return showUsageError("Missing port number after " + optName + " option", (-4));

                final int   portValue;
                try
                {
                    portValue = Integer.parseInt(portName);
                }
                catch(NumberFormatException e)
                {
                    return showUsageError("Bad port value number (" + portName + ") for " + optName + " option", (-5));
                }

                if ((portValue <= 0) || (portValue >= 0x0000FFFF))
                    return showUsageError("Illegal port value number (" + portName + ") for " + optName + " option", (-6));
                setPort(portValue);
            }
            else if ("D".equals(optVal))
            {
                aIndex++;

                final String    bindDN=(aIndex < args.length) ? args[aIndex] : null;
                if ((null == bindDN) || (bindDN.length() <= 0))
                    return showUsageError("Missing bind DN after " + optName + " option", (-7));
                setBindDN(bindDN);
            }
            else if ("w".equals(optVal))
            {
                aIndex++;

                final String    passwd=(aIndex < args.length) ? args[aIndex] : null;
                if ((null == passwd) || (passwd.length() <= 0))
                    return showUsageError("Missing password after " + optName + " option", (-8));
                setPassword(passwd);
            }
            else if ("R".equals(optVal))
            {
                setReferralMode("ignore");
            }
            else if ("v".equals(optVal))
            {
                setVerbose(true);
            }
            else if ("l".equals(optVal))
            {
                aIndex++;

                final String    limitName=(aIndex < args.length) ? args[aIndex] : null;
                try
                {
                    setTimeLimit(Integer.parseInt(limitName));
                }
                catch(NumberFormatException e)
                {
                    return showUsageError("Bad time limit value (" + limitName + ") for " + optName + " option", (-11));
                }
            }
            else if ("connect".equals(optVal))
            {
                aIndex++;

                final String    limitName=(aIndex < args.length) ? args[aIndex] : null;
                final int       limitValue=updateEnvironmentIntParam("com.sun.jndi.ldap.connect.timeout", limitName);
                if (limitValue <= 0)
                    return showUsageError("Illegal connect timeout limit value (" + limitName + ") for " + optName + " option", limitValue);
            }
            else if ("P".equals(optVal))
            {
                aIndex++;

                final String    versionName=(aIndex < args.length) ? args[aIndex] : null;
                final int       versionValue=updateEnvironmentIntParam(LDAPProtocol.DEFAULT_LDAP_VERSION_PROPNAME, versionName);
                if ((versionValue < 2) || (versionValue > 3))
                    return showUsageError("Illegal LDAP version value (" + versionName + ") for " + optName + " option", Byte.MIN_VALUE);
            }
            else
                return showUsageError("Unknown option #" + (aIndex + 1) + ": " + optName, Integer.MIN_VALUE);
        }

        int remaining=args.length - aIndex;
        if (remaining != 2) {
            return showUsageError("Incomplete arguments - missing DN, attribute value or both", Integer.MIN_VALUE);
        }

        setDN(args[aIndex]);

        String  valSpec=args[aIndex + 1];
        int     pos=valSpec.indexOf(':');
        if ((pos <= 0) || (pos >= (valSpec.length() - 1))) {
            return showUsageError("Malformed attribute value", Integer.MIN_VALUE);
        }

        setAttributeName(valSpec.substring(0, pos));

        if (valSpec.charAt(pos + 1) == ':') {
            pos++;

            if (pos >= (valSpec.length() - 1)) {
                return showUsageError("Malformed BASE64 attribute value", Integer.MIN_VALUE);
            }

            String  b64Value=valSpec.substring(pos + 1);
            try {
                setAttributeValue(Base64.decode(b64Value));
            } catch(IOException e) {
                return showUsageError("Malformed BASE64 attribute value (" + e.getClass().getSimpleName() + "): " + e.getMessage(), Short.MIN_VALUE);
            }
        } else {
            setAttributeValue(valSpec.substring(pos + 1));
        }

        return 0;
    }
    /**
     * @return search context to be used
     * @throws NamingException if exception(s) during context setup
     */
    private DirContext getSearchContext () throws NamingException
    {
        final Properties    env=getContextEnvironment();
        {
            final String    host=getHost();
            if ((null == host) || (host.length() <= 0))
                throw new NamingException("Null/empty LDAP server host name/address");

            final int   port=getPort();
            if ((port <= 0) || (port >= 0x0000FFFF))
                throw new NamingException("Bad/illegal LDAP server port: " + port);

            final String    accURL="ldap://" + host + ":" + port;
            if (isVerbose())
                getStdout().println("\t" + Context.PROVIDER_URL + "=" + accURL);
            env.put(Context.PROVIDER_URL, accURL);
        }

        {
            final String   refMode=getReferralMode();
            if ((refMode != null) && (refMode.length() > 0))
            {
                env.put(Context.REFERRAL, refMode);
            }
        }

        {
            final String    bindDN=getBindDN();
            if ((bindDN != null) && (bindDN.length() > 0))
            {
                env.put(Context.SECURITY_AUTHENTICATION, "simple");
                env.put(Context.SECURITY_PRINCIPAL, bindDN);
                env.put(Context.SECURITY_CREDENTIALS, getPassword());
            }
            else
                env.put(Context.SECURITY_AUTHENTICATION, "none");
        }

        return new InitialDirContext(env);
    }
    /**
     * @return search results
     * @throws NamingException if exception(s) during search
     */
    private NamingEnumeration<SearchResult> search () throws NamingException
    {
        DirContext  ctx=getSearchContext();
        if (null == ctx)    // should not happen
            throw new NamingException("No context instance");

        try
        {
            return ctx.search(getDN(), getAttributeName() + "=" + getAttributeValue(), this);
        }
        finally
        {
            try
            {
                ctx.close();
            }
            catch(NamingException e)
            {
                if (isVerbose())
                    getStderr().println(e.getClass().getName() + " on close context: " + e.getMessage());
            }

            ctx = null;
        }
    }

    private static int processEntryAttributes (final PrintStream out, final String entryName, final Attributes sa) throws NamingException
    {
        out.println(entryName);

        int numAttrs=0;
        final NamingEnumeration<? extends Attribute> attrs=(null == sa) ? null : sa.getAll();
        try {
            while ((attrs != null) && attrs.hasMore())
            {
                final Attribute a=attrs.next();
                if (null == a)  // should not happen
                    continue;

                out.println("\t" + a.getID());

                final int   numValues=a.size();
                for (int    vIndex=0; vIndex < numValues; vIndex++)
                {
                    final Object    v=a.get(vIndex);
                    out.println("\t\t[" + ((v != null) ? v.getClass().getName() : null) + "]=" + v);
                }

                numAttrs++;
            }
        } finally {
            if (attrs != null) {
                attrs.close();
            }
        }

        out.println();
        return numAttrs;
    }
    /**
     * @param out output {@link PrintStream} to display results to
     * @param r search result
     * @return Number of processed entries
     * @throws NamingException if unable to access attributes
     */
    private static int showSearchResult (final PrintStream out, final SearchResult r) throws NamingException
    {
        return processEntryAttributes(out, (null == r) ? null : r.getName(), (null == r) ? null : r.getAttributes());
    }
    /**
     * @return number of processed entries (negative if error)
     */
    private int doSearch ()
    {
        try
        {
            NamingEnumeration<SearchResult> res=search();
            try {
                if (res.hasMore()) {
                    if (isVerbose()) {
                        showSearchResult(getStdout(), res.next());
                    } else {
                        getStdout().println("TRUE");
                    }
                } else {
                    getStdout().println("FALSE");
                }

                return 0;
            } finally {
                res.close();
            }
        }
        catch(NamingException e)
        {
            getStderr().println(e.getClass().getName() + " while searching: " + e.getMessage());
            return Short.MIN_VALUE;
        }
    }
    /**
     * @param args same as the old trusted LDAPSEARCH
     * @param out STDOUT stream
     * @param err STDERR stream
     * @return negative if successful - otherwise number of returned entries
     */
    public static int doMain (final String[] args, final PrintStream out, final PrintStream err)
    {
        if ((null == args) || (args.length <= 0))
            return showUsage(out, 0);

        LDAPCompare ldc=new LDAPCompare(out, err);
        int         nErr=ldc.processArgs(args);
        if (nErr != 0)
            return adjustErrCode(nErr);

        return ldc.doSearch();
    }
    /**
     * @param args same as the old trusted LDAPSEARCH
     */
    public static void main (final String[] args)
    {
        try
        {
            final int   nErr=doMain(args, System.out, System.err);
            if (nErr < 0)
                System.err.println("Failed: err=" + nErr);
        }
        catch(Exception e)
        {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            e.printStackTrace(System.err);
        }
    }
}
