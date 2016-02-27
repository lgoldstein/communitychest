/*
 * 
 */
package net.community.chest.math;

import java.util.List;

/**
 * <P>Copyright GPLv2</P>
 *
 * <P>Used to implement various functions on numbers</P>
 * 
 * @author Lyor G.
 * @since May 27, 2009 9:02:58 AM
 */
public interface NumbersFunction extends FunctionInterface {
	/**
	 * @return TRUE if prefers to accept only floating point, FALSE if only
	 * non-floating point arguments and <code>null</code> if both
	 */
	Boolean getFloatingPointExecutionState ();
	/**
	 * @param args The {@link Number} arguments - most functions will attempt
	 * to convert them to the actual input type (e.g., {@link Long} ==> {@link Float}),
	 * but this is not guaranteed. If the number of supplied arguments is
	 * greater than {@link #getNumArguments()} specification then the
	 * implementation should <U>ignore</U> the extraneous ones
	 * @return The result {@link Number} - <code>null</code> if incomplete
	 * arguments 
	 * @throws IllegalArgumentException If some problem with the arguments
	 * @throws ClassCastException If cannot convert the arguments to the ones
	 * really needed by the implementation
	 */
	Number invoke (List<? extends Number> args) throws IllegalArgumentException, ClassCastException;
	Number invoke (Number ... args) throws IllegalArgumentException, ClassCastException;
}
