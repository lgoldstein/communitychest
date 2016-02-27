package net.community.chest.awt.border;

import java.awt.Color;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.TreeMap;

import javax.swing.BorderFactory;
import javax.swing.border.Border;

import net.community.chest.awt.dom.ConvUtil;
import net.community.chest.convert.ValueStringInstantiator;
import net.community.chest.dom.AbstractXmlValueStringInstantiator;
import net.community.chest.lang.StringUtil;
import net.community.chest.reflect.AttributeAccessor;
import net.community.chest.reflect.ClassUtil;
import net.community.chest.reflect.MethodsMap;

/**
 * <P>Copyright 2007 as per GPLv2</P>
 *
 * <P>String arguments are "Name(args)" where <I>Name</I> is one of the
 * <code>createName</code> methods of the {@link BorderFactory} (without
 * the <I>create</I> prefix) and the arguments are encoded using each
 * one's {@link ValueStringInstantiator} format - e.g., in order to call
 * {@link BorderFactory#createLineBorder(java.awt.Color, int)} one could
 * encode it as follows:</P></BR>
 * <UL>
 * 		<LI>LineBorder(rgb-value,width)</LI>
 * 		<LI>LineBorder(one of the known colors,width)</LI>
 * </UL>
 * <B>Note:</B> for <I>TitledBorder</I> the position and justification use
 * the {@link Enum} values strings.
 * 
 * @author Lyor G.
 * @since Nov 29, 2007 2:47:31 PM
 */
public class BorderValueInstantiator extends AbstractXmlValueStringInstantiator<Border> {
	public BorderValueInstantiator ()
	{
		super(Border.class);
	}
	/*
	 * @see net.community.chest.convert.ValueStringInstantiator#convertInstance(java.lang.Object)
	 */
	@Override
	public String convertInstance (Border inst) throws Exception
	{
		// TODO implement some kind of conversion
		throw new UnsupportedOperationException("convertInstance(" + inst + ") N/A");
	}
	// all factory creators start/end with this prefix/suffix
	public static final String	CREATOR_METHOD_PREFIX="create",
								CREATOR_METHOD_SUFFIX="border";
	public static final int	MIN_CREATOR_METHOD_NAME_LEN=
		CREATOR_METHOD_PREFIX.length() + CREATOR_METHOD_SUFFIX.length();

	public static final String getBorderTypeFromCreatorName (final String mName)
	{
		final int		mnLen=(null == mName) ? 0 : mName.length();
		// we want only "createSomehtingBorder" methods
		if (mnLen <= MIN_CREATOR_METHOD_NAME_LEN)
			return null;

		final String	prefix=mName.substring(0, CREATOR_METHOD_PREFIX.length()),
						suffix=mName.substring(mnLen-CREATOR_METHOD_SUFFIX.length());
		if ((!CREATOR_METHOD_PREFIX.equalsIgnoreCase(prefix))
		 || (!CREATOR_METHOD_SUFFIX.equalsIgnoreCase(suffix)))
			return null;

		// extract the "pure" border type
		return mName.substring(CREATOR_METHOD_PREFIX.length(), mnLen - CREATOR_METHOD_SUFFIX.length());
	}

	public static final Map<String,Collection<Method>> getCreatorsMap (Class<? extends BorderFactory> facClass) throws Exception
	{
		final Method[]	ma=(null == facClass) ? null : facClass.getMethods();
		if ((null == ma) || (ma.length <= 0))
			return null;

		Map<String,Collection<Method>>	cMap=null;
		for (final Method m : ma)
		{
			final int	mMod=m.getModifiers();
			if ((!Modifier.isPublic(mMod)) || (!Modifier.isStatic(mMod)))
				continue;	// we want only public + static methods

			final Class<?>	mRet=m.getReturnType();
			if ((null == mRet) || (!Border.class.isAssignableFrom(mRet)))
				continue;	// we want only creators of Border(s)

			final String	mName=m.getName(),
							mKey=getBorderTypeFromCreatorName(mName);
			if ((null == mKey) || (mKey.length() <= 0))
				continue;

			if (null == cMap)
				cMap = new TreeMap<String,Collection<Method>>(String.CASE_INSENSITIVE_ORDER);

			Collection<Method>	mList=cMap.get(mKey);
			if (null == mList)
			{
				mList = new LinkedList<Method>();
				cMap.put(mKey, mList);
			}
			mList.add(m);
		}

		return cMap;
	}

