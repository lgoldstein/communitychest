/*
 * 
 */
package net.community.chest.io;

import java.net.URI;

import net.community.chest.reflect.ValueStringConstructor;

/**
 * <P>Copyright as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Jul 19, 2009 8:56:10 AM
 */
public class URIStringInstantiator extends ValueStringConstructor<URI> {
	public URIStringInstantiator ()
	{
		super(URI.class);
	}

	public static final URIStringInstantiator	DEFAULT=new URIStringInstantiator();
}
