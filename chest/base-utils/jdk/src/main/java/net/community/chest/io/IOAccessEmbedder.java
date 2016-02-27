package net.community.chest.io;

import java.io.IOException;

/**
 * Copyright 2007 as per GPLv2
 * 
 * Useful interface for all embedder types
 * 
 * @param <C> Type of embedded object
 * @author Lyor G.
 * @since Jul 12, 2007 5:37:03 PM
 */
public interface IOAccessEmbedder<C> {
	/**
	 * @return embedded I/O object 
	 */
	C getEmbeddedAccess ();
	/**
	 * Sets the currently embedded I/O accessor - <B>Caveat emptor:</B> for
	 * some implementations this may lead to undetermined behavior
	 * @param c embedded I/O access
	 * @throws IOException if cannot execute this method
	 */
	void setEmbeddedAccess (C c) throws IOException;
}
