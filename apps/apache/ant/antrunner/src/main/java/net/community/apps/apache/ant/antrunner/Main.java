package net.community.apps.apache.ant.antrunner;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import net.community.apps.common.BaseMain;
import net.community.chest.CoVariantReturn;
import net.community.chest.apache.ant.helpers.AntUtils;
import net.community.chest.util.logging.factory.WrapperFactoryManager;
import net.community.chest.util.logging.factory.empty.EmptyLoggerWrapperFactory;

/**
 * <P>Copyright 2007 as per GPLv2</P>
 *
 * <P>Provides a GUI for running ANT build scripts</P>
 * @author Lyor G.
 * @since Jul 22, 2007 8:35:49 AM
 */
public final class Main extends BaseMain {
	private Main (final String ... args)
	{
		super(args);
	}
	/*
	 * @see net.community.apps.common.BaseMain#createMainFrameInstance()
	 */
	@Override
	@CoVariantReturn
	protected MainFrame createMainFrameInstance () throws Exception
	{
		return new MainFrame(getMainArguments());
	}

	private static final ClassLoader processMainArgs (final ClassLoader	clThread, final String... args)
	{
    	final int	numArgs=(null == args) ? 0 : args.length;
    	for (int aIndex=0; aIndex < numArgs; aIndex++)
    	{
    		final String	arg=args[aIndex];
    		final int		argLen=(null == arg) ? 0 : arg.length();
    		if ((argLen > 4) && ('D' == arg.charAt(1)))
            {
            	final int	sPos=arg.indexOf('=', 2);
            	if ((sPos > 2) && (sPos < (argLen-1)))
            	{
            		final String	dName=arg.substring(2, sPos),
            						dValue=arg.substring(sPos+1);
            		if (AntUtils.DEFAULT_ANT_HOME_PROP_NAME.equalsIgnoreCase(dName))
            			return AntUtils.setupAntHome(clThread, dValue);
            	}
            }
    	}

    	return AntUtils.setupAntHome(clThread, null);	// this point is reached if no override found
	}

	//////////////////////////////////////////////////////////////////////////

	public static void main (final String[] args) throws Exception
	{
		// using empty logger factory since writing to console sometimes interferes with ANT's tasks
		WrapperFactoryManager.setFactory(new EmptyLoggerWrapperFactory());

		try
		{
			final Thread		t=Thread.currentThread();
			final ClassLoader	clThread=t.getContextClassLoader(),
								clResolved=processMainArgs(clThread, args);
			if (!AntUtils.isAccessibleAntHome(clResolved))	// make sure ANT main class accessible
				throw new ClassNotFoundException("ANT classes inaccessible");
			if (clThread != clResolved)
				t.setContextClassLoader(clResolved);
		}
		catch(Throwable t)
		{
			JOptionPane.showMessageDialog(null, t.getMessage(), t.getClass().getName(), JOptionPane.ERROR_MESSAGE);
			System.exit(1);
		}

		SwingUtilities.invokeLater(new Main(args));
	}
}
