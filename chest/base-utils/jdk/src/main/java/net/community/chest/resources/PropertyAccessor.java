/*
 * 
 */
package net.community.chest.resources;

/**
 * <P>Copyright GPLv2</P>
 *
 * @param <K> Key type used to retrieve the property 
 * @param <V> Expected value type
 * @author Lyor G.
 * @since May 27, 2009 12:25:30 PM
 */
public interface PropertyAccessor<K,V> {
	// return null/"empty" (if meaningful) if not found
	V getProperty (K key);
}
