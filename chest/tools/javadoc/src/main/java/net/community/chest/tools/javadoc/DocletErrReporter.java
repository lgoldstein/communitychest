package net.community.chest.tools.javadoc;

import java.io.PrintStream;

import com.sun.javadoc.DocErrorReporter;
import com.sun.javadoc.Doclet;
import com.sun.javadoc.SourcePosition;

/**
 * <P>Copyright 2007 as per GPLv2</P>
 *
 * <P>Useful base class for {@link Doclet} implementors - provides various
 * "shortcuts" for reporting errors/warnings during doclent execution</P>
 * 
 * @author Lyor G.
 * @since Aug 16, 2007 11:07:13 AM
 */
public abstract class DocletErrReporter extends Doclet implements DocErrorReporter {
	protected DocletErrReporter ()
	{
		super();
	}
	/**
	 * @param nErr original error code
	 * @return negative value of error code (if not already such)
	 */
	public int adjustErrCode (final int nErr)
	{
		if (nErr > 0)
			return (0 - nErr);
		else
			return nErr;
	}
	/**
	 * @param out print stream to output to - if null then {@link System#err} is used
	 * @param lvl level - if null/illegal then UNKNOWN/ERROR is assumed
	 * @param pos source position - if non-null then it is appened as "at XXX" after the message
	 * @param msg message string
	 */
	public static final void print (final PrintStream out, final DocErrorLevel lvl, final SourcePosition pos, final String msg)
	{
		final String	lvlName=(lvl != null) ? lvl.getName() : "UNKNOWN",
						srcPos=(null == pos) ? null : pos.toString();

		final PrintStream	ps=(null == out) ? System.err : out;
		if ((null == srcPos) || (srcPos.length() <= 0))
			ps.println(lvlName + ": " + msg);
		else
			ps.println(lvlName + ": " + msg + " at " + srcPos);
	}
	/**
	 * @param reporter reporter to use - if null then STDOUT/STDERR are used
	 * @param lvl level - if null then UNKNWON assumed
	 * @param pos source position - may be null
	 * @param msg message to display
	 */
	public static final void report (DocErrorReporter reporter, DocErrorLevel lvl, SourcePosition pos, String msg)
	{
		final DocErrorLevel	effLevel=(null == lvl) ? DocErrorLevel.ERROR : lvl;
		switch(effLevel)
		{
			case NOTICE	:
				if (reporter != null)
				{
					if (null == pos)
						reporter.printNotice(msg);
					else
						reporter.printNotice(pos, msg);
				}
				else
					print(System.out, effLevel, pos, msg);
				break;

			case WARNING	:
				if (reporter != null)
				{
					if (null == pos)
						reporter.printWarning(msg);
					else
						reporter.printWarning(pos, msg);
				}
				else
					print(System.err, effLevel, pos, msg);
				break;

			case ERROR		:
			default			:	// same as ERROR
				if (reporter != null)
				{
					if (null == pos)
						reporter.printError(msg);
					else
						reporter.printError(pos, msg);
				}
				else
					print(System.err, effLevel, pos, msg);
				break;
		}
	}
	/**
	 * Outputs to {@link System#err} as default - unless overridden
	 * @param lvl level - if null/illegal then UNKNOWN/ERROR is assumed
	 * @param pos source position - if non-null then it is appened as "at XXX" after the message
	 * @param msg message string
	 */
	public void print (DocErrorLevel lvl, SourcePosition pos, String msg)
	{
		print(null, lvl, pos, msg);
	}
	/**
	 * @param lvl level - if null/illegal then UNKNOWN/ERROR is assumed
	 * @param pos source position - if non-null then it is appened as "at XXX" after the message
	 * @param msg message string
	 * @param errCode error code to return
	 * @return same as input code
	 */
	public int printCode (DocErrorLevel lvl, SourcePosition pos, String msg, int errCode)
	{
		print(lvl, pos, msg);
		return errCode;
	}
	/**
	 * @param lvl level - if null/illegal then UNKNOWN/ERROR is assumed
	 * @param msg message string
	 * @param errCode error code to return
	 * @return same as input code
	 */
	public int printCode (DocErrorLevel lvl, String msg, int errCode)
	{
		return printCode(lvl, null, msg, errCode);
	}
	/**
	 * @param <T> The generic type of object
	 * @param lvl level - if null/illegal then UNKNOWN/ERROR is assumed
	 * @param pos source position - if non-null then it is appended as "at XXX" after the message
	 * @param msg message string
	 * @param o object to return
	 * @return same as input object
	 */
	public <T> T printObject (DocErrorLevel lvl, SourcePosition pos, String msg, T o)
	{
		print(lvl, pos, msg);
		return o;
	}
	/**
	 * @param <T> The generic type of object
	 * @param lvl level - if null/illegal then UNKNOWN/ERROR is assumed
	 * @param msg message string
	 * @param o object to return
	 * @return same as input object
	 */
	public <T> T printObject (DocErrorLevel lvl, String msg, T o)
	{
		return printObject(lvl, null, msg, o);
	}
	/*
	 * @see com.sun.javadoc.DocErrorReporter#printError(com.sun.javadoc.SourcePosition, java.lang.String)
	 */
	@Override
	public void printError (SourcePosition pos, String msg)
	{
		print(DocErrorLevel.ERROR, pos, msg);
	}
	/*
	 * @see com.sun.javadoc.DocErrorReporter#printError(java.lang.String)
	 */
	@Override
	public void printError (String msg)
	{
		printError(null, msg);
	}
	/**
	 * @param pos position where error occurred - may be null
	 * @param msg message to be printed
	 * @param errCode error code to be returned
	 * @return same as input error code
	 */
	public int printErrorCode (SourcePosition pos, String msg, int errCode)
	{
		printError(pos, msg);
		return errCode;
	}
	/**
	 * @param msg message to be printed
	 * @param errCode error code to be returned
	 * @return same as input error code
	 */
	public int printErrorCode (String msg, int errCode)
	{
		return printErrorCode(null, msg, errCode);
	}
	/**
	 * @param <T> The generic type of object
	 * @param pos position where error occurred - may be null
	 * @param msg message to be printed
	 * @param o object to be returned
	 * @return same as input object
	 */
	public <T> T printErrorObject (SourcePosition pos, String msg, T o)
	{
		printError(pos, msg);
		return o;
	}
	/**
	 * @param <T> The generic type of object
	 * @param msg message to be printed
	 * @param o object to be returned
	 * @return same as input object
	 */
	public <T> T printErrorObject (String msg, T o)
	{
		return printErrorObject(null, msg, o);
	}
	/*
	 * @see com.sun.javadoc.DocErrorReporter#printNotice(com.sun.javadoc.SourcePosition, java.lang.String)
	 */
	@Override
	public void printNotice (SourcePosition pos, String msg)
	{
		print(DocErrorLevel.NOTICE, pos, msg);
	}
	/*
	 * @see com.sun.javadoc.DocErrorReporter#printNotice(java.lang.String)
	 */
	@Override
	public void printNotice (String msg)
	{
		printNotice(null, msg);
	}
	/**
	 * @param pos position where notice occurred - may be null
	 * @param msg message to be printed
	 * @param errCode error code to be returned
	 * @return same as input error code
	 */
	public int printNoticeCode (SourcePosition pos, String msg, int errCode)
	{
		printNotice(pos, msg);
		return errCode;
	}
	/**
	 * @param msg message to be printed
	 * @param errCode error code to be returned
	 * @return same as input error code
	 */
	public int printNoticeCode (String msg, int errCode)
	{
		return printNoticeCode(null, msg, errCode);
	}
	/**
	 * @param <T> The generic type of object
	 * @param pos position where error occurred - may be null
	 * @param msg message to be printed
	 * @param o object to be returned
	 * @return same as input object
	 */
	public <T> T printNoticeObject (SourcePosition pos, String msg, T o)
	{
		printNotice(pos, msg);
		return o;
	}
	/**
	 * @param <T> The generic type of object
	 * @param msg message to be printed
	 * @param o object to be returned
	 * @return same as input object
	 */
	public <T> T printNoticeObject (String msg, T o)
	{
		return printNoticeObject(null, msg, o);
	}
	/*
	 * @see com.sun.javadoc.DocErrorReporter#printWarning(com.sun.javadoc.SourcePosition, java.lang.String)
	 */
	@Override
	public void printWarning (SourcePosition pos, String msg)
	{
		print(DocErrorLevel.WARNING, pos, msg);
	}
	/*
	 * @see com.sun.javadoc.DocErrorReporter#printWarning(java.lang.String)
	 */
	@Override
	public void printWarning (String msg)
	{
		printWarning(null, msg);
	}
	/**
	 * @param pos position where warning occurred - may be null
	 * @param msg message to be printed
	 * @param errCode error code to be returned
	 * @return same as input error code
	 */
	public int printWarningCode (SourcePosition pos, String msg, int errCode)
	{
		printWarning(pos, msg);
		return errCode;
	}
	/**
	 * @param msg message to be printed
	 * @param errCode error code to be returned
	 * @return same as input error code
	 */
	public int printWarningCode (String msg, int errCode)
	{
		return printWarningCode(null, msg, errCode);
	}
	/**
	 * @param <T> The generic type of object
	 * @param pos position where error occurred - may be null
	 * @param msg message to be printed
	 * @param o object to be returned
	 * @return same as input object
	 */
	public <T> T printWarningObject (SourcePosition pos, String msg, T o)
	{
		printWarning(pos, msg);
		return o;
	}
	/**
	 * @param <T> The generic type of object
	 * @param msg message to be printed
	 * @param o object to be returned
	 * @return same as input object
	 */
	public <T> T printWarningObject (String msg, T o)
	{
		return printWarningObject(null, msg, o);
	}
	/**
	 * TRUE if debug printing is enabled 
	 */
	private boolean	_debugEnabled	/* =false */;
	public boolean isDebugEnabled ()
	{
		return _debugEnabled;
	}

