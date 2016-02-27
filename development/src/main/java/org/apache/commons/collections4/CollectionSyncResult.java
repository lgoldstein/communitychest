package org.apache.commons.collections4;

import java.util.Collection;
import java.util.Collections;

/**
 * @param <E> Type of element in the {@link Collection}-s
 * @author Lyor G.
 * @since Jan 25, 2012 9:00:38 AM
 */
public final class CollectionSyncResult<E> {
	private final Collection<E>	_valuesToAdd, _valuesToRemove;
	public CollectionSyncResult (Collection<E> addValues, Collection<E> delValues)
	{
		_valuesToAdd = (addValues == null) ? Collections.<E>emptyList() : addValues;
		_valuesToRemove = (delValues == null) ? Collections.<E>emptyList() : delValues;
	}

	public Collection<E> getValuesToAdd ()
	{
		return _valuesToAdd;
	}

	public Collection<E> getValuesToRemove ()
	{
		return _valuesToRemove;
	}
	/**
	 * Executes the synchronization actions
	 * @param coll The {@link Collection} to be manipulated
	 * @return TRUE if anything changed in the manipulated collection
	 */
	public boolean executeActions (Collection<E> coll)
	{
		final boolean	delResult=executeRemoveActions(coll),
						addResult=executeAddActions(coll);
		return addResult || delResult;
	}

	public boolean executeRemoveActions (Collection<E> coll)
	{
		final Collection<? extends E>	delValues=getValuesToRemove();
		// nothing to delete if collection already empty or no values to remove
		if (ExtraCollectionUtils.isEmpty(coll) || ExtraCollectionUtils.isEmpty(delValues))
			return false;	// debug breakpoint
		else
			return coll.removeAll(delValues);
	}

	public boolean executeAddActions (Collection<E> coll)
	{
		final Collection<? extends E>	addValues=getValuesToAdd();
		// nothing to delete if collection already empty or no values to remove
		if (ExtraCollectionUtils.isEmpty(addValues))
			return false;	// debug breakpoint

		if (coll == null)
			throw new IllegalArgumentException("No collection to manipulate");
		return coll.addAll(addValues);
	}
}