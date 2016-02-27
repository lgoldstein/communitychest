/**
 * 
 */
package net.community.chest.apache.httpclient.hotmail;

import java.io.Closeable;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

import net.community.chest.apache.httpclient.HttpClientUtils;
import net.community.chest.io.FileUtil;
import net.community.chest.io.IOCopier;
import net.community.chest.net.proto.text.http.hotmail.FolderHandler;
import net.community.chest.net.proto.text.http.hotmail.HotmailProtocol;
import net.community.chest.net.proto.text.http.hotmail.LoginHandler;
import net.community.chest.net.proto.text.http.hotmail.MessagesHandler;

import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpState;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpClientParams;
import org.apache.webdav.lib.methods.PropFindMethod;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Aug 3, 2008 9:42:55 AM
 */
public class HotmailClient implements Closeable {
	/**
	 * Initial host from which access starts
	 */
	public static final HostConfiguration	DefaultHostAccess=new HostConfiguration();
	static {
		DefaultHostAccess.setHost(HotmailProtocol.DEFAULT_ACCESS_HOST, HotmailProtocol.DEFAULT_ACCESS_PORT);
	}
	/**
	 * Connection manager used for all access connections
	 */
	protected static final MultiThreadedHttpConnectionManager	_mgr=new MultiThreadedHttpConnectionManager();
	/**
	 * @return connection manager used for all access connections - should be used for configuring the manager
	 */
	public static final MultiThreadedHttpConnectionManager getConnectionsManager ()
	{
		return _mgr;
	}
	/**
	 * The internal HTTP client object used for access 
	 */
	private HttpClient	_clnt=new HttpClient(_mgr);
	/**
	 * Sets the socket timeout (SO_TIMEOUT) in milliseconds which is the timeout for waiting for data.
	 * @param newTimeoutInMilliseconds - milliseconds
	 */
	public void setTimeout (int newTimeoutInMilliseconds)
	{
		final HttpClientParams	pars=_clnt.getParams();
		pars.setSoTimeout(newTimeoutInMilliseconds);
	}
	/**
	 * Current "User-Agent" header value used
	 */
	private String _userAgent=HotmailProtocol.DEFAULT_USER_AGENT;
	/**
	 * @return "User-Agent" header value used
	 */
	public String getUserAgent ()
	{
		return _userAgent;
	}
	/**
	 * @param userAgent user agent to be used - if null/empty then no change
	 */
	public void setUserAgent (String userAgent)
	{
		if ((userAgent != null) && (userAgent.length() > 0))
			_userAgent = userAgent;
	}
	/**
	 * The internal HTTP state object (credentials, cookies, etc.)
	 */
	private HttpState		_clState=_clnt.getState();
	/**
	 * Properties used to access various functions - key=case insensitive (!) property name
	 */
	private final Map<String,String> _clProps=new TreeMap<String,String>(String.CASE_INSENSITIVE_ORDER);
	// current username/password
	private String _username /* =null */, _password /* =null */;
	/**
	 * @return true if login process completed
	 */
	public boolean isLoggedIn ()
	{
		return (_username != null) && (_username.length() > 0)
			&& (_password != null) && (_password.length() > 0);
	}

	private HostConfiguration	_initialAccess=DefaultHostAccess;
	private String				_initialPath=HotmailProtocol.DEFAULT_ACCESS_PATH;
	/**
	 * Initializes the initial parameters used to access the Hotmail account. Note:
	 * MUST be called <U>prior</U> to login.
	 * @param hc host configuration for accessing the account
	 * @param path path to be used for initial request
	 * @return TRUE if successful
	 */
	public boolean setInitialParameters (HostConfiguration hc, String path)
	{
		if (isLoggedIn() || (null == hc) ||
			(null == path) || (path.length() <= 0))
			return false;

		final String	hcHost=hc.getHost();
		if ((null == hcHost) || (hcHost.length() <= 0) || (hc.getPort() <= 0))
			return false;

		_initialAccess = hc;
		_initialPath = path;

		return true;
	}

