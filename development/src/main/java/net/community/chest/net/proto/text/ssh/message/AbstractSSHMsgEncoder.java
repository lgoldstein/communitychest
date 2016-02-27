/*
 * 
 */
package net.community.chest.net.proto.text.ssh.message;

import java.io.Serializable;

import net.community.chest.lang.PubliclyCloneable;
import net.community.chest.net.proto.text.ssh.SSHMsgCode;
import net.community.chest.net.proto.text.ssh.SSHMsgEncoder;
import net.community.chest.reflect.ClassUtil;
import net.community.chest.util.compare.AbstractComparator;

/**
 * <P>Copyright as per GPLv2</P>
 *
 * @param <V> Type of message being encoded
 * @author Lyor G.
 * @since Jul 2, 2009 9:17:29 AM
 */
public abstract class AbstractSSHMsgEncoder<V> implements SSHMsgEncoder<V>, PubliclyCloneable<V>, Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -5995386868887244340L;
	private final SSHMsgCode	_msgCode;
	/*
	 * @see net.community.chest.net.proto.text.ssh.SSHMsgEncoder#getMsgCode()
	 */
	@Override
	public final SSHMsgCode getMsgCode ()
	{
		return _msgCode;
	}
	/*
	 * @see net.community.chest.net.proto.text.ssh.SSHMsgEncoder#setMsgCode(net.community.chest.net.proto.text.ssh.SSHMsgCode)
	 */
	@Override
	public final void setMsgCode (SSHMsgCode c)
	{
		if (!_msgCode.equals(c))
			throw new IllegalStateException("setMsgCode(" + c + ") not same as default=" + _msgCode);
	}

	protected AbstractSSHMsgEncoder (SSHMsgCode c)
	{
		if (null == (_msgCode=c))
			throw new IllegalStateException("No msg code provided");
	}
	/*
	 * @see java.lang.Object#clone()
	 */
	@Override
	@SuppressWarnings("unchecked")
	public V clone () throws CloneNotSupportedException
	{
		return (V) getClass().cast(super.clone());
	}
	/*
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals (Object obj)
	{
		if (!(obj instanceof AbstractSSHMsgEncoder<?>))
			return false;
		if (this == obj)
			return true;

		final AbstractSSHMsgEncoder<?>	o=(AbstractSSHMsgEncoder<?>) obj;
		return AbstractComparator.compareObjects(getMsgCode(), o.getMsgCode());
	}
	/*
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode ()
	{
		return ClassUtil.getObjectHashCode(getMsgCode());
	}
	/*
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString ()
	{
		final SSHMsgCode	c=getMsgCode();
		return (null == c) ? "" : c.getMnemonic();
	}

	protected StringBuilder appendFullDescription (StringBuilder sb)
	{
		if (null == sb)
			return null;

		final SSHMsgCode	c=getMsgCode();
		final String		m=(null == c) ? null : c.getMnemonic();
		return sb.append(m);
	}

	public String toFullDescription ()
	{
		return appendFullDescription(new StringBuilder())
			.toString()
			;
	}
}
