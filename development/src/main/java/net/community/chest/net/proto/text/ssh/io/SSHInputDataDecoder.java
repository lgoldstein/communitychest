/*
 * 
 */
package net.community.chest.net.proto.text.ssh.io;

import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.Channel;
import java.util.List;

import net.community.chest.io.IOAccessEmbedder;
import net.community.chest.io.OptionallyCloseable;
import net.community.chest.io.encode.InputDataDecoder;
import net.community.chest.io.encode.endian.ByteOrderControlled;

/**
 * <P>Copyright as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Jul 13, 2009 9:02:59 AM
 */
public interface SSHInputDataDecoder
					extends InputDataDecoder,
							ByteOrderControlled,
							IOAccessEmbedder<InputStream>,
							Channel,
							OptionallyCloseable {
	String readASCII () throws IOException;
	List<String> readNamesList () throws IOException;

	byte[] readBlob () throws IOException;
	int readBlob (byte[] buf, int off, int len) throws IOException;
	int readBlob (byte[] buf) throws IOException;
}