	public void setDebugEnabled (boolean debugEnabled)
	{
		_debugEnabled = debugEnabled;
	}
	/**
	 * Displays message as NOTICE if {@link #isDebugEnabled()}
	 * @param pos source position - may be null
	 * @param msg message
	 */
	protected void printDebug (SourcePosition pos, String msg)
	{
		if (isDebugEnabled())
			printNotice(pos, msg);
	}
	/**
	 * Displays message as NOTICE if {@link #isDebugEnabled()}
	 * @param msg message
	 */
	protected void printDebug (String msg)
	{
		printDebug(null, msg);
	}
	/**
	 * @param pos position where notice occurred - may be null
	 * @param msg message to be printed
	 * @param errCode error code to be returned
	 * @return same as input error code
	 */
	public int printDebugCode (SourcePosition pos, String msg, int errCode)
	{
		printDebug(pos, msg);
		return errCode;
	}
	/**
	 * @param msg message to be printed
	 * @param errCode error code to be returned
	 * @return same as input error code
	 */
	public int printDebugCode (String msg, int errCode)
	{
		return printDebugCode(null, msg, errCode);
	}
	/**
	 * @param <T> The generic type of object
	 * @param pos position where notice occurred - may be null
	 * @param msg message to be printed
	 * @param o object to be returned
	 * @return same as input object
	 */
	public <T> T printDebugObject (SourcePosition pos, String msg, T o)
	{
		printDebug(pos, msg);
		return o;
	}
	/**
	 * @param <T> The generic type of object
	 * @param msg message to be printed
	 * @param o object to be returned
	 * @return same as input object
	 */
	public <T> T printDebugObject (String msg, T o)
	{
		return printDebugObject(null, msg, o);
	}
	/**
	 * TRUE if verbose printing is enabled 
	 */
	private boolean	_verboseEnabled	/* =false */;
	public boolean isVerboseEnabled ()
	{
		return _verboseEnabled;
	}
	// NOTE: if TRUE then AUTOMATICALLY sets DEBUG as well
	public void setVerboseEnabled (boolean verboseEnabled)
	{
		_verboseEnabled = verboseEnabled;
		
		if (verboseEnabled)
			setDebugEnabled(true);
	}
	/**
	 * Displays message as NOTICE if {@link #isVerboseEnabled()}
	 * @param pos source position - may be null
	 * @param msg message
	 */
	protected void printVerbose (SourcePosition pos, String msg)
	{
		if (isVerboseEnabled())
			printNotice(pos, msg);
	}
	/**
	 * Displays message as NOTICE if {@link #isVerboseEnabled()}
	 * @param msg message
	 */
	protected void printVerbose (String msg)
	{
		printVerbose(null, msg);
	}
	/**
	 * @param pos position where notice occurred - may be null
	 * @param msg message to be printed
	 * @param errCode error code to be returned
	 * @return same as input error code
	 */
	public int printVerboseCode (SourcePosition pos, String msg, int errCode)
	{
		printVerbose(pos, msg);
		return errCode;
	}
	/**
	 * @param msg message to be printed
	 * @param errCode error code to be returned
	 * @return same as input error code
	 */
	public int printVerboseCode (String msg, int errCode)
	{
		return printVerboseCode(null, msg, errCode);
	}
	/**
	 * @param <T> The generic type of object
	 * @param pos position where notice occurred - may be null
	 * @param msg message to be printed
	 * @param o object to be returned
	 * @return same as input object
	 */
	public <T> T printVerboseObject (SourcePosition pos, String msg, T o)
	{
		printVerbose(pos, msg);
		return o;
	}
	/**
	 * @param <T> The generic type of object
	 * @param msg message to be printed
	 * @param o object to be returned
	 * @return same as input object
	 */
	public <T> T printVerboseObject (String msg, T o)
	{
		return printVerboseObject(null, msg, o);
	}
	/**
	 * "Copies" the verbosity level from the supplied reporter
	 * @param reporter reporter from which to update the verbosity level(s)
	 * (if null, then nothing is done)
	 * @return TRUE if something copied
	 */
	public boolean updateVerbosityLevel (DocletErrReporter reporter)
	{
		if (reporter != null)
		{
			if (reporter.isVerboseEnabled())
				setVerboseEnabled(true);
			else if (reporter.isDebugEnabled())
				setDebugEnabled(true);
			else
				return false;
			
			return true;
		}

		return false;
	}
	/**
	 * Initialized constructor
	 * @param reporter reporter to be used - may be null. If not, then its
	 * debug/verbose state is "copied" to this object as well
	 */
	protected DocletErrReporter (DocletErrReporter reporter)
	{
		this();
		updateVerbosityLevel(reporter);
	}
}
