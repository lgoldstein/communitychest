/*
 * 
 */
package net.community.chest.io.serial;

import java.io.EOFException;
import java.io.Externalizable;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectStreamConstants;
import java.io.Serializable;
import java.io.StreamCorruptedException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Stack;
import java.util.concurrent.atomic.AtomicInteger;

import net.community.chest.io.file.FileIOUtils;

/**
 * Parses a serialized object data
 * <P>Copyright as per GPLv2</P>
 * @author Lyor G.
 * @since Aug 22, 2012 9:06:59 AM
 * @see <A HREF="http://docs.oracle.com/javase/7/docs/platform/serialization/spec/protocol.html#10258">Grammar for stream format</A>
 */
public abstract class SerialDataParser extends FilterInputStream {
	private final StringBuilder	contentId=new StringBuilder();
	private final Stack<SerializedClassDescriptor> descriptors=new Stack<SerializedClassDescriptor>();
	private final AtomicInteger	refCount=new AtomicInteger(0);
	private final List<Object>	refs=new ArrayList<Object>();

	/**
	 * @param inStream The actual {@link InputStream} to use
	 */
	protected SerialDataParser (InputStream inStream) {
		super(inStream);
		
		if (inStream == null) {
			throw new IllegalStateException("No input stream provided");
		}
	}

	protected final String getCurrentContentId () {
		return (contentId.length() > 0) ? contentId.toString() : "";
	}

	protected int makeRef(Object o) {
        refs.add(o);
        final int	refValue=refCount.incrementAndGet();
        return refValue - 1;
    }
    
    protected Object getRef(final int refValue) throws IOException {
        int idx = refValue - ObjectStreamConstants.baseWireHandle;
        if ((idx < 0) || (idx >= refCount.get()))
        	throw new StreamCorruptedException("getRef(" + refValue + ") Invalid reference");

        Object o = refs.get(idx);
        if (o == null)
        	throw new StreamCorruptedException("getRef(" + refValue + ") no referenced object");
        return o;
    }

	/**
	 * Parses the provided {@link InputStream}
	 * @return <code>true</code> if parsing executed all the way,
	 * <code>false</code> if aborted by one of the event methods
	 * @throws IOException
	 */
	public boolean parse () throws IOException {
		short	magic=readShort();
		short	version=readShort();

		if (!startParsing(magic, version)) {
			return false;
		}

		if (!parseContent()) {
			return false;
		}

		if (!endParsing(magic, version)) {
			return false;
		}

		return true;
	}

	protected boolean parseContent () throws IOException {
		for (int	contentIndex=0, idLen=contentId.length(); ; contentIndex++) {
			try {
				if (idLen > 0) {
					contentId.append('.');
				}
				contentId.append(contentIndex);

				if (!startContent()) {
					return false;
				}

				final byte		tcValue=readByte();
				switch(tcValue) {
					case ObjectStreamConstants.TC_OBJECT	:
						if (!parseNewObject()) {
							return false;
						}
						break;

					case ObjectStreamConstants.TC_CLASS 	:
						if (!parseClass()) {
							return false;
						}
						break;

					case ObjectStreamConstants.TC_NULL		:
						if (!parseNull()) {
							return false;
						}
						break;

					case ObjectStreamConstants.TC_REFERENCE		:
						if (!parseReference()) {
							return false;
						}
						break;

					case ObjectStreamConstants.TC_STRING	: {
							String	str=readUTF();
							if (str != null) {
								throw new StreamCorruptedException("parseContent(" + contentId + ") unexpected string: " + str);
							}
						}
						break;

					case ObjectStreamConstants.TC_BLOCKDATA :
						if (!parseBlockDataShort()) {
							return false;
						}

						break;

					case ObjectStreamConstants.TC_BLOCKDATALONG :
						if (!parseBlockDataLong()) {
							return false;
						}

						break;

					case ObjectStreamConstants.TC_ENDBLOCKDATA	:
						if (!endContent()) {
							return false;
						}
						return true;

					default	:
						throw new StreamCorruptedException("parseContent(" + contentId + ")"
														 + "Unknown content type: 0x" + Integer.toHexString(tcValue & 0x00FF));
				}
			} finally {
				contentId.setLength(idLen);
			}
		}
	}

	protected boolean parseNewObject () throws IOException {
		if (!startObject()) {
			return false;
		}
		
		if (!parseClassDescriptor()) {
			return false;
		}

		makeRef(Object.class);	// TODO find a better placeholder

		if (!endObject()) {
			return false;
		}
		
		return true;
	}

