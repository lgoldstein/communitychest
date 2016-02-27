/*
 * 
 */
package net.community.chest.net.proto.text.ssh.io;

import java.io.UnsupportedEncodingException;

import net.community.chest.io.output.AutoGrowArrayOutputStream;

/**
 * <P>Copyright as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Jul 5, 2009 11:06:44 AM
 */
public class SSHUtf8StringBuffer extends AutoGrowArrayOutputStream {
	public SSHUtf8StringBuffer (int initialSize, int growSize)
			throws IllegalArgumentException
	{
		super(initialSize, growSize);
	}

	public SSHUtf8StringBuffer (int initialSize)
	{
		this(initialSize, 0);
	}
	public String toUTF8String () throws UnsupportedEncodingException
	{
		return toString("UTF-8");
	}
}
