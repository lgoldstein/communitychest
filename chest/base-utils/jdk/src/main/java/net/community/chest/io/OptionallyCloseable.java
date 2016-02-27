package net.community.chest.io;

import java.io.Closeable;

/**
 * Copyright 2007 as per GPLv2
 * 
 * Represents an object that might encapsulate some other {@link Closeable}
 * instance (e.g. {@link java.io.FilterOutputStream}, and we might not want to
 * close the "real" underlying instance when the {@link Closeable#close()}
 * method is called on the encapsulator
 * 
 * @author Lyor G.
 * @since Jun 13, 2007 4:08:15 PM
 */
public interface OptionallyCloseable extends Closeable {
	/**
	 * @return TRUE if calling the {@link #close()} method will also close
	 * the encapsulated instance
	 */
	boolean isRealClosure ();
	/**
	 * @return TRUE if the real closure state may be changed before calling
	 * {@link #close()} via {@link #setRealClosure(boolean)} without risking
	 * an {@link UnsupportedOperationException}
	 * @see #setRealClosure(boolean)
	 */
	boolean isMutableRealClosure ();
	/**
	 * @param enabled TRUE=close encapsulated instance on call to
	 * {@link #close()}. <B>Note:</B> calling this method <U>after</U>
	 * calling {@link #close()} has no effect
	 * @throws UnsupportedOperationException if not allowed to change the real
	 * closure behavior
	 * @see #isMutableRealClosure()
	 */
	void setRealClosure (boolean enabled) throws UnsupportedOperationException;
}
