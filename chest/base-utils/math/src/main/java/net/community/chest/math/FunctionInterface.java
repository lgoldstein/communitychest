/*
 * 
 */
package net.community.chest.math;

/**
 * <P>Copyright GPLv2</P>
 *
 * @author Lyor G.
 * @since May 27, 2009 2:24:01 PM
 */
public interface FunctionInterface {
	/**
	 * @return A function mnemonic
	 */
	String getName ();
	/**
	 * @return A "shortcut" symbols - if none available then use the name
	 */
	String getSymbol ();
	/**
	 * @return Number of expected arguments - if negative then "unlimited"
	 * (at least in theory) (Note: zero number of arguments means some kind
	 * of "constant" - e.g., {@link Math#random()})
	 */
	int getNumArguments ();
}
