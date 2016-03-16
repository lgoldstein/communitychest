/*
 *
 */
package net.community.chest.ui.components.dialog.manifest;

import net.community.chest.ui.helpers.table.EnumTableColumnReflectiveProxy;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Sep 23, 2008 10:54:29 AM
 */
public class ManifestTableColReflectiveProxy extends EnumTableColumnReflectiveProxy<ManifestTableColumns,ManifestTableCol> {
    public ManifestTableColReflectiveProxy () throws IllegalArgumentException
    {
        super(ManifestTableColumns.class, ManifestTableCol.class, true);
    }

    public static final ManifestTableColReflectiveProxy    DEFAULT=new ManifestTableColReflectiveProxy();
}
