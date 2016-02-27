package net.community.chest.jmx.dom;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

import javax.management.MBeanAttributeInfo;
import javax.management.MBeanInfo;
import javax.management.MBeanOperationInfo;

import net.community.chest.dom.DOMUtils;
import net.community.chest.jmx.EmbeddedJMXErrorHandler;
import net.community.chest.jmx.JMXErrorHandler;
import net.community.chest.jmx.MBeanOperationImpactType;
import net.community.chest.jmx.ReflectiveMBeanAttributeInfo;
import net.community.chest.jmx.ReflectiveMBeanOperationInfo;
import net.community.chest.jmx.ReflectiveMBeanParameterInfo;
import net.community.chest.jmx.XmlMBeanDescriptorAccessor;
import net.community.chest.reflect.AttributeMethodType;
import net.community.chest.reflect.ClassUtil;
import net.community.chest.reflect.MethodsMap;

import org.w3c.dom.DOMException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * <P>Copyright 2007 as per GPLv2</P>
 *
 * <P>Builds an {@link MBeanInfo} instance from an XML description of the
 * MBean and the MBean's instance {@link Class} via reflection API.</P>
 * 
 * @author Lyor G.
 * @since Aug 19, 2007 10:00:51 AM
 */
public class MBeanInfoXMLBuilder extends EmbeddedJMXErrorHandler {
	// lazy initialized by first call to {@link #getMBeanInfo()}
	private Map<String,ReflectiveMBeanAttributeInfo>	_attrsMap	/* =null */;
	public Map<String,ReflectiveMBeanAttributeInfo> getAttributesMap ()
	{
		return _attrsMap;
	}

	public void setAttributesMap (Map<String,ReflectiveMBeanAttributeInfo> attrsMap)
	{
		_attrsMap = attrsMap;
	}

	private MethodsMap<ReflectiveMBeanOperationInfo>	_opersMap	/* =null */;
	public MethodsMap<ReflectiveMBeanOperationInfo> getOperationsMap ()
	{
		return _opersMap;
	}

	public void setOperationsMap (MethodsMap<ReflectiveMBeanOperationInfo> opersMap)
	{
		_opersMap = opersMap;
	}

	private XmlMBeanDescriptorAccessor	_acc	/* =null */;
	public XmlMBeanDescriptorAccessor getDescriptorAccessor ()
	{
		return _acc;
	}

	public void setDescriptorAccessor (XmlMBeanDescriptorAccessor acc)
	{
		_acc = acc;
	}

	private Class<?>	_instClass	/* =null */;
	public Class<?> getInstanceClass ()
	{
		return _instClass;
	}

	public void setInstanceClass (Class<?> instClass)
	{
		_instClass = instClass;
	}

	public MBeanInfoXMLBuilder (Class<?> instClass, XmlMBeanDescriptorAccessor acc, JMXErrorHandler eh)
	{
		super(eh);
		_instClass = instClass;
		_acc = acc;
	}

	public MBeanInfoXMLBuilder (Class<?> instClass, XmlMBeanDescriptorAccessor acc)
	{
		this(instClass, acc, null);
	}

	public MBeanInfoXMLBuilder (JMXErrorHandler eh)
	{
		this(null, null, eh);
	}

