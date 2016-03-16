/*
 *
 */
package net.community.chest.win32.core.serial;

import java.io.IOException;
import java.io.InputStream;
import java.io.StreamCorruptedException;

import net.community.chest.CoVariantReturn;
import net.community.chest.io.encode.ElementEncoder;
import net.community.chest.lang.PubliclyCloneable;

/**
 * <P>Copyright as per GPLv2</P>
 * @author Lyor G.
 * @since Feb 5, 2013 10:04:14 AM
 *
 */
public class SystemClassWithMembersAndTypes extends AbstractClassWithMembersAndTypes
            implements PubliclyCloneable<SystemClassWithMembersAndTypes>,
                       ElementEncoder<SystemClassWithMembersAndTypes> {
    private static final long serialVersionUID = -8365190679037202106L;

    public SystemClassWithMembersAndTypes ()
    {
        super(RecordTypeEnumeration.SystemClassWithMembersAndTypes);
    }

    public SystemClassWithMembersAndTypes (InputStream in) throws IOException
    {
        super(RecordTypeEnumeration.SystemClassWithMembersAndTypes);

        Object    result=read(in);
        if (result != this)
            throw new StreamCorruptedException("Mismatched read data instance");
    }

    @Override
    @CoVariantReturn
    public SystemClassWithMembersAndTypes read (InputStream in) throws IOException
    {
        return getClass().cast(super.read(in));
    }

    @Override
    public SystemClassWithMembersAndTypes clone () throws CloneNotSupportedException
    {
        return getClass().cast(super.clone());
    }
}