	private static Map<String,Collection<Method>>	_creatorsMap	/* =null */;
	public static final synchronized Map<String,Collection<Method>> getCreatorsMap () throws Exception
	{
		if (null == _creatorsMap)
			_creatorsMap = getCreatorsMap(BorderFactory.class);
		return _creatorsMap;
	}

	public static final Collection<? extends Method> getCreatorMethods (final String crName) throws Exception
	{
		if ((null == crName) || (crName.length() <= 0))
			return null;

		final Map<String,Collection<Method>>	crMap=getCreatorsMap();
		if ((null == crMap) || (crMap.size() <= 0))
			return null;

		return crMap.get(crName);
	}

	private static final Object getTitledBorderArgumentValue (
							final String						s,
							final String 						crName,
							final int							aIndex,
							final String						argVal,
							final ValueStringInstantiator<?>	inst) throws Exception
	{
		if (aIndex < 0)
			throw new IllegalArgumentException("getTitledBorderArgumentValue(" + crName + ")[" + s + "] bad argument index: " + aIndex);

		switch(aIndex)
		{
			case 2 	:	// justification
				return TitledBorderJustificationValueStringInstantiator.DEFAULT.newInstance(argVal);

			case 3 	:	// position
				return TitledBorderPositionValueStringInstantiator.DEFAULT.newInstance(argVal);

			default	:
				return inst.newInstance(argVal);
		}
	}

	private static final Object getBevelBorderArgumentValue (
						final String						s,
						final String 						crName,
						final int							aIndex,
						final String						argVal,
						final ValueStringInstantiator<?>	inst) throws Exception
	{
		if (0 == aIndex)
		{
			final BevelBorderType	t=BevelBorderType.fromString(argVal);
			if (null == t)
				throw new NoSuchElementException("getBevelBorderArgumentValue(" + s + ") unknown " + crName + " type value: " + argVal);

			return Integer.valueOf(t.getType());
		}

		return inst.newInstance(argVal);
	}


	private static final Object getEtchedBorderArgumentValue (
						final String						s,
						final String 						crName,
						final int							aIndex,
						final String						argVal,
						final ValueStringInstantiator<?>	inst) throws Exception
	{
		if (0 == aIndex)
		{
			final EtchedBorderType	t=EtchedBorderType.fromString(argVal);
			if (null == t)
				throw new NoSuchElementException("getEtchedBorderArgumentValue(" + s + ") unknown " + crName + " type value: " + argVal);

			return Integer.valueOf(t.getType());
		}

		return inst.newInstance(argVal);
	}

	private static MethodsMap<Constructor<?>>	_specialCtorsMap;
	private static final synchronized MethodsMap<Constructor<?>> getSpecialConstructorsMap ()
	{
		if (null == _specialCtorsMap)
			_specialCtorsMap = MethodsMap.createConstructorsMap();
		return _specialCtorsMap;
	}

	private static final Constructor<?> getSpecialConstructor (
			final BorderType bType, final Class<?> ... params) throws Exception
	{
		final Class<?>	bc=(null == bType) ? null : bType.getBorderClass();
		if (null == bc)
			return null;

		final MethodsMap<Constructor<?>>	cm=getSpecialConstructorsMap();
		Constructor<?>						ctor=null;
		synchronized(cm)
		{
			if (null == (ctor=cm.getByConstructor(bc , params)))
			{
				if (null == (ctor=bc.getConstructor(params)))
					throw new NoSuchMethodError("getSpecialConstructor(" + bType + ") no match found");
				cm.putByConstructor(bc, params, ctor);
			}
		}

		return ctor;
	}

	private static final Constructor<?> checkSpecialConstructor (
			final BorderType bType, final int numArgs) throws Exception
	{
		if (null == bType)
			return null;

		switch(bType)
		{
			case LINE	:
				if (3 == numArgs)
					return getSpecialConstructor(bType, Color.class, Integer.TYPE, Boolean.TYPE);
				break;

			case TITLED	:
				// there are 2 "createTitledBorder" methods - we prefer the one with the string
				if (1 == numArgs)
					return getSpecialConstructor(bType, String.class);

			case MATTE	:
				// there are 2 "createMatterBorder" methods - we prefer the one with the Color
				if (5 == numArgs)
					return getSpecialConstructor(bType, Integer.TYPE, Integer.TYPE, Integer.TYPE, Integer.TYPE, Color.class);

			default		:
				// do nothing
		}

		return null;
	}

