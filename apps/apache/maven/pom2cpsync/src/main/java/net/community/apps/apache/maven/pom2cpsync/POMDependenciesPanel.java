/**
 *
 */
package net.community.apps.apache.maven.pom2cpsync;

import java.util.Collection;

import net.community.chest.apache.maven.helpers.BaseTargetDetails;
import net.community.chest.apache.maven.helpers.BuildDependencyDetails;
import net.community.chest.apache.maven.helpers.BuildProjectFile;
import net.community.chest.apache.maven.helpers.DependenciesList;

/*
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Aug 14, 2008 2:47:17 PM
 */
public class POMDependenciesPanel extends DependencyDetailsPanel {
    /**
     *
     */
    private static final long serialVersionUID = -5574973669429878454L;
    public POMDependenciesPanel ()
    {
        super();
    }

    private static DependenciesList mergeDependenciesList (final DependenciesList org, final Collection<? extends BaseTargetDetails> dl)
    {
        final int    numItems=(null == dl) ? 0 : dl.size();
        if (numItems <= 0)
            return org;

        DependenciesList    ret=org;
        for (final BaseTargetDetails tgt : dl)
        {
            final String    groupId=(null == tgt) ? null : tgt.getGroupId(),
                            artifactId=(null == tgt) ? null : tgt.getArtifactId(),
                            version=(null == tgt) ? null : tgt.getVersion();
            if ((null == groupId) || (groupId.length() <= 0)
             ||    (null == artifactId) || (artifactId.length() <= 0))
                continue;    // should not happen

            final int    index=(null == ret) ? (-1) : ret.indexOf(groupId, artifactId);
            if (index >= 0)
            {
                final BaseTargetDetails    prev=ret.get(index);
                final String            prevVersion=prev.getVersion();
                // prefer entries with version over those without
                if ((null == prevVersion) || (prevVersion.length() <= 0))
                    prev.setVersion(version);
            }
            else    // new entry - add it
            {
                if (null == ret)    // unlikely
                    ret = new DependenciesList(numItems);

                if (!(tgt instanceof BuildDependencyDetails))    // unlikely
                {
                    final BuildDependencyDetails    ddd=new BuildDependencyDetails();
                    ddd.setGroupId(groupId);
                    ddd.setArtifactId(artifactId);
                    ddd.setVersion(version);
                    ret.add(ddd);
                }
                else
                    ret.add((BuildDependencyDetails) tgt);
            }
        }

        return ret;
    }
    /*
     * @see net.community.apps.apache.maven.pom2cpsync.DependencyDetailsPanel#loadDependencies(java.lang.String)
     */
    @Override
    public Collection<? extends BaseTargetDetails> loadDependencies (final String path) throws Exception
    {
        final BuildProjectFile    rootProj=new BuildProjectFile(path);
        DependenciesList        projDeps=rootProj.getProjectDependencies();
        for (BuildProjectFile    parProj=rootProj.resolveParentProject(); parProj != null; parProj=parProj.resolveParentProject())
        {
            projDeps = mergeDependenciesList(projDeps, parProj.getProjectDependencies());
            projDeps = mergeDependenciesList(projDeps, parProj.getManagerDependencies());
        }

        return projDeps;
    }
}
