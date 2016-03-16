package net.community.chest.net.auth;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import net.community.chest.reflect.ClassUtil;

/**
 * <P>Copyright 2007 as per GPLv2</P>
 *
 * <P>This class adds (lazily allocated) cached digest objects to the
 * {@link AuthDigester} behavior</P>
 *
 * @author Lyor G.
 * @since Sep 19, 2007 10:04:38 AM
 */
public class AuthDigesterHelper extends AuthDigester {
    private MessageDigest    _md5Digester /* =null */;
    /*
     * @see net.community.chest.net.auth.AuthDigester#getMD5Digester()
     */
    @Override
    protected MessageDigest getMD5Digester () throws NoSuchAlgorithmException
    {
        if (null == _md5Digester)
            _md5Digester = super.getMD5Digester();

        return _md5Digester;
    }
    /**
     * Empty default constructor
     */
    public AuthDigesterHelper ()
    {
        super();
    }

    private String    _username;
    public String getUsername ()
    {
        return _username;
    }

    public void setUsername (String username)
    {
        _username = username;
    }

    private String    _password;
    public String getPassword ()
    {
        return _password;
    }
    public void setPassword (String password)
    {
        _password = password;
    }
    /**
     * Initialized constructor
     * @param username - may NOT be null/empty
     * @param password - may NOT be null/empty
     * @throws IllegalArgumentException if either is null/empty
     */
    public AuthDigesterHelper (final String username, final String password) throws IllegalArgumentException
    {
        if ((null == (_username=username)) || (username.length() <= 0) ||
            (null == (_password=password)) || (password.length() <= 0))
            throw new IllegalArgumentException(ClassUtil.getConstructorArgumentsExceptionLocation(getClass(), username, password) + " null/empty username/password");
    }
}
