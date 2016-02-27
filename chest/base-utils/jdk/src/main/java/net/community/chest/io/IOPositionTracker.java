package net.community.chest.io;

/**
 * Copyright 2007 as per GPLv2
 * 
 * Implemented by various I/O classes that can also tell how many bytes
 * and/or characters were written so far
 * @author Lyor G.
 * @since Jul 12, 2007 5:17:46 PM
 */
public interface IOPositionTracker {
	/**
	 * @return current position in I/O read/write operation. <B>Note:</B> the
	 * returned value is <U>persistent</U> - i.e., can be queried even after
	 * the I/O data source/destination is closed
	 */
	long getPos ();
}
