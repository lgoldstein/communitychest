package net.community.chest.net.auth;

/**
 * <P>Copyright 2007 as per GPLv2</P>
 *
 * <P>Base interface for various authenticators for network protocols</P>
 *
 * @author Lyor G.
 * @since Sep 19, 2007 10:08:07 AM
 */
public interface ProtocolAuthenticator {
    /**
     * @return mechanism for which this authenticator is used
     */
    AuthMechanismEnum getMechanism ();
}
