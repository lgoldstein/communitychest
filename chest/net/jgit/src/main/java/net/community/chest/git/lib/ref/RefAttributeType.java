/*
 * 
 */
package net.community.chest.git.lib.ref;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.community.chest.CoVariantReturn;
import net.community.chest.lang.TypedValuesContainer;
import net.community.chest.util.collection.CollectionsUtils;

import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Ref.Storage;

/**
 * <P>Copyright as per GPLv2</P>
 * Encapsulates as an {@link Enum} all the attributes that a {@link Ref}
 * exposes - including itself. It also provides a {@link #getAttributeValue(Ref)}
 * method for extracting the specific attribute value
 * @author Lyor G.
 * @since Mar 15, 2011 8:46:58 AM
 */
public enum RefAttributeType implements TypedValuesContainer<Object> {
	REF(Ref.class) {
			/*
			 * @see net.community.chest.git.lib.RefAttributeType#getAttributeValue(org.eclipse.jgit.lib.Ref)
			 */
			@Override
			@CoVariantReturn
			public Ref getAttributeValue (Ref ref)
			{
				return ref;
			}
		},
	NAME(String.class) {
			/*
			 * @see net.community.chest.git.lib.RefAttributeType#getAttributeValue(org.eclipse.jgit.lib.Ref)
			 */
			@Override
			@CoVariantReturn
			public String getAttributeValue (Ref ref)
			{
				return (ref == null) ? null : ref.getName();
			}
		},
	SYMBOLIC(Boolean.class) {
			/*
			 * @see net.community.chest.git.lib.RefAttributeType#getAttributeValue(org.eclipse.jgit.lib.Ref)
			 */
			@Override
			@CoVariantReturn
			public Boolean getAttributeValue (Ref ref)
			{
				return (ref == null) ? null : Boolean.valueOf(ref.isSymbolic());
			}
		},
	LEAF(Ref.class) {
			/*
			 * @see net.community.chest.git.lib.RefAttributeType#getAttributeValue(org.eclipse.jgit.lib.Ref)
			 */
			@Override
			@CoVariantReturn
			public Ref getAttributeValue (Ref ref)
			{
				return (ref == null) ? null : ref.getLeaf();
			}
		},
	TARGET(Ref.class) {
			/*
			 * @see net.community.chest.git.lib.RefAttributeType#getAttributeValue(org.eclipse.jgit.lib.Ref)
			 */
			@Override
			@CoVariantReturn
			public Ref getAttributeValue (Ref ref)
			{
				return (ref == null) ? null : ref.getTarget();
			}
		},
	OBJID(ObjectId.class) {
			/*
			 * @see net.community.chest.git.lib.RefAttributeType#getAttributeValue(org.eclipse.jgit.lib.Ref)
			 */
			@Override
			@CoVariantReturn
			public ObjectId getAttributeValue (Ref ref)
			{
				return (ref == null) ? null : ref.getObjectId();
			}
		},
	PEELID(ObjectId.class) {
			/*
			 * @see net.community.chest.git.lib.RefAttributeType#getAttributeValue(org.eclipse.jgit.lib.Ref)
			 */
			@Override
			@CoVariantReturn
			public ObjectId getAttributeValue (Ref ref)
			{
				return (ref == null) ? null : ref.getPeeledObjectId();
			}
		},
	PEELED(Boolean.class) {
			/*
			 * @see net.community.chest.git.lib.RefAttributeType#getAttributeValue(org.eclipse.jgit.lib.Ref)
			 */
			@Override
			@CoVariantReturn
			public Boolean getAttributeValue (Ref ref)
			{
				return (ref == null) ? null : Boolean.valueOf(ref.isPeeled());
			}
		},
	STORAGE(Storage.class) {
			/*
			 * @see net.community.chest.git.lib.RefAttributeType#getAttributeValue(org.eclipse.jgit.lib.Ref)
			 */
			@Override
			@CoVariantReturn
			public Storage getAttributeValue (Ref ref)
			{
				return (ref == null) ? null : ref.getStorage();
			}
		};

	public abstract Object getAttributeValue (Ref ref);

	private final Class<?>	_attrType;
	/*
	 * @see net.community.chest.lang.TypedValuesContainer#getValuesClass()
	 */
	@SuppressWarnings("unchecked")
	@Override
	public Class<Object> getValuesClass ()
	{
		return (Class<Object>) _attrType;
	}

	private RefAttributeType (final Class<?> attrType)
	{
		if ((_attrType=attrType) == null)
			throw new IllegalStateException("No attribute type specified");
	}

	public static final List<RefAttributeType>	VALUES=Collections.unmodifiableList(Arrays.asList(values()));
	public static final RefAttributeType fromString (String s)
	{
		return CollectionsUtils.fromString(VALUES, s, false);
	}
	/**
	 * @param attrType A requested attribute type {@link Class}
	 * @return A {@link Set} of all the {@link RefAttributeType}-s whose
	 * {@link #getAttributeValue(Ref)} result is assignable to the specified
	 * type - may be <code>null</code>/empty if no match found
	 */
	public static final Set<RefAttributeType> getMatchingAttributes (final Class<?> attrType)
	{
		if (attrType == null)
			return null;

		Set<RefAttributeType>	typeSet=null;
		for (final RefAttributeType val : VALUES)
		{
			final Class<?>	valType=(val == null) ? null : val.getValuesClass();
			if ((valType == null) || (!attrType.isAssignableFrom(valType)))
				continue;

			if (typeSet == null)
				typeSet = EnumSet.of(val);
			else if (!typeSet.add(val))
				continue;	// debug breakpoint
		}

		return typeSet;
	}
	/**
	 * @param ref The {@link Ref}-erence whose attributes we want to extract
	 * @return A {@link Map} of whose key=the {@link RefAttributeType},
	 * value=the result of {@link #getAttributeValue(Ref)} on the reference
	 * provided result is non-<code>null</code>. <B>Note:</B> returned map may
	 * be <code>null</code>/empty if no attribute values extracted
	 */
	public static final Map<RefAttributeType,Object> getAttributes (final Ref ref)
	{
		if (ref == null)
			return null;

		Map<RefAttributeType,Object>	attrsMap=null;
		for (final RefAttributeType val : VALUES)
		{
			final Object	attrVal=(val == null) ? null : val.getAttributeValue(ref);
			if (attrVal == null)
				continue;
			
			if (attrsMap == null)
				attrsMap = new EnumMap<RefAttributeType,Object>(RefAttributeType.class);
	
			final Object	prevVal=attrsMap.put(val, attrVal);
			if (prevVal != null)	// should not happen
				continue;	// debug breakpoint
		}

		return attrsMap;
	}
}
