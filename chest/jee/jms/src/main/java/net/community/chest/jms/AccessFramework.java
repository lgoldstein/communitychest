/*
 *
 */
package net.community.chest.jms;

import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Map;

import javax.jms.BytesMessage;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.InvalidDestinationException;
import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.ResourceAllocationException;
import javax.jms.Session;
import javax.jms.StreamMessage;
import javax.jms.TextMessage;
import javax.jms.Topic;
import javax.jms.TopicConnection;
import javax.jms.TopicConnectionFactory;
import javax.naming.ConfigurationException;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.InvalidNameException;
import javax.naming.MalformedLinkException;
import javax.naming.NamingException;
import javax.naming.directory.InvalidAttributeIdentifierException;

import net.community.chest.net.proto.text.http.HttpUtils;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * <P>Useful base class for holding the basic objects needed to access a JMS
 * {@link Queue}/{@link Topic} - mainly the {@link Connection},
 * {@link Session} and {@link Destination}</P>
 *
 * @author Lyor G.
 * @since Sep 2, 2008 8:59:06 AM
 */
public class AccessFramework {
    public AccessFramework ()
    {
        super();
    }
    /**
     * Current {@link Session}
     */
    private Session    _sess /* =null */;
    public Session getSession ()
    {
        return _sess;
    }

    public void setSession (Session s)
    {
        _sess = s;
    }
    /**
     * Current {@link Connection}
     */
    private Connection    _conn /* =null */;
    public Connection getConnection ()
    {
        return _conn;
    }

    public void setConnection (Connection c)
    {
        _conn = c;
    }
    /**
     * Current {@link Destination}
     */
    private Destination    _dest /* =null */;
    public Destination getDestination ()
    {
        return _dest;
    }

    public void setDestination (Destination d)
    {
        _dest = d;
    }
    /**
     * Resolves the {@link Connection} (queue/topic) to be used for the
     * JMS according to the class of the {@link Destination} object
     * @param cfc Retrieved {@link ConnectionFactory}
     * @param dst Retrieved {@link Destination} object
     * @param username username to be used - may be null/empty
     * @param passwd password to be used - may be null/empty
     * then a simple un-authenticated connection is returned)
     * @return The {@link Connection} object (null if unable to create one)
     * @throws JMSException if internal error(s) encountered
     */
    protected static final Connection resolveConnection (
                                    final ConnectionFactory    cfc,
                                    final Destination         dst,
                                    final String            username,
                                    final String            passwd) throws JMSException
    {
        if (null == dst)
            return null;

        if (((username != null) && (username.length() > 0))
         || ((passwd != null) && (passwd.length() > 0)))
        {
            if (dst instanceof Queue)
                return (null == cfc) /* should not happen */ ? null : ((QueueConnectionFactory) cfc).createQueueConnection(username, passwd);
            else if (dst instanceof Topic)
                return (null == cfc) /* should not happen */ ? null : ((TopicConnectionFactory) cfc).createTopicConnection(username, passwd);
        }
        else    // non-authenticated connection requested
        {
            if (dst instanceof Queue)
                return (null == cfc) /* should not happen */ ? null : ((QueueConnectionFactory) cfc).createQueueConnection();
            else if (dst instanceof Topic)
                return (null == cfc) /* should not happen */ ? null : ((TopicConnectionFactory) cfc).createTopicConnection();
        }

        // this point is reached only if destination object is neither queue nor topic
        return null;
    }
    /**
     * Creates a {@link Session} for the specified connection according to the
     * destination class (queue/topic)
     * @param conn The {@link Connection} object
     * @param dst Retrieved {@link Destination} object
     * @param transacted use transacted mode
     * @param ackMode acknowledge mode
     * @return session object (null if unable to create one)
     * @throws JMSException if internal error(s)
     * @see QueueConnection#createQueueSession(boolean, int)
     * @see TopicConnection#createTopicSession(boolean, int)
     */
    public static final Session resolveSession (final Connection conn, final Destination dst, final boolean transacted, final int ackMode) throws JMSException
    {
        if (null == dst)
            return null;
        else if (dst instanceof Queue)
            return (null == conn) /* should not happen */ ? null : ((QueueConnection) conn).createQueueSession(transacted, ackMode);
        else if (dst instanceof Topic)
            return (null == conn) /* should not happen */ ? null : ((TopicConnection) conn).createTopicSession(transacted, ackMode);
        else    // should not happen
            return null;
    }

