package net.community.apps.common.test;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collection;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import net.community.chest.reflect.ClassUtil;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * <P>Generic "main" for UI tests - accepts the "relative" main frame
 * class path of the actual UI to be shown as 1st argument</P>
 * 
 * @author Lyor G.
 * @since Mar 19, 2008 11:14:39 AM
 */
public class TestMain implements Runnable {
	private final String[]	_args;
	private TestMain (final String ... args)
	{
		_args = args;
	}

	protected String getMainFrameClass (String ... args) throws Exception
	{
		final Package	pkg=getClass().getPackage();
		final String	relativeClsPath=args[0], pkgName=pkg.getName();
		return pkgName + "." + relativeClsPath;
	}

	protected String[] getMainFrameArguments (final String ... args)
	{
		if ((null == args) || (args.length <= 1))
			return null;

		if (2 == args.length)
			return new String[] { args[1] };

		final Collection<String>	al=new ArrayList<String>(args.length - 1);
		for (int aIndex=1; aIndex < args.length; aIndex++)
			al.add(args[aIndex]);

		return al.toArray(new String[al.size()]);
	}

	protected JFrame getMainFrame (final String ... args) throws Exception
	{
		final String	clsPath=getMainFrameClass(args);
		final Class<?>	frmClass=ClassUtil.loadClassByName(clsPath);
		// Check if there are any constructors that accept a string array
		// If so, then assume this constructor expected the arguments (without the 1st one)
		final Constructor<?>[]	ctors=frmClass.getConstructors();
		for (final Constructor<?> c : ctors)
		{
			final Class<?>[]	params=c.getParameterTypes();
			if ((null == params) || (params.length != 1))
				continue;	// we look of EXACTLY one argument

			final Class<?>	pType=params[0];
			if ((null == pType) || (!String[].class.isAssignableFrom(pType)))
				continue;

			final String[]	cArgs=getMainFrameArguments(args);
			return (JFrame) c.newInstance((Object) cArgs);
		}

		// this point is reached if no special constructor found
		return (JFrame) frmClass.newInstance();
	}
	/*
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run ()
	{
		try
		{
			final JFrame	frm=getMainFrame(_args);
			frm.pack();
			frm.setVisible(true);
		}
		catch(Throwable t)
		{
			System.err.println("run() " + t.getClass().getName() + ": " + t.getMessage());
		}
	}

	//////////////////////////////////////////////////////////////////////////
	// args[0]=class name - relative to this package
	public static void main (final String[] args)
	{
		try
		{
			SwingUtilities.invokeLater(new TestMain(args));
		}
		catch(Throwable t)
		{
			System.err.println("main() " + t.getClass().getName() + ": " + t.getMessage());
		}
	}
}