	protected boolean parseClassDescriptor () throws IOException {
		if (!startClassDescriptor()) {
			return false;
		}

		final byte	tcValue=readByte();
		switch(tcValue) {
			case ObjectStreamConstants.TC_CLASSDESC 		:
				if (!parseClassInfo()) {
					return false;
				}
				break;

			case ObjectStreamConstants.TC_PROXYCLASSDESC	:
				if (!parseProxyInfo()) {
					return false;
				}
				break;

			case ObjectStreamConstants.TC_NULL				:
				if (!parseNull()) {
					return false;
				}
				break;

			case ObjectStreamConstants.TC_REFERENCE			:
				if (!parseReference()) {
					return false;
				}
				break;

			default	:
				throw new StreamCorruptedException("Unknown class descriptor type: 0x" + Integer.toHexString(tcValue & 0x00FF));
		}

		if (!endClassDescriptor()) {
			return false;
		}
		
		return true;
	}

	protected boolean parseClass () throws IOException {
		throw new StreamCorruptedException("parseClass - TODO");
	}

	protected boolean parseProxyInfo () throws IOException {
//		final int	refHandle=in.readInt();
		throw new StreamCorruptedException("parseProxyInfo - TODO");
	}

	protected boolean parseClassInfo () throws IOException {
		final String					className=readUTF();
		final long						serialVersionUID=readLong();
		final byte						flags=readByte();
		final SerializedClassDescriptor	info=
				new SerializedClassDescriptor(className, serialVersionUID, SerializedClassFlag.fromMask(flags));
		makeRef(info);

		descriptors.push(info);
		try {
			if (!startClassInfo(info)) {
				return false;
			}

			if (!parseClassDescInfo(info)) {
				return false;
			}

			Collection<SerializedClassFlag>	classFlags=info.getFlags();
			if (classFlags.contains(SerializedClassFlag.SERIALIZABLE)) {
				if (classFlags.contains(SerializedClassFlag.WRITEMETHOD)) {
					if (!parseWrClass(info))
						return false;
				} else {
					if (!parseNoWrClass(info))
						return false;
				}
			} else if (classFlags.contains(SerializedClassFlag.EXTERNALIZABLE)) {
				if (classFlags.contains(SerializedClassFlag.BLOCKDATA)) {
					if (!parseObjectAnnotation(info)) {
						return false;
					}
				} else {
					if (!parseExternalContents(info)) {
						return false;
					}
				}
			} else {
				throw new StreamCorruptedException("parseClassInfo(" + info + ")"
												 + " neither " + Serializable.class.getSimpleName()
												 + " nor " + Externalizable.class.getSimpleName());
			}
			if (!endClassInfo(info)) {
				return false;
			}
		
			return true;
		} finally {
			SerializedClassDescriptor	popped=descriptors.pop();
			if (popped != info) {
				throw new StreamCorruptedException("Mismatched class descriptors: expected=" + info + ", actual=" + popped);
			}
		}
	}

	protected boolean parseWrClass (SerializedClassDescriptor info) throws IOException {
		if (!parseNoWrClass(info)) {
			return false;
		}

		if (!parseObjectAnnotation(info)) {
			return false;
		}

		return true;
	}

	protected boolean parseNoWrClass (SerializedClassDescriptor info) throws IOException {
		return parseValues(info);
	}

	protected boolean parseObjectAnnotation (SerializedClassDescriptor info) throws IOException {
		return parseContent();
	}

	protected boolean parseExternalContents (SerializedClassDescriptor info) throws IOException {
		throw new StreamCorruptedException("parseExternalContents(" + info + ")");
	}

	protected boolean parseValues (SerializedClassDescriptor info) throws IOException {
		return parseValues(info.getClassName(), info.getFields());
	}

	protected boolean parseValues (String className, Collection<? extends SerialFieldDescriptor> fields) throws IOException {
		if ((fields == null) || fields.isEmpty()) {
			return true;
		}

		for (SerialFieldDescriptor field : fields) {
			if (!parseFieldValue(className, field)) {
				return false;
			}
		}

		return true;
	}

	protected boolean parseFieldValue (String className, SerialFieldDescriptor field) throws IOException {
		return parseFieldValue(className, field.getFieldName(), field.getTypeDescriptor(), field.getFieldType());
	}

