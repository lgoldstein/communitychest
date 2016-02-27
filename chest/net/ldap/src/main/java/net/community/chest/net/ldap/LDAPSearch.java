/**
 * 
 */
package net.community.chest.net.ldap;

import java.io.PrintStream;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 * Similar to the <A HREF="http://linux.die.net/man/1/ldapsearch">{@code ldapsearch}</A> command
 * 
 * @author Lyor G.
 * @since Jul 20, 2008 1:05:40 PM
 */
public final class LDAPSearch extends SearchControls {
    // we're not serializing it
	private static final long serialVersionUID = 1;

	private static final int adjustErrCode (final int nErr)
	{
		return (nErr > 0) ? (0 - nErr) : nErr;
	}
	/**
	 * Available options - each element is a line to be displayed 
	 */
	private static final String[]	opts={
		"-h host\t\tLDAP server name/address",
		"-p port\t\tAccess port (default=" + LDAPProtocol.IPPORT_LDAP + ")",
		"-D bindDN\tif not specified then non-authenticated access is assumed",
		"-s scope\t(one of: ONE,BASE,default=SUB)",
		"-w password\t(for simple authentication only)",
		"-b baseDN\t\tbase location to look from (default=empty)",
		"-P version\t\tLDAP version (1, 2 or 3)",
		"-R referrals are not to be followed automatically",
		"-connect limit\t\ttimeout (msec.) for connecting to the LDAP server",
		"-l limit\t\ttime limit value (msec.)",
		"-z limit\t\tsize limit value",
		"-v\tshow more info to STDOUT (recommend to define it FIRST)",
		"-entry name\tspecific entry attributes - NOTE: if specified then filter is ignored"
	};

	private static final int showUsage (final PrintStream out, final int retCode)
	{
		out.println("Usage: ldapsearch [options] [filter (default: objectclass=*)] [attrs]");
		for (final String o : opts)
            out.append('\t').println(o);

		return retCode;
	}

	private static final int showUsageError (final String msg, final PrintStream out, final PrintStream err, final int retCode)
	{
		err.println(msg);
		
		return showUsage(out, retCode);
	}

	private final transient PrintStream	_out,_err;
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
			final int			retVal=Integer.parseInt(propVal);
			final Properties	env=getContextEnvironment();
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
	private LDAPSearch (final PrintStream out, final PrintStream err) throws IllegalArgumentException
	{
		if ((null == (_out=out)) || (null == (_err=err)))
			throw new IllegalArgumentException("Missing STDOUT/ERR stream(s)");

		setSearchScope(SearchControls.SUBTREE_SCOPE);
	    {
	    	final String	facName=System.getProperty(LDAPProtocol.DEFAULT_LDAP_FACTORY_PROPNAME,LDAPProtocol.DEFAULT_LDAP_FACTORY_PROPVAL);
	    	if (isVerbose())
	    		getStdout().println("\t" + Context.INITIAL_CONTEXT_FACTORY + "=" + facName);
	    	_env.put(Context.INITIAL_CONTEXT_FACTORY, facName);
	    }
	}

	private boolean	_verbose	/* =false */;
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
	private String	_host	/* =null */;
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
	private int	_port=LDAPProtocol.IPPORT_LDAP;
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
	private String	_bindDN	/* =null */;
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
	private String	_password	/* =null */;
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
	/**
	 * Filter to be used when searching - default="objectclass=*"
	 */
	private String	_filter="objectclass=*";
	public String getFilter ()
	{
		return _filter;
	}

	public void setFilter (String filter)
	{
		if (isVerbose())
			getStdout().println("\tsetFilter(" + _filter + " => " + filter + ")");
		_filter = filter;
	}

	@Override
	public void setSearchScope (int scope)
	{
		if (isVerbose())
			getStdout().println("\tsetScope(" + getSearchScope() + " => " + scope + ")");
		super.setSearchScope(scope);
	}

	public void setSearchScope (final String scope) throws IllegalArgumentException
	{
	    if ("sub".equalsIgnoreCase(scope))
	    	setSearchScope(SearchControls.SUBTREE_SCOPE);
	    else if ("base".equalsIgnoreCase(scope))
	    	setSearchScope(SearchControls.OBJECT_SCOPE);
	    else if ("one".equalsIgnoreCase(scope))
	    	setSearchScope(SearchControls.ONELEVEL_SCOPE);
	    else
	    	throw new IllegalArgumentException("Unknown scope: " + scope);
	}