	public MBeanInfoXMLBuilder ()
	{
		this(null);
	}
	/**
	 * Cached instance - lazy allocated by first call to {@link #getMBeanInfo()},
	 * so all required "environmental" setter(s) should be called <U>before</U>
	 * call to {@link #getMBeanInfo()}
	 */
	private MBeanInfo	_mbInfo	/* =null */;
	/**
	 * Inserts a CRLF before some of the JavaDoc formatting tags to allow for
	 * nicer display 
	 * @param desc original description
	 * @return formatted description - may be same as input if not formatting done
	 */
	public static final String formatDescription (final String desc)
	{
		final int	dscLen=(null == desc) ? 0 : desc.length();
		if (dscLen <= 0)	// OK if null/empty description
			return desc;

		int				curPos=0;
		StringBuilder	sb=null;	// lazy allocated if needed
		for (int	nextPos=desc.indexOf('@'); (nextPos >= curPos) && (nextPos < dscLen); )
		{
			final int	tagStart=nextPos;
			for (nextPos++ /* skip '@' */; nextPos < dscLen; nextPos++)
			{
				final char	ch=desc.charAt(nextPos);
				// Javadoc tags of interest for formatting are all lowercase letters
				if ((ch < 'a') || (ch > 'z'))
					break;
			}

			// ignore Javadoc tags that are at the end and not followed by text
			if (nextPos >= dscLen)
				break;

			// Javadoc tags are at least one character long
			if (nextPos > tagStart)
			{
				final String	tagName=desc.substring(tagStart, nextPos);
				// @link is the only "inline" tag we pass along as-is
				if (!"@link".equals(tagName))
				{
					if (curPos < tagStart)
					{
						final String	clrText=desc.substring(curPos, tagStart);

						if (null == sb)
							sb = new StringBuilder(dscLen + 4);

						sb.append("<P>").append(clrText).append("</P>");
						curPos = tagStart;
					}
				}
			}

			nextPos = desc.indexOf('@', nextPos);
		}

		// check if any "leftovers"
		if ((curPos > 0) && (curPos < dscLen) && (sb != null))
		{
			final String	remPart=desc.substring(curPos);
			sb.append(remPart);
		}

		if ((null == sb) || (sb.length() <= 0))
			return desc;

		return sb.toString();
	}
	// XML element(s)/attribute(s) names
	public static final String	MBEAN_INFO_ROOT_ELEM="MBeanInfo",
									DESCR_ATTR_NAME="description",
									NAME_ATTR_NAME="name",
									TYPE_ATTR_NAME="type",
								MBEAN_ATTR_ELEM="attribute",
									ACCESS_ATTR_NAME="access",
										ACCESS_READ_ONLY="read-only",
										ACCESS_WRITE_ONLY="write-only",
										ACCESS_READ_WRITE="read-write",
									GETFORMAT_ATTR_NAME="getformat",
										GETFORMAT_IS_VALUE="is",
										GETFORMAT_GET_VALUE="get",
										GETFORMAT_DEFAULT_VALUE=GETFORMAT_GET_VALUE;
	/**
	 * Resolves get-ter method prefix format value
	 * @param aFormat original value - if null/empty then {@link #GETFORMAT_DEFAULT_VALUE}
	 * is returned.
	 * @return resolved value (if null or one of the known ones) - null if
	 * non-null input parameter but not one of the allowed values
	 * @see #GETFORMAT_GET_VALUE
	 * @see #GETFORMAT_IS_VALUE
	 */
	public static final String resolveGetterFormat (final String aFormat)
	{
		if ((null == aFormat) || (aFormat.length() <= 0))
			return GETFORMAT_DEFAULT_VALUE;

		if (GETFORMAT_IS_VALUE.equalsIgnoreCase(aFormat)
		 || GETFORMAT_GET_VALUE.equalsIgnoreCase(aFormat))
			return aFormat;

		return null;
	}

	public Class<?> resolveDataType (final String dtVal) throws Exception
	{
		return ClassUtil.resolveDataType(dtVal);
	}

