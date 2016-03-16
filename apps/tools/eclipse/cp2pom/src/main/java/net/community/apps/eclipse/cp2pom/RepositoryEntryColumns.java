/*
 *
 */
package net.community.apps.eclipse.cp2pom;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * <P>Copyright as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Jul 27, 2009 10:25:59 AM
 */
public enum RepositoryEntryColumns {
    GROUP,
    ARTIFACT,
    VERSION;

    public static final List<RepositoryEntryColumns>    VALUES=Collections.unmodifiableList(Arrays.asList(values()));
}
