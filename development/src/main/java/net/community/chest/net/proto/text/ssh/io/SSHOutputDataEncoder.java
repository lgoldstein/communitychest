/*
 * 
 */
package net.community.chest.net.proto.text.ssh.io;

import java.io.Flushable;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.channels.Channel;
import java.util.Collection;

import net.community.chest.io.IOAccessEmbedder;
import net.community.chest.io.OptionallyCloseable;
import net.community.chest.io.encode.OutputDataEncoder;
import net.community.chest.io.encode.endian.ByteOrderControlled;

/**
 * <P>Copyright as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Jul 13, 2009 12:48:47 PM
 */
public interface SSHOutputDataEncoder extends OutputDataEncoder,
		ByteOrderControlled, IOAccessEmbedder<OutputStream>, Channel,
		OptionallyCloseable, Flushable {

	void writeASCII (String s) throws IOException;

	// use the "toString" call
	void writeNamesList (Collection<?> nl) throws IOException;
	void writeNamesList (Object ... nl) throws IOException;
	
	void writeBlob (byte[] buf, int off, int len) throws IOException;
	void writeBlob (byte[] buf) throws IOException;
}
