package net.community.chest.mail.message;

import net.community.chest.io.OptionallyCloseable;

/**
 * <P>Copyright 2007 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Sep 19, 2007 9:15:16 AM
 */
public interface EOMDataStreamHunter extends OptionallyCloseable {
	/**
	 * @return TRUE if EOM detected - should be called only
	 * after finishing writing entire data (although calling
	 * it in-between should return FALSE...)
	 */
	boolean isEOMDetected ();
	// @see #isEOMDetected() - CAVEAT EMPTOR !!!
	void setEOMDetected (boolean haveEOM);
	/**
	 * @return if TRUE then EOM is "echoed" to the "real" stream after its
	 * detection. Otherwise, it is omitted from the real stream.
	 */
	boolean isEchoEOM ();
	void setEchoEOM (boolean echoEOM);
}