	protected boolean parseFieldValue (String className, String fieldName, FieldTypeDescriptor typeDescriptor, String fieldType) throws IOException {
		switch(typeDescriptor) {
			case BYTE:
				return handleFieldValue(className, fieldName, typeDescriptor, Byte.valueOf(readByte()));
			case SHORT:
				return handleFieldValue(className, fieldName, typeDescriptor, Short.valueOf(readShort()));
			case INTEGER:
				return handleFieldValue(className, fieldName, typeDescriptor, Integer.valueOf(readInt()));
			case LONG:
				return handleFieldValue(className, fieldName, typeDescriptor, Long.valueOf(readLong()));
			case FLOAT:
				return handleFieldValue(className, fieldName, typeDescriptor, Float.valueOf(readFloat()));
			case DOUBLE:
				return handleFieldValue(className, fieldName, typeDescriptor, Double.valueOf(readDouble()));
			case CHAR:
				return handleFieldValue(className, fieldName, typeDescriptor, Character.valueOf(readUNICODEChar()));
			case BOOLEAN:
				return handleFieldValue(className, fieldName, typeDescriptor, Boolean.valueOf(readBoolean()));

			case OBJECT:
			case ARRAY :
				if (!startCompoundFieldValue(className, fieldName, fieldType, typeDescriptor)) {
					return false;
				}

				if (!parseContent()) {
					return false;
				}

				if (!endCompoundFieldValue(className, fieldName, fieldType, typeDescriptor)) {
					return false;
				}

				return true;

			default	   :
				throw new StreamCorruptedException("parseFieldValue(" + className + "#" + fieldName + ")[" + fieldType + "] unknown descriptor:" + typeDescriptor);
		}
	}

	protected boolean parseClassDescInfo (SerializedClassDescriptor info) throws IOException {
		if (!parseClassFields(info)) {
			return false;
		}
		
		if (!parseClassAnnotations(info)) {
			return false;
		}

		if (!parseClassDescriptor()) {
			return false;
		}
		
		return true;
	}

	protected boolean parseClassAnnotations (SerializedClassDescriptor info) throws IOException {
		if (!startClassAnnotations(info)) {
			return false;
		}

		if (!parseContent()) {
			return false;
		}

		if (!endClassAnnotations(info)) {
			return false;
		}

		return true;
	}

	protected boolean parseClassFields (SerializedClassDescriptor info) throws IOException {
		final int numFields=readShort();
		if (numFields < 0) {
			throw new StreamCorruptedException("parseClassFields(" + info + ") Negative number of fields: " + numFields);
		}
		
		if (!startClassFields(info, numFields)) {
			return false;
		}

		for (int index=0; index < numFields; index++) {
			final char			typeCode=readASCIIChar();
			final String		fieldName=readUTF();
			FieldTypeDescriptor	fieldType=FieldTypeDescriptor.fromChar(typeCode);
			if (fieldType == null)
				throw new StreamCorruptedException("parseClassFields(" + info + ") unknown type code: " + String.valueOf(typeCode));

			final String	typeName;
			switch(fieldType) {
				case OBJECT	:
				case ARRAY	:	// read the actual type name
					typeName = readNewString();
					break;
				default	 :
					typeName = fieldType.getTypeName();
			}

			final SerialFieldDescriptor	field=new SerialFieldDescriptor(fieldName, typeName, fieldType, index);
			if (!handleFieldInfo(info, numFields, field)) {
				return false;
			}

			info.addField(field);
		}

		if (!endClassFields(info, numFields)) {
			return false;
		}
		
		return true;
	}

	protected boolean parseBlockDataShort () throws IOException {
		return parseBlockData(readByte() & 0x00FF);
	}
	
	protected boolean parseBlockDataLong () throws IOException {
		return parseBlockData(readInt());
	}

	protected boolean parseBlockData (int size) throws IOException {
		if (size < 0) {
			throw new StreamCorruptedException("parseBlockData(" + size + ") negative value N/A");
		}

		byte[]	buf=readFully(new byte[size]);
		if (!handleBlockData(buf, 0, size)) {
			return false;
		}

		return true;
	}

	protected boolean parseReference () throws IOException {
		final int	refHandle=readInt();
		Object 		refObject=getRef(refHandle);
		if (!reference(refHandle, refObject)) {
			return false;
		}
		
		return true;
	}
	
	protected boolean parseNull () throws IOException {
		if (!nullReference()) {
			return false;
		}

		return true;
	}

	protected boolean startParsing (short magic, short version) throws IOException {
		if (magic != ObjectStreamConstants.STREAM_MAGIC) {
			throw new StreamCorruptedException("Mismatched magic value:"
											 + " expected=" + ObjectStreamConstants.STREAM_MAGIC
											 + ", got=" + magic);
		}

		if (version != ObjectStreamConstants.STREAM_VERSION) {
			throw new StreamCorruptedException("Mismatched version value:"
					 + " expected=" + ObjectStreamConstants.STREAM_VERSION
					 + ", got=" + version);
		}

		return true;
	}

