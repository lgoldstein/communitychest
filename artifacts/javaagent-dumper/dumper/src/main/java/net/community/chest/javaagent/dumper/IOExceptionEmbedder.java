/*
 * 
 */
package net.community.chest.javaagent.dumper;

import java.io.IOException;

/**
 * <P>Copyright as per GPLv2</P>
 * @author Lyor G.
 * @since Aug 2, 2011 4:14:31 PM
 */
class IOExceptionEmbedder extends RuntimeException {
	private static final long serialVersionUID = 2962906858845436774L;

	public IOException getException ()
	{
		return (IOException) getCause();
	}

	IOExceptionEmbedder (IOException e)
	{
		super(e);
	}
}
