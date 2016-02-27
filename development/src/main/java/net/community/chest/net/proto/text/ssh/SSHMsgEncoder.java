/*
 * 
 */
package net.community.chest.net.proto.text.ssh;

import net.community.chest.io.encode.ElementEncoder;

/**
 * <P>Copyright as per GPLv2</P>
 *
 * <P>Note:</P> the read/write implementation inherited from the
 * {@link ElementEncoder} interface assume code has already been read/written
 * <U>prior</P> to calling the read/write method(s)</P>
 * 
 * @param <V> Type of message being encoded
 * @author Lyor G.
 * @since Jul 2, 2009 9:14:03 AM
 */
public interface SSHMsgEncoder<V> extends SSHDataObjectEncoder<V> {
	SSHMsgCode getMsgCode ();
	void setMsgCode (SSHMsgCode c);
}
