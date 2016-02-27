/*
 * 
 */
package net.community.chest.ui.components.table.file;

import net.community.chest.ui.helpers.table.EnumTableColumnReflectiveProxy;

/**
 * <P>Copyright 2009 as per GPLv2</P>
 *
 * @param <C> Type of {@link FilesTableColInfo} being reflected
 * @author Lyor G.
 * @since Aug 5, 2009 2:30:08 PM
 */
public class FilesTableColInfoReflectiveProxy<C extends FilesTableColInfo>
		extends EnumTableColumnReflectiveProxy<FilesTableColumns,C> {
	protected FilesTableColInfoReflectiveProxy (Class<C> objClass, boolean registerAsDefault)
			throws IllegalArgumentException, IllegalStateException
	{
		super(FilesTableColumns.class, objClass, registerAsDefault);
	}

	public FilesTableColInfoReflectiveProxy (Class<C> objClass) throws IllegalArgumentException
	{
		this(objClass, false);
	}

	public static final FilesTableColInfoReflectiveProxy<FilesTableColInfo>	FLTBLCOLS=
		new FilesTableColInfoReflectiveProxy<FilesTableColInfo>(FilesTableColInfo.class);
}
