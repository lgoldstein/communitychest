/*
 * 
 */
package org.apache.commons.collections4;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;

/**
 * @author Lyor G.
 * @since Oct 11, 2011 8:52:32 AM
 */
public final class ExtraCollectionUtils {
	private ExtraCollectionUtils ()
	{
		// no instance
	}

	public static final <T> List<T> trimToLastSize (final List<T> list, final int maxSize)
	{
		final int	listSize=sizeOf(list);
		if (listSize <= maxSize)
			return list;

		if (maxSize <= 0)
		{
			list.clear();
			return list;
		}

		while (list.size() > maxSize)
			list.remove(list.size() - 1);
		return list;
	}

	public static final int sizeOf (final Collection<?> coll)
	{
		return (coll == null) ? 0 : coll.size();
	}

    public static boolean isEmpty (final Collection<?> coll)
    {
        return (coll == null) || coll.isEmpty();
    }

    public static final <E> Collection<E> safeCollection (final Collection<E> coll)
    {
    	if (coll == null)
    		return Collections.emptyList();
    	else
    		return coll;
    }

    @SafeVarargs
    public static final <E> List<E> unionAll (final Collection<? extends E> ... collections)
	{
		if (ArrayUtils.isEmpty(collections))
			return Collections.emptyList();

		List<E>	result=null;
		for (final Collection<? extends E> coll : collections)
		{
			if (isEmpty(coll))
				continue;

			if (result == null)
				result = new ArrayList<E>(coll);
			else
				result = (List<E>) CollectionUtils.union(result, coll);
		}

		if (result == null)
			return Collections.emptyList();
		return result;
	}
	/**
	 * Makes sure that the destination {@link Collection} contains only the
	 * objects from the source. This is done by removing objects that do not
	 * appear in the source and adding those that do not appear in the destination.
	 * @param src The source {@link Collection}
	 * @param dst The destination {@link Collection}
	 * @return TRUE if anything was changed in the destination
	 * @see #calculateSyncActions(Collection, Collection)
	 */
	public static final <E> boolean syncContents (final Collection<? extends E> src, final Collection<E> dst)
	{
		final CollectionSyncResult<E>	result=calculateSyncActions(src, dst);
		return result.executeActions(dst);
	}
	/**
	 * Calculates the necessary actions in order to ensure that the destination
	 * {@link Collection} contains only the objects from the source. This is done
	 * by removing objects that do not appear in the source and adding those that
	 * do not appear in the destination.
	 * @param src The source {@link Collection}
	 * @param dst The destination {@link Collection}
	 * @return The required actions to achieve synchronization
	 * @see CollectionSyncResult#executeActions(Collection) 
	 */
	public static final <E> CollectionSyncResult<E> calculateSyncActions (
			final Collection<? extends E> src, final Collection<? extends E> dst)
	{
		if (isEmpty(src))
		{
			if (isEmpty(dst))	// both empty - nothing to do
				return new CollectionSyncResult<E>(Collections.<E>emptyList(), Collections.<E>emptyList());
			else	// need to remove all entries from destination to make it empty as well
				return new CollectionSyncResult<E>(Collections.<E>emptyList(), new ArrayList<E>(dst));
		}

		if (isEmpty(dst))	// need to add all values from source (and delete none)
			return new CollectionSyncResult<E>(new ArrayList<E>(src), Collections.<E>emptyList());

		final Collection<E> addValues=CollectionUtils.subtract(src, dst), delValues=CollectionUtils.subtract(dst, src);
		return new CollectionSyncResult<E>(addValues, delValues);
	}
}
