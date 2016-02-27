package net.community.apps.tools.srvident;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * <P>Copyright 2007 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Oct 25, 2007 9:50:33 AM
 */
public enum IdTableColumns {
	NAME,
	PROTO,
	TYPE,
	VERSION,
	WELCOME;

	public static final List<IdTableColumns>	VALUES=Collections.unmodifiableList(Arrays.asList(values()));
}
