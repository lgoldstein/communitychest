package net.community.chest.apache.ant.mvnsync.helpers;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

import net.community.chest.apache.maven.helpers.BuildDependencyDetails;
import net.community.chest.apache.maven.helpers.BuildProject;
import net.community.chest.util.compare.VersionComparator;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Jan 31, 2008 3:05:29 PM
 */
public class Dependency extends BuildDependencyDetails {
    /**
     *
     */
    private static final long serialVersionUID = -8457645994308627927L;
    public Dependency ()
    {
        super();
    }

    private String    _srcName    /* =null */;
    public String getTargetSourcesFileName ()
    {
        if (null == _srcName)
            _srcName = getBaseTargetName() + "-sources.jar";
        return _srcName;
    }

    public void setTargetSourcesFileName (String name)
    {
        _srcName = name;
    }

    private String    _docName    /* =null */;
    public String getTargetJavadocFileName ()
    {
        if (null == _docName)
            _docName = getBaseTargetName() + "-javadoc.jar";
        return _docName;
    }

    public void setTargetJavadocFileName (String name)
    {
        _docName = name;
    }

    private String    _jarName    /* =null */;
    public String getTargetJarFileName ()
    {
        if (null == _jarName)
            _jarName = getBaseTargetName() + ".jar";
        return _jarName;
    }

    public void setTargetJarFileName (String name)
    {
        _jarName = name;
    }

    private String    _pomName    /* =null */;
    public String getTargetPomFileName ()
    {
        if (null == _pomName)
            _pomName = getBaseTargetName() + ".pom";
        return _pomName;
    }

    public void setTargetPomFileName (String name)
    {
        _pomName = name;
    }

    public boolean isDependencyFile (final String tgtName)
    {
        if ((null == tgtName) || (tgtName.length() <= 0))
            return false;

        if (tgtName.equalsIgnoreCase(getTargetJarFileName())
         || tgtName.equalsIgnoreCase(getTargetSourcesFileName())
         || tgtName.equalsIgnoreCase(getTargetPomFileName())
         || tgtName.equalsIgnoreCase(getTargetJavadocFileName()))
            return true;

        return false;
    }
    /*
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString ()
    {
        return getGroupId() + "[" + getArtifactId() + "](" + getVersion() + ")";
    }

    public File findBestMatchingJar (final File ... jarFiles)
    {
        if ((null == jarFiles) || (jarFiles.length <= 0))
            return null;

        String    artId=getArtifactId();
        if ((null == artId) || (artId.length() <= 0))
            return null;    // unlikely
        else    // use lowercase all over
            artId = artId.toLowerCase();

        String ver=getVersion();
        if ((ver != null) && (ver.length() > 0))
            ver = ver.toLowerCase();    // use lowercase all over

        for (final File f : jarFiles)
        {
            if ((null == f) || (!f.isFile()))
                continue;

            // use lowercase all over
            final String    jarName=f.getName().toLowerCase();
            if (!jarName.endsWith(".jar"))
                continue;    // we want to check only JAR(s)

            if ((jarName.length() < artId.length())
             || (!jarName.startsWith(artId)))
                continue;

            final String    jarVersion=jarName.substring(artId.length(), jarName.length() - ".jar".length());
            if ((null == ver) || (ver.length() <= 0)
             || (null == jarVersion) || (jarVersion.length() <= 0))
                return f;    // if no version specified, assume match found

            final int    nRes=VersionComparator.compareVersions(ver, jarVersion);
            if (nRes <= 0)    // OK if found a greater version
                return f;
        }

        // this point is reached if no match is found
        return null;
    }

    public static final Collection<Dependency> loadPomDependencies (final File pomFile) throws Exception
    {
        final BuildProject                                    proj=new BuildProject(pomFile.getAbsolutePath());
        final Collection<? extends BuildDependencyDetails>    deps=proj.getProjectDependencies();
        final int                                            numDeps=(null == deps) ? 0 : deps.size();
        final Collection<Dependency>                        ret=(numDeps <= 0) ? null : new ArrayList<Dependency>(numDeps);
        if (numDeps > 0)
        {
            for (final BuildDependencyDetails bdd : deps)
            {
                final String    grpId=(null == bdd) ? null : bdd.getGroupId(),
                                artId=(null == bdd) ? null : bdd.getArtifactId();
                if ((null == grpId) || (grpId.length() <= 0)
                 || (null == artId) || (artId.length() <= 0))
                    continue;    // don't care about version

                final String    scope=bdd.getScope();
                if ("test".equals(scope))
                    continue;    // we do not check on test dependencies

                final Dependency    d=new Dependency();
                d.setGroupId(grpId);
                d.setArtifactId(artId);
                d.setVersion(bdd.getVersion());
                ret.add(d);
            }
        }

        return ret;
    }
}