	public static final int DEFAULT_MAX_REDIRECTS_NUM=8;
	private int	_maxRedirects=DEFAULT_MAX_REDIRECTS_NUM;
	public int getMaxRedirects ()
	{
		return _maxRedirects;
	}

	public void setMaxRedirects (int maxRedirects)
	{
		_maxRedirects = maxRedirects;
	}
	/* Note: may be called several times - only first call has the desired effect
	 * @see java.io.Closeable#close()
	 */
	@Override
	public void close ()
	{
		if (!isLoggedIn())
			return;

		_username = null;
		_password = null;

		setInitialParameters(DefaultHostAccess, HotmailProtocol.DEFAULT_ACCESS_PATH);
		_clnt = new HttpClient(_mgr);
		_clState = _clnt.getState();
		_clProps.clear();
	}

	public HotmailClient (HostConfiguration hc, String path)
	{
		setInitialParameters(hc, path);
	}

	public HotmailClient (HostConfiguration hc)
	{
		this(hc, HotmailProtocol.DEFAULT_ACCESS_PATH);
	}

	public HotmailClient (String host, int port, String path)
	{
		final HostConfiguration	hc=new HostConfiguration();
		hc.setHost(host, port);
		setInitialParameters(hc, path);
	}
	/**
	 * Default constructor - initializes internal state to default values
	 * @see #setInitialParameters(HostConfiguration hc, String path) - for
	 * overriding the default parameters
	 */
	public HotmailClient ()
	{
		this(DefaultHostAccess);
	}
	/**
	 * Makes sure the response is XML - if not, a NullPointerException
	 * occurs when calling "getResponses()" method for extracting the properties
	 * @param hm executed method object
	 * @throws IllegalStateException if bad/illegal content type returned
	 */ 
	protected static final void checkResponseContent (final HttpMethod hm) throws IllegalStateException
	{
		final Header	ctHdr=hm.getResponseHeader("content-type");
		final String	ctVal=(null == ctHdr) ? null : ctHdr.getValue();
		if ((null == ctVal) || (ctVal.length() <= 0) || (!ctVal.equalsIgnoreCase("text/xml")))
			throw new IllegalStateException("Bad/Illegal OK response content type: " + ctVal);
	}
	/**
	 * Resolves a redirection location
	 * @param locationValue fully qualified location reference
	 * @param hc host configuration to be updated with new host name/port
	 * @return new redirected path
	 * @throws IllegalStateException if errors
	 */
	protected static final String resolveStringRedirection (final String locationValue, final HostConfiguration hc) throws IllegalStateException
	{
		if ((null == locationValue) || (locationValue.length() <= 0) || (null == hc))
			throw new IllegalStateException("No re-direction location specified");

		try
		{
			final java.net.URI	redirectUri=new java.net.URI(locationValue);
			final int			redirectPort=redirectUri.getPort();
			final String		redirectHost=redirectUri.getHost();
			if ((null == redirectHost) || (redirectHost.length() <= 0))
				throw new IllegalArgumentException("resolveStringRedirection(" + locationValue + ") missing host");
			hc.setHost(redirectHost, (redirectPort <= 0) ? HotmailProtocol.DEFAULT_ACCESS_PORT : redirectPort);

			String			redirectPath=redirectUri.getPath();
			final String	redirectQuery=redirectUri.getQuery();
			// if have a query (even an empty one) than update the request path
			if ((redirectQuery != null) && (redirectQuery.length() >= 0))
				redirectPath = redirectPath + "?" + redirectQuery; 

			return redirectPath;
		}
		catch(URISyntaxException e)
		{
			throw new IllegalStateException("Bad/Illegal re-direction URI: " + locationValue);
		}
	}
	/**
	 * Resolves a redirection location
	 * @param hm HTTP method object that contains the redirection information
	 * @param hc host configuration to be updated with new host name/port
	 * @return new redirected path
	 * @throws IllegalStateException if errors
	 */
	protected static final String resolveMethodRedirection (final HttpMethod hm, final HostConfiguration hc) throws IllegalStateException
	{
		final Header locationHeader=(null == hm) ? null : hm.getResponseHeader("location");

		return resolveStringRedirection((null == locationHeader) ? null : locationHeader.getValue(), hc);
	}
	/**
	 * Creates and initialize a PROPFIND method
	 * @param path request path
	 * @param body request body
	 * @return initialized PROPFIND method
	 */
	protected HotmailPropFindMethod getPropFindMethod (String path, String body)
	{
		final HotmailPropFindMethod	pfm=new HotmailPropFindMethod();
		// do internal authentication using set credentials in state object
		pfm.setDoAuthentication(true);
		// "simulate" a Microsoft user agent
		pfm.addRequestHeader("User-Agent", _userAgent);
		/* 		Do not follow re-directions - this causes an exception
		 * since the Apache implementation does not allow for the new URI
		 * to point to a new server, and we expect this.
		 */
		pfm.setFollowRedirects(false);
		pfm.setDepth(0);
		pfm.setPath(path);
		pfm.setRequestBody(body);

		return pfm;
	}
	/**
	 * Executes the login logic
	 * @param username username to be used (usually the full e-mail address)
	 * @param password access password
	 * @throws IllegalStateException - if bad state - e.g., already logged in
	 * (see <I>"isLoggedIn"</I>), or re-direction follow-up failed, etc.
	 * @throws IllegalArgumentException - if null/empty username/password
	 * @throws IOException - if access errors
	 */
	public void login (String username, String password)
		throws IllegalStateException, IllegalArgumentException, IOException
	{
		if (isLoggedIn())
			throw new IllegalStateException("Client already logged in");
		if ((null == username) || (username.length() <= 0) ||
		    (null == password) || (password.length() <= 0))
			throw new IllegalArgumentException("Null/empty username/password");

		// prepare for login credentials - for some reason, this is the correct initialization despite documentation saying otherwise
		{
			final Credentials	creds=new UsernamePasswordCredentials(username, password);
			final AuthScope[]	sa={
					new AuthScope(AuthScope.ANY_HOST, AuthScope.ANY_PORT, "hotmail.com"),
					new AuthScope(AuthScope.ANY_HOST, AuthScope.ANY_PORT, "Microsoft Passport")
			};
			for (final AuthScope s : sa)
				_clState.setCredentials(s, creds);
		}

		// use copy constructor since we might change the contents of the access when redirected
		final HostConfiguration	hcLogin=new HostConfiguration(_initialAccess);
		final int				maxRedirects=getMaxRedirects();
		for (int	retryIndex=0; retryIndex < maxRedirects; retryIndex++)
		{
			final PropFindMethod	pfm=getPropFindMethod(_initialPath, LoginHandler.LOGIN_XML_PAYLOAD);
			final int				stCode=_clnt.executeMethod(hcLogin, pfm, _clState);

			try
			{
				if (HttpClientUtils.isOKHttpRspCode(stCode))
				{
					checkResponseContent(pfm);

					if (!LoginHandler.extractLoginProperties(pfm.getResponseDocument(), _clProps))
						throw new IllegalStateException("Cannot extract login properties from final authorization");

					_username = username;
					_password = password;
					return;
				}
				else if (HttpClientUtils.isRedirectHttpRspCode(stCode))
				{
					_initialPath = resolveMethodRedirection(pfm, hcLogin);
				}
				else	// this point is reached for bad/unexpected response code
					throw new IllegalStateException("Bad/Illegal login response code: " + stCode + " " + pfm.getStatusText());
			}
			finally
			{
				pfm.releaseConnection();
			}
		}

		// this point is reached if max retries exhausted
		throw new IllegalStateException("Login un-successful after " + maxRedirects + " retries");
	}
	/**
	 * @return available folders {@link Collection}
	 * @throws IllegalStateException if internal state problems
	 * @throws IOException if unable to access server
	 */
	public Collection<FolderHandler.FolderInfo> getFolders () throws IllegalStateException, IOException
	{
		if (!isLoggedIn())
			throw new IllegalStateException("Client is not authorized");

		final HostConfiguration	rootAccessConfig=new HostConfiguration();
		String					rootAccessPath=resolveStringRedirection(_clProps.get(LoginHandler.MsgFolderRootProp), rootAccessConfig);
		final int				maxRedirects=getMaxRedirects();
		for (int	retryIndex=0; retryIndex < maxRedirects; retryIndex++)
		{	
			final PropFindMethod	efm=getPropFindMethod(rootAccessPath, FolderHandler.FOLDERS_ENUM_XML_PAYLOAD);
			final int				stCode=_clnt.executeMethod(rootAccessConfig, efm, _clState);
			try
			{
				if (HttpClientUtils.isOKHttpRspCode(stCode))
				{	
					checkResponseContent(efm);

					final Collection<FolderHandler.FolderInfo>	fldrs=FolderHandler.extractFoldersInfo(efm.getResponseDocument());
					if ((null == fldrs) || (fldrs.size() <= 0))	// at least INBOX should exist
						throw new IllegalStateException("No folders returned");

					return fldrs;
				}
				else if (HttpClientUtils.isRedirectHttpRspCode(stCode))
				{
					rootAccessPath = resolveMethodRedirection(efm, rootAccessConfig);
				}
				else	// this point is reached for bad/unexpected response code
					throw new IllegalStateException("Bad/Illegal getFolders response code: " + stCode + " " + efm.getStatusText());
			}
			finally
			{
				efm.releaseConnection();
			}
		}

		// this point is reached if max retries exhausted
		throw new IllegalStateException("getFolders un-successful after " + maxRedirects + " retries");
	}
	/**
	 * @param folderRef folder reference whose messages information is requested
	 * @param fullInfo if TRUE the retrieves message envelope, otherwise only
	 * basic/quick info object
	 * @return A {@link Collection} of basic/quick message information (may be
	 * null/empty if no messages in folder)
	 * @throws IllegalArgumentException if bad/illegal folder reference string
	 * @throws IllegalStateException if internal processing errors
	 * @throws IOException if unable to access the remote server
	 */
	protected Collection<? extends MessagesHandler.MsgEnvelope> getFolderMsgs (String folderRef, boolean fullInfo)
		throws IllegalArgumentException, IllegalStateException, IOException
	{
		if (!isLoggedIn())
			throw new IllegalStateException("Client is not authorized");

		final HostConfiguration	fldrAccessConfig=new HostConfiguration();
		String					fldrAccessPath=resolveStringRedirection(folderRef, fldrAccessConfig);
		final int				maxRedirects=getMaxRedirects();
		for (int	retryIndex=0; retryIndex < maxRedirects; retryIndex++)
		{	
			final PropFindMethod	gfm=getPropFindMethod(fldrAccessPath, fullInfo ? MessagesHandler.envInfoXml : MessagesHandler.quickInfoXml);
			final int				stCode=_clnt.executeMethod(fldrAccessConfig, gfm, _clState);
			try
			{
				if (HttpClientUtils.isOKHttpRspCode(stCode))
				{	
					checkResponseContent(gfm);
					return MessagesHandler.extractMessagesInfo(gfm.getResponseDocument());
				}
				else if (HttpClientUtils.isRedirectHttpRspCode(stCode))
				{
					fldrAccessPath = resolveMethodRedirection(gfm, fldrAccessConfig);
				}
				else	// this point is reached for bad/unexpected response code
					throw new IllegalStateException("Bad/Illegal getFolderMsgs response code: " + stCode + " " + gfm.getStatusText());
			}
			finally
			{
				gfm.releaseConnection();
			}
		}

		// this point is reached if max retries exhausted
		throw new IllegalStateException("getFolderMsgs un-successful after " + maxRedirects + " retries");
	}
	/**
	 * @param folderRef folder reference whose messages information is requested
	 * @return A {@link Collection} of basic/quick message information (may be
	 * null/empty if no messages in folder)
	 * @throws IllegalArgumentException if bad/illegal folder reference string
	 * @throws IllegalStateException if internal processing errors
	 * @throws IOException if unable to access the remote server
	 */
	public Collection<? extends MessagesHandler.MsgInfo> getFolderMsgsInfo (String folderRef)
		throws IllegalArgumentException, IllegalStateException, IOException
	{
		return getFolderMsgs(folderRef, false);
	}
	/**
	 * @param folderRef folder reference whose messages information is requested
	 * @return array of message envelope information (may be null/empty if no messages in folder)
	 * @throws IllegalArgumentException if bad/illegal folder reference string
	 * @throws IllegalStateException if internal processing errors
	 * @throws IOException if unable to access the remote server
	 */
	public Collection<? extends MessagesHandler.MsgEnvelope> getFolderMsgsEnvelopes (String folderRef)
		throws IllegalArgumentException, IllegalStateException, IOException
	{
		return getFolderMsgs(folderRef, true);
	}
	/**
	 * Builds the GET method to be used for retrieving a message's contents
	 * @param msgAccessPath access path/URL
	 * @return GET method object to be used
	 * @throws IllegalStateException if errors encountered
	 */
	private final GetMethod getMsgDumpMethod (String msgAccessPath) throws IllegalStateException
	{
		if ((null == msgAccessPath) || (msgAccessPath.length() <= 0))
			throw new IllegalStateException("No message access path to GET method");

		final GetMethod	gm=new GetMethod();
		// do internal authentication using set credential in state object
		gm.setDoAuthentication(true);
		// "simulate" a Microsoft user agent
		gm.addRequestHeader("User-Agent", _userAgent);
		// add some more GET related headers
		gm.addRequestHeader("Accept", "message/rfc822, */*");
		gm.addRequestHeader("Translate", "f");
		gm.addRequestHeader("Cache-Control", "no-cache");

		/* 		Do not follow re-directions - this causes an exception
		 * since the Apache implementation does not allow for the new URI
		 * to point to a new server, and we expect this.
		 */
		gm.setFollowRedirects(false);
		gm.setPath(msgAccessPath);

		return gm;
	}
	/**
	 * Dumps the contents of a message to the specified outout stream
	 * @param folderRef folder reference where message resides
	 * @param msgRef message reference to be dumped (from the folder)
	 * @param out output stream to write data to
	 * @return 0 if successful
	 * @throws IllegalStateException if internal state errors
	 * @throws IOException if access errors while dumping
	 */
	public int dumpMsgContents (String folderRef, String msgRef, OutputStream out)
		throws IllegalStateException, IOException
	{
		final int	frLen=(null == folderRef) ? 0 : folderRef.length();
		if ((frLen <= 0) || (null == msgRef) || (msgRef.length() <= 0) || (null == out))
			return (-1);

		if (!isLoggedIn())
			throw new IllegalStateException("Client is not authorized");

		// build the message URL
		String	msgURL=folderRef;
		if ('/' == msgURL.charAt(frLen-1))
		{
			if ('/' == msgRef.charAt(0))
				msgURL += msgRef.substring(1);
			else
				msgURL += msgRef;
		}
		else
		{
			if ('/' == msgRef.charAt(0))
				msgURL += msgRef;
			else
				msgURL += "/" + msgRef;
		}

		final HostConfiguration	msgAccessConfig=new HostConfiguration();
		String					msgAccessPath=resolveStringRedirection(msgURL, msgAccessConfig);
		final int				maxRedirects=getMaxRedirects();
		for (int	retryIndex=0; retryIndex < maxRedirects; retryIndex++)
		{
			final GetMethod	gm=getMsgDumpMethod(msgAccessPath);
			final int		stCode=_clnt.executeMethod(msgAccessConfig, gm, _clState);
			try
			{
				if (HttpClientUtils.isOKHttpRspCode(stCode))
				{	
					final Header	clHdr=gm.getResponseHeader("content-length");
					final String	clVal=(null == clHdr) ? null : clHdr.getValue();

					if ((null == clVal) || (clVal.length() <= 0))
						throw new IllegalStateException("No content length to dump from");

					try
					{
						final long	cpyLen=IOCopier.copyStreams(gm.getResponseBodyAsStream(), out, Integer.parseInt(clVal));
						if (cpyLen < 0L)
							return (int) Math.max(cpyLen, Integer.MIN_VALUE);

						return 0;
					}
					catch(NumberFormatException nfe)
					{
						throw new IllegalStateException("Non-numerical content-length to dump (" + clVal + "): " + nfe.getMessage());
					}
				}
				else if (HttpClientUtils.isRedirectHttpRspCode(stCode))
				{
					msgAccessPath = resolveMethodRedirection(gm, msgAccessConfig);
				}
				else	// this point is reached for bad/unexpected response code
					throw new IllegalStateException("Bad/Illegal dumpMsgContents response code: " + stCode + " " + gm.getStatusText());
			}
			finally
			{
				gm.releaseConnection();
			}
		}

		// this point is reached if max retries exhausted
		throw new IllegalStateException("dumpMsgContents un-successful after " + maxRedirects + " retries");
	}
	/**
	 * Dumps the message contents to the output stream
	 * @param fldr folder object in which the message resides
	 * @param msg message object to be dumped
	 * @param out output stream to dump to
	 * @return 0 if successful
	 * @throws IllegalStateException if internal state errors
	 * @throws IOException if access errors while dumping
	 * @see #dumpMsgContents(String folderRef, String msgRef, OutputStream out)
	 */
	public int dumpMsgContents (FolderHandler.FolderInfo fldr, MessagesHandler.MsgInfo msg, OutputStream out)
		throws IllegalStateException, IOException
	{
		return dumpMsgContents((null == fldr) ? null : fldr.getHRef(), (null == msg) ? null : msg.getHRef(), out);
	}
	/**
	 * Dumps the contents of a message to the specified outout stream
	 * @param folderRef folder reference where message resides
	 * @param msgRef message reference to be dumped (from the folder)
	 * @param filePath output file path to dump to
	 * @return 0 if successful
	 * @throws IllegalStateException if internal state errors
	 * @throws IOException if access errors while dumping
	 */
	public int dumpMsgContents (String folderRef, String msgRef, String filePath)
		throws IllegalStateException, IOException
	{
		FileOutputStream	fout=null;
		try
		{
			fout = ((null == filePath) || (filePath.length() <= 0)) ? null : new FileOutputStream(filePath);
			return dumpMsgContents(folderRef, msgRef, fout);
		}
		finally
		{
			FileUtil.closeAll(fout);
		}
	}
	/**
	 * Dumps the message contents to the output stream
	 * @param fldr folder object in which the message resides
	 * @param msg message object to be dumped
	 * @param filePath output file path to dump to
	 * @return 0 if successful
	 * @throws IllegalStateException if internal state errors
	 * @throws IOException if access errors while dumping
	 * @see #dumpMsgContents(String folderRef, String msgRef, OutputStream out)
	 */
	public int dumpMsgContents (FolderHandler.FolderInfo fldr, MessagesHandler.MsgInfo msg, String filePath)
		throws IllegalStateException, IOException
	{
		return dumpMsgContents((null == fldr) ? null : fldr.getHRef(), (null == msg) ? null : msg.getHRef(), filePath);
	}
}
