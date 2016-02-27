package net.community.chest.tools.javadoc.mbean;

import net.community.chest.tools.javadoc.ClassMethodsMap;
import net.community.chest.tools.javadoc.DocletErrReporter;
import net.community.chest.tools.javadoc.DocletErrReporterHelper;

import com.sun.javadoc.ClassDoc;
import com.sun.javadoc.MethodDoc;
import com.sun.javadoc.Parameter;
import com.sun.javadoc.Type;

/**
 * <P>Copyright 2007 as per GPLv2</P>
 *
 * <P>Processes a classes methods, retrieving its attributes and operations</P>
 * 
 * @author Lyor G.
 * @since Aug 16, 2007 11:27:41 AM
 */
public class MBeanMethodsHandler extends DocletErrReporterHelper {
	/**
	 * @param reporter reporter to be used - may NOT be null
	 * @throws IllegalArgumentException if bad/illegal parameters
	 */
	public MBeanMethodsHandler (final DocletErrReporter reporter)
		throws IllegalArgumentException
	{
		super(reporter);

		if (null == reporter)	// should not happen
			throw new IllegalArgumentException("Bad/illegal handler parameteres");
	}
	/**
	 * @param mapName updated map name - used for throwing exceptions
	 * @param mm original map - if null, one is created, otherwise it is used
	 * @param md method to be mapped - may NOT be null
	 * @return updated map (null if error)
	 * @throws IllegalArgumentException if null method to map
	 * @throws IllegalStateException if method already mapped
	 */
	public static final ClassMethodsMap updateMap (final String mapName, final ClassMethodsMap mm, final MethodDoc md)
		throws IllegalArgumentException, IllegalStateException
	{
		if (null == md)
			throw new IllegalArgumentException("updateMap(" + mapName + ") no method to update");

		if (null == mm)
		{
			final ClassMethodsMap	zz=new ClassMethodsMap();
			zz.put(md);
			return zz;
		}
		else	// have an existing map - make sure unique entry
		{
			if (mm.put(md) != null)
				throw new IllegalStateException("updateMap(" + mapName + ") method already mapped: " + md);
			return mm;
		}
	}
	// cached internal information
	private ClassMethodsMap	_attrs	/* =null */, _opers /* =null */;
	/**
	 * Resets extracted attributes/operations map(s) - if any created
	 */
	public void reset ()
	{
		if (_attrs != null)
			_attrs.clear();
		if (_opers != null)
			_opers.clear();
	}
	/**
	 * @return attributes map- key=string(method key), value={@link MethodDoc}
	 * Note: may be null/empty if no attributes or no processing took place yet
	 * (or {@link #reset()}
	 */
	public ClassMethodsMap getAttributes ()
	{
		return _attrs;
	}
	/**
	 * Adds the specified method to the attributes map
	 * @param md method to be added - may NOT be null (not checked to see
	 * if it is indeed an attribute method)
	 * @return added method
	 * @throws IllegalArgumentException if null method to map
	 * @throws IllegalStateException if method already mapped
	 * @see #isGetter(MethodDoc)
	 * @see #isSetter(MethodDoc)
	 */
	public MethodDoc updateAttribute (final MethodDoc md) throws IllegalArgumentException, IllegalStateException
	{
		_attrs = updateMap("attributes", getAttributes(), md);
		return md;
	}
	/**
	 * @return operations map- key=string(method key), value={@link MethodDoc}
	 * Note: may be null/empty if no operations or no processing took place yet
	 * (or {@link #reset()}
	 */
	public ClassMethodsMap getOperations ()
	{
		return _opers;
	}
	/**
	 * Adds the specified method to the operations map
	 * @param md method to be added - may NOT be null
	 * @return added method
	 * @throws IllegalArgumentException if null method to map
	 * @throws IllegalStateException if method already mapped
	 */
	public MethodDoc updateOperation (final MethodDoc md) throws IllegalArgumentException, IllegalStateException
	{
		_opers = updateMap("operations", getOperations(), md);
		return md;
	}
	/**
	 * attribute(s) method(s) prefixes 
	 */
	private static final String[]	_attrsPrefixes={ "get", "is", "set" };
	/**
	 * @param mName method name - may NOT be null/empty
	 * @param ad attribute descriptor to be updated - may NOT be null
	 * @return method descriptor - null/empty if error
	 */
	public static final AttrDescriptor getAttributeDescriptor (final String mName, final AttrDescriptor ad)
	{
		final int	mnLen=(null == mName) /* should not happen */ ? 0 : mName.length();
		if ((mnLen <= 0) || (null == ad))
			return null;

		for (int	pIndex=0; pIndex < _attrsPrefixes.length; pIndex++)
		{
			final String	pValue=_attrsPrefixes[pIndex];
			final int		pnLen=(null == pValue) /* should not happen */ ? 0 : pValue.length();
			if (pnLen <= 0)	// should not happen
				continue;	

			// make sure not only starts with prefix but also has some attribute name
			if (mName.startsWith(pValue) && (mnLen > pnLen))
			{
				ad.setPrefix(pValue);
				ad.setName(mName.substring(pnLen));
				ad.setGetter(!"set".equals(pValue));
				return ad;
			}
		}

		return null;
	}
	/**
	 * @param mName method name - may NOT be null/empty
	 * @return method descriptor - null/empty if error
	 */
	public static final AttrDescriptor getAttributeDescriptor (final String mName)
	{
		return getAttributeDescriptor(mName, new AttrDescriptor()); 
	}
	/**
	 * @param md method info - may NOT be null
	 * @param ad attribute descriptor to be updated - may NOT be null
	 * @return method descriptor - null/empty if error
	 */
	public static final AttrDescriptor getAttributeDescriptor (final MethodDoc md, final AttrDescriptor ad)
	{
		return (null == md) ? null : getAttributeDescriptor(md.name(), ad); 
	}
	/**
	 * @param md method info - may NOT be null
	 * @return method descriptor - null/empty if error
	 */
	public static final AttrDescriptor getAttributeDescriptor (final MethodDoc md)
	{
		return (null == md) ? null : getAttributeDescriptor(md.name()); 
	}
	/**
	 * @param md method to be checked - may NOT be null
	 * @return TRUE if this is a getter method
	 * @throws IllegalArgumentException if null method object/name
	 */
	public static boolean isGetter (final MethodDoc md) throws IllegalArgumentException
	{
		final String	name=(null == md) /* should not happen */ ? null : md.name();
		final int		nLen=(null == name) /* should not happen */ ? 0 : name.length();
		if (nLen <= 0)
			throw new IllegalArgumentException("isGetter(" + md + ") bad/illegal parameter");

		// static methods are not JavaBean getter(s)
		if (md.isStatic())
			return false;

		// make sure name starts with "is" or "get" - taking into account the length...
		final boolean	isPrefix=name.startsWith("is") && (nLen > 2),
						getPrefix=name.startsWith("get") && (nLen > 3);
		if (isPrefix || getPrefix)
		{
			final Parameter[]	params=md.parameters();
			if ((params != null) && (params.length > 0))
				return false;	// getter cannot have any parameters

			// make sure return type is NOT void
			final Type		rt=md.returnType();
			final String	rn=(null == rt) /* should not happen */ ? null : rt.qualifiedTypeName();
			if ((null == rn) || (rn.length() <= 0)	// should not happen
			 || Void.TYPE.getName().equals(rn)
			 || Void.class.getName().equals(rn))
				return false;

			// for "is" make sure it returns boolean
			if (isPrefix)
				return Boolean.TYPE.getName().equals(rn)
					|| Boolean.class.getName().equals(rn)
					;

			// TODO (?) check that does not throw checked exceptions...
			return true;
		}

		return false;
	}
	/**
	 * @param md method to be checked - may NOT be null
	 * @return TRUE if this is a setter method
	 * @throws IllegalArgumentException if null method object/name
	 */
	public static boolean isSetter (final MethodDoc md) throws IllegalArgumentException
	{
		final String	name=(null == md) /* should not happen */ ? null : md.name();
		final int		nLen=(null == name) /* should not happen */ ? 0 : name.length();
		if (nLen <= 0)
			throw new IllegalArgumentException("isSetter(" + md + ") bad/illegal parameter");

		// static methods are not JavaBean setter(s)
		if (md.isStatic())
			return false;

		if (name.startsWith("set") && (nLen > 3))
		{
			final Parameter[]	params=md.parameters();
			if ((null == params) || (params.length != 1))
				return false;	// setter MUST have EXACTLY ONE parameter
			
			// make sure return type IS void
			final Type		rt=md.returnType();
			final String	rn=(null == rt) /* should not happen */ ? null : rt.qualifiedTypeName();
			return Void.TYPE.getName().equals(rn)
			 	// TODO (?) || Void.class.getName().equals(rn)
				;
		}

		return false;
	}
	/**
	 * @param methods methods to be processed - may be null/empty
	 * @return 0 if successful (<B>Note:</B> does NOT {@link #reset()} automatically)
	 */
	public int processMethods (final MethodDoc[] methods)
	{
		final int	numMethods=(null == methods) /* OK - but unusual */ ? 0 : methods.length;
		for (int	mIndex=0; mIndex < numMethods; mIndex++)
		{
			final MethodDoc	m=methods[mIndex];
			if (null == m)	// should not happen
				continue;

			// we don't issue a warning to allow in the future handling of class methods and not just interface ones
			if (m.isStatic() || (!m.isPublic()))
			{
				printDebug("processMethods(" + m.qualifiedName() + ") static=" + m.isStatic() + "public=" + m.isPublic() + " - skipped");
				continue;
			}

			try
			{
				if (isGetter(m) /* ask first - more likely */ || isSetter(m))
					updateAttribute(m);
				else
					updateOperation(m);
			}
			catch(Exception e)
			{
				return printErrorCode("processMethods(" + m.qualifiedName() + ") " + e.getClass().getName() + " on update method: " + e.getMessage(), Integer.MAX_VALUE);
			}
		}

		return 0;
	}
	/**
	 * @param cd class/interface whose methods we want to process
	 * @return 0 if successful
	 */
	public int processMethods (final ClassDoc cd)
	{
		if (null == cd)	// should not happen
			return Integer.MIN_VALUE;
		else
			return processMethods(cd.methods());
	}
}
