/*
 * 
 */
package net.community.chest.io.output;

import java.io.IOException;

/**
 * <P>Copyright as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Jun 30, 2009 12:40:37 PM
 */
public interface LineLevelAppender {
	/**
	 * @return TRUE if OK to accumulate data in work buffer
	 */
	boolean isWriteEnabled ();
	/**
	 * Called by the implementation once end of line is detected. 
	 * @param sb The {@link StringBuilder} instance containing the line data.
 	 * <B>Note:</B> It is up to the implementor to "reset" the work buffer
 	 * instance after actual write takes place.
	 * @param dLen Number of valid characters in the work buffer
	 * @throws IOException If failed to write the data
	 */
	void writeLineData (final StringBuilder sb, final int dLen) throws IOException;
}
