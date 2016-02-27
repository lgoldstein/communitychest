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
 * @since Feb 19, 2013 4:04:02 PM
 */
public class ObjectNullRecord extends SerializationRecord 
			implements PubliclyCloneable<ObjectNullRecord>,
					   ElementEncoder<ObjectNullRecord> {
	private static final long serialVersionUID = -7175907025227277330L;

	public ObjectNullRecord ()
	{
		super(RecordTypeEnumeration.ObjectNull);
	}

	@Override
	@CoVariantReturn
	public ObjectNullRecord read (InputStream in) throws IOException
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
	public ObjectNullRecord clone () throws CloneNotSupportedException
	{
		return getClass().cast(super.clone());
	}
}