	public ReflectiveMBeanAttributeInfo getMBeanAttribute (final String aName, final Element elem) throws Exception
	{
		final String	aAccess=elem.getAttribute(ACCESS_ATTR_NAME);
		if ((null == aName) || (aName.length() <= 0)
		 || (null == aAccess) || (aAccess.length() <= 0))
			throw errorThrowable(new DOMException(DOMException.INVALID_STATE_ERR, "addAttribute(" + aName + ") missing name/access specification"));

		final Class<?>	instClass=getInstanceClass();
		Method			mGet=null, mSet=null;
		if (ACCESS_READ_ONLY.equalsIgnoreCase(aAccess) || ACCESS_READ_WRITE.equalsIgnoreCase(aAccess))
		{
			final String	aFormat=elem.getAttribute(GETFORMAT_ATTR_NAME), gFormat=resolveGetterFormat(aFormat);
			if ((null == gFormat) || (gFormat.length() <= 0))
				throw errorThrowable(new NoSuchMethodException("getMBeanAttribute(" + aName + ") bad/illegal" + aAccess + " " + GETFORMAT_ATTR_NAME + " attribute value: " + aFormat));

			final String	mthdName=gFormat + aName;
			if (null == (mGet=instClass.getMethod(mthdName)))	// should not happen
				throw errorThrowable(new NoSuchMethodException("getMBeanAttribute(" + aName + ") no " + aAccess + " method=" + mthdName));

			// if have read-write access use the getter's return type as the setter's argument signature
			if (ACCESS_READ_WRITE.equalsIgnoreCase(aAccess))
			{
				// NOTE !!! this code might NOT work for co-variant return(s)
				final String	setName="set" + aName;
				if (null == (mSet=instClass.getMethod(setName, mGet.getReturnType())))
					throw errorThrowable(new DOMException(DOMException.NOT_FOUND_ERR, "getMBeanAttribute(" + aName + ") missing " + aAccess + " SET method"));
			}
		}
		else if (ACCESS_WRITE_ONLY.equalsIgnoreCase(aAccess))
		{
			final String	aType=elem.getAttribute(MBeanFeatureDescriptor.TYPE_ATTR);
			final Class<?>	argType=resolveDataType(aType);
			final String	setName="set" + aName;
			// NOTE !!! this code might NOT work for co-variant return(s)
			if (null == (mSet=instClass.getMethod(setName, argType)))
				throw errorThrowable(new DOMException(DOMException.NOT_FOUND_ERR, "getMBeanAttribute(" + aName + ") missing " + aAccess + " '" + MBeanFeatureDescriptor.TYPE_ATTR + "' specifier or method"));
		}
		else
			throw errorThrowable(new DOMException(DOMException.INVALID_STATE_ERR, "getMBeanAttribute(" + aName + ") unknown access: " + aAccess));

		String	aDesc=elem.getAttribute(MBeanFeatureDescriptor.DESC_ATTR);
		if ((null == aDesc) || (aDesc.length() <= 0))	// complain...
			aDesc = "getMBeanAttribute(" + aName + ") no description";
		else
			aDesc = formatDescription(aDesc);

		return new ReflectiveMBeanAttributeInfo(aName, aDesc, mGet, mSet);
	}

	public String getAttributeElementName ()
	{
		return MBEAN_ATTR_ELEM;
	}

	public String getAttributeIdAttrName ()
	{
		return MBeanFeatureDescriptor.NAME_ATTR;
	}