    public void setup (
                final Context    jndiCtx,
                final String     factoryJNDIName,
                final String     destJNDIName,
                final String     username,
                final String     passwd,
                final boolean    transacted,
                final int        ackMode) throws JMSException, NamingException
    {
        if ((getSession() != null) || (getConnection() != null) || (getDestination() != null))
            throw new JMSException("Already initialized");

        final ConnectionFactory    cfc=(ConnectionFactory) jndiCtx.lookup(factoryJNDIName);
        if (null == cfc)
            throw new NamingException("No connection factory found at name=" + factoryJNDIName);

        if (null == (_dest=(Destination) jndiCtx.lookup(destJNDIName)))
            throw new NamingException("No destination found at name=" + destJNDIName);

        /*
         * NOTE !!! it is assumed that the registered destination type
         *         (queue/topic) matches the registered connection factory
         *         type - since all subsequent "resolveXXX" methods use the
         *         "instanceof" for the destination object in order to
         *         determine which set of calls to invoke (queue/topic)
         */
        if (null == (_conn=resolveConnection(cfc, _dest, username, passwd)))
            throw new JMSException("Cannot resolve connection");
        if (null == (_sess=resolveSession(_conn, _dest, transacted, ackMode)))
            throw new JMSException("Cannot resolve session");
    }
    /**
     * Initializes the framework via JNDI
     * @param factoryJNDIName connections factory JNDI name
     * @param destJNDIName destination object JNDI name
     * @param username username for the connection - may be null/empty
     * @param passwd password for the connection - may be null/empty
     * @param transacted use a transacted session (or not)
     * @param ackMode acknowledge mode (see {@link QueueConnection#createQueueSession(boolean, int)}
     * and/or {@link TopicConnection#createTopicSession(boolean, int)}
     * @throws JMSException if internal JMS exception
     * @throws NamingException if JNDI related exception
     */
    public void setup (final String     factoryJNDIName,
                       final String     destJNDIName,
                       final String     username,
                       final String     passwd,
                       final boolean    transacted,
                       final int        ackMode) throws JMSException, NamingException
    {
        Context    jndiCtx=null;
        try
        {
            jndiCtx = new InitialContext();
            setup(jndiCtx, factoryJNDIName, destJNDIName, username, passwd, transacted, ackMode);
        }
        finally
        {
            if (jndiCtx != null)
            {
                try
                {
                    jndiCtx.close();
                }
                catch(NamingException ne)
                {
                    /*    (re-)throw the exception if framework initialized,
                     * otherwise assume some other exception triggered the
                     * "finally" clause
                     */
                    if ((getSession() != null) && (getConnection() != null) && (getDestination() != null))
                        throw ne;
                }
                jndiCtx = null;
            }
        }
    }
    /**
     * Closes the internal objects (if any)
     * @throws JMSException if problem closing some of them
     */
    public void close () throws JMSException
    {
        JMSException    ce=null;
        {
            final Session    s=getSession();
            if (s != null)
            {
                try
                {
                    s.close();
                }
                catch(JMSException je)
                {
                    ce = je;
                }

                setSession(null);
            }
        }

        {
            final Connection    c=getConnection();
            if (c != null)
            {
                try
                {
                    c.close();
                }
                catch(JMSException je)
                {
                    if (ce == null)
                        ce = je;
                }

                setConnection(null);
            }
        }

        if (getDestination() != null)
            setDestination(null);

        if (ce != null)
            throw ce;
    }
    /*
     * -see Session#createMessage()
     */
    public Message createMessage () throws JMSException
    {
        if (null == _sess)
            throw new JMSException("No current session");
        else
            return _sess.createMessage();
    }
    /*
     * -see Session#createObjectMessage()
     */
    public ObjectMessage createObjectMessage () throws JMSException
    {
        if (null == _sess)
            throw new JMSException("No current session");
        else
            return _sess.createObjectMessage();
    }
    /*
     * -see Session#createObjectMessage(java.io.Serializable)
     */
    public ObjectMessage createObjectMessage (Serializable object) throws JMSException
    {
        if (null == _sess)
            throw new JMSException("No current session");
        else
            return _sess.createObjectMessage(object);
    }
    /*
     * -see Session#createStreamMessage()
     */
    public StreamMessage createStreamMessage () throws JMSException
    {
        if (null == _sess)
            throw new JMSException("No current session");
        else
            return _sess.createStreamMessage();
    }
    /*
     * -see Session#createTextMessage()
     */
    public TextMessage createTextMessage () throws JMSException
    {
        if (null == _sess)
            throw new JMSException("No current session");
        else
            return _sess.createTextMessage();
    }
    /*
     * -see Session#createTextMessage(java.lang.String)
     */
    public TextMessage createTextMessage (String text) throws JMSException
    {
        if (null == _sess)
            throw new JMSException("No current session");
        else
            return _sess.createTextMessage(text);
    }
    /*
     * -see Session#createBytesMessage()
     */
    public BytesMessage createBytesMessage () throws JMSException
    {
        if (null == _sess)
            throw new JMSException("No current session");
        else
            return _sess.createBytesMessage();
    }
    /*
     * -see Session#createMapMessage()
     */
    public MapMessage createMapMessage () throws JMSException
    {
        if (null == _sess)
            throw new JMSException("No current session");
        else
            return _sess.createMapMessage();
    }
    /*
     * Just making sure object is closed on finalization
     * @see java.lang.Object#finalize()
     */
    @Override
    protected void finalize () throws Throwable
    {
        try
        {
            close();
        }
        catch(JMSException e)
        {
            // debug breakpoint - ignored
        }

        super.finalize();
    }
    /**
     * Builds the URI string for the specified JMS configuration
     * @param uriScheme scheme to be used in the URI string - may NOT be null/empty
     * @param cfcJNDIName JNDI name under which the connection factory
     * will be registered - if contains the specified JNDI prefix it will be stripped
     * @param username connection username - may be null/empty (if password
     * also null/empty)
     * @param cfcJNDIPrefix JNDI connection factory prefix - if non-empty/null
     * it will be stripped from the JNDI name
     * @param passwd  connection username - may be null/empty (if username
     * also null/empty)
     * @param dstClass destination class - should be {@link javax.jms.Queue}
     * or {@link javax.jms.Topic} - not checked (!)
     * @param params additional URI parameters {@link Map} (may be null/empty)
     * @return URI string
     * @throws URISyntaxException if bad parameters
     */
    public static final String buildURIString (
            final String                        uriScheme,
            final String                         cfcJNDIName,
            final String                        cfcJNDIPrefix,
            final String                         username,
            final String                         passwd,
            final Class<? extends Destination>    dstClass,
            final Map<String,String>            params) throws URISyntaxException
    {
        final String    dstClassName=(null == dstClass) ? null : dstClass.getName();
        final int        cfcLen=(null == cfcJNDIName) ? 0 : cfcJNDIName.length(),
                        dstLen=(null == dstClassName) ? 0 : dstClassName.length(),
                        usrLen=(null == username) ? 0 : username.length(),
                        passLen=(null == passwd) ? 0 : passwd.length();
        // MUST have a JNDI name AND a class
        if ((cfcLen <= 0) || (dstLen <= 0)
         || (null == uriScheme) || (uriScheme.length() <= 0)
            // either BOTH username & password or none
         || ((usrLen > 0) && (passLen <= 0))
         || ((usrLen <= 0) && (passLen > 0)))
            throw new URISyntaxException(uriScheme + "://" + cfcJNDIPrefix + "/" + cfcJNDIName + "[" + username + ":" + passwd + "]@" + ((null == dstClass) ? null : dstClass.getSimpleName()), "incomplete parameterers");

        final String    cfcURIHost;
        if ((cfcJNDIPrefix != null) && (cfcJNDIPrefix.length() > 0) && cfcJNDIName.startsWith(cfcJNDIPrefix))
        {
            if (cfcLen <= cfcJNDIPrefix.length())
                throw new URISyntaxException(uriScheme + "://" + cfcJNDIPrefix + "/" + cfcJNDIName + "[" + username + ":" + passwd + "]@" + ((null == dstClass) ? null : dstClass.getSimpleName()), "JNDI prefix too short");

            cfcURIHost = cfcJNDIName.substring(cfcJNDIPrefix.length());
            if ((null == cfcURIHost) || (cfcURIHost.length() <= 0))
                throw new URISyntaxException(uriScheme + "://" + cfcJNDIPrefix + "/" + cfcJNDIName + "[" + username + ":" + passwd + "]@" + ((null == dstClass) ? null : dstClass.getSimpleName()), "no URI host");
        }
        else
            cfcURIHost = cfcJNDIName;

        final Collection<? extends Map.Entry<String,String>>    paramsSet=
            ((null == params) || (params.size() <= 0)) ? null : params.entrySet();
        final int                                                numParams=
            (null == paramsSet) ? 0 : paramsSet.size();
        final StringBuilder                                        sb=
            new StringBuilder(uriScheme.length() + 4 + usrLen + 4 + passLen + 4 + cfcLen + 4 + dstLen + 2 + Math.max(numParams, 0) * 64);
        // append protocol and initial separator
        sb.append(uriScheme).append("://");

        // append username + password (if any)
        if ((usrLen > 0) && (passLen > 0))
            sb.append(username)
               .append(':')
               .append(passwd)
               .append('@')
               ;

        sb.append(cfcURIHost)
          .append('/')
          .append(dstClassName)
          ;

        if (numParams > 0)
        {
            int    pIndex=0;
            for (final Map.Entry<String,String> e : paramsSet)
            {
                if (null == e)    // should not happen
                    continue;

                final String    key=e.getKey(), val=e.getValue();
                if ((null == key) || (key.length() <= 0)
                 || (null == val) || (val.length() <= 0))
                    throw new URISyntaxException(uriScheme + "://" + cfcJNDIPrefix + "/" + cfcJNDIName + "[" + username + ":" + passwd + "]@" + ((null == dstClass) ? null : dstClass.getSimpleName()), "bad parameter(" + key + ")/value(" + val + ")");

                sb.append((pIndex > 0) ? '&' : '?')
                  .append(key)
                  .append('=')
                  .append(val)
                  ;

                pIndex++;
            }
        }

        return sb.toString();
    }
    /**
     * Default JNDI namespace prefix assumed automatically for the connection factory
     */
    public static final String DEFAULT_FACTORY_JNDI_NAME_PREFIX="java:/";
    // assumes DEFAULT_FACTORY_JNDI_NAME_PREFIX as JNDI prefix
    public static final String buildURIString (
            final String                        uriScheme,
            final String                         cfcJNDIName,
            final String                         username,
            final String                         passwd,
            final Class<? extends Destination>    dstClass,
            final Map<String,String>            params) throws URISyntaxException
    {
        return buildURIString(uriScheme, cfcJNDIName, DEFAULT_FACTORY_JNDI_NAME_PREFIX, username, passwd, dstClass, params);
    }

