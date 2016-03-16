/*
 *
 */
package net.community.chest.win32.core.serial;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import net.community.chest.CoVariantReturn;
import net.community.chest.io.encode.ElementEncoder;
import net.community.chest.lang.PubliclyCloneable;

/**
 * <P>Copyright as per GPLv2</P>
 * @author Lyor G.
 * @since Jan 6, 2013 3:19:28 PM
 *
 */
public class MessageEndRecord extends SerializationRecord
        implements PubliclyCloneable<MessageEndRecord>,
                   ElementEncoder<MessageEndRecord> {
    private static final long serialVersionUID = -4783033605387799757L;

    public MessageEndRecord ()
    {
        super(RecordTypeEnumeration.MessageEnd);
    }

    @Override
    @CoVariantReturn
    public MessageEndRecord read (InputStream in) throws IOException
    {
        return getClass().cast(super.read(in));
    }

    @Override
    public void readRecordData (InputStream in) throws IOException
    {
        if (in == null)
            throw new IOException("No input stream");
    }

    @Override
    public void writeRecordData (OutputStream out) throws IOException
    {
        if (out == null)
            throw new IOException("No output stream");
    }

    @Override
    public MessageEndRecord clone () throws CloneNotSupportedException
    {
        return getClass().cast(super.clone());
    }
}
