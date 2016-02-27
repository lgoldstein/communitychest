/*
 * 
 */
package net.community.chest.ui.helpers.table;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;

import net.community.chest.CoVariantReturn;
import net.community.chest.dom.DOMUtils;
import net.community.chest.reflect.ClassUtil;

import org.w3c.dom.Element;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * <P>Allows not to hold all the data in memory but rather use only
 * a cached and then fetch new data by demand.
 * 
 * @param <E> The {@link Enum} used to identify the column
 * @param <V> The associated column value
 * @author Lyor G.
 * @since Jan 22, 2009 1:06:29 PM
 */
public abstract class EnumColumnAbstractPagedTableModel<E extends Enum<E>,V> extends
		EnumColumnAbstractTableModel<E,V> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5156011253027140763L;
	private int	_pageSize;
	public int getPageSize ()
	{
		return _pageSize;
	}
	// CAVEAT EMPTOR - calling this method while the model is in use by a
	// 		table might have undefined effects
	public void setPageSize (int pageSize)
	{
		_pageSize = pageSize;
	}

	public static final String	PAGE_SIZE_ATTR="pageSize";
	public Integer setPageSize (Element elem)
	{
		final String	s=(null == elem) ? null : elem.getAttribute(PAGE_SIZE_ATTR);
		final Integer	v=((null == s) || (s.length() <= 0)) ? null : Integer.valueOf(s);
		if (v != null)
		{
			if (v.intValue() <= 1)
				throw new IllegalArgumentException(ClassUtil.getExceptionLocation(getClass(), "setPageSize") + " bad size (" + v + ") in element=" + DOMUtils.toString(elem));

			setPageSize(v.intValue());
		}

		return v;
	}
	/*
	 * @see net.community.chest.ui.helpers.table.EnumColumnAbstractTableModel#fromXml(org.w3c.dom.Element)
	 */
	@Override
	@CoVariantReturn
	public EnumColumnAbstractPagedTableModel<E,V> fromXml (Element elem) throws Exception
	{
		final Object	inst=super.fromXml(elem);
		if (inst != this)
			throw new IllegalStateException(ClassUtil.getExceptionLocation(getClass(), "fromXml") + " mismatched instances for element=" + DOMUtils.toString(elem));

		setPageSize(elem);
		return this;
	}

	protected EnumColumnAbstractPagedTableModel (Class<E> colClass, Class<V> valsClass, int pageSize)
		throws IllegalArgumentException
	{
		super(colClass, valsClass, pageSize);

		if ((_pageSize=pageSize) <= 1)
			throw new IllegalArgumentException("Bad initial page size: " + pageSize);
	}

	public static final int	DEFAULT_PAGE_SIZE=25;
	protected EnumColumnAbstractPagedTableModel (Class<E> colClass, Class<V> valsClass) throws IllegalArgumentException
	{
		this(colClass, valsClass, DEFAULT_PAGE_SIZE);
	}

	protected EnumColumnAbstractPagedTableModel (Class<E> colClass, Class<V> valsClass, int initialSize, Element elem) throws Exception
	{
		this(colClass, valsClass, initialSize);

		final Object	inst=fromXml(elem);
		if (inst != this)
			throw new IllegalStateException(ClassUtil.getConstructorExceptionLocation(getClass()) + " mismatched instances for element=" + DOMUtils.toString(elem));
	}

	protected EnumColumnAbstractPagedTableModel (Class<E> colClass, Class<V> valsClass, Element elem) throws Exception
	{
		this(colClass, valsClass, DEFAULT_PAGE_SIZE, elem);
	}
	// 1st row being cached in the rows (and up to page-size)
	private int	_baseRowIndex	/* =0 */; 
	protected int getBaseRowIndex ()
	{
		return _baseRowIndex;
	}

	protected void setBaseRowIndex (int baseRowIndex)
	{
		_baseRowIndex = baseRowIndex;
	}

	protected int getMaxRowIndex ()
	{
		return getBaseRowIndex() + getPageSize();
	}

	protected int fromRowIndex (int index)
	{
		final int	baseIndex=getBaseRowIndex();
		return (index - baseIndex);
	}

	protected int toRowIndex (int index)
	{
		final int	baseIndex=getBaseRowIndex();
		return baseIndex + index;
	}
	/**
	 * Called to a sequence of entries starting at a specified row index
	 * @param index The row index to start the addition
	 * @param values The {@link Collection} of values to be added
	 */
	protected abstract void addPagedValues (int index, Collection<? extends V> values);
	/**
	 * Called to a sequence of entries starting at a specified row index
	 * @param index The row index
	 * @param values The values to be added
	 */
	protected void addPagedValues (int index, V ... values)
	{
		if ((null == values) || (values.length <= 0))
			return;
		addPagedValues(index, Arrays.asList(values));
	}
	/**
	 * @param startIndex 1st row index (inclusive)
	 * @param endIndex last row index (exclusive)
	 * @return A {@link Collection} of values within that index (may be null/empty)
	 */
	protected abstract Collection<? extends V> getValues (int startIndex, int endIndex);

	protected Collection<? extends V> refreshRows (List<V> rows, int startIndex, int endIndex)
	{
		final Collection<? extends V>	vl=getValues(startIndex, endIndex);
		final int						numValues=(null == vl) ? 0 : vl.size(),
										maxValues=endIndex - startIndex;
		// there must be some value in this range since we had a cache and ADDED a value
		if (numValues > maxValues)
			throw new IllegalStateException("refreshRows(" + startIndex + "-" + endIndex + ") - too many items: expected=" + maxValues + "/got=" + numValues);

		rows.clear();

		if (numValues > 0)
			rows.addAll(vl);

		return vl;
	}

	protected Collection<? extends V> refreshRows (List<V> rows)
	{
		return refreshRows(rows, getBaseRowIndex(), getMaxRowIndex());
	}
	/*
	 * @see net.community.chest.ui.helpers.table.AbstractTypedTableModel#addAll(java.util.List, int, java.util.Collection)
	 */
	@Override
	protected boolean addAll (List<V> rows, int index, Collection<? extends V> c)
	{
		final int	numItems=(null == c) ? 0 : c.size();
		if (numItems <= 0)
			return false;

		addPagedValues(index, c);

		final int	loIndex=getBaseRowIndex(), hiIndex=getMaxRowIndex();
		// if added rows beyond the current cache range, then no need to update the cache
		if (index >= hiIndex)
			return true;

		// if added below the current cache range simply update the base row index
		if (index < loIndex)
		{
			setBaseRowIndex(loIndex + numItems);
			return true;
		}
	
		// added rows within the current cache - "push" it between the others
		final int		rIndex=fromRowIndex(index), pgSize=getPageSize();
		final boolean	added=rows.addAll(rIndex, c);
		// if by adding it we may have exceeded the max. page size, trim the cache
		for (int	curSize=rows.size(); curSize > pgSize; curSize--)
			rows.remove(curSize - 1);
		return added;
	}
	/*
	 * @see net.community.chest.ui.helpers.table.AbstractTypedTableModel#add(java.util.List, int, java.lang.Object)
	 */
	@Override
	@SuppressWarnings("unchecked")
	protected void add (List<V> rows, int index, V element)
	{
		addAll(rows, index, Arrays.asList(element));
	}
	/*
	 * @see net.community.chest.ui.helpers.table.AbstractTypedTableModel#get(java.util.List, int)
	 */
	@Override
	protected V get (List<V> rows, int index)
	{
		final int	loIndex=getBaseRowIndex(),
					maxIndex=loIndex + rows.size(),
					pgSize=getPageSize();
		// cache miss - refresh the cache
		if (index < loIndex)
		{
			final int	minIndex=Math.max(index - 1 /* get a bit below the original index */, 0);
			refreshRows(rows, minIndex, minIndex + pgSize);
			setBaseRowIndex(minIndex);
		}
		else if (index >= maxIndex)
		{
			final int	endIndex=Math.min(size(), index - 1 + (pgSize / 2) /* place the index at mid-cache */),
						startIndex=Math.max(0, endIndex - pgSize);
			refreshRows(rows, startIndex, endIndex);
			setBaseRowIndex(startIndex);
		}

		final int	rIndex=fromRowIndex(index);
		return rows.get(rIndex);
	}

	protected abstract void updatePagedValue (int index, V value);
	/*
	 * @see net.community.chest.ui.helpers.table.AbstractTypedTableModel#set(java.util.List, int, java.lang.Object)
	 */
	@Override
	protected V set (List<V> rows, int index, V element)
	{
		final V	prev=get(rows, index);
		updatePagedValue(index, element);

		final int	loIndex=getBaseRowIndex(), hiIndex=loIndex + rows.size();
		if ((index >= loIndex) && (index < hiIndex))
		{
			final int	rIndex=fromRowIndex(index);
			return rows.set(rIndex, element);
		}

		return prev;
	}

	protected abstract void clearPagedValues ();
	/*
	 * @see net.community.chest.ui.helpers.table.AbstractTypedTableModel#clear(java.util.List)
	 */
	@Override
	protected void clear (List<V> rows)
	{
		clearPagedValues();
		setBaseRowIndex(0);
		super.clear(rows);
	}

	protected abstract boolean checkPagedValuesExistence (Collection<?> c); 
	/*
	 * @see net.community.chest.ui.helpers.table.AbstractTypedTableModel#containsAll(java.util.List, java.util.Collection)
	 */
	@Override
	protected boolean containsAll (List<V> rows, Collection<?> c)
	{
		return checkPagedValuesExistence(c);
	}

	protected abstract int getTotalPagedValuesCount ();
	/*
	 * @see net.community.chest.ui.helpers.table.AbstractTypedTableModel#getMaxRowIndex(java.util.List)
	 */
	@Override
	protected int getMaxRowIndex (List<V> rows)
	{
		return getTotalPagedValuesCount();
	}

	protected abstract int getPagedValueIndex (V v);
	/*
	 * @see net.community.chest.ui.helpers.table.AbstractTypedTableModel#indexOf(java.util.List, java.lang.Object)
	 */
	@Override
	protected int indexOf (List<V> rows, Object o)
	{
		if (!isValidObject(o))
			return (-1);

		return getPagedValueIndex(getValuesClass().cast(o));
	}

	protected static class ModelListIterator<T> implements ListIterator<T> {
		private final AbstractTypedTableModel<T>	_model;
		public final AbstractTypedTableModel<T> getModel ()
		{
			return _model;
		}

		private final int	_bottomIndex;
		public final int getBottomIndex ()
		{
			return _bottomIndex;
		}

		private int	_curIndex;
		public final int getCurIndex ()
		{
			return _curIndex;
		}

		protected ModelListIterator (AbstractTypedTableModel<T> model, int startIndex)
		{
			if (null == (_model=model))
				throw new IllegalArgumentException("No model specified");
			_bottomIndex = startIndex;
			_curIndex = startIndex;
		}
		/*
		 * @see java.util.ListIterator#add(java.lang.Object)
		 */
		@Override
		public void add (T e)
		{
			getModel().add(e);
		}
		/*
		 * @see java.util.ListIterator#hasNext()
		 */
		@Override
		public boolean hasNext ()
		{
			final List<T>	model=getModel();
			return (getCurIndex() < model.size());
		}
		/*
		 * @see java.util.ListIterator#hasPrevious()
		 */
		@Override
		public boolean hasPrevious ()
		{
			return (getCurIndex() > getBottomIndex());
		}
		/*
		 * @see java.util.ListIterator#next()
		 */
		@Override
		public T next ()
		{
			final List<T>	model=getModel();
			if (_curIndex >= model.size())
				throw new NoSuchElementException("next(" + _curIndex + ") at end of list");

			_curIndex++;
			return model.get(_curIndex);
		}
		/*
		 * @see java.util.ListIterator#nextIndex()
		 */
		@Override
		public int nextIndex ()
		{
			final List<T>	model=getModel();
			if (_curIndex >= model.size())
				return _curIndex;

			_curIndex++;
			return _curIndex;
		}
		/*
		 * @see java.util.ListIterator#previous()
		 */
		@Override
		public T previous ()
		{
			if (_curIndex <= getBottomIndex())
				throw new NoSuchElementException("previous(" + _curIndex + ") already at bottom index");

			_curIndex--;
			return getModel().get(_curIndex);
		}
		/*
		 * @see java.util.ListIterator#previousIndex()
		 */
		@Override
		public int previousIndex ()
		{
			final int	curIndex=getCurIndex();
			if (curIndex > getBottomIndex())
				return curIndex - 1;
			else
				return (-1);
		}
		/*
		 * @see java.util.ListIterator#remove()
		 */
		@Override
		public void remove ()
		{
			getModel().remove(getCurIndex());
		}
		/*
		 * @see java.util.ListIterator#set(java.lang.Object)
		 */
		@Override
		public void set (T e)
		{
			getModel().set(getCurIndex(), e);
		}
	}
	/*
	 * @see net.community.chest.ui.helpers.table.AbstractTypedTableModel#listIterator(java.util.List, int)
	 */
	@Override
	protected ListIterator<V> listIterator (List<V> rows, int index)
	{
		return new ModelListIterator<V>(this, index);
	}

	protected abstract void removePagedItem (int index, V value);
	/*
	 * @see net.community.chest.ui.helpers.table.AbstractTypedTableModel#remove(java.util.List, int)
	 */
	@Override
	protected V remove (List<V> rows, int index)
	{
		final V	v=get(rows, index);
		if (v != null)
			removePagedItem(index, v);
		return v;
	}
	/*
	 * @see net.community.chest.ui.helpers.table.AbstractTypedTableModel#removeAll(java.util.List, java.util.Collection)
	 */
	@Override
	protected boolean removeAll (List<V> rows, Collection<?> c)
	{
		final int	numObjs=(null == c) ? 0 : c.size();
		if (numObjs <= 0)
			return false;

		final Class<V>	vc=getValuesClass();
		final int		hiIndex=getMaxRowIndex();
		int				numDeleted=0, curSize=size();
		boolean			refreshVals=false;
		for (final Object o : c)
		{
			final int	oIndex=indexOf(rows, o);
			if (oIndex < 0)
				continue;
			
			removePagedItem(oIndex, vc.cast(o));

			curSize--;
			// if ran out of items then stop right here
			if (curSize <= 0)
			{
				rows.clear();
				setBaseRowIndex(0);
				return true;
			}

			// if deleted value is beyond current cache range then no need to update the cache
			if (oIndex < hiIndex)
				refreshVals = true;

			numDeleted++;
		}

		if (refreshVals)
		{
			final int pgSize=getPageSize();
			if (curSize <= pgSize)
				setBaseRowIndex(0);
			else if (hiIndex > curSize)
				setBaseRowIndex(Math.max(curSize - pgSize, 0));
			refreshRows(rows);
		}

		return (numDeleted > 0);
	}
	/*
	 * @see net.community.chest.ui.helpers.table.AbstractTypedTableModel#retainAll(java.util.List, java.util.Collection)
	 */
	@Override
	protected boolean retainAll (List<V> rows, Collection<?> c)
	{
		if ((null == c) || (c.size() <= 0))
			return false;

		throw new UnsupportedOperationException("retainAll() N/A");
	}
	/*
	 * @see net.community.chest.ui.helpers.table.AbstractTypedTableModel#subList(java.util.List, int, int)
	 */
	@Override
	protected List<V> subList (List<V> rows, int fromIndex, int toIndex)
	{
		throw new UnsupportedOperationException("subList(" + fromIndex + "-" + toIndex + ") N/A");
	}
	/*
	 * @see net.community.chest.ui.helpers.table.AbstractTypedTableModel#toArray(java.util.List, T[])
	 */
	@Override
	protected <T> T[] toArray (List<V> rows, T[] a)
	{
		throw new UnsupportedOperationException("toArray(" + a.getClass().getComponentType().getName() + ") N/A");
	}
}
