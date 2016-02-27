/*
 * 
 */
package net.community.chest.win32.core.serial;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StreamCorruptedException;
import java.util.Collection;

import net.community.chest.CoVariantReturn;
import net.community.chest.reflect.ClassUtil;
import net.community.chest.util.collection.CollectionsUtils;
import net.community.chest.util.compare.AbstractComparator;

/**
 * <P>Copyright as per GPLv2</P>
 * @author Lyor G.
 * @since Feb 5, 2013 10:10:11 AM
 *
 */
public abstract class AbstractClassWithMembersAndTypes extends AbstractClassWithMembers {
	private static final long serialVersionUID = -2222884094960210038L;

	private MemberTypeInfo	_memberTypeInfo;

	protected AbstractClassWithMembersAndTypes (RecordTypeEnumeration recordType)
	{
		super(recordType);
	}

	public MemberTypeInfo getMemberTypeInfo ()
	{
		return _memberTypeInfo;
	}

	public void setMemberTypeInfo (MemberTypeInfo memberTypeInfo)
	{
		_memberTypeInfo = memberTypeInfo;
	}

	@Override
	@CoVariantReturn
	public AbstractClassWithMembersAndTypes read (InputStream in) throws IOException
	{
		return getClass().cast(super.read(in));
	}

	@Override
	public void readRecordData (InputStream in) throws IOException
	{
		super.readRecordData(in);

		ClassInfo	classInfo=getClassInfo();
		if (classInfo == null)
			throw new StreamCorruptedException("No " + ClassInfo.class.getSimpleName() + " data");

		Collection<String>	namesList=classInfo.getMemberNames();
		setMemberTypeInfo(new MemberTypeInfo(CollectionsUtils.size(namesList), in));
		logInternal("memberTypeInfo=" + getMemberTypeInfo());
	}

	@Override
	public void writeRecordData (OutputStream out) throws IOException
	{
		super.writeRecordData(out);

		MemberTypeInfo	memberInfo=getMemberTypeInfo();
		if (memberInfo == null)
			throw new StreamCorruptedException("No member info");
		memberInfo.write(out);
	}

	@Override
	@CoVariantReturn
	public AbstractClassWithMembersAndTypes clone () throws CloneNotSupportedException
	{
		AbstractClassWithMembersAndTypes	other=getClass().cast(super.clone());
		MemberTypeInfo	memberInfo=getMemberTypeInfo();
		if (memberInfo != null)
			other.setMemberTypeInfo(memberInfo.clone());
		return other;
	}

	@Override
	public int hashCode ()
	{
		return super.hashCode()
			+ ClassUtil.getObjectHashCode(getMemberTypeInfo())
			;
	}

	@Override
	public boolean equals (Object obj)
	{
		if (!super.equals(obj))
			return false;
		if (this == obj)
			return true;

		AbstractClassWithMembersAndTypes	other=(AbstractClassWithMembersAndTypes) obj;
		if (AbstractComparator.compareObjects(getMemberTypeInfo(), other.getMemberTypeInfo()))
			return true;
		else
			return false;
	}

	@Override
	public String toString ()
	{
		return super.toString()
			+ ";members=" + getMemberTypeInfo()
			;
	}
}
