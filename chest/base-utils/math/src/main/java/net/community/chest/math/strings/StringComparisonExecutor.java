/*
 * 
 */
package net.community.chest.math.strings;

import net.community.chest.math.FunctionInterface;

/**
 * <P>Copyright GPLv2</P>
 *
 * @author Lyor G.
 * @since May 27, 2009 2:19:35 PM
 */
public interface StringComparisonExecutor extends FunctionInterface {
	Boolean invoke (String s1, String s2, boolean caseSensitive);
}
