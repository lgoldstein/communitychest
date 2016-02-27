/*
 * 
 */
package net.community.chest.math.compare;

import java.util.Comparator;

/**
 * <P>Copyright GPLv2</P>
 *
 * @author Lyor G.
 * @since Apr 13, 2009 11:01:38 AM
 */
public interface ComparisonExecutor {
	/**
	 * @param nRes A {@link Comparator#compare(Object, Object)} or
	 * {@link Comparable#compareTo(Object)} result
	 * @return The operator's result - or <code>null</code>
	 * if operation not valid
	 */
	Boolean getComparisonResult (final int nRes);
	/**
	 * "Executes" the comparison operator on {@link Comparable} objects
	 * @param <V> Type of {@link Comparable} object being compared
	 * @param o1 1st {@link Comparable} object
	 * @param o2 2nd {@link Comparable} object
	 * @return The operator's result - or <code>null</code>
	 * if operation not valid
	 */
	<V extends Comparable<V>> Boolean invoke (V o1, V o2);
	/**
	 * "Executes" the comparison operator on the result of a {@link Comparator}
	 * invocation
	 * @param <V> Type of object being compared
	 * @param c The {@link Comparator} to use
	 * @param o1 1st {@link Comparable} object
	 * @param o2 2nd {@link Comparable} object
	 * @return The operator's result - or <code>null</code>
	 * if operation not valid or no {@link Comparator} instance provided.
	 */
	<V> Boolean invoke (Comparator<? super V> c, V o1, V o2);

}
