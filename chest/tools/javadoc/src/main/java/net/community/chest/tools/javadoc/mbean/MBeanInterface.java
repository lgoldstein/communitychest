package net.community.chest.tools.javadoc.mbean;

import java.io.File;
import java.io.PrintStream;
import java.util.Collection;
import java.util.Map;

import net.community.chest.tools.javadoc.ClassMethodsMap;
import net.community.chest.tools.javadoc.DocErrorLevel;
import net.community.chest.tools.javadoc.DocletUtil;
import net.community.chest.tools.javadoc.ExtendedAttributesTagMap;

import com.sun.javadoc.ClassDoc;
import com.sun.javadoc.Doc;
import com.sun.javadoc.DocErrorReporter;
import com.sun.javadoc.MethodDoc;
import com.sun.javadoc.ParamTag;
import com.sun.javadoc.Parameter;
import com.sun.javadoc.RootDoc;
import com.sun.javadoc.Tag;
import com.sun.javadoc.Type;

/**
 * <P>Copyright 2007 as per GPLv2</P>
 * 
 * <P>Class that generates an XML description file that can be imported by a
 * dynamic MBean embedder</P>
 * 
 * @author Lyor G.
 * @since Aug 16, 2007 11:30:59 AM
 */
public class MBeanInterface extends AMBeanDoclet {
	
