/**
 * 
 */
package net.community.apps.apache.maven.pom2cpsync;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * <P>Details display columns</P>
 * @author Lyor G.
 * @since Aug 14, 2008 1:30:37 PM
 */
public enum DependencyDetailsColumns {
	// NOTE !!! assumption is that columns index matches ordinal value
	GROUP,
	ARTIFACT,
	VERSION;

	public static final List<DependencyDetailsColumns>	VALUES=Collections.unmodifiableList(Arrays.asList(values()));
}
