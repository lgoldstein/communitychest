/*
 * 
 */
package net.community.chest.ui.components.table.file;

import java.io.File;
import java.util.List;

import org.w3c.dom.Element;

import net.community.chest.ui.helpers.table.EnumColumnAbstractTableModel;

/**
 * <P>Copyright 2009 as per GPLv2</P>
 *
 * @param <E> Type of {@link Enum} used for column identifier
 * @param <V> Type of {@link File} used as model contents
 * @author Lyor G.
 * @since Aug 4, 2009 2:19:27 PM
 */
public abstract class AbstractFilesTableModel<E extends Enum<E>,V extends File>
			extends EnumColumnAbstractTableModel<E,V> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6391671698319144172L;

	protected AbstractFilesTableModel (Class<E> colClass, Class<V> valsClass, int initialSize, List<E> cols)
		throws IllegalArgumentException
	{
		super(colClass, valsClass, initialSize, cols);
	}

	protected AbstractFilesTableModel (Class<E> colClass, Class<V> valsClass, List<E> cols)
		throws IllegalArgumentException
	{
		this(colClass, valsClass, 0, cols);
	}

	protected AbstractFilesTableModel (Class<E> colClass, Class<V> valsClass, int initialSize)
		throws IllegalArgumentException
	{
		this(colClass, valsClass, initialSize, (List<E>) null);
	}

	protected AbstractFilesTableModel (Class<E> colClass, Class<V> valsClass)
			throws IllegalArgumentException
	{
		this(colClass, valsClass, 0);
	}

	protected AbstractFilesTableModel (Class<E> colClass, Class<V> valsClass, int initialSize, Element elem)
		throws Exception
	{
		super(colClass, valsClass, initialSize, elem);
	}

	protected AbstractFilesTableModel (Class<E> colClass, Class<V> valsClass, Element elem)
		throws Exception
	{
		this(colClass, valsClass, 0, elem);
	}
}