	public Map<String,ReflectiveMBeanAttributeInfo> handleAttributes (final Element root) throws Exception
	{
		final Map<String,Element>								aMap=
			DOMUtils.getSubsections(root, getAttributeElementName(), getAttributeIdAttrName());
		final Collection<? extends Map.Entry<String,Element>>	aColl=
			((null == aMap) || (aMap.size() <= 0)) /* OK if no imports */ ? null : aMap.entrySet();

		if ((aColl != null) && (aColl.size() > 0))	// OK if no attributes
		{
			for (final Map.Entry<String,Element> aEntry : aColl)
			{
				final String	attrName=
					(null == aEntry) /* should not happen */ ? null : AttributeMethodType.getAdjustedAttributeName(aEntry.getKey());
				if ((null == attrName) || (attrName.length() <= 0))
					continue;	// should not happen

				final ReflectiveMBeanAttributeInfo			aInfo=getMBeanAttribute(attrName, aEntry.getValue());
				Map<String,ReflectiveMBeanAttributeInfo>	attrsMap=getAttributesMap();
				if (null == attrsMap)
				{
					attrsMap = new TreeMap<String, ReflectiveMBeanAttributeInfo>(String.CASE_INSENSITIVE_ORDER);
					setAttributesMap(attrsMap);
				}

				attrsMap.put(attrName, aInfo);
			}
		}

		return getAttributesMap();
	}
	// element used to denote a parameter
	public static final String	MBEAN_PARAM_ELEM="param";
	public String getOperationParameterElementName ()
	{
		return MBEAN_PARAM_ELEM;
	}
	/**
	 * <P>Retrieves an MBean attribute from the XML. The element should be
	 * a "value-less" <I>param</I> element with the following
	 * attributes:</P></BR>
	 * <UL>
	 * 		<LI>
	 * 		<I>name</I> - parameter name
	 * 		</LI>
	 *
	 * 		<LI>
	 * 		<I>description</I> - description text - should NOT contain any
	 * 		double-quotes to avoid XML parsing errors. <B>Note:</B> if no
	 * 		description supplied some default text is generated.
	 * 		</LI>
	 *
	 *		<LI>
	 *		<I>type</I> the <U>fully qualified</U> name of the class
	 * 		(or one of the primitive types) that serves as the parameter.
	 * 		</LI>
	 * <UL>
	 * @param elem XML element to be parsed
	 * @return operation info (null if error)
	 * @throws Exception if unable to parse
	 */
	public ReflectiveMBeanParameterInfo getMBeanOperationParam (final Element elem) throws Exception
	{
		if (null == elem)	// should not happen
			throw errorThrowable(new IllegalArgumentException("getMBeanOperationParam() no XML root element/object class instance"));

		final String	oName=elem.getAttribute(NAME_ATTR_NAME),
						oType=elem.getAttribute(TYPE_ATTR_NAME);
		final Class<?>	oClass=resolveDataType(oType);
		if ((null == oName) || (oName.length() <= 0) || (null == oClass))
			throw errorThrowable(new DOMException(DOMException.INVALID_STATE_ERR, "getMBeanOperationParam(" + oName + "[" + oType + "]) missing/invalid name/type specification"));
		
		String	oDesc=elem.getAttribute(DESCR_ATTR_NAME);
		if ((null == oDesc) || (oDesc.length() <= 0))	// complain...
			oDesc = "getMBeanOperationParam(" + oName + "[" + oType + "]) no description";
		else
			oDesc = formatDescription(oDesc);

		return new ReflectiveMBeanParameterInfo(oName, oClass, oDesc);
	}
	/**
	 * Resolves the {@link #IMPACT_ATTR_NAME} attribute value (if any)
	 * @param oImpact impact value - if null/empty then {@link MBeanOperationInfo#UNKNOWN}
	 * is returned
	 * @return decoded impact value
	 * @throws IllegalArgumentException if non-null/empty argument and not
	 * one of the known values
	 */
	public static final MBeanOperationImpactType getMBeanOperationImpact (final String oImpact) throws IllegalArgumentException
	{
		if ((null == oImpact) || (oImpact.length() <= 0))
			return MBeanOperationImpactType.UNKNOWN;
		else
			return MBeanOperationImpactType.fromString(oImpact);
	}
	// operation related elements/attributes/values
	public static final String	MBEAN_OPER_ELEM="operation",
									IMPACT_ATTR_NAME="impact",
										ACTION_IMPACT_VALUE="ACTION",
										ACTION_INFO_IMPACT_VALUE="ACTION-INFO",
										INFO_IMPACT_VALUE="INFO",
										UNKNOWN_IMPACT_VALUE="UNKNOWN";
	public ReflectiveMBeanOperationInfo getMBeanOperation (final Element elem) throws Exception
	{
		final String					oName=elem.getAttribute(NAME_ATTR_NAME),
										oImpact=elem.getAttribute(IMPACT_ATTR_NAME);
		final MBeanOperationImpactType	iType=getMBeanOperationImpact(oImpact);
		if ((null == oName) || (oName.length() <= 0) || (null == iType))
			throw errorThrowable(new DOMException(DOMException.INVALID_STATE_ERR, "getMBeanOperation(" + oName + ") missing/invalid name/type/impact specification"));

		// check if have any parameters
		final NodeList	paramNodes=elem.getElementsByTagName(getOperationParameterElementName());
		final int		numParNodes=(null == paramNodes) /* OK if no parameters */ ? 0 : paramNodes.getLength();
		final Collection<ReflectiveMBeanParameterInfo>	pars=
				(numParNodes <= 0) /* OK if no parameters */ ? null : new ArrayList<ReflectiveMBeanParameterInfo>(numParNodes);
		for (int	pIndex=0; pIndex < numParNodes; pIndex++)
		{
			final Node 	n=paramNodes.item(pIndex);
			if ((null == n) || (n.getNodeType() != Node.ELEMENT_NODE))
				continue;

			final ReflectiveMBeanParameterInfo	pInfo=getMBeanOperationParam((Element) n);
			pars.add(pInfo);
		}

		final int								numParams=(null == pars) ? 0 : pars.size();
		final ReflectiveMBeanParameterInfo[]	parsInfo=(numParams <= 0) ? null : pars.toArray(new ReflectiveMBeanParameterInfo[numParams]);
		final int								numSigVals=(null == parsInfo) ? 0 : parsInfo.length;
		final Class<?>[]						sigParams=(numSigVals <= 0) ? null : new Class[numSigVals];
		for (int	sIndex=0; sIndex < numSigVals; sIndex++)
		{
			final ReflectiveMBeanParameterInfo	pInfo=parsInfo[sIndex];
			final Class<?>						cInfo=(null == pInfo) /* should not happen */ ? null : pInfo.getParameterClass();
			if (null == cInfo)	// should not happen
				throw errorThrowable(new IllegalStateException("getMBeanOperation(" + oName + ") bad/illegal signature class for parametere #" + (sIndex + 1)));

			sigParams[sIndex] = cInfo;
		}

		final Class<?>	instClass=getInstanceClass();
		final Method	oMethod=instClass.getMethod(oName, sigParams);
		if (null == oMethod)	// should not happen
			throw errorThrowable(new NoSuchMethodException("getMBeanOperation(" + oName + ") no matching method found"));

		String	oDesc=elem.getAttribute(DESCR_ATTR_NAME);
		if ((null == oDesc) || (oDesc.length() <= 0))	// complain...
			oDesc = "getMBeanOperation(" + oName + ") no description";
		else
			oDesc = formatDescription(oDesc);

		return new ReflectiveMBeanOperationInfo(oDesc, oMethod, parsInfo);
	}

