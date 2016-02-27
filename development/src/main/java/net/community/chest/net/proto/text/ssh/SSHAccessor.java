/*
 * 
 */
package net.community.chest.net.proto.text.ssh;

import java.io.IOException;

import net.community.chest.net.TextNetConnection;
import net.community.chest.net.proto.text.NetServerWelcomeLine;
import net.community.chest.net.proto.text.TextProtocolNetConnection;

/**
 * <P>Copyright as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Jul 2, 2009 10:14:41 AM
 */
public interface SSHAccessor extends TextProtocolNetConnection, TextNetConnection {
	SSHProtocolHandler getProtocolHandler ();
	// CAVEAT EMPTOR: if changed while session is using it...
	void setProtocolHandler (SSHProtocolHandler hndlr);

	/*
	 *  NOTE !!! all the "connect" methods below call "setProtocolHandler"
	 *  upon SUCCESSFUL connection. The ones inherited from the other
	 *  interfaces (and do not have a handler parameter) call these methods
	 *  with the "getProtocolHandler" result - i.e., the developer can
	 *  call "setProtocolHandler" and then "connect(host, port)" ==> which
	 *  will use the handler set by the "setProtocolHandler" call;
	 */
	void connect (String host, int nPort, NetServerWelcomeLine wl, SSHProtocolHandler hndlr) throws IOException;
	void connect (String host, int nPort, SSHProtocolHandler hndlr) throws IOException;
	void connect (String host, NetServerWelcomeLine wl, SSHProtocolHandler hndlr) throws IOException;
	void connect (String host, SSHProtocolHandler hndlr) throws IOException;
	void login (String username, String password) throws IOException;
}
