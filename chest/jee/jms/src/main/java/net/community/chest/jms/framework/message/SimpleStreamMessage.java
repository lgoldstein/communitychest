/*
 * 
 */
package net.community.chest.jms.framework.message;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import javax.jms.JMSException;
import javax.jms.MessageNotReadableException;
import javax.jms.MessageNotWriteableException;
import javax.jms.StreamMessage;

/**
 * <P>Copyright 2010 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Jun 10, 2010 10:37:12 AM
 */
public class SimpleStreamMessage extends AbstractMessage implements StreamMessage {
	private ObjectInputStream	_input;
	protected ObjectInputStream getInputStream (boolean failIfNull) throws JMSException
	{
		if ((null == _input) && failIfNull)
			throw new MessageNotReadableException("No incoming stream");

		return _input;
	}

	private ByteArrayInputStream	_inpBytes;
	protected void closeInputStream () throws IOException
	{
		if (_inpBytes != null)
			_inpBytes = null;	// no need to close it...

		if (_input != null)
		{
			try
			{
				_input.close();
			}
			finally
			{
				_input = null;
			}
		}
	}

	private ObjectOutputStream	_output;
	protected ObjectOutputStream getOutputStream (boolean failIfNull) throws JMSException
	{
		if ((null == _output) && failIfNull)
			throw new MessageNotWriteableException("No outgoing stream");

		return _output;
	}

	private ByteArrayOutputStream	_outBytes;
	protected byte[] closeOutputStream () throws IOException
	{
		if (_output != null)
		{
			try
			{
				_output.close();
			}
			finally
			{
				_output = null;
			}
		}

		byte[]	outData=null;
		if (_outBytes != null)
		{
			outData = _outBytes.toByteArray();
			_outBytes = null;	// no need to close it since closed by the parent
		}

		return outData;
	}

