/*
 *
 */
package net.community.chest.apache.maven.helpers;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * <P>Interface used to resolve the {@link BuildProject} of a parent POM</P>
 * @author Lyor G.
 * @since Aug 14, 2008 10:17:25 AM
 */
public interface ParentTargetResolver {
    /**
     * @param proj The {@link BuildProject} whose parent {@link BuildProject}
     * is required
     * @return The {@link BuildProject} of the parent - if null, then no parent
     * project is found/available
     * @throws Exception If failed to load parent {@link BuildProject}
     */
    BuildProject resolveParentProject (BuildProject proj) throws Exception;
}
