package net.community.chest.ui.components.dialog.manifest;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import net.community.chest.util.collection.CollectionsUtils;

/**
 * <P>Copyright 2007 as per GPLv2</P>
 *
 * <P>{@link Enum} value used for displaying {@link java.util.jar.Manifest} attributes</P>
 * 
 * @author Lyor G.
 * @since Aug 6, 2007 2:27:40 PM
 */
public enum ManifestTableColumns {
	ATTR_NAME,
	ATTR_VALUE;

	public static final List<ManifestTableColumns>	VALUES=Collections.unmodifiableList(Arrays.asList(values()));
	public static ManifestTableColumns fromString (final String s)
	{
		return CollectionsUtils.fromString(VALUES, s, false);
	}
}