	public SimpleStreamMessage () throws JMSException
	{
		clearBody();	// prepare for writing
	}
	/*
	 * @see javax.jms.StreamMessage#readBoolean()
	 */
	@Override
	public boolean readBoolean () throws JMSException
	{
		final ObjectInputStream	in=getInputStream(true);
		try
		{
			return in.readBoolean();
		}
		catch(IOException e)
		{
			throw new JMSException("readBoolean(" + e.getClass().getName() + ")", e.getMessage());
		}
	}
	/*
	 * @see javax.jms.StreamMessage#readByte()
	 */
	@Override
	public byte readByte () throws JMSException
	{
		final ObjectInputStream	in=getInputStream(true);
		try
		{
			return in.readByte();
		}
		catch(IOException e)
		{
			throw new JMSException("readByte(" + e.getClass().getName() + ")", e.getMessage());
		}
	}
	/*
	 * @see javax.jms.StreamMessage#readBytes(byte[])
	 */
	@Override
	public int readBytes (byte[] value) throws JMSException
	{
		final ObjectInputStream	in=getInputStream(true);
		try
		{
			return in.read(value);
		}
		catch(IOException e)
		{
			throw new JMSException("readBytes(" + e.getClass().getName() + ")", e.getMessage());
		}
	}
	/*
	 * @see javax.jms.StreamMessage#readChar()
	 */
	@Override
	public char readChar () throws JMSException
	{
		final ObjectInputStream	in=getInputStream(true);
		try
		{
			return in.readChar();
		}
		catch(IOException e)
		{
			throw new JMSException("readChar(" + e.getClass().getName() + ")", e.getMessage());
		}
	}
	/*
	 * @see javax.jms.StreamMessage#readDouble()
	 */
	@Override
	public double readDouble () throws JMSException
	{
		final ObjectInputStream	in=getInputStream(true);
		try
		{
			return in.readDouble();
		}
		catch(IOException e)
		{
			throw new JMSException("readDouble(" + e.getClass().getName() + ")", e.getMessage());
		}
	}
	/*
	 * @see javax.jms.StreamMessage#readFloat()
	 */
	@Override
	public float readFloat () throws JMSException
	{
		final ObjectInputStream	in=getInputStream(true);
		try
		{
			return in.readFloat();
		}
		catch(IOException e)
		{
			throw new JMSException("readFloat(" + e.getClass().getName() + ")", e.getMessage());
		}
	}
	/*
	 * @see javax.jms.StreamMessage#readInt()
	 */
	@Override
	public int readInt () throws JMSException
	{
		final ObjectInputStream	in=getInputStream(true);
		try
		{
			return in.readInt();
		}
		catch(IOException e)
		{
			throw new JMSException("readInt(" + e.getClass().getName() + ")", e.getMessage());
		}
	}
	/*
	 * @see javax.jms.StreamMessage#readLong()
	 */
	@Override
	public long readLong () throws JMSException
	{
		final ObjectInputStream	in=getInputStream(true);
		try
		{
			return in.readLong();
		}
		catch(IOException e)
		{
			throw new JMSException("readLong(" + e.getClass().getName() + ")", e.getMessage());
		}
	}
	/*
	 * @see javax.jms.StreamMessage#readObject()
	 */
	@Override
	public Object readObject () throws JMSException
	{
		final ObjectInputStream	in=getInputStream(true);
		try
		{
			return in.readObject();
		}
		catch(IOException e)
		{
			throw new JMSException("readObject(" + e.getClass().getName() + ")", e.getMessage());
		}
		catch(ClassNotFoundException e)
		{
			throw new JMSException("readObject(" + e.getClass().getName() + ")", e.getMessage());
		}
	}
	/*
	 * @see javax.jms.StreamMessage#readShort()
	 */
	@Override
	public short readShort () throws JMSException
	{
		final ObjectInputStream	in=getInputStream(true);
		try
		{
			return in.readShort();
		}
		catch(IOException e)
		{
			throw new JMSException("readShort(" + e.getClass().getName() + ")", e.getMessage());
		}
	}
	/*
	 * @see javax.jms.StreamMessage#readString()
	 */
	@Override
	public String readString () throws JMSException
	{
		final ObjectInputStream	in=getInputStream(true);
		try
		{
			return in.readUTF();
		}
		catch(IOException e)
		{
			throw new JMSException("readString(" + e.getClass().getName() + ")", e.getMessage());
		}
	}
	/*
	 * @see javax.jms.StreamMessage#writeBoolean(boolean)
	 */
	@Override
	public void writeBoolean (boolean value) throws JMSException
	{
		final ObjectOutputStream	out=getOutputStream(true);
		try
		{
			out.writeBoolean(value);
		}
		catch(IOException e)
		{
			throw new JMSException("writeBoolean(" + e.getClass().getName() + ")", e.getMessage());
		}
	}
	/*
	 * @see javax.jms.StreamMessage#writeByte(byte)
	 */
	@Override
	public void writeByte (byte value) throws JMSException
	{
		final ObjectOutputStream	out=getOutputStream(true);
		try
		{
			out.writeByte(value);
		}
		catch(IOException e)
		{
			throw new JMSException("writeByte(" + e.getClass().getName() + ")", e.getMessage());
		}
	}
	/*
	 * @see javax.jms.StreamMessage#writeBytes(byte[], int, int)
	 */
	@Override
	public void writeBytes (byte[] value, int offset, int length)
			throws JMSException
	{
		final ObjectOutputStream	out=getOutputStream(true);
		try
		{
			out.write(value, offset, length);
		}
		catch(IOException e)
		{
			throw new JMSException("writeBytes(" + e.getClass().getName() + ")", e.getMessage());
		}

	}
	/*
	 * @see javax.jms.StreamMessage#writeBytes(byte[])
	 */
	@Override
	public void writeBytes (byte[] value) throws JMSException
	{
		writeBytes(value, 0, value.length);
	}
	/*
	 * @see javax.jms.StreamMessage#writeChar(char)
	 */
	@Override
	public void writeChar (char value) throws JMSException
	{
		final ObjectOutputStream	out=getOutputStream(true);
		try
		{
			out.writeChar(value);
		}
		catch(IOException e)
		{
			throw new JMSException("writeChar(" + e.getClass().getName() + ")", e.getMessage());
		}
	}
	/*
	 * @see javax.jms.StreamMessage#writeDouble(double)
	 */
	@Override
	public void writeDouble (double value) throws JMSException
	{
		final ObjectOutputStream	out=getOutputStream(true);
		try
		{
			out.writeDouble(value);
		}
		catch(IOException e)
		{
			throw new JMSException("writeDouble(" + e.getClass().getName() + ")", e.getMessage());
		}
	}
	/*
	 * @see javax.jms.StreamMessage#writeFloat(float)
	 */
	@Override
	public void writeFloat (float value) throws JMSException
	{
		final ObjectOutputStream	out=getOutputStream(true);
		try
		{
			out.writeFloat(value);
		}
		catch(IOException e)
		{
			throw new JMSException("writeFloat(" + e.getClass().getName() + ")", e.getMessage());
		}
	}
	/*
	 * @see javax.jms.StreamMessage#writeInt(int)
	 */
	@Override
	public void writeInt (int value) throws JMSException
	{
		final ObjectOutputStream	out=getOutputStream(true);
		try
		{
			out.writeInt(value);
		}
		catch(IOException e)
		{
			throw new JMSException("writeInt(" + e.getClass().getName() + ")", e.getMessage());
		}
	}
	/*
	 * @see javax.jms.StreamMessage#writeLong(long)
	 */
	@Override
	public void writeLong (long value) throws JMSException
	{
		final ObjectOutputStream	out=getOutputStream(true);
		try
		{
			out.writeLong(value);
		}
		catch(IOException e)
		{
			throw new JMSException("writeLong(" + e.getClass().getName() + ")", e.getMessage());
		}
	}
	/*
	 * @see javax.jms.StreamMessage#writeObject(java.lang.Object)
	 */
	@Override
	public void writeObject (Object value) throws JMSException
	{
		final ObjectOutputStream	out=getOutputStream(true);
		try
		{
			out.writeObject(value);
		}
		catch(IOException e)
		{
			throw new JMSException("writeObject(" + e.getClass().getName() + ")", e.getMessage());
		}
	}
	/*
	 * @see javax.jms.StreamMessage#writeShort(short)
	 */
	@Override
	public void writeShort (short value) throws JMSException
	{
		final ObjectOutputStream	out=getOutputStream(true);
		try
		{
			out.writeShort(value);
		}
		catch(IOException e)
		{
			throw new JMSException("writeShort(" + e.getClass().getName() + ")", e.getMessage());
		}
	}
	/*
	 * @see javax.jms.StreamMessage#writeString(java.lang.String)
	 */
	@Override
	public void writeString (String value) throws JMSException
	{
		final ObjectOutputStream	out=getOutputStream(true);
		try
		{
			out.writeUTF(value);
		}
		catch(IOException e)
		{
			throw new JMSException("writeString(" + e.getClass().getName() + ")", e.getMessage());
		}
	}
	/*
	 * @see javax.jms.StreamMessage#reset()
	 */
	@Override
	public void reset () throws JMSException
	{
		try
		{
			byte[]	outData=closeOutputStream();
			if (null == outData)
				outData = new byte[0];
			
			_inpBytes = new ByteArrayInputStream(outData);
			_input = new ObjectInputStream(_inpBytes);
		}
		catch(IOException e)
		{
			throw new JMSException("reset(" + e.getClass().getName() + ")", e.getMessage());
		}
	}
	/*
	 * @see javax.jms.Message#clearBody()
	 */
	@Override
	public void clearBody () throws JMSException
	{
		IOException	exc=null;
		try
		{
			closeInputStream();
		}
		catch(IOException e)
		{
			exc = e;
		}

		try
		{
			closeOutputStream();
		}
		catch(IOException e)
		{
			exc = e;
		}
		
		if (null == exc)
		{
			_outBytes = new ByteArrayOutputStream(256);

			try
			{
				_output = new ObjectOutputStream(_outBytes);
			}
			catch(IOException e)
			{
				exc = e;
				_outBytes = null;
			}
		}

		if (exc != null)
			throw new JMSException("clearBody(" + exc.getClass().getName() + ")", exc.getMessage());
	}
}
