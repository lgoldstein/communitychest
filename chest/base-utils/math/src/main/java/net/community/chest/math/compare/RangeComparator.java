/*
 * 
 */
package net.community.chest.math.compare;

import java.util.Comparator;

/**
 * <P>Copyright GPLv2</P>
 *
 * @author Lyor G.
 * @since Apr 13, 2009 2:00:33 PM
 */
public interface RangeComparator {
	/**
	 * "Executes" the range comparison operator on {@link Comparable} objects
	 * @param <V> Type of {@link Comparable} object being compared
	 * @param startValue "Bottom" value (inclusive) - if <code>null</code>
	 * then no such restriction (open-ended range)
	 * @param endValue "Top" value (inclusive) - if <code>null</code> then
	 * no such restriction (open-ended range)
	 * @param value Value to check if in specified range
	 * @return The operator's result - or <code>null</code> if operation
	 * not valid
	 */
	<V extends Comparable<V>> Boolean invoke (V value, V startValue, V endValue);
	/**
	 * "Executes" the range comparison operator on objects using a {@link Comparator}
	 * @param <V> Type of {@link Comparable} object being compared
	 * @param c The {@link Comparator} instance to use
	 * @param startValue "Bottom" value (inclusive) - if <code>null</code>
	 * then no such restriction (open-ended range)
	 * @param endValue "Top" value (inclusive) - if <code>null</code> then
	 * no such restriction (open-ended range)
	 * @param value Value to check if in specified range
	 * @return The operator's result - or <code>null</code> if operation
	 * not valid or no  {@link Comparator} instance provided
	 */
	<V> Boolean invoke (Comparator<? super V> c, V value, V startValue, V endValue);
}
