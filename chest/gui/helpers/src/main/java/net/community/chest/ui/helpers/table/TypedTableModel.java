package net.community.chest.ui.helpers.table;

import java.util.List;

import javax.swing.table.TableModel;

import net.community.chest.lang.TypedValuesContainer;

/**
 * <P>Copyright 2007 as per GPLv2</P>
 *
 * <P>Used to combine a {@link TableModel} and a {@link TypedValuesContainer}</P>
 * 
 * @param <V> The type of value associated with each row
 * @author Lyor G.
 * @since Aug 6, 2007 8:12:30 AM
 */
public interface TypedTableModel<V> extends TableModel, TypedValuesContainer<V>, List<V> {
	// nothing more to add
}
