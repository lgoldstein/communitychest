/*
 * 
 */
package net.community.apps.common.test.table;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * <P>Copyright GPLv2</P>
 *
 * @author Lyor G.
 * @since Mar 22, 2009 8:28:15 AM
 */
public enum TestTableColumnType {
	COL1,
	COL2,
	COL3,
	COL4,
	COL5;

	public static final List<TestTableColumnType>	VALUES=Collections.unmodifiableList(Arrays.asList(values()));
}
