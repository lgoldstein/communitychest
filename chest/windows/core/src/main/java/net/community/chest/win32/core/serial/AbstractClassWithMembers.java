/*
 * 
 */
package net.community.chest.win32.core.serial;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StreamCorruptedException;

import net.community.chest.CoVariantReturn;
import net.community.chest.reflect.ClassUtil;
import net.community.chest.util.compare.AbstractComparator;

/**
 * <P>Copyright as per GPLv2</P>
 * @author Lyor G.
 * @since Feb 5, 2013 10:09:31 AM
 *
 */
public abstract class AbstractClassWithMembers extends SerializationRecord implements ObjectIdCarrier {
	private static final long serialVersionUID = -5075933922981362514L;

	private ClassInfo	_classInfo;

	protected AbstractClassWithMembers (RecordTypeEnumeration recordType)
	{
		super(recordType);
	}

	public ClassInfo getClassInfo ()
	{
		return _classInfo;
	}

	public void setClassInfo (ClassInfo classInfo)
	{
		_classInfo = classInfo;
	}

	@Override
	public long getObjectId ()
	{
		ClassInfo	info=getClassInfo();
		if (info == null)
			throw new IllegalStateException("No class info available");
		return info.getObjectId();
	}

	@Override
	public void setObjectId (long objectId)
	{
		ClassInfo	info=getClassInfo();
		if (info == null)
			throw new IllegalStateException("No class info available");
		info.setObjectId(objectId);
	}

	@Override
	@CoVariantReturn
	public AbstractClassWithMembers read (InputStream in) throws IOException
	{
		return getClass().cast(super.read(in));
	}

	@Override
	public void readRecordData (InputStream in) throws IOException
	{
		ClassInfo	classInfo=new ClassInfo(in);
		setClassInfo(classInfo);
	}

	@Override
	public void writeRecordData (OutputStream out) throws IOException
	{
		ClassInfo	classInfo=getClassInfo();
		if (classInfo == null)
			throw new StreamCorruptedException("No class info");
		classInfo.write(out);
	}

	@Override
	@CoVariantReturn
	public AbstractClassWithMembers clone () throws CloneNotSupportedException
	{
		AbstractClassWithMembers	other=getClass().cast(super.clone());
		ClassInfo	classInfo=getClassInfo();
		if (classInfo != null)
			other.setClassInfo(classInfo.clone());
		return other;
	}

	@Override
	public int hashCode ()
	{
		return super.hashCode()
			+ ClassUtil.getObjectHashCode(getClassInfo())
			;
	}
	
	@Override
	public boolean equals (Object obj)
	{
		if (!super.equals(obj))
			return false;
		if (this == obj)
			return true;

		AbstractClassWithMembers	other=(AbstractClassWithMembers) obj;
		if (AbstractComparator.compareObjects(getClassInfo(), other.getClassInfo()))
			return true;
		else
			return false;
	}

	@Override
	public String toString ()
	{
		return super.toString()
			+ ";class=" + getClassInfo()
			;
	}
}
