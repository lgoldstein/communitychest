/*
 * 
 */
package net.community.chest.net.proto.text.ssh.session;

import java.io.IOException;
import java.net.Socket;
import java.nio.channels.SocketChannel;

/**
 * <P>Copyright as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Jul 12, 2009 12:57:20 PM
 */
public class SSHSession extends AbstractSSHSession {
	public SSHSession ()
	{
		super();
	}

	public SSHSession (int readBufSizeVal, int writeBufSizeVal)
	{
		super(readBufSizeVal, writeBufSizeVal);
	}

	public SSHSession (Socket sock, int readBufSizeVal, int writeBufSizeVal)
			throws IOException
	{
		super(sock, readBufSizeVal, writeBufSizeVal);
	}

	public SSHSession (Socket sock) throws IOException
	{
		super(sock);
	}

	public SSHSession (SocketChannel sock, int readBufSizeVal, int writeBufSizeVal)
		throws IOException
	{
		super(sock, readBufSizeVal, writeBufSizeVal);
	}

	public SSHSession (SocketChannel sock) throws IOException
	{
		super(sock);
	}
}