	public String getOperationElementName ()
	{
		return MBEAN_OPER_ELEM;
	}

	public MethodsMap<ReflectiveMBeanOperationInfo> handleOperations (final Element root) throws Exception
	{
		final NodeList	opers=(null == root) /* OK if no operations */ ? null : root.getElementsByTagName(getOperationElementName());
		final int		numOpers=(null == opers) /* OK if no operations */ ? 0 : opers.getLength();
		final Class<?>	instClass=getInstanceClass();
		for (int	oIndex=0; oIndex < numOpers; oIndex++)
		{
			final Node	n=opers.item(oIndex);
			if ((null == n) || (n.getNodeType() != Node.ELEMENT_NODE))
				continue;

			final ReflectiveMBeanOperationInfo			oInfo=getMBeanOperation((Element) n);
			MethodsMap<ReflectiveMBeanOperationInfo>	oMap=getOperationsMap();
			if (null == oMap)
			{
				oMap = new MethodsMap<ReflectiveMBeanOperationInfo>(ReflectiveMBeanOperationInfo.class);
				setOperationsMap(oMap);
			}

			final Method	oMethod=oInfo.getOperation();
			oMap.put(instClass, oMethod, oInfo);
		}

		return getOperationsMap();
	}

	// Element used to denote an import of a file
	public static final String MBEAN_IMPORT_ELEM="import";
	public String getImportElementName ()
	{
		return MBEAN_IMPORT_ELEM;
	}

	public String getImportIdAttrName ()
	{
		return NAME_ATTR_NAME;
	}

	private Map<String,Class<?>>	_importsMap	/* =null */;
	public Map<String,Class<?>> handleImports (final Element root) throws Exception
	{
		final Map<String,Element>								iMap=
				DOMUtils.getSubsections(root, getImportElementName(), getImportIdAttrName());
		final Collection<? extends Map.Entry<String,Element>>	iColl=
				((null == iMap) || (iMap.size() <= 0)) /* OK if no imports */ ? null : iMap.entrySet();

		if ((iColl != null) && (iColl.size() > 0))	// OK if no imports
		{
			final XmlMBeanDescriptorAccessor	acc=getDescriptorAccessor();
			for (final Map.Entry<String,Element> iEntry : iColl)
			{
				final String	subClass=(null == iEntry) /* should not happen */ ? null : iEntry.getKey();
				importXML(acc.getRootDescriptorElement(subClass), subClass);
			}
		}

		return _importsMap;
	}

	// XML document sections
	public static final String	SECTION_ELEM_NAME="section",
										IMPORTS_ID="imports",
										ATTRS_ID="attributes",
										OPERS_ID="operations";
	public String getImportsSectionName ()
	{
		return IMPORTS_ID;
	}

	public String getAttributesSectionName ()
	{
		return ATTRS_ID;
	}

	public String getOperationsSectionName ()
	{
		return OPERS_ID;
	}

	public String getSectionElementName ()
	{
		return SECTION_ELEM_NAME;
	}

