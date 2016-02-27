package net.community.chest.tools.javadoc.mbean;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.TreeMap;

import net.community.chest.tools.javadoc.ClassMethodsMap;
import net.community.chest.tools.javadoc.DocErrorLevel;
import net.community.chest.tools.javadoc.DocletErrReporterHelper;
import net.community.chest.tools.javadoc.DocletUtil;
import net.community.chest.tools.javadoc.ExtendedAttributesTagMap;

import com.sun.javadoc.ClassDoc;
import com.sun.javadoc.DocErrorReporter;
import com.sun.javadoc.MethodDoc;
import com.sun.javadoc.PackageDoc;
import com.sun.javadoc.ParamTag;
import com.sun.javadoc.Parameter;
import com.sun.javadoc.ProgramElementDoc;
import com.sun.javadoc.RootDoc;
import com.sun.javadoc.SourcePosition;
import com.sun.javadoc.Tag;

/**
 * <P>Copyright 2007 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Aug 16, 2007 11:23:54 AM
 */
public abstract class AMBeanDoclet extends DocletErrReporterHelper {
	protected AMBeanDoclet (DocErrorReporter reporter)
	{
		super(reporter);
	}

	private static DocErrorReporter	_reporter	/* =null */;
	protected static final synchronized void setStaticReporter (final DocErrorReporter reporter)
	{
		if ((null == _reporter) && (reporter != null))
			_reporter = reporter;
	}


	protected static final synchronized void report (DocErrorLevel lvl, SourcePosition pos, String msg)
	{
		report(_reporter, lvl, pos, msg);
	}

	protected static final void report (DocErrorLevel lvl, String msg)
	{
		report(lvl, null, msg);
	}