	protected boolean endParsing (short magic, short version) throws IOException {
		if (this.in == null) {
			throw new IOException("endParsing(" + magic + "/" + version + ") No current stream");
		}

		return true;
	}

	protected boolean startContent () throws IOException {
		if (this.in == null) {
			throw new IOException("No current stream");
		}

		return true;
	}

	protected boolean endContent () throws IOException {
		if (this.in == null) {
			throw new IOException("No current stream");
		}

		return true;
	}

	protected boolean handleBlockData (byte[] buf, int offset, int len) throws IOException {
		if ((buf == null) || (offset < 0) || (len < 0) || (buf.length < (offset+len))) {
			throw new StreamCorruptedException("handleBlockData(" + offset + " - " + (offset+len) + ") bad buffer");
		}

		return true;
	}

	protected boolean startClassAnnotations (SerializedClassDescriptor info) throws IOException {
		if (info == null) {
			throw new StreamCorruptedException("No class descriptor");
		}

		return true;
	}

	protected boolean endClassAnnotations (SerializedClassDescriptor info) throws IOException {
		if (info == null) {
			throw new StreamCorruptedException("No class descriptor");
		}

		return true;
	}

	protected boolean nullReference () throws IOException {
		if (this.in == null) {
			throw new IOException("No current stream");
		}

		return true;
	}

	protected boolean reference (int refHandle, Object refObject) throws IOException {
		if (this.in == null) {
			throw new IOException("reference(" + refHandle + ")[" + refObject + "] No current stream");
		}

		return true;
	}

	protected abstract boolean startClassDescriptor () throws IOException;
	protected abstract boolean endClassDescriptor () throws IOException;

	protected abstract boolean startClassInfo (SerializedClassDescriptor info) throws IOException;
	protected abstract boolean endClassInfo (SerializedClassDescriptor info) throws IOException;

	protected abstract boolean startClassFields (SerializedClassDescriptor info, int numFields) throws IOException;
	// NOTE: called BEFORE the field is added to the class descriptor
	protected abstract boolean handleFieldInfo(SerializedClassDescriptor info, int numFields, SerialFieldDescriptor field) throws IOException;

	protected boolean startCompoundFieldValue (String className, String fieldName, String fieldType, FieldTypeDescriptor typeDescriptor) throws IOException {
		if (this.in == null) {
			throw new IOException("startCompoundFieldValue(" + className + "#" + fieldName + ")[" + fieldType + "/" + typeDescriptor + ") No current stream");
		}

		return true;
	}
	
	protected boolean endCompoundFieldValue (String className, String fieldName, String fieldType, FieldTypeDescriptor typeDescriptor) throws IOException {
		if (this.in == null) {
			throw new IOException("endCompoundFieldValue(" + className + "#" + fieldName + ")[" + fieldType + "/" + typeDescriptor + ") No current stream");
		}

		return true;
	}

	// called only for primitive fields
	protected abstract boolean handleFieldValue(String className, String fieldName, FieldTypeDescriptor typeDescriptor, Serializable value) throws IOException;
	protected abstract boolean endClassFields (SerializedClassDescriptor info, int numFields) throws IOException;

	protected abstract boolean startObject () throws IOException;
	protected abstract boolean endObject () throws IOException;

	protected String readNewString () throws IOException {
		final byte	tcValue=readByte();
		switch(tcValue) {
			case ObjectStreamConstants.TC_STRING	:
				String	s=readUTF();
				makeRef(s);
				return s;

			case ObjectStreamConstants.TC_REFERENCE	: {
				int		refValue=readInt();
				Object	refString=getRef(refValue);
				if (!(refString instanceof String)) {
					throw new StreamCorruptedException("readNewString(" + refValue + "): Non string value for ref: " + refString.getClass().getName() + "/" + refString);
				}

				return refString.toString();
			}

			default	:
				throw new StreamCorruptedException("Unexpected new string type: 0x" + Integer.toHexString(tcValue & 0xFF));
		}
	}

	protected String readUTF () throws IOException {
		return readUTF(this.in);
	}

	protected double readDouble () throws IOException {
		return readDouble(this.in);
	}

	protected long readLong () throws IOException {
		return readLong(this.in);
	}

	protected float readFloat () throws IOException {
		return readFloat(this.in);
	}

