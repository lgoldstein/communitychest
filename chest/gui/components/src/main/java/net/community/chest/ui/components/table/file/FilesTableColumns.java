/*
 * 
 */
package net.community.chest.ui.components.table.file;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import net.community.chest.util.collection.CollectionsUtils;

/**
 * <P>Copyright 2009 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Aug 4, 2009 2:15:18 PM
 */
public enum FilesTableColumns {
	NAME,
	SIZE,
	TYPE,
	MODTIME,
	ATTRS;

	public static final List<FilesTableColumns>	VALUES=Collections.unmodifiableList(Arrays.asList(values()));
	public static final FilesTableColumns fromString (final String s)
	{
		return CollectionsUtils.fromString(VALUES, s, false);
	}
}