	protected static final int reportErrCode (DocErrorLevel lvl, String msg, int nErr)
	{
		report(lvl, msg);
		return nErr;
	}
	/*
	 * @see net.community.chest.tools.javadoc.DocletErrReporterHelper#print(net.community.chest.tools.javadoc.DocErrorLevel, com.sun.javadoc.SourcePosition, java.lang.String)
	 */
	@Override
	public void print (DocErrorLevel lvl, SourcePosition pos, String msg)
	{
		report(lvl, pos, msg);
	}
	/**
	 * Map of classes for which XML file has already been created (or skipped
	 * because source file is older than XML which already exists)
	 */
	private final Map<String,ClassDoc>	_processedMap=new TreeMap<String,ClassDoc>();
	/**
	 * Called to mark that a class has been processed
	 * @param cd class to be marked as processed - may NOT be null
	 * @return 0 if successful
	 */
	protected int setProcessed (final ClassDoc cd)
	{
		final String	qfName=(null == cd) /* should not happen */ ? null : cd.qualifiedName();
		if ((null == qfName) || (qfName.length() <= 0))	// should not happen
			return printErrorCode("setProcessed(" + qfName + ") bad/illegal name", (-1));
		if (null == _processedMap) // should not happen
			return printErrorCode("setProcessed(" + qfName + ") no map", (-2));

		if (_processedMap.put(qfName, cd) != null)	// complain if happens	
			printWarning("setProcessed(" + qfName + ") already set");

		return 0;
	}
	/**
	 * @param qfName <U>fully qualified</U> class name - should not be null/empty
	 * @return TRUE if this (non-null/empty) class name has already been processed
	 * @see #_processedMap
	 */
	protected boolean isProcessed (final String qfName)
	{
		if ((null == qfName) || (qfName.length() <= 0) || (null == _processedMap) /* should not happen */)
			return false;
		else
			return (_processedMap.get(qfName) != null);
	}
	/**
	 * @param cd class info to be checked - should not be null
	 * @return TRUE if this (non-null) class has already been processed
	 * @see #_processedMap
	 */
	protected boolean isProcessed (final ClassDoc cd)
	{
		return isProcessed((null == cd) ? null : cd.qualifiedName());
	}
	/**
	 * "-targetdir" location where XML(s) should be generated 
	 */
	private File	_targetDir	/* =null */;
	/**
	 * Sets the target dir - if it refers to an <U>existing</U> location then
	 * it must be a <B>directory</B>.
	 * @param targetDir "-targetdir" argument to be set - may NOT be null/empty
	 * @return 0 if successful
	 */
	protected int setTargetDir (final String targetDir)
	{
		if ((null == targetDir) || (targetDir.length() <= 0))
			return printErrorCode("setTargetDir(" + targetDir + ") bad/illegal path", (-1));

		// check if already have the option
		if (_targetDir != null)
		{
			final String	orgPath=_targetDir.getAbsolutePath();
			if (!orgPath.equalsIgnoreCase(targetDir))
				return printErrorCode("setTargetDir(" + targetDir + ") re-specified: " + orgPath, (-2));

			// complain if re-specified with SAME value
			printWarning("setTargetDir(" + targetDir + ") re-specified (same)");
		}
		else
		{
			_targetDir = new File(targetDir);

			if (_targetDir.exists() && (!_targetDir.isDirectory()))
				return printErrorCode("setTargetDir(" + targetDir + ") no a directory", (-3));
		}

		return 0;
	}
	/**
	 * @param optVals "-targetdir" argument(s) - if any
	 * @param offset offset of argument(s) in array - may NOT be negative
	 * @param len number of arguments - MUST be EXACTLY ONE
	 * @return 0 if successful
	 */
	protected int setTargetDir (final String[] optVals, final int offset, final int len)
	{
		if ((null == optVals) || (offset < 0) || (len != 1) /* exactly ONE argument expected */ || ((offset + len) > optVals.length))
			return printErrorCode("setTargetDir() bad/illegal arguments range [" + offset + "-" + (offset + len), (-1));

		return setTargetDir(optVals[offset]);
	}
	/**
	 * Suffix of MBean interface(s) name(s)
	 */
	public static final String MBEAN_SUFFIX="MBean";
	/**
	 * @return target file suffix (e.g., ".xml", ".txt", etc.) used
	 * by {@link #getTargetFilePath(ProgramElementDoc)} to generate the
	 * path.
	 */
	protected abstract String getTargetFileSuffix ();
	/**
	 * @param cd class information - may NOT be null
	 * @return {@link File} object representing the location of the matching
	 * XML file for the requested class (null if error)
	 */
	protected File getTargetFilePath (final ProgramElementDoc cd)
	{
		final String	qfName=(null == cd) /* should not happen */ ? null : cd.qualifiedName();
		final int		qfLen=(null == qfName) /* should not happen */ ? 0 : qfName.length();
		if (qfLen <= 0)
			return printErrorObject("getTargetFilePath(" + qfName + ") bad/illegal class path", null);

		if (null == _targetDir)	// should not happen
			return printErrorObject("getTargetFilePath(" + qfName + ") no target dir specified", null);

		String	fileSuffix=getTargetFileSuffix();
		if ((null == fileSuffix) || (fileSuffix.length() <= 0))
			return printErrorObject("getTargetFilePath(" + qfName + ") no file suffix specified", null);
		if (fileSuffix.charAt(0) != '.')
			fileSuffix = "." + fileSuffix;

		String	clsPath=qfName;
		// if this is an MBean interface, then its target file should point to the CLASS name
		if (qfName.endsWith(MBEAN_SUFFIX))
		{
			if (qfLen <= MBEAN_SUFFIX.length())
				return printErrorObject("getTargetFilePath(" + qfName + ") \"" + MBEAN_SUFFIX + "\" not allowed as name of interface - only as suffix", null);

			// retrieve the implementing class name by removing the "MBean" suffix
			clsPath = qfName.substring(0, qfLen - MBEAN_SUFFIX.length());
		}

		final int		pkgEndPos=clsPath.lastIndexOf('.');
		final String	pkgSubPath=clsPath.substring(0, pkgEndPos).replace('.', File.separatorChar),
						clsName=clsPath.substring(pkgEndPos + 1),
						tgtRootPath=_targetDir.getAbsolutePath();
		final int		tgtRootLen=tgtRootPath.length();
		final String	pkgPath;
		if (tgtRootPath.charAt(tgtRootLen - 1) != File.separatorChar)
			pkgPath = tgtRootPath + File.separator + pkgSubPath;
		else
			pkgPath = tgtRootPath + pkgSubPath;

		final File		pkgSubDir=new File(pkgPath);
		return new File(pkgSubDir, clsName + fileSuffix);
	}
	/**
	 * If TRUE then XML(s) are overwritten even if source file is older than
	 * its XML counterpart (default=FALSE)
	 * @see #OVERWRITE_OPT 
	 */
	private Boolean	_isOverwrite	/* =null */;
	/**
	 * @return TRUE if XML(s) should be overwritten even if source file is
	 * older than its XML counterpart (default=FALSE)
	 */
	protected boolean isOverwriteRequired ()
	{
		return (_isOverwrite != null) /* should not be otherwise */ && _isOverwrite.booleanValue();
	}
	/**
	 * @param bVal "-overwrite" argument to be set - may NOT be null/empty
	 * @return 0 if successful
	 */
	protected int setOverwrite (final Boolean bVal)
	{
		if (null == bVal)
			return printErrorCode("setOverwrite() bad/illegal value", (-1));

		if (_isOverwrite != null)
		{
			if (_isOverwrite.booleanValue() != bVal.booleanValue())
				return printErrorCode("setOverwrite(" + bVal + ") re-specified: " + _isOverwrite, (-2));

			// complain if re-specified with SAME value
			printWarning("setOverwrite(" + bVal + ") re-specified (same)");
		}
		else
			_isOverwrite = bVal;

		return 0;
	}
	/**
	 * @param ovw "-overwrite" argument to be set - may NOT be null/empty. May
	 * be "yes/no", "true/"false"
	 * @return 0 if successful
	 */
	protected int setOverwrite (final String ovw)
	{
		if ("yes".equalsIgnoreCase(ovw) || "true".equalsIgnoreCase(ovw))
			return setOverwrite(Boolean.TRUE);
		else
			return setOverwrite(Boolean.FALSE);
	}
	/**
	 * @param optVals "-overwrite" argument(s) - if any
	 * @param offset offset of argument(s) in array - may NOT be negative
	 * @param len number of arguments - MUST be EXACTLY ONE
	 * @return 0 if successful
	 */
	protected int setOverwrite (final String[] optVals, final int offset, final int len)
	{
		if ((null == optVals) || (offset < 0) || (len != 1) /* exactly ONE argument expected */ || ((offset + len) > optVals.length))
			return printErrorCode("setOverwrite() bad/illegal arguments range [" + offset + "-" + (offset + len), (-1));

		return setOverwrite(optVals[offset]);
	}
	/**
	 * <P>Checks if need to generate a target file for the associated source
	 * file according to the following rules:</P></BR>
	 * <UL>
	 * 		<LI>if target file does not exist</LI>
	 * 	or
	 * 		<LI>if target file is older that its source</LI>
	 * 	or
	 * 		<LI>if "-overwrite=true" has been specified</LI>
	 * </UL>
	 * @param srcFile source file path - should NOT be null and MUST point to
	 * an <U>existing</U> file
	 * @param tgtFile target file path - should NOT be null
	 * @return zero if target file generation at the specified location is required,
	 * positive if file generation should be skipped, negative if error
	 */
	protected int generateTargetFile (final File srcFile, final File tgtFile)
	{
		if ((null == srcFile) || (null == tgtFile))	// should not happen
			return printErrorCode("generateTargetFile() no src/xml file object(s)", (-1));

		if (!srcFile.exists())
			return printErrorCode("generateTargetFile() source does not exist: " + srcFile, (-2));

		// OK if XML does not exist or "empty" or required to overwrite in any case
		if ((!tgtFile.exists()) || (tgtFile.length() <= 10L) || isOverwriteRequired())
			return 0;

		final long	srcModified=srcFile.lastModified(),
					xmlModified=tgtFile.lastModified();
		// check which is older
		if (xmlModified < srcModified)
			return 0;

		return printDebugCode("generateTargetFile(" + srcFile + ") skip generate " + tgtFile, (+1));
	}
	/**
	 * "Parent" package prefix - inherited interfaces that do not share this
	 * common prefix are not sub-processed, but rather assumed to already
	 * exist, and are automatically imported - see "-pkgparent" option
	 */
	private String	_pkgParent	/* =null */;
	/**
	 * @param pkgParent "-pkgparent" option - may NOT be null/empty
	 * @return 0 if successful
	 */
	protected int setParentPackage (final String pkgParent)
	{
		if ((null == pkgParent) || (pkgParent.length() <= 0))
			return printErrorCode("setParentPackage(" + pkgParent + ") bad/illegal value", (-1));

		if (_pkgParent != null)
		{
			// allow (but warn) if re-set to SAME value
			if (_pkgParent.equals(pkgParent))
				return printWarningCode("setParentPackage(" + pkgParent + ") re-set to same value", 0);
			else
				return printErrorCode("setParentPackage(" + pkgParent + ") already set to " + _pkgParent, (-2));	
		}
		else
		{
			_pkgParent = printDebugObject("setParentPackage(" + pkgParent + ")", pkgParent);
			return 0;
		}
	}
	/**
	 * @param pkgName package name to be considered - may NOT be null/empty
	 * @return 0=sub-process the element, >0=skip the element, <0=error
	 */
	protected int isParentPackage (final String pkgName)
	{
		final int	pnLen=(null == pkgName) ? 0 : pkgName.length();
		if (pnLen <= 0)
			return printErrorCode("isParentPackage(" + pkgName + ") bad/illegal argument", (-1));

		final int	pkLen=(null == _pkgParent) ? 0 : _pkgParent.length();
		if (pkLen <= 0)	// if not specific "parent" specified then assum "ALL"
			return 0;

		// make sure package name STARTS with the prefix
		if (pkgName.startsWith(_pkgParent))
		{
			// if prefix shorter than actual name, then make sure '.' immediately follows it
			if (pkLen < pnLen)
			{
				if (pkgName.charAt(pkLen) != '.')
					return (+1);
			}

			return 0;
		}

		return (+1);
	}
	/**
	 * @param pd package to be considered - may NOT be null
	 * @return 0=sub-process the element, >0=skip the element, <0=error
	 */
	protected int isParentPackage (final PackageDoc pd)
	{
		return isParentPackage((null == pd) ? null : pd.name());
	}
	/**
	 * @param pe program element to be considered (usually a {@link ClassDoc})
	 * - may NOT be null
	 * @return 0=sub-process the element, >0=skip the element, <0=error
	 */
	protected int isParentPackage (final ProgramElementDoc pe)
	{
		return isParentPackage((null == pe) ? null : pe.containingPackage());
	}
	/**
	 * @param optVals "-pkgparent" argument(s) - if any
	 * @param offset offset of argument(s) in array - may NOT be negative
	 * @param len number of arguments - MUST be EXACTLY ONE
	 * @return 0 if successful
	 */
	protected int setParentPackage (final String[] optVals, final int offset, final int len)
	{
		if ((null == optVals) || (offset < 0) || (len != 1) /* exactly ONE argument expected */ || ((offset + len) > optVals.length))
			return printErrorCode("setParentPackage() bad/illegal arguments range [" + offset + "-" + (offset + len), (-1));

		return setParentPackage(optVals[offset]);
	}
	/**
	 * Cached handler - re-used every time it is needed.
	 */
	private MBeanMethodsHandler	_mmHandler	/* =null */;
	/**
	 * @return lazy creates {@link #_mmHandler} or resets it if already created
	 */
	protected MBeanMethodsHandler getMethodsHandler ()
	{
		if (null == _mmHandler)
			_mmHandler = new MBeanMethodsHandler(this);
		else
			_mmHandler.reset();

		return _mmHandler;
	}
	/**
	 * Resolves if attribute is "read-only"/"read-write"/"write-only"
	 * @param aDesc attribute descriptor - may NOT be null
	 * @param md original "is/get/set" method
	 * @param cmm attributes/methods map - key=method "key",
	 * value={@link MethodDoc} - may NOT be null/empty
	 * @return if no matching set/get-ter then same as input method, otherwise
	 * the matching method (null if error). From this result, the access can
	 * be inferred as follows:</P></BR>
	 * 
	 * 		Original	Return		Access
	 * 		Method		Value		Type
	 * 		========	======		======
	 * 		 getter		 same		read-only
	 * 		 getter		 other		read-write
	 * 		 setter		 same		write-only
	 * 		 setter		 other		read-write
	 */
	protected static MethodDoc resolveAttributeAccess (final AttrDescriptor aDesc, final MethodDoc md, final ClassMethodsMap cmm)
	{
		final String	aName=(null == aDesc) /* should not happen */ ? null : aDesc.getName();
		if ((null == aName) || (aName.length() <= 0) || (null == md) || (null == cmm) || (cmm.size() <= 0))
			return null;

		MethodDoc	retVal=null;
		if (aDesc.isGetter())
		{
			retVal = cmm.get("set" + aName, new String[] { md.returnType().qualifiedTypeName() });
		}
		else	// setter => check both options ("get/is")
		{
			if (null == (retVal=cmm.get("get" + aName, (String[]) null)))
				retVal = cmm.get("is" + aName, (String[]) null);
		}

		if (null == retVal)
			return md;
		else
			return retVal;
	}
	/**
	 * @param gMethod getter method - preferred if non-null and non-empty comment
	 * @param sMethod setter method - second best if non-null and non-empty comment
	 * @return attribute comment text (null if error)
	 */
	protected static final String resolveAttributeDescription (final MethodDoc gMethod, final MethodDoc sMethod)
	{
		final String	aDesc=DocletUtil.compactAttributeDescription(gMethod);
		if ((null == aDesc) || (aDesc.length() <= 0))
			return (null == sMethod) /* should not happen */ ? null : DocletUtil.compactAttributeDescription(sMethod);
		else
			return aDesc;
	}
	/**
	 * Cached (lazy created) attribute(s) descriptor 
	 */
	private AttrDescriptor	_aDesc	/* =null */;
	protected AttrDescriptor getAttrDescriptor ()
	{
		if (null == _aDesc)
			_aDesc = new AttrDescriptor();
		else
			_aDesc.reset();

		return _aDesc;
	}
	/**
	 * Map of classes that are <U>candidates</U> for processing and whose
	 * processed information is available. Key=class qualified name,
	 * value=associated ClassDoc
	 */
	private final Map<String,ClassDoc>	_classesMap=new TreeMap<String,ClassDoc>();
	// known/useful options strings
	public static final String	TARGETDIR_OPT="-targetdir",
								SRCDIR_OPT="-srcroot",
								OVERWRITE_OPT="-overwrite",
								PKGPARENT_OPT="-pkgparent",
								VERBLEVEL_OPT="-verblevel",
									VERBDEBUG_LEVEL="debug",
									VERBVERBOSE_LEVEL="verbose",
								EXTRACALSSES_OPT="-extraclasses",
								SCCREVISION_OPT="-sccrevid",
								SCCURL_OPT="-sccurl";
	/**
	 * Useful/necessary options - even index=option name (with '-'), odd
	 * index=explanation text 
	 */
	protected static String[]	_opts={
		TARGETDIR_OPT,		"Location where XML files should be generated - MUST be specified",
		SRCDIR_OPT,			"Root location of packages sources - recommended",
		PKGPARENT_OPT,		"Common package(s) prefix to be sub-processed - if not specified, then assumed ALL or -subpackages argument",
		EXTRACALSSES_OPT,	"Comma delimite list of extra interfaces to be parsed even though not ending in \"MBean\"",
		OVERWRITE_OPT,		"TRUE=generate XML even if source file older than (existing) target file (default=FALSE)",
		VERBLEVEL_OPT,		"DEBUG/VERBOSE verbosity level (default=none)",
		SCCREVISION_OPT,		"Source-Control revision (default=none)",
		SCCURL_OPT,			"Source-Control URL (default=none)"
	};
	// displays the useful/necessary options
	protected static void showDocletOptions (final DocErrorLevel lvl)
	{
		final int	numOpts=(null == _opts) /* should not happen */ ? 0 : _opts.length;
		for (int	oIndex=0; oIndex < numOpts; oIndex += 2)
		{
			final String	optName=_opts[oIndex], optDesc=_opts[oIndex+1];
			report(lvl, "\t" + optName + " - " + optDesc);
		}
	}
	/**
	 * @param vLevel the requested verbosity level - may NOT be null/empty
	 * @return 0 if successful
	 */
	protected int setVerbosityLevel (final String vLevel)
	{
		if ((null == vLevel) || (vLevel.length() <= 0))
			return printErrorCode("setVerbosityLevel(" + vLevel + ") bad/illegal level", (-1));

		if (VERBDEBUG_LEVEL.equalsIgnoreCase(vLevel))
		{
			if (isDebugEnabled())
				printWarning("setVerbosityLevel(" + vLevel + ") already set");
			else
				setDebugEnabled(true);
		}
		else if (VERBVERBOSE_LEVEL.equalsIgnoreCase(vLevel))
		{
			if (isVerboseEnabled())
				printWarning("setVerbosityLevel(" + vLevel + ") already set");
			else
				setVerboseEnabled(true);
		}
		else
			return printErrorCode("setVerbosityLevel(" + vLevel + ") unknown level", (-2));

		return 0;
	}
	/**
	 * @param optVals "-verblevel" argument(s) - if any
	 * @param offset offset of argument(s) in array - may NOT be negative
	 * @param len number of arguments - MUST be EXACTLY ONE
	 * @return 0 if successful
	 */
	protected int setVerbosityLevel (final String[] optVals, final int offset, final int len)
	{
		if ((null == optVals) || (offset < 0) || (len != 1) /* exactly ONE argument expected */ || ((offset + len) > optVals.length))
			return printErrorCode("setVerbosityLevel() bad/illegal arguments range [" + offset + "-" + (offset + len), (-1));

		return setVerbosityLevel(optVals[offset]);
	}
	/**
	 * "-subpackages" handler
	 * @param subPackages last known value (may be null/empty)
	 * @param newVal new value to be set - may NOT be null/empty
	 * @return extracted value (null/empty if error)
	 */
	protected String setSubPackages (final String subPackages, final String newVal)
	{
		if ((subPackages != null) && (subPackages.length() > 0))
		{
			if (subPackages.equals(newVal))
				return printWarningObject("setSubPackages(" + newVal + ") re-set", newVal);
			else
				return printWarningObject("setSubPackages(" + newVal + ") already set to " + subPackages, null);
		}
		else
			return printDebugObject("setSubPackages(" + newVal + ")", newVal);
	}
	/**
	 * "-subpackages" handler
	 * @param subPackages last known value (may be null/empty)
	 * @param optVals "-subpackages" argument(s) - if any
	 * @param offset offset of argument(s) in array - may NOT be negative
	 * @param len number of arguments - MUST be EXACTLY ONE
	 * @return extracted value (null/empty if error)
	 */
	protected String setSubPackages (final String subPackages, final String[] optVals, final int offset, final int len)
	{
		if ((null == optVals) || (offset < 0) || (len != 1) /* exactly ONE argument expected */ || ((offset + len) > optVals.length))
			return (String) printErrorObject("setSubPackages() bad/illegal arguments range [" + offset + "-" + (offset + len), null);

		return setSubPackages(subPackages, optVals[offset]);
	}
	/**
	 * If non-null/empty then contains <U>regular expression(s)</U> name(s) of
	 * "extra" interfaces to be parsed even though they do not end in "MBean"
	 */
	private Collection<String>	_extraClasses	/* =null */;
	/**
	 * @param xtraClass <U>fully qualified</U> name of extra class - OK
	 * if null/empty (nothing done)
	 * @return 0 if successfully mapped it (OK if already exists)
	 */
	protected int addExtraClass (final String xtraClass)
	{
		final String	clsPath=(null == xtraClass) ? null : xtraClass.trim();
		if ((null == clsPath) || (clsPath.length() <= 0))
			return 0;	// OK

		if (clsPath.endsWith(MBEAN_SUFFIX))	// OK but warn about it
			return printWarningCode("addExtraClass(" + clsPath + ") no need for EXTRA option", 0);

		if ("none".equalsIgnoreCase(clsPath))
			return 0;	// OK

		if (null == _extraClasses)
			_extraClasses = new LinkedList<String>();
		_extraClasses.add(clsPath);

		return 0;
	}
	/**
	 * Checks if specified class path matches one of the specified extra
	 * regular expressions.
	 * @param clsPath requested class path
	 * @return pattern that matched the class path - null/empty if no match
	 */
	protected String findExtraClass (final String clsPath)
	{
		if ((null == clsPath) || (clsPath.length() <= 0)	// should not happen
		 || (null == _extraClasses) || (_extraClasses.size() <= 0))
			return null;

		for (final String pat : _extraClasses)
		{
			if ((null == pat) || (pat.length() <= 0))
				continue;	// should not happen
			if (pat.equals(clsPath))	// first check simple equality
				return pat;
		
			if (clsPath.matches(pat))
				return pat;
		}

		return null;	// no match found
	}
	/**
	 * "-extraclasses" handler
	 * @param extraList "-extraclasses" argument(s) - if any - may be NULL/empty
	 * @return 0 if successful
	 */
	protected int setExtraClasses (final String extraList)
	{
		final int	elLen=(null == extraList) ? 0 : extraList.length();
		int			lastPos=0;
		for (int	curPos=(elLen > 0) ? extraList.indexOf(',') : (-1); (curPos > lastPos) && (curPos < elLen); curPos=extraList.indexOf(',', lastPos))
		{
			final int	nErr=addExtraClass(extraList.substring(lastPos, curPos));
			if (nErr != 0)
				return nErr;

			if ((lastPos=curPos+1) >= elLen)
				break;
		}

		if ((lastPos < elLen) && (elLen > 0))
			return addExtraClass(extraList.substring(lastPos));

		return 0;
	}
	/**
	 * "-extraclasses" handler
	 * @param optVals "-extraclasses" argument(s) - if any
	 * @param offset offset of argument(s) in array - may NOT be negative
	 * @param len number of arguments - MUST be EXACTLY ONE
	 * @return 0 if successful
	 */
	protected int setExtraClasses (final String[] optVals, final int offset, final int len)
	{
		if ((null == optVals) || (offset < 0) || (len != 1) /* exactly ONE argument expected */ || ((offset + len) > optVals.length))
			return printErrorCode("setExtraClasses() bad/illegal arguments range [" + offset + "-" + (offset + len), (-1));

		return setExtraClasses(optVals[offset]);
	}
	/**
	 * "-srcroot" specification - may be null
	 */
	private File	_srcDir	/* =null */;
	/**
	 * @param cd class element
	 * @return source file path (null if error)
	 */
	protected File getSourceFilePath (final ClassDoc cd)
	{
		if (null == _srcDir)	// if no source directory, then use source position
			return DocletUtil.getSourceFilePath(cd);

		final String	qfPath=DocletUtil.getSourceFilePath(_srcDir, cd);
		if ((null == qfPath) || (qfPath.length() <= 0))
			return null;

		return new File(qfPath);
	}
	/**
	 * Sets the target dir - it <U>must</U> refer to an <U>existing</U> folder
	 * and it must be a <B>directory</B>.
	 * @param srcDir "-srcroot" argument to be set - may NOT be null/empty
	 * @return 0 if successful
	 */
	protected int setSourceDir (final String srcDir)
	{
		if ((null == srcDir) || (srcDir.length() <= 0))
			return printErrorCode("setSourceDir(" + srcDir + ") bad/illegal path", (-1));

		// check if already have the option
		if (_srcDir != null)
		{
			final String	orgPath=_srcDir.getAbsolutePath();
			if (!orgPath.equalsIgnoreCase(srcDir))
				return printErrorCode("setSourceDir(" + srcDir + ") re-specified: " + orgPath, (-2));

			// complain if re-specified with SAME value
			printWarning("setSourceDir(" + srcDir + ") re-specified (same)");
		}
		else
		{
			_srcDir = new File(srcDir);

			if ((!_srcDir.exists()) || (!_srcDir.isDirectory()))
				return printErrorCode("setTargetDir(" + srcDir + ") not a directory or does not exist", (-3));
		}

		return 0;
	}
	/**
	 * @param optVals "-srcroot" argument(s) - if any
	 * @param offset offset of argument(s) in array - may NOT be negative
	 * @param len number of arguments - MUST be EXACTLY ONE
	 * @return 0 if successful
	 */
	protected int setSourceDir (final String[] optVals, final int offset, final int len)
	{
		if ((null == optVals) || (offset < 0) || (len != 1) /* exactly ONE argument expected */ || ((offset + len) > optVals.length))
			return printErrorCode("setSourceDir() bad/illegal arguments range [" + offset + "-" + (offset + len), (-1));

		return setSourceDir(optVals[offset]);
	}
	/**
	 * Source control revision ID - may be null/emtpy 
	 */
	private String	_sccRevId	/* =null */;
	protected final String getSourceControlRevisionID ()
	{
		return _sccRevId;
	}

