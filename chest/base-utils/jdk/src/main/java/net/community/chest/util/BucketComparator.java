package net.community.chest.util;

/**
 * Copyright 2007 as per GPLv2
 *
 * A "comparator" that is used to do bucket sorting - basically it needs to
 * return a bucket index in the buckets array. <B>Note:</B> it is up to the
 * user to make sure returned index is within specified buckets array range
 *
 * @param <V> Type of compared object(s)
 * @author Lyor G.
 * @since Jun 18, 2007 1:21:01 PM
 */
public interface BucketComparator<V> {
    /**
     * @param obj object whose bucket index we require
     * @return bucket index in the buckets array. <B>Note:</B> it is up to
     * the user to make sure returned index is within specified buckets array
     * range (and non-negative).
     */
    int getBucketIndex (V obj);
}