	private static final Object[] getCreatorInvocationArguments (
			final BorderType bType, final String crName, final String crArgs,
			final List<String> args, final Class<?> ... mParams)
	 	throws Exception
	{
		final int	numParams=(null == mParams) ? 0 : mParams.length;
		if (numParams <= 0)
			return null;

		final Object[]	invArgs=new Object[numParams];
		for (int aIndex=0; aIndex < numParams; aIndex++)
		{
			final Class<?>				pType=mParams[aIndex];
			ValueStringInstantiator<?>	inst=ConvUtil.getConverter(pType);
			if (null == inst)
				inst = ClassUtil.getJDKStringInstantiator(pType);
			if (null == inst)
				throw new ClassNotFoundException("getCreatorInvocationArguments(" + crName + "[" + crArgs + "]) no " + ValueStringInstantiator.class.getSimpleName() + " found for parameter type=" + pType.getSimpleName());

			final String	argVal=args.get(aIndex);
			if (BorderType.TITLED.equals(bType))
				invArgs[aIndex] = getTitledBorderArgumentValue(crArgs, crName, aIndex, argVal, inst);
			else if (BorderType.BEVEL.equals(bType))
				invArgs[aIndex] = getBevelBorderArgumentValue(crArgs, crName, aIndex, argVal, inst);
			else if (BorderType.ETCHED.equals(bType))
				invArgs[aIndex] = getEtchedBorderArgumentValue(crArgs, crName, aIndex, argVal, inst);
			else
				invArgs[aIndex] = inst.newInstance(argVal);
		}

		return invArgs;
	}

	public static final Border fromStringComponents (
			final String crName, final String crArgs, final Collection<? extends Method> crList)
		throws Exception
	{
		final List<String>	args=StringUtil.splitString(crArgs, ',');
		final int			numArgs=(null == args) ? 0 : args.size();
		final BorderType	bType=BorderType.fromString(crName);
		// look for supported constructors that do not have a "createXXX" border factory method
		{
			final Constructor<?>	ctor=checkSpecialConstructor(bType, numArgs);
			if (ctor != null)
			{
				final Object[]	invArgs=
					getCreatorInvocationArguments(bType, crName, crArgs, args, ctor.getParameterTypes());
				return (Border) ctor.newInstance(invArgs);
			}
		}

		if ((null == crList) || (crList.size() <= 0))
			throw new IllegalArgumentException("fromStringComponents(" + crName + "[" + crArgs + "]) no method named '" + CREATOR_METHOD_PREFIX + crName + "' available");

		for (final Method m : crList)
		{
			final Class<?>[]	mParams=m.getParameterTypes();
			final int			numParams=(null == mParams) ? 0 : mParams.length;
			// TODO check if this code covers all useful creators
			if (numParams != numArgs)
				continue;

			if (numParams <= 0)	// OK if no arguments (e.g. "createEmptyBorder")
				return (Border) m.invoke(null, AttributeAccessor.EMPTY_OBJECTS_ARRAY);

			final Object[]	invArgs=
				getCreatorInvocationArguments(bType, crName, crArgs, args, mParams);
			return (Border) m.invoke(m, invArgs);
		}

		throw new NoSuchElementException("fromStringComponents(" + crName + "[" + crArgs + "]) no matching creator found");
	}

	public static final Border fromStringComponents (
				final String crName, final String crArgs) throws Exception
	{
		return fromStringComponents(crName, crArgs, getCreatorMethods(crName));
	}