	private MBeanInterface (DocErrorReporter reporter)
	{
		super(reporter);
	}
	/*
	 * @see net.community.chest.tools.javadoc.mbean.AMBeanDoclet#getTargetFileSuffix()
	 */
	@Override
	protected String getTargetFileSuffix ()
	{
		return ".xml";
	}
	/**
	 * <B>Note:</B> assumes parent XML has been written, but not terminated with ">"
	 * @param xmlIndent indentation to be used for separate XML element - may
	 * be null/empty
	 * @param out output stream to write to - may NOT be null 
	 * @param s description string to be generated - if null/empty then ""
	 * description is generated
	 * @return 0=description generated as XML element <U>attribute</U>,
	 * >0=description generated as <U>separate</U> XML sub-element, <0=error
	 */
	public static int generateDescription (final String xmlIndent, final PrintStream out, final String s)
	{
		if (null == out)
			return Integer.MIN_VALUE;

		String		es=(null == s) ? "" : s.trim();	
		final int	sLen=(null == es) ? 0 : es.length(),
					crPos=(sLen <= 0) ? (-1) : es.indexOf('\r'),
					lfPos=(sLen <= 0) ? (-1) : es.indexOf('\n');

		// check if need separate sub-element or can use attribute - simply by checking if have CR/LF
		if (((lfPos >= 0) && (lfPos < sLen))
		 || ((crPos >= 0) && (crPos < sLen)))
		{
			final String	xi=(null == xmlIndent) ? "\t" : xmlIndent + "\t";
			// TODO(?) remove '<'/'>' formatting tags
			out.println(">");	// close leading XML element
			out.println(xi + "<description>");
			out.println(xi + es);
			out.println(xi + "</description>");
			return (+1);
		}
		else	// generate as attribute
		{
			// replace TAB with SPACE and double-quote with single
			final int	tabPos=(sLen <= 0) ? (-1) : es.indexOf('\t'),
						qtPos=(sLen <= 0) ? (-1) : es.indexOf('"');
			if ((tabPos >= 0) && (tabPos < sLen))
				es = es.replace('\t', ' ');
			if ((qtPos >= 0) && (qtPos < sLen))
				es = es.replace('"', '\'');

			out.print(" description=\"" + es + "\"");
			return 0;
		}
	}
	/**
	 * <B>Note:</B> assumes parent XML has been written, but not terminated with ">"
	 * @param xmlIndent indentation to be used for separate XML element - may
	 * be null/empty
	 * @param out output stream to write to - may NOT be null 
	 * @param d document element - if null then "" description is generated
	 * @return 0=description generated as XML element <U>attribute</U>,
	 * >0=description generated as <U>separate</U> XML sub-element, <0=error
	 */
	public static int generateDescription (final String xmlIndent, final PrintStream out, final Doc d)
	{
		return generateDescription(xmlIndent, out, (null == d) ? null : d.getRawCommentText());
	}
	/**
	 * @param gMethod getter method - may be null if "write-only" and
	 * non-null/empty setter method
	 * @param sMethod setter method - may be null if "read-only" and
	 * non-null/empty getter method
	 * @return attribute access (null/empty if error)
	 */
	private static final String resolveAttributeAccess (final MethodDoc gMethod, final MethodDoc sMethod)
	{
		if (gMethod != null)
		{
			return (null == sMethod) ? "read-only" : "read-write";
		}
		else	// no getter - only setter
		{
			return (null == sMethod) /* both null not allowed ! */ ? null : "write-only";
		}
	}
	/**
	 * @param gMethod getter method - preferred if non-null
	 * @param sMethod setter method - second best
	 * @return attribute type text (null if error)
	 */
	private String resolveAttributeType (final MethodDoc gMethod, final MethodDoc sMethod)
	{
		Type	aType=(null == gMethod) ? null : gMethod.returnType();
		if (null == aType)
		{
			final Parameter[]	params=(null == sMethod) /* should not happen */ ? null : sMethod.parameters();
			final int			numParams=(null == params) /* should not happen */ ? 0 : params.length;
			final Parameter		sParam=(numParams != 1) /* should not happen */ ? null : params[0];

			aType = (null == sParam) /* should not happen */ ? null : sParam.type();
		}

		return (null == aType) ? null : aType.qualifiedTypeName();
	}
	/*
	 * @see net.community.chest.tools.javadoc.mbean.AMBeanDoclet#isImportableSuperclass(com.sun.javadoc.ClassDoc)
	 */
	@Override
	protected boolean isImportableSuperclass (final ClassDoc cd)
	{
		// if null or not an interface, then nothing to process 
		final MethodDoc[]	methods=((null == cd) || (!cd.isInterface())) ? null : cd.methods();
		// if no methods then nothing to process
		if ((null == methods) || (methods.length <= 0))
			return false;

		// TODO (?) check if qualified name starts with "java.", "javax."
		return true;
	}
	/*
	 * @see net.community.chest.tools.javadoc.mbean.AMBeanDoclet#generateImports(java.io.PrintStream, com.sun.javadoc.ClassDoc[])
	 */
	@Override
	protected int generateImports (final PrintStream out, final ClassDoc[] ifs)
	{
		if (null == out)	// should not happen
			return Integer.MIN_VALUE;

		final int	numIfs=(null == ifs) ? 0 : ifs.length;
		if (numIfs <= 0)
			return 0;

		out.println("\t<section name=\"imports\">");
		for (final ClassDoc	ic : ifs)
		{
			if (!isImportableSuperclass(ic))
				continue;
	
			out.println("\t\t<import name=\"" + ic.qualifiedName() + "\"/>");
		}
		out.println("\t</section>");

		return numIfs;
	}
	/*
	 * @see net.community.chest.tools.javadoc.mbean.AMBeanDoclet#generateTargetFilePrefix(java.io.File, com.sun.javadoc.ClassDoc, java.io.PrintStream)
	 */
	@Override
	protected int generateTargetFilePrefix (final File tgtFile, final ClassDoc cd, final PrintStream out)
	{
		if ((null == tgtFile) || (null == cd) || (null == out))
			return (-1);

		out.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		out.println("\t<!-- generated by " + getClass().getName() + " doclet $Rev: 639 $ -->");
		out.println("\t<!-- SCC revision=" + getSourceControlRevisionID() + " URL=" + getSourceControlURL() + " -->");
		out.println("<MBeanInfo"
					+ " name=\"" + cd.name() + "\""	/* we do not expect any @param... */
					+ " description=\"" + DocletUtil.compactOperationDescription(cd) + "\""
					+ ">");

		return 0;
	}
	/**
	 * Generates the extension tag(s) name-value pair(s)
	 * @param out output stream to write these pair(s) (if any available).
	 * May NOT be null
	 * @param md method whose extension tag(s) name-value pair(s) are to be
	 * generated (may NOT be null)
	 * @param xMap extracted extension tag(s) and their pair(s) - may be
	 * null/empty if none
	 * @return 0 if successful
	 */
	protected int generateExtensionTagsAttributes (final PrintStream out, final MethodDoc md, final ExtendedAttributesTagMap xMap)
	{
		final Collection<Map.Entry<String,String>>	xSet=((null == xMap) || (xMap.size() <= 0)) ? null : xMap.entrySet();
		if ((null == xSet) || (xSet.size() <= 0))
			return 0;	// OK if no extra attributes

		final Tag		xTag=(null == xMap) /* should not happen */ ? null : xMap.getTag();
		final String	xName=(null == xTag) /* should not happen */ ? null : xTag.name().trim(),
						xPrefix;
		if ((null == xName) || (xName.length() <= 0))
			xPrefix = null;
		else if (xName.charAt(0) != '@')
			xPrefix = xName.replace('.', '-');
		else
			xPrefix = xName.substring(1).replace('.', '-');
		if ((null == xPrefix) || (xPrefix.length() <= 0))	// not allowed
			return printErrorCode("generateExtensionTagsAttributes(" + md + ") no extended tag prefix for tag=" + xTag, (-2));

		for (final Map.Entry<String,String> xEntry : xSet)
		{
			final String	aName=(null == xEntry) /* should not happen */ ? null : xEntry.getKey(),
							aValue=(null == xEntry) /* should not happen */ ? null : xEntry.getValue();
			if ((null == aName) || (aName.length() <= 0))
			{
				if ((aValue != null) && (aValue.length() > 0))	// not allowed
					return printErrorCode("generateExtensionTagsAttributes(" + md + ") no name for attribute of tag=" + xName, (-3));

				continue;
			}

			out.print(" " + xPrefix + "-" + aName + "=\"" + ((null == aValue) ? "" : aValue) + "\"");
		}

		return 0;
	}
	/**
	 * Generates the extension tag(s) name-value pair(s)
	 * @param out output stream to write these pair(s) (if any available).
	 * May NOT be null
	 * @param md method whose extension tag(s) name-value pair(s) are to be
	 * generated (may NOT be null)
	 * @param xAttrs extracted extension tag(s) and their pair(s) - may be
	 * null/empty if none
	 * @return 0 if successful
	 */
	protected int generateExtensionTagsAttributes (final PrintStream out, final MethodDoc md, final Collection<ExtendedAttributesTagMap> xAttrs)
	{
		if ((null == out) || (null == md))
			return (-1);

		if ((null == xAttrs) || (xAttrs.size() <= 0))
			return 0;	// OK if empty

		for (final ExtendedAttributesTagMap xMap : xAttrs)
		{
			final int	nErr=generateExtensionTagsAttributes(out, md, xMap);
			if (nErr != 0)
				return nErr;
		}

		return 0;
	}
	/*
	 * @see net.community.chest.tools.javadoc.mbean.AMBeanDoclet#generateAttributeEntry(java.io.PrintStream, net.community.chest.tools.javadoc.mbean.AttrDescriptor, com.sun.javadoc.MethodDoc, net.community.chest.tools.javadoc.ClassMethodsMap)
	 */
	@Override
	protected String generateAttributeEntry (final PrintStream out, final AttrDescriptor aDesc, final MethodDoc md, final ClassMethodsMap cmm)
	{
		if ((null == out) || (null == aDesc) || (null == md) || (null == cmm))
			return null;

		// resolve attribute access
		final MethodDoc	od=resolveAttributeAccess(aDesc, md, cmm),
						gMethod=aDesc.isGetter() ? md : ((od != md) ? od : null),
						sMethod=aDesc.isGetter() ? ((od != md) ? od : null) : md;
		final String	aAccess=resolveAttributeAccess(gMethod, sMethod);
		if ((null == od) || (null == aAccess) || (aAccess.length() <= 0))	// should not happen
			return printErrorObject("generateAttributeEntry(" + md + ") cannot resolve attribute access", null);

		final String	attrDescText=resolveAttributeDescription(gMethod, sMethod),
						aType=resolveAttributeType(gMethod, sMethod);
		// for write-only we MUST have a type value
		if ((null == gMethod) && ((null == aType) || (aType.length() <= 0)))
			return printErrorObject("generateAttributeEntry(" + md + ") no write-only type value", null);

		out.print("\t\t<attribute"
				  + " name=\"" + aDesc.getName() + "\""
				  // ignored for write-only
				  + " getformat=\"" + aDesc.getPrefix() + "\""
				  + " access=\"" + aAccess + "\""
				  + " type=\"" + aType + "\""
				  + " description=\"" + attrDescText + "\"");

		// check if have any extension tags and attributes
		final int	nErr=generateExtensionTagsAttributes(out, od, getExtensionTagsAttributes(od));
		if (nErr != 0)
			return null;

		out.println("/>");

		return aAccess;
	}
	/*
	 * @see net.community.chest.tools.javadoc.mbean.AMBeanDoclet#printOperationEntryPrefix(java.io.PrintStream, com.sun.javadoc.MethodDoc, com.sun.javadoc.Parameter[])
	 */
	@Override
	protected int printOperationEntryPrefix (final PrintStream out, final MethodDoc md, Parameter... params)
	{
		if ((null == out) || (null == md))
			return (-1);

		final Type		rType=md.returnType();
		final String	rTypeName=(null == rType) /* should not happen */ ? null : rType.qualifiedTypeName();
		if ((null == rTypeName) || (rTypeName.length() <= 0))
			printWarning("printOperationEntryPrefix(" + md + ") no return type name");

		final int	numParams=(null == params) /* OK */ ? 0 : params.length;
		out.print("\t\t<operation"
				+ " name=\"" + md.name() + "\""
				+ " return=\"" + rTypeName + "\""
				+ " description=\"" + DocletUtil.compactOperationDescription(md) + "\"");

		// check if have any extension tags and attributes
		final int	nErr=generateExtensionTagsAttributes(out, md, getExtensionTagsAttributes(md));
		if (nErr != 0)
			return nErr;

		out.println((numParams > 0) ? ">" : "/>");
		return 0;
	}
	/*
	 * @see net.community.chest.tools.javadoc.mbean.AMBeanDoclet#generateOperationParameters(com.sun.javadoc.MethodDoc, java.io.PrintStream, com.sun.javadoc.Parameter[], com.sun.javadoc.ParamTag[])
	 */
	@Override
	protected int generateOperationParameters (final MethodDoc md, final PrintStream out, final Parameter[] params, final ParamTag[] tags)
	{
		if (null == out)	// should not happen
			return Integer.MIN_VALUE;
		
		final int	numParams=(null == params) /* OK */ ? 0 : params.length,
					numTags=(null == tags) ? 0 : tags.length;
		for (int	pIndex=0; pIndex < numParams; pIndex++)
		{
			final Parameter	p=params[pIndex];
			final ParamTag	t=(pIndex < numTags) ? tags[pIndex] : null;
			if (null == p)
				continue;

			if (null == t)	// complain about missing tag(s)
				printWarning("generateOperationParameters(" + md + ") no tags for parameter #" + pIndex);

			final Type		pType=p.type();
			final String	pTypeName=(null == pType) /* should not happen */ ? null : pType.qualifiedTypeName(),
							pName=(null == t) /* should not happen */ ? "arg" + String.valueOf(pIndex) : t.parameterName(),
							pComment=(null == t) /* should not happen */ ? "MBeanParameter" : DocletUtil.compactDescription(t.parameterComment());
			if ((null == pTypeName) || (pTypeName.length() <= 0))	// should not happen
				return printErrorCode("generateOperationParameters(" + md + ") no type name for parameter=" + pName, (-1));

			out.println("\t\t\t<param"
					 + " name=\"" + pName + "\""
					 + " type=\"" + pTypeName + "\""
					 + " description=\"" + pComment + "\""
					 + "/>");
		}

		return 0;
	}
	/*
	 * @see net.community.chest.tools.javadoc.mbean.AMBeanDoclet#printOperationEntrySuffix(java.io.PrintStream, com.sun.javadoc.MethodDoc, com.sun.javadoc.Parameter[])
	 */
	@Override
	protected int printOperationEntrySuffix (final PrintStream out, final MethodDoc md, final Parameter... params)
	{
		if ((null == out) || (null == md))
			return (-1);

		if ((params != null) && (params.length > 0))
			out.println("\t\t</operation>");

		return 0;
	}
	/*
	 * @see net.community.chest.tools.javadoc.mbean.AMBeanDoclet#generateTargetFileSuffix(java.io.File, com.sun.javadoc.ClassDoc, java.io.PrintStream)
	 */
	@Override
	protected int generateTargetFileSuffix (File tgtFile, ClassDoc cd, PrintStream out)
	{
		if ((null == tgtFile) || (null == cd) || (null == out))
			return (-1);

		out.println("</MBeanInfo>");
		return 0;
	}

	private static MBeanInterface	_mbi	/* =null */;
	/*
	 * @see com.sun.javadoc.Doclet#start(com.sun.javadoc.RootDoc)
	 */
	public static boolean start (final RootDoc root)
	{
		/*
		 * NOTE !!! the instance is assumed to be initialized by the call to
		 * "validateOptions" - which is assumed to occur BEFORE "start"
		 */
		return (0 == start(_mbi, root));
	}
	/*
	 * @see com.sun.javadoc.Doclet#validOptions(java.lang.String[][], com.sun.javadoc.DocErrorReporter)
	 */
	public static synchronized boolean validOptions (final String[][] options, final DocErrorReporter reporter)
	{
		setStaticReporter(reporter);

		if (_mbi != null)
		{
			report(DocErrorLevel.ERROR, "validateOptions re-called");
			return false;
		}

		_mbi = new MBeanInterface(reporter);
		return (0 == validOptions(_mbi, options, reporter));
	}
}
