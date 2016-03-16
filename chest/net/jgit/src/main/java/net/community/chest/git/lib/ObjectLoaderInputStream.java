/*
 *
 */
package net.community.chest.git.lib;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StreamCorruptedException;

import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.TreeEntry;

/**
 * <P>Copyright as per GPLv2</P>
 * @author Lyor G.
 * @since Mar 16, 2011 3:23:48 PM
 */
public class ObjectLoaderInputStream extends ByteArrayInputStream {
    public ObjectLoaderInputStream (ObjectLoader loader) throws IOException
    {
        super(loader.getCachedBytes(), 0, (int) loader.getSize());

        final long    fullSize=loader.getSize();
        if (fullSize >= Integer.MAX_VALUE)
            throw new StreamCorruptedException("Reported size too big: " + fullSize);
    }

    public ObjectLoaderInputStream (Repository repo, ObjectId id) throws IOException
    {
        this(repo.openBlob(id));
    }

    public ObjectLoaderInputStream (Repository repo, Ref ref) throws IOException
    {
        this(repo, ref.getObjectId());
    }

    public ObjectLoaderInputStream (TreeEntry entry) throws IOException
    {
        this(entry.getRepository(), entry.getId());
    }
}
