/*
 * 
 */
package net.community.chest.net.jni;

import java.io.IOException;
import java.nio.ByteOrder;

import net.community.chest.io.encode.endian.EndianEncoder;

/**
 * <P>Copyright GPLv2</P>
 *
 * @author Lyor G.
 * @since Jun 23, 2009 9:14:31 AM
 */
public final class NativeSocket {
	private NativeSocket ()
	{
		// no instance
	}

	public static native int socket (int pf, int type, int protocol) throws IOException;
	public static native int bind (int socket, Sockaddr a /* addrLen can be calculated via the "getLength" method of the Sockaddr */) throws IOException;
	public static native int connect(int socket, Sockaddr a /* addrLen can be calculated via the "getLength" method of the Sockaddr */) throws IOException;
	public static native int accept(int socket, Sockaddr a /* int* addrLen is simulated via the "setDataLength" method of the Sockaddr */) throws IOException;

	public static native int listen (int socket, int backlog) throws IOException;
	public static native int close (int socket) throws IOException;

	public static native int send (int socket, byte[] buf, int off, int len, int flags) throws IOException;
	public static final int send (int socket, byte[] buf, int flags) throws IOException
	{
		return send(socket, buf, 0, buf.length, flags);
	}
	
	public static native int recv (int socket, byte[] buf, int off, int len, int flags) throws IOException;
	public static final int recv (int socket, byte[] buf, int flags) throws IOException
	{
		return recv(socket, buf, 0, buf.length, flags);
	}

	// returns number of used bytes
	public static final int htons (int v, byte[] buf, int off)
	{
		return EndianEncoder.toInt16ByteArray(v, ByteOrder.BIG_ENDIAN, buf, off);
	}

	public static final int htons (int v, byte[] buf)
	{
		return htons(v, buf, 0);
	}

	// returns number of used bytes
	public static final int htonl (int v, byte[] buf, int off)
	{
		return EndianEncoder.toInt32ByteArray(v, ByteOrder.BIG_ENDIAN, buf, off);
	}
	
	public static final int htonl (int v, byte[] buf)
	{
		return htonl(v, buf, 0);
	}
	// TODO add setsockopts
	// TODO add getJNIType that returns if Linux or Winsock
}
