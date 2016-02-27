package net.community.chest.util;

/**
 * Copyright 2007 as per GPLv2
 * 
 * Used for bucket sorting in a general {@link java.util.Map}
 * 
 * @param <K> Generic key type
 * @param <V> Generic value type
 * @author Lyor G.
 * @since Jun 18, 2007 2:08:46 PM
 */
public interface BucketMapper<K,V> {
	/**
	 * @param value value to be checked
	 * @return bucket key for the value - may NOT be null
	 */
	K getBucketKey (V value);
}