    public static final String DEFAULT_JMS_SCHEME="jms";
    // also assumes DEFAULT_JMS_SCHEME as scheme
    public static final String buildURIString (
            final String                         cfcJNDIName,
            final String                         username,
            final String                         passwd,
            final Class<? extends Destination>    dstClass,
            final Map<String,String>            params) throws URISyntaxException
    {
        return buildURIString(DEFAULT_JMS_SCHEME, cfcJNDIName, username, passwd, dstClass, params);
    }
    // inefficient so use sparingly
    protected static final String getURIExcPrefix (String location, String name, URI qURI)
    {
        return location + "(" + name + ")[URI=" + String.valueOf(qURI) + "]";
    }
    /**
     * Retrieves the specified destination object (queue/topic) according
     * to the URI specification. Assumes queues are registered under the
     * "queue/ZZZ" JNDI name and topics under "topic/ZZZ".
     * @param ctx JNDI context to be used to look-up the destination object
     * @param name name of destination object to be retrieved
     * @param qURI URI with the specification (according to format specified
     * in the {@link #setup(String, URI)} method
     * @return destination object (Queue/Topic) - null if unable to retrieve
     * @throws NamingException if internal errors
     */
    public static final Destination resolveDestination (Context ctx, String name, URI qURI)
        throws NamingException
    {
        final String    qPath=(null == qURI) ? null : qURI.getPath();
        if ((null == qPath) || (qPath.length() <= 0))
            throw new InvalidNameException(getURIExcPrefix("resolveDestination", name, qURI) + " missing JMS destination class name");

        final String                        dstClassName=
            ('/' == qPath.charAt(0)) ? qPath.substring(1) : qPath, dstLookupName;
        final Class<? extends Destination>    dstClass;
        if (Queue.class.getName().equalsIgnoreCase(dstClassName))
        {
            dstClass = Queue.class;
            dstLookupName = "queue/" + name;
        }
        else if (Topic.class.getName().equalsIgnoreCase(dstClassName))
        {
            dstClass = Topic.class;
            dstLookupName = "topic/" + name;
        }
        else
            throw new MalformedLinkException(getURIExcPrefix("resolveDestination", name, qURI) + " unknown destination class: " + dstClassName);

        final Destination    dst=(null == ctx) ? null : (Destination) ctx.lookup(dstLookupName);
        if (null == dst)
            throw new ConfigurationException(getURIExcPrefix("resolveDestination", name, qURI) + " no destination registered under " + dstLookupName);

        /*
         * NOTE !!! theoretically, it is possible to register a topic
         *         under "queue/ZZZ" (and vice-versa) - however unlikely
         *         it may be, we re-check it
         */
        if (!dstClass.isAssignableFrom(dst.getClass()))
            throw new InvalidAttributeIdentifierException(getURIExcPrefix("resolveDestination", name, qURI) + " registered destination class mismatch: expected=" + dstClass.getName() + " - got=" + dst.getClass().getName());

        return dst;
    }
    // inefficient so use sparingly
    protected static final String getMissingObjectExc (String location, String name, URI qURI, Class<?> missingClass)
    {
        return getURIExcPrefix(location, name, qURI) + " bad/missing " + missingClass.getName() + " object";
    }
    /**
     * @param dst Destination whose name is required
     * @return destination name (according to type: {@link Queue} or
     * {@link Topic}) - <I>null</I>/empty if no destination
     * @throws JMSException internal JMS error
     */
    public static final String resolveDestinationName (final Destination dst) throws JMSException
    {
        if (null == dst)
            return null;
        else if (dst instanceof Queue)
            return ((Queue) dst).getQueueName();
        else if (dst instanceof Topic)
            return ((Topic) dst).getTopicName();
        else    // should not happen
            throw new InvalidDestinationException("Unexpected destination type: " + dst.getClass().getName());
    }
    /**
     * Resolve the connection (queue/topic) to be used for the JMS
     * @param cfc retrieved connection factory (may NOT be <I>null</I>)
     * @param dst retrieved destination object (may NOT be <I>null</I>)
     * @param qURI URI that may contain username/password to be used (if not,
     * then a simple un-authenticated connection is returned) - may NOT be <I>null</I>
     * @return connection object (<I>null</I> if unable to create one)
     * @throws JMSException if unable to access the JMS
     */
    public static final Connection resolveConnection (ConnectionFactory cfc, Destination dst, URI qURI)
        throws JMSException
    {
        final String    dstName=resolveDestinationName(dst);
        if ((null == dstName) || (dstName.length() <= 0))
            throw new InvalidDestinationException(getMissingObjectExc("resolveConnection", dstName, qURI, Destination.class));

        if (null == qURI)
            throw new JMSException(getMissingObjectExc("resolveConnection", dstName, qURI, URI.class));
        if (null == cfc)
            throw new ResourceAllocationException(getMissingObjectExc("resolveConnection", dstName, qURI, ConnectionFactory.class));

        final Map.Entry<String,String>    uInfo=HttpUtils.getUserInfo(qURI.getUserInfo());
        final String                    username=(null == uInfo) ? null : uInfo.getKey(),
                                        passwd=(null == uInfo) ? null : uInfo.getValue();
        /*
         * NOTE !!! theoretically, it is possible to register in the JNDI a
         *         connection factory that does not match the destination class.
         *         We assume this is NOT the case here
         */
        if (((username != null) && (username.length() > 0))
         || ((passwd != null) && (passwd.length() > 0)))
        {
            if (dst instanceof Queue)
                return ((QueueConnectionFactory) cfc).createQueueConnection(username, passwd);
            else if (dst instanceof Topic)
                return ((TopicConnectionFactory) cfc).createTopicConnection(username, passwd);
            else    // should not happen
                throw new javax.jms.IllegalStateException(getURIExcPrefix("resolveConnection", dstName, qURI) + " unexpected authenticated destination class type: " + dst.getClass().getName());
        }
        else    // non-authenticated connection requested
        {
            if (dst instanceof Queue)
                return ((QueueConnectionFactory) cfc).createQueueConnection();
            else if (dst instanceof Topic)
                return ((TopicConnectionFactory) cfc).createTopicConnection();
            else
                throw new javax.jms.IllegalStateException(getURIExcPrefix("resolveConnection", dstName, qURI) + " unexpected un-authenticated destination class type: " + dst.getClass().getName());
        }
    }
    /**
     * URI parameter used to indicate if session is transacted - allowed
     * values are "true"/"false" - case-insensitive (if missing then FALSE
     * is assumed)
     */
    public static final String TRANSACTED_URI_PARAM_NAME="transacted";
    /**
     * Default transaction mode
     */
    public static final Boolean    DEFAULT_TRANSACTION_MODE=Boolean.FALSE;
    /**
     * Seeks for the {@link #TRANSACTED_URI_PARAM_NAME} parameter
     * @param qParams URI parameters - if <I>null</I> or attribute not found
     * in map, then returns {@link #DEFAULT_TRANSACTION_MODE}
     * @return acknowledge mode
     */
    public static final Boolean resolveTransactionMode (final Map<String,String> qParams)
    {
        final String    modeVal=(null == qParams) ? null : qParams.get(TRANSACTED_URI_PARAM_NAME);
        if ((null == modeVal) || (modeVal.length() <= 0))
            return DEFAULT_TRANSACTION_MODE;

        return Boolean.valueOf(modeVal);
    }
    /**
     * Session acknowledge mode - see {@link SessionAckMode} for allowed
     * value (case-insensitive) - if missing then {@link Session#AUTO_ACKNOWLEDGE} assumed
     */
    public static final String ACKMODE_URI_PARAM_NAME="ackmode";
    /**
     * Default acknowledge mode
     */
    public static final int DEFAULT_ACKMODE_VALUE=Session.AUTO_ACKNOWLEDGE;
    /**
     * Seeks for the {@link #ACKMODE_URI_PARAM_NAME} parameter
     * @param qParams URI parameters - if <I>null</I> or attribute not found
     * in map, then returns {@link #DEFAULT_ACKMODE_VALUE}
     * @return acknowledge mode - <I>null</I> if bad/illegal value
     * @throws IllegalArgumentException if bad/unknown mode specified
     * @throws IllegalStateException if {@link Session#SESSION_TRANSACTED}
     * specified - use {@link #TRANSACTED_URI_PARAM_NAME} for this purpose
     */
    public static final Integer resolveAckMode (final Map<String,String> qParams)
        throws IllegalArgumentException, IllegalStateException
    {
        final String    modeVal=(null == qParams) ? null : qParams.get(ACKMODE_URI_PARAM_NAME);
        if ((null == modeVal) || (modeVal.length() <= 0))
            return Integer.valueOf(DEFAULT_ACKMODE_VALUE);

        final SessionAckMode    m=SessionAckMode.fromString(modeVal);
        if (null == m)
            throw new IllegalArgumentException("resolveAckMode(" + modeVal + ") unknown mode");
        if (SessionAckMode.TRANSACTED.equals(m))
            throw new IllegalStateException("resolveAckMode(" + modeVal + ") unknown mode");

        return Integer.valueOf(m.getMode());
    }
    /**
     * Creates a session for the specified connection and destination
     * @param qURI URI that was used to create the connection - it
     * may contain further parameters (e.g., transacted, ack-mode) - may
     * NOT be <I>null</I>
     * @param conn connection object - may NOT be <I>null</I>
     * @param dst retrieved destination object - may NOT be <I>null</I>
     * @return session object (null if unable to create one)
     * @throws JMSException if unable to access the JMS
     */
    public static final Session resolveSession (URI qURI, Connection conn, Destination dst)
        throws JMSException
    {
        final String    dstName=resolveDestinationName(dst);
        if ((null == dstName) || (dstName.length() <= 0))
            throw new InvalidDestinationException(getMissingObjectExc("resolveSession", dstName, qURI, Destination.class));
        if (null == qURI)
            throw new JMSException(getMissingObjectExc("resolveSession", dstName, qURI, URI.class));
        if (null == conn)
            throw new JMSException(getMissingObjectExc("resolveSession", dstName, qURI, Connection.class));

        final Map<String,String>    params=HttpUtils.getQueryStringParametersMap(qURI.getQuery());
        final Boolean                transMode=resolveTransactionMode(params);
        if (null == transMode)
            throw new JMSException(getURIExcPrefix("resolveSession", dstName, qURI) + " cannot resolve transaction mode");

        final Integer    ackMode=resolveAckMode(params);
        if (null == ackMode)
            throw new JMSException(getURIExcPrefix("resolveSession", dstName, qURI) + " cannot resolve ACK mode");

        /*
         * NOTE !!! theoretically, this method might be called with wrong
         *         connection object - i.e., one that does not match the type
         *         of destination (queue/topic) - we assume this is not the case
         */
        if (dst instanceof Queue)
            return ((QueueConnection) conn).createQueueSession(transMode.booleanValue(), ackMode.intValue());
        else if (dst instanceof Topic)
            return ((TopicConnection) conn).createTopicSession(transMode.booleanValue(), ackMode.intValue());
        else    // should not happen
            throw new javax.jms.IllegalStateException(getURIExcPrefix("resolveSession", dstName, qURI) + " unexpected destination class type: " + dst.getClass().getName());
    }
    /**
     * <P>Initializes the internal values if not already initialized - URI syntax:</P?</BR>
     *         <UL>
     *             <LI>
     *             host - JNDI name for the connection factory (assumed to
     *             reside under "java:/" as its prefix.
     *             </LI>
     *
     *             <LI>
     *             username/password - if found, then used to create a connection
     *             </LI>
     *
     *             <LI>
     *             path - assumed to contain the destination type: javax.jms.Queue or javax.jms.Topic
     *             </LI>
     *         </UL>
     * @param cfcJNDIPrefix JNDI prefix used for connection factory registration (e.g., "java:/")
     * If <I>null</I>/empty, then using the URI "host" part as is...
     * @param name name of queue/topic to be initialized
     * @param qURI URI with required information:</BR>
     * <P>Example: "jms://foo:bar@FooBarQFactory/javax.jms.Queue?transacted=false&ackmode=AUTO"</P>
     * @param ctx JNDI context to access the JMS registered object(s)
     * @throws NamingException if unable to access the JNDI
     * @throws JMSException if unable to access the JMS
     */
    public void setup (String cfcJNDIPrefix, String name, URI qURI, Context ctx)
        throws NamingException, JMSException
    {
        final String    facName;
        if ((cfcJNDIPrefix != null) && (cfcJNDIPrefix.length() > 0))
            facName = cfcJNDIPrefix + qURI.getHost();
        else
            facName = qURI.getHost();

        final ConnectionFactory    cfc=(ConnectionFactory) ctx.lookup(facName);
        if (null == cfc)    // should not happen
            throw new ResourceAllocationException(getURIExcPrefix("setup", name, qURI) + " no object registered under JNDI=" + facName);

        {
            if (getDestination() != null)
                throw new javax.jms.IllegalStateException(getURIExcPrefix("setup", name, qURI) + " already have destination set");
            setDestination(resolveDestination(ctx, name, qURI));
            if (null == getDestination())
                throw new javax.jms.IllegalStateException(getURIExcPrefix("setup", name, qURI) + " cannot retrieve destination");
        }

        {
            if (getConnection() != null)
                throw new javax.jms.IllegalStateException(getURIExcPrefix("setup", name, qURI) + " already have connection set");
            setConnection(resolveConnection(cfc, getDestination(), qURI));
            if (null == getConnection())
                throw new javax.jms.IllegalStateException(getURIExcPrefix("setup", name, qURI) + " cannot retrieve connection");
        }

        {
            if (getSession() != null)
                throw new javax.jms.IllegalStateException(getURIExcPrefix("setup", name, qURI) + " already have session set");
            setSession(resolveSession(qURI, getConnection(), getDestination()));
            if (null == getSession())
                throw new javax.jms.IllegalStateException(getURIExcPrefix("setup", name, qURI) + " cannot retrieve session");
        }
    }
    // @see setup(prefix,name,URI,context)
    public void setup (String cfcJNDIPrefix, String name, URI qURI)
        throws NamingException, JMSException
    {
        Context    jndiCtx=null;
        try
        {
            jndiCtx = new InitialContext();
            setup(cfcJNDIPrefix, name, qURI, jndiCtx);
        }
        finally
        {
            if (jndiCtx != null)
            {
                try
                {
                    jndiCtx.close();
                }
                catch(NamingException ne)
                {
                    // ignored
                }
                jndiCtx = null;
            }
        }
    }
    // @see setup(prefix,name,URI)
    public void setup (String name, URI qURI)
        throws NamingException, JMSException
    {
        setup(DEFAULT_FACTORY_JNDI_NAME_PREFIX, name, qURI);
    }
}
