package net.community.chest.net;

/**
 * Copyright 2007 as per GPLv2
 * 
 * Useful interface for embedders of {@link NetConnection}-s (e.g., I/O streams
 * reader/writer(s), etc.)
 * 
 * @param <C> The type of embedded {@link NetConnection}
 * @author Lyor G.
 * @since Jul 4, 2007 8:13:24 AM
 */
public interface NetConnectionEmbedder<C extends NetConnection> {
	/**
	 * @return currently embedded connection - may be null if none set or
	 * the embedder has been closed (e.g., some I/O stream reader/writer)
	 */
	C getConnection ();
	void setConnection (C conn);
}