	public String getSectionIdAttrName ()
	{
		return NAME_ATTR_NAME;
	}
	/**
	 * @param elem root {@link Element} of the {@link MBeanInfo} XML
	 * descriptor - may NOT be null
	 * @param effClass if non-null/empty then this is a call due to <I>import</I>
	 * directive - in this case the imported {@link MBeanInfo} name/description
	 * are not parsed. In this case, the parameter shows the <U>imported</U>
	 * class name whose XML is actually being parsed
	 * @throws Exception if unable to parse/import
	 */
	public void importXML (final Element elem, final String effClass) throws Exception
	{
		if ((effClass != null) && (effClass.length() > 0))
		{
			// skip if already imported
			if (_importsMap != null)
			{
				if (_importsMap.get(effClass) != null)
					return;
			}
			else	// no previous imports
				_importsMap = new TreeMap<String, Class<?>>();

			_importsMap.put(effClass, getInstanceClass());
		}

		final Map<String,Element>	sectionsMap=DOMUtils.getSubsections(elem, getSectionElementName(), getSectionIdAttrName());
		if ((null == sectionsMap) || (sectionsMap.size() <= 0))
			return;	// a bit unorthodox, but OK

		handleImports(sectionsMap.get(getImportsSectionName()));
		handleAttributes(sectionsMap.get(getAttributesSectionName()));
		handleOperations(sectionsMap.get(getOperationsSectionName()));
	}

	public synchronized MBeanInfo getMBeanInfo () throws Exception
	{
		if (_mbInfo != null)
			return _mbInfo;

		final Class<?>						instClass=getInstanceClass();
		final String						instName=instClass.getName();
		final XmlMBeanDescriptorAccessor	acc=getDescriptorAccessor();
		final Element						instRoot=acc.getRootDescriptorElement(instName);
		importXML(instRoot, instName);

		final MBeanAttributeInfo[]	aInfo;
		{
			final Map<String,ReflectiveMBeanAttributeInfo>				aMap=getAttributesMap();
			final Collection<? extends ReflectiveMBeanAttributeInfo>	cAttrs=
						((null == aMap) || (aMap.size() <= 0)) ? null : aMap.values();

			final int	numAttrs=(null == cAttrs) ? 0 : cAttrs.size();
			aInfo = (numAttrs <= 0) ? null : cAttrs.toArray(new ReflectiveMBeanAttributeInfo[numAttrs]);
		}

		final MBeanOperationInfo[]	oInfo;
		{
			final MethodsMap<ReflectiveMBeanOperationInfo>				oMap=getOperationsMap();
			final Collection<? extends ReflectiveMBeanOperationInfo>	cOpers=
				((null == oMap) || (oMap.size() <= 0)) ? null : oMap.values();

			final int	numOpers=(null == cOpers) ? 0 : cOpers.size();
			oInfo = (numOpers <= 0) ? null : cOpers.toArray(new ReflectiveMBeanOperationInfo[numOpers]);
		}

		final String	desc;
		{
			final String	val=formatDescription(instRoot.getAttribute(MBeanEntryDescriptor.DESC_ATTR));
			if ((null == val) || (val.length() <= 0))
				desc = "Generic MBean";
			else
				desc = val;
		}

		_mbInfo = new MBeanInfo(instName, desc, aInfo, null /* TODO add MBean constructors */, oInfo, null /* TODO add MBean notitifcations */);
		return _mbInfo;
	}

	public synchronized void setMBeanInfo (MBeanInfo mbInfo)
	{
		_mbInfo = mbInfo;
	}
	/**
	 * Prepares for re-use - basically, forces a re-evaluation of the cached
	 * {@link MBeanInfo} instance next time {@link #getMBeanInfo()} is called.
	 * <B>Note:</B> the object can even be re-used with a <U>different</U>
	 * {@link Class} instance if {@link #setInstanceClass(Class)} is called
	 * after this method and before {@link #getMBeanInfo()} is (re-)called.
	 */
	public synchronized void reset ()
	{
		final Map<String,ReflectiveMBeanAttributeInfo>	aMap=getAttributesMap();
		if ((aMap != null) && (aMap.size() > 0))
			aMap.clear();

		final MethodsMap<ReflectiveMBeanOperationInfo>	oMap=getOperationsMap();
		if ((oMap != null) && (oMap.size() > 0))
			oMap.clear();

		if (_importsMap != null)
			_importsMap.clear();

		setMBeanInfo(null);
	}
}