	public static final Class<? extends Border> resolverBorderClass (final String v)
	{
		final String	borderType=StringUtil.getCleanStringValue(v);
		if ((null == borderType) || (borderType.length() <= 0))
			return null;

		final Collection<? extends Method>	crList;
		try
		{
			if ((null == (crList=getCreatorMethods(borderType))) || (crList.size() <= 0))
				return null;
		}
		catch(Exception e)
		{
			return null;
		}

		for (final Method m : crList)
		{
			final Class<?>	retVal=(null == m) ? null : m.getReturnType();
			if ((null == retVal) || (!Border.class.isAssignableFrom(retVal)))
				continue;

			@SuppressWarnings("unchecked")
			final Class<? extends Border>	bc=(Class<? extends Border>) retVal;
			return bc;
		}

		return null;
	}
	/**
	 * <P>Copyright 2008 as per GPLv2</P>
	 *
	 * <P>Handles only instantiation for a specific {@link Border} type
	 * @param <B> The specific {@link Border} type
	 * @author Lyor G.
	 * @since Dec 10, 2008 9:45:55 AM
	 */
	public static final class SpecificBorderValueInstantiator<B extends Border> extends AbstractXmlValueStringInstantiator<B> {
		private final String	_borderType;
		public final String getBorderType ()
		{
			return _borderType;
		}

		private final Collection<? extends Method>	_crList;
		public final Collection<? extends Method> getCreators ()
		{
			return _crList;
		}

		@SuppressWarnings("unchecked")
		protected SpecificBorderValueInstantiator (final String borderType) throws Exception
		{
			super((Class<B>) resolverBorderClass(borderType));

			if ((null == (_crList=getCreatorMethods(borderType))) || (_crList.size() <= 0))
				throw new IllegalArgumentException("<init>(" + borderType + ") no creation methods available");
			_borderType = borderType;
		}
		/*
		 * @see net.community.chest.convert.ValueStringInstantiator#convertInstance(java.lang.Object)
		 */
		@Override
		public String convertInstance (B inst) throws Exception
		{
			// TODO implement some kind of conversion
			throw new UnsupportedOperationException("convertInstance(" + inst + ") N/A");
		}
		/*
		 * @see net.community.chest.convert.ValueStringInstantiator#newInstance(java.lang.String)
		 */
		@Override
		public B newInstance (String v) throws Exception
		{
			final String	s=StringUtil.getCleanStringValue(v);
			final Border	b=fromStringComponents(getBorderType(), s, getCreators());
			if (null == b)
				return null;

			return getValuesClass().cast(b);
		}
	}

	private static Map<String,SpecificBorderValueInstantiator<?>>	_specInstsMap	/* =null */;
	public static final SpecificBorderValueInstantiator<?> getSpecificBorderInstantiator (final String borderType) throws Exception
	{
		if ((null == borderType) || (borderType.length() <= 0))
			return null;

		synchronized(BorderValueInstantiator.class)
		{
			if (null == _specInstsMap)
				_specInstsMap = new TreeMap<String,SpecificBorderValueInstantiator<?>>(String.CASE_INSENSITIVE_ORDER);
		}

		SpecificBorderValueInstantiator<?>	inst=null;
		synchronized(_specInstsMap)
		{
			if (null == (inst=_specInstsMap.get(borderType)))
			{
				@SuppressWarnings("rawtypes")
				final SpecificBorderValueInstantiator<?>	ni=
					new SpecificBorderValueInstantiator(borderType);
				_specInstsMap.put(borderType, ni);
				inst = ni;
			}
		}

		return inst;
	}

	public static final Border fromString (final String v) throws Exception
	{
		final String	s=StringUtil.getCleanStringValue(v);
		final int		sLen=(null == s) ? 0 : s.length();
		if (sLen <= 0)
			return null;

		final int	pStartPos=s.lastIndexOf('('), pEndPos=s.lastIndexOf(')');
		if ((pStartPos <= 0) || (pStartPos >= (sLen-1))
		 || (pEndPos <= pStartPos) || (pEndPos >= (sLen-1)))
		 	throw new IllegalArgumentException("fromString(" + s + ") missing/mismtached arguments list");

		final String	crName=s.substring(0, pStartPos).trim(),
						crArgs=(pEndPos > (pStartPos+1)) ? s.substring(pStartPos + 1, pEndPos).trim() : "";
		if ((null == crName) || (crName.length() <= 0))
			throw new IllegalArgumentException("fromString(" + s + ") no creator specified");

		return fromStringComponents(crName, crArgs);
	}
	/*
	 * @see net.community.chest.convert.ValueStringInstantiator#newInstance(java.lang.String)
	 */
	@Override
	public Border newInstance (final String s) throws Exception
	{
		return fromString(s);
	}

	public static final BorderValueInstantiator	DEFAULT=new BorderValueInstantiator();
}
