/*
 *
 */
package net.community.chest.io.file;

import java.io.File;

import net.community.chest.util.compare.AbstractComparator;

/**
 * <P>Copyright 2009 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Aug 6, 2009 12:24:11 PM
 */
public abstract class AbstractFileComparator extends AbstractComparator<File> {
    /**
     *
     */
    private static final long serialVersionUID = 8301131249606451369L;

    protected AbstractFileComparator (boolean reverseMatch)
    {
        super(File.class, reverseMatch);
    }
}