	protected int setSourceControlRevisionID (final String revId)
	{
		if (_sccRevId != null)
		{
			if (!_sccRevId.equals(revId))
				return printErrorCode("setSourceControlRevisionID(" + revId + ") already set to " + _sccRevId, (-1)); 
		}
		else
			_sccRevId = revId;

		return 0;
	}
	/**
	 * "-sccrevid" handler
	 * @param optVals "-sccrevid" argument(s) - if any
	 * @param offset offset of argument(s) in array - may NOT be negative
	 * @param len number of arguments - MUST be EXACTLY ONE
	 * @return 0 if successful
	 */
	protected int setSourceControlRevisionID (final String[] optVals, final int offset, final int len)
	{
		if ((null == optVals) || (offset < 0) || (len != 1) /* exactly ONE argument expected */ || ((offset + len) > optVals.length))
			return printErrorCode("setSourceControlRevisionID() bad/illegal arguments range [" + offset + "-" + (offset + len), (-1));
		
		return setSourceControlRevisionID(optVals[offset]);
	}
	/**
	 * Source control URL location - may be null/empty 
	 */
	private String	_sccURL	/* =null */;
	protected final String getSourceControlURL ()
	{
		return _sccURL;
	}

	protected int setSourceControlURL (final String sccUrl)
	{
		if (_sccURL != null)
		{
			if (!_sccURL.equalsIgnoreCase(sccUrl))
				return printErrorCode("setSourceControlURL(" + sccUrl + ") already set to " + _sccURL, (-1)); 
		}
		else
			_sccURL = sccUrl;

		return 0;
	}
	/**
	 * "-sccurl" handler
	 * @param optVals "-sccurl" argument(s) - if any
	 * @param offset offset of argument(s) in array - may NOT be negative
	 * @param len number of arguments - MUST be EXACTLY ONE
	 * @return 0 if successful
	 */
	protected int setSourceControlURL (final String[] optVals, final int offset, final int len)
	{
		if ((null == optVals) || (offset < 0) || (len != 1) /* exactly ONE argument expected */ || ((offset + len) > optVals.length))
			return printErrorCode("setSourceControlURL() bad/illegal arguments range [" + offset + "-" + (offset + len), (-1));
		
		return setSourceControlURL(optVals[offset]);
	}
	/**
	 * @param options tool invocation options
	 * @return 0 if successful
	 */
	protected int setOptions (final String[][] options)
	{
		final int	numOptions=(null == options) ? 0 : options.length;
		String		subPackages=null;
		for (int	oIndex=0; oIndex < numOptions; oIndex++)
		{
			final String[]	optVals=options[oIndex];
			final int		numVals=(null == optVals) /* should not happen */ ? 0 : optVals.length;
			final String	optName=(numVals <= 0) ? null : optVals[0];
			final int		onLen=(null == optName) ? 0 : optName.length();
			if (onLen <= 0)
				continue;	// should not happen

			// each option we are interested has EXACTLY one argument
			if ((optName.charAt(0) != '-') || (numVals != 2))
				continue;

			final int	nErr;
			if (TARGETDIR_OPT.equalsIgnoreCase(optName))
				nErr = setTargetDir(optVals, 1, numVals - 1);
			else if (SRCDIR_OPT.equalsIgnoreCase(optName))
				nErr = setSourceDir(optVals, 1, numVals - 1);
			else if (OVERWRITE_OPT.equalsIgnoreCase(optName))
				nErr = setOverwrite(optVals, 1, numVals - 1);
			else if (VERBLEVEL_OPT.equalsIgnoreCase(optName))
				nErr = setVerbosityLevel(optVals, 1, numVals - 1);
			else if (PKGPARENT_OPT.equalsIgnoreCase(optName))
				nErr = setParentPackage(optVals, 1, numVals - 1);
			else if (EXTRACALSSES_OPT.equalsIgnoreCase(optName))
				nErr = setExtraClasses(optVals, 1, numVals - 1);
			else if (SCCREVISION_OPT.equalsIgnoreCase(optName))
				nErr = setSourceControlRevisionID(optVals, 1, numVals - 1);
			else if (SCCURL_OPT.equalsIgnoreCase(optName))
				nErr = setSourceControlURL(optVals, 1, numVals - 1);
			else if ("-subpackages".equalsIgnoreCase(optName))
			{
				if ((null == (subPackages=setSubPackages(subPackages, optVals, 1, numVals - 1))) || (subPackages.length() <= 0))
					nErr = (-5);
				else
					nErr = 0;
			}
			else	// OK if not an option we care about
				nErr = 0;

			if (nErr != 0)
				return nErr;
		}

		// check that mandatory options have been specified
		if (null == _targetDir)
			return printErrorCode("missing " + TARGETDIR_OPT + " option", (-2));
			
		// set default(s) for un-specified (non-mandatory) options
		if ((null == _pkgParent) && (subPackages != null) && (subPackages.length() > 0))
			_pkgParent = printVerboseObject("set " + PKGPARENT_OPT + "=" + subPackages, subPackages);
		if (null == _isOverwrite)
			_isOverwrite = Boolean.FALSE;

		return 0;
	}
	/**
	 * @param cd class to be checked
	 * @return TRUE if it makes any sense to import/sub-process this class
	 */
	protected abstract boolean isImportableSuperclass (final ClassDoc cd);
	/**
	 * @param out output stream to write to
	 * @param ifs extended interfaces - may be null/empty (and even contain empty elements)
	 * @return number of generated imports - negative if fails
	 */
	protected abstract int generateImports (final PrintStream out, final ClassDoc[] ifs);
	/**
	 * Called by {@link #writeTargetFile(File, ClassDoc, ClassDoc[], MBeanMethodsHandler)}
	 * immediately after opening the output stream to enable some "prefix" to be written
	 * before adding the MBean(s) attribute, operations, etc.
	 * @param tgtFile target file location - may NOT be null
	 * @param cd class (interface) for which the target is generated - may NOT be null
	 * @param out output stream - may NOT be null
	 * @return 0 if successful
	 */
	protected abstract int generateTargetFilePrefix (final File tgtFile, final ClassDoc cd, final PrintStream out);
	/**
	 * @param md {@link MethodDoc} whose tags are scanned for extension tags.
	 * If such are found, then they must contain only "name=value" pairs
	 * (otherwise an error message is displayed and the tag is ignored)
	 * @return {@link Collection} of extracted extension tags and their
	 * attributes (may be null/empty if no such tags/pairs found)
	 */
	protected Collection<ExtendedAttributesTagMap> getExtensionTagsAttributes (final MethodDoc md)
	{
		final Tag[]	aTags=(null == md) ? null : md.tags();
		if ((null == aTags) || (aTags.length <= 0))
			return null;

		Collection<ExtendedAttributesTagMap>	xAttrs=null;
		for (final Tag t : aTags)
		{
			if (!DocletUtil.isTagExtension(t))
				continue;	// ignore non-extension tags

			try
			{
				final ExtendedAttributesTagMap	xMap=new ExtendedAttributesTagMap(t);
				if (xMap.size() > 0)
				{
					if (null == xAttrs)
						xAttrs = new LinkedList<ExtendedAttributesTagMap>();
					xAttrs.add(xMap);
				}
			}
			catch(Exception e)
			{
				printError(e.getClass().getName() + " while build tag=" + t.name() + " attributes for method=" + md + ": " + e.getMessage());
			}
		}

		return xAttrs;
	}
	/**
	 * @param out output stream to write to - may NOT be null
	 * @param aDesc attribute descriptor - may NOT be null
	 * @param md method whose (attribute) entry is to be written - may NOT be null
	 * @param cmm methods map - key=method "key", value={@link MethodDoc}.
	 * May be null/empty (if no attributes)
	 * @return non-null if successful
	 */
	protected abstract String generateAttributeEntry (final PrintStream out, final AttrDescriptor aDesc, final MethodDoc md, final ClassMethodsMap cmm);  
	/**
	 * Generates attributes entries
	 * @param out output stream to write to - may NOT be null
	 * @param cmm methods map - key=method "key", value={@link MethodDoc}.
	 * May be null/empty (if no attributes)
	 * @return number of generated attribute entries - negative otherwise
	 */
	protected int generateAttributes (final PrintStream out, final ClassMethodsMap cmm)
	{
		if (null == out)	// should not happen
			return Integer.MIN_VALUE;

		final Collection<MethodDoc>	attrs=((null == cmm) || (cmm.size() <= 0)) /* OK */ ? null : cmm.values();
		final int					numAttrs=(null == attrs) ? 0 : attrs.size();
		final Iterator<MethodDoc>	aIter=(numAttrs <= 0) /* OK */ ? null : attrs.iterator();
		if (null == aIter)
			return 0;

		out.println("\t<section name=\"attributes\">");

		// processed attributes map - key=attribute name (without "set/get/is"), value=attribute access
		for (Map<String,String>	aMap=null; aIter.hasNext(); )
		{
			final MethodDoc	md=aIter.next();
			if (null == md)
				continue;	// should not happen

			final AttrDescriptor	aDesc=MBeanMethodsHandler.getAttributeDescriptor(md, getAttrDescriptor());
			if (null == aDesc)
				return printErrorCode("generateAttributes(" + md + ") non-attribute method", (-1));

			final String	attrName=aDesc.getName();
			if ((aMap != null) && (aMap.get(attrName) != null))
				continue;	// skip if already processed this attribute

			final String	aAccess=generateAttributeEntry(out, aDesc, md, cmm);
			if ((null == aAccess) || (aAccess.length() <= 0))	// should not happen
				return printErrorCode("generateAttributes(" + md + ") cannot resolve attribute access", (-2));

			if (null == aMap)
				aMap = new HashMap<String,String>(numAttrs, 1.0f);
			aMap.put(attrName, aAccess);
		}

		out.println("\t</section>");
		return numAttrs;
	}
	/**
	 * Generates specific operation parameters entries
	 * @param md method for which parameters entries is generated
	 * @param out output stream to write to - may NOT be null
	 * @param params parameters we want to generate - may be null/empty (and
	 * even contain null/empty elements)
	 * @param tags array assumed to contain matching entries - if not, then
	 * some default is created. <B>Note:</B> this CAN happen for undocumented
	 * methods
	 * even contain empty elements)
	 * @return 0 if successful
	 */
	protected abstract int generateOperationParameters (final MethodDoc md, final PrintStream out, final Parameter[] params, final ParamTag[] tags);
	/**
	 * Called by {@link #generateOperations(PrintStream, ClassMethodsMap)} for
	 * each operation method to enable a "prefix" before calling {@link #generateOperationParameters(MethodDoc, PrintStream, Parameter[], ParamTag[])}
	 * @param out output stream to write to - may NOT be null
	 * @param md operation method - may NOT be null
	 * @param params operation parameters - may be null/empty
	 * @return 0 if successful
	 */
	protected abstract int printOperationEntryPrefix (final PrintStream out, final MethodDoc md, final Parameter... params);
	/**
	 * Called by {@link #generateOperations(PrintStream, ClassMethodsMap)} for
	 * each operation method to enable a "suffix" after calling {@link #generateOperationParameters(MethodDoc, PrintStream, Parameter[], ParamTag[])}
	 * @param out output stream to write to - may NOT be null
	 * @param md operation method - may NOT be null
	 * @param params operation parameters - may be null/empty
	 * @return 0 if successful
	 */
	protected abstract int printOperationEntrySuffix (final PrintStream out, final MethodDoc md, final Parameter... params);
	/**
	 * Generates operations entry
	 * @param out output stream to write to - may NOT be null
	 * @param cmm methods map - key=method "key", value={@link MethodDoc}.
	 * May be null/empty (if no attributes)
	 * @return number of generated operations - negative if error
	 */
	protected int generateOperations (final PrintStream out, final ClassMethodsMap cmm)
	{
		if (null == out)	// should not happen
			return Integer.MIN_VALUE;

		final Collection<MethodDoc>	attrs=((null == cmm) || (cmm.size() <= 0)) /* OK */ ? null : cmm.values();
		final int					numOpers=(null == attrs) ? 0 : attrs.size();
		final Iterator<MethodDoc>	aIter=(numOpers <= 0) /* OK */ ? null : attrs.iterator();
		if (null == aIter)
			return 0;

		out.println("\t<section name=\"operations\">");
		while (aIter.hasNext())
		{
			final MethodDoc	md=aIter.next();
			if (null == md)
				continue;	// should not happen

			final Parameter[]	params=md.parameters();

			int	nErr=printOperationEntryPrefix(out, md, params);
			if (nErr != 0)
				return adjustErrCode(nErr);

			if ((params != null) && (params.length > 0))
			{
				if ((nErr=generateOperationParameters(md, out, params, md.paramTags())) != 0)
					return adjustErrCode(nErr);
			}
			
			if ((nErr=printOperationEntrySuffix(out, md, params)) != 0)
				return adjustErrCode(nErr);
		}

		out.println("\t</section>");
		return numOpers;
	}
	/**
	 * Called by {@link #writeTargetFile(File, ClassDoc, ClassDoc[], MBeanMethodsHandler)}
	 * immediately before closing the output stream to enable some "suffix" to be written
	 * before adding the MBean(s) attribute, operations, etc.
	 * @param tgtFile target file location - may NOT be null
	 * @param cd class (interface) for which the target is generated - may NOT be null
	 * @param out output stream - may NOT be null
	 * @return 0 if successful
	 */
	protected abstract int generateTargetFileSuffix (final File tgtFile, final ClassDoc cd, final PrintStream out);
	/**
	 * Generates the target file descriptor
	 * @param tgtFile target file location - may NOT be null
	 * @param cd class (interface) for which the target is generated - may NOT be null
	 * @param ifs inherited interfaces (for generating "import" directives).
	 * May be null/empty (and even contain empty elements)
	 * @param mmHandler extracted attributes/operations - may NOT be null
	 * @return 0 if successful
	 */
	protected int writeTargetFile (final File tgtFile, final ClassDoc cd, final ClassDoc[] ifs, final MBeanMethodsHandler mmHandler)
	{
		if ((null == tgtFile) || (null == cd) || (null == mmHandler))
			return (-1);

		// if file does not already exist, then make sure all folders up to it are created
		if (!tgtFile.exists())
		{
			final File	parFolder=tgtFile.getParentFile();
			if (null == parFolder)
				return printErrorCode("writeTargetFile(" + tgtFile + ") no parent folder", (-2));
			if ((!parFolder.exists()) && (!parFolder.mkdirs()))
				return printErrorCode("writeTargetFile(" + tgtFile + ") cannot create parent folder(s)", (-3));
		}
		else if (!tgtFile.isFile())
		{
			return printErrorCode("writeTargetFile(" + tgtFile + ") not a file", (-4));
		}

		PrintStream	out=null;
		try
		{
			out = new PrintStream(new FileOutputStream(tgtFile));

			int	nErr=generateTargetFilePrefix(tgtFile, cd, out);
			if (nErr != 0)
				return nErr;
			if ((nErr=generateImports(out, ifs)) < 0)
				return nErr;
			if ((nErr=generateAttributes(out, mmHandler.getAttributes())) < 0)
				return nErr;
			if ((nErr=generateOperations(out, mmHandler.getOperations())) < 0)
				return nErr;
			if ((nErr=generateTargetFileSuffix(tgtFile, cd, out)) != 0)
				return nErr;

			return printDebugCode("writeTargetFile(" + tgtFile + ") done", 0);
		}
		catch(Exception e)
		{
			return printErrorCode("writeTargetFile(" + tgtFile + ") " + e.getClass().getName() + ": " + e.getMessage(), Integer.MIN_VALUE);
		}
		finally
		{
			if (out != null)
			{
				out.close();
				out = null;
			}
		}		
	}
	/**
	 * @param cd interface info to be processed
	 * @param curDepth current recursion level - 0=top level
	 * @return 0 if successful
	 */
	protected int processMBean (final ClassDoc cd, final int curDepth)
	{
		if ((null == cd) || (curDepth < 0))	// should not happen
			return Integer.MIN_VALUE;
		if (!cd.isInterface())	// should not happen
			return printErrorCode("processMBean(" + cd + ") not an interface", (-1));

		// limit the recursion to ~32K - more than enough - just for code robustness
		if (curDepth > Short.MAX_VALUE)
			return printErrorCode("processMBean(" + cd + ")[" + curDepth + "] recursion depth greater than max. allowed (" + Short.MAX_VALUE + ")", (-2));

		// skip if already processed this class
		if (isProcessed(cd))
			return printVerboseCode("processMBean(" + cd + ")[" + curDepth + "] skip - already processed", 0);

		int	nErr=isParentPackage(cd);
		if (nErr < 0)
			return nErr;
		if (nErr > 0)
			return printVerboseCode("processMBean(" + cd + ")[" + curDepth + "] skip - not a parent package", 0);

		// make sure (recursively) that extended interfaces have been processed
		final ClassDoc[]	ifs=cd.interfaces();
		final int			numIfs=(null == ifs) ? 0 : ifs.length;
		for (int	iIndex=0; iIndex < numIfs; iIndex++)
		{
			final ClassDoc	ic=ifs[iIndex];
			if (!isImportableSuperclass(ic))
				continue;

			if ((nErr=processMBean(ic, curDepth + 1)) != 0)
				return nErr;
		}

		// check if need to generate an XML file for the target
		final File	srcFile=getSourceFilePath(cd), xmlFile=getTargetFilePath(cd);
		if ((nErr=generateTargetFile(srcFile, xmlFile)) < 0)
			return nErr;
		if (nErr > 0)
			return printVerboseCode("processMBean(" + cd + ")[" + curDepth + "] skip - XML already up-to-date", 0);

		printDebug("processMBean(" + cd + ")[" + curDepth + "] start");
		final long					procStart=System.currentTimeMillis(); 
		final MBeanMethodsHandler	mmHandler=getMethodsHandler();
		if (null == mmHandler)	// should not happen
			return printErrorCode("processMBean(" + cd + ")[" + curDepth + "] no MBean methods handler", (-3));

		if ((nErr=mmHandler.processMethods(cd)) != 0)
			return nErr;

		// if error then delete the file
		if ((nErr=writeTargetFile(xmlFile, cd, ifs, mmHandler)) != 0)
		{
			if (xmlFile.exists() && (!xmlFile.delete()))
				printError("processMBean(" + cd + ")[" + curDepth + "] failed to delete due to err=" + nErr + " XML file=" + xmlFile);
		}

		if ((nErr=setProcessed(cd)) != 0)
			return nErr;	// should not happen

		final long	procEnd=System.currentTimeMillis(), procDuration=procEnd - procStart;
		return printNoticeCode("Processed " + cd + " in " + procDuration + " msec.", 0);
	}
	/**
	 * @param cd interface info to be processed
	 * @return 0 if successful
	 */
	protected int processMBean (final ClassDoc cd)
	{
		return processMBean(cd, 0);
	}
	/**
	 * @param classes processed classes Javadoc comments
	 * @return 0 if successful
	 */
	protected int start (final ClassDoc[] classes)
	{
        final int	numClasses=(null == classes) ? 0 : classes.length;
        for (int i = 0; i < numClasses; ++i)
        {
            final ClassDoc	cd=classes[i];
            if ((null == cd) // should not happen
             || (!cd.isInterface())) // we are interested only in interface(s)
            	continue;

            final String	qfName=cd.qualifiedName();
           	if ((null == qfName) || (qfName.length() <= 0))	// should not happen
           	{
           		printError(cd.position(), "no qualified name for " + cd);
           		continue;
           	}

            if (_classesMap.put(qfName, cd) != null)	// should not happen
            {
            	printError(cd.position(), "multiples instances for " + qfName);
            	return (-1);	// TODO review this decision
            }
        }

        final Collection<Map.Entry<String,ClassDoc>>	eSet=((null == _classesMap) || (_classesMap.size() <= 0)) /* should not happen */ ? null : _classesMap.entrySet();
        if ((eSet != null) && (eSet.size() > 0))
        {
        	for (final Map.Entry<String,ClassDoc> e : eSet)
        	{
        		final String	qfName=(null == e) ? null : e.getKey();
        		final ClassDoc	cd=(null == e) ? null : e.getValue();
	        	if ((null == cd) 		// should not happen
	        	 || (null == qfName) || (qfName.length() <= 0)	// should not happen
	           	 || (!cd.isPublic())	// we process only PUBLIC interfaces
	        	 || isProcessed(cd))	// skip if already processed the file
	        		continue;

	        	// if not an MBean suffix interface, then check if one of the EXTRA classes
	        	if (!qfName.endsWith(MBEAN_SUFFIX))
	        	{
	        		final String	pat=findExtraClass(qfName);
	        		if ((null == pat) || (pat.length() <= 0))
	        			continue;
	        	}

	        	final int	nErr=processMBean(cd);
	        	if (nErr != 0)
	        		return printErrorCode(cd.position(), " failed (err=" + nErr + ") to process class=" + qfName, nErr);
	        }
        }

        return 0;
	}
	/*
	 * @see com.sun.javadoc.Doclet#optionLength(java.lang.String)
	 */
	public static int optionLength (final String option)
	{
		if ((null == option) || (option.length() <= 0))
			return 0;	// should not happen

		// all our '-' options have EXACTLY ONE argument
		if ('-' == option.charAt(0))
			return 2;
		else
			return 1;
	}
	/**
	 * Should be called by the doclet's "start" (static) method
	 * @param mbDoc doclet instance (may NOT be null)
	 * @param root root document
	 * @return 0 if successful
	 */
	protected static int start (final AMBeanDoclet mbDoc, final RootDoc root)
	{
		if (null == mbDoc)
			return reportErrCode(DocErrorLevel.ERROR, "No doclet instance", (-1));

		if (null == root)
			return reportErrCode(DocErrorLevel.ERROR, "No root document", (-2));

		final int	nErr=mbDoc.start(root.classes());
		if (nErr != 0)
		{
			report(DocErrorLevel.ERROR, "Failed (err=" + nErr + ") to process classes");
			System.exit(nErr);
		}

		return nErr;
	}

	protected static final int validOptions (final AMBeanDoclet mbDoc, final String[][] options, final DocErrorReporter reporter)
	{
		setStaticReporter(reporter);

		if (null == mbDoc)
			return reportErrCode(DocErrorLevel.ERROR, "No doclet instance", (-1));

		final int	nErr=mbDoc.setOptions(options);
		if (nErr != 0)
		{
			report(DocErrorLevel.ERROR, "failed (err=" + nErr + ") to validate options");
			showDocletOptions(DocErrorLevel.ERROR);
		}

		return nErr;
	}
}
