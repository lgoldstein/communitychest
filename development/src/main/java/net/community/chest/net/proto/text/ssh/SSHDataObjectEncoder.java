/*
 * 
 */
package net.community.chest.net.proto.text.ssh;

import java.io.IOException;

import net.community.chest.io.encode.ElementEncoder;
import net.community.chest.net.proto.text.ssh.io.SSHInputDataDecoder;
import net.community.chest.net.proto.text.ssh.io.SSHOutputDataEncoder;

/**
 * <P>Copyright as per GPLv2</P>
 *
 * @param <V> Type of object being encoded
 * @author Lyor G.
 * @since Jul 13, 2009 9:57:26 AM
 */
public interface SSHDataObjectEncoder<V> extends ElementEncoder<V> {
	V decode (SSHInputDataDecoder in) throws IOException;
	void encode (SSHOutputDataEncoder out) throws IOException;
}
