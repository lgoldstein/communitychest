/*
 * 
 */
package net.community.chest.util.collection;

import java.util.Collection;
import java.util.Collections;

/**
 * <P>Copyright as per GPLv2</P>
 * @author Lyor G.
 * @param <E> Type if value in the {@link Collection}s
 * @since Aug 29, 2012 3:11:08 PM
 *
 */
public class CollectionSyncResult<E> {
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
		if (coll == null)
			throw new IllegalArgumentException("No collection to manipulate");

		final Collection<? extends E>	delValues=getValuesToRemove();
		// nothing to delete if collection already empty or no values to remove
		if (CollectionsUtils.isEmpty(coll) || CollectionsUtils.isEmpty(delValues))
			return false;	// debug breakpoint
		else
			return coll.removeAll(delValues);
	}

	public boolean executeAddActions (Collection<E> coll)
	{
		if (coll == null)
			throw new IllegalArgumentException("No collection to manipulate");

		final Collection<? extends E>	addValues=getValuesToAdd();
		// nothing to delete if collection already empty or no values to remove
		if (CollectionsUtils.isEmpty(addValues))
			return false;	// debug breakpoint

		return coll.addAll(addValues);
	}

	@Override
	public String toString ()
	{
		return "add: " + getValuesToAdd() + ", remove: " + getValuesToRemove();
	}
}
