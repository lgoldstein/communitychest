/*
 * 
 */
package net.community.chest.net.proto.text.ssh.message.channel.request;

import java.io.Serializable;

import net.community.chest.net.proto.text.ssh.SSHDataObjectEncoder;

/**
 * <P>Copyright as per GPLv2</P>
 *
 * @param <V> Type of specific data 
 * @author Lyor G.
 * @since Jul 12, 2009 11:54:12 AM
 */
public abstract class AbstractChannelRequestSpecificTypeData<V>
			implements SSHDataObjectEncoder<V>, Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 4629093342189067771L;
	private final String	_requestType;
	public final String getRequestType ()
	{
		return _requestType;
	}

	protected AbstractChannelRequestSpecificTypeData (String reqType) throws IllegalArgumentException
	{
		if ((null == (_requestType=reqType)) || (reqType.length() <= 0))
			throw new IllegalArgumentException("No request type provided");
	}
}