	@Override
	public void setCountLimit (final long limit)
	{
		if (isVerbose())
			getStdout().println("\tsetCountLimit(" + getCountLimit() + " => " + limit);
		super.setCountLimit(limit);
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
	/**
	 * Base DN - default=empty
	 */
	private String	_baseDN="";
	public String getBaseDN ()
	{
		return _baseDN;
	}

	public void setBaseDN (String baseDN)
	{
		if (isVerbose())
			getStdout().println("\tsetBaseDN(" + getBaseDN() + " => " + baseDN + ")");

		_baseDN = baseDN;
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
	/**
	 * Specific entry name whose attributes are to be extracted
	 */
	private String	_entryName	/* =null */;
	public String getEntryName ()
	{
		return _entryName;
	}

	public void setEntryName (String entryName)
	{
		_entryName = entryName;
	}
	/**
	 * @param args same as the old trusted LDAPSEARCH
	 * @return 0 if successful
	 */
	private int processArgs (final String[] args)
	{
		int	aIndex=0;
		for ( ; aIndex < args.length; aIndex++)
		{
			final String	optName=args[aIndex];
			if ((null == optName) || (optName.length() <= 0))
				return showUsageError("Null/empty option #" + (aIndex + 1) + " name", (-1));

			// stop at first non-option
			if (optName.charAt(0) != '-')
				break;

			final String	optVal=optName.substring(1);
			if ((null == optVal) || (optVal.length() <= 0))
				return showUsageError("Null/empty option #" + (aIndex + 1) + " value", (-2));

			if ("h".equals(optVal))
			{
				aIndex++;

				final String	hostName=(aIndex < args.length) ? args[aIndex] : null;
				if ((null == hostName) || (hostName.length() <= 0))
					return showUsageError("Missing host name after " + optName + " option", (-3));
				setHost(hostName);
			}
			else if ("p".equals(optVal))
			{
				aIndex++;

				final String	portName=(aIndex < args.length) ? args[aIndex] : null;
				if ((null == portName) || (portName.length() <= 0))
					return showUsageError("Missing port number after " + optName + " option", (-4));

				final int	portValue;
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

				final String	bindDN=(aIndex < args.length) ? args[aIndex] : null;
				if ((null == bindDN) || (bindDN.length() <= 0))
					return showUsageError("Missing bind DN after " + optName + " option", (-7));
				setBindDN(bindDN);
			}
			else if ("b".equals(optVal))
			{
				aIndex++;

				final String	baseDN=(aIndex < args.length) ? args[aIndex] : null;
				if (null == baseDN)
					return showUsageError("Missing base DN after " + optName + " option", (-7));
				setBaseDN(baseDN);
			}
			else if ("w".equals(optVal))
			{
				aIndex++;

				final String	passwd=(aIndex < args.length) ? args[aIndex] : null;
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
			else if ("s".equals(optVal))
			{
				aIndex++;

				final String	scope=(aIndex < args.length) ? args[aIndex] : null;
				try
				{
					setSearchScope(scope);
				}
				catch(IllegalArgumentException e)
				{
					return showUsageError("Bad/Illegal scope: " + scope, (-10));
				}
			}
			else if ("l".equals(optVal))
			{
				aIndex++;

				final String	limitName=(aIndex < args.length) ? args[aIndex] : null;
				try
				{
					setTimeLimit((int) TimeUnit.SECONDS.toMillis(Integer.parseInt(limitName)));
				}
				catch(NumberFormatException e)
				{
					return showUsageError("Bad time limit value (" + limitName + ") for " + optName + " option", (-11));
				}
			}
			else if ("z".equals(optVal))
			{
				aIndex++;

				final String	limitName=(aIndex < args.length) ? args[aIndex] : null;
				try
				{
					setCountLimit(Long.parseLong(limitName));
				}
				catch(NumberFormatException e)
				{
					return showUsageError("Bad size limit value (" + limitName + ") for " + optName + " option", (-12));
				}
			}
			else if ("entry".equals(optVal))
			{
				aIndex++;

				final String	eName=(aIndex < args.length) ? args[aIndex] : null;
				if ((null == eName) || (eName.length() <= 0))
					return showUsageError("Missing entry name after " + optName + " option", (-13));

				setEntryName(eName);
			}
			else if ("connect".equals(optVal))
			{
				aIndex++;

				final String	limitName=(aIndex < args.length) ? args[aIndex] : null;
				final int		limitValue=updateEnvironmentIntParam("com.sun.jndi.ldap.connect.timeout", limitName);
				if (limitValue <= 0)
					return showUsageError("Illegal connect timeout limit value (" + limitName + ") for " + optName + " option", limitValue);
			}
			else if ("P".equals(optVal))
			{
				aIndex++;

				final String	versionName=(aIndex < args.length) ? args[aIndex] : null;
				final int		versionValue=updateEnvironmentIntParam(LDAPProtocol.DEFAULT_LDAP_VERSION_PROPNAME, versionName);
				if ((versionValue < 2) || (versionValue > 3))
					return showUsageError("Illegal LDAP version value (" + versionName + ") for " + optName + " option", Byte.MIN_VALUE);
			}
			else
				return showUsageError("Unknown option #" + (aIndex + 1) + ": " + optName, Integer.MIN_VALUE);
		}

		if (aIndex < args.length)
		{
			final String	filter=args[aIndex];
			if ((null == filter) || (filter.length() <= 0))
				return showUsageError("Bad filter value: " + filter, Byte.MIN_VALUE);
			setFilter(filter);
			aIndex++;
		}

		// attributes
		if (aIndex < args.length) {
		    String[] attrs = new String[args.length - aIndex];
		    System.arraycopy(args, aIndex, attrs, 0, attrs.length);
		    setReturningAttributes(attrs);
		}

		return 0;
	}
	/**
	 * @return search context to be used
	 * @throws NamingException if exception(s) during context setup
	 */
	private DirContext getSearchContext () throws NamingException
	{
		final Properties	env=getContextEnvironment();
	    {
	    	final String	host=getHost();
	    	if ((null == host) || (host.length() <= 0))
	    		throw new NamingException("Null/empty LDAP server host name/address");

	    	final int	port=getPort();
	    	if ((port <= 0) || (port >= 0x0000FFFF))
	    		throw new NamingException("Bad/illegal LDAP server port: " + port);

	    	final String	accURL="ldap://" + host + ":" + port;
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
	    	final String	bindDN=getBindDN();
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
		DirContext	ctx=getSearchContext();
		if (null == ctx)	// should not happen
			throw new NamingException("No context instance");

		try
		{
			return ctx.search(getBaseDN(), getFilter(), this);
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

		int	numAttrs=0;
		final NamingEnumeration<? extends Attribute> attrs=(null == sa) ? null : sa.getAll();
		try {
    		while ((attrs != null) && attrs.hasMore())
    		{
    			final Attribute	a=attrs.next();
    			if (null == a)	// should not happen
    				continue;
    
    			out.println("\t" + a.getID());
    
    			final int	numValues=a.size();
    			for (int	vIndex=0; vIndex < numValues; vIndex++)
    			{
    				final Object	v=a.get(vIndex);
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
	 * @return Number of processed attributes
	 * @throws NamingException if unable to access attributes
	 */
	private static int showSearchResult (final PrintStream out, final SearchResult r) throws NamingException
	{
		return processEntryAttributes(out, (null == r) ? null : r.getName(), (null == r) ? null : r.getAttributes());
	}
	/**
	 * @return number of processed entries (negative if error)
	 * @throws NamingException if unable to access the server
	 */
	private int doFilteredSearch () throws NamingException
	{
		NamingEnumeration<SearchResult>	res=null;
		try
		{
			res = search();

			int	numResults=0;
			while ((res != null) && res.hasMore())
			{
				final SearchResult	r=res.next();
				if (r != null)	// should not be otherwise
				{
					showSearchResult(getStdout(), r);
					numResults++;
				}
			}

			return numResults;
		}
		finally
		{
			if (res != null)
			{
				try
				{
					res.close();
				}
				catch(NamingException ce)
				{
					if (isVerbose())
						getStderr().println(ce.getClass().getName() + " while closing results: " + ce.getMessage());
				}

				res = null;
			}
		}
	}
	/**
	 * @param eName entry name whose attributes are requested
	 * @return number of processed attributes (negative if error)
	 * @throws NamingException if unable to access the server
	 */
	private int doEntryAttributesDisplay (final String eName) throws NamingException
	{
		DirContext	ctx=getSearchContext();
		if (null == ctx)	// should not happen
			throw new NamingException("No context instance");

		try
		{
			return processEntryAttributes(getStdout(), eName, ctx.getAttributes(eName));
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
	/**
	 * @return number of processed entries (negative if error)
	 */
	private int doSearch ()
	{
		try
		{
			final String	eName=getEntryName();
			if ((null == eName) || (eName.length() <= 0))
				return doFilteredSearch();
			else
				return doEntryAttributesDisplay(eName);
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

		LDAPSearch	lds=new LDAPSearch(out, err);
		int			nErr=lds.processArgs(args);
		if (nErr != 0)
			return adjustErrCode(nErr);

		return lds.doSearch();
	}
	/**
	 * @param args same as the old trusted LDAPSEARCH
	 */
	public static void main (final String[] args)
	{
		try
		{
			final int	nErr=doMain(args, System.out, System.err);
			if (nErr < 0)
				System.err.println("Failed: err=" + nErr);
		}
		catch(Throwable t)
		{
			System.err.println(t.getClass().getName() + ": " + t.getMessage());
			t.printStackTrace(System.err);
		}
	}
}