	protected int readInt () throws IOException {
		return readInt(this.in);
	}

	protected char readUNICODEChar () throws IOException {
		return readUNICODEChar(this);
	}

	protected short readShort () throws IOException {
		return readShort(this.in);
	}

	protected char readASCIIChar () throws IOException {
		return readASCIIChar(this.in);
	}

	protected boolean readBoolean () throws IOException {
		return readBoolean(this.in);
	}

	protected byte readByte () throws IOException {
		return readByte(this.in);
	}

	protected byte[] readFully (byte[] buf) throws IOException {
		return readFully(buf, 0, buf.length);
	}

	protected byte[] readFully (byte[] buf, int offset, int len) throws IOException {
		FileIOUtils.readFully(this.in, buf, offset, len);
		return buf;
	}

	public static final String readNewString (InputStream in) throws IOException {
		final byte	tcValue=readByte(in);
		if (tcValue != ObjectStreamConstants.TC_STRING) {
			throw new StreamCorruptedException("Unexpected new string type: 0x" + Integer.toHexString(tcValue & 0xFF));
		}
	
		return readUTF(in);
	}

	public static final String readUTF (InputStream in) throws IOException {
        final short len=readShort(in);
        if (len < 0)
        	throw new StreamCorruptedException("readUTF(" + len + ") negative value N/A");

        if (len == 0) {
        	return "";
        }

        final byte[]	buf=new byte[len];
        FileIOUtils.readFully(in, buf);
        return new String(buf, "UTF-8");
    }
 
	public static final double readDouble (InputStream in) throws IOException {
		return Double.longBitsToDouble(readLong(in));
	}

	public static final long readLong (InputStream in) throws IOException {
        final long	x1=in.read(),
    				x2=(x1 == (-1L)) ? (-1L) : in.read(),
    				x3=(x2 == (-1L)) ? (-1L) : in.read(),
    				x4=(x3 == (-1L)) ? (-1L) : in.read(),
    				x5=(x4 == (-1L)) ? (-1L) : in.read(),
    				x6=(x5 == (-1L)) ? (-1L) : in.read(),
    				x7=(x6 == (-1L)) ? (-1L) : in.read(),
    				x8=(x7 == (-1L)) ? (-1L) : in.read();
    	if ((x1 == (-1)) || (x2 == (-1)) || (x3 == (-1)) || (x4 == (-1))) {
    		throw new EOFException("readLong(" + x1 + "/" + x2 + "/" + x3 + "/" + x4
    				 				+ x5 + "/" + x6 + "/" + x7 + "/" + x8 + ") EOF");
    	}

    	return (x1 << 56) + (x2 << 48) + (x3 << 40) + (x4 << 32) + (x5 << 24) + (x6 << 16) + (x7 << 8) + x8;
	}

	public static final float readFloat (InputStream in) throws IOException {
		return Float.intBitsToFloat(readInt(in));
	}

    public static final int readInt (InputStream in) throws IOException {
        final int	x1=in.read(),
        			x2=(x1 == (-1)) ? (-1) : in.read(),
           			x3=(x2 == (-1)) ? (-1) : in.read(),
           			x4=(x3 == (-1)) ? (-1) : in.read();
        if ((x1 == (-1)) || (x2 == (-1)) || (x3 == (-1)) || (x4 == (-1))) {
        	throw new EOFException("readInt(" + x1 + "/" + x2 + "/" + x3 + "/" + x4 + ") EOF");
        }

        return (x1 << 24) + (x2 << 16) + (x3 << 8) + x4;
    }

    public static final char readUNICODEChar (InputStream in) throws IOException {
    	return (char) readShort(in);
    }

    public static final short readShort (InputStream in) throws IOException {
		final int	x1=in.read(), x2=(x1 == (-1)) ? (-1) : in.read();
		if ((x1 == (-1)) || (x2 == (-1))) {
			throw new EOFException("readShort(" + x1 + "/" + x2 + ") EOF");
		}

		return (short) (((x1 << 8) + x2) & 0x0FFFF);
	}

    public static final char readASCIIChar (InputStream in) throws IOException {
    	byte	value=readByte(in);
    	return (char) (value & 0x00FF);
    }

    public static final boolean readBoolean (InputStream in) throws IOException {
    	return (1 == readByte(in));
    }

    public static final byte readByte (InputStream in) throws IOException {
		final int c=in.read();
		if (c == (-1)) {
			throw new EOFException("readByte() EOF");
		}
		
		return (byte) c;
	}
}
